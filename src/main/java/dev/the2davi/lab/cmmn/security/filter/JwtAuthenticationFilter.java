package dev.the2davi.lab.cmmn.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.the2davi.lab.cmmn.security.session.SecuritySession;
import dev.the2davi.lab.cmmn.security.session.SecuritySessionStore;
import dev.the2davi.lab.cmmn.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final SecuritySessionStore sessionStore;
	
	public JwtAuthenticationFilter(JwtUtil jwtUtil, SecuritySessionStore sessionStore) {
		this.jwtUtil = jwtUtil;
		this.sessionStore = sessionStore;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		try {
			String jwt = jwtUtil.parseJwt(request);
			Claims claims = (jwt != null) ? jwtUtil.getClaims(jwt) : null;

			if(claims != null) {
				//토큰이 유효성 검증을 통과한 이후.
				String sid = claims.get("sid", String.class);
				SecuritySession session = sessionStore.find(sid);
				
				if(session != null) {
					UsernamePasswordAuthenticationToken authentication = 
							new UsernamePasswordAuthenticationToken(session.username(), null, List.of());
					
					Map<String, String> pveDetails = Map.of(
							"pve_ticket", session.ticket(),
							"pve_csrf", session.CSRFPreventionToken()
							);
					
					authentication.setDetails(pveDetails);
					
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} catch(Exception e) {
			logger.error("cannot set user authentication:", e);
		}
		filterChain.doFilter(request, response);
	}


}
