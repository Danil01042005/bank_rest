package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void transferBetweenOwnCards(TransferRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Получаем карты
        Card fromCard = cardRepository.findByIdAndOwner(request.getFromCardId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Карта отправителя не найдена или вам не принадлежит"));

        Card toCard = cardRepository.findByIdAndOwner(request.getToCardId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Карта получателя не найдена или вам не принадлежит"));

        // Проверки
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BadRequestException("Нельзя переводить средства на ту же карту");
        }

        if (fromCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new BadRequestException("Карта отправителя должна быть активна");
        }

        if (toCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new BadRequestException("Карта получателя должна быть активна");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Недостаточно средств на карте отправителя");
        }

        // Выполняем перевод
        BigDecimal fromBalance = fromCard.getBalance().subtract(request.getAmount());
        BigDecimal toBalance = toCard.getBalance().add(request.getAmount());

        fromCard.setBalance(fromBalance);
        toCard.setBalance(toBalance);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}

