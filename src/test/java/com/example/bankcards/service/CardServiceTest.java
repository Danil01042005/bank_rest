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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testCard = new Card();
        testCard.setId(1L);
        testCard.setEncryptedCardNumber(CardEncryptionUtil.encrypt("1234567890123456"));
        testCard.setCardHolderName("Test User");
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setStatus(Card.CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.ZERO);
        testCard.setOwner(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreateCard_Success() {
        CardCreateRequest request = new CardCreateRequest();
        request.setCardNumber("1234567890123456");
        request.setCardHolderName("Test User");
        request.setExpirationDate(LocalDate.now().plusYears(2));

        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByEncryptedCardNumber(any())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        var result = cardService.createCard(request, false);

        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testCreateCard_DuplicateCardNumber() {
        CardCreateRequest request = new CardCreateRequest();
        request.setCardNumber("1234567890123456");
        request.setCardHolderName("Test User");
        request.setExpirationDate(LocalDate.now().plusYears(2));

        when(cardRepository.existsByEncryptedCardNumber(any())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> cardService.createCard(request, false));
    }

    @Test
    void testCreateCard_ExpiredDate() {
        CardCreateRequest request = new CardCreateRequest();
        request.setCardNumber("1234567890123456");
        request.setCardHolderName("Test User");
        request.setExpirationDate(LocalDate.now().minusDays(1));

        when(cardRepository.existsByEncryptedCardNumber(any())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> cardService.createCard(request, false));
    }
}


