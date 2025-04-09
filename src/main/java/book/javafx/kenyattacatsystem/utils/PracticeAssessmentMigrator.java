package book.javafx.kenyattacatsystem.utils;

import book.javafx.kenyattacatsystem.models.StudentAssessmentAttempt;
import book.javafx.kenyattacatsystem.services.PerformanceAnalyticsService;
import book.javafx.kenyattacatsystem.services.StudentAssessmentService;

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
 * Utility class to migrate practice assessment data from student_assessments to practice_assessments table.
 * This helps ensure all practice assessments are properly tracked in the dedicated table.
 */
public class PracticeAssessmentMigrator {
    private static final Logger LOGGER = Logger.getLogger(PracticeAssessmentMigrator.class.getName());
    
    /**
     * Migrates all practice assessment attempts from student_assessments to practice_assessments table.
     * This should be called at application startup to ensure all practice assessments are properly tracked.
     * 
     * @return The number of records migrated
     */
    public static int migratePracticeAssessments() {
        LOGGER.log(Level.INFO, "Starting migration of practice assessments");
        int migratedCount = 0;
        
        try {
            // First, check if we need to run the migration at all
            String checkQuery = "SELECT COUNT(*) FROM practice_assessments";
            boolean hasPracticeTests = false;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(checkQuery)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    hasPracticeTests = true;
                }
            }
            
            // If we already have practice tests, only check for new ones
            if (hasPracticeTests) {
                LOGGER.log(Level.INFO, "Practice assessments already exist, checking only for new ones");
            }
            
            // Find all practice assessment attempts in student_assessments that aren't in practice_assessments
            String query = "SELECT sa.*, a.title, a.unit_code FROM student_assessments sa " +
                    "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                    "WHERE a.is_practice = true " +
                    "AND NOT EXISTS (SELECT 1 FROM practice_assessments pa " +
                    "WHERE pa.student_id = sa.student_id AND pa.assessment_id = sa.assessment_id)";
            
            List<StudentAssessmentAttempt> practiceAttempts = new ArrayList<>();
            // Use a map to store additional data for each attempt
            List<String> titles = new ArrayList<>();
            List<String> unitCodes = new ArrayList<>();
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    StudentAssessmentAttempt attempt = new StudentAssessmentAttempt();
                    attempt.setAttemptId(rs.getString("attempt_id"));
                    attempt.setStudentId(rs.getString("student_id"));
                    attempt.setAssessmentId(rs.getString("assessment_id"));
                    attempt.setScore(rs.getInt("score"));
                    attempt.setTotalPossible(rs.getInt("total_possible"));
                    attempt.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                    attempt.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                    
                    // Store additional data in parallel lists
                    practiceAttempts.add(attempt);
                    titles.add(rs.getString("title"));
                    unitCodes.add(rs.getString("unit_code"));
                }
            }
            
            LOGGER.log(Level.INFO, "Found {0} practice assessment attempts to migrate", practiceAttempts.size());
            
            // Migrate each attempt to the practice_assessments table
            PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
            
            for (int i = 0; i < practiceAttempts.size(); i++) {
                StudentAssessmentAttempt attempt = practiceAttempts.get(i);
                String title = titles.get(i);
                String unitCode = unitCodes.get(i);
                
                // Calculate percentage
                int score = attempt.getScore();
                int totalPossible = attempt.getTotalPossible();
                double percentage = (totalPossible > 0) ? ((double) score / totalPossible * 100) : 0;
                
                // Save to practice_assessments table
                boolean success = analyticsService.savePracticeAssessmentResult(attempt, title, unitCode, percentage);
                
                if (success) {
                    migratedCount++;
                    LOGGER.log(Level.INFO, "Successfully migrated practice assessment: {0} for student {1}", 
                            new Object[]{attempt.getAssessmentId(), attempt.getStudentId()});
                } else {
                    LOGGER.log(Level.WARNING, "Failed to migrate practice assessment: {0} for student {1}", 
                            new Object[]{attempt.getAssessmentId(), attempt.getStudentId()});
                }
            }
            
            // Also migrate practice tests directly from assessments table
            int createdCount = migrateCreatedPracticeTests();
            migratedCount += createdCount;
            
            LOGGER.log(Level.INFO, "Migration complete. Migrated {0} practice assessments", migratedCount);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during practice assessment migration", e);
        }
        
        return migratedCount;
    }
    
    /**
     * Migrates practice tests directly from the assessments table to practice_assessments.
     * This ensures that practice tests created by students are properly linked to them.
     * 
     * @return The number of records migrated
     */
    public static int migrateCreatedPracticeTests() {
        LOGGER.log(Level.INFO, "Starting migration of created practice tests");
        int migratedCount = 0;
        
        try {
            // First, check if we need to run the migration at all
            String checkQuery = "SELECT COUNT(*) FROM practice_assessments WHERE status = 'CREATED'";
            boolean hasCreatedTests = false;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(checkQuery)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    hasCreatedTests = true;
                }
            }
            
            // If we already have created tests, skip the migration to avoid duplicates
            if (hasCreatedTests) {
                LOGGER.log(Level.INFO, "Created practice tests already exist, skipping migration");
                return 0;
            }
            
            // Find all practice tests in assessments that don't have entries in practice_assessments
            // Join with students table to get the correct student_id for each user_id
            String query = "SELECT DISTINCT a.*, s.student_id FROM assessments a " +
                    "JOIN students s ON a.created_by = s.user_id " +
                    "WHERE a.is_practice = true " +
                    "AND NOT EXISTS (SELECT 1 FROM practice_assessments pa " +
                    "WHERE pa.assessment_id = a.assessment_id AND pa.student_id = s.student_id)";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String assessmentId = rs.getString("assessment_id");
                    String studentId = rs.getString("student_id"); // Get student_id from the join
                    String title = rs.getString("title");
                    String unitCode = rs.getString("unit_code");
                    int totalMarks = rs.getInt("total_marks");
                    
                    // Create a new entry in practice_assessments
                    String practiceId = UUID.randomUUID().toString();
                    String insertQuery = "INSERT INTO practice_assessments (practice_id, assessment_id, student_id, title, " +
                            "unit_code, score, total_possible, percentage, grade, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement insertStmt = DatabaseUtil.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, practiceId);
                        insertStmt.setString(2, assessmentId);
                        insertStmt.setString(3, studentId);
                        insertStmt.setString(4, title);
                        insertStmt.setString(5, unitCode);
                        insertStmt.setDouble(6, 0);
                        insertStmt.setInt(7, totalMarks);
                        insertStmt.setDouble(8, 0);
                        insertStmt.setString(9, "N/A");
                        insertStmt.setString(10, "CREATED");
                        
                        if (insertStmt.executeUpdate() > 0) {
                            migratedCount++;
                            LOGGER.log(Level.INFO, "Successfully migrated created practice test: {0} for student {1}", 
                                    new Object[]{assessmentId, studentId});
                        } else {
                            LOGGER.log(Level.WARNING, "Failed to migrate created practice test: {0} for student {1}", 
                                    new Object[]{assessmentId, studentId});
                        }
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "Created practice test migration complete. Migrated {0} tests", migratedCount);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during created practice test migration", e);
        }
        
        return migratedCount;
    }
}
