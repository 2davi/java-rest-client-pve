const { createApp, ref, computed, onMounted, onUnmounted } = Vue;

const app = createApp({
	setup() {
		/* 변수 선언 */
		//01. 반응형 상태 변수
		const taskList = ref([]);
		const taskLogs = ref([]);
		const isPolling = ref(false);
		const targetNode = ref('lab');
		const targetUpid = ref('');
		const jwtToken = ref('');
		//let taskPollingTimer = null;
		let logPollingTimer = null;
		
		const vmList = ref([]);
		
		let vmPollingTimer = null;
		const formatUptime = seconds => {
			if(!seconds || seconds <= 0) return '-';
			const floored = Math.floor(seconds / 10) * 10;
			
			const h = Math.floor(floored/3600);
			const m = Math.floor((floored%3600) / 60);
			const s = floored%60;
			
			let result = '';
			if(h>0) result += `${h}시간 `;
			if(m>0) result += `${m}분 `;
			result += `${s}초`;
			
			return result.trim();
		};
		const startWatchingVmList = () => {
			if(vmPollingTimer) return;
			vmPollingTimer = setInterval(fetchVmList, 10000);
		};
		const stopWatchingVmList = () => {
			if(vmPollingTimer) clearInterval(vmPollingTimer);
		};
		
		
		/* VM List 정렬 테이블 */
		const sortKey = ref('vmid');
		const sortOrder = ref(1);
		const sortBy = key => {
			if(sortKey.value === key) {
				sortOrder.value *= -1; //같은 컬럼 누르면 오름/내림차순 토글
			} else {
				sortKey.value = key;
				sortOrder.value = 1;
			}
		};
		const sortedVmList = computed(() => {
			return [...vmList.value].sort((a,b) => {
				let valA = a[sortKey.value];
				let valB = b[sortKey.value];
				
				if(valA === undefined || valA === null) valA = '';
				if(valB === undefined || valB === null) valB = '';
				
				if(typeof valA === 'string') valA = valA.toLowerCase();
				if(typeof valB === 'string') valB = valB.toLowerCase();
				
				if(valA < valB) return -1 * sortOrder.value;
				if(valA > valB) return 1 * sortOrder.value;
			});
		});
		
		
		/* Axios 기본 세팅 */
		//1) 깡통 클라이언트 생성
		const api = axios.create({
			baseURL: '/api'
			//, headers: {
			//	'Authorization' : `Bearer ${jwtToken}`
			//}
		});
		//2) 인터셉터 장착(통신이 나갈 때마다 토큰을 동적으로 검사해서 꽂아줌)
		api.interceptors.request.use(config => {
			if(jwtToken.value) {
				config.headers.Authorization = `Bearer ${jwtToken.value}`;
			}
			return config;
		});
		//3) 토큰 자동 발급 함수
		const fetchToken = async () => {
			try {
				const response = await axios.get('/api/public/token');
				jwtToken.value = response.data;
				console.log("백엔드 토큰 발급 완료!");
			} catch(error) {
				console.error("백엔드 토큰 발급 실패:", error);
			}
		};
		
		const fetchTasks = async () => {
			try {
				const response = await api.get(`/proxmox/nodes/${targetNode.value}/tasks`);
				taskList.value = response.data;
			} catch(error) {
				console.error("작업 조회 실패:", error);
			}
		};
		
		const fetchLogs = async () => {
			if(!targetUpid.value) {
				alert("조회할 Task를 먼저 선택하세요.");
				return;
			}
			
			try{
				//1) TaskLog 조회 API 호출
				const response = await api.get(`/proxmox/nodes/${targetNode.value}/tasks/${targetUpid.value}/log`);
				taskLogs.value = response.data;
				
				//2) 로그 끝났는지 체크
				const lastLog = taskLogs.value[taskLogs.value.length -1];
				if(lastLog && lastLog.t.startsWith('TASK ')) {
					stopWatchingLogs();
					
					fetchVmList();
				}
			} catch(error) {
				console.error("로그 조회 실패:", error);
				taskLogs.value.push({ n:999, t:"통신 에러 발생!"});
				stopWatchingLogs();
			}
		};
		
		const startWatchingTasks = async () => {
			taskList.value = [];
			fetchTasks();
			setInterval(fetchTasks, 10000);
		};
		
		const onTaskSelect = () => {
			stopWatchingLogs();
			taskLogs.value = [];
			startWatchingLogs();
		};
		
		const startWatchingLogs = () => {
			if(isPolling.value) return;
			isPolling.value = true;
			taskLogs.value = [];           //←화면 초기화
			fetchLogs();                   //←1빠따 즉시 실행
			logPollingTimer = setInterval(fetchLogs, 2000);  //←2초마다 호출
		};
		
		const stopWatchingLogs = () => {
			isPolling.value = false;
			if(logPollingTimer) clearInterval(logPollingTimer);
		};
		
		const formatTime = (unixTime) => {
			if(!unixTime) return '';
			const date = new Date(unixTime * 1000);
			return date.toLocaleString('ko-KR', {
				month: 'short', day: 'numeric',
				hour: '2-digit', minute: '2-digit', second: '2-digit'
			});
		};
		
		
		
		const fetchVmList = async () => {
			try{
				const response = await api.get(`/proxmox/nodes/${targetNode.value}/qemu`);
				vmList.value = response.data;
			} catch(error) {
				console.error("VM 목록 조회 실패", error);
			}
		};
		
		const controlVm = async (vmid, action) => {
			if(!confirm(`${vmid}번 VM을 ${action} 하시겠습니까?`)) {
				return;
			}
			
			try{
				const response = await api.post(`/proxmox/nodes/${targetNode.value}/qemu/${vmid}/status/${action}`);
				const newUpid = response.data.upid;
				if(newUpid) {
					targetUpid.value = newUpid;
					fetchTasks();
					startWatchingLogs();
				}
			} catch(error) {
				console.error(error);
				alert("제어 실패")
			}
		};
		
		
		/* Storage */
		const storageFormInit = {
			type: 'nfs'
			, storage: ''
			, content: 'images,iso,backup'
			, config: {}
		}
		const isStorageModalOpen = ref(false);
		const storageForm = ref(storageFormInit);
		
		const openStorageModal = () => isStorageModalOpen.value = true;
		const closeStorageModal = () => {
			isStorageModalOpen.value = false;
			storageForm.value = storageFormInit;
		};
		
		const onTypeChange = () => {
			storageForm.value.config = {};
		};
		const submitStorage = async () => {
			if(!storageForm.value.storage) {
				alert("스토리지 이름을 입력하세요.");
				return;
			}
			
			try{
				await api.post('/proxmox/storage', storageForm.value);
				alert(`[${storageForm.value.type}] 스토리지 '${sotrageForm.value.storage}' 추가 성공!`);
				closeStorageModal();
			} catch(error) {
				console.error(error);
				alert("스토리지 추가 실패!");
			}
		};
		
		
		onMounted(async () => {
			await fetchToken();
			await startWatchingTasks();
			
			await fetchVmList();
			startWatchingVmList();
		});
		
		onUnmounted(() => {
			stopWatchingLogs();
			stopWatchingVmList();
		});
		
		//02. 템플릿(HTML)에서 쓸 변수와 함수들을 return.
		return {
			taskList,
			taskLogs,
			isPolling,
			targetNode,
			targetUpid,
			onTaskSelect,
			startWatchingLogs,
			stopWatchingLogs,
			formatTime,
			vmList,
			controlVm,
			sortedVmList,
			sortKey,
			sortOrder,
			sortBy,
			formatUptime,
			isStorageModalOpen,
			storageForm,
			openStorageModal,
			closeStorageModal,
			onTypeChange,
			submitStorage
		};
	}
});

//03. HTML의 <div id="app">에 꽂아버리기
app.mount('#app');