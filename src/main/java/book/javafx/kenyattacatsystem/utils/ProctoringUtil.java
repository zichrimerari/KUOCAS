package book.javafx.kenyattacatsystem.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for simple proctoring features.
 * Implements window focus monitoring to track when students switch away from the assessment.
 */
public class ProctoringUtil {
    private static final Logger LOGGER = Logger.getLogger(ProctoringUtil.class.getName());
    
    /**
     * Represents a focus event (when window focus is lost or gained)
     */
    public static class FocusEvent {
        private LocalDateTime timestamp;
        private boolean focused;
        
        public FocusEvent(LocalDateTime timestamp, boolean focused) {
            this.timestamp = timestamp;
            this.focused = focused;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public boolean isFocused() {
            return focused;
        }
        
        @Override
        public String toString() {
            return timestamp + " - " + (focused ? "Focused" : "Unfocused");
        }
    }
    
    /**
     * Represents a focus violation (period when window was out of focus)
     */
    public static class FocusViolation {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long durationSeconds;
        
        public FocusViolation(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        }
        
        public LocalDateTime getStartTime() {
            return startTime;
        }
        
        public LocalDateTime getEndTime() {
            return endTime;
        }
        
        public long getDurationSeconds() {
            return durationSeconds;
        }
        
        @Override
        public String toString() {
            return "Violation: " + startTime + " to " + endTime + " (" + durationSeconds + " seconds)";
        }
    }
    
    private List<FocusEvent> focusEvents = new ArrayList<>();
    private List<FocusViolation> violations = new ArrayList<>();
    private String assessmentId;
    private String studentId;
    private String studentUserId; // Store the actual user_id from the database
    private String studentUsername; // Store the username for reference
    
    /**
     * Creates a new proctoring utility for the given assessment and student.
     * 
     * @param assessmentId The ID of the assessment
     * @param studentId The ID of the student
     */
    public ProctoringUtil(String assessmentId, String studentId) {
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        
        // Look up the user_id and username for this student from the database
        lookupStudentInfo();
        
        LOGGER.info("Proctoring initialized for assessment: " + assessmentId + 
                   ", student: " + studentId + 
                   ", user_id: " + (studentUserId != null ? studentUserId : "unknown") +
                   ", username: " + (studentUsername != null ? studentUsername : "unknown"));
    }
    
    /**
     * Looks up the user_id and username for the student from the database.
     * This method handles both cases where studentId might be a username or a user_id.
     */
    private void lookupStudentInfo() {
        try {
            // First, try to find the student assuming studentId is a user_id
            String sql = "SELECT user_id, username FROM users WHERE user_id = ? AND role = 'Student'";
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, studentId);
                java.sql.ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    studentUserId = rs.getString("user_id");
                    studentUsername = rs.getString("username");
                    LOGGER.info("Found student info using user_id: " + studentId + 
                               ", username: " + studentUsername);
                    return; // Successfully found, no need to continue
                }
            }
            
