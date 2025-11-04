package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardMaskingUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardDTO {
	private Long id;
	private String cardNumber;
	private String cardHolderName;
	private LocalDate expirationDate;
	private Card.CardStatus status;
	private BigDecimal balance;
	private Long ownerId;
	private String ownerUsername;

	public static CardDTO fromEntity(Card c) {
		CardDTO d = new CardDTO();
		d.setId(c.getId());
		d.setCardNumber(CardMaskingUtil.maskEncryptedCardNumber(c.getEncryptedCardNumber()));
		d.setCardHolderName(c.getCardHolderName());
		d.setExpirationDate(c.getExpirationDate());
		d.setStatus(c.getStatus());
		d.setBalance(c.getBalance());
		d.setOwnerId(c.getOwner().getId());
		d.setOwnerUsername(c.getOwner().getUsername());
		return d;
	}
}


