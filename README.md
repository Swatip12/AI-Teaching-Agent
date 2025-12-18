# AI Teaching Platform

A web-based learning management system that teaches programming concepts from absolute beginner to job-ready level using AI-powered tutoring.

## Technology Stack

- **Frontend**: Angular 17+ with TypeScript
- **Backend**: Java 21 with Spring Boot 3.x
- **Database**: PostgreSQL
- **AI Integration**: OpenAI GPT-4 API
- **Code Execution**: Docker containers
- **Testing**: JUnit 5, Jasmine/Karma, QuickCheck, fast-check

## Project Structure

```
├── backend/                 # Java Spring Boot backend
│   ├── src/main/java/      # Main application code
│   ├── src/test/java/      # Test code
│   ├── pom.xml             # Maven dependencies
│   └── Dockerfile          # Backend container
├── frontend/               # Angular frontend
│   ├── src/                # Frontend source code
│   ├── package.json        # NPM dependencies
│   └── Dockerfile          # Frontend container
├── database/               # Database initialization
├── nginx/                  # Reverse proxy configuration
├── docker-compose.yml      # Production containers
└── docker-compose.dev.yml  # Development database
```

## Development Setup

### Prerequisites

- Java 21
- Node.js 18+
- Docker and Docker Compose
- Maven 3.9+

### Quick Start

1. **Clone and setup environment**
   ```bash
   git clone <repository-url>
   cd ai-teaching-platform
   cp .env.example .env
   # Edit .env with your configuration
   ```

2. **Start development database**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

3. **Run backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. **Run frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```

5. **Access the application**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080

### Testing

**Backend Tests**
```bash
cd backend
mvn test
```

**Frontend Tests**
```bash
cd frontend
npm test
```

### Production Deployment

```bash
docker-compose up -d
```

## Configuration

### Environment Variables

- `OPENAI_API_KEY`: Your OpenAI API key
- `JWT_SECRET`: Secret key for JWT token signing
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

### Database

The application uses PostgreSQL in production and H2 for testing. Database schema is managed by JPA/Hibernate.

## Testing Strategy

The project uses a dual testing approach:

- **Unit Tests**: Specific examples and integration testing
- **Property-Based Tests**: Universal properties across all inputs
  - Backend: QuickCheck for Java (100+ iterations per property)
  - Frontend: fast-check for TypeScript (100+ iterations per property)

## API Documentation

API documentation will be available at `/swagger-ui.html` when the backend is running.