package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.*;
import book.javafx.kenyattacatsystem.services.ReportService;
import book.javafx.kenyattacatsystem.services.SystemSettingsService;
import book.javafx.kenyattacatsystem.services.TopicService;
import book.javafx.kenyattacatsystem.services.UnitService;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static book.javafx.kenyattacatsystem.services.UnitService.assignLecturerToUnit;
import static book.javafx.kenyattacatsystem.services.UnitService.deleteUnit;

/**
 * Controller for the Admin Dashboard view.
 * Handles admin functionalities including unit management, lecturer assignment,
 * student approval, and enrollment limit settings.
 */
public class AdminDashboardController {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());
    
    // Current logged in admin
    private Admin currentAdmin;
    
    // Navigation Icons
    @FXML private FontIcon homeIcon;
    @FXML private FontIcon usersIcon;
    @FXML private FontIcon unitsIcon;
    @FXML private FontIcon settingsIcon;
    @FXML private FontIcon reportsIcon;
    
    // Dashboard content sections
    @FXML private VBox dashboardContent;
    @FXML private VBox usersContent;
    @FXML private VBox unitsContent;
    @FXML private VBox settingsContent;
    @FXML private VBox reportsContent;
    
    // Dashboard stats
    @FXML private Text welcomeText;
    @FXML private Label dateTimeLabel;
    @FXML private Text totalUsersLabel;
    @FXML private Text totalUnitsLabel;
    @FXML private Text totalStudentsLabel;
    @FXML private Text totalLecturersLabel;
    
//    // Activity table
//    @FXML private TableView<ActivityLog> activityTable;
//    @FXML private TableColumn<ActivityLog, String> activityTimeColumn;
//    @FXML private TableColumn<ActivityLog, String> activityUserColumn;
//    @FXML private TableColumn<ActivityLog, String> activityTypeColumn;
//    @FXML private TableColumn<ActivityLog, String> activityDetailsColumn;
//
    // Users table
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, Button> userActionColumn;
    
    // Units table
    @FXML private TableView<Unit> unitsTable;
    @FXML private TableColumn<Unit, String> unitCodeColumn;
    @FXML private TableColumn<Unit, String> unitNameColumn;
    @FXML private TableColumn<Unit, String> unitDepartmentColumn;
    @FXML private TableColumn<Unit, Button> unitActionColumn;
    
    // System settings
    @FXML private TextField systemNameField;
    @FXML private TextField academicYearField;
    @FXML private TextField sessionTimeoutField;
    @FXML private TextField minPasswordLengthField;
    @FXML private ComboBox<String> passwordComplexityComboBox;
    @FXML private TextField accountLockoutField;
    @FXML private TextField maxUnitsPerSemesterField; // Added for enrollment limit
    
    // Fee clearance settings
    @FXML private CheckBox requireFeeClearanceCheckbox; // Added for fee clearance requirement
    
    // Reports components
    @FXML private ComboBox<Report> reportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reportUnitFilterComboBox;
    @FXML private ComboBox<String> reportDepartmentFilterComboBox;
    @FXML private ComboBox<String> reportRoleFilterComboBox;
    @FXML private ComboBox<String> reportFormatComboBox;
    @FXML private TableView<ReportExecution> reportExecutionsTable;
    @FXML private TableColumn<ReportExecution, LocalDateTime> executionDateColumn;
    @FXML private TableColumn<ReportExecution, String> executedByColumn;
    @FXML private TableColumn<ReportExecution, String> reportNameColumn;
    @FXML private TableColumn<ReportExecution, String> downloadColumn;
    
    // Backup settings
