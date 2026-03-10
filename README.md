## Тема проекта

REST‑сервис банка: управление клиентами, счетами, картами и транзакциями.

## Основные сущности

- **Customer** – клиент банка (ФИО, email).
- **Account** – банковский счет клиента (номер, баланс, ссылка на клиента).
- **Card** – банковская карта, привязанная к счету (номер, статус блокировки).
- **Transaction** – перевод/операция по счетам (счет‑источник, счет‑получатель, сумма, время).

## Операции сервиса

- **CRUD по всем сущностям**:
  - `Customer`: `POST/GET/PUT/DELETE /api/customers`.
  - `Account`: `POST/GET/PUT/DELETE /api/accounts`.
  - `Card`: `POST/GET/PUT/DELETE /api/cards`.
  - `Transaction`: `POST/GET/PUT/DELETE /api/transactions`.
- **Бизнес‑операции (транзакционные)** (`/api/operations`):
  - `POST /api/operations/open-account` – открыть счет для клиента.
  - `POST /api/operations/issue-card` – выпустить карту для счета.
  - `POST /api/operations/deposit/{accountId}` – пополнение счета.
  - `POST /api/operations/withdraw/{accountId}` – снятие средств (без ухода в минус).
  - `POST /api/operations/transfer` – перевод между счетами.

## База данных

- По умолчанию используется **H2 in‑memory** (для быстрого запуска, без установки БД).
- Для PostgreSQL создан профиль `prod`:
  - файл `laba22/src/main/resources/application-prod.properties`;
  - используются переменные окружения: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Связи и ограничения:
  - `Customer` ↔ `Account` – связь «один‑ко‑многим» (через `customer_id` в таблице `accounts`), email клиента уникален.
  - `Account` ↔ `Card` – связь «один‑ко‑многим` (через `account_id` в таблице `cards`), номер счета и номер карты уникальны.
  - `Transaction` ссылается на два счета: `from_account_id`, `to_account_id`, сумма и время обязательны.

## Как запускать

1. **Dev (H2):**
   - перейти в каталог `laba22`;
   - запустить `Laba22Application` из IDE или командой `./mvnw spring-boot:run`;
   - база и тестовые данные поднимаются автоматически из `src/main/resources/data.sql`.
2. **PostgreSQL (prod):**
   - создать БД `bank_project_db`;
   - выставить переменные окружения:
     - `DB_URL=jdbc:postgresql://localhost:5432/bank_project_db`
     - `DB_USERNAME=<пользователь>`
     - `DB_PASSWORD=<пароль или пусто>`
   - запустить приложение с профилем `prod`, например:
     - в IDE: `SPRING_PROFILES_ACTIVE=prod`;
     - из терминала: `SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run`.

## Postman

- Коллекция запросов лежит в `laba22/postman/bank-api.postman_collection.json`.
- В коллекции настроена переменная окружения `baseUrl` (`http://localhost:8080`).
- Можно прогнать сценарий: создать клиента → открыть счет → выпустить карту → пополнить, перевести и т.д.

