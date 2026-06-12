package dev.the2davi.lab.api.vm.dto;

public record ProxmoxVmCloneDto(
		String node
		, String newVmid
		, String name
		, Boolean isFull
) {}
