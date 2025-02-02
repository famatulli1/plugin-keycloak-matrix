# Keycloak Matrix 2FA Plugin Architecture

## Overview
This plugin implements a two-factor authentication mechanism for Keycloak using Matrix as the delivery channel for one-time passwords (OTP).

## Core Components

### 1. Matrix Authentication SPI
- Implements Keycloak's `AuthenticatorFactory` and `Authenticator` interfaces
- Handles the authentication flow steps:
  1. Primary authentication (username/password)
  2. OTP generation and delivery via Matrix
  3. OTP verification

### 2. Matrix Integration Service
- Manages communication with Matrix servers
- Handles bot authentication and message delivery
- Implements error handling and retry logic

### 3. Configuration Management
- Admin UI configuration for:
  - Matrix server URL
  - Bot credentials
  - Message templates
  - OTP settings (length, validity duration)
- User attribute mapping for Matrix IDs

### 4. Security Considerations
- Secure storage of Matrix bot credentials
- OTP generation using cryptographically secure methods
- Rate limiting for Matrix message delivery
- Audit logging of authentication events

## Technical Stack
- Keycloak 21.x or higher
- Matrix Java SDK
- Maven for build management
- JUnit and Mockito for testing

## Data Flow
1. User initiates login
2. After successful password verification:
   - Generate OTP
   - Retrieve user's Matrix ID
   - Send OTP via Matrix bot
   - Present OTP input form
3. Verify submitted OTP
4. Complete authentication

## Configuration Schema
```yaml
matrix:
  server_url: "https://matrix.org"
  bot_username: "@securitybot:matrix.org"
  bot_credentials: "encrypted_credentials"
  message_template: "Your authentication code is: {code}"
  otp_validity: 300  # seconds
  otp_length: 6
  user_id_attribute: "matrix_id"
```

## Testing Strategy
- Unit tests for:
  - OTP generation/validation
  - Matrix message formatting
  - Configuration handling
- Integration tests for:
  - Matrix communication
  - Full authentication flow
- Security testing:
  - Credential protection
  - Rate limiting
  - Error handling

## Deployment Requirements
- Java 11 or higher
- Access to Matrix homeserver
- Network connectivity between Keycloak and Matrix server
- Proper bot account setup on Matrix server