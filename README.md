# tyk-webapp

Skeleton Hyper-Website game (deploy)

A lightweight skeleton for a web-based game / hyper-website that combines a Java backend with a HTML/JavaScript frontend. This repository provides a starting point for a deployable full-stack webapp — backend services (Java), a static frontend (HTML/JS), and optional Docker-based deployment. Use this repository as a template to bootstrap a playable/interactive web experience and adapt it to your game's needs.

---

Table of contents
- [Repository overview](#repository-overview)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Getting started (development)](#getting-started-development)
  - [Prerequisites](#prerequisites)
  - [Run backend](#run-backend)
  - [Run frontend](#run-frontend)
  - [Run with Docker / Docker Compose](#run-with-docker--docker-compose)
- [Build for production](#build-for-production)
- [Configuration](#configuration)
- [Testing](#testing)
- [Deployment notes](#deployment-notes)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

Repository overview

This project is a skeleton "hyper-website game" scaffold. It separates responsibilities into:
- A Java-based backend (APIs, game state, persistence, auth glue)
- A static frontend (HTML + JavaScript) that consumes the backend
- Optional deployment artifacts (Docker, examples)

Because this is a skeleton project, the repository intentionally keeps implementation details minimal and easy to adapt.

Tech stack

- Backend: Java (37.8% of repo)
  - Use any JVM framework you prefer (Spring Boot, Micronaut, Quarkus, plain servlet, etc.)
- Frontend: HTML + JavaScript (≈56.5% combined)
  - Static site assets, small client-side logic
- Documentation / diagrams: Mermaid
- Other: small utility files and configuration

Architecture

A simple component diagram:

```mermaid
flowchart LR
  Browser[Browser (HTML / JS)]
  Frontend[Static Frontend Assets]
  Backend[Java Backend API]
  DB[(Database)]
  Browser -->|requests / websockets| Backend
  Browser -->|loads assets| Frontend
  Backend -->|persistence| DB
  subgraph "Build & Deploy"
    CI[CI / Build]
    Docker[Docker Image]
    CI --> Docker -->|deploy| Prod[Production]
  end
```

Getting started (development)

Prerequisites

- Java 17+ (or the Java version your project uses)
- A build tool for Java: Maven or Gradle (instructions show both)
- Node.js and npm (if you want to run / build frontend tooling)
- Docker & Docker Compose (optional, for containerized runs)

Clone the repo

```bash
git clone https://github.com/Nobodywinsbutme/tyk-webapp.git
cd tyk-webapp
```

Run backend

The exact command depends on how the Java project is configured. If you use Maven:

```bash
# from repo root (where pom.xml lives)
mvn clean package
java -jar target/<your-backend-jar>.jar
```

If you use Gradle:

```bash
./gradlew clean build
java -jar build/libs/<your-backend-jar>.jar
```

Common tips
- If you use Spring Boot, replace `<your-backend-jar>.jar` with the generated fat jar (for example `tyk-webapp-0.0.1-SNAPSHOT.jar`).
- Check `src/main/resources/application.properties` or `application.yml` (or your framework config) for server port and DB connection values.

Run frontend

If the frontend is a plain static folder (e.g., `src/main/resources/static`, `public/`, or `frontend/dist`), you can open `index.html` in a browser or serve it with a simple static server.

To run a simple static server with Node:

```bash
# install a lightweight static server globally (optional)
npm install -g serve

# serve the directory containing index.html
serve ./frontend
# or
serve ./public
```

If you have a build step (webpack / vite / parcel), run:

```bash
cd frontend
npm install
npm run dev    # or npm run serve / npm start depending on package.json
```

If the frontend calls APIs on the backend during development, ensure CORS is enabled on the backend or use a dev proxy.

Run with Docker / Docker Compose

A sample docker-compose snippet (example only — adapt to your project layout):

```yaml
version: "3.8"
services:
  web:
    build: ./backend
    image: tyk-webapp-backend:local
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=local
      - DATABASE_URL=jdbc:postgresql://db:5432/tykdb
    depends_on:
      - db

  frontend:
    build: ./frontend
    image: tyk-webapp-frontend:local
    ports:
      - "3000:3000"

  db:
    image: postgres:15
    environment:
      - POSTGRES_PASSWORD=example
      - POSTGRES_DB=tykdb
    volumes:
      - db-data:/var/lib/postgresql/data

volumes:
  db-data:
```

Build for production

- Backend: produce an optimized, fat JAR (or native image if using GraalVM/Quarkus).
- Frontend: produce a minified static bundle (npm run build), then serve it from your backend or a CDN/static host.

Example backend build
```bash
# Maven
mvn -Pprod clean package

# Gradle
./gradlew clean assemble -Pprod
```

Example frontend build
```bash
cd frontend
npm ci
npm run build
# copy the output to the backend static resource directory or deploy to a static host
```

Configuration

Recommended environment variables / config keys (examples — adapt to your stack):
- SERVER_PORT (default 8080)
- DATABASE_URL (JDBC URL)
- DATABASE_USER / DATABASE_PASSWORD
- JWT_SECRET (if using token auth)
- LOG_LEVEL
- CORS_ALLOWED_ORIGINS

Place sensitive values in environment variables or a secrets manager. Do not commit secrets to source control.

Testing

- Backend: use your Java testing stack (JUnit, Mockito).
  - Maven: mvn test
  - Gradle: ./gradlew test
- Frontend: run unit tests with your JS framework (jest, vitest, etc.): npm test
- Integration: consider spinning up a test database (Docker) and running API integration tests.

Deployment notes

- Prefer containerized images for reproducible deployments.
- Use a managed database in production and set proper backups.
- Enable HTTPS / TLS in production (reverse proxy like nginx, load balancer).
- Use CI/CD to build, test, and publish images to a registry.

Troubleshooting

- Backend won't start: check logs, port conflicts, missing DB or incorrect JDBC URL.
- Frontend fails to call backend: check browser console and network tab, verify CORS and correct API base URL.
- Docker issues: ensure correct context paths in Dockerfile and compose file.

Contributing

Contributions are welcome. A simple workflow:
1. Fork the repo.
2. Create a feature branch: git checkout -b feat/description
3. Implement your change with clear commits.
4. Add or update tests.
5. Open a pull request describing the change.

Please follow these guidelines:
- Keep changes focused and small.
- Write tests for new behaviour.
- Update README and docs when adding or changing features.

If you want help choosing a first task, open an issue and tag it "good first issue".

License

This project is currently provided as-is. If you want to add an explicit license, consider adding a LICENSE file (for example, MIT):

MIT License
Copyright (c) 2025 Nobodywinsbutme

(Replace with the license of your choice.)

Contact

- Repo: https://github.com/Nobodywinsbutme/tyk-webapp
- Author / maintainer: Nobodywinsbutme

Acknowledgments

- This repository is a skeleton starter — adapt freely for your game or interactive web experiences.

---