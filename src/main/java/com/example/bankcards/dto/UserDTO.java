package com.example.bankcards.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserDTO {
	private Long id;
	private String username;
	private String email;
	private String fullName;
	private Set<String> roles;
}


