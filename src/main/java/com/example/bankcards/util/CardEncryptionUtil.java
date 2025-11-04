package com.example.bankcards.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CardEncryptionUtil {
	private static final Logger log = LoggerFactory.getLogger(CardEncryptionUtil.class);
	private static final String ALGORITHM = "AES";
	private static final String DEFAULT_KEY = "MySecretKeyForEncryptionOfCardNumbers256";

	private static String getSecret() {
		String fromEnv = System.getenv("CARD_ENC_SECRET");
		if (fromEnv != null && !fromEnv.isBlank()) {
			log.debug("Используется секрет из переменной окружения CARD_ENC_SECRET");
			return fromEnv;
		}
		log.warn("Используется дефолтный секрет (не рекомендуется для production)");
		return DEFAULT_KEY;
	}

	private static SecretKeySpec key() {
		return new SecretKeySpec(getSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
	}

	public static String encrypt(String cardNumber) {
		if (cardNumber == null || cardNumber.isBlank()) {
			log.error("Попытка зашифровать пустой номер карты");
			throw new IllegalArgumentException("Номер карты не может быть пустым");
		}
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key());
			String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8)));
			log.debug("Номер карты успешно зашифрован (длина: {})", encrypted.length());
			return encrypted;
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception e) {
			log.error("Ошибка шифрования номера карты", e);
			throw new RuntimeException("Ошибка шифрования номера карты", e);
		}
	}

	public static String decrypt(String encrypted) {
		if (encrypted == null || encrypted.isBlank()) {
			log.error("Попытка расшифровать пустую строку");
			throw new IllegalArgumentException("Зашифрованная строка не может быть пустой");
		}
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key());
			byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
			String cardNumber = new String(decrypted, StandardCharsets.UTF_8);
			log.debug("Номер карты успешно расшифрован");
			return cardNumber;
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception e) {
			log.error("Ошибка расшифровки номера карты", e);
			throw new RuntimeException("Ошибка расшифровки номера карты", e);
		}
	}

	public static String generateCardNumber() {
		log.debug("Генерация случайного номера карты");
		SecureRandom r = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			sb.append(r.nextInt(10));
		}
		String cardNumber = sb.toString();
		log.debug("Сгенерирован номер карты: {}****", cardNumber.substring(0, 4));
		return cardNumber;
	}
}

