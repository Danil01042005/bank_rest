package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardUpdateRequest {
    private String cardHolderName;
    private LocalDate expirationDate;
    private Card.CardStatus status;
    private BigDecimal balance;
    private Long ownerId;
}
