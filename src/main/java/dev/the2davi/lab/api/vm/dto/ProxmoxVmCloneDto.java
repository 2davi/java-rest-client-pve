package dev.the2davi.lab.api.vm.dto;

public record ProxmoxVmCloneDto(
		String newVmid
		, String name
		, Boolean isFull
) {}
