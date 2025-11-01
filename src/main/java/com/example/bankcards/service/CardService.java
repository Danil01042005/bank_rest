package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CardDTO createCard(CardCreateRequest request, boolean isAdmin) {
        // Проверка на уникальность номера карты
        String encryptedCardNumber = CardEncryptionUtil.encrypt(request.getCardNumber());
        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            throw new BadRequestException("Карта с таким номером уже существует");
        }

        // Определение владельца
        User owner;
        if (isAdmin && request.getOwnerId() != null) {
            owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + request.getOwnerId()));
        } else {
            // Для обычных пользователей - текущий пользователь
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            owner = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        }

        // Проверка срока действия
        if (request.getExpirationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Срок действия карты истек");
        }

        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setCardHolderName(request.getCardHolderName());
        card.setExpirationDate(request.getExpirationDate());
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setBalance(java.math.BigDecimal.ZERO);
        card.setOwner(owner);

        Card savedCard = cardRepository.save(card);
        return CardDTO.fromEntity(savedCard);
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> getAllCards(Pageable pageable, boolean isAdmin) {
        if (isAdmin) {
            return cardRepository.findAll(pageable)
                    .map(CardDTO::fromEntity);
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            return cardRepository.findByOwner(user, pageable)
                    .map(CardDTO::fromEntity);
        }
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> searchCards(String search, Pageable pageable, boolean isAdmin) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (!isAdmin) {
            return cardRepository.findByOwnerAndSearch(user, search, pageable)
                    .map(CardDTO::fromEntity);
        } else {
            // Для админа можно добавить более сложный поиск
            return cardRepository.findAll(pageable)
                    .map(CardDTO::fromEntity);
        }
    }

    @Transactional(readOnly = true)
    public CardDTO getCardById(Long id, boolean isAdmin) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Card card;
        if (isAdmin) {
            card = cardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Карта не найдена с id: " + id));
        } else {
            card = cardRepository.findByIdAndOwner(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Карта не найдена с id: " + id));
        }

        return CardDTO.fromEntity(card);
    }

    @Transactional
    public CardDTO blockCard(Long id, boolean isAdmin) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Card card;
        if (isAdmin) {
            card = cardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Карта не найдена с id: " + id));
        } else {
            card = cardRepository.findByIdAndOwner(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Карта не найдена с id: " + id));
            // Обычный пользователь может только запросить блокировку
            if (card.getStatus() == Card.CardStatus.ACTIVE) {
                card.setStatus(Card.CardStatus.BLOCKED);
            } else {
                throw new BadRequestException("Невозможно заблокировать карту со статусом: " + card.getStatus());
            }
        }

        if (card.getStatus() == Card.CardStatus.EXPIRED) {
            throw new BadRequestException("Нельзя заблокировать карту с истекшим сроком действия");
        }

        card.setStatus(Card.CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        return CardDTO.fromEntity(savedCard);
    }

    @Transactional
    public CardDTO activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Карта не найдена с id: " + id));

        if (card.getStatus() == Card.CardStatus.EXPIRED) {
            throw new BadRequestException("Нельзя активировать карту с истекшим сроком действия");
        }

        card.setStatus(Card.CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        return CardDTO.fromEntity(savedCard);
    }

    @Transactional
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Карта не найдена с id: " + id);
        }
        cardRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getUserCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + userId));
        return cardRepository.findByOwner(user).stream()
                .map(CardDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

