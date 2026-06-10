package dev.the2davi.lab.cmmn.security.session;

import java.time.Duration;
import java.time.Instant;

public record SecuritySession(
		String username
		, String ticket
		, String CSRFPreventionToken
		, Instant issuedAt) {
	
	//PVE 티켓 수명은 2시간. 만료 직전 사용을 피하려고 약간 짧게(110분) 잡는다.
	private static final Duration TTL = Duration.ofMinutes(110);
	
	//만료 여부 판별
	public boolean isExpired() {
		return Instant.now().isAfter(issuedAt.plus(TTL));
	}
}
