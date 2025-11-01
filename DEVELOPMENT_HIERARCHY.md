# 🏗️ Иерархия Разработки Проекта Bank REST API

## 📋 Порядок разработки (снизу вверх)

### Этап 1: Базовая инфраструктура и конфигурация
**Цель**: Подготовить окружение для разработки

1. **pom.xml** - Настройка зависимостей
   - Spring Boot Starter (Web, Security, Data JPA, Validation)
   - PostgreSQL Driver
   - Liquibase (миграции БД)
   - JWT библиотеки (jjwt)
   - Swagger/OpenAPI
   - Lombok
   - Тестовые зависимости

2. **application.yml** - Конфигурация приложения
   - Подключение к БД
   - Настройки JPA/Hibernate
   - Настройки Liquibase
   - JWT секрет и время жизни токена
   - Swagger настройки

3. **docker-compose.yml** - Dev окружение
   - PostgreSQL контейнер
   - Настройки БД (имя, пользователь, пароль)

---

### Этап 2: Enum и базовая модель данных
**Цель**: Определить фундаментальные типы данных

4. **UserRole.java** (enum) - Роли системы
   ```java
   - ADMIN, USER
   - getAuthority() - для Spring Security
   - fromString() - парсинг строки в enum
   ```
   **Почему сначала enum?**
   - Роли используются во всех слоях
   - Это фундаментальный тип данных
   - Нет зависимостей от других классов

---

### Этап 3: Entity-классы (JPA сущности)
**Цель**: Определить модель данных для БД

5. **User.java** - Пользователь
   ```java
   - Поля: id, username, password, email, fullName
   - @ElementCollection Set<UserRole> roles
   - Реализует UserDetails (для Spring Security)
   - Связь @OneToMany с Card
   ```
   **Зависимости**: UserRole enum
   **Почему сначала User?**
   - Card зависит от User (владелец карты)
   - User нужен для аутентификации

6. **Card.java** - Банковская карта
   ```java
   - Поля: id, encryptedCardNumber, cardHolderName, 
          expirationDate, status, balance
   - @ManyToOne User owner
   - @PrePersist/@PreUpdate - авто-проверка истечения срока
   ```
   **Зависимости**: User
   **Enum CardStatus**: ACTIVE, BLOCKED, EXPIRED

---

### Этап 4: Repository слой (доступ к данным)
**Цель**: Определить интерфейсы для работы с БД

7. **UserRepository.java**
   ```java
   - findByUsername()
   - findByEmail()
   - existsByUsername(), existsByEmail()
   ```
   **Зависимости**: User entity

8. **CardRepository.java**
   ```java
   - findByOwner() - со страницами
   - findByOwnerAndSearch() - поиск по владельцу
   - findByIdAndOwner() - проверка принадлежности
   ```
   **Зависимости**: Card, User entities

---

### Этап 5: Утилиты (вспомогательные классы)
**Цель**: Переиспользуемые функции без бизнес-логики

9. **CardEncryptionUtil.java** - Шифрование
   ```java
   - encrypt() - AES шифрование номера карты
   - decrypt() - расшифровка
   - generateCardNumber() - для тестов
   ```
   **Зависимости**: Нет (чистые функции)

10. **CardMaskingUtil.java** - Маскирование
    ```java
    - maskCardNumber() - "**** **** **** 1234"
    - maskEncryptedCardNumber() - для зашифрованных данных
    ```
    **Зависимости**: CardEncryptionUtil

---

### Этап 6: Exception Handling
**Цель**: Унифицированная обработка ошибок

11. **Исключения**:
    - `ResourceNotFoundException.java` - 404
    - `BadRequestException.java` - 400
    - `UnauthorizedException.java` - 401
    - `ForbiddenException.java` - 403
    - `ErrorResponse.java` - DTO для ответа об ошибке

12. **GlobalExceptionHandler.java**
    ```java
    - @RestControllerAdvice
    - Обработка всех исключений
    - Валидация @Valid
    ```
    **Зависимости**: Exception классы

---

### Этап 7: Security слой (JWT и аутентификация)
**Цель**: Безопасность и аутентификация

13. **JwtTokenUtil.java**
    ```java
    - generateToken() - создание JWT
    - getUsernameFromToken() - извлечение username
    - validateToken() - проверка токена
    ```
    **Зависимости**: application.yml (jwt.secret)

14. **UserDetailsServiceImpl.java**
    ```java
    - Реализует UserDetailsService
    - loadUserByUsername() - загрузка для Spring Security
    ```
    **Зависимости**: UserRepository, User entity

