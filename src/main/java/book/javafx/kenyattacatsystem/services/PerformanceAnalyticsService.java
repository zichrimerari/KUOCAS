package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.*;
import book.javafx.kenyattacatsystem.services.StudentAssessmentService;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

/**
 * Service class for performance analytics.
 * Provides methods for retrieving and analyzing student and assessment performance data.
 */
public class PerformanceAnalyticsService {
    private static final Logger LOGGER = Logger.getLogger(PerformanceAnalyticsService.class.getName());
    private final StudentAssessmentService studentAssessmentService;
    
    public PerformanceAnalyticsService() {
        this.studentAssessmentService = new StudentAssessmentService();
    }
    
    /**
     * Get a student's performance across all assessments.
     *
     * @param studentId The ID of the student
     * @return Map containing performance metrics
     */
    public Map<String, Object> getStudentPerformance(String studentId) {
        Map<String, Object> performance = new HashMap<>();
        LOGGER.log(Level.INFO, "Getting performance data for student: {0}", studentId);
        
        try {
            // Get all student assessment attempts using the StudentAssessmentService
            List<StudentAssessmentAttempt> allAttempts = studentAssessmentService.getStudentAssessments(studentId);
            LOGGER.log(Level.INFO, "Retrieved {0} assessment attempts for student {1}", 
                      new Object[]{allAttempts.size(), studentId});
            
            // Separate formal and practice assessments
            List<Map<String, Object>> formalAssessments = new ArrayList<>();
            List<Map<String, Object>> practiceAssessments = new ArrayList<>();
            double formalAvgScore = 0;
            double practiceAvgScore = 0;
            int formalCount = 0;
            int practiceCount = 0;
            
            // Process each attempt
            for (StudentAssessmentAttempt attempt : allAttempts) {
                // Get the assessment details
                String assessmentQuery = "SELECT a.title, a.unit_code, a.total_marks, a.is_practice " +
                        "FROM assessments a WHERE a.assessment_id = ?";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentQuery)) {
                    stmt.setString(1, attempt.getAssessmentId());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        Map<String, Object> assessment = new HashMap<>();
                        assessment.put("assessmentId", attempt.getAssessmentId());
                        assessment.put("title", rs.getString("title"));
                        assessment.put("unitCode", rs.getString("unit_code"));
                        assessment.put("score", attempt.getScore());
                        assessment.put("totalMarks", attempt.getTotalPossible());
                        
                        double percentage = 0;
                        if (attempt.getTotalPossible() > 0) {
                            percentage = ((double) attempt.getScore() / attempt.getTotalPossible()) * 100;
                        }
                        assessment.put("percentage", percentage);
                        
                        assessment.put("startTime", attempt.getStartTime());
                        assessment.put("endTime", attempt.getEndTime());
                        
                        // Calculate time taken in minutes
                        long timeTakenMinutes = 0;
                        if (attempt.getStartTime() != null && attempt.getEndTime() != null) {
                            timeTakenMinutes = java.time.Duration.between(
                                attempt.getStartTime(), attempt.getEndTime()).toMinutes();
                        }
                        assessment.put("timeTaken", timeTakenMinutes);
                        
                        boolean isPractice = rs.getBoolean("is_practice");
                        
                        // Add to appropriate list
                        if (isPractice) {
                            practiceAssessments.add(assessment);
                            practiceAvgScore += percentage;
                            practiceCount++;
                        } else {
                            formalAssessments.add(assessment);
                            formalAvgScore += percentage;
                            formalCount++;
                        }
                    }
                }
            }
            
            // Get performance by unit
            String unitQuery = "SELECT a.unit_code, u.unit_name, " +
                    "AVG((sa.score / sa.total_possible) * 100) as avg_percentage, " +
                    "COUNT(sa.attempt_id) as assessment_count " +
                    "FROM student_assessments sa " +
                    "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                    "JOIN units u ON a.unit_code = u.unit_code " +
                    "WHERE sa.student_id = ? " +
                    "GROUP BY a.unit_code, u.unit_name";
            
            List<Map<String, Object>> unitPerformance = new ArrayList<>();
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(unitQuery)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> unit = new HashMap<>();
                    unit.put("unitCode", rs.getString("unit_code"));
                    unit.put("unitName", rs.getString("unit_name"));
                    unit.put("avgPercentage", rs.getDouble("avg_percentage"));
                    unit.put("assessmentCount", rs.getInt("assessment_count"));
                    
                    unitPerformance.add(unit);
                }
            }
            
            // Calculate overall metrics
            if (formalCount > 0) {
                formalAvgScore /= formalCount;
            }
            
            if (practiceCount > 0) {
                practiceAvgScore /= practiceCount;
            }
            
            // Calculate overall average score
            double overallAvgScore = 0;
            int totalCount = formalCount + practiceCount;
            if (totalCount > 0) {
                overallAvgScore = (formalAvgScore * formalCount + practiceAvgScore * practiceCount) / totalCount;
            }
            
            // Add all data to the performance map
            performance.put("formalAssessments", formalAssessments);
            performance.put("practiceAssessments", practiceAssessments);
            performance.put("unitPerformance", unitPerformance);
            performance.put("formalAvgScore", formalAvgScore);
            performance.put("practiceAvgScore", practiceAvgScore);
            performance.put("totalAssessments", formalCount + practiceCount);
            
            // Add the specific keys that StudentDashboardController is looking for
            performance.put("averageScore", overallAvgScore);
            performance.put("assessmentsCompleted", formalCount);
            performance.put("practiceTestsCompleted", practiceCount);
            
            LOGGER.log(Level.INFO, "Processed performance data: {0} formal assessments, {1} practice assessments", 
                      new Object[]{formalCount, practiceCount});
            
            return performance;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving student performance", e);
        }
        
        return performance;
    }
    
    /**
     * Get detailed performance for a specific assessment.
     *
     * @param assessmentId The ID of the assessment
     * @param studentId The ID of the student (optional, if null returns data for all students)
     * @return Map containing assessment performance metrics
     */
    public Map<String, Object> getAssessmentPerformance(String assessmentId, String studentId) {
        Map<String, Object> performance = new HashMap<>();
        
        try {
            // Get assessment details
            String assessmentQuery = "SELECT a.*, u.unit_name FROM assessments a " +
                    "JOIN units u ON a.unit_code = u.unit_code " +
                    "WHERE a.assessment_id = ?";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentQuery)) {
                stmt.setString(1, assessmentId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    performance.put("assessmentId", rs.getString("assessment_id"));
                    performance.put("title", rs.getString("title"));
                    performance.put("description", rs.getString("description"));
                    performance.put("unitCode", rs.getString("unit_code"));
                    performance.put("unitName", rs.getString("unit_name"));
                    performance.put("totalMarks", rs.getInt("total_marks"));
                    performance.put("isPractice", rs.getBoolean("is_practice"));
                }
            }
            
            // Get student performance data
            String studentQuery;
            List<Map<String, Object>> studentPerformance = new ArrayList<>();
            
            if (studentId != null) {
                // Get performance for a specific student
                studentQuery = "SELECT sa.*, s.first_name, s.last_name " +
                        "FROM student_assessments sa " +
                        "JOIN students s ON sa.student_id = s.student_id " +
                        "WHERE sa.assessment_id = ? AND sa.student_id = ?";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(studentQuery)) {
                    stmt.setString(1, assessmentId);
                    stmt.setString(2, studentId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        Map<String, Object> student = new HashMap<>();
                        student.put("studentId", rs.getString("student_id"));
                        student.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                        student.put("score", rs.getDouble("score"));
                        student.put("startTime", rs.getTimestamp("start_time"));
                        student.put("endTime", rs.getTimestamp("end_time"));
                        
                        double totalMarks = (Integer) performance.get("totalMarks");
                        double percentage = 0;
                        if (totalMarks > 0) {
                            percentage = (rs.getDouble("score") / totalMarks) * 100;
                        }
                        student.put("percentage", percentage);
                        
                        studentPerformance.add(student);
                    }
                }
                
                // Get question-level performance for this student
                String questionQuery = "SELECT q.question_id, q.question_text, q.marks, sr.is_correct, sr.marks_awarded " +
                        "FROM assessment_questions aq " +
                        "JOIN questions q ON aq.question_id = q.question_id " +
                        "LEFT JOIN student_responses sr ON q.question_id = sr.question_id " +
                        "LEFT JOIN student_assessments sa ON sr.attempt_id = sa.attempt_id " +
                        "WHERE aq.assessment_id = ? AND sa.student_id = ?";
                
                List<Map<String, Object>> questionPerformance = new ArrayList<>();
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(questionQuery)) {
                    stmt.setString(1, assessmentId);
                    stmt.setString(2, studentId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        Map<String, Object> question = new HashMap<>();
                        question.put("questionId", rs.getString("question_id"));
                        question.put("questionText", rs.getString("question_text"));
                        question.put("marks", rs.getInt("marks"));
                        question.put("isCorrect", rs.getBoolean("is_correct"));
                        question.put("marksAwarded", rs.getInt("marks_awarded"));
                        
                        questionPerformance.add(question);
                    }
                }
                
                performance.put("questionPerformance", questionPerformance);
            } else {
                // Get performance for all students
                studentQuery = "SELECT sa.*, s.first_name, s.last_name " +
                        "FROM student_assessments sa " +
                        "JOIN students s ON sa.student_id = s.student_id " +
                        "WHERE sa.assessment_id = ?";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(studentQuery)) {
                    stmt.setString(1, assessmentId);
                    ResultSet rs = stmt.executeQuery();
                    
                    double totalScore = 0;
                    int studentCount = 0;
                    
                    while (rs.next()) {
                        Map<String, Object> student = new HashMap<>();
                        student.put("studentId", rs.getString("student_id"));
                        student.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                        student.put("score", rs.getDouble("score"));
                        student.put("startTime", rs.getTimestamp("start_time"));
                        student.put("endTime", rs.getTimestamp("end_time"));
                        
                        double totalMarks = (Integer) performance.get("totalMarks");
                        double percentage = 0;
                        if (totalMarks > 0) {
                            percentage = (rs.getDouble("score") / totalMarks) * 100;
                        }
                        student.put("percentage", percentage);
                        
                        studentPerformance.add(student);
                        totalScore += percentage;
                        studentCount++;
                    }
                    
                    // Calculate average score
                    double avgScore = 0;
                    if (studentCount > 0) {
                        avgScore = totalScore / studentCount;
                    }
                    performance.put("averageScore", avgScore);
                    performance.put("studentCount", studentCount);
                }
                
                // Get question-level performance for all students
                String questionQuery = "SELECT aq.question_id, q.question_text, q.question_type as type, " +
                        "q.difficulty, " +
                        "COUNT(sr.response_id) as attempt_count, " +
                        "SUM(CASE WHEN sr.is_correct = 1 THEN 1 ELSE 0 END) as correct_count " +
                        "FROM assessment_questions aq " +
                        "JOIN questions q ON aq.question_id = q.question_id " +
                        "LEFT JOIN student_responses sr ON q.question_id = sr.question_id " +
                        "LEFT JOIN student_assessments sa ON sr.attempt_id = sa.attempt_id " +
                        "WHERE aq.assessment_id = ? " +
                        "GROUP BY aq.question_id";
                
                List<Map<String, Object>> questionPerformance = new ArrayList<>();
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(questionQuery)) {
                    stmt.setString(1, assessmentId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        Map<String, Object> question = new HashMap<>();
                        question.put("questionId", rs.getString("question_id"));
                        question.put("questionText", rs.getString("question_text"));
                        question.put("type", rs.getString("type"));
                        question.put("difficulty", rs.getDouble("difficulty"));
                        
                        int attemptCount = rs.getInt("attempt_count");
                        int correctCount = rs.getInt("correct_count");
                        
                        double successRate = 0;
                        if (attemptCount > 0) {
                            successRate = ((double) correctCount / attemptCount) * 100;
                        }
                        
                        question.put("attemptCount", attemptCount);
                        question.put("correctCount", correctCount);
                        question.put("successRate", successRate);
                        
                        questionPerformance.add(question);
                    }
                }
                
                performance.put("questionPerformance", questionPerformance);
                
                // Calculate highest and lowest scores
                double highestScore = 0;
                double lowestScore = 100;
                
                for (Map<String, Object> student : studentPerformance) {
                    double percentage = (Double) student.get("percentage");
                    if (percentage > highestScore) {
                        highestScore = percentage;
                    }
                    if (percentage < lowestScore) {
                        lowestScore = percentage;
                    }
                }
                
                if (studentPerformance.isEmpty()) {
                    lowestScore = 0;
                }
                
                performance.put("highestScore", highestScore);
                performance.put("lowestScore", lowestScore);
            }
            
            performance.put("studentPerformance", studentPerformance);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving assessment performance", e);
        }
        
        return performance;
    }
    
    /**
     * Get unit-level performance analytics.
     *
     * @param unitCode The unit code
     * @return Map containing performance metrics
     */
    public Map<String, Object> getUnitPerformance(String unitCode) {
        LOGGER.log(Level.INFO, "Getting performance data for unit: " + unitCode);
        
        Map<String, Object> performance = new HashMap<>();
        performance.put("unitCode", unitCode);
        
        try {
            // Get unit details
            String unitQuery = "SELECT * FROM units WHERE unit_code = ?";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(unitQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    performance.put("unitName", rs.getString("unit_name"));
                    performance.put("department", rs.getString("department"));
                }
            }
            
            // Get formal assessments
            String formalAssessmentQuery = "SELECT a.assessment_id, a.title, a.total_marks, a.is_practice, " +
                    "COUNT(sa.student_id) as attempt_count, " +
                    "AVG(sa.score) as avg_score, " +
                    "MAX(sa.score) as max_score, " +
                    "MIN(sa.score) as min_score, " +
                    "AVG((sa.score / a.total_marks) * 100) as avg_percentage " +
                    "FROM assessments a " +
                    "LEFT JOIN student_assessments sa ON a.assessment_id = sa.assessment_id " +
                    "WHERE a.unit_code = ? AND a.is_practice = 0 " +
                    "GROUP BY a.assessment_id, a.title, a.total_marks";
            
            List<Map<String, Object>> formalAssessments = new ArrayList<>();
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(formalAssessmentQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                LOGGER.log(Level.INFO, "Executing formal assessment query for unit: " + unitCode);
                int formalCount = 0;
                
                while (rs.next()) {
                    formalCount++;
                    Map<String, Object> assessment = new HashMap<>();
                    assessment.put("assessmentId", rs.getString("assessment_id"));
                    assessment.put("title", rs.getString("title"));
                    assessment.put("totalMarks", rs.getDouble("total_marks"));
                    assessment.put("isPractice", false);
                    assessment.put("attemptCount", rs.getInt("attempt_count"));
                    assessment.put("avgScore", rs.getDouble("avg_score"));
                    assessment.put("maxScore", rs.getDouble("max_score"));
                    assessment.put("minScore", rs.getDouble("min_score"));
                    assessment.put("avgPercentage", rs.getDouble("avg_percentage"));
                    
                    LOGGER.log(Level.INFO, "Found formal assessment: " + assessment.get("title") + 
                            ", attempts: " + assessment.get("attemptCount") + 
                            ", avg score: " + assessment.get("avgScore") + 
                            ", avg percentage: " + assessment.get("avgPercentage"));
                    
                    formalAssessments.add(assessment);
                }
                
                LOGGER.log(Level.INFO, "Total formal assessments found: " + formalCount);
            }
            
            // Get practice assessments
            String practiceAssessmentQuery = "SELECT a.assessment_id, a.title, a.total_marks, a.is_practice, " +
                    "COUNT(pa.student_id) as attempt_count, " +
                    "AVG(pa.score) as avg_score, " +
                    "MAX(pa.score) as max_score, " +
                    "MIN(pa.score) as min_score, " +
                    "AVG((pa.score / a.total_marks) * 100) as avg_percentage " +
                    "FROM assessments a " +
                    "LEFT JOIN practice_assessments pa ON a.assessment_id = pa.assessment_id " +
                    "WHERE a.unit_code = ? AND a.is_practice = 1 " +
                    "GROUP BY a.assessment_id, a.title, a.total_marks";
            
            List<Map<String, Object>> practiceAssessments = new ArrayList<>();
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(practiceAssessmentQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                LOGGER.log(Level.INFO, "Executing practice assessment query for unit: " + unitCode);
                int practiceCount = 0;
                
                while (rs.next()) {
                    practiceCount++;
                    Map<String, Object> assessment = new HashMap<>();
                    assessment.put("assessmentId", rs.getString("assessment_id"));
                    assessment.put("title", rs.getString("title"));
                    assessment.put("totalMarks", rs.getDouble("total_marks"));
                    assessment.put("isPractice", true);
                    assessment.put("attemptCount", rs.getInt("attempt_count"));
                    assessment.put("avgScore", rs.getDouble("avg_score"));
                    assessment.put("maxScore", rs.getDouble("max_score"));
                    assessment.put("minScore", rs.getDouble("min_score"));
                    assessment.put("avgPercentage", rs.getDouble("avg_percentage"));
                    
                    LOGGER.log(Level.INFO, "Found practice assessment: " + assessment.get("title") + 
                            ", attempts: " + assessment.get("attemptCount") + 
                            ", avg score: " + assessment.get("avgScore") + 
                            ", avg percentage: " + assessment.get("avgPercentage"));
                    
                    practiceAssessments.add(assessment);
                }
                
                LOGGER.log(Level.INFO, "Total practice assessments found: " + practiceCount);
            }
            
            // Combine both types of assessments
            List<Map<String, Object>> allAssessments = new ArrayList<>();
            allAssessments.addAll(formalAssessments);
            allAssessments.addAll(practiceAssessments);
            
            performance.put("assessmentPerformance", allAssessments);
            
            LOGGER.log(Level.INFO, "Total assessments found for unit " + unitCode + ": " + allAssessments.size());
            
            // Get student enrollment and performance
            String studentQuery = "SELECT s.student_id, u.full_name, " +
                    "COALESCE(AVG((sa.score / a.total_marks) * 100), 0) as avg_percentage " +
                    "FROM students s " +
                    "JOIN enrollments e ON s.student_id = e.student_id " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "LEFT JOIN student_assessments sa ON s.student_id = sa.student_id " +
                    "LEFT JOIN assessments a ON sa.assessment_id = a.assessment_id AND a.unit_code = ? " +
                    "WHERE e.unit_code = ? " +
                    "GROUP BY s.student_id, u.full_name";
            
            List<Map<String, Object>> students = new ArrayList<>();
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(studentQuery)) {
                stmt.setString(1, unitCode);
                stmt.setString(2, unitCode);
                
                LOGGER.log(Level.INFO, "Executing student performance query for unit: " + unitCode);
                LOGGER.log(Level.INFO, "Student query: " + studentQuery);
                
                ResultSet rs = stmt.executeQuery();
                int studentCount = 0;
                
                while (rs.next()) {
                    studentCount++;
                    Map<String, Object> student = new HashMap<>();
                    String studentId = rs.getString("student_id");
                    String fullName = rs.getString("full_name");
                    double avgPercentage = rs.getDouble("avg_percentage");
                    
                    student.put("studentId", studentId);
                    student.put("fullName", fullName);
                    student.put("avgPercentage", avgPercentage);
                    
                    LOGGER.log(Level.INFO, "Found student: " + student.get("fullName") + 
                            ", avg percentage: " + student.get("avgPercentage"));
                    
                    // Count the number of assessments taken by this student
                    String assessmentCountQuery = "SELECT COUNT(*) as count FROM student_assessments sa " +
                            "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                            "WHERE sa.student_id = ? AND a.unit_code = ?";
                    
                    try (PreparedStatement countStmt = DatabaseUtil.prepareStatement(assessmentCountQuery)) {
                        countStmt.setString(1, studentId);
                        countStmt.setString(2, unitCode);
                        ResultSet countRs = countStmt.executeQuery();
                        
                        if (countRs.next()) {
                            int assessmentCount = countRs.getInt("count");
                            student.put("assessmentCount", assessmentCount);
                            LOGGER.log(Level.INFO, "Student " + student.get("fullName") + 
                                    " has taken " + assessmentCount + " assessments");
                        } else {
                            student.put("assessmentCount", 0);
                            LOGGER.log(Level.INFO, "Student " + student.get("fullName") + 
                                    " has taken 0 assessments");
                        }
                    }
                    
                    students.add(student);
                }
                
                LOGGER.log(Level.INFO, "Total students found for unit " + unitCode + ": " + studentCount);
            }
            
            performance.put("studentPerformance", students);
            
            // Log the entire performance map for debugging
            LOGGER.log(Level.INFO, "Performance data for unit " + unitCode + ":");
            LOGGER.log(Level.INFO, "  Unit code: " + performance.get("unitCode"));
            LOGGER.log(Level.INFO, "  Unit name: " + performance.get("unitName"));
            LOGGER.log(Level.INFO, "  Department: " + performance.get("department"));
            LOGGER.log(Level.INFO, "  Assessment count: " + ((List<?>)performance.get("assessmentPerformance")).size());
            LOGGER.log(Level.INFO, "  Student count: " + ((List<?>)performance.get("studentPerformance")).size());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving unit performance", e);
        }
        
        return performance;
    }
    
    /**
     * Get a student's performance history over time.
     *
     * @param studentId The ID of the student
     * @return List of performance data points ordered by date
     */
    public List<Map<String, Object>> getStudentPerformanceHistory(String studentId) {
        List<Map<String, Object>> performanceHistory = new ArrayList<>();
        LOGGER.log(Level.INFO, "Getting performance history for student: {0}", studentId);
        
        try {
            // Get all student assessment attempts using the StudentAssessmentService
            List<StudentAssessmentAttempt> allAttempts = studentAssessmentService.getStudentAssessments(studentId);
            
            // Sort attempts by end time (ascending)
            allAttempts.sort((a1, a2) -> {
                // Handle null end times
                if (a1.getEndTime() == null && a2.getEndTime() == null) {
                    return 0; // Both null, consider them equal
                } else if (a1.getEndTime() == null) {
                    return -1; // Null comes before non-null
                } else if (a2.getEndTime() == null) {
                    return 1; // Non-null comes after null
                } else {
                    return a1.getEndTime().compareTo(a2.getEndTime());
                }
            });
            
            // Process each attempt
            for (StudentAssessmentAttempt attempt : allAttempts) {
                // Get the assessment details
                String assessmentQuery = "SELECT a.title, a.unit_code, a.is_practice " +
                        "FROM assessments a WHERE a.assessment_id = ?";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentQuery)) {
                    stmt.setString(1, attempt.getAssessmentId());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        Map<String, Object> dataPoint = new HashMap<>();
                        double score = attempt.getScore();
                        double totalMarks = attempt.getTotalPossible();
                        double percentage = (totalMarks > 0) ? (score / totalMarks) * 100 : 0;
                        
                        dataPoint.put("assessmentId", attempt.getAssessmentId());
                        dataPoint.put("title", rs.getString("title"));
                        dataPoint.put("unitCode", rs.getString("unit_code"));
                        dataPoint.put("score", percentage);
                        dataPoint.put("date", attempt.getEndTime() != null ? attempt.getEndTime().toString() : "In Progress");
                        dataPoint.put("type", rs.getBoolean("is_practice") ? "Practice" : "Formal");
                        
                        performanceHistory.add(dataPoint);
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "Retrieved {0} performance history data points for student {1}", 
                      new Object[]{performanceHistory.size(), studentId});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving student performance history", e);
        }
        
        return performanceHistory;
    }
    
    /**
     * Get a student's formal assessments.
     *
     * @param studentId The ID of the student
     * @return List of assessment data
     */
    public List<Map<String, Object>> getStudentAssessments(String studentId) {
        List<Map<String, Object>> assessments = new ArrayList<>();
        LOGGER.log(Level.INFO, "Getting formal assessments for student: {0}", studentId);
        
        try {
            // Get all student assessment attempts using the StudentAssessmentService
            List<StudentAssessmentAttempt> allAttempts = studentAssessmentService.getStudentAssessments(studentId);
            
            // Process each attempt
            for (StudentAssessmentAttempt attempt : allAttempts) {
                // Get the assessment details
                String assessmentQuery = "SELECT a.title, a.unit_code, a.is_practice " +
                        "FROM assessments a WHERE a.assessment_id = ?";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentQuery)) {
                    stmt.setString(1, attempt.getAssessmentId());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next() && !rs.getBoolean("is_practice")) {
                        Map<String, Object> assessment = new HashMap<>();
                        double score = attempt.getScore();
                        double totalMarks = attempt.getTotalPossible();
                        double percentage = (totalMarks > 0) ? (score / totalMarks) * 100 : 0;
                        
                        assessment.put("assessmentId", attempt.getAssessmentId());
                        assessment.put("title", rs.getString("title"));
                        assessment.put("unitCode", rs.getString("unit_code"));
                        assessment.put("score", percentage);
                        assessment.put("date", attempt.getEndTime().toString());
                        
                        assessments.add(assessment);
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "Retrieved {0} formal assessments for student {1}", 
                      new Object[]{assessments.size(), studentId});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving student assessments", e);
        }
        
        return assessments;
    }
    
    /**
     * Gets a student's practice tests.
     *
     * @param studentId The ID of the student
     * @return List of practice test data
     */
    public List<Map<String, Object>> getStudentPracticeTests(String studentId) {
        List<Map<String, Object>> practiceTests = new ArrayList<>();
        LOGGER.log(Level.INFO, "Getting practice assessments for student: {0}", studentId);
        
        try {
            // First try to get practice tests from the dedicated practice_assessments table
            String practiceQuery = "SELECT pa.*, a.duration_minutes FROM practice_assessments pa " +
                                  "LEFT JOIN assessments a ON pa.assessment_id = a.assessment_id " +
                                  "WHERE pa.student_id = ? ORDER BY pa.completion_date DESC";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(practiceQuery)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    Map<String, Object> practiceTest = new HashMap<>();
                    practiceTest.put("practiceId", rs.getString("practice_id"));
                    practiceTest.put("assessmentId", rs.getString("assessment_id"));
                    practiceTest.put("title", rs.getString("title"));
                    practiceTest.put("unitCode", rs.getString("unit_code"));
                    
                    // Get raw score and total possible
                    double score = rs.getDouble("score");
                    int totalPossible = rs.getInt("total_possible");
                    
                    // Get percentage from database or calculate it if needed
                    double percentage = rs.getDouble("percentage");
                    if (percentage <= 0 && totalPossible > 0) {
                        percentage = (score / totalPossible) * 100;
                    }
                    
                    practiceTest.put("score", score);
                    practiceTest.put("totalPossible", totalPossible);
                    practiceTest.put("percentage", percentage);
                    practiceTest.put("grade", rs.getString("grade"));
                    practiceTest.put("endTime", rs.getTimestamp("completion_date"));
                    practiceTest.put("date", rs.getTimestamp("completion_date"));
                    practiceTest.put("status", rs.getString("status"));
                    practiceTest.put("durationMinutes", rs.getInt("duration_minutes"));
                    
                    practiceTests.add(practiceTest);
                }
                LOGGER.log(Level.INFO, "Retrieved {0} practice assessments from practice_assessments table for student {1}", 
                          new Object[]{count, studentId});
            }
            
            // Get all practice assessments from student_assessments table that are marked as practice
            // in the assessments table (is_practice = 1)
            String studentAssessmentQuery = 
                    "SELECT sa.*, a.title, a.unit_code, a.is_practice, a.duration_minutes " +
                    "FROM student_assessments sa " +
                    "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                    "WHERE sa.student_id = ? AND a.is_practice = 1 " +
                    "ORDER BY sa.end_time DESC";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(studentAssessmentQuery)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                int duplicates = 0;
                int migrated = 0;
                
                while (rs.next()) {
                    count++;
                    String assessmentId = rs.getString("assessment_id");
                    
                    // Check if this assessment is already in our list
                    boolean isDuplicate = false;
                    for (Map<String, Object> existingTest : practiceTests) {
                        if (existingTest.get("assessmentId").equals(assessmentId)) {
                            isDuplicate = true;
                            duplicates++;
                            break;
                        }
                    }
                    
                    if (!isDuplicate) {
                        Map<String, Object> practiceTest = new HashMap<>();
                        int score = rs.getInt("score");
                        int totalMarks = rs.getInt("total_possible");
                        double percentage = (totalMarks > 0) ? (score / (double)totalMarks) * 100 : 0;
                        String grade = calculateGrade(percentage);
                        
                        practiceTest.put("assessmentId", assessmentId);
                        practiceTest.put("title", rs.getString("title"));
                        practiceTest.put("unitCode", rs.getString("unit_code"));
                        practiceTest.put("score", score);
                        practiceTest.put("totalPossible", totalMarks);
                        practiceTest.put("percentage", percentage);
                        practiceTest.put("grade", grade);
                        practiceTest.put("endTime", rs.getTimestamp("end_time"));
                        practiceTest.put("status", "COMPLETED");
                        practiceTest.put("durationMinutes", rs.getInt("duration_minutes"));
                        
                        // Migrate this practice assessment to the practice_assessments table
                        StudentAssessmentAttempt attempt = new StudentAssessmentAttempt();
                        attempt.setAttemptId(rs.getString("attempt_id"));
                        attempt.setStudentId(rs.getString("student_id"));
                        attempt.setAssessmentId(assessmentId);
                        attempt.setScore(score);
                        attempt.setTotalPossible(totalMarks);
                        attempt.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                        attempt.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                        
                        boolean migrationSuccess = savePracticeAssessmentResult(attempt, rs.getString("title"), rs.getString("unit_code"), percentage);
                        if (migrationSuccess) {
                            migrated++;
                            LOGGER.log(Level.INFO, "Successfully migrated practice assessment {0} for student {1} to practice_assessments table", 
                                      new Object[]{assessmentId, studentId});
                        }
                        
                        practiceTests.add(practiceTest);
                    }
                }
                
                LOGGER.log(Level.INFO, "Found {0} practice assessments in student_assessments table for student {1}, {2} duplicates, {3} migrated", 
                          new Object[]{count, studentId, duplicates, migrated});
            }
            
            // Check for practice tests in the assessments table that were created by this student
            // but don't have entries in practice_assessments yet
            String createdAssessmentsQuery = 
                    "SELECT a.*, s.student_id FROM assessments a " +
                    "JOIN students s ON a.created_by = s.user_id " +
                    "LEFT JOIN practice_assessments pa ON a.assessment_id = pa.assessment_id AND pa.student_id = s.student_id " +
                    "WHERE s.student_id = ? AND a.is_practice = 1 AND pa.practice_id IS NULL";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(createdAssessmentsQuery)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    String assessmentId = rs.getString("assessment_id");
                    
                    // Create a new practice assessment entry
                    String practiceId = UUID.randomUUID().toString();
                    String title = rs.getString("title");
                    String unitCode = rs.getString("unit_code");
                    int totalMarks = rs.getInt("total_marks");
                    
                    String insertQuery = "INSERT INTO practice_assessments (practice_id, assessment_id, student_id, title, " +
                            "unit_code, score, total_possible, percentage, grade, completion_date, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
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
                        insertStmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                        insertStmt.setString(11, "CREATED");
                        
                        if (insertStmt.executeUpdate() > 0) {
                            LOGGER.log(Level.INFO, "Created practice_assessments entry for assessment {0}", assessmentId);
                            
                            // Add to our list
                            Map<String, Object> practiceTest = new HashMap<>();
                            practiceTest.put("practiceId", practiceId);
                            practiceTest.put("assessmentId", assessmentId);
                            practiceTest.put("title", title);
                            practiceTest.put("unitCode", unitCode);
                            practiceTest.put("score", 0);
                            practiceTest.put("totalPossible", totalMarks);
                            practiceTest.put("percentage", 0.0);
                            practiceTest.put("grade", "N/A");
                            practiceTest.put("endTime", Timestamp.valueOf(LocalDateTime.now()));
                            practiceTest.put("status", "CREATED");
                            practiceTest.put("durationMinutes", rs.getInt("duration_minutes"));
                            
                            practiceTests.add(practiceTest);
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error creating practice_assessments entry for assessment " + assessmentId, e);
                    }
                }
                
                LOGGER.log(Level.INFO, "Found and linked {0} additional practice assessments created by student {1}", 
                          new Object[]{count, studentId});
            }
            
            LOGGER.log(Level.INFO, "Retrieved {0} practice assessments for student {1}", 
                      new Object[]{practiceTests.size(), studentId});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving student practice tests", e);
        }
        
        return practiceTests;
    }
    
    /**
     * Saves a practice assessment result to the practice_assessments table
     * 
     * @param attempt The student assessment attempt
     * @param title The assessment title
     * @param unitCode The unit code
     * @param percentage The percentage score
     * @return True if saved successfully, false otherwise
     */
    public boolean savePracticeAssessmentResult(StudentAssessmentAttempt attempt, String title, String unitCode, double percentage) {
        LOGGER.log(Level.INFO, "Saving practice assessment result for student: {0}, assessment: {1}", 
                  new Object[]{attempt.getStudentId(), attempt.getAssessmentId()});
        
        try {
            // Generate a unique ID for the practice assessment record if not already set
            String practiceId = attempt.getAttemptId();
            if (practiceId == null || practiceId.isEmpty()) {
                practiceId = UUID.randomUUID().toString();
                attempt.setAttemptId(practiceId);
            }
            
            // Calculate grade based on percentage
            String grade = calculateGrade(percentage);
            
            // Check if this practice assessment already exists
            String checkSql = "SELECT practice_id FROM practice_assessments WHERE practice_id = ?";
            boolean exists = false;
            
            try (PreparedStatement checkStmt = DatabaseUtil.prepareStatement(checkSql)) {
                checkStmt.setString(1, practiceId);
                ResultSet rs = checkStmt.executeQuery();
                exists = rs.next();
            }
            
            int rowsAffected = 0;
            
            if (exists) {
                // Update existing record
                String updateSql = "UPDATE practice_assessments SET student_id = ?, assessment_id = ?, title = ?, " +
                        "unit_code = ?, start_time = ?, end_time = ?, score = ?, total_possible = ?, percentage = ?, " +
                        "grade = ?, status = 'COMPLETED' WHERE practice_id = ?";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(updateSql)) {
                    stmt.setString(1, attempt.getStudentId());
                    stmt.setString(2, attempt.getAssessmentId());
                    stmt.setString(3, title);
                    stmt.setString(4, unitCode);
                    stmt.setObject(5, attempt.getStartTime());
                    stmt.setObject(6, attempt.getEndTime());
                    stmt.setInt(7, attempt.getScore());
                    stmt.setInt(8, attempt.getTotalPossible());
                    stmt.setDouble(9, percentage);
                    stmt.setString(10, grade);
                    stmt.setString(11, practiceId);
                    
                    rowsAffected = stmt.executeUpdate();
                }
            } else {
                // Insert new record
                String insertSql = "INSERT INTO practice_assessments (practice_id, student_id, assessment_id, title, " +
                        "unit_code, start_time, end_time, score, total_possible, percentage, grade, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'COMPLETED')";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(insertSql)) {
                    stmt.setString(1, practiceId);
                    stmt.setString(2, attempt.getStudentId());
                    stmt.setString(3, attempt.getAssessmentId());
                    stmt.setString(4, title);
                    stmt.setString(5, unitCode);
                    stmt.setObject(6, attempt.getStartTime());
                    stmt.setObject(7, attempt.getEndTime());
                    stmt.setInt(8, attempt.getScore());
                    stmt.setInt(9, attempt.getTotalPossible());
                    stmt.setDouble(10, percentage);
                    stmt.setString(11, grade);
                    
                    rowsAffected = stmt.executeUpdate();
                }
            }
            
            LOGGER.log(Level.INFO, "Saved practice assessment result, rows affected: {0}", rowsAffected);
            
            // Save student responses
            if (rowsAffected > 0 && attempt.getAllResponses() != null && !attempt.getAllResponses().isEmpty()) {
                LOGGER.log(Level.INFO, "Saving {0} student responses for practice test", attempt.getAllResponses().size());
                boolean responsesSaved = studentAssessmentService.saveStudentResponses(attempt);
                LOGGER.log(Level.INFO, "Student responses saved: {0}", responsesSaved);
            } else {
                LOGGER.log(Level.INFO, "No student responses to save for practice test");
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving practice assessment result", e);
            return false;
        }
    }
    
    /**
     * Saves a practice assessment result to the practice_assessments table using an existing database connection
     * 
     * @param conn The database connection to use (must be part of an active transaction)
     * @param attempt The student assessment attempt
     * @param title The assessment title
     * @param unitCode The unit code
     * @param percentage The percentage score
     * @return True if saved successfully, false otherwise
     */
    public boolean savePracticeAssessmentResult(Connection conn, StudentAssessmentAttempt attempt, 
                                               String title, String unitCode, double percentage) {
        LOGGER.log(Level.INFO, "Saving practice assessment result for student: {0}, assessment: {1} using existing connection", 
                  new Object[]{attempt.getStudentId(), attempt.getAssessmentId()});
        
        try {
            // Generate a unique ID for the practice assessment record if not already set
            String practiceId = attempt.getAttemptId();
            if (practiceId == null || practiceId.isEmpty()) {
                practiceId = UUID.randomUUID().toString();
                attempt.setAttemptId(practiceId);
            }
            
            // Calculate grade based on percentage
            String grade = calculateGrade(percentage);
            
            // Check if this practice assessment already exists
            String checkSql = "SELECT practice_id FROM practice_assessments WHERE practice_id = ?";
            boolean exists = false;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, practiceId);
                ResultSet rs = checkStmt.executeQuery();
                exists = rs.next();
            }
            
            int rowsAffected = 0;
            
            if (exists) {
                // Update existing record
                String updateSql = "UPDATE practice_assessments SET student_id = ?, assessment_id = ?, title = ?, " +
                        "unit_code = ?, start_time = ?, end_time = ?, score = ?, total_possible = ?, percentage = ?, " +
                        "grade = ?, status = 'COMPLETED' WHERE practice_id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, attempt.getStudentId());
                    stmt.setString(2, attempt.getAssessmentId());
                    stmt.setString(3, title);
                    stmt.setString(4, unitCode);
                    stmt.setObject(5, attempt.getStartTime());
                    stmt.setObject(6, attempt.getEndTime());
                    stmt.setInt(7, attempt.getScore());
                    stmt.setInt(8, attempt.getTotalPossible());
                    stmt.setDouble(9, percentage);
                    stmt.setString(10, grade);
                    stmt.setString(11, practiceId);
                    
                    rowsAffected = stmt.executeUpdate();
                }
            } else {
                // Insert new record
                String insertSql = "INSERT INTO practice_assessments (practice_id, student_id, assessment_id, title, " +
                        "unit_code, start_time, end_time, score, total_possible, percentage, grade, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'COMPLETED')";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setString(1, practiceId);
                    stmt.setString(2, attempt.getStudentId());
                    stmt.setString(3, attempt.getAssessmentId());
                    stmt.setString(4, title);
                    stmt.setString(5, unitCode);
                    stmt.setObject(6, attempt.getStartTime());
                    stmt.setObject(7, attempt.getEndTime());
                    stmt.setInt(8, attempt.getScore());
                    stmt.setInt(9, attempt.getTotalPossible());
                    stmt.setDouble(10, percentage);
                    stmt.setString(11, grade);
                    
                    rowsAffected = stmt.executeUpdate();
                }
            }
            
            LOGGER.log(Level.INFO, "Saved practice assessment result, rows affected: {0}", rowsAffected);
            
            // Save student responses using the existing connection
            if (rowsAffected > 0 && attempt.getAllResponses() != null && !attempt.getAllResponses().isEmpty()) {
                LOGGER.log(Level.INFO, "Saving {0} student responses for practice test", attempt.getAllResponses().size());
                boolean responsesSaved = studentAssessmentService.saveStudentResponses(conn, attempt);
                LOGGER.log(Level.INFO, "Student responses saved: {0}", responsesSaved);
            } else {
                LOGGER.log(Level.INFO, "No student responses to save for practice test");
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving practice assessment result: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Calculates a letter grade based on percentage score
     * 
     * @param percentage The percentage score
     * @return The letter grade
     */
    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        if (percentage >= 50) return "E";
        return "F";
    }
    
    /**
     * Creates a line chart for trend analysis.
     *
     * @param title The chart title
     * @param xAxisLabel The x-axis label
     * @param yAxisLabel The y-axis label
     * @param data List of data points
     * @param xField The field name for x-axis values
     * @param yField The field name for y-axis values
     * @return A configured LineChart
     */
    public LineChart<Number, Number> createLineChart(
            String title,
            String xAxisLabel,
            String yAxisLabel,
            List<Map<String, Object>> data,
            String xField,
            String yField) {
        
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);
        
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(title);
        
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> point = data.get(i);
            double xValue = i; // Default to index if xField is not a number
            double yValue = 0;
            
            if (point.containsKey(xField) && point.get(xField) instanceof Number) {
                xValue = ((Number) point.get(xField)).doubleValue();
            }
            
            if (point.containsKey(yField) && point.get(yField) instanceof Number) {
                yValue = ((Number) point.get(yField)).doubleValue();
            }
            
            series.getData().add(new XYChart.Data<>(xValue, yValue));
        }
        
        lineChart.getData().add(series);
        return lineChart;
    }
    
    /**
     * Creates a pie chart for showing proportions.
     *
     * @param title The chart title
     * @param data Map of slice names to values
     * @return A configured PieChart
     */
    public PieChart createPieChart(String title, Map<String, Double> data) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle(title);
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        
        pieChart.setData(pieChartData);
        return pieChart;
    }
    
    /**
     * Get department-level performance analytics.
     *
     * @param department The department name
     * @return Map containing performance metrics
     */
    public Map<String, Object> getDepartmentPerformance(String department) {
        Map<String, Object> performance = new HashMap<>();
        
        try {
            // Get department details
            performance.put("departmentName", department);
            
            // Get unit performance for the department
            String unitQuery = "SELECT u.unit_code, u.unit_name, " +
                    "COUNT(e.student_id) as student_count " +
                    "FROM units u " +
                    "LEFT JOIN enrollments e ON u.unit_code = e.unit_code " +
                    "WHERE u.department = ? " +
                    "GROUP BY u.unit_code";
            
            List<Map<String, Object>> units = new ArrayList<>();
            int totalStudents = 0;
            int unitCount = 0;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(unitQuery)) {
                stmt.setString(1, department);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> unit = new HashMap<>();
                    String unitCode = rs.getString("unit_code");
                    unit.put("unitCode", unitCode);
                    unit.put("unitName", rs.getString("unit_name"));
                    
                    int studentCount = rs.getInt("student_count");
                    unit.put("studentCount", studentCount);
                    
                    // Get unit performance data
                    Map<String, Object> unitPerformance = getUnitPerformance(unitCode);
                    if (unitPerformance != null && unitPerformance.containsKey("averageScore")) {
                        unit.put("avgScore", unitPerformance.get("averageScore"));
                    } else {
                        unit.put("avgScore", 0.0);
                    }
                    
                    units.add(unit);
                    totalStudents += studentCount;
                    unitCount++;
                }
            }
            
            // Calculate overall department metrics
            double totalScore = 0;
            for (Map<String, Object> unit : units) {
                double avgScore = (Double) unit.get("avgScore");
                totalScore += avgScore;
            }
            
            double avgScore = 0;
            if (unitCount > 0) {
                avgScore = totalScore / unitCount;
            }
            
            performance.put("units", units);
            performance.put("unitCount", unitCount);
            performance.put("studentCount", totalStudents);
            performance.put("averageScore", avgScore);
            
            // Get assessment performance
            String assessmentQuery = "SELECT a.assessment_id, a.title, a.unit_code, a.total_marks, " +
                    "COUNT(sa.attempt_id) as attempt_count, " +
                    "AVG(sa.score) as avg_score " +
                    "FROM assessments a " +
                    "JOIN units u ON a.unit_code = u.unit_code " +
                    "LEFT JOIN student_assessments sa ON a.assessment_id = sa.assessment_id " +
                    "WHERE u.department = ? " +
                    "GROUP BY a.assessment_id";
            
            List<Map<String, Object>> assessments = new ArrayList<>();
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentQuery)) {
                stmt.setString(1, department);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> assessment = new HashMap<>();
                    assessment.put("assessmentId", rs.getString("assessment_id"));
                    assessment.put("title", rs.getString("title"));
                    assessment.put("unitCode", rs.getString("unit_code"));
                    assessment.put("attemptCount", rs.getInt("attempt_count"));
                    
                    double avgAssessmentScore = rs.getDouble("avg_score");
                    double totalMarks = rs.getDouble("total_marks");
                    double percentage = 0;
                    if (totalMarks > 0) {
                        percentage = (avgAssessmentScore / totalMarks) * 100;
                    }
                    assessment.put("avgPercentage", percentage);
                    
                    assessments.add(assessment);
                }
            }
            
            performance.put("assessments", assessments);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving department performance", e);
        }
        
        return performance;
    }
    
    /**
     * Verify the database structure and data for analytics.
     * This method checks if all required tables and data exist.
     * 
     * @param unitCode The unit code to check
     * @return A map containing verification results
     */
    public Map<String, Object> verifyDatabaseForAnalytics(String unitCode) {
        Map<String, Object> results = new HashMap<>();
        results.put("unitCode", unitCode);
        
        try {
            LOGGER.log(Level.INFO, "Verifying database structure and data for unit: " + unitCode);
            
            // Check if units table exists and has data
            boolean unitsTableExists = false;
            boolean unitExists = false;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SHOW TABLES LIKE 'units'")) {
                ResultSet rs = stmt.executeQuery();
                unitsTableExists = rs.next();
                results.put("unitsTableExists", unitsTableExists);
                LOGGER.log(Level.INFO, "Units table exists: " + unitsTableExists);
            }
            
            if (unitsTableExists) {
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SELECT * FROM units WHERE unit_code = ?")) {
                    stmt.setString(1, unitCode);
                    ResultSet rs = stmt.executeQuery();
                    unitExists = rs.next();
                    results.put("unitExists", unitExists);
                    LOGGER.log(Level.INFO, "Unit " + unitCode + " exists: " + unitExists);
                }
            }
            
            // Check if assessments table exists and has data for this unit
            boolean assessmentsTableExists = false;
            int assessmentCount = 0;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SHOW TABLES LIKE 'assessments'")) {
                ResultSet rs = stmt.executeQuery();
                assessmentsTableExists = rs.next();
                results.put("assessmentsTableExists", assessmentsTableExists);
                LOGGER.log(Level.INFO, "Assessments table exists: " + assessmentsTableExists);
            }
            
            if (assessmentsTableExists) {
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(
                        "SELECT COUNT(*) FROM assessments WHERE unit_code = ?")) {
                    stmt.setString(1, unitCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        assessmentCount = rs.getInt(1);
                    }
                    results.put("assessmentCount", assessmentCount);
                    LOGGER.log(Level.INFO, "Assessment count for unit " + unitCode + ": " + assessmentCount);
                }
            }
            
            // Check if student_assessments table exists and has data for this unit
            boolean studentAssessmentsTableExists = false;
            int studentAssessmentCount = 0;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SHOW TABLES LIKE 'student_assessments'")) {
                ResultSet rs = stmt.executeQuery();
                studentAssessmentsTableExists = rs.next();
                results.put("studentAssessmentsTableExists", studentAssessmentsTableExists);
                LOGGER.log(Level.INFO, "Student assessments table exists: " + studentAssessmentsTableExists);
            }
            
            if (studentAssessmentsTableExists && assessmentsTableExists) {
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(
                        "SELECT COUNT(*) FROM student_assessments sa " +
                        "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                        "WHERE a.unit_code = ?")) {
                    stmt.setString(1, unitCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        studentAssessmentCount = rs.getInt(1);
                    }
                    results.put("studentAssessmentCount", studentAssessmentCount);
                    LOGGER.log(Level.INFO, "Student assessment count for unit " + unitCode + ": " + studentAssessmentCount);
                }
            }
            
            // Check if practice_assessments table exists and has data for this unit
            boolean practiceAssessmentsTableExists = false;
            int practiceAssessmentCount = 0;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SHOW TABLES LIKE 'practice_assessments'")) {
                ResultSet rs = stmt.executeQuery();
                practiceAssessmentsTableExists = rs.next();
                results.put("practiceAssessmentsTableExists", practiceAssessmentsTableExists);
                LOGGER.log(Level.INFO, "Practice assessments table exists: " + practiceAssessmentsTableExists);
            }
            
            if (practiceAssessmentsTableExists && assessmentsTableExists) {
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(
                        "SELECT COUNT(*) FROM practice_assessments pa " +
                        "JOIN assessments a ON pa.assessment_id = a.assessment_id " +
                        "WHERE a.unit_code = ?")) {
                    stmt.setString(1, unitCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        practiceAssessmentCount = rs.getInt(1);
                    }
                    results.put("practiceAssessmentCount", practiceAssessmentCount);
                    LOGGER.log(Level.INFO, "Practice assessment count for unit " + unitCode + ": " + practiceAssessmentCount);
                }
            }
            
            // Check if students table exists and has enrollments for this unit
            boolean studentsTableExists = false;
            boolean enrollmentsTableExists = false;
            int enrollmentCount = 0;
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SHOW TABLES LIKE 'students'")) {
                ResultSet rs = stmt.executeQuery();
                studentsTableExists = rs.next();
                results.put("studentsTableExists", studentsTableExists);
                LOGGER.log(Level.INFO, "Students table exists: " + studentsTableExists);
            }
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement("SHOW TABLES LIKE 'enrollments'")) {
                ResultSet rs = stmt.executeQuery();
                enrollmentsTableExists = rs.next();
                results.put("enrollmentsTableExists", enrollmentsTableExists);
                LOGGER.log(Level.INFO, "Enrollments table exists: " + enrollmentsTableExists);
            }
            
            if (studentsTableExists && enrollmentsTableExists) {
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(
                        "SELECT COUNT(*) FROM enrollments WHERE unit_code = ?")) {
                    stmt.setString(1, unitCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        enrollmentCount = rs.getInt(1);
                    }
                    results.put("enrollmentCount", enrollmentCount);
                    LOGGER.log(Level.INFO, "Enrollment count for unit " + unitCode + ": " + enrollmentCount);
                }
            }
            
            // Overall assessment
            boolean hasRequiredTables = unitsTableExists && assessmentsTableExists && 
                    studentAssessmentsTableExists && studentsTableExists && enrollmentsTableExists;
            boolean hasData = unitExists && assessmentCount > 0 && 
                    (studentAssessmentCount > 0 || practiceAssessmentCount > 0) && enrollmentCount > 0;
            
            results.put("hasRequiredTables", hasRequiredTables);
            results.put("hasData", hasData);
            
            LOGGER.log(Level.INFO, "Database verification results for unit " + unitCode + ":");
            LOGGER.log(Level.INFO, "  Has required tables: " + hasRequiredTables);
            LOGGER.log(Level.INFO, "  Has data: " + hasData);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error verifying database", e);
            results.put("error", e.getMessage());
        }
        
        return results;
    }
}
