package dev.the2davi.lab.api.auth.dto;

public record LoginRequestDto(
	String username
	, String password
	, String realm
) {}
