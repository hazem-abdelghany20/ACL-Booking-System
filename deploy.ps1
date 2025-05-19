Write-Host "Starting deployment..."

# Namespace
Write-Host "`n--- Applying Namespace ---"
kubectl apply -f .\deployment\namespace.yaml

# Secrets
Write-Host "`n--- Applying Secrets ---"
kubectl apply -f .\deployment\secrets.yaml --namespace=microservices-app

# ConfigMaps
Write-Host "`n--- Applying ConfigMaps ---"
kubectl apply -f .\deployment\configmaps.yaml --namespace=microservices-app

# PVCs
Write-Host "`n--- Applying PVCs ---"
kubectl apply -f .\deployment\pvc.yaml --namespace=microservices-app

# StatefulSets
Write-Host "`n--- Applying StatefulSets ---"
kubectl apply -f .\deployment\statefulsets.yaml --namespace=microservices-app

# Deployments
Write-Host "`n--- Applying Deployments ---"
kubectl apply -f .\deployment\deployments.yaml --namespace=microservices-app

# Services
Write-Host "`n--- Applying Services ---"
kubectl apply -f .\deployment\services.yaml --namespace=microservices-app

# API Gateway
Write-Host "`n--- Applying API Gateway ---"
kubectl apply -f .\deployment\api-gateway.yaml --namespace=microservices-app

# Redis
Write-Host "`n--- Applying Redis ---"
kubectl apply -f .\deployment\redis.yaml --namespace=microservices-app

# RabbitMQ
Write-Host "`n--- Applying RabbitMQ ---"
kubectl apply -f .\deployment\rabbitmq.yaml --namespace=microservices-app

# Wait for all pods
Write-Host "`nWaiting for pods to be ready (timeout: 5 min)..."
kubectl wait --for=condition=ready pod --all --namespace=microservices-app --timeout=300s

# Show pod status
Write-Host "`n✅ Deployment complete. Current pod status:"
kubectl get pods -n microservices-app
