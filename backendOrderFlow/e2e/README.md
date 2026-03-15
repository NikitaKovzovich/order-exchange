# OrderFlow E2E

Этот набор предназначен для автопроверки основных бизнес-флоу ТЗ по всем ролям через Postman/Newman.

## Что входит

- `postman/OrderFlow.E2E.postman_collection.json` — основная коллекция;
- `postman/OrderFlow.local.postman_environment.json` — локальное окружение;
- `test-files/payment-proof.txt` — файл для проверки multipart `payment-proof`;
- `test-files/support-attachment.txt` — файл для проверки вложений в support tickets;
- `../run-e2e.ps1` — PowerShell runner с автоматическим сбором docker-логов при падении.

## Покрытие бизнес-флоу

### ADMIN
- логин;
- dashboard/statistics;
- поиск заявок на верификацию;
- approve / reject verification;
- просмотр списка пользователей;
- block / unblock пользователя;
- модерация товаров.

### SUPPLIER
- профиль компании;
- справочники и работа с каталогом;
- создание товара и публикация каталога;
- управление остатками;
- подтверждение заказа;
- подтверждение оплаты;
- генерация ТТН;
- отгрузка;
- закрытие заказа;
- корректировка после расхождения;
- order chat;
- аналитика;
- уведомления.

### RETAIL_CHAIN
- поиск товаров;
- база поставщиков;
- запрос на сотрудничество;
- корзина и checkout;
- оплата заказа;
- приемка без расхождений;
- сценарий расхождения;
- журнал приемки;
- support ticket с вложением;
- аналитика.

## Быстрый запуск

Из корня репозитория:

```powershell
./run-e2e.ps1
```

С переопределением базового URL:

```powershell
./run-e2e.ps1 -BaseUrl "http://localhost:8765"
```

Если зависимости уже установлены:

```powershell
./run-e2e.ps1 -SkipInstall
```

## Что происходит при падении

Если Newman завершился с ошибкой, runner автоматически складывает артефакты в `e2e/artifacts/`:

- `reports/newman-*.json` — подробный JSON-отчет;
- `reports/newman-*.xml` — JUnit-отчет;
- `reports/newman-*.html` — HTML-отчет;
- `logs/docker-ps-*.log` — состояние контейнеров;
- `logs/<service>-*.log` — логи сервисов и инфраструктуры.

## Как валидировать проблему по логам

1. Открыть HTML или JSON-отчет Newman и определить упавший request.
2. По имени request понять доменный блок:
   - `Admin` / `auth` → `auth-service`;
   - `products`, `partnerships`, `inventory` → `catalog-service`;
   - `orders`, `cart`, `analytics`, `acceptance-journal` → `order-service`;
   - `chats`, `support` → `chat-service`;
   - `generated-documents` → `document-service`;
   - маршрутизация/401 на входе → `api-gateway`.
3. Проверить `docker-ps-*.log` и затем соответствующий лог сервиса.
4. Для межсервисных проблем дополнительно смотреть `rabbitmq`, `minio`, `eureka-server`.

## Важные замечания

- Коллекция рассчитана на запуск **сверху вниз**.
- Используются seeded-аккаунты:
  - `admin@test.com / password123`
  - `supplier@test.com / password123`
  - `retailchain@test.com / password123`
- Для admin verification коллекция дополнительно создает 2 новых компании с уникальными email/УНП на каждом прогоне.
- Коллекция специально проверяет оба режима `payment-proof`:
  - `multipart/form-data`;
  - legacy JSON по `documentKey`.

