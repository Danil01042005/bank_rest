package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private String cardNumber; // Маскированный номер
    private String cardHolderName;
    private LocalDate expirationDate;
    private Card.CardStatus status;
    private BigDecimal balance;
    private Long ownerId;
    private String ownerUsername;

    public static CardDTO fromEntity(com.example.bankcards.entity.Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardNumber(com.example.bankcards.util.CardMaskingUtil.maskEncryptedCardNumber(card.getEncryptedCardNumber()));
        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setOwnerId(card.getOwner().getId());
        dto.setOwnerUsername(card.getOwner().getUsername());
        return dto;
    }
}


