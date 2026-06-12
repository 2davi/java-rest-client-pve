const { createApp, computed, onMounted, onUnmounted, nextTick } = Vue;

/** FormData 기본값 팩토리 */
const DEFAULT_STORAGE_TYPE = 'nfs';
const STORAGE_TYPE_SCHEMA = {
	nfs: {
		label: 'NFS',
		fields: [
			{ key: 'server', label: 'NFS ServerIP',   placeholder: '예: 192.168.10.50'    },
			{ key: 'export', label: 'Export Path',    placeholder: '예: /srv/volume1/nfs' },
		],
	},
	lvmthin: {
		label: 'LVM-Thin',
		fields: [
			{ key: 'vgname', label: 'VolumeGroup',    placeholder: '예: pve'  },
			{ key: 'thinpool', label: 'Thin Pool',      placeholder: '예: data' },
		],
	},
	rbd: {
		label: 'Ceph RBD',
		fields: [
			{ key: 'pool',   label: 'Ceph Pool Name', placeholder: '예: ceph-data' },
		],
	},
};

const buildStorageConfig = type =>
	Object.fromEntries(( STORAGE_TYPE_SCHEMA[type]?.fields ?? [] ).map( f => [f.key, ''] ));

const createStorageForm = () => ({
	type: DEFAULT_STORAGE_TYPE,
	storage: '',
	content: 'images,iso,backup',
	config: buildStorageConfig(DEFAULT_STORAGE_TYPE), // 중첩 객체도 매번 새로
});

const createCloneForm = () => ({
	node: '',          // 소스 VM의 현재 노드(작업 라우팅용)
	sourceVmid: null,
	newVmid: null,
	name: '',
	isFull: true,
});

const createDestroyForm = () => ({
	node: '',          // 대상 VM의 현재 노드
	vmid: null,
	purge: true,
	destroyUnreferencedDisk: true,
});


