-- V1__Create_initial_schema.sql
-- Initial database schema for AI Teaching Platform

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    
    -- User preferences (embedded)
    learning_pace VARCHAR(20) DEFAULT 'NORMAL',
    explanation_style VARCHAR(20) DEFAULT 'BALANCED',
    show_hints BOOLEAN DEFAULT TRUE,
    celebration_enabled BOOLEAN DEFAULT TRUE,
    dark_mode BOOLEAN DEFAULT FALSE,
    notification_enabled BOOLEAN DEFAULT TRUE
);

-- Create indexes for users table
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

-- Create lessons table
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    sequence_order INTEGER NOT NULL,
    content TEXT,
    objectives TEXT,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    estimated_duration_minutes INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for lessons table
CREATE INDEX idx_lesson_subject ON lessons(subject);
CREATE INDEX idx_lesson_sequence ON lessons(subject, sequence_order);
CREATE INDEX idx_lesson_difficulty ON lessons(difficulty);

-- Create lesson prerequisites table
CREATE TABLE lesson_prerequisites (
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    prerequisite_lesson_id BIGINT NOT NULL,
    PRIMARY KEY (lesson_id, prerequisite_lesson_id)
);

-- Create checkpoint questions table
CREATE TABLE checkpoint_questions (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    explanation TEXT,
    question_type VARCHAR(20) NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    sequence_order INTEGER
);

-- Create index for checkpoint questions
CREATE INDEX idx_checkpoint_lesson ON checkpoint_questions(lesson_id);

-- Create practice questions table
CREATE TABLE practice_questions (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    expected_solution TEXT,
    hints TEXT,
    question_type VARCHAR(20) NOT NULL DEFAULT 'CODING',
    difficulty VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    sequence_order INTEGER,
    starter_code TEXT,
    test_cases TEXT
);

-- Create indexes for practice questions
CREATE INDEX idx_practice_lesson ON practice_questions(lesson_id);
CREATE INDEX idx_practice_difficulty ON practice_questions(difficulty);

-- Create progress table
CREATE TABLE progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    completion_percentage INTEGER DEFAULT 0 CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    score INTEGER CHECK (score >= 0 AND score <= 100),
    attempts_count INTEGER DEFAULT 0,
    time_spent_minutes INTEGER DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_checkpoint_passed INTEGER,
    practice_questions_completed INTEGER DEFAULT 0,
    
    CONSTRAINT uk_user_lesson UNIQUE (user_id, lesson_id)
);

-- Create indexes for progress table
CREATE INDEX idx_progress_user ON progress(user_id);
CREATE INDEX idx_progress_lesson ON progress(lesson_id);
CREATE INDEX idx_progress_user_lesson ON progress(user_id, lesson_id);
CREATE INDEX idx_progress_status ON progress(status);

-- Create AI conversations table
CREATE TABLE ai_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE SET NULL,
    student_message TEXT NOT NULL,
    ai_response TEXT,
    conversation_type VARCHAR(20) DEFAULT 'QUESTION',
    response_status VARCHAR(20) DEFAULT 'PENDING',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time_ms BIGINT,
    context_data TEXT,
    feedback_rating INTEGER CHECK (feedback_rating >= 1 AND feedback_rating <= 5),
    feedback_comment TEXT
);

-- Create indexes for AI conversations table
CREATE INDEX idx_conversation_user ON ai_conversations(user_id);
CREATE INDEX idx_conversation_lesson ON ai_conversations(lesson_id);
CREATE INDEX idx_conversation_timestamp ON ai_conversations(timestamp);
CREATE INDEX idx_conversation_user_lesson ON ai_conversations(user_id, lesson_id);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to tables with updated_at columns
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lessons_updated_at BEFORE UPDATE ON lessons
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_progress_updated_at BEFORE UPDATE ON progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();