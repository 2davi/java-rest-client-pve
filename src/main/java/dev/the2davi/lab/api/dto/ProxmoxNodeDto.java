package dev.the2davi.lab.api.dto;

public record ProxmoxNodeDto(
	String node
	, String status
	, String cpu
	, Long maxmem
	, Long mem
	) {}
