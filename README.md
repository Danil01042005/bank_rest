# Bank REST API

Backend на Spring Boot для управления банковскими картами (Java 17, Spring Security + JWT, Spring Data JPA, Liquibase, OpenAPI).

## Запуск
1) База данных (PostgreSQL):
```bash
docker-compose up -d
```
2) Переменные окружения (dev пример):
- Windows PowerShell
```powershell
$env:JWT_SECRET="super-long-random-32bytes-secret"
$env:CARD_ENC_SECRET="super-long-32-bytes-key"
```
3) Приложение (без установленного Maven):
```powershell
$env:JWT_SECRET="super-long-random-32bytes-secret"
$env:CARD_ENC_SECRET="super-long-32-bytes-key"
.\mvnw.cmd spring-boot:run
```
Если Maven установлен:
```powershell
$env:JWT_SECRET="super-long-random-32bytes-secret"
$env:CARD_ENC_SECRET="super-long-32-bytes-key"
mvn spring-boot:run
```
4) Документация:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: docs/openapi.yaml

## Конфигурация (application.yml)
- spring.datasource — параметры подключения к PostgreSQL
- spring.jpa.hibernate.ddl-auto: validate — схему ведёт Liquibase
- jwt.secret / accessExpiration / refreshExpiration — настройки JWT
- Hikari (datasource.hikari) — ожидание готовности БД на старте

## Роли и аутентификация
- Предустановленные пользователи:
  - ADMIN: admin / admin123
  - USER: user / user123
- Вход:
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```
Ответ: `{ token, refreshToken, type, username, roles }`
- Обновление access токена:
```http
POST /api/auth/refresh
Content-Type: application/json

{ "refreshToken": "<refresh>" }
```
- Авторизация: `Authorization: Bearer <token>`

## Эндпойнты и примеры

### Карты (пользователь)
- Создать карту (номер шифруется, отображается маской):
```http
POST /api/cards
Content-Type: application/json
Authorization: Bearer <token>

{
  "cardNumber": "1234567890123456",
  "cardHolderName": "Test User",
  "expirationDate": "2027-12-31"
}
```
- Список своих карт с пагинацией и поиском:
```http
GET /api/cards?page=0&size=10&sortBy=id&search=visa
Authorization: Bearer <token>
```
- Получить свою карту по id:
```http
GET /api/cards/1
Authorization: Bearer <token>
```
- Запросить блокировку своей карты:
```http
PUT /api/cards/1/block
Authorization: Bearer <token>
```

### Карты (администратор)
- Активировать карту:
```http
PUT /api/cards/1/activate
Authorization: Bearer <admin-token>
```
- Удалить карту:
```http
DELETE /api/cards/1
Authorization: Bearer <admin-token>
```
- Просмотреть все карты (пагинация + поиск):
```http
GET /api/admin/cards?page=0&size=20&sortBy=id&search=user
Authorization: Bearer <admin-token>
```

### Переводы (пользователь)
- Перевод между своими картами:
```http
POST /api/transfers
Content-Type: application/json
Authorization: Bearer <token>

{ "fromCardId": 1, "toCardId": 2, "amount": 100.00 }
```

### Пользователи (администратор)
```http
POST   /api/users              # создать пользователя
GET    /api/users              # список
GET    /api/users/{id}         # получить по id
DELETE /api/users/{id}         # удалить
```

## Поиск и пагинация
- Параметры: `page` (по умолчанию 0), `size` (10), `sortBy` (id)
- Параметр `search` (без учёта регистра):
  - для пользователя: по `cardHolderName` и `id` (в пределах своих карт)
  - для администратора: по `cardHolderName`, `owner.username`, `id`

## Безопасность
- JWT access/refresh, ролевой доступ через `@PreAuthorize`
- Номера карт хранятся зашифрованными (AES), в ответах — маскирование `**** **** **** 1234`
- Никогда не коммитьте секреты в репозиторий; задавайте `JWT_SECRET` через переменные окружения/секрет‑хранилище

## База данных и миграции
- Liquibase применяет миграции при старте (`src/main/resources/db/migration`)
- Таблицы: `users`, `user_roles` (через @ElementCollection), `cards`
- Индексы: на ключевые поля и связи (owner/status)

## Примечания
- В dev логирование SQL включено через логгер; форматирование — `hibernate.format_sql=true`
- Hikari настроен ждать БД на старте, чтобы избежать гонки поднятия контейнеров
- Все новые эндпойнты отражены в `docs/openapi.yaml`; Swagger UI доступен по /swagger-ui.html
