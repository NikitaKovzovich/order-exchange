# Coverage Audit: backend endpoints -> UI entrypoints

Дата фиксации: 2026-03-15

## Scope

Аудит основан на:
- текущем коде `clientOrderFlow`
- текущих контроллерах backend-сервисов
- фактических Angular service / component связях
- успешной production-сборке фронтенда (`npm run build`)

Это **code-level audit**, а не полный ручной e2e smoke для каждого endpoint.

---

## Executive summary

### Подтверждённо покрыто UI
- аутентификация и регистрационные потоки
- role-based routing для `SUPPLIER` и `RETAIL_CHAIN` настроен (`/supplier`, `/retail`) через guard + default route mapping
- retail/supplier order flows
- order chat channels/messages через routed communications screens
- order documents и download-all archive
- supplier product detail/edit + stock update
- supplier/retail support list/detail/messages
- admin `users` list/detail + block/unblock/delete + edit
- admin `verification` list/detail + approve/reject
- admin `support` list/detail + assign/resolve/close/reopen + messages
- admin `content` moderation list + actions + category CRUD
- admin `dictionaries` (units/VAT CRUD)
- admin/supplier/retail notifications backend-backed
- dashboards / analytics / notifications / support / suppliers / cart / reception

### Частично покрыто UI
- по основным routed entrypoint'ам подтверждённых partial-gap'ов на текущую фиксацию не осталось
- supplier document screens используют status-aware order-level workflow и read/download flows; direct ad-hoc generation через `/api/generated-documents/*` намеренно не подключался, так как для UI-safe сценариев достаточно order-level действий

### Непокрыто / осталось как tech-debt
- подтверждённых blocker-gap'ов по основным routed entrypoint'ам не зафиксировано; доказуемо orphan admin bundle `clientOrderFlow/src/app/admin/users/user-detail.*` уже удалён

---

## Coverage matrix

## 1. Auth service

| Backend controller / endpoint family | Frontend service | UI entrypoint | Status | Notes |
|---|---|---|---|---|
| `AuthController` login/session/profile | `auth.service.ts`, `auth-api.service.ts`, bootstrap services | login, registration, app bootstrap, role-based routing | Covered | Основные auth entrypoints присутствуют |
| `VerificationController` admin verification list/detail/actions | `admin.service.ts` | `admin/verification`, `admin/verification/:id` | Covered | server-side pagination/filter/search подключены |
| `AdminController` dashboard KPI endpoints | `admin.service.ts` | `admin/dashboard`, `admin/analytics` | Covered | Через admin dashboard/analytics экраны |
| `AdminController /users` list/search/page | `admin.service.ts` | `admin/users` | Covered | server-side role/status/search/pagination |
| `AdminController /users/{id}` detail | `admin.service.ts` | `admin/users/:id` | Covered | Реальные данные вместо моков |
| `AdminController /users/{id}/block` | `admin.service.ts` | `admin/users/:id` | Covered | Через модалку |
| `AdminController /users/{id}/unblock` | `admin.service.ts` | `admin/users/:id` | Covered | Через detail screen |
| `AdminController PUT /users/{id}` | `admin.service.ts` | `admin/users/:id` | Covered | Подключена edit-модалка |
| `AdminController DELETE /users/{id}` | `admin.service.ts` | `admin/users/:id` | Covered | Soft-delete через detail screen |
| `AdminController /users/{id}/events` | `admin.service.ts` | `admin/users/:id` | Covered | Последние события показываются в detail |

## 2. Catalog service

