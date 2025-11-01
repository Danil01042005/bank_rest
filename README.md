# 🚀 Система Управления Банковскими Картами

Backend-приложение на Java (Spring Boot) для управления банковскими картами.

> 📖 **Для изучения архитектуры проекта**: см. [DEVELOPMENT_HIERARCHY.md](DEVELOPMENT_HIERARCHY.md)

## 📋 Описание

Система позволяет:
- Создание и управление банковскими картами
- Просмотр карт с поиском и пагинацией
- Переводы между своими картами
- Управление пользователями (для администраторов)

## 💳 Атрибуты карты

- **Номер карты** - зашифрован, отображается маской: `**** **** **** 1234`
- **Владелец**
- **Срок действия**
- **Статус**: Активна, Заблокирована, Истек срок
- **Баланс**

## 🛠 Технологии

- Java 17+
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker Compose
- Swagger/OpenAPI

## 📦 Требования

- Java 17 или выше
- Maven 3.6+
- Docker и Docker Compose

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://gitlab.com/PaatoM/bank_rest.git
cd bank_rest
```

### 2. Запуск PostgreSQL через Docker Compose

```bash
docker-compose up -d
```

Это запустит PostgreSQL на порту `5432`:
- База данных: `bankdb`
- Пользователь: `bankuser`
- Пароль: `bankpass`

### 3. Сборка и запуск приложения

```bash
mvn clean install
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

### 4. Проверка работы

- Swagger UI: http://localhost:8080/swagger-ui.html
- API документация: http://localhost:8080/api-docs

## 🔐 Аутентификация и роли

### Роли

- **ADMIN** - Полный доступ ко всем операциям
- **USER** - Доступ только к своим картам

### Тестовые пользователи

После первого запуска автоматически создаются:
- **Администратор**: `username=admin`, `password=admin123`
- **Пользователь**: `username=user`, `password=user123`

⚠️ **Важно**: В продакшене измените пароли по умолчанию!

### Получение JWT токена

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

Ответ:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ADMIN"]
}
```

### Использование токена

Добавьте заголовок в каждый запрос:
```
Authorization: Bearer <your_jwt_token>
```

## 📡 API Endpoints

### Аутентификация
- `POST /api/auth/login` - Вход в систему

### Карты (требуется авторизация)
- `GET /api/cards` - Получить список карт (с пагинацией и поиском)
- `POST /api/cards` - Создать новую карту
- `GET /api/cards/{id}` - Получить карту по ID
- `PUT /api/cards/{id}/block` - Заблокировать карту
- `PUT /api/cards/{id}/activate` - Активировать карту (только ADMIN)
- `DELETE /api/cards/{id}` - Удалить карту (только ADMIN)

### Переводы (требуется роль USER)
- `POST /api/transfers` - Перевод между своими картами

### Управление пользователями (требуется роль ADMIN)
- `GET /api/users` - Список всех пользователей
- `POST /api/users` - Создать пользователя
- `GET /api/users/{id}` - Получить пользователя по ID
- `DELETE /api/users/{id}` - Удалить пользователя

## 👤 Возможности по ролям

### Администратор (ADMIN)
- ✅ Создаёт, блокирует, активирует, удаляет карты
- ✅ Управляет пользователями
- ✅ Видит все карты в системе

### Пользователь (USER)
- ✅ Просматривает свои карты (с поиском и пагинацией)
- ✅ Запрашивает блокировку своей карты
- ✅ Делает переводы между своими картами
- ✅ Просматривает баланс своих карт

## 🔒 Безопасность

- Шифрование номеров карт (AES)
- Маскирование номеров при отображении
- JWT аутентификация
- Ролевой доступ (RBAC)
- Валидация входных данных

## 🗄 База данных

База данных настраивается автоматически через Liquibase миграции при первом запуске.

### Структура таблиц:
- `users` - Пользователи
- `roles` - Роли (ADMIN, USER)
- `user_roles` - Связь пользователей и ролей
- `cards` - Банковские карты

### Миграции

Миграции находятся в `src/main/resources/db/migration/`

## 🧪 Тестирование

```bash
mvn test
```

## 📝 Конфигурация

Основные настройки в `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankdb
    username: bankuser
    password: bankpass

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET:MySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong}
  expiration: 86400000  # 24 часа
```

## 🐳 Docker

### Остановка PostgreSQL

```bash
docker-compose down
```

### Остановка с удалением данных

```bash
docker-compose down -v
```

## 📚 Документация API

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI спецификация: `docs/openapi.yaml`

## 🔧 Разработка

### Структура проекта

```
src/main/java/com/example/bankcards/
├── config/          # Конфигурация (Security, Swagger)
├── controller/       # REST контроллеры
├── dto/              # Data Transfer Objects
├── entity/           # JPA сущности
├── exception/        # Обработка исключений
├── repository/       # Spring Data JPA репозитории
├── security/         # JWT, фильтры безопасности
├── service/          # Бизнес-логика
└── util/             # Утилиты (шифрование, маскирование)
```

## 📄 Лицензия

Apache 2.0

## 👥 Автор

Проект разработан для тестового задания.
