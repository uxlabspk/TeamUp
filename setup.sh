#!/bin/bash

echo "======================================"
echo "TeamUp Application Setup Script"
echo "======================================"
echo ""

# Check for Java
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "✓ Java found: $JAVA_VERSION"
else
    echo "✗ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check for Maven
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✓ Maven found: $MVN_VERSION"
else
    echo "✗ Maven not found. Installing..."
    echo "Please install Maven manually:"
    echo "  Fedora/RHEL: sudo dnf install maven"
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  macOS: brew install maven"
    exit 1
fi

# Check for PostgreSQL
echo "Checking PostgreSQL installation..."
if command -v psql &> /dev/null; then
    PG_VERSION=$(psql --version)
    echo "✓ PostgreSQL found: $PG_VERSION"
else
    echo "⚠ PostgreSQL not found. Please install PostgreSQL 12 or higher."
    echo "  Fedora/RHEL: sudo dnf install postgresql postgresql-server"
    echo "  Ubuntu/Debian: sudo apt install postgresql postgresql-contrib"
    echo "  macOS: brew install postgresql"
fi

echo ""
echo "======================================"
echo "Database Setup"
echo "======================================"
echo ""
echo "Please ensure PostgreSQL is running and create the database:"
echo ""
echo "  sudo -u postgres psql"
echo "  CREATE DATABASE teamup_db;"
echo "  CREATE USER teamup_user WITH PASSWORD 'teamup_pass';"
echo "  GRANT ALL PRIVILEGES ON DATABASE teamup_db TO teamup_user;"
echo "  \\q"
echo ""
read -p "Have you created the database? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Please create the database and run this script again."
    exit 1
fi

echo ""
echo "======================================"
echo "Building Application"
echo "======================================"
echo ""

# Build the application
echo "Running Maven clean install..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build successful!"
    echo ""
    echo "======================================"
    echo "Starting Application"
    echo "======================================"
    echo ""
    echo "To start the application, run:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Or use the compiled JAR:"
    echo "  java -jar target/TeamUp-0.0.1-SNAPSHOT.jar"
    echo ""
    echo "The application will be available at:"
    echo "  http://localhost:8080"
    echo ""
    echo "Default credentials for first user:"
    echo "  Register at: http://localhost:8080/register"
    echo ""
else
    echo "✗ Build failed. Please check the error messages above."
    exit 1
fi