15. **JwtAuthenticationFilter.java**
    ```java
    - Фильтр для проверки JWT токена
    - Извлечение токена из заголовка Authorization
    - Установка Authentication в SecurityContext
    ```
    **Зависимости**: JwtTokenUtil, UserDetailsService

16. **SecurityConfig.java**
    ```java
    - Настройка SecurityFilterChain
    - CORS конфигурация
    - Правила доступа (@PreAuthorize)
    ```
    **Зависимости**: UserDetailsService, JwtAuthenticationFilter

---

### Этап 8: DTO (Data Transfer Objects)
**Цель**: Объекты для передачи данных между слоями

17. **Request DTOs**:
    - `LoginRequest.java` - вход в систему
    - `CardCreateRequest.java` - создание карты
    - `UserCreateRequest.java` - создание пользователя
    - `TransferRequest.java` - перевод средств

18. **Response DTOs**:
    - `JwtResponse.java` - ответ с токеном
    - `CardDTO.java` - информация о карте (с маскированием)
    - `UserDTO.java` - информация о пользователе

**Особенность CardDTO**:
```java
- fromEntity() - статический метод
- Автоматическое маскирование номера карты
```

---

### Этап 9: Service слой (бизнес-логика)
**Цель**: Вся логика приложения

19. **UserService.java**
    ```java
    - createUser() - создание с валидацией
    - getAllUsers() - список всех
    - getUserById() - по ID
    - deleteUser() - удаление
    - Конвертация в DTO
    ```
    **Зависимости**: UserRepository, PasswordEncoder, UserRole

20. **CardService.java**
    ```java
    - createCard() - создание (шифрование номера)
    - getAllCards() - список с пагинацией
    - searchCards() - поиск
    - getCardById() - с проверкой прав
    - blockCard() - блокировка
    - activateCard() - активация (только ADMIN)
    - deleteCard() - удаление
    ```
    **Зависимости**: CardRepository, UserRepository, 
                   CardEncryptionUtil, SecurityContext

21. **TransferService.java**
    ```java
    - transferBetweenOwnCards() - перевод между своими картами
    - Проверки: баланс, статус, принадлежность
    - Транзакционность (@Transactional)
    ```
    **Зависимости**: CardRepository, UserRepository

---

### Этап 10: Controller слой (REST API)
**Цель**: HTTP endpoints

22. **AuthController.java**
    ```java
    - POST /api/auth/login - аутентификация
    - Возврат JWT токена
    ```
    **Зависимости**: AuthenticationManager, JwtTokenUtil

23. **CardController.java**
    ```java
    - POST /api/cards - создание
    - GET /api/cards - список (с пагинацией и поиском)
    - GET /api/cards/{id} - по ID
    - PUT /api/cards/{id}/block - блокировка
    - PUT /api/cards/{id}/activate - активация
    - DELETE /api/cards/{id} - удаление
    ```
    **Зависимости**: CardService, SecurityContext (проверка роли)

24. **UserController.java**
    ```java
    - POST /api/users - создание (только ADMIN)
    - GET /api/users - список
    - GET /api/users/{id} - по ID
    - DELETE /api/users/{id} - удаление
    ```
    **Зависимости**: UserService

25. **TransferController.java**
    ```java
    - POST /api/transfers - перевод между картами
    ```
    **Зависимости**: TransferService

---

### Этап 11: Конфигурационные классы
**Цель**: Настройка фреймворков

26. **SwaggerConfig.java**
    ```java
    - Настройка OpenAPI документации
    - Настройка JWT в Swagger UI
    ```
    **Зависимости**: Нет (только конфигурация)

27. **DataInitializer.java**
    ```java
    - CommandLineRunner - запуск при старте
    - Создание тестовых пользователей (admin, user)
    ```
    **Зависимости**: UserRepository, PasswordEncoder, UserRole

---

### Этап 12: Миграции БД (Liquibase)
**Цель**: Структура базы данных

28. **changelog-master.yml** - главный файл миграций
29. **001-create-tables.yml** - создание таблиц:
    - users
    - user_roles (для @ElementCollection)
    - cards
    - Индексы

**Почему в конце?**
- Структура БД зависит от Entity классов
- Нужно знать все связи между таблицами

---

### Этап 13: Main класс приложения
**Цель**: Точка входа

30. **BankRestApplication.java**
    ```java
    - @SpringBootApplication
    - main() метод
    ```
    **Зависимости**: Все остальные компоненты

