# TeamUp Quick Start Guide

This guide will help you get TeamUp up and running quickly.

## Prerequisites Installation

### 1. Install Java (Required)
```bash
# Fedora/RHEL
sudo dnf install java-17-openjdk java-17-openjdk-devel

# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# macOS
brew install openjdk@17
```

### 2. Install Maven (Required)
```bash
# Fedora/RHEL
sudo dnf install maven

# Ubuntu/Debian
sudo apt install maven

# macOS
brew install maven
```

### 3. Install PostgreSQL (Required)
```bash
# Fedora/RHEL
sudo dnf install postgresql postgresql-server
sudo postgresql-setup --initdb
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql

# macOS
brew install postgresql
brew services start postgresql
```

## Database Setup

1. **Access PostgreSQL**:
```bash
sudo -u postgres psql
```

2. **Create database and user**:
```sql
CREATE DATABASE teamup_db;
CREATE USER teamup_user WITH PASSWORD 'teamup_pass';
GRANT ALL PRIVILEGES ON DATABASE teamup_db TO teamup_user;
\q
```

3. **Update application.properties** (if you used different credentials):
```properties
spring.datasource.username=teamup_user
spring.datasource.password=teamup_pass
```

## Build and Run

### Option 1: Using the Setup Script (Recommended)
```bash
./setup.sh
```

### Option 2: Manual Setup
```bash
# Install dependencies
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

### Option 3: Run as JAR
```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/TeamUp-0.0.1-SNAPSHOT.jar
```

## Access the Application

1. Open your browser and navigate to: **http://localhost:8080**
2. You'll be redirected to the login page
3. Click **"Sign Up"** to create your account
4. Fill in your details and register
5. You'll be automatically logged in and redirected to your workspace

## First Steps

### Create Your First Workspace
1. Click **"+ New Workspace"** in the sidebar
2. Enter a workspace name (e.g., "My Team")
3. The workspace will be created with a default **#general** channel

### Start Chatting
1. Click on the **#general** channel
2. Type a message in the input box at the bottom
3. Press **Enter** or click **"Send"**
4. Your message will appear in real-time

### Invite Team Members
1. Click on your workspace
2. Go to workspace settings
3. Generate an invitation link
4. Share the link with team members
5. They can use it to join your workspace

### Create Additional Channels
1. Click the **"+"** button next to "Channels"
2. Enter channel name and description
3. Choose **Public** or **Private**
4. Click **"Create"**

### Start a Video Meeting (Optional - Requires LiveKit)
1. Select a channel
2. Click **"📹 Start Meeting"** in the header
3. A new window will open with the video meeting
4. Allow camera and microphone access
5. Share the meeting link with participants

## LiveKit Setup (Optional - For Video Meetings)

### Install LiveKit Server
```bash
# Download LiveKit
wget https://github.com/livekit/livekit/releases/latest/download/livekit-server_linux_amd64

# Make executable
chmod +x livekit-server_linux_amd64

# Run in dev mode
./livekit-server_linux_amd64 --dev
```

### Update Configuration
Edit `src/main/resources/application.properties`:
```properties
livekit.api.key=devkey
livekit.api.secret=secret
livekit.url=ws://localhost:7880
```

## Configuration Options

### Change Server Port
```properties
server.port=9090
```

### Change JWT Secret (Important for Production!)
```properties
jwt.secret=YourVeryLongAndSecureSecretKeyHere123456789
```

### File Upload Settings
```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

## Theme Toggle

Click the **🌙** button in the sidebar to switch between light and dark themes.

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
sudo lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change the port in application.properties
```

### Database Connection Error
- Ensure PostgreSQL is running: `sudo systemctl status postgresql`
- Verify database exists: `sudo -u postgres psql -l`
- Check credentials in `application.properties`

### Maven Build Fails
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -DskipTests
```

### WebSocket Connection Issues
- Check firewall settings
- Ensure port 8080 is accessible
- Try disabling browser extensions

## API Testing with cURL

### Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }'
```

### Create Workspace (Replace TOKEN with JWT from login)
```bash
curl -X POST "http://localhost:8080/api/workspaces?userId=1" \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Workspace",
    "description": "Team collaboration space"
  }'
```

## Development Mode

For development with hot reload:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## Production Deployment

1. **Build production JAR**:
```bash
mvn clean package -DskipTests
```

2. **Set environment variables**:
```bash
export JWT_SECRET="your-production-secret-key"
export DB_URL="jdbc:postgresql://prod-db:5432/teamup_db"
export DB_USERNAME="prod_user"
export DB_PASSWORD="prod_password"
```

3. **Run with production profile**:
```bash
java -jar target/TeamUp-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Next Steps

- Explore the API documentation in README.md
- Customize the UI in `src/main/resources/static/css/style.css`
- Add more features by extending the services
- Set up email notifications for invitations
- Implement file storage with AWS S3

## Support

- Check the main README.md for detailed documentation
- Review the code in `src/main/java/com/codehuntspk/teamup/`
- Open an issue on GitHub for bugs or feature requests

---

**Happy Collaborating with TeamUp! 🚀**

