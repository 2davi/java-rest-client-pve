package dev.the2davi.lab.cmmn.type;

/**
 * <pre>
 * https://www.mail-archive.com/pve-devel@lists.proxmox.com/msg01451.html
 *  
 * << 개정이력 >>
 *   
 *  수정일      수정자		수정내용
 *  ------------------------------------------------
 *  2026-06-11  kcy0122			최초 생성
 * </pre>
 */
public enum TaskAction {
	START("시작")
	, STOP("정지")
	, SHUTDOWN("종료")
	, REBOOT("재시작")
	, RESET("강제 재시작")
	, SUSPEND("일시중지")
	, RESUME("재개")
	, CREATE("생성")
	, DESTROY("삭제")
	, CLONE("복제")
	, MIGRATE("마이그레이션")
	, MOVE("디스크 이동")
	, TEMPLATE("템플릿 변환")
	, SNAPSHOT( "스냅샷")
	, ROLLBACK("롤백")
	, RESTORE("복원")
	, BACKUP("백업")
	, UPDATE("업데이트")
	, VNC("VNC")
	, UNKNOWN("알 수 없음");
	
	//private final String keyword;
	private final String displayName;
	
	TaskAction(/* String keyword, */String displayName) {
		//this.keyword = keyword;
		this.displayName = displayName;
	}
	
	public String getName() {
		return displayName;
	}
	
//	public static TaskAction fromTaskType(String taskType) {
//		if(taskType == null) { return UNKNOWN; }
//		
//		for(TaskAction action : values()) {
//			if( !action.keyword.isEmpty()  && taskType.contains(action.keyword) ) {
//				return action;
//			}
//		}
//		
//		return UNKNOWN;
//	}

}
