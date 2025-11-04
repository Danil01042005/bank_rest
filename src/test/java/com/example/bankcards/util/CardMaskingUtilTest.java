package com.example.bankcards.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardMaskingUtilTest {
	@Test void mask_valid() { assertEquals("**** **** **** 3456", CardMaskingUtil.maskCardNumber("1234567890123456")); }
	@Test void mask_spaces() { assertEquals("**** **** **** 3456", CardMaskingUtil.maskCardNumber("1234 5678 9012 3456")); }
	@Test void mask_short() { assertEquals("**** **** **** ****", CardMaskingUtil.maskCardNumber("123")); }
}



