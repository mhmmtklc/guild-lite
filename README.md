# GuildLite - Team Chat & Coin Sharing System

A minimal backend system for multiplayer game features with team management, shared coin pools, and real-time chat.

**Developer:** [Muhammet KILIC](https://github.com/mhmmtklc) | [LinkedIn](https://linkedin.com/in/mhmmtklc) | [Email](mailto:muhammetkilic@yahoo.com)

## Architecture

### Module Structure
```
guild-lite/
├── guild-main-app/          # Main application (Java)
├── user-module/             # JWT authentication & user management (Kotlin)
├── team-module/             # Team creation & membership (Kotlin)
├── coin-module/             # Shared coin pool management (Kotlin)
├── chat-module/             # Real-time WebSocket chat (Java)
└── common-security/         # Shared JWT security components (Java)
```

### Module Responsibilities

| Module | Technology                     | Features |
|--------|--------------------------------|----------|
| **user-module** | Kotlin + Spring Security + JWT | User login, JWT token generation |
| **team-module** | Kotlin + Spring Boot + JPA     | Create teams, join/leave teams |
| **coin-module** | Kotlin + Spring Boot + JPA     | Add coins to team pool, view balance |
| **chat-module** | Java + Spring WebSocket        | Real-time team chat, event broadcasting |
| **common-security** | Java + JWT                     | JWT validation, WebSocket security |

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Team Management
- `POST /api/teams/create` - Create team
- `POST /api/teams/{id}/join` - Join team
- `GET /api/teams/{id}/get` - View team info

### Coin Management
- `POST /api/coins/{teamId}/add` - Add coins to team pool
- `GET /api/coins/{teamId}/get-balance` - View team coin balance

### Chat System
- `WS /ws/chat?token={jwt}` - Real-time team chat
- `GET /api/chat/history` - Get chat history

## WebSocket Usage

**Connect:**
```
ws://localhost:8080/ws/chat?token=YOUR_JWT_TOKEN
```

**Send Message:**
```json
{
    "message": "Hello team!",
    "teamId": "{{team-id}}"
}
```

## Quick Start

### Development (H2 Database)
```bash
mvn clean install
cd guild-main-app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production (PostgreSQL)
```bash
mvn clean package
java -jar guild-main-app/target/guild-main-app-1.0.0.jar --spring.profiles.active=prod
```

## Database Configuration

- **Development:** H2 in-memory database
- **Production:** PostgreSQL database

## Documentation

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/v3/api-docs

## Features

- JWT-based authentication across all services
- Real-time team chat with WebSocket
- Automatic event broadcasting (team join/leave, coin additions)
- Modular architecture with Java-Kotlin interoperability
- Multi-profile support (dev/prod environments)
