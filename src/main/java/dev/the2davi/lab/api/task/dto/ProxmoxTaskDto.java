package dev.the2davi.lab.api.task.dto;

public record ProxmoxTaskDto(
		String upid				//작업 고유 ID
		, String node			//노드 이름
		, String type			//작업 종류 (qmstart, qmstop, ...)
		, String id				//타겟 리소스 번호 (VMID, ...)
		, String user			//실행자 (e.g., "spring-api@pve!backend")
		, String status			//running, stopped
		, String exitstatus		//OK 또는 ERR_MSG
		, Long starttime		//시작 시각
		, Long endtime			//종료 시각
) {}
