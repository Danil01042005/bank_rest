package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RefreshTokenRequest;
import com.example.bankcards.security.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final UserDetailsService userDetailsService;

	@PostMapping("/login")
	public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
		Authentication a = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
		String access = jwtTokenUtil.generateAccessToken(a);
		String refresh = jwtTokenUtil.generateRefreshToken(a.getName());
		Set<String> roles = a.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(r -> r.replace("ROLE_",""))
				.collect(Collectors.toSet());
		UserDetails ud = (UserDetails) a.getPrincipal();
		JwtResponse resp = new JwtResponse();
		resp.setToken(access); resp.setRefreshToken(refresh); resp.setType("Bearer"); resp.setUsername(ud.getUsername()); resp.setRoles(roles);
		return ResponseEntity.ok(resp);
	}
	@PostMapping("/refresh")
	public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
		if (!jwtTokenUtil.validate(req.getRefreshToken()) || !jwtTokenUtil.isRefresh(req.getRefreshToken())) return ResponseEntity.status(401).build();
		String username = jwtTokenUtil.getUsername(req.getRefreshToken());
		UserDetails ud = userDetailsService.loadUserByUsername(username);
		String access = jwtTokenUtil.generateAccessToken(ud);
		String refresh = jwtTokenUtil.generateRefreshToken(username);
		JwtResponse resp = new JwtResponse();
		resp.setToken(access); resp.setRefreshToken(refresh); resp.setType("Bearer"); resp.setUsername(username);
		resp.setRoles(ud.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(r -> r.replace("ROLE_","")).collect(Collectors.toSet()));
		return ResponseEntity.ok(resp);
	}
}

