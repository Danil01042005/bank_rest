# ⚡ Быстрая Шпаргалка по Проекту

## 📊 Порядок разработки (15 этапов)

```
1. pom.xml + application.yml + docker-compose.yml  (Инфраструктура)
2. UserRole.java (enum)                              (Базовый тип)
3. User.java, Card.java                              (Entity)
4. UserRepository, CardRepository                    (Доступ к данным)
5. CardEncryptionUtil, CardMaskingUtil               (Утилиты)
6. Exception классы + GlobalExceptionHandler         (Ошибки)
7. JwtTokenUtil, UserDetailsService, 
   JwtAuthenticationFilter, SecurityConfig           (Security)
8. DTO классы (LoginRequest, CardDTO, etc.)          (Передача данных)
9. UserService, CardService, TransferService         (Бизнес-логика)
10. AuthController, CardController, 
    UserController, TransferController               (REST API)
11. SwaggerConfig, DataInitializer                   (Конфигурация)
12. Liquibase миграции                               (БД структура)
13. BankRestApplication.java                         (Main класс)
14. Unit тесты                                        (Тестирование)
15. README.md, OpenAPI                                (Документация)
```

## 🎯 Ключевые ответы на вопросы

### Q: Почему @ElementCollection вместо @ManyToMany?
**A**: Роли - это enum (ADMIN, USER), не нужна отдельная Entity. Упрощает код и БД.

### Q: Как работает шифрование карт?
**A**: 
1. При создании: номер → AES шифрование → сохраняется `encryptedCardNumber`
2. При отображении: расшифровка → маскирование → `**** **** **** 1234`

### Q: Как работает JWT авторизация?
**A**:
1. POST /api/auth/login → получаем JWT токен
2. Заголовок: `Authorization: Bearer <token>`
3. JwtAuthenticationFilter проверяет токен
4. Spring Security устанавливает Authentication
5. @PreAuthorize проверяет роли

### Q: Почему переводы только между своими картами?
**A**: Безопасность. Проверка через `findByIdAndOwner()` - карта должна принадлежать текущему пользователю.

### Q: Как обрабатываются ошибки?
**A**: 
- @RestControllerAdvice (GlobalExceptionHandler)
- Кастомные исключения → HTTP статусы
- Валидация через @Valid → 400 Bad Request

### Q: Почему @Transactional в Service?
**A**: Гарантия атомарности (все или ничего). Например, перевод: списание с одной карты и зачисление на другую должно быть атомарно.

### Q: Почему DTO вместо Entity в Controller?
**A**:
1. Безопасность (скрываем внутреннюю структуру)
2. Маскирование данных (номера карт)
3. Защита от LazyInitializationException

## 🏗️ Архитектура (слои сверху вниз)

```
Controller  →  Service  →  Repository  →  Entity
   ↓             ↓            ↓            ↓
REST API    Бизнес-логика  Доступ к БД  Модель данных
```

## 🔐 Security Flow

```
1. Login → AuthenticationManager.authenticate()
2. JwtTokenUtil.generateToken() → JWT
3. Клиент сохраняет токен
4. Запросы: Authorization: Bearer <token>
5. JwtAuthenticationFilter → проверка токена
6. UserDetailsService → загрузка User
7. SecurityContext → установка Authentication
8. @PreAuthorize → проверка ролей
```

## 💾 БД Структура

```
users
  ├─ id, username, password, email, full_name
  └─ cards (OneToMany)

user_roles (@ElementCollection)
  ├─ user_id (FK → users.id)
  └─ role (VARCHAR: ADMIN, USER)

cards
  ├─ id, encrypted_card_number, card_holder_name
  ├─ expiration_date, status, balance
  └─ owner_id (FK → users.id)
```

## 📝 Важные паттерны

1. **DTO Pattern** - CardDTO.fromEntity() - преобразование Entity → DTO
2. **Repository Pattern** - абстракция доступа к данным
3. **Service Layer** - вся бизнес-логика в одном месте
4. **Global Exception Handling** - централизованная обработка ошибок
5. **JWT Filter** - проверка токена на каждом запросе

## 🛠 Технологии и причины

| Технология | Зачем |
|------------|-------|
| Spring Boot | Быстрая разработка, автоконфигурация |
| Spring Security | Готовые решения безопасности |
| JWT | Stateless аутентификация |
| Liquibase | Версионирование схемы БД |
| PostgreSQL | Надежная БД для финансов |
| Swagger | Автодокументация API |
| @ElementCollection | Упрощенное хранение enum |

## ✅ Чеклист для защиты проекта

- [ ] Могу объяснить порядок разработки компонентов
- [ ] Понимаю зависимости между слоями
- [ ] Знаю, почему выбраны технологии
- [ ] Могу объяснить работу Security (JWT)
- [ ] Понимаю шифрование и маскирование
- [ ] Знаю, как работает авторизация
- [ ] Могу объяснить транзакции
- [ ] Понимаю обработку ошибок
- [ ] Знаю структуру БД
- [ ] Могу объяснить архитектурные решения


