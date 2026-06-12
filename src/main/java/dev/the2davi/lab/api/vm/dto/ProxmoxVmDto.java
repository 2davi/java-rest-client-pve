package dev.the2davi.lab.api.vm.dto;

public record ProxmoxVmDto(
		String vmid
		, String name
		, String status
		, Double maxmem
		, Double cpus
		, Long uptime
) {}
