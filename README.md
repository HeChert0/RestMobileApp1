```markdown
# MobileApp

## Описание
Это REST API для управления смартфонами, пользователями и заказами. Проект реализован на Spring Boot, использует базу данных MySQL через Spring Data JPA (Hibernate) и реализует in-memory кэширование. Также добавлены:
- Логирование действий и ошибок с использованием аспектов, с записью логов в файл.
- Полезный GET-запрос с параметрами, реализованный через кастомные запросы (@Query) в JPQL и native SQL для фильтрации данных по вложенной сущности.
- Документация API с помощью Swagger (OpenAPI 2.8.6).

---

## Выполненные задания
1. **Подключение БД (MySQL) и использование Hibernate/Spring Data JPA**  
   - Сущности: `Smartphone`, `Order`, `User`.  
   - Связи:  
     - `User` ↔ `Order` (один ко многим / многие к одному)  
     - `Order` ↔ `Smartphone` (многие ко многим через join‑таблицу `order_smartphone`)  
   - Настроены каскадные операции и ленивые/жадные загрузки.

2. **In-memory кэширование**  
   - Реализован отдельный бин для хранения данных (с ограниченным размером и политикой удаления наименее часто используемых элементов).  
   - При CRUD‑операциях кэш обновляется.

3. **Логирование действий и ошибок**  
   - Используются аспекты (AOP) для логирования входа/выхода из методов контроллеров, сервисов и репозиториев.  
   - Логи записываются в файл (например, `app.log`), путь и уровень задаются в `application.properties`.

4. **Запрос для получения лог-файла**  
   - Добавлен endpoint `/logs` (и `/logs/download`) для фильтрации и возврата логов за указанную дату.  
   - Можно получить содержимое лог-файла как текст или скачать файл.

5. **Кастомный GET запрос с фильтрацией по вложенной сущности**  
   - В `OrderRepository` реализованы два метода с @Query – один на JPQL и один с native SQL – для фильтрации заказов по `User.username`.

6. **Swagger (OpenAPI)**
   - Подключен модуль `springdoc-openapi-starter-webmvc-api` и `springdoc-openapi-starter-webmvc-ui` версии **2.8.6**.
   - Документация доступна по адресу:  
     `http://localhost:8081/swagger-ui/index.html`
   - Swagger позволяет просматривать и тестировать все endpoint’ы API, видеть их параметры и схемы ответов.

---

## Стек технологий
- Java 17
- Spring Boot (Web, Data JPA, Security*)
- Hibernate
- MySQL
- Maven
- MapStruct (для маппинга DTO)
- Spring AOP (для логирования)
- In-memory кэш
- OpenAPI/Swagger (springdoc-openapi 2.8.6)

\* Spring Security включён, но настроен для открытого доступа (см. `SecurityConfig`).

---

## Структура проекта

```
MobileApp
├── src
│   ├── main
│   │   ├── java
│   │   │   └── app
│   │   │       ├── MobileApplication.java          # Главный класс Spring Boot
│   │   │       ├── aspect                          # Аспекты логирования (LoggingAspect.java)
│   │   │       ├── cache                           # In-memory кэш (InMemoryCache.java)
│   │   │       ├── config                          # Конфигурация (SecurityConfig, SwaggerConfig)
│   │   │       ├── controller                      # REST-контроллеры (SmartphoneController.java, OrderController.java, UserController.java, LogController.java)
│   │   │       ├── dao                             # Репозитории (JpaRepository интерфейсы)
│   │   │       ├── dto                             # DTO-классы (UserDto, SmartphoneDto, OrderDto)
│   │   │       ├── entities (или models)           # JPA-сущности (Smartphone.java, Order.java, User.java)
│   │   │       ├── mapper                          # MapStruct мапперы (BaseMapper.java, SmartphoneMapper.java, OrderMapper.java, UserMapper.java)
│   │   │       └── service                         # Сервисный слой (SmartphoneService.java, OrderService.java, UserService.java)
│   │   └── resources
│   │       ├── application.properties              # Настройки Spring Boot, БД, логирования, swagger и т.д.
├── pom.xml
└── README.md
```

---

## Установка и запуск

### 1. Клонирование репозитория
```sh
git clone https://github.com/HeChert0/RestMobileApp1
cd MobileApp
```

### 2. Настройка базы данных
В файле `application.properties` укажите параметры подключения к MySQL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartphones_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
Создайте базу данных `smartphones_db` (либо настройте автоматическое создание).

### 3. Сборка и запуск
```sh
mvn spring-boot:run
```
Приложение запустится на `http://localhost:8081` (или другой порт, если указан).

### 4. Проверка Swagger
Откройте в браузере:  
```
http://localhost:8081/swagger-ui/index.html
```
Здесь вы увидите интерактивную документацию API с описаниями всех endpoint’ов.

### 5. Проверка логирования и кэша
- Логи записываются в файл `app.log` (путь настраивается в `application.properties`).
- Для получения логов по дате используйте endpoint:  
  ```
  GET /logs?date=2025-03-19
  ```
  или для скачивания:  
  ```
  GET /logs/download?date=2025-03-19
  ```

---

## Примеры запросов для тестирования

### **Пользователи**
- Создание:  
  `POST /users`  
  ```json
  {
    "username": "JohnDoe",
    "password": "secret123",
    "orderIds": []
  }
  ```
- Получение:  
  `GET /users`

### **Смартфоны**
- Создание:  
  `POST /phones`  
  ```json
  {
    "brand": "Samsung",
    "model": "Galaxy S21",
    "price": 799.99
  }
  ```
- Фильтрация:  
  `GET /phones/filter?brand=Apple&model=iPhone 13`

### **Заказы**
- Создание:  
  `POST /orders`  
  ```json
  {
    "userId": 1,
    "smartphoneIds": [3, 6, 6]
  }
  ```
  (totalAmount вычисляется автоматически как сумма цен, учитывая повторы)
- Обновление:  
  `PUT /orders/{id}`  
  Если передан пустой список smartphoneIds, заказ удаляется и возвращается статус 204.
- Фильтрация заказов по username (JPQL/native):  
  `GET /orders/filter?username=Alice`  
  или  
  `GET /orders/filter?username=Alice&nativeQuery=true`

### **Логи**
- Получение логов за определенную дату:  
  `GET /logs?date=2025-03-19`

---

## Swagger и логирование: краткое пояснение

**Swagger (OpenAPI)** – это инструмент для документирования REST API. Он автоматически генерирует интерактивную документацию, позволяя разработчикам и клиентам:
- Просматривать все endpoint’ы, их параметры и схемы ответов.
- Тестировать запросы прямо из браузера.
- Облегчать интеграцию и поддержку API.

**Логирование через аспекты** – позволяет централизованно регистрировать входы, выходы и ошибки во всех методах приложения. Логи записываются в файл, что помогает анализировать работу системы и отлаживать ошибки.

---
