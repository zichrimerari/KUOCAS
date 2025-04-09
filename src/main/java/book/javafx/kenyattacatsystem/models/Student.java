package book.javafx.kenyattacatsystem.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student user in the system.
 * Extends the base User class with student-specific properties.
 */
public class Student extends User {
    private String studentId;
    private String program;
    private int yearOfStudy;
    private List<String> enrolledUnits;
    
    /**
     * Default constructor
     */
    public Student() {
        super();
        this.enrolledUnits = new ArrayList<>();
    }
    
    /**
     * Constructor with all fields
     *
     * @param userId      The user ID
     * @param username    The username
     * @param password    The password
     * @param fullName    The full name
     * @param email       The email
     * @param studentId   The student ID
     * @param program     The program of study
     * @param yearOfStudy The year of study
     */
    public Student(String userId, String username, String password, String fullName, String email,
                  String studentId, String program, int yearOfStudy) {
        super(userId, username, password, fullName, email, "Student");
        this.studentId = studentId;
        this.program = program;
        this.yearOfStudy = yearOfStudy;
        this.enrolledUnits = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        this.program = program;
    }
    
    public int getYearOfStudy() {
        return yearOfStudy;
    }
    
    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }
    
    public List<String> getEnrolledUnits() {
        return enrolledUnits;
    }
    
    public void setEnrolledUnits(List<String> enrolledUnits) {
        this.enrolledUnits = enrolledUnits;
    }
    
    /**
     * Enrolls the student in a unit
     *
     * @param unitCode The unit code
     */
    public void enrollInUnit(String unitCode) {
        if (!enrolledUnits.contains(unitCode)) {
            enrolledUnits.add(unitCode);
        }
    }
    
    /**
     * Adds a unit to the student's enrolled units list
     * This method is used by the UserService when loading enrolled units
     *
     * @param unitCode The unit code to add
     */
    public void addEnrolledUnit(String unitCode) {
        if (!enrolledUnits.contains(unitCode)) {
            enrolledUnits.add(unitCode);
        }
    }
    
    /**
     * Unenrolls the student from a unit
     *
     * @param unitCode The unit code
     */
    public void unenrollFromUnit(String unitCode) {
        enrolledUnits.remove(unitCode);
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "userId='" + getUserId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", studentId='" + studentId + '\'' +
                ", program='" + program + '\'' +
                ", yearOfStudy=" + yearOfStudy +
                ", enrolledUnits=" + enrolledUnits +
                '}';
    }
}