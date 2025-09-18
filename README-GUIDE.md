# GuildLite Testing Guide

## Overview
This guide provides step-by-step instructions for testing the GuildLite system functionality including team management, real-time chat, and automatic event broadcasting.

## Prerequisites
- **Postman** (for API testing)
- **WebSocket client** (Postman WebSocket or browser-based client)
- **Java 21+** and **Maven** (to run the application)

## Quick Start

### 1. Start the Application
```bash
git clone [repository-url]
cd guild-lite
mvn clean install
cd guild-main-app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Application will start on: http://localhost:8080

### 2. Verify Application is Running
```bash
curl http://localhost:8080/api/test/ping
# Expected response: "pong"
```

## Main Testing Scenario

### Phase 1: User Registration & Team Creation

#### Step 1: Register First User
**Endpoint:** `POST http://localhost:8080/api/auth/register`

**Request Body:**
```json
{
    "username": "test1",
    "email": "test1@test.com",
    "password": "password123"
}
```

**Expected Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzOTI3Z...",
  "user": {
    "id": "252f75a0-9b14-4d42-ac18-e927e85656a4",
    "username": "test1",
    "email": "test1@test.com",
    "status": "ACTIVE",
    "createdAt": null,
    "lastLoginAt": null
  },
  "success": true,
  "message": "Successful registration for user: test1"
}
```

**Save:** `token1` and `user1_id` for next steps

#### Step 2: Create Team (Using User 1's Token)
**Endpoint:** `POST http://localhost:8080/api/teams/create

**Headers:**
```
Authorization: Bearer {token1}
Content-Type: application/json
```

**Request Body:**
```json
{
    "name": "Test Squad",
    "description": "Testing team for demonstration"
}
```

**Expected Response (200):**
```json
{
  "success": true,
  "newToken": "eyJhbGciOiJIUzUxMiJ9.....",
  "team": {
    "id": "71d355e9-d21b-497b-b605-5d50c1616271",
    "name": "Test Squad",
    "description": "Testing team for demonstration",
    "createdBy": "252f75a0-9b14-4d42-ac18-e927e85656a4",
    "teamCoinBalance": 0,
    "currentMembersCount": 1,
    "createdAt": "2025-09-18T17:10:48.045934",
    "members": [
      {
        "userId": "252f75a0-9b14-4d42-ac18-e927e85656a4",
        "username": "test1",
        "role": "OWNER",
        "joinedAt": "2025-09-18T17:10:48.052942",
        "addedCoinsCount": 0
      }
    ]
  },
  "message": "Team Test Squad created successfully"
}
```

**Save:** `newToken` for next steps (new token contains team ID)

**Save:** `team_id` for next steps

### Phase 2: WebSocket Connection (User 1)

#### Step 3: Connect User 1 to Chat
**WebSocket URL:** `ws://localhost:8080/ws/chat?token={newToken}`

**Using Postman:**
1. Create new WebSocket request
2. URL: `ws://localhost:8080/ws/chat?token={newToken}`
3. Click "Connect"

**Expected:** Connection successful (Status: 101 Switching Protocols)

### Phase 3: Second User & Team Join

#### Step 4: Register Second User
**Endpoint:** `POST http://localhost:8080/api/auth/register`

**Request Body:**
```json
{
    "username": "test2",
    "email": "test2@test.com",
    "password": "password123"
}
```

**Save:** `team_id` (saved from previous step)

**Save:** `token2` and `user2_id`

#### Step 5: Join Team (Using User 2's Token)
**Endpoint:** `POST http://localhost:8080/api/teams/{team_id}/join`

**Headers:**
```
Authorization: Bearer {token2}
```

**Expected Response (200):** Success message

**EXPECTED BEHAVIOR:** User 1's WebSocket should receive automatic notification:
```json
{
    "senderId": "SYSTEM",
    "senderUsername": "System",
    "teamId": "team-uuid",
    "message": "test2 joined the team!",
    "type": "TEAM_JOIN",
    "timestamp": "2025-09-17 15:30:45"
}
```

### Phase 4: Second WebSocket Connection

#### Step 6: Connect User 2 to Chat
**WebSocket URL:** `ws://localhost:8080/ws/chat?token={token2}`

**Expected:** Connection successful + both users in same chat room

### Phase 5: Real-time Messaging Test

#### Step 7: Send Message from User 1
**WebSocket Message (User 1):**
```json
{
    "message": "Hello test2! Welcome to the team!",
    "teamId": "team-uuid"
}
```

**EXPECTED:** User 2's WebSocket receives the message:
```json
{
    "senderId": "user-uuid-1",
    "senderUsername": "test1",
    "teamId": "team-uuid",
    "message": "Hello test2! Welcome to the team!",
    "type": "CHAT",
    "timestamp": "2025-09-17 15:31:00"
}
```

#### Step 8: Send Reply from User 2
**WebSocket Message (User 2):**
```json
{
    "message": "Thanks test1! Happy to be here!",
    "teamId": "team-uuid"
}
```

**EXPECTED:** User 1's WebSocket receives the reply

### Phase 6: Coin System & Event Broadcasting

#### Step 9: Add Coins to Team
**Endpoint:** `POST http://localhost:8080/api/coins/{team_id}/add`

**Headers:**
```
Authorization: Bearer {token1}
Content-Type: application/json
```

**Request Body:**
```json
{
    "amount": 100,
    "description": "Added by test1"
}
```

**Expected Response (200):**
```json
{
    "newTotal": 100,
    "teamId": "team-uuid"
}
```

**EXPECTED:** Both WebSocket connections receive automatic notification:
```json
{
    "senderId": "SYSTEM",
    "senderUsername": "System",
    "teamId": "team-uuid",
    "message": "test1 added 100 coins to the team pool! Total: 100 coins",
    "type": "COIN_ADD",
    "timestamp": "2025-09-17 15:32:00"
}
```

## Additional API Tests

### Get Chat History
**Endpoint:** `GET http://localhost:8080/api/chat/history?limit=10`
**Headers:** `Authorization: Bearer {token}`

**Expected:** Array of chat messages including system notifications

### Get Coin Balance
**Endpoint:** `GET http://localhost:8080/api/coins/{team_id}/get-balance
**Headers:** `Authorization: Bearer {token}`

**Expected:** Current team coin balance

### Get Team Info
**Endpoint:** `GET http://localhost:8080/api/teams/{team_id}/get
**Headers:** `Authorization: Bearer {token}`

**Expected:** Team details with member count

---

**Contact:** [Muhammet KILIC](https://github.com/mhmmtklc) | [LinkedIn](https://linkedin.com/in/mhmmtklc) | [Email](mailto:muhammetkilic@yahoo.com)