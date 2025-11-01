package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен состоять из 16 цифр")
    private String cardNumber;

    @NotBlank(message = "Имя владельца обязательно")
    private String cardHolderName;

    @NotNull(message = "Дата истечения обязательна")
    private LocalDate expirationDate;

    private Long ownerId; // Для ADMIN - можно указать владельца
}