//    @FXML private TableView<BackupData> backupsTable;
//    @FXML private TableColumn<BackupData, String> backupDateColumn;
//    @FXML private TableColumn<BackupData, String> backupSizeColumn;
//    @FXML private TableColumn<BackupData, String> backupTypeColumn;
//    @FXML private TableColumn<BackupData, Button> backupActionColumn;
    
    // Data models
    //private ObservableList<ActivityLog> activityLogs = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Unit> units = FXCollections.observableArrayList();
    private ObservableList<Report> reports = FXCollections.observableArrayList();
    private ObservableList<ReportExecution> reportExecutions = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     * Sets up UI components and loads initial data.
     */
    @FXML
    public void initialize() {
        // Set up date time label with current date and time
        updateDateTime();
        
        // Set up users table
        setupUsersTable();
        
        // Set up units table
        setupUnitsTable();
        
        // Set up reports table
        setupReportsTable();
        
        // Set up combo boxes
        setupComboBoxes();
        
        // Load initial data
        loadDashboardStats();
        loadUsers();
        loadUnits();
        loadReports();
        
        // Set default enrollment settings
        loadEnrollmentSettings();
        
        // Initialize reports
        initializeReports();
    }
    
    /**
     * Sets the current admin user.
     *
     * @param admin The admin user
     */
    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        welcomeText.setText("Welcome, " + admin.getFullName());
    }
    
    /**
     * Updates the date and time label.
     */
    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        dateTimeLabel.setText(formattedDateTime);
    }
    
    /**
     * Loads dashboard statistics.
     */
    private void loadDashboardStats() {
        try {
            // Get counts from database
            int totalUsers = countRecords("users");
            int totalUnits = countRecords("units");
            int totalStudents = countRecords("students");
            int totalLecturers = countRecords("lecturers");
            
            // Update labels
            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalUnitsLabel.setText(String.valueOf(totalUnits));
            totalStudentsLabel.setText(String.valueOf(totalStudents));
            totalLecturersLabel.setText(String.valueOf(totalLecturers));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading dashboard stats", e);
        }
    }
    
    /**
     * Counts records in a table.
     * 
     * @param tableName The table name
     * @return The count of records
     * @throws SQLException If a database error occurs
     */
    private int countRecords(String tableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (var rs = DatabaseUtil.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Sets up the users table.
     */
    private void setupUsersTable() {
        // Set up user table columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Add action buttons to the user action column
        userActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });
                
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /**
     * Loads users into the users table.
     */
    private void loadUsers() {
        try {
            // Clear existing items
            users.clear();
            
            // Load users from database
            String query = "SELECT * FROM users ORDER BY full_name";
            try (var rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    
                    users.add(user);
                }
            }
            
            // Set items to table
            usersTable.setItems(users);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading users", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "Failed to load users: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the units table.
     */
    private void setupUnitsTable() {
        // Set up unit table columns
        unitCodeColumn.setCellValueFactory(new PropertyValueFactory<>("unitCode"));
        unitNameColumn.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        unitDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        
        // Add action buttons to the unit action column
        unitActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button assignButton = new Button("Assign");
            private final Button topicsButton = new Button("Topics");
            
            {
                editButton.setOnAction(event -> {
                    Unit unit = getTableView().getItems().get(getIndex());
                    editUnit(unit);
                });
                
                deleteButton.setOnAction(event -> {
                    Unit unit = getTableView().getItems().get(getIndex());
                    deleteUnit(unit);
                });
                
                assignButton.setOnAction(event -> {
                    Unit unit = getTableView().getItems().get(getIndex());
                    assignLecturerToUnit(unit);
                });
                
                topicsButton.setOnAction(event -> {
                    Unit unit = getTableView().getItems().get(getIndex());
                    manageTopics(unit);
                });
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, assignButton, topicsButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /**
     * Loads units into the units table.
     */
    private void loadUnits() {
        try {
            // Clear existing items
            units.clear();
            
            // Load units from database
            String query = "SELECT u.*, l.full_name AS lecturer_name FROM units u " +
                    "LEFT JOIN lecturers lec ON u.lecturer_id = lec.staff_id " +
                    "LEFT JOIN users l ON lec.user_id = l.user_id " +
                    "ORDER BY u.unit_code";
            
            try (var rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    Unit unit = new Unit();
                    unit.setUnitCode(rs.getString("unit_code"));
                    unit.setUnitName(rs.getString("unit_name"));
                    unit.setDepartment(rs.getString("department"));
                    unit.setLecturerId(rs.getString("lecturer_id"));
                    unit.setLecturerName(rs.getString("lecturer_name"));
                    
                    units.add(unit);
                }
            }
            
            // Set items to table
            unitsTable.setItems(units);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading units", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "Failed to load units: " + e.getMessage());
        }
    }
    
    /**
     * Sets up combo boxes.
     */
    private void setupComboBoxes() {
        // Set up password complexity combo box
        passwordComplexityComboBox.getItems().addAll("Low", "Medium", "High");
        passwordComplexityComboBox.setValue("Medium");
        
        // Set up report filter combo boxes
        reportRoleFilterComboBox.getItems().addAll("All", "Admin", "Lecturer", "Student");
        reportRoleFilterComboBox.setValue("All");
        
        // Set up report format combo box
        reportFormatComboBox.getItems().addAll("CSV", "PDF");
        reportFormatComboBox.setValue("CSV");
    }
    
    /**
     * Adds a new user.
     */
    @FXML
    public void addUser() {
        try {
            // Create dialog
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Add New User");
            dialog.setHeaderText("Enter User Details");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField fullNameField = new TextField();
            TextField emailField = new TextField();
            PasswordField passwordField = new PasswordField();
            ComboBox<String> roleField = new ComboBox<>();
            roleField.getItems().addAll("Admin", "Lecturer", "Student");
            roleField.setValue("Student");
            
            grid.add(new Label("Full Name:"), 0, 0);
            grid.add(fullNameField, 1, 0);
            grid.add(new Label("Email:"), 0, 1);
            grid.add(emailField, 1, 1);
            grid.add(new Label("Password:"), 0, 2);
            grid.add(passwordField, 1, 2);
            grid.add(new Label("Role:"), 0, 3);
            grid.add(roleField, 1, 3);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the full name field by default
            fullNameField.requestFocus();
            
            // Convert the result to a user when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Validate input
                    if (fullNameField.getText().isEmpty() || emailField.getText().isEmpty() || 
                            passwordField.getText().isEmpty() || roleField.getValue() == null) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Missing Required Fields", 
                                "Please fill in all required fields.");
                        return null;
                    }
                    
                    User user = new User();
                    user.setFullName(fullNameField.getText());
                    user.setEmail(emailField.getText());
                    user.setPassword(passwordField.getText());
                    user.setRole(roleField.getValue());
                    
                    return user;
                }
                return null;
            });
            
            Optional<User> result = dialog.showAndWait();
            
            result.ifPresent(user -> {
                try {
                    // Generate user ID
                    String userId = generateUserId(user.getRole());
                    user.setUserId(userId);
                    
                    // Save user to database
                    String query = "INSERT INTO users (user_id, full_name, email, password, role) VALUES (?, ?, ?, ?, ?)";
                    
                    try (var stmt = DatabaseUtil.prepareStatement(query)) {
                        stmt.setString(1, user.getUserId());
                        stmt.setString(2, user.getFullName());
                        stmt.setString(3, user.getEmail());
                        stmt.setString(4, user.getPassword());
                        stmt.setString(5, user.getRole());
                        
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            // Add to table
                            users.add(user);
                            
                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User Added", 
                                    "User has been added successfully.");
                            
                            // Refresh users count
                            loadDashboardStats();
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error adding user", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to add user: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in add user dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to create add user dialog: " + e.getMessage());
        }
    }
    
    /**
     * Generates a user ID based on role.
     * 
     * @param role The user role
     * @return The generated user ID
     * @throws SQLException If a database error occurs
     */
    private String generateUserId(String role) throws SQLException {
        String prefix;
        switch (role) {
            case "Admin":
                prefix = "ADM";
                break;
            case "Lecturer":
                prefix = "LEC";
                break;
            case "Student":
                prefix = "STU";
                break;
            default:
                prefix = "USR";
        }
        
        // Get the current highest ID for this role
        String query = "SELECT MAX(user_id) FROM users WHERE user_id LIKE ?";
        try (var stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String maxId = rs.getString(1);
                    if (maxId != null) {
                        // Extract the number part and increment
                        int num = Integer.parseInt(maxId.substring(3)) + 1;
                        return prefix + String.format("%04d", num);
                    }
                }
            }
        }
        
        // If no existing IDs, start with 0001
        return prefix + "0001";
    }
    
    /**
     * Edits an existing user.
     * 
     * @param user The user to edit
     */
    private void editUser(User user) {
        try {
            // Create dialog
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Edit User");
            dialog.setHeaderText("Edit User Details");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField userIdField = new TextField(user.getUserId());
            userIdField.setDisable(true); // User ID cannot be changed
            TextField fullNameField = new TextField(user.getFullName());
            TextField emailField = new TextField(user.getEmail());
            PasswordField passwordField = new PasswordField();
            ComboBox<String> roleField = new ComboBox<>();
            roleField.getItems().addAll("Admin", "Lecturer", "Student");
            roleField.setValue(user.getRole());
            
            grid.add(new Label("User ID:"), 0, 0);
            grid.add(userIdField, 1, 0);
            grid.add(new Label("Full Name:"), 0, 1);
            grid.add(fullNameField, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("New Password:"), 0, 3);
            grid.add(passwordField, 1, 3);
            grid.add(new Label("Role:"), 0, 4);
            grid.add(roleField, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convert the result to a user when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Validate input
                    if (fullNameField.getText().isEmpty() || emailField.getText().isEmpty() || 
                            roleField.getValue() == null) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Missing Required Fields", 
                                "Please fill in all required fields.");
                        return null;
                    }
                    
                    user.setFullName(fullNameField.getText());
                    user.setEmail(emailField.getText());
                    if (!passwordField.getText().isEmpty()) {
                        user.setPassword(passwordField.getText());
                    }
                    user.setRole(roleField.getValue());
                    
                    return user;
                }
                return null;
            });
            
            Optional<User> result = dialog.showAndWait();
            
            result.ifPresent(updatedUser -> {
                try {
                    // Update user in database
                    String query;
                    if (passwordField.getText().isEmpty()) {
                        query = "UPDATE users SET full_name = ?, email = ?, role = ? WHERE user_id = ?";
                    } else {
                        query = "UPDATE users SET full_name = ?, email = ?, password = ?, role = ? WHERE user_id = ?";
                    }
                    
                    try (var stmt = DatabaseUtil.prepareStatement(query)) {
                        stmt.setString(1, updatedUser.getFullName());
                        stmt.setString(2, updatedUser.getEmail());
                        
                        if (passwordField.getText().isEmpty()) {
                            stmt.setString(3, updatedUser.getRole());
                            stmt.setString(4, updatedUser.getUserId());
                        } else {
                            stmt.setString(3, updatedUser.getPassword());
                            stmt.setString(4, updatedUser.getRole());
                            stmt.setString(5, updatedUser.getUserId());
                        }
                        
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            // Refresh table
                            loadUsers();
                            
                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User Updated", 
                                    "User has been updated successfully.");
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error updating user", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to update user: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in edit user dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to create edit user dialog: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a user.
     * 
     * @param user The user to delete
     */
    private void deleteUser(User user) {
        try {
            // Confirm deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete User");
            alert.setContentText("Are you sure you want to delete user " + user.getFullName() + "?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete user from database
                String query = "DELETE FROM users WHERE user_id = ?";
                
                try (var stmt = DatabaseUtil.prepareStatement(query)) {
                    stmt.setString(1, user.getUserId());
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // Remove from table
                        users.remove(user);
                        
                        // Show success message
                        showAlert(Alert.AlertType.INFORMATION, "Success", "User Deleted", 
                                "User has been deleted successfully.");
                        
                        // Refresh users count
                        loadDashboardStats();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                    "Failed to delete user: " + e.getMessage());
        }
    }
    
    /**
     * Edits an existing unit.
     * 
     * @param unit The unit to edit
     */
    private void editUnit(Unit unit) {
        try {
            // Create a dialog
            Dialog<Unit> dialog = new Dialog<>();
            dialog.setTitle("Edit Unit");
            dialog.setHeaderText("Edit Unit Details");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Create text fields
            TextField unitCodeField = new TextField(unit.getUnitCode());
            unitCodeField.setDisable(true); // Unit code cannot be changed
            TextField unitNameField = new TextField(unit.getUnitName());
            TextField departmentField = new TextField(unit.getDepartment());
            
            // Add fields to grid
            grid.add(new Label("Unit Code:"), 0, 0);
            grid.add(unitCodeField, 1, 0);
            grid.add(new Label("Unit Name:"), 0, 1);
            grid.add(unitNameField, 1, 1);
            grid.add(new Label("Department:"), 0, 2);
            grid.add(departmentField, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            
            // Add buttons
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(false);
            
            // Convert result to unit object
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    unit.setUnitName(unitNameField.getText());
                    unit.setDepartment(departmentField.getText());
                    return unit;
                }
                return null;
            });
            
            // Show dialog and handle result
            Optional<Unit> result = dialog.showAndWait();
            result.ifPresent(updatedUnit -> {
                try {
                    // Update unit in database
                    String sql = "UPDATE units SET unit_name = ?, department = ? WHERE unit_code = ?";
                    try (PreparedStatement stmt = DatabaseUtil.getConnection().prepareStatement(sql)) {
                        stmt.setString(1, updatedUnit.getUnitName());
                        stmt.setString(2, updatedUnit.getDepartment());
                        stmt.setString(3, updatedUnit.getUnitCode());
                        stmt.executeUpdate();
                    }
                    
                    // Refresh units table
                    loadUnits();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Unit Updated", 
                            "Unit " + updatedUnit.getUnitCode() + " has been updated successfully.");
                    
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error updating unit", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to update unit: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing edit unit dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to show edit unit dialog: " + e.getMessage());
        }
    }
    
    /**
     * Opens a dialog to add a new unit.
     *
     * @param event The action event
     */
    @FXML
    private void addUnit(ActionEvent event) {
        try {
            // Create dialog
            Dialog<Unit> dialog = new Dialog<>();
            dialog.setTitle("Add Unit");
            dialog.setHeaderText("Add a new unit");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Create form fields
            TextField unitCodeField = new TextField();
            unitCodeField.setPromptText("Unit Code");
            
            TextField unitTitleField = new TextField();
            unitTitleField.setPromptText("Unit Title");
            
            TextField departmentField = new TextField();
            departmentField.setPromptText("Department");
            
            TextField creditsField = new TextField();
            creditsField.setPromptText("Credits");
            
            // Add fields to grid
            grid.add(new Label("Unit Code:"), 0, 0);
            grid.add(unitCodeField, 1, 0);
            grid.add(new Label("Unit Title:"), 0, 1);
            grid.add(unitTitleField, 1, 1);
            grid.add(new Label("Department:"), 0, 2);
            grid.add(departmentField, 1, 2);
            grid.add(new Label("Credits:"), 0, 3);
            grid.add(creditsField, 1, 3);
            
            // Set content
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the unit code field by default
            Platform.runLater(unitCodeField::requestFocus);
            
            // Convert the result to a Unit when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Validate input
                    if (unitCodeField.getText().isEmpty() || unitTitleField.getText().isEmpty() || 
                            departmentField.getText().isEmpty() || creditsField.getText().isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Validation Error", "All fields are required.");
                        return null;
                    }
                    
                    try {
                        int credits = Integer.parseInt(creditsField.getText());
                        
                        // Create new unit
                        Unit unit = new Unit();
                        unit.setUnitCode(unitCodeField.getText());
                        unit.setUnitName(unitTitleField.getText());
                        unit.setDepartment(departmentField.getText());
                        unit.setCreditHours(credits);
                        
                        return unit;
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Credits must be a number.");
                        return null;
                    }
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<Unit> result = dialog.showAndWait();
            
            result.ifPresent(unit -> {
                try {
                    // Save the unit to the database
                    String sql = "INSERT INTO units (unit_code, unit_name, department, credit_hours) VALUES (?, ?, ?, ?)";
                    try (Connection conn = DatabaseUtil.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        
                        pstmt.setString(1, unit.getUnitCode());
                        pstmt.setString(2, unit.getUnitName());
                        pstmt.setString(3, unit.getDepartment());
                        pstmt.setInt(4, unit.getCreditHours());
                        
                        int rowsAffected = pstmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Unit Added", 
                                    "Unit " + unit.getUnitCode() + " has been added successfully.");
                            
                            // Refresh units table
                            loadUnits();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                                    "Failed to add unit. No rows affected.");
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error adding unit", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to add unit: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing add unit dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to show add unit dialog: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a unit.
     * 
     * @param unit The unit to delete
     */
    private void deleteUnit(Unit unit) {
        try {
            // Confirm deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Unit");
            alert.setContentText("Are you sure you want to delete unit " + unit.getUnitCode() + " - " + unit.getUnitName() + "?\n\n" +
                    "This will also delete all associated topics, assessments, and student enrollments.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete all topics for this unit first
                List<Topic> unitTopics = TopicService.getTopicsByUnit(unit.getUnitCode());
                for (Topic topic : unitTopics) {
                    TopicService.deleteTopic(topic.getTopicId());
                }
                
                // Delete unit from database
                boolean success = UnitService.deleteUnit(unit);
                
                if (success) {
                    // Remove from table
                    units.remove(unit);
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Unit Deleted", 
                            "Unit " + unit.getUnitCode() + " has been deleted successfully.");
                    
                    // Refresh units count
                    loadDashboardStats();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to delete unit.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting unit", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                    "Failed to delete unit: " + e.getMessage());
        }
    }
    
    /**
     * Assigns a lecturer to a unit.
     * 
     * @param unit The unit to assign a lecturer to
     */
    private void assignLecturerToUnit(Unit unit) {
        try {
            // Create a dialog
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Assign Lecturer");
            dialog.setHeaderText("Assign Lecturer to " + unit.getUnitCode() + " - " + unit.getUnitName());
            
            // Set the button types
            ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Create lecturer combo box
            ComboBox<Lecturer> lecturerComboBox = new ComboBox<>();
            
            // Load lecturers
            try {
                String query = "SELECT l.staff_id, u.user_id, u.full_name, u.email FROM lecturers l " +
                        "JOIN users u ON l.user_id = u.user_id ORDER BY u.full_name";
                try (var rs = DatabaseUtil.executeQuery(query)) {
                    while (rs.next()) {
                        Lecturer lecturer = new Lecturer();
                        lecturer.setStaffId(rs.getString("staff_id"));
                        lecturer.setUserId(rs.getString("user_id"));
                        lecturer.setFullName(rs.getString("full_name"));
                        lecturer.setEmail(rs.getString("email"));
                        
                        lecturerComboBox.getItems().add(lecturer);
                    }
                }
                
                // Set current lecturer if any
                if (unit.getLecturerId() != null && !unit.getLecturerId().isEmpty()) {
                    for (Lecturer lecturer : lecturerComboBox.getItems()) {
                        if (lecturer.getStaffId().equals(unit.getLecturerId())) {
                            lecturerComboBox.setValue(lecturer);
                            break;
                        }
                    }
                }
                
                // Set converter for display
                lecturerComboBox.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Lecturer lecturer) {
                        return lecturer != null ? lecturer.getFullName() + " (" + lecturer.getStaffId() + ")" : "";
                    }
                    
                    @Override
                    public Lecturer fromString(String string) {
                        return null; // Not needed for this use case
                    }
                });
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading lecturers", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                        "Failed to load lecturers: " + e.getMessage());
                return;
            }
            
            // Add fields to grid
            grid.add(new Label("Select Lecturer:"), 0, 0);
            grid.add(lecturerComboBox, 1, 0);
            
            // Set content
            dialog.getDialogPane().setContent(grid);
            
            // Enable/Disable assign button depending on whether a lecturer is selected
            Node assignButton = dialog.getDialogPane().lookupButton(assignButtonType);
            assignButton.setDisable(true);
            
            lecturerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> 
                assignButton.setDisable(newValue == null));
            
            // Convert the result to a lecturer ID when the assign button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    Lecturer selectedLecturer = lecturerComboBox.getValue();
                    return selectedLecturer != null ? selectedLecturer.getStaffId() : null;
                }
                return null;
            });
            
            // Show dialog and process result
            Optional<String> result = dialog.showAndWait();
            
            result.ifPresent(lecturerId -> {
                try {
                    // Assign lecturer to unit
                    boolean success = UnitService.assignLecturerToUnit(unit.getUnitCode(), lecturerId);
                    
                    if (success) {
                        // Update unit in table
                        unit.setLecturerId(lecturerId);
                        unit.setLecturerName(lecturerComboBox.getValue().getFullName());
                        unitsTable.refresh();
                        
                        // Show success message
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Lecturer Assigned", 
                                "Lecturer has been assigned to unit " + unit.getUnitCode() + " successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                                "Failed to assign lecturer to unit.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error assigning lecturer", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to assign lecturer: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing assign lecturer dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to show assign lecturer dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles logout.
     */
    @FXML
    private void handleLogout() {
        try {
            // Confirm logout
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("Confirm Logout");
            alert.setContentText("Are you sure you want to logout?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Navigate to login screen
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/book/javafx/kenyattacatsystem/views/login-view.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root, 800, 600);
                
                // Add CSS stylesheet
                String cssPath = "/book/javafx/kenyattacatsystem/styles/main.css";
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                
                // Set scene to stage
                Stage stage = (Stage) dateTimeLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Login - Kenyatta CAT System");
                stage.show();
                
                LOGGER.log(Level.INFO, "Successfully logged out and returned to login screen");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logout", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Logout Failed", 
                    "Failed to logout: " + e.getMessage());
        }
    }
    
    /**
     * Shows the dashboard content.
     */
    @FXML
    public void showDashboard() {
        resetNavigationIcons();
        homeIcon.getStyleClass().add("sidebar-icon-active");
        
        dashboardContent.setVisible(true);
        usersContent.setVisible(false);
        unitsContent.setVisible(false);
        settingsContent.setVisible(false);
        reportsContent.setVisible(false);
        
        // Refresh dashboard stats
        loadDashboardStats();
    }
    
    /**
     * Shows the users content.
     */
    @FXML
    public void showUsers() {
        resetNavigationIcons();
        usersIcon.getStyleClass().add("sidebar-icon-active");
        
        dashboardContent.setVisible(false);
        usersContent.setVisible(true);
        unitsContent.setVisible(false);
        settingsContent.setVisible(false);
        reportsContent.setVisible(false);
        
        // Refresh users data
        loadUsers();
    }
    
    /**
     * Shows the units content.
     */
    @FXML
    public void showUnits() {
        resetNavigationIcons();
        unitsIcon.getStyleClass().add("sidebar-icon-active");
        
        dashboardContent.setVisible(false);
        usersContent.setVisible(false);
        unitsContent.setVisible(true);
        settingsContent.setVisible(false);
        reportsContent.setVisible(false);
        
        // Refresh units data
        loadUnits();
    }
    
    /**
     * Shows the settings content.
     */
    @FXML
    public void showSettings() {
        resetNavigationIcons();
        settingsIcon.getStyleClass().add("sidebar-icon-active");
        
        dashboardContent.setVisible(false);
        usersContent.setVisible(false);
        unitsContent.setVisible(false);
        settingsContent.setVisible(true);
        reportsContent.setVisible(false);
        
        // Load current settings
        loadEnrollmentSettings();
    }
    
    /**
     * Resets all navigation icons to their default style.
     */
    private void resetNavigationIcons() {
        homeIcon.getStyleClass().remove("sidebar-icon-active");
        homeIcon.getStyleClass().add("sidebar-icon");
        usersIcon.getStyleClass().remove("sidebar-icon-active");
        usersIcon.getStyleClass().add("sidebar-icon");
        unitsIcon.getStyleClass().remove("sidebar-icon-active");
        unitsIcon.getStyleClass().add("sidebar-icon");
        settingsIcon.getStyleClass().remove("sidebar-icon-active");
        settingsIcon.getStyleClass().add("sidebar-icon");
        reportsIcon.getStyleClass().remove("sidebar-icon-active");
        reportsIcon.getStyleClass().add("sidebar-icon");
    }
    
    /**
     * Loads enrollment settings.
     */
    private void loadEnrollmentSettings() {
        try {
            // Load fee clearance requirement setting
            boolean feeClearanceRequired = SystemSettingsService.isFeeClearanceRequired();
            
            // Load max units per semester setting
            int maxUnitsPerSemester = SystemSettingsService.getMaxUnitsPerSemester();
            
            // Update UI with these settings (assuming we have these controls in the settings panel)
            if (requireFeeClearanceCheckbox != null) {
                requireFeeClearanceCheckbox.setSelected(feeClearanceRequired);
            }
            
            if (maxUnitsPerSemesterField != null) {
                maxUnitsPerSemesterField.setText(String.valueOf(maxUnitsPerSemester));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading enrollment settings", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Settings Error", "Failed to load enrollment settings: " + e.getMessage());
        }
    }
    
    /**
     * Saves enrollment settings.
     */
    @FXML
    public void saveEnrollmentSettings() {
        try {
            // Get values from UI
            boolean feeClearanceRequired = requireFeeClearanceCheckbox.isSelected();
            int maxUnitsPerSemester;
            
            try {
                maxUnitsPerSemester = Integer.parseInt(maxUnitsPerSemesterField.getText());
                if (maxUnitsPerSemester < 1) {
                    throw new NumberFormatException("Value must be positive");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Input", 
                         "Maximum units per semester must be a positive number.");
                return;
            }
            
            // Save to system settings
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE system_settings SET setting_value = ? WHERE setting_name = ?")) {
                
                // Save fee clearance setting
                stmt.setString(1, String.valueOf(feeClearanceRequired));
                stmt.setString(2, "require_fee_clearance");
                stmt.executeUpdate();
                
                // Save max units setting
                stmt.setString(1, String.valueOf(maxUnitsPerSemester));
                stmt.setString(2, "max_units_per_semester");
                stmt.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Settings Saved", 
                         "Enrollment settings have been saved successfully.");
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error saving enrollment settings", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Settings Error", 
                         "Failed to save enrollment settings: " + e.getMessage());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving enrollment settings", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Settings Error", 
                     "Failed to save enrollment settings: " + e.getMessage());
        }
    }
    
    /**
     * Loads reports into the reports table.
     */
    private void loadReports() {
        try {
            reportExecutions.clear();
            
            // Updated query to only show admin-generated reports and system reports
            String query = "SELECT re.execution_id, re.execution_date, u.full_name AS executed_by, " +
                           "r.report_name AS report_name, re.result_file_path, r.is_system, u.role AS user_role " +
                           "FROM report_executions re " +
                           "JOIN reports r ON re.report_id = r.report_id " +
                           "JOIN users u ON re.executed_by = u.user_id " +
                           "WHERE (u.role = 'Admin' OR r.is_system = 1) " +
                           "ORDER BY re.execution_date DESC";
            
            try (var rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    ReportExecution reportExecution = new ReportExecution();
                    // Convert the String date to LocalDateTime
                    String dateString = rs.getString("execution_date");
                    LocalDateTime executionDate;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        executionDate = LocalDateTime.parse(dateString, formatter);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error parsing date from database", e);
                        // Use current time as fallback
                        executionDate = LocalDateTime.now();
                    }
                    
                    reportExecution.setExecutionDate(executionDate);
                    reportExecution.setExecutedBy(rs.getString("executed_by"));
                    reportExecution.setReportName(rs.getString("report_name"));
                    reportExecution.setResultFilePath(rs.getString("result_file_path"));
                    reportExecution.setExecutionId(rs.getString("execution_id"));
                    
                    reportExecutions.add(reportExecution);
                }
            }
            
            // Set items to table
            reportExecutionsTable.setItems(reportExecutions);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading reports", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "Failed to load reports: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the reports table.
     */
    private void setupReportsTable() {
        // Set up report table columns
        executionDateColumn.setCellValueFactory(new PropertyValueFactory<>("executionDate"));
        executedByColumn.setCellValueFactory(new PropertyValueFactory<>("executedBy"));
        reportNameColumn.setCellValueFactory(new PropertyValueFactory<>("reportName"));
        
        // Add action buttons to the download column
        downloadColumn.setCellFactory(col -> new TableCell<ReportExecution, String>() {
            private final Button downloadButton = new Button("Download");
            
            {
                // Set the button style to make it more visible
                downloadButton.getStyleClass().add("action-button");
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Create a new action handler each time the cell is updated
                    // This ensures the handler is refreshed between clicks
                    downloadButton.setOnAction(event -> {
                        ReportExecution reportExecution = getTableView().getItems().get(getIndex());
                        downloadReport(reportExecution);
                    });
                    
                    setGraphic(downloadButton);
                    setText(null);
                }
            }
        });
    }
    
    /**
     * Initializes reports.
     */
    private void initializeReports() {
        try {
            // Load reports from the reports table
            String query = "SELECT * FROM reports";
            try (var rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    Report report = new Report();
                    report.setReportId(rs.getString("report_id"));
                    report.setReportName(rs.getString("report_name"));
                    report.setReportType(rs.getString("report_type"));
                    report.setDescription(rs.getString("description"));
                    
                    reports.add(report);
                }
            }
            
            // Set reports to combo box
            reportTypeComboBox.setItems(reports);
            
            // Set a cell factory to display report names
            reportTypeComboBox.setCellFactory(param -> new ListCell<Report>() {
                @Override
                protected void updateItem(Report item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getReportName());
                    }
                }
            });
            
            // Set a converter to display report names
            reportTypeComboBox.setConverter(new StringConverter<Report>() {
                @Override
                public String toString(Report report) {
                    return report != null ? report.getReportName() : "";
                }
                
                @Override
                public Report fromString(String string) {
                    return null; // Not needed for this use case
                }
            });
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing reports", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "Failed to initialize reports: " + e.getMessage());
        }
    }
    
    /**
     * Generates a report based on selected filters.
     *
     * @param event The action event
     */
    @FXML
    private void generateReport(ActionEvent event) {
        try {
            // Get selected report
            Report selectedReport = reportTypeComboBox.getSelectionModel().getSelectedItem();
            if (selectedReport == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "No Report Selected", "Please select a report type.");
                return;
            }
            
            // Prepare parameters
            Map<String, Object> parameters = new HashMap<>();
            
            // Add date range parameters
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null) {
                parameters.put("start_date", startDate.toString());
            } else {
                parameters.put("start_date", LocalDate.now().minusYears(1).toString());
            }
            
            if (endDate != null) {
                parameters.put("end_date", endDate.toString());
            } else {
                parameters.put("end_date", LocalDate.now().toString());
            }
            
            // Add unit filter
            String selectedUnit = reportUnitFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedUnit != null && !selectedUnit.equals("All Units")) {
                parameters.put("unit_code", selectedUnit);
            } else {
                parameters.put("unit_code", null);
            }
            
            // Add department filter
            String selectedDepartment = reportDepartmentFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedDepartment != null && !selectedDepartment.equals("All Departments")) {
                parameters.put("department", selectedDepartment);
            } else {
                parameters.put("department", null);
            }
            
            // Add role filter
            String selectedRole = reportRoleFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedRole != null && !selectedRole.equals("All Roles")) {
                parameters.put("role", selectedRole);
            } else {
                parameters.put("role", null);
            }
            
            // Add report format
            String selectedFormat = reportFormatComboBox.getSelectionModel().getSelectedItem();
            if (selectedFormat != null) {
                parameters.put("format", selectedFormat);
            } else {
                parameters.put("format", null);
            }
            
            // Show loading indicator
            showAlert(Alert.AlertType.INFORMATION, "Processing", "Generating Report", 
                    "Please wait while the report is being generated...");
            
            // Execute report
            ReportExecution execution = ReportService.executeReport(
                    selectedReport.getReportId(), 
                    parameters, 
                    currentAdmin.getUserId());
            
            if (execution != null && execution.getResultFilePath() != null) {
                // Show success message with download link
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Report Generated");
                alert.setHeaderText("Report Generated Successfully");
                alert.setContentText("The report has been generated and saved to: " + execution.getResultFilePath());
                
                // Add button to open the file
                ButtonType openButton = new ButtonType("Open File");
                ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(openButton, closeButton);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == openButton) {
                    // Open the file
                    openReportFile(execution.getResultFilePath());
                }
                
                // Refresh report executions list
                loadReports();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Report Generation Failed", 
                        "Failed to generate the report. Please check the logs for more details.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Report Generation Failed", 
                    "An error occurred while generating the report: " + e.getMessage());
        }
    }
    
    /**
     * Downloads a report.
     * 
     * @param reportExecution The report execution
     */
    private void downloadReport(ReportExecution reportExecution) {
        try {
            // Get report file path
            String filePath = ReportService.getReportFilePath(reportExecution.getReportName());
            
            // Check if file path is valid
            if (filePath == null || filePath.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Report file not found for: {0}", reportExecution.getReportName());
                showAlert(Alert.AlertType.WARNING, "Report Not Found", 
                        "Report File Not Found", 
                        "The report file for '" + reportExecution.getReportName() + "' could not be found. It may have been deleted or moved.");
                return;
            }
            
            // Download report
            boolean success = ReportService.downloadReport(filePath);
            if (!success) {
                LOGGER.log(Level.WARNING, "Failed to open report file: {0}", filePath);
                showAlert(Alert.AlertType.WARNING, "Open Failed", 
                        "Failed to Open Report", 
                        "The report file exists but could not be opened. Please check if you have appropriate software to view the file.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error downloading report", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Report Error", "Failed to download report: " + e.getMessage());
        }
    }
    
    /**
     * Shows the reports screen.
     * 
     * @param event The mouse event
     */
    @FXML
    public void showReports(MouseEvent event) {
        resetNavigationIcons();
        reportsIcon.getStyleClass().add("sidebar-icon-active");
        
        dashboardContent.setVisible(false);
        usersContent.setVisible(false);
        unitsContent.setVisible(false);
        settingsContent.setVisible(false);
        reportsContent.setVisible(true);
        
        // Load reports data
        loadReports();
        
        // Load filter data
        loadReportFilters();
    }
    
    /**
     * Loads filter data for reports.
     */
    private void loadReportFilters() {
        try {
            // Load units for filter
            List<String> unitCodes = new ArrayList<>();
            unitCodes.add("All Units");
            String unitQuery = "SELECT unit_code FROM units ORDER BY unit_code";
            try (var rs = DatabaseUtil.executeQuery(unitQuery)) {
                while (rs.next()) {
                    unitCodes.add(rs.getString("unit_code"));
                }
            }
            reportUnitFilterComboBox.getItems().setAll(unitCodes);
            reportUnitFilterComboBox.getSelectionModel().selectFirst();
            
            // Load departments for filter
            List<String> departments = new ArrayList<>();
            departments.add("All Departments");
            String deptQuery = "SELECT DISTINCT department FROM units ORDER BY department";
            try (var rs = DatabaseUtil.executeQuery(deptQuery)) {
                while (rs.next()) {
                    departments.add(rs.getString("department"));
                }
            }
            reportDepartmentFilterComboBox.getItems().setAll(departments);
            reportDepartmentFilterComboBox.getSelectionModel().selectFirst();
            
            // Load roles for filter
            List<String> roles = new ArrayList<>();
            roles.add("All Roles");
            roles.add("STUDENT");
            roles.add("LECTURER");
            roles.add("ADMIN");
            reportRoleFilterComboBox.getItems().setAll(roles);
            reportRoleFilterComboBox.getSelectionModel().selectFirst();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading report filters", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "Failed to load report filters: " + e.getMessage());
        }
    }
    
    /**
     * Opens a report file.
     *
     * @param filePath The file path
     */
    private void openReportFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // Open the file with the default application
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "", filePath);
                pb.start();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "File Not Found", "The report file could not be found: " + filePath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening report file", e);
            showAlert(Alert.AlertType.ERROR, "Error", "File Error", "Failed to open report file: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param type Alert type
     * @param title Alert title
     * @param header Alert header
     * @param content Alert content
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Manages topics for a unit.
     * 
     * @param unit The unit to manage topics for
     */
    private void manageTopics(Unit unit) {
        try {
            // Create a dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Manage Topics");
            dialog.setHeaderText("Manage Topics for " + unit.getUnitCode() + " - " + unit.getUnitName());
            
            // Create the content pane
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            
            // Create a label
            Label infoLabel = new Label("Topics are used to categorize questions for assessments.");
            infoLabel.setWrapText(true);
            
            // Create a table view for topics
            TableView<Topic> topicsTable = new TableView<>();
            topicsTable.setPrefHeight(300);
            
            // Create table columns
            TableColumn<Topic, String> topicIdColumn = new TableColumn<>("ID");
            topicIdColumn.setCellValueFactory(new PropertyValueFactory<>("topicId"));
            topicIdColumn.setPrefWidth(80);
            
            TableColumn<Topic, String> topicNameColumn = new TableColumn<>("Topic Name");
            topicNameColumn.setCellValueFactory(new PropertyValueFactory<>("topicName"));
            topicNameColumn.setPrefWidth(200);
            
            TableColumn<Topic, String> descriptionColumn = new TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            descriptionColumn.setPrefWidth(250);
            
            TableColumn<Topic, Button> actionColumn = new TableColumn<>("Actions");
            actionColumn.setPrefWidth(120);
            
            // Add action buttons to the action column
            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                
                {
                    editButton.setOnAction(event -> {
                        Topic topic = getTableView().getItems().get(getIndex());
                        editTopic(topic, unit, topicsTable);
                    });
                    
                    deleteButton.setOnAction(event -> {
                        Topic topic = getTableView().getItems().get(getIndex());
                        deleteTopic(topic, topicsTable);
                    });
                }
                
                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttons = new HBox(5, editButton, deleteButton);
                        setGraphic(buttons);
                    }
                }
            });
            
            // Add columns to table
            topicsTable.getColumns().addAll(topicIdColumn, topicNameColumn, descriptionColumn, actionColumn);
            
            // Load topics for the unit
            ObservableList<Topic> topics = FXCollections.observableArrayList();
            loadTopicsForUnit(unit.getUnitCode(), topics);
            topicsTable.setItems(topics);
            
            // Create buttons
            Button addButton = new Button("Add Topic");
            addButton.setOnAction(e -> addTopic(unit, topics));
            
            // Add components to content
            content.getChildren().addAll(infoLabel, topicsTable, addButton);
            
            // Set the content
            dialog.getDialogPane().setContent(content);
            
            // Add close button
            ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(closeButtonType);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing topics dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to show topics dialog: " + e.getMessage());
        }
    }
    
    /**
     * Loads topics for a unit.
     * 
     * @param unitCode The unit code
     * @param topics The observable list to populate
     */
    private void loadTopicsForUnit(String unitCode, ObservableList<Topic> topics) {
        try {
            // Clear existing topics
            topics.clear();
            
            // Get topics from service
            List<Topic> unitTopics = TopicService.getTopicsByUnit(unitCode);
            topics.addAll(unitTopics);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading topics", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                    "Failed to load topics: " + e.getMessage());
        }
    }
    
    /**
     * Adds a new topic for a unit.
     * 
     * @param unit The unit
     * @param topics The observable list of topics
     */
    private void addTopic(Unit unit, ObservableList<Topic> topics) {
        try {
            // Show topic dialog
            Topic topic = TopicService.showTopicDialog(null, unit.getUnitCode(), currentAdmin.getUserId());
            
            if (topic != null) {
                // Add topic to database
                boolean success = TopicService.createTopic(topic);
                
                if (success) {
                    // Add to list
                    topics.add(topic);
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Topic Added", 
                            "Topic has been added successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to add topic.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding topic", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to add topic: " + e.getMessage());
        }
    }
    
    /**
     * Edits an existing topic.
     * 
     * @param topic The topic to edit
     * @param unit The unit
     * @param topicsTable The topics table
     */
    private void editTopic(Topic topic, Unit unit, TableView<Topic> topicsTable) {
        try {
            // Show topic dialog
            Topic updatedTopic = TopicService.showTopicDialog(topic, unit.getUnitCode(), currentAdmin.getUserId());
            
            if (updatedTopic != null) {
                // Update topic in database
                boolean success = TopicService.updateTopic(updatedTopic);
                
                if (success) {
                    // Refresh table
                    loadTopicsForUnit(unit.getUnitCode(), topicsTable.getItems());
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Topic Updated", 
                            "Topic has been updated successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to update topic.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing topic", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to edit topic: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a topic.
     * 
     * @param topic The topic to delete
     * @param topicsTable The topics table
     */
    private void deleteTopic(Topic topic, TableView<Topic> topicsTable) {
        try {
            // Confirm deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Topic");
            alert.setContentText("Are you sure you want to delete topic '" + topic.getTopicName() + "'?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete topic from database
                boolean success = TopicService.deleteTopic(topic.getTopicId());
                
                if (success) {
                    // Remove from table
                    topicsTable.getItems().remove(topic);
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Topic Deleted", 
                            "Topic has been deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                            "Failed to delete topic.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting topic", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", 
                    "Failed to delete topic: " + e.getMessage());
        }
    }
}