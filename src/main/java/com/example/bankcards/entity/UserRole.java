package com.example.bankcards.entity;

/**
 * Enum для ролей пользователей в системе
 */
public enum UserRole {
    ADMIN,
    USER;

    /**
     * Получить название роли для Spring Security (с префиксом ROLE_)
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Получить роль из строки (без префикса ROLE_)
     */
    public static UserRole fromString(String roleName) {
        if (roleName == null) {
            return null;
        }
        // Убираем префикс ROLE_ если есть
        String cleanName = roleName.startsWith("ROLE_") 
            ? roleName.substring(5) 
            : roleName;
        try {
            return UserRole.valueOf(cleanName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

