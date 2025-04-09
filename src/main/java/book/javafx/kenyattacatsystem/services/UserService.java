package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Admin;
import book.javafx.kenyattacatsystem.models.Lecturer;
import book.javafx.kenyattacatsystem.models.Student;
import book.javafx.kenyattacatsystem.models.User;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for user-related operations.
 * Handles user authentication, registration, and profile management.
 */
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username The username
     * @param password The password
     * @param role     The role (Student, Lecturer, Admin), can be null to check any role
     * @return The authenticated user, or null if authentication fails
     */
    public static User authenticateUser(String username, String password, String role) {
        String query;
        PreparedStatement stmt = null;
        
        try {
            // If role is specified, include it in the query
            if (role != null && !role.isEmpty()) {
                query = "SELECT * FROM users WHERE username = ? AND role = ?";
                LOGGER.info("Executing query: " + query + " with username: " + username + ", role: " + role);
                stmt = DatabaseUtil.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, role);
            } else {
                // If no role is specified, query without role restriction
                query = "SELECT * FROM users WHERE username = ?";
                LOGGER.info("Executing query: " + query + " with username: " + username);
                stmt = DatabaseUtil.prepareStatement(query);
                stmt.setString(1, username);
            }

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LOGGER.info("User found in database: " + username);
                // Verify the password using PasswordUtil
                String storedPassword = rs.getString("password");
                LOGGER.info("Verifying password...");
                if (!book.javafx.kenyattacatsystem.utils.PasswordUtil.verifyPassword(password, storedPassword)) {
                    LOGGER.warning("Password verification failed for user: " + username);
                    return null; // Password doesn't match
                }
                LOGGER.info("Password verified successfully for user: " + username);
                User user = new User(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                );

                // Update last login time
                updateLastLogin(user.getUserId());

                // Load role-specific data based on user's role from database if role parameter is null
                String userRole = role != null ? role : user.getRole();
                
                if ("Student".equals(userRole)) {
                    return getStudentDetails(user);
                } else if ("Lecturer".equals(userRole)) {
                    return getLecturerDetails(user);
                } else if ("Admin".equals(userRole)) {
                    return getAdminDetails(user);
                }

                return user;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user", e);
        }

        return null;
    }

    /**
     * Updates the last login time for a user.
     *
     * @param userId The user ID
     */
    private static void updateLastLogin(String userId) {
        String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating last login time", e);
        }
    }

    /**
     * Loads student-specific details for a user.
     *
     * @param user The base user object
     * @return The student object with all details
     */
    private static Student getStudentDetails(User user) {
        String query = "SELECT * FROM students WHERE user_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Student student = new Student(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getFullName(),
                        user.getEmail(),
                        rs.getString("student_id"),
                        rs.getString("program"),
                        rs.getInt("year_of_study")
                );

                // Load enrolled units
                loadEnrolledUnits(student);

                return student;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading student details", e);
        }

        return null;
    }

    /**
     * Loads lecturer-specific details for a user.
     *
     * @param user The base user object
     * @return The lecturer object with all details
     */
    private static Lecturer getLecturerDetails(User user) {
        String query = "SELECT * FROM lecturers WHERE user_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Lecturer lecturer = new Lecturer(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getFullName(),
                        user.getEmail(),
                        rs.getString("staff_id"),
                        rs.getString("department")
                );

                // Load taught units
                loadTaughtUnits(lecturer);

                return lecturer;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading lecturer details", e);
        }

        return null;
    }

    /**
     * Loads admin-specific details for a user.
     *
     * @param user The base user object
     * @return The admin object with all details
     */
    private static Admin getAdminDetails(User user) {
        String query = "SELECT * FROM admins WHERE user_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Admin(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getFullName(),
                        user.getEmail(),
                        rs.getString("admin_id"),
                        rs.getString("department"),
                        rs.getString("access_level")
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading admin details", e);
        }

        return null;
    }

    /**
     * Loads the enrolled units for a student.
     *
     * @param student The student object
     */
    private static void loadEnrolledUnits(Student student) {
        String query = "SELECT unit_code FROM enrollments WHERE student_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, student.getStudentId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                student.addEnrolledUnit(rs.getString("unit_code"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading enrolled units", e);
        }
    }

    /**
     * Loads the taught units for a lecturer.
     *
     * @param lecturer The lecturer object
     */
    private static void loadTaughtUnits(Lecturer lecturer) {
        String query = "SELECT unit_code FROM units WHERE lecturer_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, lecturer.getStaffId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lecturer.addTaughtUnit(rs.getString("unit_code"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading taught units", e);
        }
    }

    /**
     * Registers a new student user.
     *
     * @param username    The username
     * @param password    The password
     * @param fullName    The full name
     * @param email       The email
     * @param studentId   The student ID
     * @param program     The program of study
     * @param yearOfStudy The year of study
     * @return The newly created student, or null if registration fails
     */
    public static Student registerStudent(String username, String password, String fullName,
                                         String email, String studentId, String program, int yearOfStudy) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Create user record
            String userId = UUID.randomUUID().toString();
            String userQuery = "INSERT INTO users (user_id, username, password, full_name, email, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, userId);
                stmt.setString(2, username);
                stmt.setString(3, book.javafx.kenyattacatsystem.utils.PasswordUtil.createHashedPassword(password));
                stmt.setString(4, fullName);
                stmt.setString(5, email);
                stmt.setString(6, "Student");
                stmt.executeUpdate();
            }

            // Create student record
            String studentQuery = "INSERT INTO students (student_id, user_id, program, year_of_study) "
                    + "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(studentQuery)) {
                stmt.setString(1, studentId);
                stmt.setString(2, userId);
                stmt.setString(3, program);
                stmt.setInt(4, yearOfStudy);
                stmt.executeUpdate();
            }

            conn.commit();

            return new Student(userId, username, password, fullName, email, studentId, program, yearOfStudy);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering student", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
        }

        return null;
    }

    /**
     * Registers a new lecturer user.
     *
     * @param username   The username
     * @param password   The password
     * @param fullName   The full name
     * @param email      The email
     * @param staffId    The staff ID
     * @param department The department
     * @return The newly created lecturer, or null if registration fails
     */
    public static Lecturer registerLecturer(String username, String password, String fullName,
                                           String email, String staffId, String department) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Create user record
            String userId = UUID.randomUUID().toString();
            String userQuery = "INSERT INTO users (user_id, username, password, full_name, email, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, userId);
                stmt.setString(2, username);
                stmt.setString(3, book.javafx.kenyattacatsystem.utils.PasswordUtil.createHashedPassword(password));
                stmt.setString(4, fullName);
                stmt.setString(5, email);
                stmt.setString(6, "Lecturer");
                stmt.executeUpdate();
            }

            // Create lecturer record
            String lecturerQuery = "INSERT INTO lecturers (staff_id, user_id, department) "
                    + "VALUES (?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(lecturerQuery)) {
                stmt.setString(1, staffId);
                stmt.setString(2, userId);
                stmt.setString(3, department);
                stmt.executeUpdate();
            }

            conn.commit();

            return new Lecturer(userId, username, password, fullName, email, staffId, department);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering lecturer", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
        }

        return null;
    }

    /**
     * Registers a new admin user.
     *
     * @param username    The username
     * @param password    The password
     * @param fullName    The full name
     * @param email       The email
     * @param adminId     The admin ID
     * @param department  The department
     * @param accessLevel The access level
     * @return The newly created admin, or null if registration fails
     */
    public static Admin registerAdmin(String username, String password, String fullName,
                                     String email, String adminId, String department, String accessLevel) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Create user record
            String userId = UUID.randomUUID().toString();
            String userQuery = "INSERT INTO users (user_id, username, password, full_name, email, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, userId);
                stmt.setString(2, username);
                stmt.setString(3, book.javafx.kenyattacatsystem.utils.PasswordUtil.createHashedPassword(password));
                stmt.setString(4, fullName);
                stmt.setString(5, email);
                stmt.setString(6, "Admin");
                stmt.executeUpdate();
            }

            // Create admin record
            String adminQuery = "INSERT INTO admins (admin_id, user_id, department, access_level) "
                    + "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
                stmt.setString(1, adminId);
                stmt.setString(2, userId);
                stmt.setString(3, department);
                stmt.setString(4, accessLevel);
                stmt.executeUpdate();
            }

            conn.commit();

            return new Admin(userId, username, password, fullName, email, adminId, department, accessLevel);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering admin", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
        }

        return null;
    }

    /**
     * Gets a list of all users.
     *
     * @return A list of all users
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
            while (rs.next()) {
                User user = new User(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all users", e);
        }

        return users;
    }

    /**
     * Updates a user's profile information.
     *
     * @param user The user with updated information
     * @return True if the update was successful, false otherwise
     */
    public static boolean updateUserProfile(User user) {
        String query = "UPDATE users SET full_name = ?, email = ? WHERE user_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user profile", e);
        }

        return false;
    }

    /**
     * Changes a user's password.
     *
     * @param userId      The user ID
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return True if the password change was successful, false otherwise
     */
    public static boolean changePassword(String userId, String oldPassword, String newPassword) {
        // First verify the old password
        String verifyQuery = "SELECT password FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(verifyQuery)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getString("password").equals(oldPassword)) {
                // Old password is correct, update to new password
                String updateQuery = "UPDATE users SET password = ? WHERE user_id = ?";

                try (PreparedStatement updateStmt = DatabaseUtil.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, newPassword); // In a real app, use password hashing
                    updateStmt.setString(2, userId);

                    return updateStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error changing password", e);
        }

        return false;
    }

    /**
     * Deletes a user account.
     *
     * @param userId The user ID
     * @return True if the deletion was successful, false otherwise
     */
    public static boolean deleteUser(String userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            return false;
        }
    }
    
    /**
     * Gets a user's full name based on their user ID.
     *
     * @param userId The ID of the user
     * @return The user's full name, or null if not found
     */
    public static String getUserFullName(String userId) {
        String query = "SELECT full_name FROM users WHERE user_id = ?";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("full_name");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user's full name", e);
        }
        
        return null;
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check
     * @return True if the username exists, false otherwise
     */
    public static boolean isUsernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking username existence", e);
        }

        return false;
    }

    /**
     * Checks if an email already exists in the database.
     *
     * @param email The email to check
     * @return True if the email exists, false otherwise
     */
    public static boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email existence", e);
        }

        return false;
    }

    /**
     * Gets a lecturer by their ID.
     *
     * @param lecturerId The lecturer ID
     * @return The lecturer object, or null if not found
     */
    public static Lecturer getLecturerById(String lecturerId) {
        try {
            String query = "SELECT l.*, u.full_name, u.email FROM lecturers l " +
                    "JOIN users u ON l.user_id = u.user_id " +
                    "WHERE l.staff_id = ?";

            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, lecturerId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Lecturer lecturer = new Lecturer();
                    lecturer.setStaffId(rs.getString("staff_id"));
                    lecturer.setUserId(rs.getString("user_id")); // Fixing the type mismatch
                    lecturer.setDepartment(rs.getString("department"));
                    lecturer.setFullName(rs.getString("full_name"));
                    lecturer.setEmail(rs.getString("email"));
                    return lecturer;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting lecturer by ID", e);
        }

        return null;
    }

    /**
     * Gets a student by their ID.
     *
     * @param studentId The student ID
     * @return The student object, or null if not found
     */
    public static Student getStudentById(String studentId) {
        try {
            String query = "SELECT s.*, u.full_name, u.email FROM students s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.student_id = ?";

            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getString("student_id"));
                    student.setUserId(rs.getString("user_id"));
                    student.setProgram(rs.getString("program"));
                    student.setYearOfStudy(rs.getInt("year_of_study"));
                    student.setFullName(rs.getString("full_name"));
                    student.setEmail(rs.getString("email"));
                    
                    // Load enrolled units
                    loadEnrolledUnits(student);
                    
                    return student;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting student by ID", e);
        }

        return null;
    }

    /**
     * Updates a student's enrolled units in the database.
     *
     * @param student The student with updated enrolled units
     * @return True if the update was successful, false otherwise
     */
    public static boolean updateStudentEnrollment(Student student) {
        if (student == null || student.getStudentId() == null) {
            LOGGER.warning("Cannot update enrollment: student or studentId is null");
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // First, delete all existing enrollments for this student
            String deleteQuery = "DELETE FROM enrollments WHERE student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                stmt.setString(1, student.getStudentId());
                stmt.executeUpdate();
            }
            
            // Then insert the current enrollments
            if (student.getEnrolledUnits() != null && !student.getEnrolledUnits().isEmpty()) {
                String insertQuery = "INSERT INTO enrollments (student_id, unit_code, enrollment_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    for (String unitCode : student.getEnrolledUnits()) {
                        stmt.setString(1, student.getStudentId());
                        stmt.setString(2, unitCode);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
            
            conn.commit();
            LOGGER.info("Successfully updated enrollments for student: " + student.getStudentId());
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating student enrollments", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Updates a student in the database.
     *
     * @param student The student to update
     * @return True if the update was successful, false otherwise
     */
    public static boolean saveUser(Student student) {
        if (student == null) {
            return false;
        }
        
        // Update basic user information
        boolean userUpdated = updateUserProfile(student);
        
        // Update student-specific information
        String query = "UPDATE students SET program = ?, year_of_study = ? WHERE student_id = ?";
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, student.getProgram());
            stmt.setInt(2, student.getYearOfStudy());
            stmt.setString(3, student.getStudentId());
            
            int rowsUpdated = stmt.executeUpdate();
            
            // Update enrollments
            boolean enrollmentsUpdated = updateStudentEnrollment(student);
            
            return userUpdated && rowsUpdated > 0 && enrollmentsUpdated;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating student", e);
        }
        
        return false;
    }

    /**
     * Gets all departments from the users table.
     *
     * @return A list of all unique department names
     */
    public static List<String> getAllDepartments() {
        List<String> departments = new ArrayList<>();
        String query = "SELECT DISTINCT department FROM users WHERE department IS NOT NULL ORDER BY department";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String department = rs.getString("department");
                if (department != null && !department.isEmpty()) {
                    departments.add(department);
                }
            }
            
            LOGGER.info("Retrieved " + departments.size() + " departments");
            return departments;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving departments", e);
            return new ArrayList<>();
        }
    }
}