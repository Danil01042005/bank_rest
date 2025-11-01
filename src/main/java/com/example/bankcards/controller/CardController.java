package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {
    @Autowired
    private CardService cardService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardCreateRequest request) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        CardDTO card = cardService.createCard(request, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String search) {
        Sort sort = sortBy != null ? Sort.by(sortBy) : Sort.by("id");
        Pageable pageable = PageRequest.of(page, size, sort);

        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<CardDTO> cards;
        if (search != null && !search.isEmpty()) {
            cards = cardService.searchCards(search, pageable, isAdmin);
        } else {
            cards = cardService.getAllCards(pageable, isAdmin);
        }

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        CardDTO card = cardService.getCardById(id, isAdmin);
        return ResponseEntity.ok(card);
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardDTO> blockCard(@PathVariable Long id) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        CardDTO card = cardService.blockCard(id, isAdmin);
        return ResponseEntity.ok(card);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDTO> activateCard(@PathVariable Long id) {
        CardDTO card = cardService.activateCard(id);
        return ResponseEntity.ok(card);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}


