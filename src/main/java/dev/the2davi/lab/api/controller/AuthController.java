package dev.the2davi.lab.api.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import dev.the2davi.lab.api.dto.LoginRequestDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxTicketResponse;
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
	
	private final JwtUtil jwtUtil;
	private final RestClient authRestClient;
	private final SecuritySessionStore sessionStore;
	
	public AuthController(
			JwtUtil jwtUtil
			, @Value("${proxmox.api.url}") String apiUrl
			, @Value("${proxmox.auth.default-realm:pam}") String defaultRealm
			, SecuritySessionStore sessionStore
			, ClientHttpRequestFactory pveRequestFactory) {
		this.jwtUtil = jwtUtil;
		this.authRestClient = RestClient.builder()
				.requestFactory(pveRequestFactory)
				.baseUrl(apiUrl)
				.build();
		this.sessionStore = sessionStore;
		this.DEFAULT_REALM = defaultRealm;
	}
	
	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto dto) {
		
		try{
			//* String formData = String.format("username=%s&password=%s", TypeUtil.encodeUTF_8(dto.username()), TypeUtil.encodeUTF_8(dto.password()));
			String username = TypeUtil.encodeUTF_8(dto.username());
			String password = TypeUtil.encodeUTF_8(dto.password());
			String realm = StringUtils.hasText(dto.realm()) ? TypeUtil.encodeUTF_8(dto.realm()) : DEFAULT_REALM;
			String formData = String.format(
					"username=%s&password=%s&realm=%s"
					, username
					, password
					, realm
			);
			
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
		} catch(HttpClientErrorException.Unauthorized e) {
			String errState = "아이디/비밀번호/Realm 틀림, 2FA";
			String errMsg = "아이디·비밀번호·REALM을 확인하세요.";
			log.error(String.format("ERROR: %s", errState), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", errMsg));
		} catch(HttpClientErrorException e) {
			String errState = "401제외 4xx";
			String errMsg = "요청이 올바르지 않습니다.";
			log.error(String.format("ERROR: %s", errState), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", errMsg));
		} catch(HttpServerErrorException e) {
			String errState = "PVE 5xx";
			String errMsg = "Proxmox 서버에 문제가 발생했습니다.";
			log.error(String.format("ERROR: %s", errState), e);
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
					.body(Map.of("message", errMsg));
		} catch(ResourceAccessException e) {
			String errState = "PVE 연결 불가·타임아웃";
			String errMsg = "Proxmox 서버에 연결할 수 없습니다.";
			log.error(String.format("ERROR: %s", errState), e);
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(Map.of("message", errMsg));
		} catch(RestClientException e) {
			String errState = "응답파싱 실패";
			String errMsg = "서버 내부 오류";
			log.error(String.format("ERROR: %s", errState), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", errMsg));
		} catch(Exception e) {
			String errState = "알 수 없는 오류";
			String errMsg = "알 수 없는 오류";
			log.error(String.format("ERROR: %s", errState), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", errMsg));
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
