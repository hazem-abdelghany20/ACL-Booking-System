# ACL-Booking-System

A microservice-based event booking platform for managing events, registrations, and notifications.

## Project Structure

This project consists of six microservices, each responsible for specific domain functionality:

### UserAuthService

Authentication and authorization service powered by Supabase.

Features:
- Email/password authentication
- Phone number authentication with OTP
- Google OAuth integration
- Spring Security integration
- User profile management
- Payment processing integration
- User event relationships management

### BookingService

Handles event bookings and reservation management.

Features:
- Event booking creation and management
- Payment processing for event tickets
- Booking cancellation and refund handling
- Integration with EventService for availability checks
- Real-time booking notifications via RabbitMQ

### EventService

Manages event creation, updates, and search capabilities.

Features:
- Event creation and publication
- Event categorization and filtering
- Public and private event support
- Capacity and ticket management
- Event participation tracking
- Comprehensive event search and filtering

### NotificationService

Handles user notifications across the platform.

Features:
- Real-time notifications for bookings, events, and system alerts
- Multiple notification channels (email, in-app, SMS)
- Notification status tracking (read/unread)
- Command pattern implementation for notification actions
- RabbitMQ integration for asynchronous processing

### SearchService

Provides advanced search capabilities across the platform.

Features:
- Multi-criteria event search (keyword, location, date)
- Strategy pattern implementation for different search types
- Integration with EventService for data retrieval
- Optimized search results with filtering

### ApiGateway

API Gateway that routes client requests to appropriate services.

Features:
- Centralized routing for all microservices
- Authentication and authorization middleware
- Security configurations with JWT validation
- Redis-based session management
- Cross-Origin Resource Sharing (CORS) support

## Technology Stack

- **Backend**: Spring Boot, Java 23
- **Messaging**: RabbitMQ for asynchronous communication
- **API Communication**: OpenFeign for service-to-service calls
- **Authentication**: Supabase
- **Database**: PostgreSQL, H2 (for tests)
- **Caching**: Redis for session management
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven
- **Containerization**: Docker
- **API Gateway**: Spring Cloud Gateway

## Architecture

The system follows a microservice architecture pattern with:
- Centralized API Gateway for client communication
- Service-to-service communication via REST APIs and message queues
- Event-driven architecture for real-time updates
- Repository pattern for data access
- Design patterns (Strategy, Factory, Command) for extensibility

## Getting Started

### Prerequisites

- Java 23+
- Maven 3.8+
- Docker and Docker Compose
- Supabase account (for authentication)

### Running the Services

Each service can be run independently. Navigate to the service directory and run:

```bash
mvn spring-boot:run
```

To run all services with their dependencies:

```bash
docker-compose up
```

### Testing the Full Application

To test the complete application with all microservices:

1. **Start all services using Docker Compose**:
   ```bash
   docker-compose up -d
   ```

2. **Access the application through the API Gateway**:
   Once all services are up and running, access the API Gateway at:
   ```
   http://localhost:8000
   ```
   
   All API requests should be made to this endpoint, which will route to the appropriate service.

3. **Test individual endpoints**:
   - Authentication: `http://localhost:8000/api/auth/login`
   - Events: `http://localhost:8000/api/events`
   - Bookings: `http://localhost:8000/api/bookings`
   - Notifications: `http://localhost:8000/api/notifications`
   - Search: `http://localhost:8000/api/search`

4. **Use Postman or cURL to test APIs**:
   You can use tools like Postman to create and test API requests, for example:
   ```bash
   curl -X GET http://localhost:8000/api/events
   ```

5. **Monitor logs**:
   Monitor service logs to track request flow and debug issues:
   ```bash
   docker-compose logs -f api-gateway
   ```
   
   Or to view logs for a specific service:
   ```bash
   docker-compose logs -f [service-name]
   ```

6. **Access RabbitMQ Management Console**:
   Monitor message queues at:
   ```
   http://localhost:15672
   ```
   (login with guest/guest)

### Testing

To run tests for individual services:

```bash
mvn test
```

To run integration tests:

```bash
mvn verify
```