| Backend controller / endpoint family | Frontend service | UI entrypoint | Status | Notes |
|---|---|---|---|---|
| public/search product catalog | `catalog.service.ts` | retail catalog, supplier catalog | Covered | Основные сценарии заказа и управления каталогом присутствуют |
| categories / reference data / units / VAT | `catalog.service.ts` | create/edit product, admin dictionaries, `admin/content` categories tab | Covered | `admin/dictionaries` использует реальные create/edit/delete flows для units/VAT, category CRUD остаётся в `admin/content` |
| supplier product CRUD | `catalog.service.ts` | supplier catalog flows | Covered | list/create/detail/edit + stock update подключены |
| product images | `catalog.service.ts` | supplier add/edit product | Covered | Upload используется в create flow |
| partnership endpoints | `partnership.service.ts` | retail suppliers / partnerships | Covered | Ранее уже внедрено |
| admin moderation `/api/products/admin/all` | `admin.service.ts` | `admin/content` | Covered | server-side supplier/category/search/pagination подключены для moderation list |
| admin product hide/show/delete | `admin.service.ts` | `admin/content` | Covered | Через moderation actions |

## 3. Order service

| Backend controller / endpoint family | Frontend service | UI entrypoint | Status | Notes |
|---|---|---|---|---|
| customer orders list/detail | `order.service.ts` | `retail-network/orders`, `retail-network/orders/:id` | Covered | server-side filters/pagination для retail orders list |
| supplier orders list/detail | `order.service.ts` | `supplier/orders`, supplier order detail flows | Covered | list на server-side filters/pagination |
| order lifecycle actions (`confirm`, `reject`, `cancel`, `ship`, `deliver`, `close`, `correct`) | `order.service.ts` | order detail screens | Covered | UI entrypoints присутствуют по ролям |
| payment proof upload | `order.service.ts` | retail order detail | Covered | multipart flow подключён |
| discrepancy flows | `order.service.ts` | retail order detail / reception-related flows | Covered | create + list присутствуют |
| repeat order | `order.service.ts` | retail order detail | Covered | alert заменён на UI notification |
| order history / documents | `order.service.ts`, `document.service.ts` | order detail screens, `supplier/orders/:id/invoice`, `supplier/orders/:id/upd` | Covered | history + docs list + routed status-aware document workspace доступны |
| cart / checkout | `cart.service.ts` | `retail-network/cart` | Covered | alert убран, UI notification добавлен |
| order notifications | `notification.service.ts`, `user-notification.service.ts` | retail/supplier notifications, admin notifications | Covered | retail/supplier используют real notification API; admin notifications переведены на `/api/notifications` с real read-state |
| acceptance journal | `acceptance-journal.service.ts` | retail reception | Covered | Ранее был внедрён |
| analytics endpoints | analytics services | dashboards / analytics | Covered | admin/supplier/retail analytics экраны используют реальные backend данные |

## 4. Document service

| Backend controller / endpoint family | Frontend service | UI entrypoint | Status | Notes |
|---|---|---|---|---|
| `/api/documents/**` upload/download/list | `document.service.ts` | payment proofs, company docs, order docs | Covered | Используется в order and company-related screens |
| `/api/generated-documents/**` generate/download/list by order | `document.service.ts` | order-related document flows | Covered | Включая download-all archive |
| aggregated order download-all | `order-document-download.service.ts` | retail orders list/detail | Covered | Клиентский ZIP из uploaded + generated документов |

## 5. Chat / support service

| Backend controller / endpoint family | Frontend service | UI entrypoint | Status | Notes |
|---|---|---|---|---|
| support ticket create/list/detail/messages (user side) | `support.service.ts`, `chat.service.ts` | retail support, supplier support | Covered | retail и supplier support используют реальные list/detail/messages flows |
| admin support list `/api/support/tickets/admin` | `support.service.ts` | `admin/support` | Covered | server-side pagination + status filter + search |
| admin support detail/messages | `support.service.ts` | `admin/support/:id` | Covered | detail screen грузит реальный тикет и реальные сообщения |
| assign ticket | `support.service.ts` | `admin/support`, `admin/support/:id` | Covered | action доступен из list и detail |
| resolve/reopen/close ticket | `support.service.ts` | `admin/support/:id`, user support detail | Covered | admin detail screen подключён к backend actions |
| order chat channels/messages | `chat.service.ts` | `supplier/communications`, `retail/communications` | Covered | Routed communications screens используют реальные chat channels/messages |

