package dev.the2davi.lab.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.security.util.JwtUtil;

@RestController
public class TestAuthController {

	private final JwtUtil jwtUtil;
	public TestAuthController(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}
	
	@GetMapping("/api/public/token")
	public String getTestToken() {
		return jwtUtil.generateToken("kcy0122-admin");
	}
}
