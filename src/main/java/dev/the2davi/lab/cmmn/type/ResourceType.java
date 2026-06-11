package dev.the2davi.lab.cmmn.type;

import java.util.Set;

public enum ResourceType {
	VM("VM")
	, CONTAINER("컨테이너")
	, NODE("노드")
	, BACKUP("백업")
	, UNKNOWN("알 수 없음");
	
	//private final String keyword;
	private final String displayName;
	
	ResourceType(/* String keyword, */String displayName) {
		//this.keyword = keyword;
		this.displayName = displayName;
	}
//	public String getKeyword() {
//		return keyword;
//	}
	public String getName() {
		return displayName;
	}

	// 접두사로 안 갈리는 노드/클러스터 레벨 작업 (확장 지점 — task_desc_table 보고 추가)
	private static final Set<String> NODE_TASKS = Set.of("aptupdate", "startall", "stopall");

	public static ResourceType fromTaskType(String taskType) {
		if(taskType == null || taskType.isBlank()) return UNKNOWN;

		// (1) 접두사로 구분 안 되는 예외부터 판별
		// vzdump은 'vz'로 시작하지만 컨테이너가 아니라 백업이다. 접두사 검사보다 먼저 와야 함.
		if(taskType.equals("vzdump"))     return BACKUP;
		if(NODE_TASKS.contains(taskType)) return NODE;

		// (2) 접두사로 구분되는 게스트 작업
		// qm* → (qmstart/qmstop/qmdestroy/qmclone/qmsnapshot/qmmigrate/qmrestore …)
		// vz* → (vzstart/vzdestroy/vzcreate/vzclone …)
		// ※ vzdump은 (1)에서 처리.
		if(taskType.startsWith("qm")) return VM;
		if(taskType.startsWith("vz")) return CONTAINER;

		// (3) worker type은 사양상 '임의 ASCII 문자열'이라 신규 타입이 언제든 나온다 → 모르면 솔직히 UNKNOWN
		return UNKNOWN;
	}
}
