# User Authentication Service

This service handles user authentication and wallet functionality for the ACL Booking System.

## Features

- Email authentication via Supabase
- Google OAuth authentication
- Session management using the Singleton pattern
- Authentication strategies using the Strategy pattern
- Wallet management for users
- Password reset functionality

## Setup and Running

### Prerequisites

- Java 23 or higher
- Maven 3.8 or higher
- Supabase project with the required tables and functions

### Running the Service

1. Make sure you have Java 23 installed:
   ```
   java -version
   ```

2. Set the JAVA_HOME environment variable:
   ```
   export JAVA_HOME=/path/to/java23
   ```

3. Run the service using Maven:
   ```
   mvn org.springframework.boot:spring-boot-maven-plugin:run
   ```

   Or using the provided script:
   ```
   ./run.sh
   ```

### Setting up Supabase

1. Create a Supabase project and enable Email and Google authentication

2. For wallet functionality, execute the SQL in `src/main/resources/supabase-wallet-setup.sql` in the Supabase SQL editor

3. Configure the Supabase URL and API key in `src/main/resources/application.properties`

## API Endpoints

### Authentication

- `POST /api/auth/signup` - Register with email/password
- `POST /api/auth/signin` - Login with email/password
- `POST /api/auth/signout` - Logout
- `POST /api/auth/reset-password` - Request password reset email
- `GET /api/auth/validate` - Validate session

### OAuth

- `GET /api/oauth/google` - Get Google sign-in URL
- `GET /api/oauth/callback` - OAuth callback endpoint
- `GET /api/oauth/google/redirect` - Redirect to Google sign-in

### Wallet

- `GET /api/wallet/balance` - Get wallet balance
- `POST /api/wallet/add-funds` - Add funds to wallet
- `POST /api/wallet/deduct-funds` - Deduct funds from wallet

## Design Patterns

This service uses the following design patterns:

1. **Strategy Pattern** - For different authentication mechanisms (email, Google)
2. **Singleton Pattern** - For session management
3. **Factory Pattern** - For creating authentication strategies

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request 