const app = createApp({
	setup() {
		/* 변수 선언 */
		//01. 반응형 상태 변수
		const taskList = ref([]);
		const taskLogs = ref([]);
		const isPolling = ref(false);
		// UPID 2번째 세그먼트가 노드다 → UPID:<node>:pid:pstart:starttime:type:id:user:
		const nodeFromUpid = (upid) => (upid ?? '').split(':')[1] ?? '';
		const targetUpid = ref('');
		const jwtToken = ref('');
		const userProfile = ref({
			username: ''
		});
		const TOKEN_KEY = 'cmp_jwt';
		const USER_KEY = 'cmp_username';
		const loginForm = ref({
			username: '', password: ''
		});
		//let taskPollingTimer = null;
		let logPollingTimer = null;
		
		const vmList = ref([]);
		
		/* Network (노드 선택 기반) */
		const nodes = ref([]);            // 노드 드롭다운 목록
		const selectedNode = ref('');     // 현재 선택 노드
		const networkList = ref([]);      // 선택 노드의 인터페이스 목록
		
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
		


		/* Auth */
		const clearSession = () => {
			sessionStorage.removeItem(TOKEN_KEY);
			sessionStorage.removeItem(USER_KEY);
			userProfile.value = {};
			jwtToken.value = '';
			//런타임(handleLogout)에서만 호출되니, 뒤에 선언된 함수 참조해도 문제 없음.
			stopWatchingLogs();
			stopWatchingVmList();
		};
		
		const handleLogin = async () => {
			if(!loginForm.value.username || !loginForm.value.password) {
				alert("아이디와 비번을 입력해라.");
				return;
			};
			
			try{
				const response = await axios.post("/auth/login", loginForm.value);
				const token = response.data.token;
				jwtToken.value = token;
				sessionStorage.setItem(TOKEN_KEY, token);
				sessionStorage.setItem(USER_KEY, loginForm.value.username);
				userProfile.value.username = loginForm.value.username;
				
				//로그인 성공할 시에 백엔드 데이터 동시다발 호출
				await fetchTasks();
				await fetchVmList();
				startWatchingVmList();
				fetchNodes();
			} catch(error) {
				const pd = error.response?.data;
				alert(pd?.detail ?? "인증 실패 ^ㅂ^");
				console.error("로그인 실패", pd ?? error);
				//pd?.code로 응답 상태코드 값으로 분기 처리 가능
			}
		};
		const handleLogout = async () => {
			try{
				await axios.post('/auth/logout', {}, {
					headers: { Authorization: `Bearer ${jwtToken.value}` }
				});
			} catch(error) {
				console.warn("서버 로그아웃 실패(무시 가능):", error);
			}
			
			clearSession();
			loginForm.value.password = '';
			alert("로그아웃~");
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
				
				if(sortKey.value === 'vmid' || sortKey.value === 'uptime') {
					return (Number(valA) - Number(valB)) * sortOrder.value;
				}
				
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
		//2) 요청 인터셉터 장착(통신이 나갈 때마다 토큰을 동적으로 검사해서 꽂아줌)
		api.interceptors.request.use(config => {
			if(jwtToken.value) {
				config.headers.Authorization = `Bearer ${jwtToken.value}`;
			}
			return config;
		});
		//3) 응답 인터셉터 장착(401 응답 일괄 처리)
		api.interceptors.response.use(
			response => response
			, error => {
				if(error.response?.status == 401) {
					clearSession();
					console.warn("로그인 세션 만료");
				}
				return Promise.reject(error);
			}
		);
		//4) 토큰 자동 발급 함수
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
				const response = await api.get('/proxmox/cluster/tasks'); // 데이터센터 스코프
				taskList.value = response.data;
			} catch(error) {
				console.error("작업 조회 실패:", error);
			}
		};
		
		const logContainer = ref(null); //log 패널 HTML 태그를 담음.
		const fetchLogs = async () => {
			if(!targetUpid.value) {
				alert("조회할 Task를 먼저 선택하세요.");
				return;
			}
			
			try{
				//1) TaskLog 조회 API 호출
				const node = nodeFromUpid(targetUpid.value); // targetNode 대신 UPID에서 추출
				const response = await api.get(`/proxmox/nodes/${node}/tasks/${targetUpid.value}/log`);
				taskLogs.value = response.data;
				
				await nextTick();
				if(logContainer.value) {
					logContainer.value.scrollTop = logContainer.value.scrollHeight;
				}
				
				//2) 로그 끝났는지 체크
				const lastLog = taskLogs.value[taskLogs.value.length -1];
				if(lastLog && lastLog.t.startsWith('TASK ')) {
					stopWatchingLogs();
					
					setTimeout(() => {
						fetchVmList();
						fetchTasks();
					}, 1500);
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
				const response = await api.get('/proxmox/cluster/qemu'); // 데이터센터 스코프
				vmList.value = response.data;
			} catch(error) {
				console.error("VM 목록 조회 실패", error);
			}
		};
		
		const controlVm = async (vm, action) => {
			if(!confirm(`${vm.vmid}번 VM을 ${action} 하시겠습니까?`)) {
				return;
			}
			
			try{
				const response = await api.post(`/proxmox/nodes/${vm.node}/qemu/${vm.vmid}/status/${action}`);
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
		
		
		/* Network */
		const fetchNodes = async () => {
			try {
				const response = await api.get('/proxmox/nodes');
				nodes.value = response.data;
				// 선택 노드 없으면 첫 노드 자동 선택 후 네트워크 로드
				if(!selectedNode.value && nodes.value.length > 0) {
					selectedNode.value = nodes.value[0].node;
					fetchNetwork(selectedNode.value);
				}
			} catch(error) {
				console.error("노드 목록 조회 실패", error);
			}
		};
		const fetchNetwork = async (node) => {
			if(!node) return;
			try {
				const response = await api.get(`/proxmox/nodes/${node}/network`);
				networkList.value = response.data;
			} catch(error) {
				console.error("네트워크 조회 실패", error);
			}
		};
		const onNodeChange = () => fetchNetwork(selectedNode.value);
		
		
		/* Storage */
		const {
			form: storageForm
			, isOpen: isStorageModalOpen
			, open: openStorageBase
			, close: closeStorageModal
		} = useModalForm(createStorageForm);
		
		//@click="openStorageModal"에서 이벤트를 인자로 넘긴다.
		const openStorageModal = () => openStorageBase();
		const onStorageTypeChange = () => {
			storageForm.value.config = buildStorageConfig(storageForm.value.type);
		};
		
		const storageTypeOptions = Object.entries(STORAGE_TYPE_SCHEMA)
				.map(([value, { label }]) => ({ value, label }));
		const storageFields = computed(
			() => STORAGE_TYPE_SCHEMA[storageForm.value.type]?.fields ?? []
		);
		
		const submitStorage = async () => {
			if(!storageForm.value.storage) {
				alert("스토리지 이름을 입력하세요.");
				return;
			}
			
			try{
				await api.post('/proxmox/storage', storageForm.value);
				alert(`[${storageForm.value.type}] 스토리지 '${storageForm.value.storage}' 추가 성공!`);
				closeStorageModal();
			} catch(error) {
				console.error(error);
				alert("스토리지 추가 실패!");
			}
		};
		
		/* VM Clone */
		const {
			form: cloneForm
			, isOpen: isCloneModalOpen
			, open: openCloneBase
			, close: closeCloneModal
		} = useModalForm(createCloneForm);
		const openCloneModal = vm =>
				openCloneBase({ node: vm.node, sourceVmid: vm.vmid, newVmid: Number(vm.vmid) + 100 });
		
		const submitCloneVm = async () => {
			if(!cloneForm.value.sourceVmid) {
				alert("타겟 VMID를 어떻게 지웠냐?");
				return;
			}
			if(!cloneForm.value.newVmid) {
				alert("복제할 새 VMID는 필수다.");
				return;
			}
			
			try{
				const response = await api.post(`/proxmox/nodes/${cloneForm.value.node}/qemu/${cloneForm.value.sourceVmid}/clone`, {
					newVmid: cloneForm.value.newVmid
					, name: cloneForm.value.name
					, isFull: cloneForm.value.isFull
				});
				
				targetUpid.value = response.data.upid;
				closeCloneModal();
				fetchTasks();
				startWatchingLogs();
			} catch(error) {
				console.error(error);
				alert("복제 실패! 권한이나 VMID 누락 여부 확인");
			}
		};
		
		/* VM Destroy */
		const {
			form: destroyForm
			, isOpen: isDestroyModalOpen
			, open: openDestroyBase
			, close: closeDestroyModal
		} = useModalForm(createDestroyForm);
		const openDestroyModal = vm => openDestroyBase({ node: vm.node, vmid: vm.vmid });
		
		const submitDestroyVm = async () => {
			if(!destroyForm.value.vmid) {
				alert("타겟 VMID를 어떻게 지웠냐?");
				return;
			}
			
			try{
				const response = await api.delete(`/proxmox/nodes/${destroyForm.value.node}/qemu/${destroyForm.value.vmid}`, {
					data: {
						purge: destroyForm.value.purge
						, destroyUnreferencedDisk: destroyForm.value.destroyUnreferencedDisk
					}
				});
				
				targetUpid.value = response.data.upid;
				closeDestroyModal();
				fetchTasks();
				startWatchingLogs();
			} catch(error) {
				console.error(error);
				alert("삭제 실패! VM이 켜져있거나 Lock이 걸려있는지 확인!");
			}
		};
		
		onMounted(async () => {
			/* sessionStorage 기반으로 jwtToken 복원 */
			const savedToken = sessionStorage.getItem(TOKEN_KEY);
			const savedUsername = sessionStorage.getItem(USER_KEY);
			if(savedToken) {
				jwtToken.value = savedToken;
				if(savedUsername) {userProfile.value.username = savedUsername;}
				
				await fetchTasks();
				await fetchVmList();
				startWatchingVmList();
				fetchNodes();
			} else {
				console.debug("저장된 세션 없음 - 로그인 화면");
			}
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
			storageForm,
			isStorageModalOpen,
			openStorageModal,
			closeStorageModal,
			onStorageTypeChange,
			storageTypeOptions,
			storageFields,
			submitStorage,
			jwtToken,
			loginForm,
			handleLogin,
			handleLogout,
			isCloneModalOpen,
			cloneForm,
			openCloneModal,
			closeCloneModal,
			submitCloneVm,
			isDestroyModalOpen,
			destroyForm,
			openDestroyModal,
			closeDestroyModal,
			submitDestroyVm,
			fetchVmList,
			nodes,
			selectedNode,
			networkList,
			fetchNetwork,
			onNodeChange,
			logContainer,
			userProfile
		};
	}
});

//03. HTML의 <div id="app">에 꽂아버리기
app.mount('#app');