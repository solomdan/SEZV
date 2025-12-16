
# Lost & Found – спецификация модели и план реализации (TJV)

Проект: **сервис для работы с потерянными вещами** (Lost & Found).  
Технологии сервера: **Java + Spring Boot + Spring Data JPA + relační DB**.

---

## 1. Доменные сущности

Проект использует **3 доменных типа** (entities) с полной поддержкой CRUD и одной связью **many-to-many**.

### 1.1. Entity `LostItem`

Найденная вещь.

**Таблица:** `lost_item`

**Поля:**

| Поле             | Тип                    | Описание                                                  |
|------------------|------------------------|-----------------------------------------------------------|
| `id`             | `Long` (PK)            | Идентификатор вещи                                       |
| `title`          | `String`               | Название вещи (например, "iPhone 13", "Рюкзак")          |
| `description`    | `String` / `Text`      | Краткое описание                                          |
| `foundTransport` | `String` / `Enum`      | Тип транспорта (`BUS`, `TRAM`, `METRO`, ...)             |
| `routeNumber`    | `String`               | Номер маршрута                                           |
| `vehicleNumber`  | `String`               | Номер транспортного средства                             |
| `foundAt`        | `LocalDateTime`        | Дата и время находки                                     |
| `storageLocation`| `String`               | Место хранения (адрес/описание склада)                   |
| `status`         | `LostItemStatus` (enum)| `FOUND`, `RESERVED`, `RETURNED`, `DISPOSED`              |
| `createdAt`      | `LocalDateTime`        | Дата создания записи                                     |
| `updatedAt`      | `LocalDateTime`        | Дата последнего обновления                               |

**Связи:**

- `categories: Set<Category>` – связь **M:N** с `Category`
- `returnRequests: List<ReturnRequest>` – связь **1:N** с `ReturnRequest`

---

### 1.2. Entity `Category`

Категория вещи (телефон, документы, одежда и т.д.).

**Таблица:** `category`

**Поля:**

| Поле        | Тип             | Описание                  |
|-------------|-----------------|---------------------------|
| `id`        | `Long` (PK)     | Идентификатор категории   |
| `name`      | `String`        | Название категории        |
| `createdAt` | `LocalDateTime` | Дата создания             |

**Связи:**

- `items: Set<LostItem>` – обратная сторона **M:N** (через join table)

---

### 1.3. Entity `ReturnRequest`

Заявка на возврат вещи.

**Таблица:** `return_request`

**Поля:**

| Поле             | Тип                      | Описание                                                   |
|------------------|--------------------------|------------------------------------------------------------|
| `id`             | `Long` (PK)              | Идентификатор заявки                                      |
| `item`           | `LostItem` (FK)          | Вещь, к которой относится заявка                          |
| `requesterName`  | `String`                 | Имя человека, подавшего заявку                            |
| `requesterEmail` | `String`                 | Email                                                      |
| `requesterPhone` | `String`                 | Телефон                                                    |
| `status`         | `ReturnRequestStatus`    | `PENDING`, `APPROVED`, `REJECTED`, `COMPLETED`, `CANCELLED` |
| `comment`        | `String` / `Text`        | Описание/доказательства, почему это его вещь              |
| `fulfillmentType`| `FulfillmentType`        | `PICKUP`, `DELIVERY_DOMESTIC`, `DELIVERY_INTERNATIONAL`   |
| `shippingAddress`| `String` (nullable)      | Адрес доставки (если выбран delivery)                     |
| `createdAt`      | `LocalDateTime`          | Дата создания заявки                                      |
| `processedAt`    | `LocalDateTime` (nullable)| Дата обработки (approve/reject/complete)                 |

**Связи:**

- `item: LostItem` – связь **M:1** с `LostItem`.

---

## 2. Связи между сущностями

- `LostItem` **1 – N** `ReturnRequest`  
  (одна вещь → несколько заявок, но по бизнес-логике только одна может быть APPROVED/COMPLETED)

- `LostItem` **M – N** `Category` через join таблицу `lost_item_category`:

