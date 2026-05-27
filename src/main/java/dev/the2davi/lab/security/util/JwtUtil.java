package dev.the2davi.lab.security.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

	private final SecretKey secretKey;
	
	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	//Token에서 username 뽑아오기
	public String getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
						.verifyWith(secretKey)
						.build()
						.parseSignedClaims(token)
						.getPayload();
		return claims.getSubject();
	}
	
	//Token 진위/만료 여부 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			return true;
		} catch(SignatureException e) {
			//서명 조작된 위조 토큰
		} catch(MalformedJwtException e) {
			//JWT 형식 불일치
		} catch(ExpiredJwtException e) {
			//만료된 토큰
		} catch(UnsupportedJwtException e) {
			//지원 않는 형식의 토큰
		} catch(IllegalArgumentException e) {
			//토큰이 
		}
		return false;
	}
	
	//JWT Token 발급
	public String generateToken(String userId) {
		return Jwts.builder()
				.subject(userId)
				.issuedAt(new java.util.Date())
				.expiration(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
				.signWith(secretKey)
				.compact();
	}
}
