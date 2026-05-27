const { createApp, ref, onMounted, onUnmounted } = Vue;

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
		
		onMounted(async () => {
			await fetchToken();
			await startWatchingTasks();
		});
		
		onUnmounted(() => {
			stopWatchingLogs();
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
			formatTime
		};
	}
});

//03. HTML의 <div id="app">에 꽂아버리기
app.mount('#app');