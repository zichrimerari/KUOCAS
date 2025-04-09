package book.javafx.kenyattacatsystem.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a student's attempt at an assessment.
 * Tracks start and end times, score, and student responses.
 */
public class StudentAssessmentAttempt {
    private String attemptId;
    private String studentId;
    private String assessmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int score;
    private int totalPossible;
    private String status; // IN_PROGRESS, COMPLETED, SUBMITTED, GRADED
    private boolean isOffline;
    private boolean isPractice;
    private Map<String, StudentResponse> responses; // Map of questionId to response
    
    /**
     * Default constructor
     */
    public StudentAssessmentAttempt() {
        this.startTime = LocalDateTime.now();
        this.status = "IN_PROGRESS";
        this.isOffline = false;
        this.isPractice = false;
        this.responses = new HashMap<>();
        this.score = 0;
    }
    
    /**
     * Constructor with essential fields
     *
     * @param attemptId    The attempt ID
     * @param studentId    The student ID
     * @param assessmentId The assessment ID
     */
    public StudentAssessmentAttempt(String attemptId, String studentId, String assessmentId) {
        this.attemptId = attemptId;
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.startTime = LocalDateTime.now();
        this.status = "IN_PROGRESS";
        this.isOffline = false;
        this.isPractice = false;
        this.responses = new HashMap<>();
        this.score = 0;
    }
    
    /**
     * Adds a response to this attempt
     *
     * @param questionId The question ID
     * @param response   The student's response
     */
    public void addResponse(String questionId, StudentResponse response) {
        responses.put(questionId, response);
    }
    
    /**
     * Gets a response for a question
     *
     * @param questionId The question ID
     * @return The student's response, or null if not found
     */
    public StudentResponse getResponse(String questionId) {
        return responses.get(questionId);
    }
    
    /**
     * Gets all responses for this attempt
     *
     * @return A list of all responses
     */
    public List<StudentResponse> getAllResponses() {
        return new ArrayList<>(responses.values());
    }
    
    /**
     * Calculates the current score based on all responses
     *
     * @return The calculated score
     */
    public int calculateScore() {
        int calculatedScore = 0;
        for (StudentResponse response : responses.values()) {
            calculatedScore += response.getMarksAwarded();
        }
        this.score = calculatedScore;
        return calculatedScore;
    }
    
    /**
     * Submits the attempt, setting the end time and status
     */
    public void submit() {
        this.endTime = LocalDateTime.now();
        this.status = "SUBMITTED";
        calculateScore();
    }
    
    // Getters and Setters
    public String getAttemptId() {
        return attemptId;
    }
    
    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getAssessmentId() {
        return assessmentId;
    }
    
    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getTotalPossible() {
        return totalPossible;
    }
    
    public void setTotalPossible(int totalPossible) {
        this.totalPossible = totalPossible;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isOffline() {
        return isOffline;
    }
    
    public void setOffline(boolean offline) {
        isOffline = offline;
    }
    
    public boolean isPractice() {
        return isPractice;
    }
    
    public void setPractice(boolean isPractice) {
        this.isPractice = isPractice;
    }
    
    public Map<String, StudentResponse> getResponses() {
        return responses;
    }
    
    public void setResponses(Map<String, StudentResponse> responses) {
        this.responses = responses;
    }
}