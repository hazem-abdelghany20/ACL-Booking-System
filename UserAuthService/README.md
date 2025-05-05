# UserAuthService

A Spring Boot microservice for user authentication and authorization using Supabase as the authentication provider.

## Overview

This microservice implements authentication features for an event booking platform using Supabase's authentication services. The implementation provides a comprehensive set of authentication methods including email/password, phone number with OTP verification, and Google OAuth.

## Features

- **Email Authentication**
  - User signup with email and password
  - User signin with email and password
  
- **Phone Authentication**
  - User signup with phone number
  - Phone number verification with OTP
  - User signin with phone number and OTP
  
- **OAuth Authentication**
  - Google OAuth integration
  - OAuth callback handling
  
- **Security Implementation**
  - Integration with Spring Security
  - Secured API endpoints
  - Cross-Origin Resource Sharing (CORS) configuration
  
## Technical Implementation

### API Endpoints

The service exposes the following endpoints:

- `/api/auth/*` - Core authentication endpoints
- `/api/supabase/*` - Supabase-specific authentication operations
- `/api/oauth/*` - OAuth authentication flows

### Architecture

- **WebClient Integration**: Uses Spring WebClient to interact with Supabase REST APIs
- **Reactive Programming**: Implemented with Project Reactor for asynchronous operations
- **Microservice Design**: Part of a larger event booking system, focusing solely on authentication concerns

### Data Models

- **Request/Response Models**:
  - `EmailAuthRequest` - For email-based authentication
  - `PhoneAuthRequest` - For phone-based authentication
  - `OtpVerificationRequest` - For verifying OTP codes
  - `AuthResponse` - Standardized authentication response

### Security Configuration

- Configured Spring Security to secure endpoints
- Permitted access to authentication endpoints
- Implemented CSRF protection
- Configured CORS for cross-domain requests

## Testing Infrastructure

### Unit Tests

- Unit tests with mocked WebClient responses
- Tests for controllers and services

### Integration Tests

- Integration tests for Supabase API interactions
- Custom test configuration for security
- Test-specific application properties

### Test Categories

- **Controller Tests**: Testing API endpoints
  - `OAuthControllerTest` - Tests for OAuth endpoints
  - `AuthControllerTest` - Tests for auth endpoints
  
- **Service Tests**:
  - `SupabaseAuthServiceTest` - Unit tests for auth service
  - `SupabaseAuthServiceIntegrationTest` - Integration tests with Supabase

## Configuration

### Application Properties

- Development and production configurations
- Test-specific configurations
- Supabase API configuration

### Environment Variables

- Supabase URL
- Supabase API key
- Supabase Secret key

## Technical Challenges Solved

1. **Supabase Java Integration**: Implemented a custom WebClient-based integration in the absence of an official Java SDK.

2. **Error Handling**: Created a robust error handling mechanism for WebClient errors with `GlobalExceptionHandler`.

3. **OAuth Flow**: Implemented complete OAuth flow for Google authentication.

4. **Testing Challenges**: 
   - Resolved package name casing inconsistencies
   - Implemented custom security configuration for tests
   - Created error handling for API limitations in integration tests

5. **API Version Compatibility**: Adapted to Supabase API changes by handling HTTP method limitations in tests.

## Development Guide

### Prerequisites

- Java 23
- Maven 3.8+
- Supabase account with API keys

### Building the Service

```bash
mvn clean package
```

### Running the Service

```bash
java -jar target/UserAuthService-0.0.1-SNAPSHOT.jar
```

### Running Tests

```bash
mvn test
```

## Future Improvements

- Implement refresh token handling
- Add more OAuth providers (Facebook, Twitter, etc.)
- Implement role-based authorization
- Add rate limiting for authentication attempts 