package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CardService {
	private final CardRepository cardRepository;
	private final UserRepository userRepository;

    @Transactional
    public CardDTO create(String cardNumber, String cardHolderName, java.time.LocalDate expirationDate, Long ownerId, boolean isAdmin) {
        String encrypted = CardEncryptionUtil.encrypt(cardNumber);
        if (cardRepository.existsByEncryptedCardNumber(encrypted)) throw new BadRequestException("Card exists");
        if (expirationDate.isBefore(LocalDate.now())) throw new BadRequestException("Expired date");
        User owner = resolveOwner(ownerId, isAdmin);
        Card c = new Card();
        c.setEncryptedCardNumber(encrypted);
        c.setLast4(cardNumber.substring(cardNumber.length() - 4));
        c.setCardHolderName(cardHolderName);
        c.setExpirationDate(expirationDate);
        c.setStatus(Card.CardStatus.ACTIVE);
        c.setOwner(owner);
        return CardDTO.fromEntity(cardRepository.save(c));
    }
	@Transactional(readOnly = true)
	public Page<CardDTO> mine(Pageable pageable, String search) {
		User u = current();
		if (search == null || search.isBlank()) return cardRepository.findByOwner(u, pageable).map(CardDTO::fromEntity);
		return cardRepository.searchByOwner(u, search, pageable).map(CardDTO::fromEntity);
	}
	@Transactional(readOnly = true)
	public Page<CardDTO> adminList(String search, Pageable pageable) {
		if (search == null || search.isBlank()) return cardRepository.findAll(pageable).map(CardDTO::fromEntity);
		return cardRepository.searchAll(search, pageable).map(CardDTO::fromEntity);
	}
	@Transactional(readOnly = true)
	public CardDTO byId(Long id) {
		User u = current();
		Card c = cardRepository.findByIdAndOwner(id, u).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
		return CardDTO.fromEntity(c);
	}
	@Transactional public CardDTO block(Long id) {
		User u = current();
		Card c = cardRepository.findByIdAndOwner(id, u).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
		if (c.getStatus() == Card.CardStatus.EXPIRED) throw new BadRequestException("Already expired");
		c.setStatus(Card.CardStatus.BLOCKED);
		return CardDTO.fromEntity(cardRepository.save(c));
	}
	@Transactional public CardDTO activate(Long id) {
		Card c = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
		if (c.getStatus() == Card.CardStatus.EXPIRED) throw new BadRequestException("Cannot activate expired");
		c.setStatus(Card.CardStatus.ACTIVE);
		return CardDTO.fromEntity(cardRepository.save(c));
	}
    
	@Transactional public void delete(Long id) {
		if (!cardRepository.existsById(id)) throw new ResourceNotFoundException("Card not found");
		cardRepository.deleteById(id);
	}

    @Transactional public CardDTO updateAdmin(Long id, String cardHolderName, java.time.LocalDate expirationDate,
                                              Card.CardStatus status, java.math.BigDecimal balance, Long ownerId) {
        Card c = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        if (cardHolderName != null) c.setCardHolderName(cardHolderName);
        if (expirationDate != null) c.setExpirationDate(expirationDate);
        if (status != null) c.setStatus(status);
        if (balance != null) c.setBalance(balance);
        if (ownerId != null) {
            User newOwner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
            c.setOwner(newOwner);
        }
        return CardDTO.fromEntity(cardRepository.save(c));
    }
	private User current() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		return userRepository.findByUsername(a.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
	private User resolveOwner(Long ownerId, boolean isAdmin) {
		if (isAdmin && ownerId != null) return userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
		return current();
	}
}
