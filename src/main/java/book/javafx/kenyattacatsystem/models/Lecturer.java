package book.javafx.kenyattacatsystem.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a lecturer user in the system.
 * Extends the base User class with lecturer-specific properties.
 */
public class Lecturer extends User {
    private String staffId;
    private String department;
    private List<String> taughtUnits;
    
    /**
     * Default constructor
     */
    public Lecturer() {
        super();
        this.taughtUnits = new ArrayList<>();
    }
    
    /**
     * Constructor with all fields
     *
     * @param userId     The user ID
     * @param username   The username
     * @param password   The password
     * @param fullName   The full name
     * @param email      The email
     * @param staffId    The staff ID
     * @param department The department
     */
    public Lecturer(String userId, String username, String password, String fullName, String email,
                   String staffId, String department) {
        super(userId, username, password, fullName, email, "Lecturer");
        this.staffId = staffId;
        this.department = department;
        this.taughtUnits = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getStaffId() {
        return staffId;
    }
    
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public List<String> getTaughtUnits() {
        return taughtUnits;
    }
    
    public void setTaughtUnits(List<String> taughtUnits) {
        this.taughtUnits = taughtUnits;
    }
    
    /**
     * Adds a unit to the lecturer's taught units
     *
     * @param unitCode The unit code
     */
    public void addTaughtUnit(String unitCode) {
        if (!taughtUnits.contains(unitCode)) {
            taughtUnits.add(unitCode);
        }
    }
    
    /**
     * Removes a unit from the lecturer's taught units
     *
     * @param unitCode The unit code
     */
    public void removeTaughtUnit(String unitCode) {
        taughtUnits.remove(unitCode);
    }
    
    @Override
    public String toString() {
        return getFullName() + " (" + staffId + ")";
    }
}