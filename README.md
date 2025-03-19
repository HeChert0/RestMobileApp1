# MobileApp

## Описание
Это REST API для управления смартфонами, пользователями и заказами. Проект реализован на Spring Boot и использует базу данных MySQL через Spring Data JPA. Также добавлен in-memory кеш для ускорения повторных запросов.

### Выполненные задания
1. **Подключение БД (MySQL) и использование Hibernate/Spring Data JPA**  
   - Сущности: `Smartphone`, `Order`, `User`.  
   - Связи:  
     - `User` ↔ `order` (один ко многим / многие к одному)  
     - `Order` ↔ `Smartphone` (многие ко многим)  
   - Настроены каскадные операции и ленивые/жадные загрузки.
---

## Функциональность
- **CRUD для смартфонов**: создание, получение, удаление (`/phones`).
- **CRUD для заказов**: создание, получение, удаление (`/orders`).
- **CRUD для пользователей**: создание, получение, обновление, удаление (`/users`).
- **Фильтрация смартфонов** по `customerName` заказа (через кастомный запрос `@Query`).
---

## Стек технологий
- Java 17
- Spring Boot (Web, Data JPA, Security\*)
- Hibernate
- MySQL
- Maven
- MapStruct (для маппинга DTO)
- REST API

\* Spring Security по умолчанию отключён для удобства тестирования (см. `SecurityConfig`).

---

## Структура проекта

```
MobileApp
├── src
│   ├── main
│   │   ├── java
│   │   │   └── app
│   │   │       ├── MobileApplication.java          # Главный класс Spring Boot
│   │   │       ├── config                          # Настройки Spring (SecurityConfig и т.д.)
│   │   │       ├── controller                      # REST-контроллеры
│   │   │       │   ├── SmartphoneController.java
│   │   │       │   ├── OrderController.java
│   │   │       │   ├── UserController.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── dao (repositories)              # Интерфейсы JpaRepository
│   │   │       │   ├── SmartphoneRepository.java
│   │   │       │   ├── OrderRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       ├── dto                             # DTO-классы (UserDTO, SmartphoneDTO, OrderDTO)
│   │   │       ├── entities (или models)           # JPA-сущности
│   │   │       │   ├── Smartphone.java
│   │   │       │   ├── Order.java
│   │   │       │   └── User.java
│   │   │       ├── mapper                          # MapStruct мапперы
│   │   │       │   ├── BaseMapper.java
│   │   │       │   ├── SmartphoneMapper.java
│   │   │       │   └── OrderMapper.java
│   │   │       ├── service                         # Сервисный слой
│   │   │       │   ├── SmartphoneService.java
│   │   │       │   ├── OrderService.java
│   │   │       │   └── UserService.java
│   │   │       └── cache                           # In-memory кеш
│   │   │           └── SmartphoneCache.java
│   │   ├── resources
│   │   │   ├── application.properties              # Настройки Spring Boot и БД
├── pom.xml
└── README.md
```

---

## Установка и запуск

### 1. Клонирование репозитория
```sh
git clone <(https://github.com/HeChert0/RestMobileApp1)>
cd MobileApp
```

### 2. Настройка базы данных
В `application.properties` пропиши параметры подключения к MySQL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartphones_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
Создай базу данных `smartphones_db` вручную, либо настроь автоматическое создание при запуске.

### 3. Сборка и запуск
```sh
mvn spring-boot:run
```
Приложение запустится на `http://localhost:8081` (или `8080`, если не менял `server.port`).

---

## API (основные точки)

### **1. Smartphones**

- **Получение всех смартфонов**  
  `GET /phones`
  
- **Получение смартфона по ID**  
  `GET /phones/{id}`

- **Создание смартфона**  
  `POST /phones`  
  ```json
  {
    "brand": "Samsung",
    "model": "Galaxy S21",
    "price": 799.99,
    "orderId": 1
  }
  ```
  
- **Удаление смартфона**  
  `DELETE /phones/{id}`

- **Фильтрация по имени заказчика** (кастомный запрос + кеш)
  `GET /phones/by-customer?customerName=Alice[&nativeQuery=true]`  
  - `customerName` – имя заказчика  
  - `nativeQuery` – (опционально) `true` для использования нативного SQL  
  - Результаты кешируются в `SmartphoneCache`. При повторном запросе с тем же `customerName` данные возвращаются из кеша.

### **2. Orders**

- **Получение всех заказов**  
  `GET /orders`

- **Получение заказа по ID**  
  `GET /orders/{id}`

- **Создание заказа**  
  `POST /orders`  
  ```json
  {
    "customerName": "Alice",
    "smartphoneIds": []
  }
  ```

- **Удаление заказа**  
  `DELETE /orders/{id}`

### **3. Users**

- **Получение всех пользователей**  
  `GET /users`

- **Получение пользователя по ID**  
  `GET /users/{id}`

- **Создание пользователя**  
  `POST /users`  
  ```json
  {
    "username": "JohnDoe",
    "password": "secret123",
    "smartphoneIds": []
  }
  ```

- **Обновление пользователя**  
  `PUT /users/{id}`  
  ```json
  {
    "username": "JohnUpdated",
    "smartphoneIds": [2, 3]
  }
  ```
  Пароль не обновляется, если не передан (MapStruct маппер игнорирует поле password при merge).

- **Удаление пользователя**  
  `DELETE /users/{id}`

---

## Примеры запросов

```sh
# Получить смартфоны для заказчика 'Alice' через JPQL
curl -X GET "http://localhost:8081/phones/by-customer?customerName=Alice"

# Получить смартфоны для заказчика 'Bob' через native query
curl -X GET "http://localhost:8081/phones/by-customer?customerName=Bob&nativeQuery=true"
```

При повторном запросе с тем же `customerName` результат возвращается из кеша.

---

## Дополнительно
- **Валидация**: При создании/обновлении сущностей, если поля не соответствуют ограничениям (`@NotBlank`, `@Size` и т.д.), срабатывает `GlobalExceptionHandler`, возвращая 400 Bad Request с описанием ошибок.
- **Spring Security**: Проект содержит базовую конфигурацию (см. `SecurityConfig`). По умолчанию доступ открыт для всех (пара строк закомментированы). Если требуется защита, раскомментируй и укажи правила доступа.
- **MapStruct**: Для маппинга между сущностями и DTO (UserDTO, SmartphoneDTO, OrderDTO) используются интерфейсы, расширяющие `BaseMapper`. При сборке (Maven) автоматически генерируются реализации мапперов.

---

Это README охватывает:  
- Подключение к БД  
- Связи сущностей  
- Использование JPA (кастомные запросы)  
- Примеры запросов для тестирования  

Надеюсь, оно поможет быстро разобраться в проекте и его возможностях. Удачи!
