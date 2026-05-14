# 2 Развертывание программного средства

## 2.1 Необходимое программное обеспечение

Для развертывания программного средства необходимо программное обеспечение, представленное в таблице 1.

Таблица 1 – Необходимое программное обеспечение

| ПО | Версия | Назначение |
| --- | --- | --- |
| Docker Desktop | 24.0+ | Контейнеризация и оркестрация сервисов |
| Minikube | 1.31+ | Локальный Kubernetes кластер |
| kubectl | 1.28+ | CLI для управления Kubernetes |
| PowerShell | 5.1+ | Запуск скриптов развертывания |
| Java JDK | 21+ | Компиляция backend-микросервисов (Spring Boot 3.5) |
| Gradle | 8.5+ | Сборка backend-микросервисов |
| Node.js | 18+ | Сборка frontend-приложения (Angular 20) |
| Git | 2.40+ | Клонирование репозитория |

Таким образом было представлено необходимое для развертывания программное обеспечение.


## 2.2 Системные требования

Далее представлены минимальные требования для развертывания программного средства:
- CPU: 4 ядра;
- RAM: 8 GB;
- Диск: 50 GB свободного места;
- ОС: Windows 10/11, Linux, macOS.

Таким образом были представлены минимальные требования для развертывания программного средства.


## 2.3 Развертывание через Docker Compose

Docker Compose позволяет развернуть все сервисы на одной машине в изолированных контейнерах. Этот метод подходит для:
- локальной разработки;
- тестирования;
- демонстрации системы.

### 2.3.1 Шаг 1. Клонирование репозитория

Первоначально необходимо клонировать репозиторий. Код представлен далее:

```bash
git clone https://github.com/NikitaKovzovich/order-exchange.git
cd order-exchange
```

Таким образом был склонирован репозиторий проекта.

### 2.3.2 Шаг 2. Проверка Docker

Далее необходимо проверить установку Docker.

```powershell
docker --version
docker-compose --version
```

Таким образом была проверена установка Docker.

### 2.3.3 Шаг 3. Запуск развертывания

Далее необходимо запустить скрипт развертывания.

```powershell
cd {PATH}\order-exchange
.\deploy-docker.ps1 -Action up
```

Скрипт `deploy-docker.ps1` (исполняемый файл PowerShell) выполняет следующие шаги:

```powershell
Write-Host "Order Exchange - Docker Compose Deployment" -ForegroundColor Cyan

# [1/3] Проверка Docker
Test-Docker

# [2/3] Генерация конфигурации Prometheus
Create-PrometheusConfig

# [3/3] Сборка и запуск всех сервисов
docker-compose -f ".\backendOrderFlow\docker-compose.yaml" up -d --build

# Ожидание готовности и вывод статуса
Start-Sleep -Seconds 10
docker-compose -f ".\backendOrderFlow\docker-compose.yaml" ps

Write-Host "=== Access Information ===" -ForegroundColor Green
Write-Host "Frontend:        http://localhost"
Write-Host "API Gateway:     http://localhost:8765"
Write-Host "Eureka Server:   http://localhost:8761"
Write-Host "RabbitMQ:        http://localhost:15672 (admin/admin123)"
Write-Host "Prometheus:      http://localhost:9090"
Write-Host "Grafana:         http://localhost:3001 (admin/admin)"
```

Скрипт автоматически собирает Docker-образы для семи микросервисов (eureka-server, api-gateway, auth-service, catalog-service, order-service, chat-service, document-service) и frontend-приложения, а также запускает пять экземпляров MySQL 8.0 (по одной БД на сервис), RabbitMQ, MinIO, Prometheus и Grafana.

Таким образом был представлен скрипт, автоматизирующий развертывание Docker-контейнеров.

### 2.3.4 Шаг 4. Проверка развертывания

Для проверки статусов контейнеров необходимо выполнить следующие команды:

```powershell
# Статус всех контейнеров
.\deploy-docker.ps1 -Action status

# Просмотр логов конкретного сервиса
docker-compose -f .\backendOrderFlow\docker-compose.yaml logs -f api-gateway

# Просмотр логов всех сервисов
.\deploy-docker.ps1 -Action logs
```

Таким образом были представлены команды, необходимые для проверки развертывания.

### 2.3.5 Остановка и очистка

Для остановки и очистки доступны как ручной, так и автоматический варианты с использованием параметров скрипта `deploy-docker.ps1`. Команды представлены далее:

```powershell
# Остановка всех контейнеров
.\deploy-docker.ps1 -Action down

# Полная очистка (включая volumes и образы)
.\deploy-docker.ps1 -Action clean

# Или вручную
docker-compose -f .\backendOrderFlow\docker-compose.yaml down -v
docker system prune -a --volumes
```

