package com.example.bankcards.entity;

public enum UserRole {
	ADMIN,
	USER;

	public String getAuthority() {
		return "ROLE_" + this.name();
	}
}

