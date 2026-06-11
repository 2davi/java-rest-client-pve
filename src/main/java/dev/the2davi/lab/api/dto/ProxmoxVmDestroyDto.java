package dev.the2davi.lab.api.dto;

public record ProxmoxVmDestroyDto(
		Boolean purge
		, Boolean destroyUnreferencedDisk
) {}
