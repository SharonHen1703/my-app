# My App - Auction System

A full-stack auction system built with Spring Boot backend and React frontend. The system manages user authentication, auction creation, and bidding functionality through REST APIs with JWT-based authentication via cookies.

## Prerequisites

- **Java**: 21 (as defined in `server/pom.xml`)
- **Maven**: Use Maven wrapper
- **Node.js**: 18+ (for React frontend)
- **PostgreSQL**: 12+ (for development database)

## Quickstart

### Backend

The Spring Boot server runs on port `8081` by default.

**Windows:**
cd server
mvn spring-boot:run

- **Default port:** `8081`
- **Configuration:** `server/src/main/resources/application.properties` and `application-dev.properties`
- **Database:** PostgreSQL (configured for dev)
- **Modules:** Authentication (`auth`), Auctions (`auctions`), Bidding (`bids`)

### Frontend

The React app runs on port `5173` (Vite default) and proxies API calls to `localhost:8081`.

cd client
npm install
npm run dev

- **Default port:** `5173`
- **Build tool:** Vite with TypeScript
- **API proxy:** All `/api/*` requests forwarded to `http://localhost:8081`

## Database

- **Development:** PostgreSQL (`myapp_dev` database)
- **Setup PostgreSQL:**
  CREATE DATABASE myapp_dev;
  CREATE USER myapp WITH PASSWORD 'YOUR_PASSWORD';
  GRANT ALL PRIVILEGES ON DATABASE myapp_dev TO myapp;
