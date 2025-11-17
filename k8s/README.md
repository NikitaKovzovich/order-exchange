# Order Exchange - Kubernetes Deployment

Инфраструктура для развертывания Order Exchange в Kubernetes

## Архитектура

Система развертывается на 4 узлах:
- **Узел 1 (Frontend)**: Веб-приложение Angular
- **Узел 2 (Backend)**: Микросервисы Spring Boot + API Gateway + Eureka
- **Узел 3 (Database)**: MySQL 8.0
- **Узел 4 (Services)**: RabbitMQ, Prometheus, Grafana, OpenTelemetry

## Предварительные требования

- Docker Desktop с включенным Kubernetes
- kubectl (устанавливается с Docker Desktop)
- PowerShell 5.1+
- Минимум 8GB RAM
- 20GB свободного места на диске

## Быстрый старт

### 1. Подготовка

```powershell
# Убедитесь, что Docker Desktop запущен
# Включите Kubernetes: Settings -> Kubernetes -> Enable Kubernetes

# Проверьте доступность кластера
kubectl cluster-info
```

### 2. Развертывание

```powershell
# Полное развертывание (сборка + деплой)
.\deploy.ps1

# Только сборка образов
.\deploy.ps1 -Action build

# Только развертывание (если образы уже собраны)
.\deploy.ps1 -Action deploy -SkipBuild

# Проверка статуса
.\deploy.ps1 -Action status

# Удаление
.\deploy.ps1 -Action clean
```

## Структура манифестов

```
k8s/
├── 00-namespace.yaml              # Namespace для проекта
├── 01-configmaps-secrets.yaml     # Конфигурация и секреты
├── 02-database.yaml               # MySQL deployment
├── 03-services-layer.yaml         # RabbitMQ, Prometheus, Grafana, OTEL
├── 04-backend-services.yaml       # Все бэкенд микросервисы
└── 05-frontend.yaml               # Frontend + Ingress
```

## Dockerfiles

Каждый сервис имеет multi-stage Dockerfile:
- **Backend**: `gradle:8.5-jdk21-alpine` (build) → `eclipse-temurin:21-jre-alpine` (runtime)
- **Frontend**: `node:20-alpine` (build) → `nginx:alpine` (runtime)

## Доступ к сервисам

После успешного развертывания:

| Сервис | URL | Порт | Учетные данные |
|--------|-----|------|----------------|
| Frontend | http://localhost:30080 | 30080 | - |
| Prometheus | http://localhost:30090 | 30090 | - |
| Grafana | http://localhost:30300 | 30300 | admin/admin |
| RabbitMQ | http://localhost:15672* | 15672 | admin/admin123 |

*Для RabbitMQ используйте port-forward:
```powershell
kubectl port-forward -n order-exchange svc/rabbitmq-service 15672:15672
```

## Мониторинг

### Просмотр логов

```powershell
# Логи конкретного пода
kubectl logs -n order-exchange <pod-name>

# Логи с отслеживанием
kubectl logs -n order-exchange <pod-name> -f

# Логи всех подов сервиса
kubectl logs -n order-exchange -l app=auth-service --all-containers=true
```

### Статус подов

```powershell
# Все поды
kubectl get pods -n order-exchange

# С подробной информацией
kubectl get pods -n order-exchange -o wide

# Описание пода
kubectl describe pod -n order-exchange <pod-name>
```

### Метрики

```powershell
# Использование ресурсов
kubectl top pods -n order-exchange
kubectl top nodes
```

## Масштабирование

```powershell
# Увеличить количество реплик
kubectl scale deployment -n order-exchange auth-service --replicas=3

# Автомасштабирование
kubectl autoscale deployment -n order-exchange auth-service --min=2 --max=5 --cpu-percent=80
```

## Обновление

```powershell
# Пересобрать образ
cd backendOrderFlow\auth-service
docker build -t order-exchange/auth-service:latest .

# Перезапустить deployment
kubectl rollout restart deployment -n order-exchange auth-service

# Проверить статус обновления
kubectl rollout status deployment -n order-exchange auth-service

# Откатить изменения
kubectl rollout undo deployment -n order-exchange auth-service
```

## Настройка узлов

Скрипт автоматически помечает узлы метками. Для ручной настройки:

```powershell
# Просмотр узлов
kubectl get nodes --show-labels

# Назначение роли
kubectl label nodes <node-name> node-role=frontend
kubectl label nodes <node-name> node-role=backend
kubectl label nodes <node-name> node-role=database
kubectl label nodes <node-name> node-role=services
```

## Troubleshooting

### Поды не запускаются

```powershell
# Проверить события
kubectl get events -n order-exchange --sort-by='.lastTimestamp'

# Проверить описание пода
kubectl describe pod -n order-exchange <pod-name>

# Проверить логи
kubectl logs -n order-exchange <pod-name> --previous
```

### Проблемы с образами

```powershell
# Если образ не найден, убедитесь что он собран
docker images | grep order-exchange

# Пересобрать конкретный образ
cd backendOrderFlow\<service-name>
docker build -t order-exchange/<service-name>:latest .
```

### База данных не инициализируется

```powershell
# Проверить логи MySQL
kubectl logs -n order-exchange -l app=mysql

# Подключиться к MySQL
kubectl exec -it -n order-exchange <mysql-pod-name> -- mysql -uroot -p

# Проверить базы данных
kubectl exec -it -n order-exchange <mysql-pod-name> -- mysql -uroot -p -e "SHOW DATABASES;"
```

### Сервисы не видят друг друга

```powershell
# Проверить DNS
kubectl exec -it -n order-exchange <pod-name> -- nslookup mysql-service

# Проверить сервисы
kubectl get svc -n order-exchange

# Проверить endpoints
kubectl get endpoints -n order-exchange
```

## Очистка

```powershell
# Удалить все ресурсы
.\deploy.ps1 -Action clean

# Или вручную
kubectl delete namespace order-exchange

# Удалить образы
docker images "order-exchange/*" -q | ForEach-Object { docker rmi $_ -f }

# Очистить неиспользуемые ресурсы Docker
docker system prune -a --volumes
```

## Конфигурация

### Изменение паролей

Отредактируйте `k8s/01-configmaps-secrets.yaml`:
- `db-secrets`: пароли БД
- `rabbitmq-secrets`: пароли RabbitMQ
- `jwt-secrets`: секретный ключ JWT

После изменения:
```powershell
kubectl apply -f k8s/01-configmaps-secrets.yaml
kubectl rollout restart deployment -n order-exchange <deployment-name>
```

### Изменение ресурсов

Отредактируйте `resources` в соответствующих манифестах и примените:
```powershell
kubectl apply -f k8s/<manifest-file>.yaml
```

## Production рекомендации

1. **Безопасность**:
   - Используйте внешние Secrets (HashiCorp Vault, AWS Secrets Manager)
   - Включите Network Policies
   - Настройте RBAC
   - Используйте TLS для всех соединений

2. **Мониторинг**:
   - Настройте алерты в Prometheus
   - Интегрируйте с внешними системами мониторинга
   - Настройте дашборды в Grafana

3. **Резервное копирование**:
   - Настройте регулярные бэкапы БД
   - Используйте persistent volumes для критичных данных

4. **Масштабирование**:
   - Настройте Horizontal Pod Autoscaler
   - Используйте несколько узлов в каждой зоне доступности
   - Настройте Pod Disruption Budgets

5. **CI/CD**:
   - Автоматизируйте сборку образов
   - Используйте GitOps (ArgoCD, Flux)
   - Настройте автоматическое тестирование

## Дополнительная информация

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)