## 6. Admin verification / users / support / content state

### `admin/users`
- list подключён к backend pagination/search/status/role filter
- detail подключён к реальному backend profile
- edit / block / unblock / delete доступны через UI
- flash notifications работают при возврате к списку

### `admin/verification`
- list переведён на server-side pagination/search/status/role filter
- detail использует реальные backend data
- approve/reject без `alert/confirm`, через нормальный UI
- flash notifications работают при возврате к списку

### `admin/support`
- list использует server-side pagination/status/search
- detail использует реальный backend ticket/messages
- actions `assign / resolve / close / reopen` подключены к backend без mock flow

### `admin/content`
- moderation products используют server-side `supplier/category/search/pagination`
- hide/show/delete подключены к backend
- category management tab подключён к backend `create/edit/delete`

### `admin/dictionaries`
- `units` и `VAT rates` подключены к backend `ReferenceDataController`
- create / edit / delete используют реальные HTTP-запросы, без локальных заглушек
- после мутаций инвалидируется клиентский cache, поэтому UI не остаётся на устаревших данных

### `notifications`
- `admin/notifications` использует реальный backend notification API (`/api/notifications`) с `markAsRead` / `markAllAsRead`
- `supplier/notifications` помечает уведомление прочитанным при переходе к связанному заказу
- misleading локальное удаление в `retail/notifications` удалено; открытие уведомления использует backend `markAsRead`

## 7. Supplier / retail_chain routed UI state

### `SUPPLIER` (URL: `/supplier`)
- маршрутизация и role-gating настроены через `authGuard` и default route `/supplier/dashboard`
- основные экраны `dashboard`, `catalog`, `add product`, `orders`, `communications`, `notifications`, `analytics`, `clients` ходят в реальные сервисы
- основные routed entrypoint'ы покрыты реальными backend flows
- `orders/:id/invoice` и `orders/:id/upd` используют status-aware workspace: просмотр/скачивание документов + безопасные order-level document actions

### `RETAIL_CHAIN` (URL: `/retail`)
- маршрутизация и role-gating настроены через `authGuard` и default route `/retail/dashboard`
- основные экраны `dashboard`, `catalog`, `cart`, `orders`, `order detail`, `reception`, `communications`, `support`, `notifications`, `suppliers/:id/catalog`, `analytics` подключены к backend
- модуль покрывает основные routed entrypoint'ы без подтверждённых placeholder/mock screen в текущем срезе

---

## Remaining gaps / explicit follow-up items

1. **Next priority backlog**
   - опционально расширить supplier document workspace до direct generated-doc editor/generator, если это понадобится продуктово
   - frontend performance: coarse-grained lazy bundles `supplier-module` / `retail-network-module` уже разрезаны на route-level chunks; дополнительно `chart.js` вынесен в deferred import для admin/supplier/retail dashboard+analytics. Remaining tech-debt: dev cold-load на Windows/`ng serve` может оставаться медленным из-за source maps и Angular dev server, а shared lazy chunk для графиков (`auto`) можно оптимизировать дальше только как отдельную performance-итерацию

2. **Codebase hygiene (optional)**
   - после стабилизации routed entrypoints можно продолжить точечную зачистку возможного scaffolding/unused artifacts, но подтверждённый orphan admin user-detail bundle уже удалён

---

## Verification performed

Проверено production-сборкой фронтенда:

```powershell
Set-Location "C:\Users\pavel\IdeaProjects\order-exchange\clientOrderFlow"
npm run build
```

Результат: **build successful**.

Проверено backend build+tests:

```powershell
Set-Location "C:\Users\pavel\IdeaProjects\order-exchange\backendOrderFlow"
.\build-and-test.ps1
```

Результат: **500 tests passed, 0 failed**.

Проверено runtime E2E business-flow коллекцией:

```powershell
Set-Location "C:\Users\pavel\IdeaProjects\order-exchange\backendOrderFlow"
.\run-e2e.ps1
```

Результат: **69 requests / 69 assertions passed / 0 failed**.

