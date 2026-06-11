package dev.the2davi.lab.api.dto;

public record ProxmoxVmCloneDto(
		String newVmid
		, String name
		, Boolean isFull
) {}
