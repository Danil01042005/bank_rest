# Скрипт для сборки с тестами
# Запускает тесты локально, затем собирает Docker образ

Write-Host "Запуск тестов..." -ForegroundColor Green
.\mvnw.cmd clean test

if ($LASTEXITCODE -ne 0) {
    Write-Host "Тесты не прошли! Сборка Docker образа отменена." -ForegroundColor Red
    exit 1
}

Write-Host "Тесты прошли успешно!" -ForegroundColor Green
Write-Host "Сборка Docker образа..." -ForegroundColor Green
docker compose build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка при сборке Docker образа!" -ForegroundColor Red
    exit 1
}

Write-Host "Сборка завершена успешно!" -ForegroundColor Green

