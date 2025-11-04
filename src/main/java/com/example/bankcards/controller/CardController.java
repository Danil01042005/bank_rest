package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CardController {
	private final CardService cardService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	public ResponseEntity<CardDTO> create(@Valid @RequestBody CardCreateRequest req, Authentication auth) {
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.create(req.getCardNumber(), req.getCardHolderName(), req.getExpirationDate(), req.getOwnerId(), isAdmin));
	}
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	public ResponseEntity<Page<CardDTO>> mine(@RequestParam(defaultValue = "0") int page,
	                                          @RequestParam(defaultValue = "10") int size,
	                                          @RequestParam(required = false) String sortBy,
	                                          @RequestParam(required = false) String search) {
		Sort sort = sortBy != null ? Sort.by(sortBy) : Sort.by("id");
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseEntity.ok(cardService.mine(pageable, search));
	}
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	public ResponseEntity<CardDTO> byId(@PathVariable Long id) { return ResponseEntity.ok(cardService.byId(id)); }
	@PutMapping("/{id}/block")
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	public ResponseEntity<CardDTO> block(@PathVariable Long id) { return ResponseEntity.ok(cardService.block(id)); }

	@PutMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CardDTO> activate(@PathVariable Long id) { return ResponseEntity.ok(cardService.activate(id)); }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDTO> update(@PathVariable Long id, @Valid @RequestBody CardUpdateRequest req) {
        return ResponseEntity.ok(
                cardService.updateAdmin(id, req.getCardHolderName(), req.getExpirationDate(), req.getStatus(), req.getBalance(), req.getOwnerId())
        );
    }
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) { cardService.delete(id); return ResponseEntity.noContent().build(); }
}
