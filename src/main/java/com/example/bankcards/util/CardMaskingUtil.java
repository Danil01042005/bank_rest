package com.example.bankcards.util;

public class CardMaskingUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CardMaskingUtil.class);
	public static String maskCardNumber(String cardNumber) {
		if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() < 4) {
			return "**** **** **** ****";
		}
		String clean = cardNumber.replaceAll("\\s", "");
		String last4 = clean.substring(clean.length() - 4);
		return "**** **** **** " + last4;
	}

	public static String maskEncryptedCardNumber(String encrypted) {
		try {
			return maskCardNumber(CardEncryptionUtil.decrypt(encrypted));
		} catch (Exception e) {
			log.warn("Card masking: decrypt failed, returning masked placeholder");
			return "**** **** **** ****";
		}
	}
}

