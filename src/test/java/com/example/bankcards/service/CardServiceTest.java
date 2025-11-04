package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
	@Mock private CardRepository cardRepository;
	@Mock private UserRepository userRepository;
	@Mock private Authentication authentication;
	@Mock private SecurityContext securityContext;
	@InjectMocks private CardService cardService;

	private User user;

	@BeforeEach
	void setup() {
		user = new User(); user.setId(1L); user.setUsername("u");
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getName()).thenReturn("u");
		when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
	}

	@Test
	void mineSearch_ok() {
		Pageable p = PageRequest.of(0,10);
		when(cardRepository.findByOwnerAndCardHolderNameContainingIgnoreCase(eq(user), anyString(), eq(p)))
				.thenReturn(new PageImpl<>(List.of()));
		Page<?> res = cardService.mineSearch("test", p);
		assertNotNull(res);
		verify(cardRepository).findByOwnerAndCardHolderNameContainingIgnoreCase(eq(user), eq("test"), eq(p));
	}

	@Test
	void adminList_ok() {
		Pageable p = PageRequest.of(0,10);
		when(cardRepository.findAll(p)).thenReturn(new PageImpl<>(List.of()));
		assertNotNull(cardService.adminList(p));
		verify(cardRepository).findAll(p);
	}

	@Test
	void activateAdmin_notFound() {
		when(cardRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> cardService.activateAdmin(99L));
	}

	@Test
	void deleteAdmin_notFound() {
		when(cardRepository.existsById(99L)).thenReturn(false);
		assertThrows(ResourceNotFoundException.class, () -> cardService.deleteAdmin(99L));
	}

	@Test void create_ok() {
		CardCreateRequest req = new CardCreateRequest();
		req.setCardNumber("1234567812345678"); req.setCardHolderName("U"); req.setExpirationDate(LocalDate.now().plusYears(1));
		when(cardRepository.existsByEncryptedCardNumber(any())).thenReturn(false);
		when(cardRepository.save(any(Card.class))).thenAnswer(i -> { Card c = i.getArgument(0); c.setId(1L); return c; });
		assertNotNull(cardService.create(req, false));
	}
	@Test void create_duplicate() {
		CardCreateRequest req = new CardCreateRequest();
		req.setCardNumber("1234567812345678"); req.setCardHolderName("U"); req.setExpirationDate(LocalDate.now().plusYears(1));
		when(cardRepository.existsByEncryptedCardNumber(any())).thenReturn(true);
		assertThrows(BadRequestException.class, () -> cardService.create(req, false));
	}
}
