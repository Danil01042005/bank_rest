package com.example.bankcards.service;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TransferService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void betweenOwn(Long fromCardId, Long toCardId, java.math.BigDecimal amount) {
        User u = current();
        Card from = cardRepository.findByIdAndOwner(fromCardId, u).orElseThrow(() -> new ResourceNotFoundException("From card not found"));
        Card to = cardRepository.findByIdAndOwner(toCardId, u).orElseThrow(() -> new ResourceNotFoundException("To card not found"));
        if (from.getId().equals(to.getId())) throw new BadRequestException("Same card");
        if (from.getStatus() != Card.CardStatus.ACTIVE || to.getStatus() != Card.CardStatus.ACTIVE) throw new BadRequestException("Cards must be ACTIVE");
        if (from.getBalance().compareTo(amount) < 0) throw new BadRequestException("Insufficient funds");
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        cardRepository.save(from);
        cardRepository.save(to);
    }
	private User current() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		return userRepository.findByUsername(a.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
