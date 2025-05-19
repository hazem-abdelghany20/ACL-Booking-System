#!/bin/bash

# Exit on any error
set -e

echo "Starting deployment process..."

# Create namespace
echo "Creating namespace..."
kubectl apply -f deployment/namespace.yaml

# Apply secrets
echo "Applying secrets..."
kubectl apply -f deployment/secrets.yaml --namespace=microservices-app

# Apply configmaps
echo "Applying configmaps..."
kubectl apply -f deployment/configmaps.yaml --namespace=microservices-app

# Apply PVCs
echo "Applying persistent volume claims..."
kubectl apply -f deployment/pvc.yaml --namespace=microservices-app

# Apply StatefulSets
echo "Applying statefulsets..."
kubectl apply -f deployment/statefulsets.yaml --namespace=microservices-app

# Apply Deployments
echo "Applying deployments..."
kubectl apply -f deployment/deployments.yaml --namespace=microservices-app

# Apply Services
echo "Applying services..."
kubectl apply -f deployment/services.yaml --namespace=microservices-app

# Apply API Gateway
echo "Applying API Gateway..."
kubectl apply -f deployment/api-gateway.yaml --namespace=microservices-app

# Apply Redis
echo "Applying Redis..."
kubectl apply -f deployment/redis.yaml --namespace=microservices-app

# Apply RabbitMQ
echo "Applying RabbitMQ..."
kubectl apply -f deployment/rabbitmq.yaml --namespace=microservices-app

# Wait for pods to be ready
echo "Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod --all --namespace=microservices-app --timeout=300s

# Print pod status
echo "Deployment completed. Pod status:"
kubectl get pods --namespace=microservices-app

minikube start 