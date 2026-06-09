package dev.the2davi.lab.api.dto;

import java.util.Map;

public record ProxmoxStorageDto(
		String type						//Storage Type
		, String storage				//Storage ID
		, String content				//e.g., disks, images, ...
		, Map<String, Object> config	//Variables
) {}
