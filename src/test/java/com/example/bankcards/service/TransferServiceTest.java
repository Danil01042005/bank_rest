package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
	@Mock CardRepository cardRepository;
	@Mock UserRepository userRepository;
	@Mock Authentication authentication;
	@Mock SecurityContext securityContext;
	@InjectMocks TransferService transferService;
	User user; Card from; Card to;
	@BeforeEach void setUp() {
		user = new User(); user.setId(1L); user.setUsername("u");
		from = new Card(); from.setId(1L); from.setOwner(user); from.setStatus(Card.CardStatus.ACTIVE); from.setBalance(new BigDecimal("100.00"));
		to = new Card(); to.setId(2L); to.setOwner(user); to.setStatus(Card.CardStatus.ACTIVE); to.setBalance(new BigDecimal("50.00"));
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("u");
		SecurityContextHolder.setContext(securityContext);
		when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
	}
	@Test void ok() {
		when(cardRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(from));
		when(cardRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(to));
		assertDoesNotThrow(() -> transferService.betweenOwn(1L, 2L, new BigDecimal("10.00")));
		verify(cardRepository, times(2)).save(any(Card.class));
	}
	@Test void insufficient() {
		when(cardRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(from));
		when(cardRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(to));
		assertThrows(BadRequestException.class, () -> transferService.betweenOwn(1L, 2L, new BigDecimal("1000.00")));
	}
}
