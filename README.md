# ♛ ChessTune

**The Ultimate Tournament Arena**

ChessTune is a high-stakes, full-stack competitive chess platform built for serious tournament organizers and players. It features real-time WebSocket gameplay, live Redis-powered leaderboards, Swiss-system automated tournament pairings, and a mandatory precision upsolve system.

---

## ⚡ Key Features

- **⚔️ Swiss-System Pairings**: Professional Dutch-system tournament pairings that automatically match players of similar scores.
- **⏱️ Real-Time Arena Engine**: Sub-millisecond WebSocket-powered game rooms with precision clocks and instant move broadcasting.
- **🏆 Live Leaderboards**: Real-time standings dynamically powered by Redis.
- **🧩 Mandatory Upsolving**: Players must mathematically solve their tournament blunders before being permitted to register for their next contest.
- **📈 Automated Rating System**: Performance-based Glicko/Elo rating tracking for all tournament participants.
- **🎓 Mentor Marketplace**: Integrated digital storefront where users can browse titled players by specialization and unlock personalized training.

---

## 🛠️ Tech Stack & Architecture

ChessTune uses a modern, high-performance **Hybrid Modular Monolith** architecture designed to handle "thundering herd" traffic during synchronized scheduled tournament rounds.

### Frontend
- **Framework**: Next.js 16 (React)
- **Styling**: Tailwind CSS v4 (Black & Gold Premium Custom Theme)
- **State Management**: Zustand
- **Chess Engine/UI**: `react-chessboard` (v5 options API) + `chess.js`

### Backend
- **Framework**: Java 21 & Spring Boot 3.5.x
- **API & Data**: REST APIs, Spring Data JPA, Hibernate
- **Real-Time WebSockets**: Spring STOMP over WebSockets utilizing `ChannelInterceptor` JWT validation.
- **Matchmaking & Cache**: Redis 7
- **Message Broker**: RabbitMQ 3 (For async Stockfish engine analysis requests)
- **Database**: PostgreSQL 16 

---

## 🚀 Local Deployment & Setup

### Prerequisites
- Docker & Docker Compose
- Java 21 & Maven
- Node.js & npm

### 1. Infrastructure (Database, Redis, RabbitMQ)
First, ensure that any local conflicting Postgres instances are stopped (e.g., `brew services stop postgresql`). 
Spin up the required infrastructure via Docker:
```bash
docker compose up -d
```
*This will deploy Postgres on port 5432, Redis on 6379, and RabbitMQ on 5672.*

### 2. Backend (Java Spring Boot)
Navigate to the core module and start the Spring Boot application:
```bash
cd backend/chesstune-core
mvn clean compile spring-boot:run
```
*The backend API server will start on port `8080`.*

### 3. Frontend (Next.js)
Navigate to the frontend directory, install dependencies, and run the development server:
```bash
cd frontend
npm install
npm run dev
```
*The web interface will be accessible at `http://localhost:3000`.*

---

## 🔐 Environment Variables

Ensure you have a `.env` file in your frontend root directory:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
```

The backend configuration relies on the environment details established in `application.yml` communicating automatically with your local `docker-compose` stack.
