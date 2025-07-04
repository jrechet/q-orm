#!/bin/bash

echo "Starting q-orm application with dual ORM setup..."

# Start PostgreSQL database
echo "Starting PostgreSQL database..."
docker-compose up -d

# Wait for database to be ready
echo "Waiting for database to be ready..."
sleep 5

# Run the application
echo "Starting Quarkus application..."
./mvnw quarkus:dev