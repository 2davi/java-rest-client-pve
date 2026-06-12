package dev.the2davi.lab.api.vm.dto;

public record ProxmoxVmDestroyDto(
		Boolean purge
		, Boolean destroyUnreferencedDisk
) {}
