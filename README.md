# ACL-Booking-System

A microservice-based event booking platform.

## Project Structure

This project consists of multiple microservices, each responsible for specific domain functionality:

### UserAuthService

Authentication and authorization service powered by Supabase. [View documentation](./UserAuthService/README.md).

Features:
- Email/password authentication
- Phone number authentication with OTP
- Google OAuth integration
- Spring Security integration

### Other Services

*Additional services will be documented as they are implemented.*

## Technology Stack

- **Backend**: Spring Boot, Java 23
- **Authentication**: Supabase
- **Database**: PostgreSQL, H2 (for tests)
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- Java 23+
- Maven 3.8+
- Supabase account (for authentication)

### Running the Services

Each service can be run independently. Navigate to the service directory and run:

```bash
mvn spring-boot:run
```

### Testing

```bash
mvn test
```