package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
	private String token;
	private String refreshToken;
	private String type = "Bearer";
	private String username;
	private Set<String> roles;
}


