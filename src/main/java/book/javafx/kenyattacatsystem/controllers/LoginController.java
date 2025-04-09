package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.User;
import book.javafx.kenyattacatsystem.models.Admin;
import book.javafx.kenyattacatsystem.models.Student;
import book.javafx.kenyattacatsystem.models.Lecturer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller for the login view.
 * Handles user authentication and navigation to appropriate dashboards.
 */
public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;



    @FXML
    private Button loginButton;

    @FXML
    private Label errorMessage;

    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    private static final Map<String, String> ROLE_DASHBOARD_MAP = Map.of(
            "Student", "/book/javafx/kenyattacatsystem/views/student-dashboard.fxml",
            "Lecturer", "/book/javafx/kenyattacatsystem/views/lecturer-dashboard.fxml",
            "Admin", "/book/javafx/kenyattacatsystem/views/admin-dashboard.fxml"
    );

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        errorMessage.setVisible(false); // Ensure error message is hidden initially
    }

    /**
     * Handles the login button click event.
     */
    @FXML
    protected void handleLogin(ActionEvent event) {
        errorMessage.setVisible(false); // Reset error visibility

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Improved validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        if (username.length() < 4 || password.length() < 6) {
            showError("Username must be at least 4 characters and password at least 6 characters.");
            return;
        }
        
        // Debug logging
        logger.info("Attempting login with username: " + username);

        // Authentication logic - don't specify role to allow any valid role
        User authenticatedUser = authenticate(username, password, null);
        if (authenticatedUser != null) {
            String role = authenticatedUser.getRole();
            String dashboardView = ROLE_DASHBOARD_MAP.get(role);
            loadView(dashboardView, "Kenyatta University CAT System - " + role, authenticatedUser);
        } else {
            showError("Invalid username or password");
        }
    }

    /**
     * Handles the register hyperlink click event.
     */
    @FXML
    protected void handleRegister(ActionEvent event) {
        try {
            // Use a simpler approach for loading FXML
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/book/javafx/kenyattacatsystem/views/register-view.fxml"));
            
            if (loader.getLocation() == null) {
                throw new IOException("Could not find registration resource");
            }
            
            logger.info("Loading registration FXML from: " + loader.getLocation());
            Parent root = loader.load();
            
            // Load CSS
            URL cssUrl = LoginController.class.getResource("/book/javafx/kenyattacatsystem/styles/main.css");
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                logger.info("Added CSS from: " + cssUrl);
            } else {
                logger.warning("Could not find CSS resource");
            }
            
            stage.setScene(scene);
            stage.setTitle("Kenyatta University CAT System - Registration");
            stage.centerOnScreen();
        } catch (IOException e) {
            logger.severe("Error loading registration page: " + e.getMessage());
            showError("Error loading registration page: " + e.getMessage());
        }
    }

    /**
     * Loads the specified view with the given title and passes the authenticated user to the controller.
     * 
     * @param fxmlPath The path to the FXML file
     * @param title The window title
     * @param user The authenticated user
     */
    private void loadView(String fxmlPath, String title, User user) {
        try {
            // Use a simpler approach for loading FXML
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(fxmlPath));
            
            if (loader.getLocation() == null) {
                throw new IOException("Could not find resource: " + fxmlPath);
            }
            
            logger.info("Loading FXML from: " + loader.getLocation());
            Parent root = loader.load();
            
            // Get the controller and set the user
            Object controller = loader.getController();
            if (controller instanceof AdminDashboardController && user instanceof book.javafx.kenyattacatsystem.models.Admin) {
                ((AdminDashboardController) controller).setCurrentAdmin((book.javafx.kenyattacatsystem.models.Admin) user);
                logger.info("Admin user set in AdminDashboardController");
            } else if (controller instanceof StudentDashboardController && user instanceof book.javafx.kenyattacatsystem.models.Student) {
                ((StudentDashboardController) controller).setCurrentStudent((book.javafx.kenyattacatsystem.models.Student) user);
                logger.info("Student user set in StudentDashboardController");
            } else if (controller instanceof LecturerDashboardController && user instanceof book.javafx.kenyattacatsystem.models.Lecturer) {
                ((LecturerDashboardController) controller).setCurrentLecturer((book.javafx.kenyattacatsystem.models.Lecturer) user);
                logger.info("Lecturer user set in LecturerDashboardController");
            }
            
            // Load CSS
            URL cssUrl = LoginController.class.getResource("/book/javafx/kenyattacatsystem/styles/main.css");
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                logger.info("Added CSS from: " + cssUrl);
            } else {
                logger.warning("Could not find CSS resource");
            }
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            logger.severe("Error loading page: " + e.getMessage());
            showError("Error loading page: " + e.getMessage());
        }
    }

    /**
     * Authenticates the user using the UserService.
     * 
     * @return The authenticated user, or null if authentication fails
     */
    private User authenticate(String username, String password, String role) {
        try {
            logger.info("Authenticating user with role: " + role);
            User user = book.javafx.kenyattacatsystem.services.UserService.authenticateUser(username, password, role);
            if (user != null) {
                logger.info("Authentication successful for user: " + username);
            } else {
                logger.warning("Authentication failed for user: " + username + " with role: " + role);
            }
            return user;
        } catch (Exception e) {
            logger.severe("Authentication error: " + e.getMessage());
            showError("Authentication error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
    
    /**
     * Determines the user role based on email domain.
     * 
     * @param email The email address
     * @return The determined role (Student, Lecturer, or Admin)
     */
    private String determineRoleFromEmail(String email) {
        // Default to Student if email doesn't contain @ (not an email format)
        if (!email.contains("@")) {
            return "Student";
        }
        
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        
        // Determine role based on domain patterns
        if (domain.contains("student") || domain.contains("learner") || domain.startsWith("s.")) {
            return "Student";
        } else if (domain.contains("lecturer") || domain.contains("faculty") || domain.contains("teacher") || domain.startsWith("l.")) {
            return "Lecturer";
        } else if (domain.contains("admin") || domain.contains("staff") || domain.startsWith("a.")) {
            return "Admin";
        }
        
        // Default to Student if no specific pattern is matched
        return "Student";
    }
}
