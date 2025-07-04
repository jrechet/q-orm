# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus-based Java application demonstrating **dual ORM coexistence** with both Hibernate ORM (via Quarkus Panache) and EclipseLink JPA working together in the same application. The project showcases how two different ORM technologies can operate independently while sharing the same PostgreSQL database.

## Key Commands

### Quick Start
- `./startup.sh` - Start PostgreSQL and run the application in one command
- `docker-compose up -d` - Start PostgreSQL database only
- `./mvnw quarkus:dev` - Run application in development mode with live reload

### Development
- `./mvnw clean compile` - Clean and compile the project
- `./mvnw package` - Package the application (creates quarkus-run.jar)
- `./mvnw package -Dquarkus.package.jar.type=uber-jar` - Create an uber-jar

### Testing
- `./mvnw test` - Run all tests (both ORM systems)
- `./mvnw test -Dtest=GiftServiceTest` - Test Quarkus ORM (Panache) only
- `./mvnw test -Dtest=EclipseLinkStandaloneTest` - Test EclipseLink ORM only
- `./mvnw verify` - Run integration tests

### Database Management
- Database recreated from scratch on every application start
- `docker-compose down -v` - Stop database and remove volumes
- Access database: `psql -h localhost -p 5432 -U qorm -d qorm`

## Architecture

### Dual ORM System
This project demonstrates two ORM approaches working together:

1. **Quarkus ORM (Hibernate with Panache)** - `lab.quarkus.*`
   - Uses active record pattern
   - Managed by Quarkus dependency injection
   - Handles `Gift` entities in `gifts` table
   - Repository pattern with PanacheRepository

2. **EclipseLink ORM** - `lab.eclipselink.*`
   - Uses traditional JPA approach
   - Manual EntityManagerFactory management
   - Handles `Shop` entities in `shops` table
   - Repository pattern with manual transaction management

### Core Technologies
- **Quarkus 3.24.2**: Supersonic Subatomic Java Framework
- **Java 17**: Target runtime version (adjusted for compatibility)
- **Hibernate ORM with Panache**: Quarkus-managed ORM for Gift entities
- **EclipseLink 4.0.7**: Standalone JPA provider for Shop entities
- **PostgreSQL**: Shared database for both ORM systems
- **Testcontainers**: For EclipseLink integration testing

### Package Structure
```
lab/
├── quarkus/           # Quarkus ORM implementation
│   ├── entity/        # Gift entity (extends PanacheEntity)
│   ├── repository/    # GiftRepository (PanacheRepository)
│   └── service/       # GiftService (CDI managed)
├── eclipselink/       # EclipseLink ORM implementation
│   ├── entity/        # Shop entity (plain JPA)
│   ├── repository/    # ShopRepository (manual EM)
│   └── service/       # ShopService (CDI managed)
└── MyEntity.java      # Legacy entity (can be removed)
```

### Configuration Files
- `application.properties`: Quarkus ORM configuration
- `META-INF/persistence.xml`: EclipseLink persistence unit configuration
- `import.sql`: Sample data for Quarkus ORM (gifts table)
- `docker-compose.yml`: PostgreSQL database setup

## Development Notes

### Database Strategy
- Both ORM systems use the same PostgreSQL database
- Different table prefixes: `gifts` (Quarkus) vs `shops` (EclipseLink)
- Database recreated on every startup for clean state
- Quarkus ORM uses `import.sql` for sample data
- EclipseLink can be initialized programmatically via service calls

### ORM Coexistence Configuration
- Quarkus ORM configured via `application.properties`
- `quarkus.hibernate-orm.persistence-xml.ignore=true` prevents conflicts
- EclipseLink uses separate `persistence.xml` with `RESOURCE_LOCAL` transactions
- Each ORM manages its own entities independently

### Cache Management Strategy
**Dual ORM Cache Coordination:**
- **Hibernate L1 + L2 Cache**: Caffeine-based with 5-minute expiry, 1000 entities max
- **EclipseLink L1 + L2 Cache**: SoftWeak cache with 5-minute expiry, 500 entities max
- **Cache Isolation**: No coordination between ORM systems (different tables)
- **Centralized Management**: `DualOrmCacheManager` for cache operations and monitoring

**Cache Configuration:**
- Hibernate: `@Cacheable` + `@Cache(READ_WRITE)` annotations
- EclipseLink: `@Cache(SOFT_WEAK)` annotations with custom expiry
- Connection pools coordinated: Quarkus (2-20), EclipseLink (2-10)
- Metrics and monitoring via Micrometer/Prometheus

### Testing Strategy
- **GiftServiceTest**: Quarkus Test framework for Gift CRUD operations
- **EclipseLinkStandaloneTest**: Testcontainers for Shop CRUD operations
- **DualOrmCacheTest**: Cache behavior and isolation testing
- Tests demonstrate both ORM systems working correctly
- Each test suite runs in isolation to avoid conflicts
- Cache metrics and statistics validation

### Key Design Decisions
1. **Separate packages**: Clear separation between ORM technologies
2. **Manual transaction management**: EclipseLink uses programmatic transactions
3. **Different entity patterns**: Active record vs traditional JPA
4. **Independent testing**: Each ORM tested separately to avoid framework conflicts
5. **Shared database**: Both systems use same PostgreSQL instance but different tables

## Troubleshooting

### Common Issues
- **EclipseLink not found**: Ensure persistence.xml uses `RESOURCE_LOCAL` transactions
- **CDI conflicts**: EclipseLink components use manual EntityManagerFactory creation
- **Test failures**: Run tests separately if experiencing CDI conflicts between ORM systems
- **Database connection**: Ensure PostgreSQL is running via `docker-compose up -d`

### Development Tips
- Use different service classes for each ORM to avoid injection conflicts
- EclipseLink requires manual transaction management in this setup
- Quarkus Dev UI available at http://localhost:8080/q/dev/ when running
- Monitor SQL logs to see both ORM systems in action