# MobileApp REST API

## Описание
Этот проект представляет собой REST API для управления каталогом мобильных телефонов. Реализовано с использованием Spring Boot.

## Функциональность
- Получение информации о телефоне по ID
- Фильтрация списка телефонов по бренду, модели и цене

## Структура проекта
```
MobileApp/
├── src/main/java/app/
│   ├── MobileApplication.java          # Главный класс приложения
│   ├── controller/
│   │   ├── PhoneController.java        # REST контроллер
│   ├── service/
│   │   ├── PhoneService.java           # Интерфейс сервиса
│   │   ├── PhoneServiceLocal.java      # Реализация сервиса
│   ├── dao/
│   │   ├── PhoneDAO.java               # DAO-класс с моковыми данными
│   ├── entities/
│   │   ├── Phone.java                  # Класс-сущность "Телефон"
```

## Установка и запуск

### 1. Клонирование репозитория
```sh
git clone https://github.com/HeChert0/MobileAppRest.git
cd MobileAppRest
```

### 2. Сборка и запуск

#### Использование Maven
```sh
mvn spring-boot:run
```

#### Использование Java
```sh
mvn package
java -jar target/MobileApp-0.0.1-SNAPSHOT.jar
```

## API

### Получить телефон по ID
**GET** `/phones/{id}`
```sh
curl -X GET http://localhost:8080/phones/1
```

### Фильтрация телефонов
**GET** `/phones`
```sh
curl -X GET "http://localhost:8080/phones?brand=Apple&price=1000"
```

## Контакты
Автор: HeChert0  
GitHub: [https://github.com/HeChert0](https://github.com/HeChert0)

