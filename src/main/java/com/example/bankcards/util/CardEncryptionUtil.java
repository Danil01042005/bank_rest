package com.example.bankcards.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Утилита для шифрования номеров карт
 */
public class CardEncryptionUtil {
    private static final String ALGORITHM = "AES";
    
    // В продакшене ключ должен храниться в безопасном хранилище (Vault, KMS и т.д.)
    // Здесь используется фиксированный ключ для примера
    private static final String SECRET_KEY = "MySecretKeyForEncryptionOfCardNumbers256"; // 32 символа для AES-256

    /**
     * Шифрует номер карты
     */
    public static String encrypt(String cardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании номера карты", e);
        }
    }

    /**
     * Расшифровывает номер карты
     */
    public static String decrypt(String encryptedCardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при расшифровке номера карты", e);
        }
    }

    /**
     * Генерирует случайный номер карты (для тестирования)
     */
    public static String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }
}

