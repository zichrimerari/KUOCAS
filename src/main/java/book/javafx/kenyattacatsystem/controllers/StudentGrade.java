package book.javafx.kenyattacatsystem.controllers;

/**
 * Represents a student's grade for an assessment.
 * This is a helper class for the LecturerDashboardController.
 */
public class StudentGrade {
    private String studentId;
    private String studentName;
    private String assessmentTitle;
    private double score;
    
    /**
     * Default constructor
     */
    public StudentGrade() {
    }
    
    /**
     * Constructor with all fields
     *
     * @param studentId      The student ID
     * @param studentName    The student name
     * @param assessmentTitle The assessment title
     * @param score          The score
     */
    public StudentGrade(String studentId, String studentName, String assessmentTitle, double score) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.assessmentTitle = assessmentTitle;
        this.score = score;
    }
    
    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getAssessmentTitle() {
        return assessmentTitle;
    }
    
    public void setAssessmentTitle(String assessmentTitle) {
        this.assessmentTitle = assessmentTitle;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
}