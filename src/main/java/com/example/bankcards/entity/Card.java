package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String encryptedCardNumber; // Зашифрованный номер карты

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public enum CardStatus {
        ACTIVE,
        BLOCKED,
        EXPIRED
    }

    @PrePersist
    @PreUpdate
    public void checkExpiration() {
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            if (status == CardStatus.ACTIVE || status == CardStatus.BLOCKED) {
                this.status = CardStatus.EXPIRED;
            }
        }
    }
}


