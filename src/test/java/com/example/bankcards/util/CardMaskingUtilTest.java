package com.example.bankcards.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardMaskingUtilTest {
    @Test
    void testMaskCardNumber_ValidCard() {
        String cardNumber = "1234567890123456";
        String masked = CardMaskingUtil.maskCardNumber(cardNumber);
        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void testMaskCardNumber_WithSpaces() {
        String cardNumber = "1234 5678 9012 3456";
        String masked = CardMaskingUtil.maskCardNumber(cardNumber);
        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void testMaskCardNumber_ShortNumber() {
        String cardNumber = "123";
        String masked = CardMaskingUtil.maskCardNumber(cardNumber);
        assertEquals("**** **** **** ****", masked);
    }

    @Test
    void testMaskCardNumber_Null() {
        String masked = CardMaskingUtil.maskCardNumber(null);
        assertEquals("**** **** **** ****", masked);
    }
}


