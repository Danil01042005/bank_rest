package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest req) {
        transferService.betweenOwn(req.getFromCardId(), req.getToCardId(), req.getAmount());
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "OK"));
	}
}


