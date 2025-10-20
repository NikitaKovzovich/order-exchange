<img width="3212" height="1116" alt="уведомления" src="https://github.com/user-attachments/assets/556ca450-992a-4313-8516-0b92abcbeb99" /># **Программное средство автоматизации обмена заказами между торговыми сетями и поставщиками**

---

### Краткое описание проекта

**OrderFlow** – это платформа для автоматизации обмена заказами между торговыми сетями и поставщиками. 
Её задача — превратить процесс, который ранее сопровождался ошибками, дублированием данных и устаревшими справочниками, 
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
4. [Пользовательский интерфейс](#Пользовательский_интерфейс)
	1. [Примеры экранов UI](#Примеры_экранов_UI)
6. [Детали реализации](#Детали_реализации)
	1. [UML-диаграммы](#UML-диаграммы)
	2. [Спецификация API](#Спецификация_API)
	3. [Безопасность](#Безопасность)
	4. [Оценка качества кода](#Оценка_качества_кода)
7. [Тестирование](#Тестирование)
	1. [Unit-тесты](#Unit-тесты)
	2. [Интеграционные тесты](#Интеграционные_тесты)
8. [Установка и  запуск](#installation)
	1. [Манифесты для сборки docker образов](#Манифесты_для_сборки_docker_образов)
	2. [Манифесты для развертывания k8s кластера](#Манифесты_для_развертывания_k8s_кластера)
9. [Лицензия](#Лицензия)
10. [Контакты](#Контакты)

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

Описание отношений и структур данных, используемых в ПС. Также представить скрипт (программный код), который необходим для генерации БД

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

Описать подходы, использованные для обеспечения безопасности, включая описание процессов аутентификации и авторизации с примерами кода из репозитория сервера

### Оценка качества кода

Используя показатели качества и метрики кода, оценить его качество

---

## **Тестирование**

### Unit-тесты

Представить код тестов для пяти методов и его пояснение

### Интеграционные тесты

Представить код тестов и его пояснение

---

## **Установка и  запуск**

### Манифесты для сборки docker образов

Представить весь код манифестов или ссылки на файлы с ними (при необходимости снабдить комментариями)

### Манифесты для развертывания k8s кластера

Представить весь код манифестов или ссылки на файлы с ними (при необходимости снабдить комментариями)

---

## **Лицензия**

Этот проект лицензирован по лицензии MIT - подробности представлены в файле [LICENSE.md]

---

## **Контакты**

Автор: nkovzovic231104@gmail.com
