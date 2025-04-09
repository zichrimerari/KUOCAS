package book.javafx.kenyattacatsystem.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an academic unit/course in the system.
 */
public class Unit {
    private String unitCode;
    private String unitName;
    private String department;
    private int creditHours;
    private String description;
    private String lecturerId; // ID of the lecturer teaching this unit
    private String lecturerName; // Name of the lecturer teaching this unit
    private List<String> enrolledStudents; // List of student IDs enrolled in this unit
    private List<String> assessments; // List of assessment IDs for this unit
    private List<String> topics; // List of topic IDs for this unit
    
    /**
     * Default constructor
     */
    public Unit() {
        this.enrolledStudents = new ArrayList<>();
        this.assessments = new ArrayList<>();
        this.topics = new ArrayList<>();
    }
    
    /**
     * Constructor with essential fields
     *
     * @param unitCode    The unit code
     * @param unitName    The unit name
     * @param department  The department offering the unit
     * @param creditHours The credit hours
     */
    public Unit(String unitCode, String unitName, String department, int creditHours) {
        this.unitCode = unitCode;
        this.unitName = unitName;
        this.department = department;
        this.creditHours = creditHours;
        this.enrolledStudents = new ArrayList<>();
        this.assessments = new ArrayList<>();
        this.topics = new ArrayList<>();
    }
    
    /**
     * Constructor with all fields
     *
     * @param unitCode        The unit code
     * @param unitName        The unit name
     * @param department      The department offering the unit
     * @param creditHours     The credit hours
     * @param description     The unit description
     * @param lecturerId      The ID of the lecturer teaching this unit
     */
    public Unit(String unitCode, String unitName, String department, int creditHours, 
               String description, String lecturerId) {
        this.unitCode = unitCode;
        this.unitName = unitName;
        this.department = department;
        this.creditHours = creditHours;
        this.description = description;
        this.lecturerId = lecturerId;
        this.enrolledStudents = new ArrayList<>();
        this.assessments = new ArrayList<>();
        this.topics = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getUnitCode() {
        return unitCode;
    }
    
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
    
    public String getUnitName() {
        return unitName;
    }
    
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public int getCreditHours() {
        return creditHours;
    }
    
    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLecturerId() {
        return lecturerId;
    }
    
    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }
    
    public String getLecturerName() {
        return lecturerName;
    }
    
    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }
    
    public List<String> getEnrolledStudents() {
        return enrolledStudents;
    }
    
    public void setEnrolledStudents(List<String> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
    
    public List<String> getAssessments() {
        return assessments;
    }
    
    public void setAssessments(List<String> assessments) {
        this.assessments = assessments;
    }
    
    public List<String> getTopics() {
        return topics;
    }
    
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
    
    /**
     * Enrolls a student in this unit
     *
     * @param studentId The student ID
     */
    public void enrollStudent(String studentId) {
        if (!enrolledStudents.contains(studentId)) {
            enrolledStudents.add(studentId);
        }
    }
    
    /**
     * Adds a student to the enrolled students list (alias for enrollStudent)
     *
     * @param studentId The student ID
     */
    public void addEnrolledStudent(String studentId) {
        enrollStudent(studentId);
    }
    
    /**
     * Unenrolls a student from this unit
     *
     * @param studentId The student ID
     */
    public void unenrollStudent(String studentId) {
        enrolledStudents.remove(studentId);
    }
    
    /**
     * Adds an assessment to this unit
     *
     * @param assessmentId The assessment ID
     */
    public void addAssessment(String assessmentId) {
        if (!assessments.contains(assessmentId)) {
            assessments.add(assessmentId);
        }
    }
    
    /**
     * Removes an assessment from this unit
     *
     * @param assessmentId The assessment ID
     */
    public void removeAssessment(String assessmentId) {
        assessments.remove(assessmentId);
    }
    
    /**
     * Adds a topic to this unit
     *
     * @param topicId The topic ID
     */
    public void addTopic(String topicId) {
        if (!topics.contains(topicId)) {
            topics.add(topicId);
        }
    }
    
    /**
     * Removes a topic from this unit
     *
     * @param topicId The topic ID
     */
    public void removeTopic(String topicId) {
        topics.remove(topicId);
    }
    
    @Override
    public String toString() {
        return "Unit{" +
                "unitCode='" + unitCode + '\'' +
                ", unitName='" + unitName + '\'' +
                ", department='" + department + '\'' +
                ", creditHours=" + creditHours +
                ", description='" + description + '\'' +
                ", lecturerId='" + lecturerId + '\'' +
                ", lecturerName='" + lecturerName + '\'' +
                ", enrolledStudents=" + enrolledStudents.size() +
                ", assessments=" + assessments.size() +
                ", topics=" + topics.size() +
                '}';
    }
}