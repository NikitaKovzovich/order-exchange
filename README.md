
# **Программное средство автоматизации обмена заказами между торговыми сетями и поставщиками**

---

### Краткое описание проекта

**OrderFlow** – это платформа для автоматизации обмена заказами между торговыми сетями и поставщиками.
Её задача – превратить процесс, который ранее сопровождался ошибками, дублированием данных и устаревшими справочниками,
в прозрачный и надёжный механизм, где каждое действие контролируется и синхронизируется в реальном времени.

Система создавалась с опорой на современные архитектурные принципы: микросервисы обеспечивают модульность и масштабируемость,
**Clean Architecture** отделяет бизнес-логику от технической реализации,
а **Domain-Driven Design** позволяет строить решение вокруг реальных бизнес-понятий.
Использование **CQRS** и событийного подхода делает взаимодействие гибким и предсказуемым, а средства наблюдаемости гарантируют,
что процессы остаются управляемыми и прозрачными.

В результате **Order Exchange** – это не просто набор сервисов, а целостная экосистема,
где объединяются безопасность, автоматизация и аналитика, чтобы бизнес-процесс заказов стал устойчивым, точным и эффективным.

### Цели проекта

- Повысить качество и скорость обработки заказов;
- Минимизировать влияние человеческого фактора и снизить количество ошибок;
- Обеспечить прозрачное взаимодействие между всеми участниками процесса;
- Создать централизованный и актуальный источник данных о заказах, товарах и контрагентах;
- Внедрить удобный интерфейс и единые стандарты работы с заказами и документами.

### Основные возможности

- Централизованная база контрагентов с валидацией и актуализацией данных;
- Единый каталог товаров с проверкой артикулов и синхронизацией цен;
- Автоматическая генерация документов и хранение в системе;
- Стандартизированные формы заказов с корректной обработкой адресов и условий доставки;
- Встроенный чат для оперативного взаимодействия между поставщиками и торговыми сетями;
- Аналитический модуль для мониторинга показателей и формирования отчётов.

---

## **Содержание**