Таким образом был представлен полный цикл развертывания через docker-compose.


## 2.4 Развертывание в Kubernetes (Minikube)

Kubernetes обеспечивает:
- автоматическое масштабирование;
- self-healing (автоматический перезапуск упавших подов);
- service discovery и load balancing;
- rolling updates и rollbacks;
- управление secrets и конфигурацией.

Система развертывается на 4 специализированных логических нодах:
- **Frontend Node**: Angular-приложение;
- **Backend Node**: микросервисы Spring Boot (eureka-server, api-gateway, auth, catalog, order, chat, document);
- **Database Node**: MySQL 8.0;
- **Services Node**: RabbitMQ, Prometheus, Grafana, OpenTelemetry Collector.

В Minikube все ноды эмулируются на одном физическом узле с помощью меток (labels). Команды, необходимые для первоначальной проверки, представлены далее:

```powershell
# Проверка установки
minikube version
kubectl version --client

# Убеждаемся, что Docker запущен
docker ps
```

Таким образом были представлены команды первоначальной проверки.

### 2.4.1 Шаг 1. Запуск Minikube

Команды запуска представлены далее:

```powershell
# Останавливаем предыдущий кластер (если есть)
minikube delete

# Запускаем новый кластер с нужными ресурсами
minikube start --cpus=4 --memory=8192 --disk-size=50g --driver=docker

# Проверяем статус
minikube status

# Проверяем ноды
kubectl get nodes
```

Ожидаемый вывод представлен далее:

```
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

Таким образом были представлены команды запуска.

### 2.4.2 Шаг 2. Настройка Docker-окружения

Команды настройки Docker-окружения представлены далее:

```powershell
# Переключаемся на Docker daemon внутри Minikube
# Это позволяет использовать локальные образы без push в registry
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# Проверяем
docker ps
```

Таким образом были представлены команды настройки Docker-окружения.

### 2.4.3 Шаг 3. Запуск развертывания

Команды запуска развертывания представлены далее:

```powershell
# Переходим в директорию проекта
cd {PATH}\order-exchange

# Запускаем скрипт развертывания
.\deploy-k8s-cluster.ps1 -Action all
```

Скрипт `deploy-k8s-cluster.ps1` выполняет следующие шаги:

```powershell
# [1/4] Проверка предварительных требований
Test-Prerequisites   # docker, kubectl, доступность кластера

# [2/4] Настройка меток на нодах для распределения подов
foreach ($node in (kubectl get nodes -o json | ConvertFrom-Json).items) {
    kubectl label nodes $node.metadata.name node-role=frontend --overwrite
    kubectl label nodes $node.metadata.name node-role=backend --overwrite
    kubectl label nodes $node.metadata.name node-role=database --overwrite
    kubectl label nodes $node.metadata.name node-role=services --overwrite
}

# [3/4] Сборка Docker-образов для всех микросервисов и frontend
$services = @(
    "eureka-server", "api-gateway", "auth-service", "catalog-service",
    "order-service", "chat-service", "document-service", "frontend"
)
foreach ($service in $services) {
    docker build -t "order-exchange/$service:latest" .
}

# [4/4] Применение Kubernetes-манифестов
$manifests = @(
    "00-namespace.yaml",
    "01-configmaps-secrets.yaml",
    "02-database.yaml",
    "03-services-layer.yaml",
    "04-backend-services.yaml",
    "05-frontend.yaml"
)
foreach ($manifest in $manifests) {
    kubectl apply -f ".\k8s\$manifest"
}
```

Таким образом были представлены команды, выполняемые скриптом `deploy-k8s-cluster.ps1`.

### 2.4.4 Шаг 4. Проверка развертывания

Проверка развертывания происходит при помощи следующих команд:

```powershell
# Проверяем статус подов
kubectl get pods -n order-exchange

# Проверяем сервисы
kubectl get services -n order-exchange

# Проверяем deployments
kubectl get deployments -n order-exchange

# Постоянный мониторинг подов
kubectl get pods -n order-exchange -w

# Сводный статус через скрипт
.\deploy-k8s-cluster.ps1 -Action status
```

Ожидаемый вывод (все поды в статусе Running) представлен на рисунке 1.

Рисунок 1 – Ожидаемый вывод

Таким образом была представлена проверка развертывания.

### 2.4.5 Шаг 5. Мониторинг и отладка

Для мониторинга и отладки можно использовать следующие команды:

```powershell
# Просмотр логов пода
kubectl logs -n order-exchange <pod-name>

