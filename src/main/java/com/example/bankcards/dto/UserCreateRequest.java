package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserCreateRequest {
	@NotBlank @Size(min = 3, max = 50) private String username;
	@NotBlank @Size(min = 6) private String password;
	@NotBlank @Email private String email;
	@NotBlank private String fullName;
	private Set<String> roles;
}


