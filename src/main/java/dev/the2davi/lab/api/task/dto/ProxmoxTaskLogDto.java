package dev.the2davi.lab.api.task.dto;

public record ProxmoxTaskLogDto(
		Integer n, // Line Number
		String t   // Log Texts
) {}
