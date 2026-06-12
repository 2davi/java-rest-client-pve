package dev.the2davi.lab.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CmpTaskHistoryDto(
		String upid				//TASK's UPID
		, String node
		, @JsonProperty("resource-type")
		  String resourceType	//VM, CT, NODE, ...
		, String resourceId		//VMID, ...
		, String action			//START, STOP, SHUTDOWN, ...
		, String displayTitle	//프론트엔드 UI전용 이쁜 텍스트
		, String status			
		, Long timestamp		//(starttime 기준)
) {}
