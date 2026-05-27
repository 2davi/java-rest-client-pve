package dev.the2davi.lab.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.the2davi.lab.security.filter.JwtAuthenticationFilter;
import dev.the2davi.lab.security.handler.AuthenticationEntryPoint;
import dev.the2davi.lab.security.util.JwtUtil;

@Configuration @EnableWebSecurity
public class SecurityConfig {
	
	private final JwtUtil jwtUtil;
	private final AuthenticationEntryPoint entryPoint;
	
	public SecurityConfig(JwtUtil jwtUtil, AuthenticationEntryPoint entryPoint) {
		this.jwtUtil = jwtUtil;
		this.entryPoint = entryPoint;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
		http
			//REST API일 땐 CSRF 방어 비활성
			.csrf(csrf -> csrf.disable())
			
			//폼로그인, HTTP Basic 인증 비활성
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			
			//서버에 세션 상태 저장 안 하겠다
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			
			//인증 에러를 핸들링할 entryPoint를 커스텀하겠다.
			.exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
			
			//경로별 권한 설정
			.authorizeHttpRequests(auth -> auth
					.requestMatchers("/api/public/**", "/auth/**").permitAll()
					.requestMatchers("/api/proxmox/**").authenticated()
					.anyRequest().authenticated()
			)

			.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
}
