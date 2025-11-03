package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CardCreateRequest {
	@NotBlank
	@Pattern(regexp = "^[0-9]{16}$")
	private String cardNumber;
	
	@NotBlank
	private String cardHolderName;
	
	@NotNull
	private LocalDate expirationDate;
	
	private Long ownerId;
}

