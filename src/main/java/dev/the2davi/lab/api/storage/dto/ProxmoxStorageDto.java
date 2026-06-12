package dev.the2davi.lab.api.storage.dto;

public record ProxmoxStorageDto(
	//api-index의 /cluster/resources Returns를 보고, `for types 'storage'`인 항목만
	String id
	, String node
	, String type
	, String storage
	, String content
	, String disk
) {}
