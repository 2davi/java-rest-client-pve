package dev.the2davi.lab.api.vm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProxmoxVmDto(
		@JsonProperty("vmid")     String vmid
		, @JsonProperty("name")   String name
		, @JsonProperty("status") String status
		, @JsonProperty("maxmem") Double maxmem
		, @JsonProperty("cpus")   Double cpus
		, @JsonProperty("uptime") Long uptime
) {}
