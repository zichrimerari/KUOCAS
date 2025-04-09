package book.javafx.kenyattacatsystem.utils;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database operations.
 * Handles connections, queries, and other database-related functionality.
 */
public class DatabaseUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());
    // MySQL connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kenyatta_cat_system";
    private static final String DB_USER = "root"; // Change to your MySQL username
    private static final String DB_PASSWORD = "Kasparov3634!"; // Password should be provided securely
    
    private static Connection connection = null;
    
    /**
     * Initializes the database by creating necessary tables if they don't exist.
     */
    public static void initializeDatabase() {
        try {
            // Ensure we have a connection
            getConnection();
            
            // Create tables if they don't exist
            createUserTable();
            createStudentTable();
            createLecturerTable();
            createAdminTable();
            createUnitTable();
            createEnrollmentTable();
            createStudentUnitEnrollmentsTable(); 
            createAssessmentTable();
            createQuestionTable();
            createAssessmentQuestionTable();
            createStudentAssessmentTable();
            createStudentResponseTable();
            createPracticeAssessmentTable();
            createReportsTable();
            createReportExecutionsTable();
            createSystemSettingsTable();
            createTopicsTable();
            createFocusViolationsTable();
            
            // Update existing tables with new columns if needed
            updateStudentAssessmentTable();
            
            LOGGER.info("Database initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
        }
    }
    
    /**
     * Gets a connection to the database.
     * If a connection already exists, returns the existing connection.
     * Otherwise, creates a new connection.
     *
     * @return A connection to the database
     * @throws SQLException If a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Open a connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                connection.setAutoCommit(true);
                
                LOGGER.info("Database connection established");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "JDBC Driver not found", e);
                throw new SQLException("JDBC Driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Closes the database connection.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }
    
    /**
     * Creates the user table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createUserTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id VARCHAR(50) PRIMARY KEY,"
                + "username VARCHAR(50) UNIQUE NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "full_name VARCHAR(100) NOT NULL,"
                + "email VARCHAR(100) UNIQUE NOT NULL,"
                + "role VARCHAR(20) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "last_login TIMESTAMP"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the student table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createStudentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students ("
                + "student_id VARCHAR(50) PRIMARY KEY,"
                + "user_id VARCHAR(50) NOT NULL,"
                + "program VARCHAR(100) NOT NULL,"
                + "year_of_study INT NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the lecturer table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createLecturerTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS lecturers ("
                + "staff_id VARCHAR(50) PRIMARY KEY,"
                + "user_id VARCHAR(50) NOT NULL,"
                + "department VARCHAR(100) NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the admin table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createAdminTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS admins ("
                + "admin_id VARCHAR(50) PRIMARY KEY,"
                + "user_id VARCHAR(50) NOT NULL,"
                + "department VARCHAR(100) NOT NULL,"
                + "access_level VARCHAR(20) NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the unit table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createUnitTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS units ("
                + "unit_code VARCHAR(20) PRIMARY KEY,"
                + "unit_name VARCHAR(100) NOT NULL,"
                + "department VARCHAR(100) NOT NULL,"
                + "credit_hours INT NOT NULL,"
                + "description TEXT,"
                + "lecturer_id VARCHAR(50),"
                + "FOREIGN KEY (lecturer_id) REFERENCES lecturers(staff_id)"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the enrollment table if it doesn't exist.
     * This table tracks which students are enrolled in which units.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createEnrollmentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS enrollments ("
                + "student_id VARCHAR(50),"
                + "unit_code VARCHAR(20),"
                + "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (student_id, unit_code),"
                + "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (unit_code) REFERENCES units(unit_code) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the assessment table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createAssessmentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS assessments ("
                + "assessment_id VARCHAR(50) PRIMARY KEY,"
                + "title VARCHAR(200) NOT NULL,"
                + "description TEXT,"
                + "unit_code VARCHAR(20) NOT NULL,"
                + "created_by VARCHAR(50) NOT NULL,"
                + "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "start_date_time TIMESTAMP,"
                + "end_date_time TIMESTAMP,"
                + "duration_minutes INT NOT NULL,"
                + "is_active TINYINT(1) DEFAULT 0,"
                + "is_practice TINYINT(1) DEFAULT 0,"
                + "total_marks INT NOT NULL,"
                + "allow_offline_attempt TINYINT(1) DEFAULT 0,"
                + "FOREIGN KEY (unit_code) REFERENCES units(unit_code) ON DELETE CASCADE,"
                + "FOREIGN KEY (created_by) REFERENCES users(user_id)"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the question table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createQuestionTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS questions ("
                + "question_id VARCHAR(50) PRIMARY KEY,"
                + "question_text TEXT NOT NULL,"
                + "question_type VARCHAR(30) NOT NULL,"
                + "topic VARCHAR(100),"
                + "difficulty VARCHAR(20),"
                + "options TEXT,"
                + "correct_answers TEXT NOT NULL,"
                + "marks INT NOT NULL,"
                + "unit_code VARCHAR(20) NOT NULL,"
                + "created_by VARCHAR(50) NOT NULL,"
                + "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "approved TINYINT(1) DEFAULT 0,"
                + "feedback TEXT,"
                + "FOREIGN KEY (unit_code) REFERENCES units(unit_code) ON DELETE CASCADE,"
                + "FOREIGN KEY (created_by) REFERENCES users(user_id)"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the assessment_questions table if it doesn't exist.
     * This table maps questions to assessments.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createAssessmentQuestionTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS assessment_questions ("
                + "assessment_id VARCHAR(50),"
                + "question_id VARCHAR(50),"
                + "question_order INT,"
                + "PRIMARY KEY (assessment_id, question_id),"
                + "FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the student_assessments table if it doesn't exist.
     * This table tracks student attempts at assessments.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createStudentAssessmentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS student_assessments ("
                + "attempt_id VARCHAR(50) PRIMARY KEY,"
                + "student_id VARCHAR(50),"
                + "assessment_id VARCHAR(50),"
                + "start_time TIMESTAMP,"
                + "end_time TIMESTAMP,"
                + "score INT,"
                + "total_possible INT,"
                + "status VARCHAR(20),"
                + "is_offline TINYINT(1) DEFAULT 0,"
                + "is_submitted TINYINT(1) DEFAULT 0,"
                + "is_practice TINYINT(1) DEFAULT 0,"
                + "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the student_responses table if it doesn't exist.
     * This table stores student responses to questions.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createStudentResponseTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS student_responses ("
                + "response_id VARCHAR(50) PRIMARY KEY,"
                + "attempt_id VARCHAR(50),"
                + "question_id VARCHAR(50),"
                + "response_text TEXT,"
                + "is_correct TINYINT(1),"
                + "marks_awarded INT,"
                + "feedback TEXT,"
                + "FOREIGN KEY (attempt_id) REFERENCES student_assessments(attempt_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the practice_assessments table if it doesn't exist.
     * This table specifically tracks student practice assessment attempts and results.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createPracticeAssessmentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS practice_assessments ("
                + "practice_id VARCHAR(50) PRIMARY KEY,"
                + "student_id VARCHAR(50) NOT NULL,"
                + "assessment_id VARCHAR(50) NOT NULL,"
                + "title VARCHAR(200) NOT NULL,"
                + "unit_code VARCHAR(20) NOT NULL,"
                + "start_time TIMESTAMP,"
                + "end_time TIMESTAMP,"
                + "score INT,"
                + "total_possible INT,"
                + "percentage DOUBLE,"
                + "grade VARCHAR(10),"
                + "completion_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (unit_code) REFERENCES units(unit_code) ON DELETE CASCADE"
                + ");";        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Practice assessments table created or already exists");
        }
    }
    
    /**
     * Creates the reports table if it doesn't exist.
     * This table stores report configurations and templates.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createReportsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS reports ("
                + "report_id VARCHAR(50) PRIMARY KEY,"
                + "report_name VARCHAR(100) NOT NULL,"
                + "report_type VARCHAR(50) NOT NULL,"
                + "description TEXT,"
                + "query_template TEXT NOT NULL,"
                + "parameters TEXT,"
                + "created_by VARCHAR(50),"
                + "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "is_system TINYINT(1) DEFAULT 0,"
                + "FOREIGN KEY (created_by) REFERENCES users(user_id)"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the report_executions table if it doesn't exist.
     * This table tracks report execution history.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createReportExecutionsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS report_executions ("
                + "execution_id VARCHAR(50) PRIMARY KEY,"
                + "report_id VARCHAR(50) NOT NULL,"
                + "executed_by VARCHAR(50) NOT NULL,"
                + "execution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "parameters TEXT,"
                + "result_file_path VARCHAR(255),"
                + "FOREIGN KEY (report_id) REFERENCES reports(report_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (executed_by) REFERENCES users(user_id)"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the system_settings table if it doesn't exist.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createSystemSettingsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS system_settings ("
                + "setting_key VARCHAR(50) PRIMARY KEY,"
                + "setting_value TEXT NOT NULL,"
                + "description TEXT,"
                + "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
            
            // Insert default settings if they don't exist
            String insertDefaultSettings = "INSERT IGNORE INTO system_settings (setting_key, setting_value, description) VALUES "
                    + "('system_name', 'Kenyatta CAT System', 'The name of the system'),"
                    + "('academic_year', '2024-2025', 'Current academic year'),"
                    + "('session_timeout', '30', 'Session timeout in minutes'),"
                    + "('min_password_length', '8', 'Minimum password length'),"
                    + "('password_complexity', 'medium', 'Password complexity level (low, medium, high)'),"
                    + "('account_lockout', '5', 'Number of failed login attempts before account lockout'),"
                    + "('max_units_per_semester', '6', 'Maximum number of units a student can enroll in per semester'),"
                    + "('fee_clearance_required', 'true', 'Whether fee clearance is required for enrollment');";
            
            stmt.execute(insertDefaultSettings);
        }
    }
    
    /**
     * Creates the topics table if it doesn't exist.
     * This table stores topics for units to categorize questions.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createTopicsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS topics ("
                + "topic_id VARCHAR(50) PRIMARY KEY,"
                + "topic_name VARCHAR(100) NOT NULL,"
                + "description TEXT,"
                + "unit_code VARCHAR(20) NOT NULL,"
                + "created_by VARCHAR(50) NOT NULL,"
                + "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (unit_code) REFERENCES units(unit_code) ON DELETE CASCADE,"
                + "FOREIGN KEY (created_by) REFERENCES users(user_id)"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Creates the student_unit_enrollments table if it doesn't exist.
     * This table is used for tracking student enrollments in units.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void createStudentUnitEnrollmentsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS student_unit_enrollments ("
                + "student_id VARCHAR(36),"
                + "unit_code VARCHAR(10),"
                + "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "status VARCHAR(20) DEFAULT 'ACTIVE',"
                + "PRIMARY KEY (student_id, unit_code),"
                + "FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (unit_code) REFERENCES units(unit_code) ON DELETE CASCADE"
                + ");";
        
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
            LOGGER.info("student_unit_enrollments table created or already exists");
            
            // Migrate data from enrollments table if it exists
            migrateEnrollmentsData();
        }
    }
    
    /**
     * Migrates data from the old enrollments table to the new student_unit_enrollments table
     * if the old table exists and contains data.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void migrateEnrollmentsData() throws SQLException {
        // First check if the enrollments table exists
        DatabaseMetaData dbMetaData = getConnection().getMetaData();
        ResultSet tables = dbMetaData.getTables(null, null, "enrollments", null);
        
        if (tables.next()) {
            LOGGER.info("Found enrollments table, checking for data to migrate");
            
            // Check if there's data to migrate
            String countQuery = "SELECT COUNT(*) FROM enrollments";
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {
                
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.info("Found " + rs.getInt(1) + " enrollment records to migrate");
                    
                    // Insert data from enrollments to student_unit_enrollments
                    String migrationQuery = "INSERT IGNORE INTO student_unit_enrollments (student_id, unit_code, enrollment_date) "
                                          + "SELECT student_id, unit_code, enrollment_date FROM enrollments";
                    
                    stmt.execute(migrationQuery);
                    LOGGER.info("Enrollment data migrated to student_unit_enrollments table");
                } else {
                    LOGGER.info("No enrollment data to migrate");
                }
            } catch (SQLException e) {
                // Table might not exist or have a different structure
                LOGGER.log(Level.WARNING, "Could not migrate enrollment data: " + e.getMessage());
            }
        }
    }
    
    /**
     * Updates the student_assessments table to add any missing columns.
     * This ensures backward compatibility with existing databases.
     *
     * @throws SQLException If a database access error occurs
     */
    private static void updateStudentAssessmentTable() throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, "student_assessments", "is_submitted");
        
        // Check if is_submitted column exists
        if (!columns.next()) {
            LOGGER.info("Adding is_submitted column to student_assessments table");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE student_assessments ADD COLUMN is_submitted TINYINT(1) DEFAULT 0");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error adding is_submitted column: " + e.getMessage());
            }
        }
        columns.close();
        
        // Check if is_practice column exists
        columns = metaData.getColumns(null, null, "student_assessments", "is_practice");
        if (!columns.next()) {
            LOGGER.info("Adding is_practice column to student_assessments table");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE student_assessments ADD COLUMN is_practice TINYINT(1) DEFAULT 0");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error adding is_practice column: " + e.getMessage());
            }
        }
        columns.close();
    }
    
    /**
     * Creates the focus_violations table if it doesn't exist.
     * This table stores information about window focus violations during assessments.
     * 
     * @throws SQLException If a database error occurs
     */
    private static void createFocusViolationsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS focus_violations (" +
                "violation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "assessment_id VARCHAR(36) NOT NULL, " +
                "student_id VARCHAR(36) NOT NULL, " +
                "start_time DATETIME NOT NULL, " +
                "end_time DATETIME NOT NULL, " +
                "duration_seconds INT NOT NULL, " +
                "FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                "INDEX idx_assessment_id (assessment_id), " +
                "INDEX idx_student_id (student_id), " +
                "INDEX idx_start_time (start_time)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("focus_violations table created or already exists");
        }
    }
    
    /**
     * Executes a query and returns the result set.
     *
     * @param query The SQL query to execute
     * @return The result set
     * @throws SQLException If a database access error occurs
     */
    public static ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(query);
    }
    
    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     *
     * @param query The SQL query to execute
     * @return The number of rows affected
     * @throws SQLException If a database access error occurs
     */
    public static int executeUpdate(String query) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeUpdate(query);
        }
    }
    
    /**
     * Prepares a statement for execution.
     *
     * @param query The SQL query to prepare
     * @return The prepared statement
     * @throws SQLException If a database access error occurs
     */
    public static PreparedStatement prepareStatement(String query) throws SQLException {
        return getConnection().prepareStatement(query);
    }
    
    /**
     * Prepares a statement for execution with the specified result set type and concurrency.
     *
     * @param query The SQL query to prepare
     * @param resultSetType The type of ResultSet (e.g., ResultSet.TYPE_SCROLL_INSENSITIVE)
     * @param resultSetConcurrency The concurrency of ResultSet (e.g., ResultSet.CONCUR_READ_ONLY)
     * @return The prepared statement
     * @throws SQLException If a database access error occurs
     */
    public static PreparedStatement prepareStatement(String query, int resultSetType, int resultSetConcurrency) throws SQLException {
        return getConnection().prepareStatement(query, resultSetType, resultSetConcurrency);
    }
    
    /**
     * Creates test users for development purposes.
     * This method should only be called in development environments.
     */
    public static void createTestUsers() {
        try {
            // Check if test users already exist
            if (userExists("admin")) {
                LOGGER.info("Test users already exist, skipping creation");
                return;
            }
            
            LOGGER.info("Creating test users for development");
            
            // Create admin user
            String adminId = "ADMIN001";
            String adminPassword = book.javafx.kenyattacatsystem.utils.PasswordUtil.createHashedPassword("admin123");
            String insertAdminSql = "INSERT INTO users (user_id, username, password, full_name, email, role, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement stmt = prepareStatement(insertAdminSql)) {
                stmt.setString(1, adminId);
                stmt.setString(2, "admin");
                stmt.setString(3, adminPassword);
                stmt.setString(4, "System Administrator");
                stmt.setString(5, "admin@ku.ac.ke");
                stmt.setString(6, "Admin");
                stmt.executeUpdate();
                
                // Insert into admin table
                String insertAdminDetailsSql = "INSERT INTO admins (admin_id, user_id, department, access_level) VALUES (?, ?, ?, ?)";
                try (PreparedStatement adminStmt = prepareStatement(insertAdminDetailsSql)) {
                    adminStmt.setString(1, adminId);
                    adminStmt.setString(2, adminId); // user_id is the same as admin_id
                    adminStmt.setString(3, "IT Department");
                    adminStmt.setString(4, "System Administrator"); // Changed from position to access_level
                    adminStmt.executeUpdate();
                }
            }
            
            // Create lecturer user
            String lecturerId = "LEC001";
            String lecturerPassword = book.javafx.kenyattacatsystem.utils.PasswordUtil.createHashedPassword("lecturer123");
            String insertLecturerSql = "INSERT INTO users (user_id, username, password, full_name, email, role, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement stmt = prepareStatement(insertLecturerSql)) {
                stmt.setString(1, lecturerId);
                stmt.setString(2, "lecturer");
                stmt.setString(3, lecturerPassword);
                stmt.setString(4, "John Doe");
                stmt.setString(5, "lecturer@ku.ac.ke");
                stmt.setString(6, "Lecturer");
                stmt.executeUpdate();
                
                // Insert into lecturer table
                String insertLecturerDetailsSql = "INSERT INTO lecturers (staff_id, department, specialization) VALUES (?, ?, ?)";
                try (PreparedStatement lecturerStmt = prepareStatement(insertLecturerDetailsSql)) {
                    lecturerStmt.setString(1, lecturerId);
                    lecturerStmt.setString(2, "Computer Science");
                    lecturerStmt.setString(3, "Software Engineering");
                    lecturerStmt.executeUpdate();
                }
            }
            
            // Create student user
            String studentId = "STU001";
            String studentPassword = book.javafx.kenyattacatsystem.utils.PasswordUtil.createHashedPassword("student123");
            String insertStudentSql = "INSERT INTO users (user_id, username, password, full_name, email, role, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement stmt = prepareStatement(insertStudentSql)) {
                stmt.setString(1, studentId);
                stmt.setString(2, "student");
                stmt.setString(3, studentPassword);
                stmt.setString(4, "Jane Smith");
                stmt.setString(5, "student@ku.ac.ke");
                stmt.setString(6, "Student");
                stmt.executeUpdate();
                
                // Insert into student table
                String insertStudentDetailsSql = "INSERT INTO students (student_id, user_id, program, year_of_study) VALUES (?, ?, ?, ?)";
                try (PreparedStatement studentStmt = prepareStatement(insertStudentDetailsSql)) {
                    studentStmt.setString(1, studentId);
                    studentStmt.setString(2, studentId); // user_id is the same as student_id
                    studentStmt.setString(3, "BSc Computer Science");
                    studentStmt.setInt(4, 3);
                    studentStmt.executeUpdate();
                }
            }
            
            LOGGER.info("Test users created successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating test users", e);
        }
    }
    
    /**
     * Checks if a user with the given username exists.
     *
     * @param username The username to check
     * @return True if the user exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    private static boolean userExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Validates a user's password.
     * 
     * @param userId The user ID
     * @param password The password to validate
     * @return True if the password is valid, false otherwise
     * @throws SQLException If a database error occurs
     */
    public static boolean validatePassword(String userId, String password) throws SQLException {
        String query = "SELECT password_hash FROM users WHERE user_id = ?";
        
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");
                // In a real application, you would use proper password hashing
                return password.equals(storedPassword);
            }
            
            return false;
        }
    }
}