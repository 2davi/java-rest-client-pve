package dev.the2davi.lab.api.vm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProxmoxVmCloneDto(
		@JsonProperty("newid") String newVmid
		, @JsonProperty("name") String name
		, @JsonProperty("full") Boolean isFull
) {}
