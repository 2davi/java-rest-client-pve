package dev.the2davi.lab.api.dto;

//Proxmox TASK 상태 정보 DTO
public record ProxmoxTaskStatusDto(
		String status,
		String exitStatus) {}
