package dev.the2davi.lab.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.the2davi.lab.security.filter.JwtAuthenticationFilter;
import dev.the2davi.lab.security.handler.AuthenticationEntryPoint;
import dev.the2davi.lab.security.session.SecuritySessionStore;
import dev.the2davi.lab.security.util.JwtUtil;

@Configuration @EnableWebSecurity
public class SecurityConfig {
	
	@SuppressWarnings("unused")
	private final JwtUtil jwtUtil;
	private final AuthenticationEntryPoint entryPoint;
	
	public SecurityConfig(JwtUtil jwtUtil, AuthenticationEntryPoint entryPoint) {
		this.jwtUtil = jwtUtil;
		this.entryPoint = entryPoint;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil, SecuritySessionStore sessionStore) throws Exception {
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
					//정적 파일의 404 에러는 JWT 토큰이 없기 때문에, Security에서 401 권한없음 에러로 덮어씌울 수 있다. /error도 .permitAll()에 포함시켜서 해결.
					//* .requestMatchers("/", "/index.html", "/css/**", "/js/**" , "/favicon.ico", "/error").permitAll()
					.requestMatchers("/api/public/**", "/auth/**").permitAll()
					.requestMatchers("/api/proxmox/**").authenticated()
					.anyRequest().authenticated()
			)

			.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, sessionStore), UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		//정적 파일들은 permitAll()로 막기보다, 아예 시큐리티 필터 자체를 통째로 건너뛰게 최전선에서 패스시키는 것이 훨씬 깔끔하고 성능상으로 이득이다.
		return web -> web.ignoring().requestMatchers(
				"/favicon.ico"
				, "/css/**"
				, "/js/**"
				, "/index.html"
				, "/"
		);
	}
}