# Следить за логами в реальном времени
kubectl logs -n order-exchange <pod-name> -f

# Логи предыдущего контейнера (если под перезапускался)
kubectl logs -n order-exchange <pod-name> --previous

# Детальная информация о поде
kubectl describe pod -n order-exchange <pod-name>

# Выполнение команды внутри пода
kubectl exec -n order-exchange <pod-name> -it -- /bin/sh

# Проверка events
kubectl get events -n order-exchange --sort-by='.lastTimestamp'

# Ресурсы подов
kubectl top pods -n order-exchange

# Ресурсы узлов
kubectl top nodes
```

Точки доступа к приложению в Minikube:

```
Frontend:    http://localhost:30080
API Gateway: http://localhost:30800
Eureka:      http://<minikube-ip>:30761
Prometheus:  http://localhost:30090
Grafana:     http://localhost:30300 (admin/admin)
```

Таким образом были представлены команды для мониторинга и отладки.

### 2.4.6 Шаг 6. Остановка и очистка

Команды для остановки и очистки представлены далее:

```powershell
# Удаление всех ресурсов проекта
.\deploy-k8s-cluster.ps1 -Action clean

# Либо удалить namespace вручную
kubectl delete namespace order-exchange

# Остановка Minikube
minikube stop

# Полное удаление кластера
minikube delete
```

Таким образом были представлены команды для остановки и очистки.


## 2.5 Kubernetes-манифесты

Структура манифестов представлена на рисунке 2.

```
k8s/
├── 00-namespace.yaml           # Namespace order-exchange
├── 01-configmaps-secrets.yaml  # ConfigMap и Secrets (БД, JWT, RabbitMQ)
├── 02-database.yaml            # MySQL: PV, PVC, Deployment, Service, init-скрипты
├── 03-services-layer.yaml      # RabbitMQ, Prometheus, Grafana, OpenTelemetry
├── 04-backend-services.yaml    # Eureka, API Gateway, 5 микросервисов
└── 05-frontend.yaml            # Angular frontend + Ingress
```

Рисунок 2 – Структура манифестов

Все манифесты используют `nodeSelector` для размещения компонентов на соответствующих логических нодах (`node-role: frontend|backend|database|services`).

Далее представлен манифест слоя хранения данных (БД) и backend-микросервисов.

**Манифест слоя БД (`02-database.yaml`):**

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: mysql-pv
  namespace: order-exchange
spec:
  capacity:
    storage: 20Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: local-storage
  hostPath:
    path: /mnt/data/mysql
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: order-exchange
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 20Gi
  storageClassName: local-storage
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: order-exchange
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
        tier: database
    spec:
      nodeSelector:
        node-role: database
      containers:
      - name: mysql
        image: mysql:8.0
        ports:
        - containerPort: 3306
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: MYSQL_ROOT_PASSWORD
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
        - name: init-scripts
          mountPath: /docker-entrypoint-initdb.d
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc
      - name: init-scripts
        configMap:
          name: mysql-init-scripts
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: order-exchange
spec:
  type: ClusterIP
  selector:
    app: mysql
  ports:
  - protocol: TCP
    port: 3306
    targetPort: 3306
```

**Манифест backend-микросервисов (`04-backend-services.yaml`, фрагмент с Eureka, API Gateway и Auth):**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
  namespace: order-exchange
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
        tier: backend
    spec:
      nodeSelector:
        node-role: backend
      containers:
      - name: eureka-server
        image: order-exchange/eureka-server:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8761
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server-service
  namespace: order-exchange
spec:
  type: ClusterIP
  selector:
    app: eureka-server
  ports:
  - protocol: TCP
    port: 8761
    targetPort: 8761
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: order-exchange
spec:
  replicas: 2
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
        tier: backend
    spec:
      nodeSelector:
        node-role: backend
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
          valueFrom:
            configMapKeyRef:
              name: common-config
              key: EUREKA_URL
        - name: JWT_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: jwt-secrets
              key: JWT_SECRET_KEY
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-service
  namespace: order-exchange
spec:
  type: ClusterIP
  selector:
    app: api-gateway
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: order-exchange
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
        tier: backend
    spec:
      nodeSelector:
        node-role: backend
      containers:
      - name: auth-service
        image: order-exchange/auth-service:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: USER_DB_URL
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: DB_USERNAME
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: DB_PASSWORD
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: common-config
              key: EUREKA_URL
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: order-exchange
spec:
  type: ClusterIP
  selector:
    app: auth-service
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
```

Таким образом было представлено 2 основных манифеста.
