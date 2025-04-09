package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.StudentAssessmentAttempt;
import book.javafx.kenyattacatsystem.models.StudentResponse;
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
 * Service class for student assessment attempts and responses.
 */
public class StudentAssessmentService {
    private static final Logger LOGGER = Logger.getLogger(StudentAssessmentService.class.getName());
    
    /**
     * Saves just the student assessment attempt record to the database without responses.
     * This is useful for initializing an attempt before tracking begins.
     * 
     * @param attempt The student assessment attempt to save
     * @return True if the attempt was saved successfully, false otherwise
     */
    public boolean saveStudentAssessmentAttempt(StudentAssessmentAttempt attempt) {
        LOGGER.log(Level.INFO, "Initializing student assessment attempt in database: {0}", attempt.getAttemptId());
        
        String sql = "INSERT INTO student_assessments (attempt_id, student_id, assessment_id, start_time, " +
                    "end_time, score, total_possible, is_submitted, is_practice) " +
                    "VALUES (?, ?, ?, ?, NULL, 0, ?, FALSE, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, attempt.getAttemptId());
            stmt.setString(2, attempt.getStudentId());
            stmt.setString(3, attempt.getAssessmentId());
            stmt.setObject(4, LocalDateTime.now());
            stmt.setInt(5, attempt.getTotalPossible());
            stmt.setBoolean(6, attempt.isPractice());
            
            int result = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Initialized assessment attempt in database, result: {0}", result);
            return result > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing assessment attempt in database", e);
            return false;
        }
    }
    
    /**
     * Saves a student assessment attempt to the database
     * 
     * @param attempt The student assessment attempt to save
     * @return True if the attempt was saved successfully, false otherwise
     */
    public boolean saveStudentAssessment(StudentAssessmentAttempt attempt) {
        LOGGER.log(Level.INFO, "Saving student assessment attempt: {0}", attempt.getAttemptId());
        
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            LOGGER.log(Level.INFO, "Database connection established, autocommit set to false");
            
            // Check if the attempt already exists
            String checkQuery = "SELECT attempt_id FROM student_assessments WHERE attempt_id = ?";
            boolean attemptExists = false;
            
            try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                stmt.setString(1, attempt.getAttemptId());
                ResultSet rs = stmt.executeQuery();
                attemptExists = rs.next();
            }
            
            // SQL for insert or update
            String sql;
            if (attemptExists) {
                // Update existing attempt
                sql = "UPDATE student_assessments SET end_time = ?, score = ?, total_possible = ?, status = ? "
                    + "WHERE attempt_id = ?";
                LOGGER.log(Level.INFO, "Updating existing attempt record");
            } else {
                // Insert new attempt
                sql = "INSERT INTO student_assessments (attempt_id, student_id, assessment_id, start_time, "
                    + "end_time, score, total_possible, status, is_offline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                LOGGER.log(Level.INFO, "Creating new attempt record");
            }
            
            // Execute the insert or update
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (attemptExists) {
                    // Update parameters
                    stmt.setObject(1, attempt.getEndTime());
                    stmt.setInt(2, attempt.getScore());
                    stmt.setInt(3, attempt.getTotalPossible());
                    stmt.setString(4, attempt.getStatus());
                    stmt.setString(5, attempt.getAttemptId());
                } else {
                    // Insert parameters
                    stmt.setString(1, attempt.getAttemptId());
                    stmt.setString(2, attempt.getStudentId());
                    stmt.setString(3, attempt.getAssessmentId());
                    stmt.setObject(4, attempt.getStartTime());
                    stmt.setObject(5, attempt.getEndTime());
                    stmt.setInt(6, attempt.getScore());
                    stmt.setInt(7, attempt.getTotalPossible());
                    stmt.setString(8, attempt.getStatus());
                    stmt.setBoolean(9, attempt.isOffline());
                }
                
                int rowsAffected = stmt.executeUpdate();
                LOGGER.log(Level.INFO, "Saved attempt to database, rows affected: {0}", rowsAffected);
            }
            
            // Save all responses
            boolean responsesSuccess = saveStudentResponsesInternal(conn, attempt);
            if (!responsesSuccess) {
                LOGGER.log(Level.WARNING, "Failed to save student responses, rolling back transaction");
                conn.rollback();
                return false;
            }
            
            // Check if this is a practice assessment and save to practice_assessments table
            String checkPracticeQuery = "SELECT is_practice, title, unit_code FROM assessments WHERE assessment_id = ?";
            boolean isPractice = false;
            String title = "";
            String unitCode = "";
            
            try (PreparedStatement stmt = conn.prepareStatement(checkPracticeQuery)) {
                stmt.setString(1, attempt.getAssessmentId());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    isPractice = rs.getBoolean("is_practice");
                    title = rs.getString("title");
                    unitCode = rs.getString("unit_code");
                }
            }
            
            // If this is a practice assessment, save to practice_assessments table
            if (isPractice && "COMPLETED".equalsIgnoreCase(attempt.getStatus())) {
                LOGGER.log(Level.INFO, "This is a practice assessment, saving to practice_assessments table");
                
                // Calculate percentage
                double percentage = 0;
                if (attempt.getTotalPossible() > 0) {
                    percentage = ((double) attempt.getScore() / attempt.getTotalPossible()) * 100;
                }
                
                // Create a PerformanceAnalyticsService instance to save the practice assessment
                // Pass the existing connection to ensure we're using the same transaction
                PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
                boolean practiceSuccess = analyticsService.savePracticeAssessmentResult(
                    conn, attempt, title, unitCode, percentage);
                
                if (!practiceSuccess) {
                    LOGGER.log(Level.WARNING, "Failed to save to practice_assessments table, rolling back transaction");
                    conn.rollback();
                    return false;
                }
                
                LOGGER.log(Level.INFO, "Successfully saved practice assessment to practice_assessments table");
            }
            
            // Commit the transaction
            conn.commit();
            LOGGER.log(Level.INFO, "Transaction committed successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving student assessment: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "SQL State: {0}, Error Code: {1}", new Object[]{e.getSQLState(), e.getErrorCode()});
            LOGGER.log(Level.SEVERE, "Full exception: ", e);
            
            // Rollback the transaction
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.INFO, "Transaction rolled back due to error");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction: {0}", ex.getMessage());
                }
            }
            return false;
        } finally {
            // Reset auto-commit and close the connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                    LOGGER.log(Level.INFO, "Database connection closed");
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing database connection: {0}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Saves student responses for an attempt
     * 
     * @param conn The database connection
     * @param attempt The student assessment attempt
     * @return True if all responses were saved successfully, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean saveStudentResponsesInternal(Connection conn, StudentAssessmentAttempt attempt) throws SQLException {
        if (attempt.getAllResponses() == null || attempt.getAllResponses().isEmpty()) {
            LOGGER.log(Level.INFO, "No responses to save for attempt: {0}", attempt.getAttemptId());
            return true;
        }
        
        LOGGER.log(Level.INFO, "Saving {0} student responses for attempt {1}", 
                  new Object[]{attempt.getAllResponses().size(), attempt.getAttemptId()});
        
        // First delete any existing responses for this attempt
        String deleteSql = "DELETE FROM student_responses WHERE attempt_id = ?";
        int deletedRows = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, attempt.getAttemptId());
            deletedRows = stmt.executeUpdate();
        }
        
        LOGGER.log(Level.INFO, "Deleted {0} existing responses for attempt {1}", 
                  new Object[]{deletedRows, attempt.getAttemptId()});
        
        // Now insert all responses
        String insertSql = "INSERT INTO student_responses (response_id, attempt_id, question_id, response_text, marks_awarded) "
                + "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (StudentResponse response : attempt.getAllResponses()) {
                stmt.setString(1, response.getResponseId());
                stmt.setString(2, attempt.getAttemptId());
                stmt.setString(3, response.getQuestionId());
                stmt.setString(4, response.getResponseText());
                stmt.setInt(5, response.getMarksAwarded());
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            LOGGER.log(Level.INFO, "Saved {0} student responses to database", results.length);
            return true;
        }
    }
    
    /**
     * Public method to save student responses for an attempt
     * 
     * @param attempt The student assessment attempt
     * @return True if all responses were saved successfully, false otherwise
     */
    public boolean saveStudentResponses(StudentAssessmentAttempt attempt) {
        LOGGER.log(Level.INFO, "Saving student responses for attempt: {0}", attempt.getAttemptId());
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            return saveStudentResponsesInternal(conn, attempt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving student responses", e);
            return false;
        }
    }
    
    /**
     * Public method to save student responses for an attempt using an existing database connection
     * 
     * @param conn The database connection to use (must be part of an active transaction)
     * @param attempt The student assessment attempt
     * @return True if all responses were saved successfully, false otherwise
     */
    public boolean saveStudentResponses(Connection conn, StudentAssessmentAttempt attempt) {
        LOGGER.log(Level.INFO, "Saving student responses for attempt: {0}", attempt.getAttemptId());
        
        try {
            return saveStudentResponsesInternal(conn, attempt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving student responses", e);
            return false;
        }
    }
    
    /**
     * Gets a student assessment attempt by ID
     * 
     * @param attemptId The attempt ID
     * @return The student assessment attempt, or null if not found
     */
    public StudentAssessmentAttempt getStudentAssessment(String attemptId) {
        LOGGER.log(Level.INFO, "Getting student assessment attempt: {0}", attemptId);
        
        String query = "SELECT * FROM student_assessments WHERE attempt_id = ?";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, attemptId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                StudentAssessmentAttempt attempt = new StudentAssessmentAttempt();
                attempt.setAttemptId(rs.getString("attempt_id"));
                attempt.setStudentId(rs.getString("student_id"));
                attempt.setAssessmentId(rs.getString("assessment_id"));
                attempt.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                attempt.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                attempt.setScore(rs.getInt("score"));
                attempt.setTotalPossible(rs.getInt("total_possible"));
                attempt.setStatus(rs.getString("status"));
                attempt.setOffline(rs.getBoolean("is_offline"));
                
                // Load responses
                loadStudentResponses(attempt);
                
                LOGGER.log(Level.INFO, "Found student assessment attempt: {0}", attemptId);
                return attempt;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting student assessment: {0}", e.getMessage());
        }
        
        LOGGER.log(Level.WARNING, "Student assessment attempt not found: {0}", attemptId);
        return null;
    }
    
    /**
     * Loads student responses for an attempt
     * 
     * @param attempt The student assessment attempt
     * @throws SQLException If a database error occurs
     */
    private void loadStudentResponses(StudentAssessmentAttempt attempt) throws SQLException {
        String query = "SELECT * FROM student_responses WHERE attempt_id = ?";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, attempt.getAttemptId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                StudentResponse response = new StudentResponse();
                response.setResponseId(rs.getString("response_id"));
                response.setAttemptId(rs.getString("attempt_id"));
                response.setQuestionId(rs.getString("question_id"));
                response.setResponseText(rs.getString("response_text"));
                response.setCorrect(rs.getBoolean("is_correct"));
                response.setMarksAwarded(rs.getInt("marks_awarded"));
                response.setFeedback(rs.getString("feedback"));
                
                attempt.addResponse(response.getQuestionId(), response);
            }
            
            LOGGER.log(Level.INFO, "Loaded {0} responses for attempt {1}", 
                      new Object[]{attempt.getAllResponses().size(), attempt.getAttemptId()});
        }
    }
    
    /**
     * Gets all student assessment attempts for a student
     * 
     * @param studentId The student ID
     * @return A list of student assessment attempts
     */
    public List<StudentAssessmentAttempt> getStudentAssessments(String studentId) {
        LOGGER.log(Level.INFO, "Getting student assessment attempts for student: {0}", studentId);
        
        List<StudentAssessmentAttempt> attempts = new ArrayList<>();
        String query = "SELECT * FROM student_assessments WHERE student_id = ? ORDER BY start_time DESC";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                StudentAssessmentAttempt attempt = new StudentAssessmentAttempt();
                attempt.setAttemptId(rs.getString("attempt_id"));
                attempt.setStudentId(rs.getString("student_id"));
                attempt.setAssessmentId(rs.getString("assessment_id"));
                attempt.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                attempt.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                attempt.setScore(rs.getInt("score"));
                attempt.setTotalPossible(rs.getInt("total_possible"));
                attempt.setStatus(rs.getString("status"));
                attempt.setOffline(rs.getBoolean("is_offline"));
                
                attempts.add(attempt);
            }
            
            LOGGER.log(Level.INFO, "Found {0} student assessment attempts for student {1}", 
                      new Object[]{attempts.size(), studentId});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting student assessments: {0}", e.getMessage());
        }
        
        return attempts;
    }
    
    /**
     * Gets all student assessment attempts for an assessment
     * 
     * @param assessmentId The assessment ID
     * @return A list of student assessment attempts
     */
    public List<StudentAssessmentAttempt> getAssessmentAttempts(String assessmentId) {
        LOGGER.log(Level.INFO, "Getting student assessment attempts for assessment: {0}", assessmentId);
        
        List<StudentAssessmentAttempt> attempts = new ArrayList<>();
        String query = "SELECT * FROM student_assessments WHERE assessment_id = ? ORDER BY start_time DESC";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessmentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                StudentAssessmentAttempt attempt = new StudentAssessmentAttempt();
                attempt.setAttemptId(rs.getString("attempt_id"));
                attempt.setStudentId(rs.getString("student_id"));
                attempt.setAssessmentId(rs.getString("assessment_id"));
                attempt.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                attempt.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                attempt.setScore(rs.getInt("score"));
                attempt.setTotalPossible(rs.getInt("total_possible"));
                attempt.setStatus(rs.getString("status"));
                attempt.setOffline(rs.getBoolean("is_offline"));
                
                attempts.add(attempt);
            }
            
            LOGGER.log(Level.INFO, "Found {0} student assessment attempts for assessment {1}", 
                      new Object[]{attempts.size(), assessmentId});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting assessment attempts: {0}", e.getMessage());
        }
        
        return attempts;
    }
    
    /**
     * Gets student responses for a practice test
     * 
     * @param practiceId The practice test ID
     * @return A list of student responses for the practice test
     */
    public List<StudentResponse> getStudentResponses(String practiceId) {
        LOGGER.log(Level.INFO, "Getting student responses for practice test: {0}", practiceId);
        
        List<StudentResponse> responses = new ArrayList<>();
        
        // First, get the assessment_id from the practice_assessments table
        String assessmentIdQuery = "SELECT assessment_id FROM practice_assessments WHERE practice_id = ?";
        
        try {
            String assessmentId = null;
            
            // Get the assessment ID
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentIdQuery)) {
                stmt.setString(1, practiceId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    assessmentId = rs.getString("assessment_id");
                    LOGGER.log(Level.INFO, "Found assessment ID: {0} for practice ID: {1}", new Object[]{assessmentId, practiceId});
                } else {
                    LOGGER.log(Level.WARNING, "No assessment found for practice ID: {0}", practiceId);
                    return responses; // Return empty list if no assessment found
                }
            }
            
            // Now get all student responses for this assessment ID
            String responsesQuery = "SELECT sr.* FROM student_responses sr " +
                    "WHERE sr.attempt_id = ?";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(responsesQuery)) {
                stmt.setString(1, practiceId);
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    StudentResponse response = new StudentResponse();
                    response.setResponseId(rs.getString("response_id"));
                    response.setAttemptId(rs.getString("attempt_id"));
                    response.setQuestionId(rs.getString("question_id"));
                    response.setResponseText(rs.getString("response_text"));
                    response.setCorrect(rs.getBoolean("is_correct"));
                    response.setMarksAwarded(rs.getInt("marks_awarded"));
                    response.setFeedback(rs.getString("feedback"));
                    
                    responses.add(response);
                }
                
                LOGGER.log(Level.INFO, "Retrieved {0} responses for practice test {1}", new Object[]{count, practiceId});
            }
            
            return responses;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting student responses for practice test: {0}", e.getMessage());
            e.printStackTrace();
        }
        
        return responses;
    }
}
