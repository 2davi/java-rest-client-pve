package dev.the2davi.lab.security.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <pre>
 * 
 *  
 * << 개정이력 >>
 *   
 *  수정일      수정자		수정내용
 *  ------------------------------------------------
 *  2026-06-10  kcy0122			최초 생성
 * </pre>
 */
@Component
public class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
	private final SecretKey secretKey;
	
	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	//Token에서 username 뽑아오기
//	public String getUserIdFromToken(String token) {
//		Claims claims = Jwts.parser()
//						.verifyWith(secretKey)
//						.build()
//						.parseSignedClaims(token)
//						.getPayload();
//		return claims.getSubject();
//	}

//	public Claims getClaims(String token) {
//		return Jwts.parser()
//				.verifyWith(secretKey)
//				.build()
//				.parseSignedClaims(token)
//				.getPayload();
//	}
	
	//Token의 진위/만료 여부를 검증하여, Claims 또는 Null 객체를 반환
	public Claims getClaims(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch(ExpiredJwtException e) {
			//만료된 토큰: 토큰 생명주기상 지극히 정상. 그냥 잡음이라 DEBUG.
			log.debug("만료된 토큰 거부: {}", e.getMessage());
		} catch(SignatureException e) {
			//서명 조작된 위조 토큰: 위조 가능성 → 약한 보안 신호라 WARN.
	        log.warn("서명 검증 실패 — 위조 의심 토큰 거부: {}", e.getMessage());
		} catch(MalformedJwtException | UnsupportedJwtException e) {
			//지원 않는 형식의 토큰: 클라이언트가 쓰레기를 보낸 것. DEBUG.
	        log.debug("형식 불량/미지원 토큰 거부: {}", e.getMessage());
		} catch(IllegalArgumentException e) {
			//null/빈/공백 문자열: 사실상 '토큰 없음'. DEBUG.
	        log.debug("빈 토큰 거부: {}", e.getMessage());
		} catch(JwtException e) {
	        // 위에서 안 걸린 그 외 JWT 예외 전부 ─ 안전망(catch-all).
	        log.warn("기타 JWT 예외로 토큰 거부: {}", e.getMessage());
		}
		return null;
	}
	public String getUsernameFromToken(String token) {
		Claims claims = this.getClaims(token);
		return (claims != null) ? claims.getSubject() : null;
	}
	
	//JWT Token 발급
	public String generateToken(String userId) {
		return Jwts.builder()
				.subject(userId)
				.issuedAt(new java.util.Date())
				.expiration(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * /*24*/ 2))
				.signWith(secretKey)
				.compact();
	}
	
	//JWT Token 발급
	public String createToken(String username, String ticket, String CSRFPreventionToken) {
		return Jwts.builder()
				.claims()
					.subject(username)
					.issuedAt(new java.util.Date())
					.add("pve_ticket", ticket)
					.add("pve_csrf", CSRFPreventionToken)
					.and()
				.expiration(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
				.signWith(secretKey)
				.compact();
	}
	
	/**
	 * <pre>
	 * 요청(Request) 객체로부터 Jwt(PVE API Token)를 읽어내는 함수
	 * 
	 * 사용처: **JwtAuthenticationFilter**
	 * </pre>
	 * @param request
	 * @return
	 * @author kcy0122
	 * @since 2026-06-10
	 */
	public String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7);
		}
		return null;
	}
}
