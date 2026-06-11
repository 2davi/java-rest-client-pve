package dev.the2davi.lab.api.vm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProxmoxVmDestroyDto(
		@JsonProperty("purge") Boolean purge
		, @JsonProperty("destroy-unreferenced-disk") Boolean destroyUnreferencedDisk
) {}
