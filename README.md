# MobileApp REST API

## Описание
Этот проект представляет собой REST API для управления каталогом мобильных телефонов. Реализовано с использованием Spring Boot.

## Функциональность
- Получение информации о телефоне по ID
- Фильтрация списка телефонов по бренду, модели и цене

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

