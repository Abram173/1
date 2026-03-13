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
  - `Account` ↔ `Card` – связь «один‑ко‑многим» (через `account_id` в таблице `cards`), номер счета и номер карты уникальны.
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

---

## Лаба 6: HTTPS и CI

### Цепочка сертификатов (3 звена)

Имена: **BankRootCA**, **BankInterCA**, **BankServerCert**. В субъектах сертификатов указывается номер студенческого билета (O=BankLab-&lt;STUDENT_ID&gt;).

1. Сгенерировать цепочку и keystore:
   ```bash
   cd laba22/scripts
   export STUDENT_ID=12345678   # твой номер студбилета
   chmod +x gen-cert-chain.sh
   ./gen-cert-chain.sh ../certs
   ```
   В каталоге `laba22/certs` появятся: `BankRootCA.crt`, `BankInterCA.crt`, `BankServerCert.crt`, `bank-server.p12` (пароль по умолчанию: `changeit`).

2. Добавить корневой сертификат в доверенные (чтобы браузер не ругался):
   - **macOS:** «Связка ключей» → «Связка “системная”» → «Сертификаты» → перетащить `BankRootCA.crt` → дважды клик → «Доверять: всегда».
   - **Windows:** дважды клик по `BankRootCA.crt` → «Установить сертификат» → «Локальный компьютер» → «Поместить все сертификаты в следующее хранилище» → «Доверенные корневые центры сертификации».
   - **Браузер (Chrome/Firefox):** можно импортировать тот же `BankRootCA.crt` в настройках конфиденциальности/сертификатов.

### Запуск сервиса по HTTPS (TLS)

- Скопировать `laba22/certs/bank-server.p12` в `laba22/` (или указать путь в переменной).
- Запуск с профилем `tls`:
  ```bash
  cd laba22
  export SSL_KEY_STORE=file:./certs/bank-server.p12
  export SSL_KEY_STORE_PASSWORD=changeit
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=tls
  ```
  Сервис будет доступен по **https://localhost:8443**.

### CI (GitHub Actions)

- В проекте настроен workflow `laba22/.github/workflows/ci.yml`: компиляция, тесты, упаковка JAR, загрузка артефакта в хранилище GitHub Actions.
- Для использования keystore в CI (опциональный шаг «Run with HTTPS») добавь в **GitHub → Settings → Secrets and variables → Actions** два секрета:
  - **SSL_KEY_STORE_BASE64** — содержимое файла `bank-server.p12`, закодированное в Base64 (в терминале: `base64 -i laba22/certs/bank-server.p12 | pbcopy` или аналог).
  - **SSL_KEY_STORE_PASSWORD** — пароль от keystore (например, `changeit`).
