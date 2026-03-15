# OrderFlow frontend API tracking

Статус: рабочий tracking покрытия клиентской части относительно `backendOrderFlow/.docs/frontend-client-api-min-spec.md`.

Легенда:
- `done` — реализовано и подключено в коде
- `partial` — есть типы/сервис или частичная интеграция, но не всё UI/flow закрыто
- `missing` — ещё не реализовано
- `blocked` — зависит от backend/gateway-контракта или требует отдельной доработки

Обновлено: 2026-03-15

## 3. Auth и профиль

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| `POST /api/auth/login` | done | `AuthApiService`, `AuthService`, `token storage`, login page | — |
| `GET /api/auth/profile` | done | типы + `refreshProfile()` + session sync + bootstrap restore | — |
| `GET /api/auth/company/{companyId}` | done | typed method в `AuthApiService/AuthService` | подключить в UI профиля компании |
| role/session helpers | done | `hasRole`, `hasAnyRole`, `getDefaultRoute*` | — |
| interceptor auth headers | done | только `Authorization: Bearer` | gateway должен добавлять `X-User-*` |

## 4. Справочники и bootstrap

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| `GET /api/categories/tree` | done | `CatalogService` | — |
| `GET /api/categories` | done | `CatalogService` | — |
| `GET /api/units` | done | `CatalogService` | — |
| `GET /api/vat-rates` | done | `CatalogService` | — |
| bootstrap после login | done | добавлен `AppBootstrapService` с preload `profile/categories/units/vat-rates` | — |

## 5. Supplier: каталог, остатки, партнёрства

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| supplier products CRUD/status | done | `CatalogService` | — |
| publish/update catalog | partial | backend endpoint известен | добавить методы и кнопки UI |
| product images | done | `CatalogService` | — |
| inventory get/update | done | `CatalogService` | — |
| supplier partnerships | done | `PartnershipService` + экран `supplier/clients` для pending/active/all и редактирования договора | — |

## 6. Retail chain: suppliers, catalog, cart

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| supplier directory | done | `PartnershipService` + интеграция `retail-network/suppliers` | — |
| create partnership request | done | запрос на сотрудничество подключён из карточки поставщика | позже можно добавить форму реквизитов вместо автозаполнения-заглушки |
| retail catalog | done | `CatalogService` | — |
| cart | done | `CartService` | checkout response можно доуточнить по UI |

## 7. Orders

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| supplier/customer order lists | done | supplier/retail order lists переведены на `summary`, server-side filters и pagination | — |
| order detail | done | supplier/retail detail pages подключены к `history/documents/discrepancies` | — |
| supplier actions | partial | добавлены `generate-ttn`, `correct`, `summary`, `close`; detail UI расширен | summary badges ещё можно вывести в list/dashboard |
| customer actions | done | retail detail поддерживает `payment-proof multipart`, `repeat`, `discrepancy`, `deliver`, `cancel` | — |
| acceptance journal | done | `AcceptanceJournalService` + интеграция `retail-network/reception` | — |

## 8. Documents

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| document upload / entity docs | partial | `DocumentService` выровнен под `documentTypeCode`, `entity docs`, `url` | подключить новые методы в UI документов |
| document types | done | добавлен `getDocumentTypes()` | — |
| generated documents | done | added typed client для `generated-documents` + download/url | подключить list UI при необходимости |

## 9. Order chat

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| chats / channel / messages / read | done | `ChatService`, supplier communications UI | расширить search при необходимости |

## 10. Support tickets

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| user tickets / create / messages | done | выделен `SupportService`, supplier/retail support UI подключены к API | — |
| multipart attachments | done | multipart create-message/create-ticket подключены в support forms supplier/retail | — |
| admin actions | done | `SupportService` содержит `admin list`, `assign`, `resolve`, `close`, `reopen` | подключить admin support UI при необходимости |

## 11. Notifications и dashboards

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| order notifications | done | `NotificationService` + интеграция supplier/retail notification pages | — |
| supplier dashboard | done | `dashboard/supplier` подключён к backend analytics/dashboard + low-stock | — |
| customer dashboard | done | `dashboard/customer` подключён к backend analytics/dashboard | — |
| advanced analytics | done | `AnalyticsService` расширен dashboard endpoint-ами и `product-history` | — |

## 12. Admin API

| Endpoint / блок | Статус | Что сделано | Gap / next step |
|---|---|---|---|
| dashboard stats/users/verifications/tickets | done | `AdminService` + admin dashboard используют `orders-stats`, `recent-*`, `verification-rate`, `registration-activity` | — |
| verification | partial | базовые методы есть | выровнять фильтры/pagination |
| users | partial | базовые методы есть | добавить update/delete/events |
| product moderation | partial | `AdminService` + интеграция `admin/content` для списка/hide/show/delete | category tab пока не относится к spec и остаётся упрощённой |

## Следующий рабочий фокус

1. Довести `admin/users` и `verification` до полной pagination/filter поддержки из spec.
2. При необходимости сделать отдельный UI для `generated documents` и `document types`.
3. Провести ручной smoke-test по ключевым ролям и основным переходам.
4. При необходимости добавить toasts/единый error UX вместо `console.error` и `alert`.

