package dev.the2davi.lab.cmmn.type;

public enum TaskAction {
	START("start", "시작")
	, STOP("stop", "강제 종료")
	, SHUTDOWN("shutdown", "정상 종료")
	, CLONE("clone", "복제")
	, UNKNOWN("unknown", "알 수 없음");
	
	private final String keyword;
	private final String name;
	
	TaskAction(String keyword, String name) {
		this.keyword = keyword;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static TaskAction fromTaskType(String taskType) {
		if(taskType == null) { return UNKNOWN; }
		
		for(TaskAction action : values()) {
			if( !action.keyword.isEmpty()  && taskType.contains(action.keyword) ) {
				return action;
			}
		}
		
		return UNKNOWN;
	}

}