            // If not found, try assuming studentId is a username
            sql = "SELECT user_id, username FROM users WHERE username = ? AND role = 'Student'";
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, studentId);
                java.sql.ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    studentUserId = rs.getString("user_id");
                    studentUsername = rs.getString("username");
                    LOGGER.info("Found student info using username: " + studentId + 
                               ", user_id: " + studentUserId);
                } else {
                    LOGGER.warning("Could not find student info for: " + studentId);
                    studentUserId = null;
                    studentUsername = null;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error looking up student info: " + e.getMessage(), e);
            studentUserId = null;
            studentUsername = null;
        }
    }
    
    /**
     * Attaches focus monitoring to the given scene.
     * 
     * @param scene The scene to monitor
     */
    public void attachFocusMonitoring(Scene scene) {
        if (scene == null) {
            LOGGER.warning("Cannot attach focus monitoring to null scene");
            return;
        }
        
        Stage stage = (Stage) scene.getWindow();
        
        // Record initial focus state
        recordFocusEvent(stage.isFocused());
        
        // Add focus change listeners
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            recordFocusEvent(newValue);
            if (!newValue) {
                LOGGER.info("Assessment window lost focus");
            } else {
                LOGGER.info("Assessment window regained focus");
                calculateViolation();
            }
        });
        
        LOGGER.info("Focus monitoring attached to assessment window");
    }
    
    /**
     * Records a focus event.
     * 
     * @param focused Whether the window is focused
     */
    private void recordFocusEvent(boolean focused) {
        focusEvents.add(new FocusEvent(LocalDateTime.now(), focused));
    }
    
    /**
     * Calculates a focus violation when focus is regained.
     */
    private void calculateViolation() {
        if (focusEvents.size() < 2) return;
        
        // Get the last two events (lost focus and regained focus)
        int lastIndex = focusEvents.size() - 1;
        FocusEvent regainedFocus = focusEvents.get(lastIndex);
        FocusEvent lostFocus = null;
        
        // Find the most recent "lost focus" event
        for (int i = lastIndex - 1; i >= 0; i--) {
            if (!focusEvents.get(i).isFocused()) {
                lostFocus = focusEvents.get(i);
                break;
            }
        }
        
        if (lostFocus != null && regainedFocus.isFocused()) {
            FocusViolation violation = new FocusViolation(lostFocus.getTimestamp(), regainedFocus.getTimestamp());
            violations.add(violation);
            LOGGER.info("Recorded focus violation: " + violation);
            
            // Always save to database regardless of duration
            // This ensures all violations are recorded
            saveFocusViolationToDatabase(violation);
            
            // Log the violation count for debugging
            LOGGER.info("Current violation count: " + violations.size() + 
                       ", Total duration: " + getTotalViolationDuration() + " seconds");
        }
    }
    
    /**
     * Saves a focus violation to the database.
     * 
     * @param violation The violation to save
     */
    private void saveFocusViolationToDatabase(FocusViolation violation) {
        try {
            // Log the attempt to save the violation
            LOGGER.log(Level.INFO, "Attempting to save focus violation to database - Assessment ID: {0}, Student ID: {1}, Username: {2}, Duration: {3} seconds",
                    new Object[]{assessmentId, studentId, studentUsername, violation.getDurationSeconds()});
            
            // Skip the main table and directly use the simple table without foreign keys
            LOGGER.log(Level.INFO, "Using focus_violations_simple table for all violations");
            createSimpleFocusViolationsTable();
            
            String sql = "INSERT INTO focus_violations_simple (assessment_id, student_id, student_username, start_time, end_time, duration_seconds) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            LOGGER.log(Level.INFO, "Executing SQL: {0} with params: [{1}, {2}, {3}, {4}, {5}, {6}]", 
                    new Object[]{sql, assessmentId, studentId, studentUsername, 
                                violation.getStartTime(), violation.getEndTime(), violation.getDurationSeconds()});
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, assessmentId);
                stmt.setString(2, studentId);
                stmt.setString(3, studentUsername != null ? studentUsername : studentId); // Store the username as well for reference
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(violation.getStartTime()));
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(violation.getEndTime()));
                stmt.setLong(6, violation.getDurationSeconds());
                
                int rowsAffected = stmt.executeUpdate();
                LOGGER.log(Level.INFO, "Saved focus violation to simple table, rows affected: {0}", rowsAffected);
                
                // Debug: Verify the data was inserted by querying it back
                verifyViolationWasSaved(violation);
                
            } catch (java.sql.SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to save focus violation to simple table: {0}", e.getMessage());
                LOGGER.log(Level.SEVERE, "SQL State: {0}, Error Code: {1}", new Object[]{e.getSQLState(), e.getErrorCode()});
                LOGGER.log(Level.SEVERE, "Stack trace:", e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error saving focus violation to database", e);
        }
    }
    
    /**
     * Verifies that a violation was saved correctly by querying it back.
     * 
     * @param violation The violation to verify
     */
    private void verifyViolationWasSaved(FocusViolation violation) {
        try {
            String sql = "SELECT * FROM focus_violations_simple WHERE assessment_id = ? AND student_id = ? " +
                         "AND start_time = ? ORDER BY violation_id DESC LIMIT 1";
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, assessmentId);
                stmt.setString(2, studentId);
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(violation.getStartTime()));
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int violationId = rs.getInt("violation_id");
                        LOGGER.log(Level.INFO, "Verified violation was saved with ID: {0}", violationId);
                    } else {
                        LOGGER.log(Level.WARNING, "Could not verify violation was saved - no matching record found");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error verifying violation was saved: {0}", e.getMessage());
        }
    }
    
    /**
     * Creates a simple focus violations table without foreign key constraints.
     */
    private void createSimpleFocusViolationsTable() {
        try {
            LOGGER.log(Level.INFO, "Creating focus_violations_simple table if it doesn't exist");
            
            // Check if the table already exists
            boolean tableExists = false;
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection()) {
                java.sql.DatabaseMetaData dbm = conn.getMetaData();
                java.sql.ResultSet tables = dbm.getTables(null, null, "focus_violations_simple", null);
                tableExists = tables.next();
                
                LOGGER.log(Level.INFO, "focus_violations_simple table exists: {0}", tableExists);
                
                if (!tableExists) {
                    // Create a simple table without foreign keys
                    String createTableSql = "CREATE TABLE IF NOT EXISTS focus_violations_simple (" +
                                "violation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "assessment_id VARCHAR(36) NOT NULL, " +
                                "student_id VARCHAR(50) NOT NULL, " +
                                "student_username VARCHAR(100), " +
                                "start_time TIMESTAMP NOT NULL, " +
                                "end_time TIMESTAMP NOT NULL, " +
                                "duration_seconds BIGINT NOT NULL, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")";
                    
                    LOGGER.log(Level.INFO, "Creating table with SQL: {0}", createTableSql);
                    
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        stmt.execute(createTableSql);
                        LOGGER.log(Level.INFO, "Created focus_violations_simple table successfully");
                    }
                }
                
                // Debug: Count existing records
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM focus_violations_simple")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        LOGGER.log(Level.INFO, "Current record count in focus_violations_simple: {0}", count);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error counting records: {0}", e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating simple focus violations table: {0}", e.getMessage());
            LOGGER.log(Level.WARNING, "Stack trace:", e);
        }
    }
    
    /**
     * Gets a summary of focus violations.
     * 
     * @return A summary string
     */
    public String getViolationSummary() {
        if (violations.isEmpty()) {
            return "No focus violations detected";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Focus Violations Summary:\n");
        summary.append("Total violations: ").append(violations.size()).append("\n");
        
        long totalDuration = violations.stream()
                .mapToLong(FocusViolation::getDurationSeconds)
                .sum();
        
        summary.append("Total time out of focus: ").append(totalDuration).append(" seconds\n");
        
        // List top 5 longest violations
        violations.sort((v1, v2) -> Long.compare(v2.getDurationSeconds(), v1.getDurationSeconds()));
        
        summary.append("Longest violations:\n");
        int count = Math.min(5, violations.size());
        for (int i = 0; i < count; i++) {
            FocusViolation v = violations.get(i);
            summary.append(i + 1).append(". ")
                   .append(v.getStartTime().toLocalTime())
                   .append(" - ")
                   .append(v.getDurationSeconds())
                   .append(" seconds\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Gets the total number of focus violations.
     * 
     * @return The number of violations
     */
    public int getViolationCount() {
        return violations.size();
    }
    
    /**
     * Gets the total duration of all focus violations in seconds.
     * 
     * @return The total duration in seconds
     */
    public long getTotalViolationDuration() {
        return violations.stream()
                .mapToLong(FocusViolation::getDurationSeconds)
                .sum();
    }
    
    /**
     * Retrieves focus violations for a specific assessment from the database.
     * 
     * @param assessmentId The ID of the assessment
     * @return A list of focus violations
     */
    public static List<FocusViolation> getFocusViolationsForAssessment(String assessmentId) {
        List<FocusViolation> violations = new ArrayList<>();
        
        try {
            String sql = "SELECT student_id, start_time, end_time, duration_seconds FROM focus_violations " +
                         "WHERE assessment_id = ? ORDER BY start_time";
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, assessmentId);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String studentId = rs.getString("student_id");
                        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
                        
                        FocusViolation violation = new FocusViolation(startTime, endTime);
                        violations.add(violation);
                        
                        LOGGER.info("Retrieved focus violation for assessment " + assessmentId + 
                                   ", student " + studentId + ": " + violation);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving focus violations for assessment " + assessmentId, e);
        }
        
        return violations;
    }
    
    /**
     * Retrieves focus violations for a specific student's assessment attempt from the database.
     * 
     * @param assessmentId The ID of the assessment
     * @param studentId The ID of the student
     * @return A list of focus violations
     */
    public static List<FocusViolation> getFocusViolationsForStudentAssessment(String assessmentId, String studentId) {
        List<FocusViolation> violations = new ArrayList<>();
        
        try {
            String sql = "SELECT start_time, end_time, duration_seconds FROM focus_violations " +
                         "WHERE assessment_id = ? AND student_id = ? ORDER BY start_time";
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, assessmentId);
                stmt.setString(2, studentId);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
                        
                        FocusViolation violation = new FocusViolation(startTime, endTime);
                        violations.add(violation);
                        
                        LOGGER.info("Retrieved focus violation for student " + studentId + 
                                   ", assessment " + assessmentId + ": " + violation);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving focus violations for student " + studentId + 
                      ", assessment " + assessmentId, e);
        }
        
        return violations;
    }
    
    /**
     * Gets a summary of focus violations for a specific assessment.
     * 
     * @param assessmentId The ID of the assessment
     * @return A summary of focus violations
     */
    public static String getFocusViolationSummaryForAssessment(String assessmentId) {
        try {
            String sql = "SELECT student_id, COUNT(*) as violation_count, SUM(duration_seconds) as total_duration " +
                         "FROM focus_violations WHERE assessment_id = ? GROUP BY student_id ORDER BY total_duration DESC";
            
            StringBuilder summary = new StringBuilder();
            summary.append("<h3>Proctoring Data: Window Focus Violations</h3>");
            summary.append("<p>This data shows when students switched away from the assessment window during the test.</p>");
            summary.append("<table><tr><th>Student ID</th><th>Violations</th><th>Total Time Out of Focus</th></tr>");
            
            try (java.sql.Connection conn = DatabaseUtil.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, assessmentId);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    boolean hasData = false;
                    
                    while (rs.next()) {
                        hasData = true;
                        String studentId = rs.getString("student_id");
                        int violationCount = rs.getInt("violation_count");
                        long totalDuration = rs.getLong("total_duration");
                        
                        summary.append("<tr>");
                        summary.append("<td>").append(studentId).append("</td>");
                        summary.append("<td>").append(violationCount).append("</td>");
                        summary.append("<td>").append(formatDuration(totalDuration)).append("</td>");
                        summary.append("</tr>");
                    }
                    
                    if (!hasData) {
                        summary.append("<tr><td colspan='3'>No focus violations detected for this assessment.</td></tr>");
                    }
                }
            }
            
            summary.append("</table>");
            return summary.toString();
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error generating focus violation summary for assessment " + assessmentId, e);
            return "<p>Error retrieving proctoring data: " + e.getMessage() + "</p>";
        }
    }
    
    /**
     * Formats a duration in seconds to a human-readable string.
     * 
     * @param seconds The duration in seconds
     * @return A formatted string (e.g. "2m 30s")
     */
    private static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        
        if (minutes < 60) {
            return minutes + "m " + remainingSeconds + "s";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        return hours + "h " + remainingMinutes + "m " + remainingSeconds + "s";
    }
}
