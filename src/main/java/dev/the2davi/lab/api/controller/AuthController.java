package dev.the2davi.lab.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.LoginRequestDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxTicketResponse;
import dev.the2davi.lab.security.session.SecuritySession;
import dev.the2davi.lab.security.session.SecuritySessionStore;
import dev.the2davi.lab.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final JwtUtil jwtUtil;
	private final RestClient authRestClient;
	private final SecuritySessionStore sessionStore;
	
	public AuthController(JwtUtil jwtUtil, @Value("${proxmox.api.url}") String apiUrl, SecuritySessionStore sessionStore) {
		this.jwtUtil = jwtUtil;
		this.authRestClient = RestClient.builder()
				.baseUrl(apiUrl)
				.build();
		this.sessionStore = sessionStore;
	}
	
	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto dto) {
		
		try{
			String formData = String.format("username=%s&password=%s", dto.username(), dto.password());
			
			ParameterizedTypeReference<ProxmoxResponse<ProxmoxTicketResponse>> responseType =
					new ParameterizedTypeReference<>() {};
			
			ProxmoxResponse<ProxmoxTicketResponse> pveRes = authRestClient.post()
					.uri("/access/ticket")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.body(formData)
					.retrieve()
					.body(responseType);
			
			if(pveRes == null || pveRes.data() == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증 실패"));
			}
			
			ProxmoxTicketResponse ticketData = pveRes.data();
			
//			String jwtToken = jwtUtil.createToken(
//				ticketData.username()
//				, ticketData.ticket()
//				, ticketData.CSRFPreventionToken()
//			);
			
			SecuritySession session = new SecuritySession(
					ticketData.username()
					, ticketData.ticket()
					, ticketData.CSRFPreventionToken()
					, java.time.Instant.now()
			);
			String sid = sessionStore.create(session);
			
			String jwt = jwtUtil.createToken(ticketData.username(), sid);
			return ResponseEntity.ok(Map.of("token", jwt));
			
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Proxmox 계정 정보 불일치."));
		}
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		String jwt = jwtUtil.parseJwt(request);
		Claims claims = (jwt != null) ? jwtUtil.getClaims(jwt) : null;
		if(claims != null) {
			sessionStore.remove(claims.get("sid", String.class));
		}
		return ResponseEntity.noContent().build();
	}
}
