# My App - Auction System

A full-stack auction system built with Spring Boot backend and React frontend. The system manages user authentication, auction creation, and bidding functionality through REST APIs with JWT-based authentication via cookies.

## Prerequisites

- **Java**: 21 (as defined in `server/pom.xml`)
- **Maven**: Use Maven wrapper (`./mvnw` or `mvnw.cmd`)
- **Node.js**: 18+ (for React frontend)
- **PostgreSQL**: 12+ (for development database)

## Quickstart (Development)

### Backend

The Spring Boot server runs on port `8081` by default.

**Windows:**

```cmd
cd server
mvnw.cmd spring-boot:run
# Or if Maven is installed globally:
mvn spring-boot:run
```

- **Default port:** `8081`
- **Configuration:** `server/src/main/resources/application.properties` and `application-dev.properties`
- **Database:** PostgreSQL (configured for dev)
- **Modules:** Authentication (`auth`), Auctions (`auctions`), Bidding (`bids`)

### Frontend

The React app runs on port `5173` (Vite default) and proxies API calls to `localhost:8081`.

```bash
cd client
npm install
npm run dev
```

- **Default port:** `5173`
- **Build tool:** Vite with TypeScript
- **API proxy:** All `/api/*` requests forwarded to `http://localhost:8081`

## Configuration

### Environment Variables / Properties

For development, update `server/src/main/resources/application-dev.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/myapp_dev
spring.datasource.username=myapp
spring.datasource.password=YOUR_PASSWORD

# JWT Secret (32+ characters)
app.auth.jwt.secret=mySecretKeyThatIsAtLeast32BytesLongForSecureSigning

# Server port
server.port=8081
```

**Note:** Do not use the example password in production.

## Database & Migrations

- **Development:** PostgreSQL (`myapp_dev` database)
- **Migrations:** Flyway automatically applies migrations from `server/src/main/resources/db/migration/`
- **Setup PostgreSQL:**
  ```sql
  CREATE DATABASE myapp_dev;
  CREATE USER myapp WITH PASSWORD 'YOUR_PASSWORD';
  GRANT ALL PRIVILEGES ON DATABASE myapp_dev TO myapp;
  ```

To switch database configuration, update the `spring.datasource.*` properties in `application-dev.properties`.

## Testing

**Backend tests:**

```bash
cd server
./mvnw test          # Unix/macOS (wrapper)
mvnw.cmd test        # Windows (wrapper)
mvn test             # If Maven installed globally
```

**Frontend tests:**

```bash
cd client
npm test             # Run once
npm run test:watch   # Watch mode
```

## Docker

TODO: No Dockerfile or docker-compose found in repository.

## Troubleshooting

- **Port 8081 already in use:** Change `server.port` in `application-dev.properties` or kill the process using the port
- **Port 5173 already in use:** Vite will automatically suggest another port, or use `npm run dev -- --port 3000`
- **PostgreSQL connection failed:** Ensure PostgreSQL is running and database/user exist with correct credentials
- **Maven/Node version issues:** Verify Java 21 and Node 18+ are installed and in PATH
- **Flyway migration errors:** Check database permissions and ensure previous migrations completed successfully

## Architecture at a glance

```
Client (React/Vite) → HTTP/JSON → Spring Boot Controllers → Services (Auth/Auction/Bid) → Repos/DAOs → PostgreSQL
Frontend (port 5173) ← API proxy ← Backend (port 8081) ← JPA + JDBC ← Database (port 5432)
```
