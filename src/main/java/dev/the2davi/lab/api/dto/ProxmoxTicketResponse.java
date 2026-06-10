package dev.the2davi.lab.api.dto;

public record ProxmoxTicketResponse(
		String ticket
		, String CSRFPreventionToken
		, String username
) {}
