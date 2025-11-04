package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<CardDTO>> all(@RequestParam(defaultValue = "0") int page,
	                                         @RequestParam(defaultValue = "10") int size,
	                                         @RequestParam(required = false) String sortBy,
	                                         @RequestParam(required = false) String search) {
		Sort sort = sortBy != null ? Sort.by(sortBy) : Sort.by("id");
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseEntity.ok(cardService.adminList(search, pageable));
	}
}

