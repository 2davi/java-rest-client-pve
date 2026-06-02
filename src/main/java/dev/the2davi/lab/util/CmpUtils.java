package dev.the2davi.lab.util;

import dev.the2davi.lab.api.dto.CmpTaskHistoryDto;
import dev.the2davi.lab.api.dto.ProxmoxTaskDto;

public class CmpUtils {

	//유틸리티 클래스는 인스턴스화 방지가 국룰이래. (???)
	private CmpUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	public static CmpTaskHistoryDto parseToCmpTask(ProxmoxTaskDto raw) {
/*
		String resourceType = "UNKNOWN";
		String action = "UNKNOWN";
		String actionName = raw.type();
		
		//01. 리소스 타입 판별
		if(raw.type().startsWith("qm")) {
			resourceType = "VM";
		} else if(raw.type().startsWith("vz")) {
			resourceType = "CT";
		} else {
			resourceType = "NODE";
		}
		
		//02. Action(명령어) 구조분해
		if(raw.type().contains("start")) {
			action = "START";
			actionName = "시작";
		} else if(raw.type().contains("stop")) {
			action = "STOP";
			actionName = "강제 종료";
		} else if(raw.type().contains("shutdown")) {
			action = "SHUTDOWN";
			actionName = "정상 종료";
		} else if(raw.type().contains("clone")) {
			action = "CLONE";
			actionName = "복제";
		}
*/
		ResourceType resourceType = ResourceType.fromTaskType(raw.type());
		TaskAction taskAction = TaskAction.fromTaskType(raw.type());
		
		//03. 프론트엔드 UI 전용 문자열 조합
		String targetName = ( raw.id() != null && !raw.id().isEmpty() ) ? raw.id() : raw.node();
//		String displayTitle = String.format("[%s %s] %s", resourceType, targetName, actionName);
		String displayTitle = String.format("[%s %s] %s"
				, resourceType.getDisplayName()
				, targetName
				, taskAction.getActionName()
		);
		
		return new CmpTaskHistoryDto(
				raw.upid()
				//, resourceType
				, resourceType.name()
				, raw.id()
				//, action
				,taskAction.name()
				, displayTitle
				, raw.status()
				, raw.starttime()
		);
	}
}
