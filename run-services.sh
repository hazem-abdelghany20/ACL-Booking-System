#!/bin/bash

# Function to print colored output
print_status() {
  if [ "$2" == "success" ]; then
    echo -e "\033[32m$1\033[0m"  # Green
  elif [ "$2" == "info" ]; then
    echo -e "\033[34m$1\033[0m"  # Blue
  elif [ "$2" == "warning" ]; then
    echo -e "\033[33m$1\033[0m"  # Yellow
  elif [ "$2" == "error" ]; then
    echo -e "\033[31m$1\033[0m"  # Red
  else
    echo "$1"
  fi
}

# Start infrastructure services
start_infrastructure() {
  print_status "Starting infrastructure services..." "info"
  docker-compose up -d postgres mongodb redis rabbitmq elasticsearch
  
  print_status "Waiting for services to be ready..." "info"
  sleep 10
  
  # Check if services are running
  print_status "Checking infrastructure services:" "info"
  
  # Check PostgreSQL
  echo -n "PostgreSQL: "
  if docker exec -it postgres pg_isready -U postgres > /dev/null 2>&1; then
    print_status "Running ✅" "success"
  else
    print_status "Not running ❌" "error"
  fi
  
  # Check Redis
  echo -n "Redis: "
  if docker exec -it redis redis-cli ping 2>/dev/null | grep -q 'PONG'; then
    print_status "Running ✅" "success"
  else
    print_status "Not running ❌" "error"
  fi
  
  # Check RabbitMQ
  echo -n "RabbitMQ: "
  if curl -s http://localhost:15672 > /dev/null 2>&1; then
    print_status "Running ✅" "success"
  else
    print_status "Not running ❌" "error"
  fi
  
  # Check MongoDB
  echo -n "MongoDB: "
  if docker exec -it mongodb mongosh --quiet --eval "db.runCommand({ ping: 1 }).ok" 2>/dev/null | grep -q '1'; then
    print_status "Running ✅" "success"
  else
    print_status "Not running ❌" "error"
  fi
}

# Main script starts here
clear
print_status "========== ACL-Booking-System Services Runner ==========" "info"
echo ""

# Start required infrastructure
start_infrastructure

echo ""
print_status "========== Microservices Instructions ==========" "info"
echo ""
print_status "To manually run individual microservices:" "info"
echo ""
print_status "1. API Gateway" "warning"
echo "cd ApiGateway"
echo "mvn spring-boot:run"
echo ""

print_status "2. User Auth Service" "warning"
echo "cd UserAuthService"
echo "mvn spring-boot:run"
echo ""

print_status "3. Event Service" "warning"
echo "cd EventService"
echo "mvn spring-boot:run"
echo ""

print_status "4. Booking Service" "warning"
echo "cd BookingService"
echo "mvn spring-boot:run"
echo ""

print_status "5. Notification Service" "warning"
echo "cd NotificationService"
echo "mvn spring-boot:run"
echo ""

print_status "6. Search Service" "warning"
echo "cd SearchService"
echo "mvn spring-boot:run"
echo ""

print_status "========== Testing the Application ==========" "info"
echo ""
print_status "1. Import the Postman collection:" "info"
echo "   File -> Import -> Choose postman.json"
echo ""
print_status "2. Create a Postman environment with these variables:" "info"
echo "   - auth_token: (will be set after login)"
echo "   - booking_id: (will be set after creating a booking)"
echo ""
print_status "3. Follow the testing sequence:" "info"
echo "   a. Register a user"
echo "   b. Login and save the token"
echo "   c. Create an event"
echo "   d. Test search, booking, and notifications"
echo ""
print_status "========== Monitoring ==========" "info"
echo ""
echo "RabbitMQ Management Console: http://localhost:15672"
echo "Username: guest"
echo "Password: guest"
echo ""

print_status "Infrastructure services are now running and ready for testing!" "success"
echo "" 