1. [Архитектура](#Архитектура)
    1. [C4-модель](#C4-модель)
    2. [Схема данных](#Схема_данных)
2. [Функциональные возможности](#Функциональные_возможности)
    1. [Диаграмма вариантов использования](#Диаграмма_вариантов_использования)
    2. [User-flow диаграммы](#User-flow_диаграммы)
3. [Пользовательский интерфейс](#Пользовательский_интерфейс)
    1. [Примеры экранов UI](#Примеры_экранов_UI)
4. [Детали реализации](#Детали_реализации)
    1. [UML-диаграммы](#UML-диаграммы)
    2. [Спецификация API](#Спецификация_API)
    3. [Безопасность](#Безопасность)
    4. [Оценка качества кода](#Оценка_качества_кода)
5. [Тестирование](#Тестирование)
    1. [Unit-тесты](#Unit-тесты)
    2. [Интеграционные тесты](#Интеграционные_тесты)
6. [Установка и запуск](#Установка-и-запуск)
    1. [Требования к окружению](#Требования-к-окружению)
    2. [Вариант 1: Развертывание через Docker Compose](#Вариант-1-Развертывание-через-Docker-Compose)
    3. [Вариант 2: Развертывание в Kubernetes](#Вариант-2-Развертывание-в-Kubernetes)
    4. [Манифесты для сборки Docker образов](#Манифесты-для-сборки-Docker-образов)
    5. [Docker Compose манифест](#Docker-Compose-манифест)
    6. [Манифесты для развертывания Kubernetes кластера](#Манифесты-для-развертывания-Kubernetes-кластера)
7. [Лицензия](#Лицензия)
8. [Контакты](#Контакты)

---
## **Архитектура**

### C4-модель

Система спроектирована как набор микросервисов с разделением по бизнес-доменам.
Взаимодействие внешнего интерфейса и сервисов происходит синхронно через **API Gateway** (**REST/HTTP, JSON, JWT**)
и асинхронно – через **Message Broker** (событийная шина для **CQRS/ES** и обмена событиями).
Система использует принципы **Clean Architecture, DDD и CQRS**, применяет **JSON** как основной формат обмена, аутентификацию на основе **JWT**,
и покрыта средствами наблюдаемости (**OpenTelemetry**).

#### Контейнерный уровень

<img width="10524" height="7524" alt="image" src="https://github.com/user-attachments/assets/22d395e0-1634-4a36-90ac-6932de413619" />

Архитектура **Order Exchange** построена по микросервисному принципу и разделена на несколько контейнеров, каждый из которых отвечает за свой бизнес-домен.
Пользователи – администратор, торговая сеть и поставщик — работают через веб-приложение (**Angular + TypeScript**),
которое взаимодействует с серверной частью по защищённому протоколу **HTTPS** с использованием **JWT**.

Центральным узлом является **API Gateway**, обеспечивающий маршрутизацию и проверку прав доступа.
За ключевые процессы отвечают отдельные сервисы:
- `Auth Service` управляет аутентификацией и авторизацией;
- `Order Service` обрабатывает заказы и управляет их жизненным циклом;
- `Catalog Service` отвечает за товары и каталоги;
- `Document Service` формирует и хранит документы;
- `Chat Service` обеспечивает обмен сообщениями в реальном времени;
- `Analytics Service` агрегирует данные и формирует отчёты.

Для асинхронного обмена событиями используется **RabbitMQ**, что снижает связанность между сервисами и повышает надёжность.
Данные каждого сервиса хранятся в отдельной базе, а наблюдаемость обеспечивается через **OpenTelemetry**.

#### Компонентный уровень

<img width="16384" height="8744" alt="image" src="https://github.com/user-attachments/assets/c0c53526-d7ba-4a59-8ada-3ca17cc39713" />

На компонентном уровне каждая подсистема **Order Exchange** разделена на контроллеры, сервисы, доменные модели и интеграционные компоненты.  
Такой подход отражает принципы **DDD** и **Clean Architecture**, где бизнес-логика изолирована от инфраструктуры, а все взаимодействия между модулями происходят через чётко определённые интерфейсы.

- **Контроллеры** – принимают внешние запросы и передают их в слой приложений  
  `AuthController`, `OrderController`, `CatalogController`, `DocumentController`, `ChatController`

- **Сервисы** – реализуют основную бизнес-логику: обработку заказов, управление каталогами, генерацию документов, аналитику и чат  
  *Примеры:* `AuthService`, `OrderCommandHandler`, `CatalogCommandHandler`, `DocumentService`, `ChatService`, `AnalyticsService`

- **Доменные модели (Aggregates)** – инкапсулируют правила предметной области и обеспечивают целостность данных  
  `OrderAggregate`, `ProductAggregate`, `InventoryAggregate`

- **Репозитории** – обеспечивают доступ к данным и скрывают детали работы с базами  
  `UserRepository`, `OrderRepository`, `CatalogRepository`, `AnalyticsRepository`

- **Интеграционные компоненты** – связывают сервисы через брокер сообщений и внешние системы  
  `EventPublisher`, `EventsListener`, `ObjectStorageClient`, `SupplierIntegration`

- **Инфраструктурные элементы** – поддерживают вспомогательные процессы, не затрагивая бизнес-логику  
  `PDFGenerator`, `TemplateEngine`, `TracingAdapter`, `SecurityConfig`


### Схема данных

#### **user_db**
<img width="874" height="732" alt="user_db" src="https://github.com/user-attachments/assets/0090c272-ca78-4280-b63a-9378239912e0" />

#### **catalog_db**
<img width="734" height="673" alt="catalog_db" src="https://github.com/user-attachments/assets/8baad1c2-7542-4a20-8fb7-78660c3ae508" />

#### **order_db**
<img width="649" height="527" alt="order_db" src="https://github.com/user-attachments/assets/b4325af1-443c-4a0d-ba05-f8e3aba43e1c" />

#### **chat_db**
<img width="413" height="420" alt="chat_db" src="https://github.com/user-attachments/assets/a3a517fa-4093-4758-a101-349d5c77f56c" />

### UML-диаграммы

#### **Диаграмма развертывания**
<img width="1408" height="1411" alt="Диаграмма развертывания" src="https://github.com/user-attachments/assets/e5eb436a-e848-4366-85a9-64565019f55f" />

#### **Диаграмма пакетов "order-service"**
<img width="741" height="601" alt="Диаграмма пакетов" src="https://github.com/user-attachments/assets/fc9a8252-468c-440d-a713-824fe0ff3954" />

#### **Диаграмма вариантов использования**
<img width="1070" height="911" alt="Диаграмма_вариантов_использования" src="https://github.com/user-attachments/assets/a52fdf10-c1c1-4bae-adca-aecc41b491e2" />

#### **Диаграмма состояний "Заказ"**
<img width="593" height="1124" alt="Диаграмма состояний" src="https://github.com/user-attachments/assets/10f7aa80-7cb9-4da1-8747-963dd19ce457" />

#### **Диаграмма последовательности "Создание заказа"**
<img width="1242" height="861" alt="Диаграмма_последовательности" src="https://github.com/user-attachments/assets/e2ea1e39-521e-4c65-863a-0b7fc5edb500" />

#### **Диаграмма деятельности "Приемка товара"**
<img width="574" height="681" alt="Диаграмма_деятельностиактивности" src="https://github.com/user-attachments/assets/4e8463c3-a74c-4b5e-8629-341a86bcd4b8" />

---

## **Функциональные возможности**

### Диаграмма вариантов использования

Диаграмма вариантов использования и ее описание

### User-flow диаграммы

#### **User-flow для роли "Торговая сеть"**
<img width="5284" height="8484" alt="image" src="https://github.com/user-attachments/assets/1f60d282-ca9c-4c56-8b27-2f88de73cb41" />

#### **User-flow для роли "Поставщик"**
<img width="6884" height="8084" alt="image" src="https://github.com/user-attachments/assets/671d2d40-50a0-4087-b76c-35e210425ebf" />

#### **User-flow для роли "Администратор"**
<img width="6888" height="5684" alt="image" src="https://github.com/user-attachments/assets/ca283753-ba89-4396-91f0-bc794db96ad9" />

---

## **Пользовательский интерфейс**

### Примеры экранов UI

#### **Общие страницы**

- главная страница
  <img width="3868" height="4016" alt="главная" src="https://github.com/user-attachments/assets/e00fbbf8-4e35-463b-8581-bd3f5cae128a" />

- авторизация
  <img width="3212" height="2000" alt="авторизация" src="https://github.com/user-attachments/assets/85136ae9-4119-4ba0-95c2-ed9d95a31407" />

#### **Примеры экранов UI для роли "Поставщик"**

- регистрация шаг 1
  <img width="3212" height="1712" alt="регистрация шаг 1" src="https://github.com/user-attachments/assets/6d3f6f46-2be8-48a9-85e7-885f8193e20a" />

- регистрация шаг 2
  <img width="3212" height="2528" alt="регистрация шаг 2" src="https://github.com/user-attachments/assets/f4256b54-3146-4924-8d03-c8ef26febcf0" />

- регистрация шаг 3
  <img width="3212" height="2528" alt="регистрация шаг 3" src="https://github.com/user-attachments/assets/86e29f14-ab9f-4dcc-a53d-304b68f112a3" />

- регистрация шаг 4
  <img width="3212" height="2528" alt="регистрация шаг 4" src="https://github.com/user-attachments/assets/0b258129-23bf-4d84-ac89-261adef86542" />

- регистрация шаг 5
  <img width="3212" height="2528" alt="регистрация шаг 5" src="https://github.com/user-attachments/assets/f45e9b1f-cea9-422d-99e7-e4942245df21" />

- отказ в регистрации
  <img width="3212" height="2000" alt="отказ в регистрации" src="https://github.com/user-attachments/assets/df9c25f5-99e4-45c1-b4c5-db0eb436f3a9" />

- главная панель
  <img width="3212" height="2000" alt="главная панель" src="https://github.com/user-attachments/assets/99f2cfea-701f-44a7-af30-5abe73ae2dbf" />

- создание кааталога
  <img width="3212" height="2000" alt="создание кааталога" src="https://github.com/user-attachments/assets/9b342ee1-3dcd-439a-8f8e-bad4c4f83521" />

- добавление товара шаг 1
  <img width="3212" height="2000" alt="добавление товара шаг 1" src="https://github.com/user-attachments/assets/6ad0f887-7b89-4ff0-b20b-f2fa24b7e71e" />

- добавление товара шаг 2
  <img width="3212" height="2000" alt="добавление товара шаг 2" src="https://github.com/user-attachments/assets/452fcf55-68ef-41f1-9452-e8dfe6ea2439" />

- добавление товара шаг 3
  <img width="3212" height="2000" alt="добавление товара шаг 3" src="https://github.com/user-attachments/assets/97d5041a-ff4e-47b5-b75e-ca15374d1daa" />

- каталог (неопубликованный)
  <img width="3212" height="2000" alt="каталог (неопубликованный)" src="https://github.com/user-attachments/assets/c1ba3902-3df4-4fbf-bff9-d0a7f974fa98" />

- каталог (с черновиками)
  <img width="3212" height="2000" alt="каталог (с черновиками)" src="https://github.com/user-attachments/assets/913497b6-2f3b-42ac-8b26-783814631f8d" />

- каталог (опубликован)
  <img width="3212" height="2000" alt="каталог (опубликован)" src="https://github.com/user-attachments/assets/15418421-beea-4780-9178-41d3ee22d1a3" />

- карточка товара
  <img width="3212" height="2000" alt="карточка товара" src="https://github.com/user-attachments/assets/68e3b68d-b07f-4ee2-ad52-c23c3a802897" />

- заказы
  <img width="3212" height="2000" alt="заказы" src="https://github.com/user-attachments/assets/e67d3495-0633-4069-999c-19b13c40d669" />

- заказ (ожидает подтверждения)
  <img width="3212" height="2400" alt="заказ (ожидает подтверждения)" src="https://github.com/user-attachments/assets/3e84aca6-5a4f-40b0-919a-98f8a5e8c745" />

- заказ (ожидает оплаты)
  <img width="3212" height="2400" alt="заказ (ожидает оплаты)" src="https://github.com/user-attachments/assets/4f2fcb07-df48-4c14-ae56-dc32d1baebea" />

- заказ (ожидает проверки оплаты)
  <img width="3212" height="2548" alt="заказ (ожидает проверки оплаты)" src="https://github.com/user-attachments/assets/c6c71ed5-60f4-42e7-b0c5-3fe87606f4c4" />

- заказ (оплачен)
  <img width="3212" height="2548" alt="заказ (оплачен)" src="https://github.com/user-attachments/assets/70c08352-48c4-405f-9a1e-56c3ec8b59bc" />

- заказ (ожидает отгрузки)
  <img width="3212" height="2644" alt="заказ (ожидает отгрузки)" src="https://github.com/user-attachments/assets/e468ed79-1f67-4620-a88e-ee192bd7dbe5" />

- заказ (в пути)
  <img width="3212" height="2644" alt="заказ (в пути)" src="https://github.com/user-attachments/assets/16d7c6ba-f478-4e1a-8a60-88422da8ec5f" />

- заказ (доставлен)
  <img width="3212" height="2644" alt="заказ (доставлен)" src="https://github.com/user-attachments/assets/499cd3ae-8ce0-4394-bafb-aa3253b8fef0" />

- уведомления
  <img width="3212" height="1508" alt="уведомления" src="https://github.com/user-attachments/assets/fc3e2356-aa79-4b75-9eb8-ee9a93a91f54" />

- коммуникации
  <img width="3212" height="1508" alt="коммуникации" src="https://github.com/user-attachments/assets/b3521a99-5fb8-4743-882e-1aaf1c6ee230" />

- поддержка
  <img width="3212" height="1508" alt="поддержка" src="https://github.com/user-attachments/assets/da2c8b1c-81fb-4c5a-ac71-18b82a185cf9" />

- создание обращения в поддержку
  <img width="3212" height="1508" alt="создание обращения в поддержку" src="https://github.com/user-attachments/assets/e25517d0-867d-4067-b6ea-e1dc3af8befc" />

- аналитика
  <img width="3212" height="2752" alt="аналитика" src="https://github.com/user-attachments/assets/ae9eb26b-6311-4fa9-b9d5-d33e64a4441d" />

#### **Примеры экранов UI для роли "Торговая сеть"**

- регистрация шаг 1
  <img width="3212" height="2216" alt="регистрация шаг 1" src="https://github.com/user-attachments/assets/e23d67e2-e339-4d80-b293-6c71313c58b2" />

-регистрация шаг 2
<img width="3212" height="2216" alt="регистрация шаг 2" src="https://github.com/user-attachments/assets/8eb92203-84d9-4cb8-a1c5-bce4601a575a" />

- регистрация шаг 3
  <img width="3212" height="2216" alt="регистрация шаг 3" src="https://github.com/user-attachments/assets/221f9089-0335-41c5-997d-f27ee522eea8" />

- регистрация шаг 4
  <img width="3212" height="2216" alt="регистрация шаг 4" src="https://github.com/user-attachments/assets/982e8432-e4e9-4818-ab98-ada3b14c982e" />

- регистрация шаг 5
  <img width="3212" height="2216" alt="регистрация шаг 5" src="https://github.com/user-attachments/assets/89c1185d-5b70-41c2-af32-ade4a9c8bf5c" />

- отказ в регистрации
  <img width="3212" height="2000" alt="отказ в регистрации" src="https://github.com/user-attachments/assets/5e3ad0fb-ba5c-4bc7-a3e0-e3b0e95c842b" />

- главная панель
  <img width="1919" height="940" alt="главная панель" src="https://github.com/user-attachments/assets/b4e0c1d2-a6ad-46e1-9b34-8228aabb5228" />

- каталог товаров
  <img width="3212" height="1692" alt="каталог товаров" src="https://github.com/user-attachments/assets/243a4197-f308-4e49-be07-f36ea5e045fe" />

- база поставщиков
  <img width="3212" height="1692" alt="база поставщиков" src="https://github.com/user-attachments/assets/51d0e98f-4fbc-4019-ad9d-f5c9aa5e67e2" />

- каталог поставщика
  <img width="3212" height="1692" alt="каталог поставщика" src="https://github.com/user-attachments/assets/173bd374-e147-4d76-a8a3-e3f21940ca93" />

- формирование заказа
  <img width="3212" height="2164" alt="формирование заказа" src="https://github.com/user-attachments/assets/fbe39bca-7454-4015-8a3e-afb9791742b1" />

- заказы
  <img width="3212" height="2164" alt="заказы" src="https://github.com/user-attachments/assets/6cdc070c-2a05-425a-9d1a-8feadfe38bac" />

- закказ (ожидает подтверждения)
  <img width="3212" height="2260" alt="закказ (ожидает подтверждения)" src="https://github.com/user-attachments/assets/0b3d37bc-57a3-4714-925e-c46f28759579" />

- заказ (отклонен)
  <img width="3212" height="1848" alt="заказ (отклонен)" src="https://github.com/user-attachments/assets/c1060d71-cae4-4b5b-97d5-f3189d361dfb" />

- заказ (ожидает подтверждение оплаты)
  <img width="3212" height="2288" alt="заказ (ожидает подтверждение оплаты)" src="https://github.com/user-attachments/assets/9de875f6-4232-40b7-a3a9-b9871aba86dd" />

- заказ (оплачен)
  <img width="3212" height="2288" alt="заказ (оплачен)" src="https://github.com/user-attachments/assets/cae087f0-f796-44ff-9e9c-2ec0db587712" />

- заказ (проблемы с оплатой)
  <img width="3212" height="2412" alt="заказ (проблемы с оплатой)" src="https://github.com/user-attachments/assets/c9064edb-8aba-419c-b9f3-9cce16866f7f" />

- заказ (ожидает отгрузки)
  <img width="3212" height="2412" alt="заказ (ожидает отгрузки)" src="https://github.com/user-attachments/assets/af32c214-e11d-4494-9cb4-162b0f257be0" />

- заказ (в пути)
  <img width="3212" height="2412" alt="заказ (в пути)" src="https://github.com/user-attachments/assets/7c330be9-1bb1-4193-b5d2-f978faacb1c2" />

- заказ (доставлен)
  <img width="3212" height="2412" alt="заказ (доставлен)" src="https://github.com/user-attachments/assets/ca609526-6516-44a5-963c-44848ce71ad5" />

- заказ (закрыт)
  <img width="3212" height="2412" alt="заказ (закрыт)" src="https://github.com/user-attachments/assets/d0500aa8-892a-4d15-b022-7aab971ec7f6" />

- уведомления
  <img width="3212" height="2412" alt="уведомления" src="https://github.com/user-attachments/assets/acbde121-bc0e-447b-a386-cd71452da395" />

- коммуникации
  <img width="3212" height="2412" alt="коммуникации" src="https://github.com/user-attachments/assets/0bf1613a-7504-46a8-9d80-c3bd3568ef1f" />

- поддержка
  <img width="3212" height="1196" alt="поддержка" src="https://github.com/user-attachments/assets/fba00139-8e6f-4b0c-9415-f166cf60b4dc" />

- создание обращения в поддержку
  <img width="3212" height="1764" alt="создание обращения в поддержку" src="https://github.com/user-attachments/assets/f23698fa-e9c0-46ec-8379-6ae3e45fdf53" />

- аналитика
  <img width="3212" height="2684" alt="аналитика" src="https://github.com/user-attachments/assets/4e40afdb-3660-4096-ba51-a32386aace60" />

- журнал приемки
  <img width="3212" height="1892" alt="журнал приемки" src="https://github.com/user-attachments/assets/3a0fd7cc-25e4-4e25-aff1-0ee4f8be27a0" />

#### **Примеры экранов UI для роли "Администратор"**

- главная панель
  <img width="3212" height="2312" alt="главная панель" src="https://github.com/user-attachments/assets/e384647b-6ac4-4abb-a0e8-a7bcb522f0bc" />

- заявки на верификацию
  <img width="3212" height="1216" alt="заявки на верификацию" src="https://github.com/user-attachments/assets/9ac4fac9-5d84-4fd9-a3ff-af11648c17a0" />

- заявка на верификацию
  <img width="3212" height="2392" alt="заявка на верификацию" src="https://github.com/user-attachments/assets/54d2c675-3f11-44ac-aa61-0e1fe9aa5c38" />

- управление пользователями
  <img width="3212" height="1220" alt="управление пользователями" src="https://github.com/user-attachments/assets/025d8135-8f7f-4d37-a1b1-177b8110338a" />

- управление пользователем
  <img width="3212" height="1236" alt="управление пользователем" src="https://github.com/user-attachments/assets/08d5efb2-db29-450e-a966-51cee2323970" />

- управление товарами
  <img width="3212" height="1236" alt="управление товарами" src="https://github.com/user-attachments/assets/222f7d3d-f43a-4bd3-9d27-936e4b7a4ab6" />

- управление категориями
  <img width="3212" height="1236" alt="управление категориями" src="https://github.com/user-attachments/assets/36a523da-167e-42c9-8702-31c7cc5121d1" />

- управление ед. измерения
  <img width="3212" height="1236" alt="управление ед  измерения" src="https://github.com/user-attachments/assets/8b10ecdf-abee-45f9-b5f7-d71e192a03a7" />

- управление ставками НДС
  <img width="3212" height="1236" alt="управление ставками НДС" src="https://github.com/user-attachments/assets/d69d815c-3a83-4e1e-ad97-6afbdd0ff3b5" />

- аналитика
  <img width="3212" height="2744" alt="аналитика" src="https://github.com/user-attachments/assets/2edf614a-7fb6-4221-9828-709a4d845735" />

- служба поддержки
  <img width="3212" height="1116" alt="служба поддержки" src="https://github.com/user-attachments/assets/b4001e99-0a52-4de7-aa69-72a94e6c026c" />

- обращение в службу поддержки
  <img width="3212" height="1116" alt="обращение в службу поддержки" src="https://github.com/user-attachments/assets/5937f3bd-d6db-4e38-a1df-1a5b39597e6d" />

- уведомления
  <img width="3212" height="1116" alt="уведомления" src="https://github.com/user-attachments/assets/0e8c144a-2f6a-4fe6-8a4a-4829a9861063" />

---

## **Детали реализации**

### UML-диаграммы

Представить все UML-диаграммы , которые позволят более точно понять структуру и детали реализации ПС

### Спецификация API

Представить описание реализованных функциональных возможностей ПС с использованием Open API (можно представить либо полный файл спецификации, либо ссылку на него)

### Безопасность

#### Архитектура безопасности

##### 1. API Gateway - первая линия защиты
API Gateway выступает единой точкой входа и выполняет первичную валидацию JWT-токенов перед маршрутизацией запросов к микросервисам.

##### 2. Auth Service - управление пользователями
Микросервис аутентификации отвечает за регистрацию, вход пользователей и генерацию JWT-токенов.

#### Аутентификация

##### JWT (JSON Web Tokens)

Система использует JWT для stateless-аутентификации. Токены подписываются с использованием алгоритма **HS256** и секретного ключа длиной 256 бит.

###### Структура токена

```java
// Генерация JWT-токена
public String generateToken(String email, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    return createToken(claims, email);
}

private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationTime);
    
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
    
    return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
}
```

**Параметры JWT:**
- **Алгоритм**: HS256 (HMAC with SHA-256)
- **Срок действия**: 3600000 мс (1 час)
- **Payload**: email (subject), role (claims)

###### Валидация токена

```java
public boolean validateToken(String token) {
    try {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```

##### Процесс аутентификации

###### 1. Регистрация (`POST /api/auth/register`)

Регистрация - это критически важный процесс, который включает несколько этапов безопасности:

**Этапы регистрации:**
1. Валидация входных данных (email, пароль, данные компании)
2. Проверка уникальности email в системе
3. Хеширование пароля с использованием BCrypt
4. Создание пользователя и связанной компании
5. Автоматическая генерация JWT-токена
6. Публикация событий для аудита

**Пример кода регистрации:**

```java
@Transactional
public String register(RegisterRequest request) {
    // Проверка уникальности email
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("User with this email already exists");
    }

    // Определение роли пользователя
    User.Role userRole = User.Role.valueOf(request.getType().toUpperCase());

    // Создание пользователя с хешированным паролем
    User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(userRole)
            .isActive(userRole == User.Role.ADMIN)  // Админы активны сразу
            .createdAt(LocalDateTime.now())
            .build();

    // Создание компании
    Company company = Company.builder()
            .name(request.getName())
            .legalForm(Company.LegalForm.valueOf(request.getLegalForm()))
            .taxId(request.getTaxId())
            .contactPhone(request.getContactPhone())
            .status(Company.CompanyStatus.PENDING_VERIFICATION)
            .build();
    
    company = companyRepository.save(company);
    user.setCompany(company);
    user = userRepository.save(user);

    // Публикация событий для аудита
    eventPublisher.publish("User", user.getId().toString(), "UserRegistered",
        Map.of(
            "userId", user.getId(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "companyId", company.getId()
        ));

    // Генерация JWT-токена
    return jwtProvider.generateToken(user.getEmail(), user.getRole().name());
}
```

**Особенности безопасности при регистрации:**
- Пароль никогда не хранится в открытом виде
- Email проверяется на уникальность до создания пользователя
- Новые пользователи (кроме ADMIN) требуют верификации компании
- Транзакционность гарантирует целостность данных
- События регистрации логируются для аудита

###### 2. Вход в систему (`POST /api/auth/login`)

Процесс входа включает проверку учетных данных и выдачу JWT-токена:

**Этапы входа:**
1. Получение email и пароля
2. Поиск пользователя в базе данных
3. Проверка хеша пароля с использованием BCrypt
4. Проверка активности аккаунта
5. Генерация JWT-токена при успешной аутентификации
6. Возврат токена клиенту

```java
public String login(LoginRequest request) {
    // Поиск пользователя по email
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    // Проверка пароля
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        throw new IllegalArgumentException("Invalid credentials");
    }

    // Проверка активности аккаунта
    if (!user.getIsActive()) {
        throw new IllegalArgumentException("Account is not activated");
    }

    // Генерация и возврат JWT-токена
    return jwtProvider.generateToken(user.getEmail(), user.getRole().name());
}
```

**Меры безопасности при входе:**
- Одинаковое сообщение об ошибке для несуществующего email и неверного пароля (защита от перебора email)
- Проверка активности аккаунта перед выдачей токена
- BCrypt автоматически сравнивает хеши с учетом соли
- Рекомендуется добавить rate limiting для защиты от brute force атак

#### Авторизация

##### Роли пользователей

Система поддерживает три основные роли:
- **ADMIN** - администратор системы
- **SUPPLIER** - поставщик
- **RETAIL_CHAIN** - розничная сеть

##### Контроль доступа на уровне API Gateway

API Gateway проверяет JWT-токен для всех защищённых маршрутов:

```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    
    // Публичные пути без аутентификации
    if (isPublicPath(path)) {
        return chain.filter(exchange);
    }
    
    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    
    String token = authHeader.substring(7);
    
    if (!jwtProvider.validateToken(token)) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    
    // Извлечение данных пользователя и добавление в заголовки
    String email = jwtProvider.getEmailFromToken(token);
    String role = jwtProvider.getRoleFromToken(token);
    
    ServerWebExchange mutatedExchange = exchange.mutate()
            .request(exchange.getRequest().mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build())
            .build();
    
    return chain.filter(mutatedExchange);
}
```

**Публичные эндпоинты** (без аутентификации):
- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/validate`
- `/api/auth/company/**`
- `/api/addresses/company/**`

##### Контроль доступа на уровне микросервисов

Auth Service дополнительно использует Spring Security для защиты эндпоинтов:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("GET", "/api/addresses/**").permitAll()
            .requestMatchers("/api/addresses/**").authenticated()
            .requestMatchers("/api/admin/**").authenticated()
            .requestMatchers("/api/verification/**").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(jwtAuthenticationFilter(), 
            UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

##### JWT Authentication Filter

Фильтр извлекает и валидирует JWT-токен, устанавливая контекст безопасности Spring Security:

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) 
        throws ServletException, IOException {
    
    String authHeader = request.getHeader("Authorization");
    
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        
        if (jwtProvider.validateToken(token)) {
            String email = jwtProvider.getEmailFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);
            
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
                );
            
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
    
    filterChain.doFilter(request, response);
}
```

#### Защита данных

##### Хеширование паролей

Пароли хешируются с использованием **BCrypt** - адаптивной криптографической функции с поддержкой соли:

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// Использование при регистрации
User user = User.builder()
        .email(request.getEmail())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .role(userRole)
        .build();
```

**Преимущества BCrypt:**
- Автоматическая генерация соли
- Защита от rainbow table атак
- Адаптивная стоимость вычислений

##### База данных

- **MySQL 8.0** с диалектом Hibernate
- Безопасное хранение учетных данных БД в `application.properties`
- Рекомендуется использовать переменные окружения для production

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/user_db
spring.datasource.username=app_user
spring.datasource.password=app_password
```

#### CORS (Cross-Origin Resource Sharing)

Настроена политика CORS для безопасного взаимодействия с фронтенд-приложениями:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:4200", 
        "http://localhost:3000", 
        "http://localhost:4201"
    ));
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
    ));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Оценка качества кода

Используя показатели качества и метрики кода, оценить его качество

---

## **Тестирование**

### Unit-тесты

Представить код тестов для пяти методов и его пояснение

### Интеграционные тесты

Представить код тестов и его пояснение

---

## **Установка и запуск**

### Требования к окружению

**Минимальные требования:**
- **ОС**: Windows 10/11, Linux, macOS
- **RAM**: 8 GB (рекомендуется 16 GB)
- **Дисковое пространство**: 20 GB

**Необходимое ПО:**
- **Docker Desktop** 4.x или выше ([скачать](https://www.docker.com/products/docker-desktop))
- **Docker Compose** v2.x (включен в Docker Compose)
- **kubectl** (для Kubernetes, включен в Docker Desktop)
- **PowerShell** 5.1+ (для Windows)

---

### Вариант 1: Развертывание через Docker Compose

**Рекомендуется для разработки и тестирования**

#### Быстрый старт

```powershell
# 1. Запустите Docker Desktop

# 2. Перейдите в директорию проекта
cd C:\учеба\OrderFlow

# 3. Запустите автоматизированный скрипт
.\deploy-docker.ps1 -Action up

# 4. Дождитесь завершения (3-5 минут)
```

#### Команды управления

```powershell
# Просмотр статуса сервисов
.\deploy-docker.ps1 -Action status

# Просмотр логов в реальном времени
.\deploy-docker.ps1 -Action logs

# Остановка всех сервисов
.\deploy-docker.ps1 -Action down

# Перезапуск сервисов
.\deploy-docker.ps1 -Action restart

# Пересборка Docker образов
.\deploy-docker.ps1 -Action build

# Полная очистка (удаление контейнеров, volumes, образов)
.\deploy-docker.ps1 -Action clean
```

#### Скрипт deploy-docker.ps1

**Расположение:** `deploy-docker.ps1` (корень проекта)

**Основные функции:**

```powershell
# Проверка наличия Docker
function Test-Docker {
    docker --version
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker не найден или не запущен"
        exit 1
    }
}

# Запуск всех сервисов
function Start-Services {
    Create-PrometheusConfig
    docker-compose -f "$ProjectRoot\docker-compose.yaml" up -d --build
    Show-AccessInfo
}

# Остановка сервисов
function Stop-Services {
    docker-compose -f "$ProjectRoot\docker-compose.yaml" down
}
```

**Полный код скрипта:** [deploy-docker.ps1](./deploy-docker.ps1)

---

### Вариант 2: Развертывание в Kubernetes

**Рекомендуется для production**

#### Быстрый старт

```powershell
# 1. Включите Kubernetes в Docker Desktop
# Settings → Kubernetes → Enable Kubernetes → Apply & Restart

# 2. Проверьте доступность кластера
kubectl cluster-info
kubectl get nodes

# 3. Запустите автоматизированный скрипт
.\deploy-k8s-cluster.ps1 -Action all

# 4. Дождитесь завершения (5-10 минут)
```

**Доступ к RabbitMQ Management:**
```cmd
kubectl port-forward -n order-exchange svc/rabbitmq-service 15672:15672
```
Затем откройте: http://localhost:15672

#### Команды управления

```powershell
# Полное развертывание (проверка + сборка + деплой)
.\deploy-k8s-cluster.ps1 -Action all

# Только настройка нод кластера
.\deploy-k8s-cluster.ps1 -Action setup

# Только сборка Docker образов
.\deploy-k8s-cluster.ps1 -Action build

# Только развертывание (без сборки образов)
.\deploy-k8s-cluster.ps1 -Action deploy -SkipBuild

# Просмотр статуса
.\deploy-k8s-cluster.ps1 -Action status

# Полная очистка
.\deploy-k8s-cluster.ps1 -Action clean
```

#### Скрипт deploy-k8s-cluster.ps1

**Расположение:** `deploy-k8s-cluster.ps1` (корень проекта)

**Основные функции:**

```powershell
# Проверка предварительных требований
function Test-Prerequisites {
    docker --version
    kubectl version
    kubectl cluster-info
}

# Сборка Docker образов
function Build-DockerImages {
    $services = @(
        "eureka-server", "api-gateway", "auth-service",
        "catalog-service", "order-service", "chat-service",
        "document-service", "frontend"
    )
    foreach ($service in $services) {
        docker build -t "order-exchange/$service:latest" $contextPath
    }
}

# Развертывание в Kubernetes
function Deploy-ToKubernetes {
    kubectl apply -f k8s\00-namespace.yaml
    kubectl apply -f k8s\01-configmaps-secrets.yaml
    kubectl apply -f k8s\02-database.yaml
    kubectl apply -f k8s\03-services-layer.yaml
    kubectl apply -f k8s\04-backend-services.yaml
    kubectl apply -f k8s\05-frontend.yaml
}
```

**Полный код скрипта:** [deploy-k8s-cluster.ps1](./deploy-k8s-cluster.ps1)

#### Альтернативный метод (ручное развертывание)

```cmd
# Применение манифестов по порядку
kubectl apply -f k8s\00-namespace.yaml
kubectl apply -f k8s\01-configmaps-secrets.yaml
kubectl apply -f k8s\02-database.yaml
kubectl apply -f k8s\03-services-layer.yaml
kubectl apply -f k8s\04-backend-services.yaml
kubectl apply -f k8s\05-frontend.yaml

# Проверка статуса
kubectl get pods -n order-exchange
kubectl get services -n order-exchange
```

---

### Манифесты для сборки Docker образов

#### Backend микросервисы (Spring Boot)

**Расположение:** `backendOrderFlow/{service-name}/Dockerfile`

**Применяется для:** eureka-server, api-gateway, auth-service, catalog-service, order-service, chat-service, document-service

```dockerfile
# Этап сборки
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src
RUN gradle build --no-daemon -x test

# Этап runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Файлы:**
- [eureka-server/Dockerfile](./backendOrderFlow/eureka-server/Dockerfile)
- [api-gateway/Dockerfile](./backendOrderFlow/api-gateway/Dockerfile)
- [auth-service/Dockerfile](./backendOrderFlow/auth-service/Dockerfile)
- [catalog-service/Dockerfile](./backendOrderFlow/catalog-service/Dockerfile)
- [order-service/Dockerfile](./backendOrderFlow/order-service/Dockerfile)
- [chat-service/Dockerfile](./backendOrderFlow/chat-service/Dockerfile)
- [document-service/Dockerfile](./backendOrderFlow/document-service/Dockerfile)

#### Frontend (Angular)

**Расположение:** `clientOrderFlow/Dockerfile`

```dockerfile
# Этап сборки
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Этап production
FROM nginx:alpine
COPY --from=build /app/dist/client-order-flow/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Файл:** [clientOrderFlow/Dockerfile](./clientOrderFlow/Dockerfile)

---

### Docker Compose манифест

**Расположение:** `docker-compose.yaml`

**Структура:**
- 4 базы данных MySQL (user-db, catalog-db, order-db, chat-db)
- RabbitMQ с management плагином
- Eureka Server (Service Discovery)
- API Gateway
- 6 микросервисов backend
- Frontend (Angular + Nginx)
- Prometheus + Grafana для мониторинга

**Ключевые особенности:**
- Единая сеть `order-exchange-network`
- Persistent volumes для данных
- Health checks для всех сервисов
- Зависимости между сервисами (depends_on)

**Пример конфигурации сервиса:**

```yaml
auth-service:
  build:
    context: ./backendOrderFlow/auth-service
    dockerfile: Dockerfile
  ports:
    - "8081:8081"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:mysql://user-db:3306/user_db
    - SPRING_RABBITMQ_HOST=rabbitmq
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  depends_on:
    - eureka-server
    - user-db
    - rabbitmq
  networks:
    - order-exchange-network
```

**Полный файл:** [docker-compose.yaml](./docker-compose.yaml)

---

### Манифесты для развертывания Kubernetes кластера

**Расположение:** `k8s/` (директория в корне проекта)

#### Структура манифестов

```
k8s/
├── 00-namespace.yaml              # Namespace для проекта
├── 01-configmaps-secrets.yaml     # Конфигурация и секреты
├── 02-database.yaml               # MySQL StatefulSets
├── 03-services-layer.yaml         # RabbitMQ, Prometheus, Grafana
├── 04-backend-services.yaml       # Микросервисы backend
└── 05-frontend.yaml               # Frontend + NodePort Services
```

#### 1. Namespace (00-namespace.yaml)

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: order-exchange
  labels:
    name: order-exchange
```

**Файл:** [k8s/00-namespace.yaml](./k8s/00-namespace.yaml)

#### 2. ConfigMaps и Secrets (01-configmaps-secrets.yaml)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-config
  namespace: order-exchange
data:
  MYSQL_ROOT_PASSWORD: "root"
  MYSQL_USER: "app_user"
  MYSQL_PASSWORD: "app_password"
---
apiVersion: v1
kind: Secret
metadata:
  name: rabbitmq-secret
  namespace: order-exchange
type: Opaque
stringData:
  username: "admin"
  password: "admin123"
```

**Файл:** [k8s/01-configmaps-secrets.yaml](./k8s/01-configmaps-secrets.yaml)

#### 3. Базы данных (02-database.yaml)

**Содержит:**
- 4 StatefulSets для MySQL (user-db, catalog-db, order-db, chat-db)
- PersistentVolumeClaims для каждой БД
- Services (ClusterIP) для доступа

**Пример StatefulSet:**

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: user-db
  namespace: order-exchange
spec:
  serviceName: user-db-service
  replicas: 1
  selector:
    matchLabels:
      app: mysql
      db: user-db
  template:
    metadata:
      labels:
        app: mysql
        db: user-db
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: "root"
        - name: MYSQL_DATABASE
          value: "user_db"
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql
  volumeClaimTemplates:
  - metadata:
      name: mysql-data
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 5Gi
```

**Файл:** [k8s/02-database.yaml](./k8s/02-database.yaml)

#### 4. Сервисный слой (03-services-layer.yaml)

**Содержит:**
- RabbitMQ Deployment + Service
- Prometheus Deployment + Service (NodePort 30090)
- Grafana Deployment + Service (NodePort 30300)

**Файл:** [k8s/03-services-layer.yaml](./k8s/03-services-layer.yaml)

#### 5. Backend сервисы (04-backend-services.yaml)

**Содержит:**
- Eureka Server Deployment + Service (NodePort 30761)
- API Gateway Deployment + Service (NodePort 30800)
- Auth Service Deployment + Service
- Catalog Service Deployment + Service
- Order Service Deployment + Service
- Chat Service Deployment + Service
- Document Service Deployment + Service

**Пример Deployment:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: order-exchange
spec:
  replicas: 1
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: order-exchange/api-gateway:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: "http://eureka-server:8761/eureka/"
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: order-exchange
spec:
  selector:
    app: api-gateway
  ports:
  - port: 8080
    targetPort: 8080
    nodePort: 30800
  type: NodePort
```

**Файл:** [k8s/04-backend-services.yaml](./k8s/04-backend-services.yaml)

#### 6. Frontend (05-frontend.yaml)

**Содержит:**
- Frontend Deployment (Angular + Nginx)
- Frontend Service (NodePort 30080)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: order-exchange
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: order-exchange/frontend:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: frontend
  namespace: order-exchange
spec:
  selector:
    app: frontend
  ports:
  - port: 80
    targetPort: 80
    nodePort: 30080
  type: NodePort
```

**Файл:** [k8s/05-frontend.yaml](./k8s/05-frontend.yaml)

---

### Полная документация по развертыванию

Подробная инструкция с troubleshooting и дополнительными деталями: **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)**

---

## **Лицензия**

Этот проект лицензирован по лицензии MIT - подробности представлены в файле [LICENSE.md]

---

## **Контакты**

Автор: nkovzovic231104@gmail.com
