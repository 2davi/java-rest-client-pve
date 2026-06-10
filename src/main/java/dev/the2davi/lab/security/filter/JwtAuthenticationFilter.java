package dev.the2davi.lab.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.the2davi.lab.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	public JwtAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		try {
			String jwt = jwtUtil.parseJwt(request);
			Claims claims = (jwt != null) ? jwtUtil.getClaims(jwt) : null;

			//* if(jwt != null && jwtUtil.validateToken(jwt)) {
			if(claims != null) {
				//토큰이 유효성 검증을 통과한 이후.
				//* String userId = jwtUtil.getUserIdFromToken(jwt);
				String username = claims.getSubject();
				String pveTicket = claims.get("pve_ticket", String.class);
				String pveCsrf = claims.get("pve_csrf", String.class);
				
				UsernamePasswordAuthenticationToken authentication = 
						//* new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
						new UsernamePasswordAuthenticationToken(username, null, List.of());
				
				//** authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				Map<String, String> pveDetails = Map.of(
					"pve_ticket", (pveTicket != null) ? pveTicket : "",
					"pve_csrf", (pveCsrf != null) ? pveCsrf : ""
				);
				authentication.setDetails(pveDetails);
				
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch(Exception e) {
			logger.error("cannot set user authentication:", e);
		}
		filterChain.doFilter(request, response);
	}


}