---

### Этап 14: Тестирование
**Цель**: Проверка работоспособности

31. **Unit тесты**:
    - CardServiceTest.java
    - TransferServiceTest.java
    - CardMaskingUtilTest.java

---

### Этап 15: Документация
**Цель**: Описание проекта

32. **README.md** - инструкции запуска
33. **docs/openapi.yaml** - API спецификация

---

## 🔄 Зависимости между слоями

```
┌─────────────────────────────────────┐
│        Controller Layer             │  ← REST API
│  (AuthController, CardController)   │
└──────────────┬──────────────────────┘
               │ использует
               ▼
┌─────────────────────────────────────┐
│        Service Layer                │  ← Бизнес-логика
│  (UserService, CardService)         │
└──────────────┬──────────────────────┘
               │ использует
               ▼
┌─────────────────────────────────────┐
│     Repository Layer               │  ← Доступ к данным
│  (UserRepository, CardRepository)   │
└──────────────┬──────────────────────┘
               │ использует
               ▼
┌─────────────────────────────────────┐
│        Entity Layer                 │  ← Модель данных
│  (User, Card, UserRole enum)        │
└─────────────────────────────────────┘
```

---

## 💡 Ключевые архитектурные решения

### 1. Почему @ElementCollection вместо @ManyToMany?
- Роли - это простой enum, не требуется отдельная Entity
- Упрощает код, убирает лишний слой абстракции
- Таблица `user_roles` хранит строковые значения (ADMIN, USER)

### 2. Почему шифрование номеров карт?
- Требование безопасности (PCI DSS)
- Номера хранятся в зашифрованном виде в БД
- При отображении - маскирование (**** **** **** 1234)

### 3. Почему JWT вместо сессий?
- Stateless архитектура (масштабируемость)
- Микросервисная архитектура
- Не нужно хранить сессии на сервере

### 4. Почему @Transactional в Service?
- Гарантия атомарности операций (например, перевод)
- Откат при ошибке
- Изоляция транзакций

### 5. Почему DTO вместо Entity в Controller?
- Безопасность (не показываем внутреннюю структуру)
- Гибкость (маскирование, преобразование)
- Защита от lazy loading проблем

---

## 🎯 Что может спросить ментор?

### Вопросы по архитектуре:
1. **Почему использовали @ElementCollection?**
   - Роли - простой enum, не нужна отдельная Entity
   - Упрощает код и структуру БД

2. **Как работает шифрование номеров карт?**
   - AES шифрование при сохранении
   - Маскирование при отображении (только последние 4 цифры)

3. **Как обеспечивается безопасность?**
   - JWT токены для аутентификации
   - @PreAuthorize для проверки ролей
   - Шифрование чувствительных данных

4. **Как работает авторизация?**
   - Spring Security + JWT фильтр
   - Роли через GrantedAuthority
   - @PreAuthorize на методах контроллеров

### Вопросы по реализации:
1. **Почему переводы только между своими картами?**
   - Безопасность (пользователь не может перевести чужие средства)
   - Проверка через `findByIdAndOwner()`

2. **Как обрабатываются ошибки?**
   - Глобальный @RestControllerAdvice
   - Кастомные исключения с HTTP статусами
   - Валидация через @Valid

3. **Как работает пагинация?**
   - Spring Data JPA Pageable
   - Параметры page, size, sortBy в контроллере

4. **Как проверяется истечение срока карты?**
   - @PrePersist/@PreUpdate в Card entity
   - Автоматическая проверка при сохранении

---

## 📚 Ключевые технологии и почему:

- **Spring Boot** - быстрая разработка, автоконфигурация
- **Spring Security** - готовые решения безопасности
- **JWT** - stateless аутентификация
- **Liquibase** - версионирование схемы БД
- **PostgreSQL** - надежная БД для финансовых данных
- **Swagger/OpenAPI** - документация API
- **@ElementCollection** - упрощенное хранение enum коллекций

---

## ✅ Чеклист для ответов на вопросы:

- [ ] Понимаю архитектуру слоев (Controller → Service → Repository → Entity)
- [ ] Могу объяснить выбор технологий
- [ ] Понимаю безопасность (JWT, шифрование, роли)
- [ ] Могу объяснить работу с БД (миграции, связи)
- [ ] Знаю обработку ошибок
- [ ] Понимаю транзакции (@Transactional)
- [ ] Могу объяснить DTO паттерн
- [ ] Понимаю Spring Security и авторизацию


