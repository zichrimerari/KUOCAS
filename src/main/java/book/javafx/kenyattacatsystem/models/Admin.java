package book.javafx.kenyattacatsystem.models;

/**
 * Represents an administrator user in the system.
 * Extends the base User class with admin-specific properties.
 */
public class Admin extends User {
    private String adminId;
    private String department;
    private String accessLevel;
    
    /**
     * Default constructor
     */
    public Admin() {
        super();
    }
    
    /**
     * Constructor with all fields
     *
     * @param userId      The user ID
     * @param username    The username
     * @param password    The password
     * @param fullName    The full name
     * @param email       The email
     * @param adminId     The admin ID
     * @param department  The department
     * @param accessLevel The access level
     */
    public Admin(String userId, String username, String password, String fullName, String email,
                String adminId, String department, String accessLevel) {
        super(userId, username, password, fullName, email, "Admin");
        this.adminId = adminId;
        this.department = department;
        this.accessLevel = accessLevel;
    }
    
    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }
    
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getAccessLevel() {
        return accessLevel;
    }
    
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "userId='" + getUserId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", adminId='" + adminId + '\'' +
                ", department='" + department + '\'' +
                ", accessLevel='" + accessLevel + '\'' +
                '}';
    }
}