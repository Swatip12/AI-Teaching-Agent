# AI Teaching Platform Design Document

## Overview

The AI Teaching Platform is a full-stack web application that provides personalized, step-by-step programming education from beginner to job-ready level. The system uses OpenAI for intelligent tutoring, Java Spring Boot for the backend API, and Angular for the responsive frontend interface. The platform emphasizes patient, mentor-style teaching with interactive coding exercises and comprehensive progress tracking.

## Architecture

### Technology Stack
- **Frontend**: Angular 17+ with TypeScript
- **Backend**: Java 21 with Spring Boot 3.x
- **AI Integration**: OpenAI GPT-4 API for intelligent tutoring
- **Database**: PostgreSQL for user data and progress tracking
- **Code Execution**: Docker containers for safe code execution
- **Authentication**: JWT tokens with Spring Security
- **Deployment**: Docker containers with nginx reverse proxy

### System Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Angular SPA   │────│  Java Backend   │────│   PostgreSQL    │
│   (Frontend)    │    │  (Spring Boot)  │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       
         │                       │                       
         │              ┌─────────────────┐              
         │              │   OpenAI API    │              
         │              │  (AI Tutoring)  │              
         │              └─────────────────┘              
         │                       │                       
         │              ┌─────────────────┐              
         └──────────────│ Docker Runtime  │              
                        │ (Code Execution)│              
                        └─────────────────┘              
