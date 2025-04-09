-- Database schema for Kenyatta CAT System
USE kenyatta_cat_system;

-- Create users table
CREATE TABLE users (
    user_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Create units table
CREATE TABLE units (
    unit_code VARCHAR(10) PRIMARY KEY,
    unit_name VARCHAR(100) NOT NULL,
    description TEXT,
    department VARCHAR(100),
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Create topics table
CREATE TABLE topics (
    topic_id VARCHAR(36) PRIMARY KEY,
    topic_name VARCHAR(100) NOT NULL,
    description TEXT,
    unit_code VARCHAR(10) NOT NULL,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_code) REFERENCES units(unit_code),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Create questions table
CREATE TABLE questions (
    question_id VARCHAR(36) PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    options TEXT,
    correct_answers TEXT NOT NULL,
    marks INT NOT NULL,
    unit_code VARCHAR(10) NOT NULL,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved BOOLEAN DEFAULT FALSE,
    feedback TEXT,
    topic VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    FOREIGN KEY (unit_code) REFERENCES units(unit_code),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Create assessments table
CREATE TABLE assessments (
    assessment_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    unit_code VARCHAR(10) NOT NULL,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_date_time TIMESTAMP,
    end_date_time TIMESTAMP,
    duration_minutes INT NOT NULL,
    is_practice BOOLEAN DEFAULT FALSE,
    allow_offline_attempt BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (unit_code) REFERENCES units(unit_code),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Create assessment_questions table (junction table)
CREATE TABLE assessment_questions (
    assessment_id VARCHAR(36),
    question_id VARCHAR(36),
    question_order INT NOT NULL,
    PRIMARY KEY (assessment_id, question_id),
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Create student_assessments table
CREATE TABLE student_assessments (
    student_assessment_id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36),
    assessment_id VARCHAR(36),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    score DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'NOT_STARTED',
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE
);

-- Create student_answers table
CREATE TABLE student_answers (
    answer_id VARCHAR(36) PRIMARY KEY,
    student_assessment_id VARCHAR(36),
    question_id VARCHAR(36),
    answer_text TEXT,
    is_correct BOOLEAN,
    marks_awarded DECIMAL(5,2),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_assessment_id) REFERENCES student_assessments(student_assessment_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

-- Create student_unit_enrollments table
CREATE TABLE student_unit_enrollments (
    student_id VARCHAR(36),
    unit_code VARCHAR(10),
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    PRIMARY KEY (student_id, unit_code),
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (unit_code) REFERENCES units(unit_code)
);

-- Create practice_tests table
CREATE TABLE practice_tests (
    practice_test_id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36),
    title VARCHAR(100) NOT NULL,
    unit_code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INT NOT NULL,
    difficulty VARCHAR(20),
    question_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'NOT_STARTED',
    score DECIMAL(5,2) DEFAULT 0,
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (unit_code) REFERENCES units(unit_code)
);

-- Create practice_test_questions table
CREATE TABLE practice_test_questions (
    practice_test_id VARCHAR(36),
    question_id VARCHAR(36),
    question_order INT NOT NULL,
    PRIMARY KEY (practice_test_id, question_id),
    FOREIGN KEY (practice_test_id) REFERENCES practice_tests(practice_test_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

-- Create notifications table
CREATE TABLE notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    notification_type VARCHAR(20),
    related_id VARCHAR(36),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create a default admin user (password: admin123)
INSERT INTO users (user_id, username, password, email, full_name, role)
VALUES ('e2b78635-a080-46cc-81a8-69025e337415', 'admin', '$2a$10$dJmcmxEzxGvjjLapp0.pEuQRnqOZFYUbQXT5eHI1Y0ygQrFLqnRFq', 'admin@kenyatta.ac.ke', 'System Administrator', 'ADMIN');

-- Create a default lecturer (password: lecturer123)
INSERT INTO users (user_id, username, password, email, full_name, role, department)
VALUES ('f3c89b47-d123-45a6-b789-0123456789ab', 'lecturer', '$2a$10$hxLzHvnYy3cAWlB9jdFQR.ZSKfnPsUPo4hQn0JBbMXlQP3pWO9/Uu', 'lecturer@kenyatta.ac.ke', 'John Lecturer', 'LECTURER', 'Computer Science');

-- Create a default student (password: student123)
INSERT INTO users (user_id, username, password, email, full_name, role)
VALUES ('a1b2c3d4-e5f6-47a8-b9c0-1234567890ab', 'student', '$2a$10$8K1p/a7OlZ1f5K.Sh3YYVeXLFyAPDyUPLEHcI.HYDlXQqFqIkqxfS', 'student@kenyatta.ac.ke', 'Jane Student', 'STUDENT');

-- Create a sample unit
INSERT INTO units (unit_code, unit_name, description, department, created_by)
VALUES ('SCO100', 'Introduction to Computer Science', 'Foundational course covering basic computer science concepts', 'Computer Science', 'e2b78635-a080-46cc-81a8-69025e337415');

-- Create sample topics for SCO100
INSERT INTO topics (topic_id, topic_name, description, unit_code, created_by)
VALUES 
('t1', 'Digital Electronics', 'Study of electronic circuits that handle digital signals', 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415'),
('t2', 'Basic Electrical Concepts', 'Fundamental concepts of electricity and electrical circuits', 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415'),
('t3', 'Logic Gates', 'Study of electronic circuits that perform logical operations', 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415'),
('t4', 'Semiconductor Devices', 'Study of electronic components made from semiconductor materials', 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415'),
('t5', 'Logic Design & Combinational Circuits', 'Design and analysis of digital circuits', 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415');

-- Enroll the student in SCO100
INSERT INTO student_unit_enrollments (student_id, unit_code)
VALUES ('a1b2c3d4-e5f6-47a8-b9c0-1234567890ab', 'SCO100');
