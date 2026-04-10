# API tests — testslotegrator.com

Maven-проект с **API-автотестами** для `https://testslotegrator.com`: RestAssured, JUnit 5, AssertJ, Jackson, dotenv для секретов, **Allure** для отчётов.

Продакшен-кода нет: всё лежит в **`src/test/java`**.

---

## Важно: Swagger и реальный API

Фактическое поведение эндпоинтов **сильно отличается** от того, что обычно ожидается по Swagger/OpenAPI (коды ответа, форма тел запросов/ответов, вложенность JSON). Пришлось **импровизировать** по живым ответам сервера, например:

- **`POST .../login`** для успешного логина отдаёт **201 Created**, а не классический 200.
- **`POST .../automationTask/getOne`** тоже завязан на **201**, хотя по смыслу это чтение сущности.
- **`GET .../automationTask/getAll`**: структура списка пользователей в JSON **неочевидна** — в клиенте заложены варианты корня массива: корень ответа, `data`, `users` (см. `AutomationTaskClient.extractUsersList`).
- Идентификатор в объектах может приходить как **`_id`** или **`id`** — везде учитывается оба варианта (`extractId`, `userIdFromMap`).

Имеет смысл сверять тесты с **реальными** ответами и логами RestAssured, а не только со спецификацией.

---

## Стек

| Компонент        | Назначение |
|------------------|------------|
| **RestAssured**  | HTTP, спеки, JSON |
| **JUnit 5**      | тесты, lifecycle |
| **AssertJ**      | проверки в `LoginApiTest` |
| **Hamcrest**     | матчеры в негативных сценариях логина (через RestAssured) |
| **Jackson**      | сериализация DTO (records с `@JsonProperty` для `snake_case`) |
| **dotenv-java**  | `TEST_EMAIL`, `TEST_PASSWORD` из `.env` в корне проекта |
| **Allure**       | `allure-junit5`, шаги и метки в отчёте |

Java **17**, кодировка UTF-8.

---

## Структура проекта

```text
slotegrator/
├── pom.xml
├── .env                          # не коммитить; TEST_EMAIL, TEST_PASSWORD
├── README.md
├── allure-results/               # артефакты Allure (после прогона; при желании в .gitignore)
└── src/test/java/
    ├── specs/
    │   └── BaseSpec.java         # base URI, JSON content-type/accept
    ├── clients/
    │   ├── AuthClient.java       # логин, getAccessToken(), сырой ответ при необходимости
    │   └── AutomationTaskClient.java
    │                             # create, getOne, getAll, delete + разбор id/списка пользователей
    ├── models/
    │   ├── LoginRequest.java
    │   ├── LoginResponse.java
    │   ├── UserResponse.java
    │   ├── AutomationTaskCreateRequest.java   # record + фабрика testUser(index, timestamp)
    │   └── AutomationTaskGetOneRequest.java    # record { email }
    ├── teststeps/                # пакет testSteps
    │   └── AutomationTaskFlowSteps.java
    │                             # сценарные шаги с Allure.step(...)
    └── tests/
        ├── AutomationTaskFlowTest.java
        ├── LoginApiTest.java
        └── support/
            └── AutomationUsersCleanup.java    # getAll → удалить всех; используется как precondition
```

---

## Что уже сделано

### Логин (`AuthClient`, `LoginApiTest`)

- Успешный логин с десериализацией в `LoginResponse` / `UserResponse` и проверками AssertJ.
- Негативные кейсы: `null`/отсутствие email или password, короткий пароль, невалидный email, неверный пароль, несуществующий пользователь — ожидаемые статусы и тела ответов через Hamcrest.

### Automation Task API

- **`AutomationTaskClient`** — единая точка вызовов `/api/automationTask/*` с Bearer-токеном.
- **`AutomationTaskFlowTest`** — сквозной сценарий: создание двух пользователей → `getOne` по email первого → `getAll` (наличие созданных, сортировка по `name`) → удаление по id → пустой `getAll`.
- Перед сценарием **`@BeforeEach`** вызывает **`AutomationUsersCleanup.deleteAllExisting()`**, чтобы не копить данные от прошлых запусков.
- Шаги сценария вынесены в **`AutomationTaskFlowSteps`** (см. Allure ниже).

### Конфигурация

- **`BaseSpec`**: `https://testslotegrator.com`, `application/json`.
- Учётные данные — из **`.env`** (`dotenv-java`).

---

## Allure

Подключён **`allure-junit5`** (см. `pom.xml`). В отчёт попадают результаты тестов и **шаги**.

### Шаги без AspectJ

Шаги реализованы через **`io.qameta.allure.Allure.step(String name, …)`** (лямбды), а не через аннотацию **`@Step`** и **AspectJ weaver**. Так проще поддерживать **новые JDK**: старый `aspectjweaver` ломается на свежих class file versions (например, с сообщением вроде `Unsupported class file major version …`). Программные шаги работают на любом JDK без `-javaagent`.

### Где смотреть шаги в коде

- **`tests.AutomationTaskFlowTest`** — аннотации **`@Epic`**, **`@Feature`**, **`@DisplayName`** (человекочитаемое имя кейса в отчёте).
- **`teststeps.AutomationTaskFlowSteps`** — вложенные **`Allure.step(...)`**: precondition очистки, получение токена, создание пользователей (внутри — шаг на каждого), проверки `getOne` / `getAll`, удаление, финальная проверка пустого списка.

### Результаты и отчёт

После `mvn test` или запуска из IDE Allure пишет JSON в каталог результатов (часто **`allure-results`** в корне репозитория или **`target/allure-results`** — зависит от настроек запуска; при необходимости задайте системное свойство **`allure.results.directory`**, например в `maven-surefire-plugin` → `<systemPropertyVariables>`).

Сборка HTML-отчёта (нужен [Allure CLI](https://github.com/allure-framework/allure2)):

```bash
allure serve allure-results
# или, если результаты в target:
allure serve target/allure-results
```

Опционально можно добавить **`io.qameta.allure:allure-maven-plugin`** и вызывать `mvn allure:serve` — в README это не зафиксировано в `pom.xml`, только описано как типичный вариант.

---

## Запуск

Из корня проекта:

```bash
mvn test
```

Один класс:

```bash
mvn test -Dtest=AutomationTaskFlowTest
mvn test -Dtest=LoginApiTest
```

Создайте **`.env`** в корне:

```env
TEST_EMAIL=your@email.com
TEST_PASSWORD=your_password
```

---

## Возможные улучшения

- Вынести base URI и пути в конфиг / профили Maven.
- Подключить **allure-maven-plugin** и зафиксировать **`allure.results.directory`** в Surefire.
- Расширить клиенты и модели под остальные API, когда спецификация и фактическое API сойдутся или будут задокументированы отличия.