```

## Components and Interfaces

### Frontend Components (Angular)

#### Core Components
- **AppComponent**: Main application shell with navigation
- **DashboardComponent**: Student progress overview and subject selection
- **LessonComponent**: Interactive lesson delivery with step-by-step content
- **CodeEditorComponent**: Integrated code editor with syntax highlighting
- **ProgressComponent**: Visual progress tracking and achievements
- **ProfileComponent**: Student preferences and learning analytics

#### Services
- **AuthService**: JWT authentication and user session management
- **LessonService**: Lesson content delivery and progress tracking
- **AITutorService**: Communication with OpenAI through backend API
- **CodeExecutionService**: Code compilation and execution requests
- **ProgressService**: Student progress data management

### Backend Components (Java Spring Boot)

#### Controllers
- **AuthController**: User registration, login, and JWT token management
- **LessonController**: Lesson content CRUD operations and delivery
- **ProgressController**: Student progress tracking and analytics
- **AITutorController**: OpenAI integration for personalized tutoring
- **CodeExecutionController**: Safe code execution in Docker containers

#### Services
- **UserService**: User management and authentication logic
- **LessonService**: Lesson content management and sequencing
- **AITutorService**: OpenAI API integration and prompt engineering
- **ProgressTrackingService**: Student progress calculation and persistence
- **CodeExecutionService**: Docker-based code execution with security

#### Repositories
- **UserRepository**: User data persistence with JPA
- **LessonRepository**: Lesson content and metadata storage
- **ProgressRepository**: Student progress and completion tracking
- **SubjectRepository**: Subject and curriculum management

## Data Models

### User Entity
```java
@Entity
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private UserPreferences preferences;
}
```

### Lesson Entity
```java
@Entity
public class Lesson {
    private Long id;
    private String title;
    private String subject;
    private Integer sequenceOrder;
    private String content;
    private List<CheckpointQuestion> checkpointQuestions;
    private List<PracticeQuestion> practiceQuestions;
    private String difficulty;
}
```

### Progress Entity
```java
@Entity
public class Progress {
    private Long id;
    private Long userId;
    private Long lessonId;
    private ProgressStatus status;
    private Integer score;
    private LocalDateTime completedAt;
    private Integer attemptsCount;
}
```

### AI Conversation Entity
```java
@Entity
public class AIConversation {
    private Long id;
    private Long userId;
    private Long lessonId;
    private String studentMessage;
    private String aiResponse;
    private LocalDateTime timestamp;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

<function_calls>
<invoke name="prework">
<parameter name="featureName">ai-teaching-platform

### Property Reflection

After reviewing all testable properties from the prework analysis, several can be consolidated to eliminate redundancy:

**Consolidations:**
- Properties 1.4 and 1.5 (AI response behavior) can be combined into a comprehensive AI adaptation property
- Properties 2.1, 2.2, 2.3 (lesson structure) can be combined into a single lesson format property  
- Properties 3.1, 3.2, 3.3, 3.4, 3.5 (feedback behavior) can be consolidated into comprehensive feedback properties
- Properties 4.1, 4.2, 4.3, 4.4, 4.5 (progress tracking) can be combined into progress management properties
- Properties 6.1, 6.2, 6.3, 6.4, 6.5 (code interaction) can be consolidated into code execution properties
- Properties 7.1, 7.2, 7.3, 7.4, 7.5 (personalization) can be combined into adaptive learning properties
- Properties 8.1, 8.2, 8.3, 8.4, 8.5 (platform reliability) can be consolidated into system reliability properties

This consolidation reduces 30+ individual properties to 12 comprehensive properties that provide unique validation value without redundancy.

### Core Properties

**Property 1: Subject accessibility for beginners**
*For any* new student account, accessing the platform should display all available subjects without requiring prior knowledge indicators or prerequisites
**Validates: Requirements 1.1**

**Property 2: Subject progression starts at basics**
*For any* subject selection, the first lesson should be marked as beginner level with no prerequisites
**Validates: Requirements 1.2**

**Property 3: AI adaptive response behavior**
*For any* student confusion signal or struggle indicator, the AI should provide different, simpler explanations and encouraging feedback
**Validates: Requirements 1.4, 1.5**

**Property 4: Consistent lesson structure**
*For any* lesson, the content should follow the sequence: simple explanation → real-life example → technical example → exactly one checkpoint question → 2-3 practice questions
**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

**Property 5: Immediate feedback provision**
*For any* student response to questions or exercises, the system should provide immediate, contextual feedback with explanations
**Validates: Requirements 3.1, 3.3**

**Property 6: Error-specific guidance**
*For any* common mistake or error, the system should provide specific explanations about typical beginner errors and additional support
**Validates: Requirements 3.2, 3.4**

**Property 7: Success reinforcement**
*For any* successful completion or correct answer, the system should provide encouraging feedback
**Validates: Requirements 3.5**

**Property 8: Progress persistence and restoration**
*For any* lesson completion or student return, the system should accurately record progress and restore the student's last position
**Validates: Requirements 4.1, 4.3**

**Property 9: Progress-based unlocking**
*For any* progress update, the system should unlock exactly the next appropriate lesson and celebrate milestone achievements
**Validates: Requirements 4.4, 4.5**

**Property 10: Subject isolation and paths**
*For any* subject switch, the system should maintain separate progress tracking and display appropriate learning paths
**Validates: Requirements 5.2, 5.3**

**Property 11: Prerequisite enforcement**
*For any* lesson with prerequisites, the system should enforce proper learning sequence before allowing access
**Validates: Requirements 5.4**

**Property 12: Interactive code execution**
*For any* coding lesson, the system should provide an interactive editor that executes code immediately, handles errors gracefully, validates solutions automatically, and provides progressive hints
**Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**

**Property 13: Adaptive personalization**
*For any* demonstrated learning pattern or preference change, the system should adapt pacing, teaching approach, and provide additional practice or advanced challenges accordingly
**Validates: Requirements 7.1, 7.2, 7.4, 7.5**

**Property 14: Settings persistence**
*For any* login session, the system should restore all personalized settings and preferences
**Validates: Requirements 7.3**

**Property 15: Cross-device reliability**
*For any* device switch or network issue, the system should maintain functionality, synchronize progress, handle connectivity problems gracefully, and provide responsive design
**Validates: Requirements 8.1, 8.2, 8.3, 8.4**

**Property 16: Data persistence reliability**
*For any* data storage operation, the system should persist user information reliably without data loss
**Validates: Requirements 8.5**

## Error Handling

### Frontend Error Handling
- **Network Errors**: Retry mechanisms with exponential backoff for API calls
- **Authentication Errors**: Automatic token refresh and graceful logout
- **Validation Errors**: Real-time form validation with clear error messages
- **Code Execution Errors**: Syntax highlighting and helpful error explanations

### Backend Error Handling
- **Database Errors**: Connection pooling with automatic retry and failover
- **OpenAI API Errors**: Rate limiting, timeout handling, and fallback responses
- **Code Execution Errors**: Sandboxed execution with resource limits and timeout
- **Authentication Errors**: JWT validation with proper error responses

### AI Integration Error Handling
- **API Rate Limits**: Request queuing and intelligent retry strategies
- **Response Validation**: Content filtering and appropriateness checking
- **Fallback Mechanisms**: Pre-written responses for common scenarios when AI is unavailable

## Testing Strategy

### Dual Testing Approach
The platform requires both unit testing and property-based testing for comprehensive coverage:

**Unit Tests**: Verify specific examples, edge cases, and integration points between components. Unit tests catch concrete bugs and validate specific scenarios.

**Property-Based Tests**: Verify universal properties that should hold across all inputs using generated test data. Property tests verify general correctness and catch edge cases that might be missed in unit testing.

### Property-Based Testing Requirements
- **Library**: Use QuickCheck for Java (backend) and fast-check for TypeScript (frontend)
- **Iterations**: Configure each property-based test to run minimum 100 iterations
- **Tagging**: Each property-based test must include a comment with format: `**Feature: ai-teaching-platform, Property {number}: {property_text}**`
- **Implementation**: Each correctness property must be implemented by exactly one property-based test

### Unit Testing Requirements
- **Backend**: JUnit 5 with Spring Boot Test for integration testing
- **Frontend**: Jasmine and Karma for Angular component testing
- **Coverage**: Focus on critical business logic, API endpoints, and user interactions
- **Integration**: Test component interactions and data flow between services

### Testing Focus Areas
- **AI Response Quality**: Validate that AI responses meet educational standards
- **Progress Tracking Accuracy**: Ensure progress calculations are correct
- **Code Execution Security**: Verify sandboxed execution prevents security issues
- **Cross-Device Synchronization**: Test data consistency across multiple devices
- **Performance**: Load testing for concurrent users and large lesson content