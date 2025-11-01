package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {
    @Autowired
    private TransferService transferService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> transferBetweenCards(@Valid @RequestBody TransferRequest request) {
        transferService.transferBetweenOwnCards(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Перевод выполнен успешно");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}


