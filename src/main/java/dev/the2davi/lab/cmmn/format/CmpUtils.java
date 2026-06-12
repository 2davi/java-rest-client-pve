package dev.the2davi.lab.cmmn.format;

import dev.the2davi.lab.api.dto.CmpTaskHistoryDto;
import dev.the2davi.lab.api.task.dto.ProxmoxTaskDto;
import dev.the2davi.lab.cmmn.type.ResourceType;
import dev.the2davi.lab.cmmn.type.TaskAction;
import dev.the2davi.lab.cmmn.type.TaskType;

public class CmpUtils {

	//유틸리티 클래스는 인스턴스화 방지가 국룰이래. (???)
	private CmpUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	public static CmpTaskHistoryDto parseToCmpTask(ProxmoxTaskDto raw) {

		TaskType taskType = TaskType.from(raw.type());
		ResourceType resourceType = taskType.resourceType();
		TaskAction taskAction = taskType.action();
		
		//프론트엔드 UI 전용 문자열 조합
		String targetName = ( raw.id() != null && !raw.id().isEmpty() ) ? raw.id() : raw.node();
		
		String displayTitle = String.format("[%s %s] %s"
				, resourceType.getName()
				, targetName
				, taskAction.getName()
		);
		
		return new CmpTaskHistoryDto(
				raw.upid()
				, raw.node()
				, resourceType.name()
				, raw.id()
				, taskAction.name()
				, displayTitle
				, raw.status()
				, raw.starttime()
		);
	}
}