**Join-таблица:** `lost_item_category`

| Поле           | Тип      | Описание                               |
|----------------|----------|----------------------------------------|
| `lost_item_id` | `Long`   | FK на `lost_item.id`                   |
| `category_id`  | `Long`   | FK на `category.id`                    |

Тем самым выполняется требование: **min. 1 vazba many-to-many, 4 таблицы**.

---

## 3. Требуемый extra JPQL запрос

Пример "dotaz navíc" (над рамцем CRUD):

1. **Найти все доступные вещи по названию категории и интервалу дат находки:**

```text
Input:
- categoryName: String
- from: LocalDateTime
- to: LocalDateTime

Output:
- List<LostItem>
  такие, что:
    - связаны с Category.name = :categoryName
    - foundAt BETWEEN :from AND :to
    - status = FOUND


JPQL-псевдокод:

```jpql
SELECT i
FROM LostItem i
JOIN i.categories c
WHERE c.name = :categoryName
  AND i.foundAt BETWEEN :from AND :to
  AND i.status = cz.cvut.tjv.lostfound.domain.LostItemStatus.FOUND
```

Можно реализовать в `LostItemRepository` как метод с `@Query`.

---

## 4. REST API (черновой набросок)

### 4.1. LostItem REST (CRUD + поиск)

* `GET /api/items`

  * query params: `title`, `categoryId`, `fromFoundAt`, `toFoundAt` (опциональные)
  * возвращает список `LostItemDto`

* `GET /api/items/{id}`

  * детальная информация по вещи

* `POST /api/items`

  * body: `LostItemCreateDto`
  * создаёт вещь, возвращает `LostItemDto`
  * HTTP 201

* `PUT /api/items/{id}`

  * обновляет данные вещи
  * HTTP 200 / 404

* `DELETE /api/items/{id}`

  * удаляет вещь
  * HTTP 204 / 404

* `POST /api/items/{id}/categories/{categoryId}`

  * привязать категорию к вещи (манипуляция M:N)
  * HTTP 204

* `DELETE /api/items/{id}/categories/{categoryId}`

  * отвязать категорию
  * HTTP 204

---

### 4.2. Category REST (CRUD)

* `GET /api/categories`
* `GET /api/categories/{id}`
* `POST /api/categories`
* `PUT /api/categories/{id}`
* `DELETE /api/categories/{id}`

---

### 4.3. ReturnRequest REST (CRUD + бизнес-операции)

* `GET /api/requests`

  * фильтры: `status`, `itemId`, `email` и т.п.

* `GET /api/requests/{id}`

* `POST /api/requests`

  * создаёт новую заявку (status = `PENDING`)
  * body: `ReturnRequestCreateDto` (содержит `itemId`, данные пользователя, fulfillmentType, адрес при delivery)

* `PUT /api/requests/{id}`

  * общие обновления (например, изменение контактных данных до обработки)

* `DELETE /api/requests/{id}`

  * логическое "отмена" (по факту можно делать status = `CANCELLED`)

**Доп. действия (бизнес-логика):**

* `POST /api/requests/{id}/approve`

  * меняет status → `APPROVED`
  * одновременно меняет `LostItem.status` → `RESERVED`
  * ставит `processedAt = now()`

* `POST /api/requests/{id}/reject`

  * status → `REJECTED`
  * `processedAt = now()`

* `POST /api/requests/{id}/complete`

  * когда вещь выдана/доставлена:

    * `ReturnRequest.status → COMPLETED`
    * `LostItem.status → RETURNED`

(Можно реализовать как отдельные методы в контроллере или объединить под /transition.)

---

## 5. Слои приложения (3-tier)

### 5.1. Пример структуры пакетов

```text
cz.cvut.tjv.lostfound
 ├─ domain        // entities + enums
 ├─ dto           // DTO классы для API
 ├─ repository    // Spring Data JPA репозитории
 ├─ service       // бизнес-логика
 ├─ controller    // REST контроллеры
 └─ config        // конфигурация (OpenAPI, CORS, и т.п.)
