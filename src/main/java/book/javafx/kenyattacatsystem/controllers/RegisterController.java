package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.KenyattaCatSystemApp;
import book.javafx.kenyattacatsystem.models.Admin;
import book.javafx.kenyattacatsystem.models.Lecturer;
import book.javafx.kenyattacatsystem.models.Student;
import book.javafx.kenyattacatsystem.models.User;
import book.javafx.kenyattacatsystem.services.UserService;
import book.javafx.kenyattacatsystem.utils.PasswordUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller for the registration view.
 * Handles user registration with automatic role determination based on email domain.
 */
public class RegisterController {
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorMessage;
    
    // Field-specific error labels
    @FXML
    private Label firstNameError;
    
    @FXML
    private Label lastNameError;
    
    @FXML
    private Label emailError;
    
    @FXML
    private Label passwordError;
    
    @FXML
    private Label confirmPasswordError;
    
    @FXML
    private Label successMessage;
    
    @FXML
    private ProgressIndicator progressIndicator;
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // No initialization needed for the simplified form
    }
    
    /**
     * Handles the register button click event.
     * Validates input and registers the user with role determined by email domain.
     *
     * @param event The action event
     */
    @FXML
    protected void handleRegister(ActionEvent event) {
        // Reset all error messages
        resetErrors();
        
        // Get form fields
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate fields
        if (!validateFields(firstName, lastName, email, password, confirmPassword)) {
            return;
        }
        
        // Show progress indicator
        showProgress();
        
        // Determine role based on email domain
        String role = determineRoleFromEmail(email);
        
        // Disable the register button to prevent multiple submissions
        registerButton.setDisable(true);
        
        // Run registration in a background thread to keep UI responsive
        new Thread(() -> {
            User registeredUser = null;
            
            try {
                // Generate a username from first and last name
                String username = generateUsername(firstName, lastName);
                
                // Create full name
                String fullName = firstName + " " + lastName;
                
                // Register user based on determined role
                switch (role) {
                    case "Student" -> {
                        // For students, use email prefix as student ID
                        String studentId = email.substring(0, email.indexOf('@'));
                        registeredUser = UserService.registerStudent(
                                username, password, fullName, email, studentId, "Not Specified", 1);
                    }
                    case "Lecturer" -> {
                        // For lecturers, use email prefix as staff ID
                        String staffId = email.substring(0, email.indexOf('@'));
                        registeredUser = UserService.registerLecturer(
                                username, password, fullName, email, staffId, "Not Specified");
                    }
                    case "Admin" -> {
                        // For admins, use email prefix as admin ID
                        String adminId = email.substring(0, email.indexOf('@'));
                        registeredUser = UserService.registerAdmin(
                                username, password, fullName, email, adminId, "Not Specified", "Basic");
                    }
                }
                
                // Final result handling on UI thread
                final User finalUser = registeredUser;
                Platform.runLater(() -> handleRegistrationResult(finalUser));
                
            } catch (Exception e) {
                // Handle errors on UI thread
                Platform.runLater(() -> handleRegistrationError(e));
            }
        }).start();
    }
    
    /**
     * Handles the cancel button click event.
     * Returns to the login view.
     *
     * @param event The action event
     */
    @FXML
    protected void handleCancel(ActionEvent event) {
        showLoginView();
    }
    
    /**
     * Shows the login view.
     */
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(KenyattaCatSystemApp.class.getResource("views/login-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(KenyattaCatSystemApp.class.getResource("styles/main.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading login view: " + e.getMessage());
        }
    }
    
    // Method moved to avoid duplication
    
    /**
     * Shows the progress indicator.
     */
    private void showProgress() {
        progressIndicator.setVisible(true);
    }
    
    /**
     * Hides the progress indicator.
     */
    private void hideProgress() {
        progressIndicator.setVisible(false);
    }
    
    /**
     * Shows an error message in the main error label.
     *
     * @param message The error message
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        successMessage.setVisible(false);
    }
    
    /**
     * Shows a success message.
     *
     * @param message The success message
     */
    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        errorMessage.setVisible(false);
    }
    
    // Method moved to avoid duplication
    
    /**
     * Resets all error messages.
     */
    private void resetErrors() {
        errorMessage.setVisible(false);
        successMessage.setVisible(false);
        firstNameError.setVisible(false);
        lastNameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
    }
    
    /**
     * Shows a field-specific error message.
     *
     * @param errorLabel The error label to show
     * @param message The error message
     */
    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Checks if a username already exists in the database.
     * 
     * @param username The username to check
     * @return True if the username exists, false otherwise
     */
    private boolean isUsernameExists(String username) {
        try {
            return UserService.isUsernameExists(username);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Checks if an email already exists in the database.
     * 
     * @param email The email to check
     * @return True if the email exists, false otherwise
     */
    private boolean isEmailExists(String email) {
        try {
            return UserService.isEmailExists(email);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates the registration fields.
     * 
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param email The user's email
     * @param password The password
     * @param confirmPassword The confirmed password
     * @return True if all validations pass, false otherwise
     */
    private boolean validateFields(String firstName, String lastName, String email, String password, String confirmPassword) {
        boolean isValid = true;
        
        // Validate first name
        if (firstName.isEmpty()) {
            showFieldError(firstNameError, "First name is required");
            isValid = false;
        } else if (firstName.length() < 2) {
            showFieldError(firstNameError, "First name must be at least 2 characters");
            isValid = false;
        }
        
        // Validate last name
        if (lastName.isEmpty()) {
            showFieldError(lastNameError, "Last name is required");
            isValid = false;
        } else if (lastName.length() < 2) {
            showFieldError(lastNameError, "Last name must be at least 2 characters");
            isValid = false;
        }
        
        // Validate email
        if (email.isEmpty()) {
            showFieldError(emailError, "Email is required");
            isValid = false;
        } else if (!email.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showFieldError(emailError, "Invalid email format");
            isValid = false;
        } else if (isEmailExists(email)) {
            showFieldError(emailError, "Email already exists");
            isValid = false;
        }
        
        // Validate password
        if (password.isEmpty()) {
            showFieldError(passwordError, "Password is required");
            isValid = false;
        } else if (!book.javafx.kenyattacatsystem.utils.PasswordUtil.isStrongPassword(password)) {
            showFieldError(passwordError, "Password must be at least 6 characters");
            isValid = false;
        }
        
        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordError, "Confirm password is required");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            showFieldError(confirmPasswordError, "Passwords do not match");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Determines the user role based on email domain.
     * 
     * @param email The email address
     * @return The determined role (Student, Lecturer, or Admin)
     */
    private String determineRoleFromEmail(String email) {
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
    
    /**
     * Generates a username from first and last name.
     * 
     * @param firstName The first name
     * @param lastName The last name
     * @return The generated username
     */
    private String generateUsername(String firstName, String lastName) {
        // Create base username from first letter of first name and full last name
        String baseUsername = (firstName.charAt(0) + lastName).toLowerCase();
        
        // Remove spaces and special characters
        baseUsername = baseUsername.replaceAll("[^a-z0-9]", "");
        
        // Add a random number if the username might already exist
        if (isUsernameExists(baseUsername)) {
            // Add a random 3-digit number
            int randomNum = 100 + (int)(Math.random() * 900);
            baseUsername = baseUsername + randomNum;
        }
        
        return baseUsername;
    }
    
    /**
     * Handles the registration result.
     * 
     * @param user The registered user
     */
    private void handleRegistrationResult(User user) {
        // Hide progress indicator
        hideProgress();
        
        // Enable register button
        registerButton.setDisable(false);
        
        if (user != null) {
            showSuccess("Registration successful! You can now login.");
            // Clear all fields
            firstNameField.clear();
            lastNameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            showError("Registration failed. Please try again.");
        }
    }
    
    /**
     * Handles registration errors.
     * 
     * @param e The exception
     */
    private void handleRegistrationError(Exception e) {
        e.printStackTrace();
        hideProgress();
        registerButton.setDisable(false);
        showError("Registration error: " + e.getMessage());
    }
}