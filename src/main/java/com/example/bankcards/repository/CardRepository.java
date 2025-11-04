package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
	Page<Card> findByOwner(User owner, Pageable pageable);
	List<Card> findByOwner(User owner);
	Optional<Card> findByIdAndOwner(Long id, User owner);
	boolean existsByEncryptedCardNumber(String encryptedCardNumber);

	@Query("SELECT c FROM Card c WHERE " +
	       "LOWER(c.cardHolderName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
	       "c.last4 LIKE CONCAT('%', :q, '%') OR " +
	       "CAST(c.id AS string) LIKE CONCAT('%', :q, '%') OR " +
	       "LOWER(c.owner.username) LIKE LOWER(CONCAT('%', :q, '%'))")
	Page<Card> searchAll(@Param("q") String q, Pageable pageable);

	@Query("SELECT c FROM Card c WHERE c.owner = :owner AND (" +
	       "LOWER(c.cardHolderName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
	       "c.last4 LIKE CONCAT('%', :q, '%') OR " +
	       "CAST(c.id AS string) LIKE CONCAT('%', :q, '%'))")
	Page<Card> searchByOwner(@Param("owner") User owner, @Param("q") String q, Pageable pageable);
}