```

---

## 6. План реализации (по шагам)

### 6.1. Подготовка проекта

* [ ] Создать **Spring Boot** проект (Gradle):

  * зависимости:

    * `spring-boot-starter-web`
    * `spring-boot-starter-data-jpa`
    * DB driver (`postgresql` / `mysql` / другой server DB)
    * (опц.) `spring-boot-starter-validation`
    * (опц.) `springdoc-openapi-starter-webmvc-ui`
* [ ] Настроить подключение к реальной БД (НЕ in-memory) в `application.yml`.
* [ ] Проверить, что `./gradlew build` работает.

---

### 6.2. Domain layer (entities + enums)

* [ ] Создать entity-класс `LostItem`

  * поля согласно таблице
  * `@Entity`, `@Id`, `@GeneratedValue`
  * `@ManyToMany` к `Category`
  * `@OneToMany(mappedBy = "item")` к `ReturnRequest`
  * enum `LostItemStatus`

* [ ] Создать entity-класс `Category`

  * поля согласно таблице
  * `@Entity`
  * обратная сторона `@ManyToMany(mappedBy = "categories")`

* [ ] Создать entity-класс `ReturnRequest`

  * поля согласно таблице
  * `@Entity`
  * `@ManyToOne(optional = false)` к `LostItem`
  * enums:

    * `ReturnRequestStatus`
    * `FulfillmentType`

---

### 6.3. Repository layer (Spring Data JPA)

* [ ] `LostItemRepository extends JpaRepository<LostItem, Long>`

  * [ ] метод для extra JPQL запроса (`findAvailableItemsByCategoryAndFoundAtBetween` или аналогичный)
* [ ] `CategoryRepository extends JpaRepository<Category, Long>`
* [ ] `ReturnRequestRepository extends JpaRepository<ReturnRequest, Long>`

---

### 6.4. DTO модель и маппинг

* [ ] Создать DTO-классы:

  * `LostItemDto`
  * `LostItemCreateDto` / `LostItemUpdateDto`
  * `CategoryDto`
  * `CategoryCreateDto`
  * `ReturnRequestDto`
  * `ReturnRequestCreateDto` / `ReturnRequestUpdateDto`
* [ ] Реализовать маппинг Entity ↔ DTO:

  * руками (service/mapper класс)
  * или через MapStruct (если хочешь, но можно и без).

---

### 6.5. Service layer (бизнес-логика)

#### 6.5.1. `LostItemService`

* [ ] `List<LostItem> findAll(...)` (с фильтрами)
* [ ] `LostItem findById(Long id)`
* [ ] `LostItem create(LostItemCreateDto dto)` (или принимающий domain)
* [ ] `LostItem update(Long id, LostItemUpdateDto dto)`
* [ ] `void delete(Long id)`
* [ ] `void addCategory(Long itemId, Long categoryId)`
* [ ] `void removeCategory(Long itemId, Long categoryId)`
* [ ] `List<LostItem> findAvailableByCategoryAndDateInterval(...)` (обёртка над JPQL-запросом)

#### 6.5.2. `CategoryService`

* [ ] стандартный CRUD:

  * `findAll`, `findById`, `create`, `update`, `delete`

#### 6.5.3. `ReturnRequestService`

* [ ] `List<ReturnRequest> findAll(...)` (фильтр по статусу / item / email)
* [ ] `ReturnRequest findById(Long id)`
* [ ] `ReturnRequest create(ReturnRequestCreateDto dto)`

  * устанавливает `status = PENDING`, `createdAt = now()`
* [ ] `ReturnRequest update(Long id, ReturnRequestUpdateDto dto)` (до обработки)
* [ ] `void cancel(Long id)` – ставит `status = CANCELLED`
* [ ] `ReturnRequest approve(Long id)`

  * `status = APPROVED`
  * `processedAt = now()`
  * `LostItem.status = RESERVED`
* [ ] `ReturnRequest reject(Long id, String reason?)`

  * `status = REJECTED`
  * `processedAt = now()`
* [ ] `ReturnRequest complete(Long id)`

  * `status = COMPLETED`
  * `LostItem.status = RETURNED`

---

### 6.6. Controller layer (REST API)

* [ ] `LostItemController`

  * [ ] `GET /api/items`
  * [ ] `GET /api/items/{id}`
  * [ ] `POST /api/items`
  * [ ] `PUT /api/items/{id}`
  * [ ] `DELETE /api/items/{id}`
  * [ ] `POST /api/items/{id}/categories/{categoryId}`
  * [ ] `DELETE /api/items/{id}/categories/{categoryId}`

* [ ] `CategoryController`

  * [ ] `GET /api/categories`
  * [ ] `GET /api/categories/{id}`
  * [ ] `POST /api/categories`
  * [ ] `PUT /api/categories/{id}`
  * [ ] `DELETE /api/categories/{id}`

* [ ] `ReturnRequestController`

  * [ ] `GET /api/requests`
  * [ ] `GET /api/requests/{id}`
  * [ ] `POST /api/requests`
  * [ ] `PUT /api/requests/{id}`
  * [ ] `DELETE /api/requests/{id}` (или `cancel`)
  * [ ] `POST /api/requests/{id}/approve`
  * [ ] `POST /api/requests/{id}/reject`
  * [ ] `POST /api/requests/{id}/complete`

Соблюдать:

* корректные HTTP-коды (200/201/204/400/404/409 и т.д.)
* RESTful стиль (ресурсы, глаголы: GET/POST/PUT/DELETE)

---

### 6.7. Документация API (OpenAPI)

* [ ] Подключить `springdoc-openapi` (WebMVC).
* [ ] Настроить доступ к Swagger UI (например, `/swagger-ui.html`).
* [ ] Убедиться, что все endpoints отображаются, DTO описаны.
* [ ] При необходимости добавить описания (`@Operation`, `@Schema` и т.д.).

---

### 6.8. Тесты (3 типа)

Требование: **3 разных типа тестов** (пример):

* [ ] **Unit тесты** (Jupiter) для сервисов:

  * `LostItemServiceTest`
  * `ReturnRequestServiceTest`
  * с использованием моков репозиториев (`@ExtendWith(MockitoExtension.class)`)

* [ ] **Integration тесты репозиториев**:

  * `@DataJpaTest`
  * `LostItemRepositoryTest`, `ReturnRequestRepositoryTest`
  * с тестовой БД (H2 только для тестов – это ок)

* [ ] **Web/Controller тесты (MockMvc)**:

  * `@WebMvcTest`
  * `LostItemControllerTest`, `ReturnRequestControllerTest`
  * проверка HTTP-кодов, JSON-ответов и т.п.

Все тесты должны выполняться при `./gradlew test` / `./gradlew build`.

---

### 6.9. Клиентская часть

* [ ] Выбрать стек клиента:

  * простой вариант: **консольное интерактивное приложение** на Java/Kotlin/Python
  * или веб-клиент (React/Vue) – по желанию
* [ ] Реализовать **одну комплексную бизнес-операцию**, напр.:

  > *"Найти вещь по параметрам → выбрать → оформить заявку на возврат"*

  * шаги клиента:

    * запрос к `GET /api/items` с фильтрами
    * показ списка пользователю
    * выбор вещи
    * сбор данных пользователя
    * `POST /api/requests` с нужными полями
* [ ] Сделать нормальное управление ошибками (показать пользователю, если сервер вернул 4xx/5xx).

---

### 6.10. Git + Gitlab

* [ ] Создать репозиторий `gitlab.fit.cvut.cz/<username>/<server_repo>`.
* [ ] Коммитить по шагам (не один гигантский коммит).
* [ ] Настроить `.gitignore` (Java, Gradle, IDE).
* [ ] При необходимости – отдельный репозиторий для клиента или общий монорепо.

---

Этот файл можно класть как `docs/spec.md` или `README.md` и использовать как чеклист по проекту.

