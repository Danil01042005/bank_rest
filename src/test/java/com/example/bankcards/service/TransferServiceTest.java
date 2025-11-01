package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setOwner(testUser);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setOwner(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testTransfer_Success() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwner(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwner(2L, testUser)).thenReturn(Optional.of(toCard));

        assertDoesNotThrow(() -> transferService.transferBetweenOwnCards(request));

        assertEquals(new BigDecimal("900.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("600.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void testTransfer_InsufficientFunds() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("2000.00"));

        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwner(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwner(2L, testUser)).thenReturn(Optional.of(toCard));

        assertThrows(BadRequestException.class, () -> transferService.transferBetweenOwnCards(request));
    }

    @Test
    void testTransfer_SameCard() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(1L);
        request.setAmount(new BigDecimal("100.00"));

        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwner(1L, testUser)).thenReturn(Optional.of(fromCard));

        assertThrows(BadRequestException.class, () -> transferService.transferBetweenOwnCards(request));
    }

    @Test
    void testTransfer_CardNotFound() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwner(1L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transferService.transferBetweenOwnCards(request));
    }
}


