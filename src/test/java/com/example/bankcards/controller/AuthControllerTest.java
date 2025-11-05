package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RefreshTokenRequest;
import com.example.bankcards.security.JwtTokenUtil;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private JwtTokenUtil jwtTokenUtil;

	@MockBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void login_validCredentials_shouldReturnTokens() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setUsername("user");
		request.setPassword("password");

		Authentication auth = mock(Authentication.class);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
		when(auth.getName()).thenReturn("user");
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		doReturn(authorities).when(auth).getAuthorities();
		when(auth.getPrincipal()).thenReturn(mock(org.springframework.security.core.userdetails.UserDetails.class));
		when(jwtTokenUtil.generateAccessToken(any(Authentication.class))).thenReturn("access-token");
		when(jwtTokenUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("access-token"))
				.andExpect(jsonPath("$.refreshToken").value("refresh-token"))
				.andExpect(jsonPath("$.type").value("Bearer"))
				.andExpect(jsonPath("$.username").value("user"));

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtTokenUtil).generateAccessToken(any(Authentication.class));
		verify(jwtTokenUtil).generateRefreshToken("user");
	}

	@Test
	void login_invalidCredentials_shouldReturnUnauthorized() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setUsername("user");
		request.setPassword("wrong");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
	}

	@Test
	void refresh_validToken_shouldReturnNewTokens() throws Exception {
		RefreshTokenRequest request = new RefreshTokenRequest();
		request.setRefreshToken("valid-refresh-token");

		when(jwtTokenUtil.validate("valid-refresh-token")).thenReturn(true);
		when(jwtTokenUtil.isRefresh("valid-refresh-token")).thenReturn(true);
		when(jwtTokenUtil.getUsername("valid-refresh-token")).thenReturn("user");
		org.springframework.security.core.userdetails.UserDetails userDetails = mock(org.springframework.security.core.userdetails.UserDetails.class);
		when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
		when(jwtTokenUtil.generateAccessToken(any(org.springframework.security.core.userdetails.UserDetails.class))).thenReturn("new-access-token");
		when(jwtTokenUtil.generateRefreshToken("user")).thenReturn("new-refresh-token");

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("new-access-token"))
				.andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

		verify(jwtTokenUtil).validate("valid-refresh-token");
		verify(jwtTokenUtil).isRefresh("valid-refresh-token");
	}

	@Test
	void refresh_invalidToken_shouldReturnUnauthorized() throws Exception {
		RefreshTokenRequest request = new RefreshTokenRequest();
		request.setRefreshToken("invalid-token");

		when(jwtTokenUtil.validate("invalid-token")).thenReturn(false);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());

		verify(jwtTokenUtil).validate("invalid-token");
		verify(jwtTokenUtil, never()).isRefresh(anyString());
	}

	@Test
	void refresh_notRefreshToken_shouldReturnUnauthorized() throws Exception {
		RefreshTokenRequest request = new RefreshTokenRequest();
		request.setRefreshToken("access-token");

		when(jwtTokenUtil.validate("access-token")).thenReturn(true);
		when(jwtTokenUtil.isRefresh("access-token")).thenReturn(false);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());

		verify(jwtTokenUtil).validate("access-token");
		verify(jwtTokenUtil).isRefresh("access-token");
	}
}
