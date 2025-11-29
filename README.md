# TeamUp - Team Collaboration Platform

A comprehensive team collaboration platform built with Spring Boot, PostgreSQL, WebSocket, and LiveKit for real-time communication and video meetings.

## Features

### 1. User Authentication & Authorization
- User registration with email and password
- Login with JWT token generation
- Spring Security configuration with role-based access (USER, ADMIN)
- Password encryption using BCrypt
- Session management with JWT

### 2. Workspace & Channel Management
- Create/edit/delete workspaces (teams)
- Create public and private channels within workspaces
- User invitation to workspaces via invite links
- Channel membership management
- Direct messaging between users

### 3. Real-time Chat System
- WebSocket connection using STOMP protocol
- Send/receive text messages in real-time
- Message persistence in PostgreSQL database
- Display message history when joining channel
- Typing indicators
- Online/offline user status
- Message timestamps and read receipts
- File attachment support (images, documents)

### 4. Video Meeting Integration (LiveKit)
- Generate LiveKit room tokens from backend
- Create meeting rooms for channels
- Join/leave meeting functionality
- Display active participants
- Screen sharing support
- Meeting invite links

### 5. User Interface (Thymeleaf + HTMX)
- Responsive layout with sidebar navigation
- Channel list with unread message indicators
- Message thread view
- User profile page
- Settings page
- Dark/light theme toggle

## Technology Stack

- **Backend**: Spring Boot 4.0.0
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Real-time**: WebSocket/STOMP
- **Video**: LiveKit Server SDK
- **Frontend**: Thymeleaf, HTMX, Vanilla JavaScript
- **Build Tool**: Maven

## Prerequisites

- Java 25 or higher
- PostgreSQL 12 or higher
- Maven 3.6+
- LiveKit server (optional for video meetings)

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE teamup_db;
```

### 2. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/teamup_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password

# JWT Configuration
jwt.secret=ChangeThisToYourOwnSecretKeyAtLeast256BitsLong123456789

# LiveKit Configuration (if using video meetings)
livekit.api.key=your-livekit-api-key
livekit.api.secret=your-livekit-api-secret
livekit.url=ws://localhost:7880
```

### 3. Install Dependencies

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Project Structure

```
src/main/java/com/codehuntspk/teamup/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   └── WebSocketConfig.java
├── controller/          # REST and Web Controllers
│   ├── AuthController.java
│   ├── ChatController.java
│   ├── MeetingController.java
│   ├── WebController.java
│   └── WorkspaceController.java
├── dto/                 # Data Transfer Objects
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── MessageDto.java
│   └── RegisterRequest.java
├── entity/              # JPA Entities
│   ├── Channel.java
│   ├── Invitation.java
│   ├── Meeting.java
│   ├── Message.java
│   ├── User.java
│   ├── Workspace.java
│   └── WorkspaceMember.java
├── repository/          # JPA Repositories
│   ├── ChannelRepository.java
│   ├── InvitationRepository.java
│   ├── MeetingRepository.java
│   ├── MessageRepository.java
│   ├── UserRepository.java
│   ├── WorkspaceMemberRepository.java
│   └── WorkspaceRepository.java
├── security/            # Security Components
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/             # Business Logic
│   ├── AuthService.java
│   ├── ChannelService.java
│   ├── InvitationService.java
│   ├── LiveKitService.java
│   ├── MeetingService.java
│   ├── MessageService.java
│   └── WorkspaceService.java
└── TeamUpApplication.java

src/main/resources/
├── static/
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── auth.js
│       ├── meeting.js
│       └── workspace.js
└── templates/
    ├── layout.html
    ├── login.html
    ├── meeting.html
    ├── register.html
    └── workspace.html
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Workspaces
- `POST /api/workspaces` - Create workspace
- `GET /api/workspaces/user/{userId}` - Get user's workspaces
- `GET /api/workspaces/{workspaceId}` - Get workspace details
- `PUT /api/workspaces/{workspaceId}` - Update workspace
- `DELETE /api/workspaces/{workspaceId}` - Delete workspace
- `GET /api/workspaces/{workspaceId}/members` - Get workspace members

### Messages
- `GET /api/messages/channel/{channelId}` - Get channel messages

### Meetings
- `POST /api/meetings/channel/{channelId}` - Create meeting
- `GET /api/meetings/{meetingId}/token` - Get meeting token
- `POST /api/meetings/{meetingId}/end` - End meeting
- `GET /api/meetings/{meetingId}` - Get meeting details

### WebSocket
- `/ws` - WebSocket endpoint
- `/app/chat/{channelId}` - Send message
- `/topic/channel/{channelId}` - Subscribe to channel messages

## Database Schema

### Users
- id, username, email, password, firstName, lastName, avatar, status, roles, createdAt, updatedAt

### Workspaces
- id, name, description, ownerId, imageUrl, createdAt, updatedAt

### Channels
- id, name, description, workspaceId, type, isDirectMessage, createdAt, updatedAt

### Messages
- id, channelId, senderId, content, attachmentUrl, attachmentType, timestamp, edited

### WorkspaceMembers
- id, workspaceId, userId, role, joinedAt

### Invitations
- id, workspaceId, inviterId, inviteeEmail, token, status, createdAt, expiresAt

### Meetings
- id, channelId, roomName, livekitRoomId, createdById, status, startedAt, endedAt

## Usage

1. **Register/Login**: Navigate to `http://localhost:8080` and create an account
2. **Create Workspace**: Click "New Workspace" to create your team workspace
3. **Create Channels**: Add channels for different topics or projects
4. **Chat**: Select a channel and start messaging in real-time
5. **Video Meetings**: Click "Start Meeting" to create a video room
6. **Invite Members**: Use invite links to add team members to your workspace

## LiveKit Setup (Optional)

For video meeting functionality, you need to run LiveKit server:

1. Download LiveKit server from https://livekit.io/
2. Run the server:
   ```bash
   livekit-server --dev
   ```
3. Update `application.properties` with your LiveKit credentials

## Security Notes

- Change the JWT secret in production
- Use environment variables for sensitive configuration
- Enable HTTPS in production
- Configure CORS properly for your domain
- Regular security updates for dependencies

## Future Enhancements

- [ ] File upload and storage (AWS S3 integration)
- [ ] Push notifications
- [ ] Email notifications
- [ ] Advanced user presence tracking
- [ ] Message reactions and threads
- [ ] Voice channels
- [ ] Screen recording
- [ ] Calendar integration
- [ ] Task management
- [ ] Analytics dashboard

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and questions, please open an issue on the GitHub repository.

