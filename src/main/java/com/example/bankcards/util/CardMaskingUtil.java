package com.example.bankcards.util;

/**
 * Утилита для маскирования номеров карт
 */
public class CardMaskingUtil {
    private static final String MASK_PATTERN = "**** **** **** ";

    /**
     * Маскирует номер карты в формате: **** **** **** 1234
     * Показывает только последние 4 цифры
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        
        // Убираем пробелы и получаем последние 4 цифры
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        if (cleanNumber.length() < 4) {
            return "**** **** **** ****";
        }
        
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return MASK_PATTERN + lastFour;
    }

    /**
     * Маскирует зашифрованный номер карты
     * Сначала расшифровывает, затем маскирует
     */
    public static String maskEncryptedCardNumber(String encryptedCardNumber) {
        if (encryptedCardNumber == null) {
            return "**** **** **** ****";
        }
        
        try {
            String decrypted = CardEncryptionUtil.decrypt(encryptedCardNumber);
            return maskCardNumber(decrypted);
        } catch (Exception e) {
            // Если не удалось расшифровать, возвращаем общую маску
            return "**** **** **** ****";
        }
    }
}


