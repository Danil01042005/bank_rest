package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RefreshTokenRequest;
import com.example.bankcards.security.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserDetailsService userDetailsService;

	@PostMapping("/login")
	public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getUsername(),
						loginRequest.getPassword()
				)
		);

		String accessToken = jwtTokenUtil.generateAccessToken(authentication);
		String refreshToken = jwtTokenUtil.generateRefreshToken(authentication.getName());
		Set<String> roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.map(authority -> authority.replace("ROLE_", ""))
				.collect(Collectors.toSet());

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		JwtResponse response = new JwtResponse();
		response.setToken(accessToken);
		response.setRefreshToken(refreshToken);
		response.setType("Bearer");
		response.setUsername(userDetails.getUsername());
		response.setRoles(roles);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();
		if (!jwtTokenUtil.validateToken(refreshToken) || !jwtTokenUtil.isRefreshToken(refreshToken)) {
			return ResponseEntity.status(401).build();
		}
		String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
		// Подгружаем актуальные роли пользователя из БД
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
		String newRefreshToken = jwtTokenUtil.generateRefreshToken(username); // ротация

		JwtResponse response = new JwtResponse();
		response.setToken(newAccessToken);
		response.setRefreshToken(newRefreshToken);
		response.setType("Bearer");
		response.setUsername(username);
		response.setRoles(userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.map(a -> a.replace("ROLE_", ""))
				.collect(Collectors.toSet()));

		return ResponseEntity.ok(response);
	}
}

