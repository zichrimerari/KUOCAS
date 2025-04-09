package book.javafx.kenyattacatsystem.models;

import book.javafx.kenyattacatsystem.services.QuestionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an assessment in the system.
 * This can be a formal CAT or a practice test.
 */
public class Assessment {
    private String assessmentId;
    private String title;
    private String description;
    private String unitCode; // The unit this assessment belongs to
    private String createdBy; // User ID of the creator (lecturer or system)
    private LocalDateTime creationDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int durationMinutes;
    private boolean isActive;
    private boolean isPractice; // Whether this is a practice test or formal assessment
    private List<String> questionIds; // List of question IDs in this assessment
    private int totalMarks;
    private boolean allowOfflineAttempt; // Whether students can download and take offline
    private String difficulty; // Difficulty level of the assessment (e.g., Easy, Medium, Hard)
    private String questionType; // Type of questions in the assessment (e.g., Multiple Choice, True/False)
    private int questionCount; // Number of questions in the assessment
    private String status; // Status of the assessment (e.g., NOT_STARTED, IN_PROGRESS, COMPLETED)
    private double score; // Score achieved in the assessment (for practice tests)
    private double percentage; // Percentage score achieved in the assessment
    private String grade; // Grade achieved in the assessment (e.g., A, B, C, D, F)
    private String practiceId; // ID from the practice_assessments table for practice tests

    /**
     * Default constructor
     */
    public Assessment() {
        this.questionIds = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        this.isActive = false;
        this.difficulty = "";
        this.questionType = "";
        this.questionCount = 0;
        this.status = "NOT_STARTED";
        this.score = 0.0;
        this.percentage = 0.0;
        this.grade = "N/A";
        this.practiceId = "";
    }

    /**
     * Constructor with essential fields
     *
     * @param assessmentId   The assessment ID
     * @param title          The assessment title
     * @param unitCode       The unit code
     * @param createdBy      The creator's user ID
     * @param durationMinutes The duration in minutes
     * @param isPractice     Whether this is a practice test
     */
    public Assessment(String assessmentId, String title, String unitCode, String createdBy,
                     int durationMinutes, boolean isPractice) {
        this.assessmentId = assessmentId;
        this.title = title;
        this.unitCode = unitCode;
        this.createdBy = createdBy;
        this.durationMinutes = durationMinutes;
        this.isPractice = isPractice;
        this.questionIds = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        this.isActive = false;
        this.totalMarks = 0;
        this.allowOfflineAttempt = false;
        this.difficulty = "";
        this.questionType = "";
        this.questionCount = 0;
        this.status = "NOT_STARTED";
        this.score = 0.0;
        this.percentage = 0.0;
        this.grade = "N/A";
        this.practiceId = "";
    }

    /**
     * Constructor with all fields
     *
     * @param assessmentId      The assessment ID
     * @param title             The assessment title
     * @param description       The assessment description
     * @param unitCode          The unit code
     * @param createdBy         The creator's user ID
     * @param creationDate      The creation date
     * @param startDateTime     The start date and time
     * @param endDateTime       The end date and time
     * @param durationMinutes   The duration in minutes
     * @param isActive          Whether the assessment is active
     * @param isPractice        Whether this is a practice test
     * @param totalMarks        The total marks for the assessment
     * @param allowOfflineAttempt Whether students can take offline
     * @param difficulty        The difficulty level of the assessment
     * @param questionType      The type of questions in the assessment
     * @param questionCount     The number of questions in the assessment
     */
    public Assessment(String assessmentId, String title, String description, String unitCode,
                     String createdBy, LocalDateTime creationDate, LocalDateTime startDateTime,
                     LocalDateTime endDateTime, int durationMinutes, boolean isActive,
                     boolean isPractice, int totalMarks, boolean allowOfflineAttempt, String difficulty,
                     String questionType, int questionCount) {
        this.assessmentId = assessmentId;
        this.title = title;
        this.description = description;
        this.unitCode = unitCode;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.durationMinutes = durationMinutes;
        this.isActive = isActive;
        this.isPractice = isPractice;
        this.questionIds = new ArrayList<>();
        this.totalMarks = totalMarks;
        this.allowOfflineAttempt = allowOfflineAttempt;
        this.difficulty = difficulty;
        this.questionType = questionType;
        this.questionCount = questionCount;
        this.status = "NOT_STARTED";
        this.score = 0.0;
        this.percentage = 0.0;
        this.grade = "N/A";
        this.practiceId = "";
    }

    // Getters and Setters
    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        // Ensure title is uppercase with no spaces for aesthetics
        if (title != null) {
            this.title = title.toUpperCase().replaceAll("\\s+", "");
        } else {
            this.title = null;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /**
     * Alias for setDurationMinutes to maintain compatibility with existing code
     * 
     * @param durationMinutes The duration in minutes
     */
    public void setDuration(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isPractice() {
        return isPractice;
    }

    public void setPractice(boolean practice) {
        isPractice = practice;
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public boolean isAllowOfflineAttempt() {
        return allowOfflineAttempt;
    }

    public void setAllowOfflineAttempt(boolean allowOfflineAttempt) {
        this.allowOfflineAttempt = allowOfflineAttempt;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
    public double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPracticeId() {
        return practiceId;
    }

    public void setPracticeId(String practiceId) {
        this.practiceId = practiceId;
    }

    /**
     * Adds a question to this assessment
     *
     * @param questionId The question ID
     */
    public void addQuestion(String questionId) {
        if (!questionIds.contains(questionId)) {
            questionIds.add(questionId);
            this.questionCount++;
        }
    }

    /**
     * Adds a question ID to this assessment
     * This is an alias for addQuestion method to maintain compatibility
     *
     * @param questionId The question ID
     */
    public void addQuestionId(String questionId) {
        addQuestion(questionId);
    }

    /**
     * Removes a question from this assessment
     *
     * @param questionId The question ID
     */
    public void removeQuestion(String questionId) {
        if (questionIds.remove(questionId)) {
            this.questionCount--;
        }
    }

    /**
     * Calculates the total marks for this assessment based on its questions
     * This is a placeholder method that would typically query the database for question marks
     * and sum them up
     *
     * @return The total marks
     */
    public int calculateTotalMarks() {
        // In a real implementation, this would query the database for each question's marks
        // and sum them up
        return totalMarks;
    }

    /**
     * Gets the questions for this assessment
     * This method loads the questions from the database using the question IDs
     *
     * @return A list of Question objects
     */
    public List<Question> getQuestions() {
        // In a real implementation, this would query the database for each question
        List<Question> questions = new ArrayList<>();
        for (String questionId : questionIds) {
            Question question = QuestionService.getQuestionById(questionId);
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "assessmentId='" + assessmentId + '\'' +
                ", title='" + title + '\'' +
                ", unitCode='" + unitCode + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", durationMinutes=" + durationMinutes +
                ", isActive=" + isActive +
                ", isPractice=" + isPractice +
                ", questionCount=" + questionCount +
                ", totalMarks=" + totalMarks +
                ", difficulty='" + difficulty + '\'' +
                ", questionType='" + questionType + '\'' +
                ", status='" + status + '\'' +
                ", score=" + score +
                ", percentage=" + percentage +
                ", grade='" + grade + '\'' +
                ", practiceId='" + practiceId + '\'' +
                '}';
    }
}