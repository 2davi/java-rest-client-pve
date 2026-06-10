package dev.the2davi.lab.api.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.LoginRequestDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxTicketResponse;
import dev.the2davi.lab.cmmn.exception.error.auth.InvalidCredentialsException;
import dev.the2davi.lab.cmmn.format.TypeUtil;
import dev.the2davi.lab.cmmn.security.session.SecuritySession;
import dev.the2davi.lab.cmmn.security.session.SecuritySessionStore;
import dev.the2davi.lab.cmmn.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	
	private final String DEFAULT_REALM;
	private final String AUTH_QUERYSTRING;
	
	private final JwtUtil jwtUtil;
	private final RestClient authRestClient;
	private final SecuritySessionStore sessionStore;
	
	public AuthController(
			JwtUtil jwtUtil
			, @Value("${proxmox.api.url}") String apiUrl
			, @Value("${proxmox.auth.default-realm:pam}") String defaultRealm
			, @Value("${proxmox.auth.query-string:username=%s&password=%s&realm=%s}") String authQuerystring
			, SecuritySessionStore sessionStore
			, ClientHttpRequestFactory pveRequestFactory) {
		this.jwtUtil = jwtUtil;
		this.authRestClient = RestClient.builder()
				.requestFactory(pveRequestFactory)
				.baseUrl(apiUrl)
				.build();
		this.sessionStore = sessionStore;
		this.DEFAULT_REALM = defaultRealm;
		this.AUTH_QUERYSTRING = "username=%s&password=%s&realm=%s";
	}
	
	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto dto) {
		
		String username = TypeUtil.encodeUTF_8(dto.username());
		String password = TypeUtil.encodeUTF_8(dto.password());
		String realm = StringUtils.hasText(dto.realm()) ? TypeUtil.encodeUTF_8(dto.realm()) : DEFAULT_REALM;
		String formData = String.format(AUTH_QUERYSTRING, username, password, realm);
		ParameterizedTypeReference<ProxmoxResponse<ProxmoxTicketResponse>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<ProxmoxTicketResponse> pveRes;
		try{
			pveRes = authRestClient.post()
					.uri("/access/ticket")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.body(formData)
					.retrieve()
					.body(responseType);
		} catch(HttpClientErrorException.Unauthorized e) {
			//401 - (Context: Login) = 자격증명 문제. 의미에 맞춰 도메인 예외로 분류.
			throw new InvalidCredentialsException(e);
		}
		
		
		if(pveRes == null || pveRes.data() == null) {
			//* return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증 실패"));
			throw new InvalidCredentialsException(null);
		}
		// ResourceAccessException, HttpServerErrorException 등은 잡지 않는다 → 전역 advice로 보낸다.
		
		ProxmoxTicketResponse ticketData = pveRes.data();
		SecuritySession session = new SecuritySession(
				ticketData.username()
				, ticketData.ticket()
				, ticketData.CSRFPreventionToken()
				, java.time.Instant.now()
		);
		String sid = sessionStore.create(session);
		String jwt = jwtUtil.createToken(ticketData.username(), sid);
		return ResponseEntity.ok(Map.of("token", jwt));
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
