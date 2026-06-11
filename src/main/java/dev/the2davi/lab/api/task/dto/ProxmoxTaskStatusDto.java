package dev.the2davi.lab.api.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//Proxmox TASK 상태 정보 DTO
public record ProxmoxTaskStatusDto(
		@JsonProperty("status")String status,
		@JsonProperty("exitstatus") String exitStatus) {}
