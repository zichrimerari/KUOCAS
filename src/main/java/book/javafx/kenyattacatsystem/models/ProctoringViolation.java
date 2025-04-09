package book.javafx.kenyattacatsystem.models;

import java.time.LocalDateTime;

/**
 * Represents a proctoring violation detected during an assessment.
 */
public class ProctoringViolation {
    private int violationId;
    private String assessmentId;
    private String assessmentTitle;
    private String studentId;
    private String studentName;
    private String unitCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
    
    /**
     * Default constructor
     */
    public ProctoringViolation() {
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param violationId The violation ID
     * @param assessmentId The assessment ID
     * @param assessmentTitle The assessment title
     * @param studentId The student ID
     * @param studentName The student name
     * @param unitCode The unit code
     * @param startTime The start time of the violation
     * @param endTime The end time of the violation
     * @param durationSeconds The duration in seconds
     */
    public ProctoringViolation(int violationId, String assessmentId, String assessmentTitle, String studentId, 
                              String studentName, String unitCode, LocalDateTime startTime, 
                              LocalDateTime endTime, long durationSeconds) {
        this.violationId = violationId;
        this.assessmentId = assessmentId;
        this.assessmentTitle = assessmentTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.unitCode = unitCode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
    }
    
    /**
     * Gets the violation ID
     * 
     * @return The violation ID
     */
    public int getViolationId() {
        return violationId;
    }
    
    /**
     * Sets the violation ID
     * 
     * @param violationId The violation ID
     */
    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }
    
    /**
     * Gets the assessment ID
     * 
     * @return The assessment ID
     */
    public String getAssessmentId() {
        return assessmentId;
    }
    
    /**
     * Sets the assessment ID
     * 
     * @param assessmentId The assessment ID
     */
    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }
    
    /**
     * Gets the assessment title
     * 
     * @return The assessment title
     */
    public String getAssessmentTitle() {
        return assessmentTitle;
    }
    
    /**
     * Sets the assessment title
     * 
     * @param assessmentTitle The assessment title
     */
    public void setAssessmentTitle(String assessmentTitle) {
        this.assessmentTitle = assessmentTitle;
    }
    
    /**
     * Gets the student ID
     * 
     * @return The student ID
     */
    public String getStudentId() {
        return studentId;
    }
    
    /**
     * Sets the student ID
     * 
     * @param studentId The student ID
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    /**
     * Gets the student name
     * 
     * @return The student name
     */
    public String getStudentName() {
        return studentName;
    }
    
    /**
     * Sets the student name
     * 
     * @param studentName The student name
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    /**
     * Gets the unit code
     * 
     * @return The unit code
     */
    public String getUnitCode() {
        return unitCode;
    }
    
    /**
     * Sets the unit code
     * 
     * @param unitCode The unit code
     */
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
    
    /**
     * Gets the start time
     * 
     * @return The start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Sets the start time
     * 
     * @param startTime The start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Gets the end time
     * 
     * @return The end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * Sets the end time
     * 
     * @param endTime The end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Gets the duration in seconds
     * 
     * @return The duration in seconds
     */
    public long getDurationSeconds() {
        return durationSeconds;
    }
    
    /**
     * Sets the duration in seconds
     * 
     * @param durationSeconds The duration in seconds
     */
    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    /**
     * Gets a formatted duration string
     * 
     * @return A formatted duration string (e.g. "2m 30s")
     */
    public String getFormattedDuration() {
        if (durationSeconds < 60) {
            return durationSeconds + "s";
        }
        
        long minutes = durationSeconds / 60;
        long remainingSeconds = durationSeconds % 60;
        
        if (minutes < 60) {
            return minutes + "m " + remainingSeconds + "s";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        return hours + "h " + remainingMinutes + "m " + remainingSeconds + "s";
    }
    
    /**
     * Gets the severity level of the violation
     * 
     * @return The severity level (HIGH, MEDIUM, LOW)
     */
    public String getSeverityLevel() {
        if (durationSeconds > 300) { // More than 5 minutes
            return "HIGH";
        } else if (durationSeconds > 60) { // More than 1 minute
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
