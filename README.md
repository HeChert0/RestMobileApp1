# MobileApp

## Описание
MobileApp - это RESTful приложение, реализованное с использованием Spring Boot. Оно управляет каталогом телефонов, предоставляя API для получения списка телефонов, поиска по ID и фильтрации по параметрам.

## Стек технологий
- Java 17
- Spring Boot
- Spring Web
- Maven
- REST API

## Структура проекта
```
MobileApp
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── app
│   │   │   │   ├── controller      # Контроллеры REST API
│   │   │   │   │   ├── PhoneController.java
│   │   │   │   ├── dao             # Работа с данными
│   │   │   │   │   ├── PhoneDao.java
│   │   │   │   ├── entities        # Сущности
│   │   │   │   │   ├── Phone.java
│   │   │   │   ├── service         # Логика приложения
│   │   │   │   │   ├── PhoneService.java
│   │   │   │   │   ├── PhoneServiceLocal.java
│   │   │   │   ├── MobileApplication.java  # Главный класс Spring Boot
│   │   ├── resources
│   │   │   ├── application.properties  # Настройки Spring Boot
```

## Установка и запуск
### 1. Клонирование репозитория
```sh
git clone <URL-репозитория>
cd MobileApp
```
### 2. Сборка и запуск
```sh
mvn spring-boot:run
```
Приложение запустится на `http://localhost:8080`

## API
### Получение всех телефонов
```http
GET /phones
```
**Пример ответа:**
```json
[
    {"id": 1, "brand": "Apple", "model": "12 Pro", "price": 1000},
    {"id": 2, "brand": "Huawei", "model": "X32", "price": 666}
]
```
### Получение телефона по ID
```http
GET /phones/{id}
```
### Фильтрация по параметрам
```http
GET /phones?brand=Apple&model=12 Pro&price=1000
```

## Контакты
Автор: Nikita

