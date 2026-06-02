package dev.the2davi.lab.api.dto;

public record ProxmoxVmDto(
		String vmid
		, String name
		, String status
		, Double maxmem
		, Double cpus
		, Long uptime
) {}
