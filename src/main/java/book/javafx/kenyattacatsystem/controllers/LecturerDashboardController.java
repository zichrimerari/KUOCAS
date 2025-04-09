package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.*;
import book.javafx.kenyattacatsystem.services.*;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import book.javafx.kenyattacatsystem.utils.ProctoringUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Lecturer Dashboard view.
 * Handles displaying assigned units, creating assessments, and managing grades.
 */
public class LecturerDashboardController {
    private static final Logger LOGGER = Logger.getLogger(LecturerDashboardController.class.getName());
    
    // Current logged in lecturer
    private Lecturer currentLecturer;
    
    // Navigation Icons
    @FXML private FontIcon dashboardIcon;
    @FXML private FontIcon assessmentsIcon;
    @FXML private FontIcon analyticsIcon;
    @FXML private FontIcon reportsIcon;
    @FXML private FontIcon settingsIcon;
    
    // Content containers
    @FXML private VBox dashboardContent;
    @FXML private VBox assessmentsContent;
    @FXML private VBox analyticsContent;
    @FXML private VBox reportsContent;
    @FXML private VBox settingsContent;
    @FXML private VBox reportDescriptionsContainer;
    
    // Dashboard components
    @FXML private Text welcomeText;
    @FXML private Label dateTimeLabel;
    @FXML private FlowPane unitCardsContainer;
    
    // Assessments components
    @FXML private TableView<Assessment> assessmentsTable;
    @FXML private ComboBox<Unit> unitFilterComboBox;
    @FXML private ComboBox<String> reportTypeComboBox;
    
    // Reports components
    @FXML private TableView<ProctoringViolation> proctoringViolationsTable;
    @FXML private ComboBox<Unit> proctoringUnitFilterComboBox;
    @FXML private ComboBox<String> proctoringTimeFilterComboBox;
    @FXML private ComboBox<String> proctoringStudentFilterComboBox;
    @FXML private ComboBox<String> proctoringAssessmentFilterComboBox;
    @FXML private Label proctoringTotalViolationsLabel;
    
    // Settings components
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Services
    private UnitService unitService;
    private AssessmentService assessmentService;
    private QuestionService questionService;
    private TopicService topicService;
    private PerformanceAnalyticsService performanceAnalyticsService;
    
    // Data lists
    private ObservableList<Unit> assignedUnits = FXCollections.observableArrayList();
    private ObservableList<Assessment> assessments = FXCollections.observableArrayList();
    private ObservableList<ProctoringViolation> proctoringViolations = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     * Sets up UI components and loads initial data.
     */
    @FXML
    public void initialize() {
        // Initialize services
        unitService = new UnitService();
        assessmentService = new AssessmentService();
        questionService = new QuestionService();
        topicService = new TopicService();
        performanceAnalyticsService = new PerformanceAnalyticsService();
        
        // Set up date/time display with automatic updates
        updateDateTime();
        Thread timeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(this::updateDateTime);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Time update thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
        
        // Initialize report types
        if (reportTypeComboBox != null) {
            reportTypeComboBox.getItems().addAll(
                "Student Performance Report",
                "Assessment Results Summary",
                "Unit Progress Report",
                "Question Analysis Report",
                "Attendance Report"
            );
        }
        
        // Set up the assessments table columns
        if (assessmentsTable != null) {
            // Clear any existing columns
            assessmentsTable.getColumns().clear();
            
            // Title column
            TableColumn<Assessment, String> titleColumn = new TableColumn<>("Title");
            titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
            titleColumn.setPrefWidth(200);
            
            // Unit column
            TableColumn<Assessment, String> unitColumn = new TableColumn<>("Unit");
            unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnitCode()));
            unitColumn.setPrefWidth(100);
            
            // Type column
            TableColumn<Assessment, String> typeColumn = new TableColumn<>("Type");
            typeColumn.setCellValueFactory(data -> {
                boolean isPractice = data.getValue().isPractice();
                return new SimpleStringProperty(isPractice ? "Practice" : "Formal");
            });
            typeColumn.setPrefWidth(100);
            
            // Status column
            TableColumn<Assessment, String> statusColumn = new TableColumn<>("Status");
            statusColumn.setCellValueFactory(data -> {
                Assessment assessment = data.getValue();
                LocalDateTime now = LocalDateTime.now();
                
                if (assessment.getStartDateTime() == null) {
                    return new SimpleStringProperty("Draft");
                } else if (now.isBefore(assessment.getStartDateTime())) {
                    return new SimpleStringProperty("Scheduled");
                } else if (assessment.getEndDateTime() != null && now.isAfter(assessment.getEndDateTime())) {
                    return new SimpleStringProperty("Closed");
                } else {
                    return new SimpleStringProperty("Active");
                }
            });
            statusColumn.setPrefWidth(100);
            
            // Date column
            TableColumn<Assessment, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(data -> {
                LocalDateTime startDate = data.getValue().getStartDateTime();
                if (startDate == null) {
                    return new SimpleStringProperty("Not scheduled");
                }
                return new SimpleStringProperty(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            });
            dateColumn.setPrefWidth(150);
            
            // Actions column
            TableColumn<Assessment, Void> actionsColumn = new TableColumn<>("Actions");
            actionsColumn.setCellFactory(col -> new TableCell<Assessment, Void>() {
                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");
                private final HBox hbox = new HBox(5, viewButton, editButton);
                
                {
                    viewButton.setOnAction(event -> {
                        Assessment assessment = getTableView().getItems().get(getIndex());
                        viewAssessment(assessment);
                    });
                    
                    editButton.setOnAction(event -> {
                        Assessment assessment = getTableView().getItems().get(getIndex());
                        editAssessment(assessment);
                    });
                    
                    hbox.setAlignment(Pos.CENTER);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : hbox);
                }
            });
            actionsColumn.setPrefWidth(150);
            
            // Add all columns to the table
            assessmentsTable.getColumns().addAll(
                titleColumn, unitColumn, typeColumn, statusColumn, dateColumn, actionsColumn
            );
        }
        
        // Set up unit filter combo box
        if (unitFilterComboBox != null) {
            // Set a cell factory to display unit names properly
            unitFilterComboBox.setCellFactory(param -> new ListCell<Unit>() {
                @Override
                protected void updateItem(Unit unit, boolean empty) {
                    super.updateItem(unit, empty);
                    
                    if (empty || unit == null) {
                        setText(null);
                    } else {
                        setText(unit.getUnitCode() + " - " + unit.getUnitName());
                    }
                }
            });
            
            // Set a string converter to display the selected unit properly
            unitFilterComboBox.setConverter(new StringConverter<Unit>() {
                @Override
                public String toString(Unit unit) {
                    if (unit == null) {
                        return null;
                    }
                    return unit.getUnitCode() + " - " + unit.getUnitName();
                }
                
                @Override
                public Unit fromString(String string) {
                    return null; // Not needed for combo box
                }
            });
        }
        
        // Set default view to dashboard
        showDashboard();
    }
    
    /**
     * Sets the current lecturer user.
     * 
     * @param lecturer The lecturer user
     */
    public void setCurrentLecturer(Lecturer lecturer) {
        this.currentLecturer = lecturer;
        welcomeText.setText("Welcome, " + lecturer.getFullName());
        
        // Initialize settings fields if they exist
        if (nameField != null && emailField != null) {
            nameField.setText(lecturer.getFullName());
            emailField.setText(lecturer.getEmail());
        }
        
        // Load assigned units for this lecturer
        loadAssignedUnits();
    }
    
    /**
     * Updates the date and time label.
     */
    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy - HH:mm:ss");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }
    
    /**
     * Loads units assigned to the current lecturer.
     */
    private void loadAssignedUnits() {
        try {
            // Clear existing units
            assignedUnits.clear();
            unitCardsContainer.getChildren().clear();
            
            // Log for debugging
            LOGGER.info("Loading assigned units for lecturer: " + (currentLecturer != null ? currentLecturer.getUserId() : "null"));
            
            // Load assigned units from database
            List<Unit> units = getUnitsByLecturerId(currentLecturer.getUserId());
            
            assignedUnits.addAll(units);
            
            // Log for debugging
            LOGGER.info("Found " + units.size() + " units assigned to lecturer");
            
            // Create unit cards for dashboard
            for (Unit unit : units) {
                createUnitCard(unit);
            }
            
            // Populate unit filter combo box for assessments tab
            unitFilterComboBox.setItems(assignedUnits);
            unitFilterComboBox.setPromptText("Filter by Unit");
            unitFilterComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadAssessments(newVal)
            );
            
            // Also populate unit selector for reports tab
            if (proctoringUnitFilterComboBox != null) {
                Unit allUnits = new Unit();
                allUnits.setUnitCode("ALL");
                allUnits.setUnitName("All Units");
                
                proctoringUnitFilterComboBox.getItems().add(allUnits);
                proctoringUnitFilterComboBox.getItems().addAll(assignedUnits);
                proctoringUnitFilterComboBox.setPromptText("Select Unit");
                
                // Set cell factory to display unit code and name properly
                proctoringUnitFilterComboBox.setCellFactory(param -> new ListCell<Unit>() {
                    @Override
                    protected void updateItem(Unit unit, boolean empty) {
                        super.updateItem(unit, empty);
                        
                        if (empty || unit == null) {
                            setText(null);
                        } else {
                            if ("ALL".equals(unit.getUnitCode())) {
                                setText("All Units");
                            } else {
                                setText(unit.getUnitCode() + " - " + unit.getUnitName());
                            }
                        }
                    }
                });
                
                // Set string converter to display the selected unit properly
                proctoringUnitFilterComboBox.setConverter(new StringConverter<Unit>() {
                    @Override
                    public String toString(Unit unit) {
                        if (unit == null) {
                            return null;
                        }
                        if ("ALL".equals(unit.getUnitCode())) {
                            return "All Units";
                        }
                        return unit.getUnitCode() + " - " + unit.getUnitName();
                    }
                    
                    @Override
                    public Unit fromString(String string) {
                        return null; // Not needed for combo box
                    }
                });
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading assigned units", e);
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load assigned units", 
                    "An error occurred while loading your assigned units: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error loading assigned units", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load assigned units", 
                    "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Creates a card for a unit to display on the dashboard.
     * 
     * @param unit The unit to display
     */
    private void createUnitCard(Unit unit) {
        // Log for debugging
        LOGGER.info("Creating unit card for: " + unit.getUnitCode() + " - " + unit.getUnitName());
        
        // Create card container
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setPrefHeight(200);
        card.getStyleClass().add("unit-card");
        
        // Set random background color with pastel shade
        String[] colors = {"#FFD6D6", "#D6FFD6", "#D6D6FF", "#FFD6FF", "#FFFFD6", "#D6FFFF"};
        String color = colors[new Random().nextInt(colors.length)];
        card.setBackground(new Background(new BackgroundFill(
                Color.web(color), new CornerRadii(10), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(
                Color.web(color).darker(), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));
        
        // Add shadow effect to make cards more visible
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0.4, 0.4, 0.4, 0.4));
        card.setEffect(shadow);
        
        // Unit code and name
        Label codeLabel = new Label(unit.getUnitCode());
        codeLabel.getStyleClass().add("unit-code");
        codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label nameLabel = new Label(unit.getUnitName());
        nameLabel.getStyleClass().add("unit-name");
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Department and credits
        Label deptLabel = new Label("Department: " + unit.getDepartment());
        deptLabel.setStyle("-fx-font-size: 12px;");
        
        Label creditsLabel = new Label("Credits: " + unit.getCreditHours());
        creditsLabel.setStyle("-fx-font-size: 12px;");
        
        // Student count
        Label studentsLabel = new Label("Students: " + getStudentCount(unit));
        studentsLabel.setStyle("-fx-font-size: 12px;");
        
        // Create assessment button
        Button createButton = new Button("Create Assessment");
        createButton.getStyleClass().add("create-assessment-btn");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createButton.setOnAction(e -> openCreateAssessmentDialog(unit));
        
        // Add all components to card
        card.getChildren().addAll(codeLabel, nameLabel, deptLabel, creditsLabel, studentsLabel, createButton);
        
        // Add card to container
        unitCardsContainer.getChildren().add(card);
        
        // Log for debugging
        LOGGER.info("Added unit card to container. Container now has " + unitCardsContainer.getChildren().size() + " children.");
    }
    
    /**
     * Gets the number of students enrolled in a unit.
     * 
     * @param unit The unit
     * @return The number of students
     */
    private int getStudentCount(Unit unit) {
        try {
            // Check if the student_units table exists
            String checkTableQuery = "SHOW TABLES LIKE 'student_units'";
            try (PreparedStatement checkStmt = DatabaseUtil.prepareStatement(checkTableQuery)) {
                ResultSet checkRs = checkStmt.executeQuery();
                if (!checkRs.next()) {
                    // Table doesn't exist, create it
                    LOGGER.info("Creating student_units table");
                    String createTableQuery = "CREATE TABLE student_units (" +
                            "id VARCHAR(36) PRIMARY KEY, " +
                            "student_id VARCHAR(36) NOT NULL, " +
                            "unit_code VARCHAR(20) NOT NULL, " +
                            "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (student_id) REFERENCES users(user_id), " +
                            "FOREIGN KEY (unit_code) REFERENCES units(unit_code)" +
                            ")";
                    try (PreparedStatement createStmt = DatabaseUtil.prepareStatement(createTableQuery)) {
                        createStmt.executeUpdate();
                    }
                    return 0;
                }
            }
            
            // Count students in the unit
            String query = "SELECT COUNT(*) FROM student_units WHERE unit_code = ?";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, unit.getUnitCode());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error getting student count for unit " + unit.getUnitCode(), e);
            return 0;
        }
    }
    
    /**
     * Opens a dialog to create a new assessment.
     * 
     * @param unit The unit for which to create an assessment
     */
    private void openCreateAssessmentDialog(Unit unit) {
        LOGGER.log(Level.INFO, "Opening create assessment dialog for unit {0}: {1}", 
                  new Object[]{unit.getUnitCode(), unit.getUnitName()});
        try {
            // Create dialog
            Dialog<Assessment> dialog = new Dialog<>();
            dialog.setTitle("Create Assessment");
            dialog.setHeaderText("Create a new assessment for " + unit.getUnitCode() + ": " + unit.getUnitName());
            
            // Set the button types
            ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Form fields
            TextField titleField = new TextField();
            titleField.setPromptText("Assessment Title");
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Assessment Description");
            descriptionArea.setPrefRowCount(3);
            
            ComboBox<String> difficultyCombo = new ComboBox<>();
            difficultyCombo.getItems().addAll("Easy", "Medium", "Hard", "Mixed");
            difficultyCombo.setValue("Medium");
            
            DatePicker datePicker = new DatePicker(LocalDate.now());
            
            Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
            hourSpinner.setEditable(true);
            hourSpinner.setPrefWidth(70);
            
            Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());
            minuteSpinner.setEditable(true);
            minuteSpinner.setPrefWidth(70);
            
            HBox timeBox = new HBox(5, hourSpinner, new Label(":"), minuteSpinner);
            
            Spinner<Integer> durationSpinner = new Spinner<>(10, 180, 60);
            durationSpinner.setEditable(true);
            
            // Number of questions spinner (no longer conditional on a checkbox)
            Spinner<Integer> questionCountSpinner = new Spinner<>(5, 50, 10);
            questionCountSpinner.setEditable(true);
            
            // Create a CheckComboBox for topic selection
            CheckComboBox<Topic> topicCheckComboBox = new CheckComboBox<>();
            topicCheckComboBox.setTitle("Select Topics");
            topicCheckComboBox.setPrefWidth(300);
            
            // Set converter for displaying topic names
            topicCheckComboBox.setConverter(new StringConverter<Topic>() {
                @Override
                public String toString(Topic topic) {
                    return topic != null ? topic.getTopicName() : "";
                }

                @Override
                public Topic fromString(String string) {
                    return null; // Not needed
                }
            });
            
            // Load topics for the unit
            try {
                List<Topic> topics = TopicService.getTopicsByUnit(unit.getUnitCode());
                topicCheckComboBox.getItems().addAll(topics);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading topics for unit " + unit.getUnitCode(), e);
            }
            
            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Difficulty:"), 0, 2);
            grid.add(difficultyCombo, 1, 2);
            grid.add(new Label("Date:"), 0, 3);
            grid.add(datePicker, 1, 3);
            grid.add(new Label("Time:"), 0, 4);
            grid.add(timeBox, 1, 4);
            grid.add(new Label("Duration (minutes):"), 0, 5);
            grid.add(durationSpinner, 1, 5);
            grid.add(new Label("Number of Questions:"), 0, 6);
            grid.add(questionCountSpinner, 1, 6);
            grid.add(new Label("Select Topics:"), 0, 7);
            grid.add(topicCheckComboBox, 1, 7);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);
            
            // Convert the result to an assessment when the create button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == createButtonType) {
                    try {
                        // Get form values
                        String title = titleField.getText();
                        String description = descriptionArea.getText();
                        
                        // Validate input
                        if (title == null || title.trim().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                                "Invalid Title", "Please enter a title for the assessment.");
                            return null;
                        }
                        
                        // Convert title to uppercase with no spaces for aesthetics
                        title = title.toUpperCase().replaceAll("\\s+", "");
                        
                        // Set date and time
                        LocalDateTime startDateTime = LocalDateTime.of(
                            datePicker.getValue(),
                            LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                        );
                        
                        // Validate that start time is >= current time
                        LocalDateTime now = LocalDateTime.now();
                        if (startDateTime.isBefore(now)) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                                "Invalid Start Time", 
                                "The assessment start time must be equal to or greater than the current time.");
                            return null;
                        }
                        
                        // Check if there are enough questions available before creating the assessment
                        int availableQuestionCount = countAvailableQuestions(unit.getUnitCode(), difficultyCombo.getValue());
                        int requestedQuestionCount = questionCountSpinner.getValue();
                        
                        LOGGER.log(Level.INFO, "Checking question availability: {0} available vs {1} requested for difficulty {2}", 
                                  new Object[]{availableQuestionCount, requestedQuestionCount, difficultyCombo.getValue()});
                        
                        if (availableQuestionCount < requestedQuestionCount) {
                            String errorMsg = "Only " + availableQuestionCount + " questions are available with " + 
                                difficultyCombo.getValue() + " difficulty, but " + requestedQuestionCount + " were requested. " +
                                "Please reduce the number of questions or add more questions to the question bank.";
                            LOGGER.log(Level.WARNING, "Insufficient questions: {0}", errorMsg);
                            
                            // Show alert but don't return null to prevent dialog from closing
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Insufficient Questions", 
                                    "Cannot Create Assessment", errorMsg);
                            });
                            
                            // Return a dummy value that will be checked later
                            return new Assessment("INSUFFICIENT_QUESTIONS", "", "", "", 0, false);
                        }
                        
                        // End date/time is start + duration
                        LocalDateTime endDateTime = startDateTime.plusMinutes(durationSpinner.getValue());
                        
                        // Create assessment in database
                        LOGGER.log(Level.INFO, "Creating assessment with title: {0}, unit: {1}, lecturer: {2}", 
                                  new Object[]{title, unit.getUnitCode(), currentLecturer.getUserId()});
                        
                        Assessment assessment = AssessmentService.createAssessment(
                            title,
                            description,
                            unit.getUnitCode(),
                            currentLecturer.getUserId(),
                            startDateTime,
                            endDateTime,
                            durationSpinner.getValue(),
                            false, // Not a practice test
                            false  // No offline attempts
                        );
                        
                        if (assessment != null) {
                            LOGGER.log(Level.INFO, "Assessment created successfully with ID: {0}", assessment.getAssessmentId());
                            
                            // Auto-generate questions
                            List<Topic> selectedTopics = new ArrayList<>(topicCheckComboBox.getCheckModel().getCheckedItems());
                            LOGGER.log(Level.INFO, "Generating questions for assessment {0} with {1} selected topics", 
                                      new Object[]{assessment.getAssessmentId(), selectedTopics.size()});
                            
                            int questionsAdded = generateQuestionsForAssessment(
                                assessment.getAssessmentId(),
                                unit.getUnitCode(),
                                difficultyCombo.getValue(),
                                questionCountSpinner.getValue(),
                                selectedTopics
                            );
                            
                            // Refresh assessments list
                            loadAssessments(unit);
                            
                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                "Assessment Created", 
                                "The assessment has been created successfully with " + questionsAdded + " questions.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", 
                                "Failed to Create Assessment", 
                                "Could not create the assessment in the database.");
                        }
                            
                        return assessment;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error creating assessment", e);
                        LOGGER.log(Level.SEVERE, "Exception details: {0}", e.toString());
                        if (e.getCause() != null) {
                            LOGGER.log(Level.SEVERE, "Caused by: {0}", e.getCause().toString());
                        }
                        
                        showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to Create Assessment", 
                            "An error occurred: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            // Show the dialog and wait for the result
            Optional<Assessment> result = dialog.showAndWait();
            
            // Handle the result
            result.ifPresent(assessment -> {
                // Check if this is our special case for insufficient questions
                if ("INSUFFICIENT_QUESTIONS".equals(assessment.getAssessmentId())) {
                    LOGGER.log(Level.INFO, "Dialog kept open due to insufficient questions");
                    // Do nothing, the dialog will remain open and the error message was already shown
                } else {
                    LOGGER.log(Level.INFO, "Assessment created successfully: {0}", assessment.getTitle());
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening create assessment dialog", e);
            LOGGER.log(Level.SEVERE, "Exception details: {0}", e.toString());
            if (e.getCause() != null) {
                LOGGER.log(Level.SEVERE, "Caused by: {0}", e.getCause().toString());
            }
            
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Dialog Error", 
                "Could not open the create assessment dialog: " + e.getMessage());
        }
    }
    
    /**
     * Counts the number of available questions for a unit with the specified difficulty.
     * 
     * @param unitCode The unit code
     * @param difficulty The difficulty level
     * @return The number of available questions
     * @throws SQLException If a database error occurs
     */
    private int countAvailableQuestions(String unitCode, String difficulty) throws SQLException {
        LOGGER.log(Level.INFO, "Counting available questions for unit {0} with difficulty {1}", new Object[]{unitCode, difficulty});
        
        // Get questions from the question bank
        List<Question> questions = QuestionService.getQuestionsByUnit(unitCode, true);
        LOGGER.log(Level.INFO, "Found {0} total questions for unit {1}", new Object[]{questions.size(), unitCode});
        
        // If difficulty is "Mixed", return all questions
        if ("Mixed".equalsIgnoreCase(difficulty)) {
            LOGGER.log(Level.INFO, "Mixed difficulty selected, returning all {0} questions", questions.size());
            return questions.size();
        }
        
        // Convert UI difficulty to database format (uppercase)
        String dbDifficulty = difficulty.toUpperCase();
        LOGGER.log(Level.INFO, "Converted UI difficulty '{0}' to database format '{1}'", new Object[]{difficulty, dbDifficulty});
        
        // Otherwise, count questions with the matching difficulty
        int count = 0;
        for (Question question : questions) {
            if (dbDifficulty.equals(question.getDifficulty())) {
                count++;
            }
        }
        
        LOGGER.log(Level.INFO, "Found {0} questions matching difficulty {1} for unit {2}", new Object[]{count, dbDifficulty, unitCode});
        return count;
    }
    
    /**
     * Generates questions for an assessment.
     * 
     * @param assessmentId The assessment ID
     * @param unitCode The unit code
     * @param difficulty The difficulty level
     * @param count The number of questions to generate
     * @param selectedTopics List of selected topics to filter questions by
     * @return The number of questions actually added to the assessment
     * @throws SQLException If a database error occurs
     */
    private int generateQuestionsForAssessment(String assessmentId, String unitCode, 
                                             String difficulty, int count, List<Topic> selectedTopics) throws SQLException {
        LOGGER.log(Level.INFO, "Generating {0} questions for assessment {1}, unit {2}, difficulty {3}", 
                  new Object[]{count, assessmentId, unitCode, difficulty});
        
        // Get questions from the question bank
        List<Question> questions = QuestionService.getQuestionsByUnit(unitCode, true);
        LOGGER.log(Level.INFO, "Retrieved {0} total questions for unit {1}", new Object[]{questions.size(), unitCode});
        
        // Filter by difficulty and topic
        List<Question> filteredQuestions = new ArrayList<>();
        
        // If difficulty is "Mixed", take questions of all difficulties
        boolean isMixed = "Mixed".equalsIgnoreCase(difficulty);
        LOGGER.log(Level.INFO, "Mixed difficulty: {0}", isMixed);
        
        // Convert UI difficulty to database format (uppercase)
        String dbDifficulty = difficulty.toUpperCase();
        LOGGER.log(Level.INFO, "Converted UI difficulty '{0}' to database format '{1}'", new Object[]{difficulty, dbDifficulty});
        
        // If no topics are selected, use all topics
        boolean useAllTopics = selectedTopics == null || selectedTopics.isEmpty();
        LOGGER.log(Level.INFO, "Using all topics: {0}", useAllTopics);
        
        // Create a list of topic names for easier comparison
        List<String> topicNames = new ArrayList<>();
        if (!useAllTopics) {
            for (Topic topic : selectedTopics) {
                if (topic.isSelected()) {
                    topicNames.add(topic.getTopicName());
                }
            }
            // If no topics were actually selected (all checkboxes unchecked), use all topics
            useAllTopics = topicNames.isEmpty();
            LOGGER.log(Level.INFO, "Selected topics: {0}, using all topics: {1}", new Object[]{topicNames, useAllTopics});
        }
        
        for (Question question : questions) {
            // Check if the question matches the selected difficulty or if we want mixed difficulty
            boolean difficultyMatches = isMixed || dbDifficulty.equals(question.getDifficulty());
            
            // Check if the question matches any of the selected topics or if we're using all topics
            boolean topicMatches = useAllTopics || 
                                  (question.getTopic() != null && topicNames.contains(question.getTopic()));
            
            if (difficultyMatches && topicMatches) {
                filteredQuestions.add(question);
                if (filteredQuestions.size() >= count && !isMixed) {
                    break; // We have enough questions of the specific difficulty
                }
            }
        }
        
        LOGGER.log(Level.INFO, "Filtered questions count: {0}", filteredQuestions.size());
        
        // If we're using mixed difficulty, limit to the requested count
        if (isMixed && filteredQuestions.size() > count) {
            // Shuffle the list to get a random mix of difficulties
            java.util.Collections.shuffle(filteredQuestions);
            filteredQuestions = filteredQuestions.subList(0, count);
            LOGGER.log(Level.INFO, "Shuffled and limited mixed difficulty questions to {0}", count);
        }
        
        // Add questions to the assessment
        int order = 1;
        for (Question question : filteredQuestions) {
            LOGGER.log(Level.INFO, "Adding question ID {0} to assessment {1} at order {2}", 
                      new Object[]{question.getQuestionId(), assessmentId, order});
            AssessmentService.addQuestionToAssessment(assessmentId, question.getQuestionId(), order++);
        }
        
        LOGGER.log(Level.INFO, "Successfully added {0} questions to assessment {1}", 
                  new Object[]{filteredQuestions.size(), assessmentId});
        return filteredQuestions.size();
    }
    
    /**
     * Loads assessments for a specific unit.
     * 
     * @param unit The unit to load assessments for
     */
    private void loadAssessments(Unit unit) {
        try {
            assessments.clear();
            
            if (unit != null) {
                List<Assessment> unitAssessments = AssessmentService.getAssessmentsByUnit(unit.getUnitCode());
                assessments.addAll(unitAssessments);
            } else {
                // If no unit selected, load all assessments for this lecturer
                List<Assessment> lecturerAssessments = getAssessmentsByLecturer(currentLecturer.getUserId());
                assessments.addAll(lecturerAssessments);
            }
            
            assessmentsTable.setItems(assessments);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading assessments", e);
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                "Failed to load assessments", 
                "An error occurred while loading assessments: " + e.getMessage());
        }
    }
    
    /**
     * Gets assessments created by a lecturer.
     * 
     * @param lecturerId The lecturer ID
     * @return List of assessments created by the lecturer
     * @throws SQLException If a database error occurs
     */
    private List<Assessment> getAssessmentsByLecturer(String lecturerId) throws SQLException {
        List<Assessment> lecturerAssessments = new ArrayList<>();
        
        // First, get the lecturer's staff ID from the lecturers table
        String staffId = null;
        String getStaffIdQuery = "SELECT staff_id FROM lecturers WHERE user_id = ?";
        try (PreparedStatement staffStmt = DatabaseUtil.prepareStatement(getStaffIdQuery)) {
            staffStmt.setString(1, lecturerId);
            ResultSet staffRs = staffStmt.executeQuery();
            if (staffRs.next()) {
                staffId = staffRs.getString("staff_id");
                LOGGER.info("Found staff ID: " + staffId + " for lecturer ID: " + lecturerId);
            } else {
                LOGGER.warning("No staff ID found for lecturer: " + lecturerId);
                return lecturerAssessments; // Return empty list if no staff ID found
            }
        }
        
        // Get all units assigned to this lecturer
        List<String> unitCodes = new ArrayList<>();
        String unitsQuery = "SELECT unit_code FROM units WHERE lecturer_id = ?";
        try (PreparedStatement unitsStmt = DatabaseUtil.prepareStatement(unitsQuery)) {
            unitsStmt.setString(1, staffId);
            ResultSet unitsRs = unitsStmt.executeQuery();
            while (unitsRs.next()) {
                unitCodes.add(unitsRs.getString("unit_code"));
            }
        }
        
        if (unitCodes.isEmpty()) {
            LOGGER.warning("No units found for lecturer with staff ID: " + staffId);
            return lecturerAssessments; // Return empty list if no units found
        }
        
        // Build query to get assessments for all units assigned to this lecturer
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM assessments WHERE unit_code IN (");
        for (int i = 0; i < unitCodes.size(); i++) {
            queryBuilder.append("?");
            if (i < unitCodes.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(") OR created_by = ?");
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(queryBuilder.toString())) {
            // Set unit code parameters
            for (int i = 0; i < unitCodes.size(); i++) {
                stmt.setString(i + 1, unitCodes.get(i));
            }
            // Also include assessments directly created by this lecturer (using user_id)
            stmt.setString(unitCodes.size() + 1, lecturerId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Assessment assessment = new Assessment();
                assessment.setAssessmentId(rs.getString("assessment_id"));
                assessment.setTitle(rs.getString("title"));
                assessment.setDescription(rs.getString("description"));
                assessment.setUnitCode(rs.getString("unit_code"));
                assessment.setCreatedBy(rs.getString("created_by"));
                assessment.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                assessment.setStartDateTime(rs.getObject("start_date_time", LocalDateTime.class));
                assessment.setEndDateTime(rs.getObject("end_date_time", LocalDateTime.class));
                assessment.setDurationMinutes(rs.getInt("duration_minutes"));
                assessment.setActive(rs.getBoolean("is_active"));
                assessment.setPractice(rs.getBoolean("is_practice"));
                assessment.setTotalMarks(rs.getInt("total_marks"));
                assessment.setAllowOfflineAttempt(rs.getBoolean("allow_offline_attempt"));
                
                lecturerAssessments.add(assessment);
            }
        }
        
        return lecturerAssessments;
    }
    
    /**
     * Displays the details of an assessment.
     * 
     * @param assessment The assessment to view
     */
    private void viewAssessment(Assessment assessment) {
        LOGGER.log(Level.INFO, "Viewing assessment: " + assessment.getTitle());
        
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Assessment Details");
            dialog.setHeaderText("Assessment: " + assessment.getTitle());
            
            // Set the button types
            ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButtonType);
            
            // Create the content grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Add assessment details
            int row = 0;
            
            grid.add(new Label("Title:"), 0, row);
            grid.add(new Label(assessment.getTitle()), 1, row++);
            
            grid.add(new Label("Unit:"), 0, row);
            grid.add(new Label(assessment.getUnitCode()), 1, row++);
            
            grid.add(new Label("Type:"), 0, row);
            grid.add(new Label(assessment.isPractice() ? "Practice" : "Formal"), 1, row++);
            
            grid.add(new Label("Description:"), 0, row);
            TextArea descArea = new TextArea(assessment.getDescription());
            descArea.setEditable(false);
            descArea.setPrefRowCount(3);
            grid.add(descArea, 1, row++);
            
            grid.add(new Label("Start Date:"), 0, row);
            grid.add(new Label(assessment.getStartDateTime() != null ? 
                    assessment.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
                    "Not scheduled"), 1, row++);
            
            grid.add(new Label("End Date:"), 0, row);
            grid.add(new Label(assessment.getEndDateTime() != null ? 
                    assessment.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
                    "Not scheduled"), 1, row++);
            
            grid.add(new Label("Duration:"), 0, row);
            grid.add(new Label(assessment.getDurationMinutes() + " minutes"), 1, row++);
            
            grid.add(new Label("Total Marks:"), 0, row);
            grid.add(new Label(Integer.toString(assessment.getTotalMarks())), 1, row++);
            
            // Load questions for this assessment
            List<Question> questions = QuestionService.getQuestionsByAssessment(assessment.getAssessmentId());
            
            grid.add(new Label("Questions:"), 0, row);
            grid.add(new Label(questions.size() + " questions"), 1, row++);
            
            // Add a list of questions
            ListView<String> questionsList = new ListView<>();
            ObservableList<String> questionItems = FXCollections.observableArrayList();
            
            for (Question question : questions) {
                questionItems.add(question.getQuestionText());
            }
            
            questionsList.setItems(questionItems);
            questionsList.setPrefHeight(150);
            grid.add(questionsList, 0, row++, 2, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing assessment", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to view assessment", 
                    "An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Opens the assessment editor for an existing assessment.
     * 
     * @param assessment The assessment to edit
     */
    private void editAssessment(Assessment assessment) {
        LOGGER.log(Level.INFO, "Editing assessment: " + assessment.getTitle());
        
        try {
            // Get the unit for this assessment
            Unit unit = null;
            for (Unit u : assignedUnits) {
                if (u.getUnitCode().equals(assessment.getUnitCode())) {
                    unit = u;
                    break;
                }
            }
            
            if (unit == null) {
                LOGGER.log(Level.WARNING, "Unit not found for assessment: " + assessment.getUnitCode());
                showAlert(Alert.AlertType.WARNING, "Warning", 
                        "Unit not found", 
                        "The unit for this assessment could not be found.");
                return;
            }
            
            // Create a dialog
            Dialog<Assessment> dialog = new Dialog<>();
            dialog.setTitle("Edit Assessment");
            dialog.setHeaderText("Edit Assessment for " + unit.getUnitCode() + " - " + unit.getUnitName());
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Form fields
            TextField titleField = new TextField(assessment.getTitle());
            titleField.setPromptText("Assessment Title");
            
            TextArea descriptionArea = new TextArea(assessment.getDescription());
            descriptionArea.setPromptText("Assessment Description");
            descriptionArea.setPrefRowCount(3);
            
            ComboBox<String> difficultyCombo = new ComboBox<>();
            difficultyCombo.getItems().addAll("Easy", "Medium", "Hard", "Mixed");
            difficultyCombo.setValue("Medium"); // Set default or get from assessment
            
            LocalDateTime startDateTime = assessment.getStartDateTime();
            DatePicker datePicker = new DatePicker(startDateTime != null ? startDateTime.toLocalDate() : LocalDate.now());
            
            Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 
                    startDateTime != null ? startDateTime.getHour() : LocalTime.now().getHour());
            hourSpinner.setEditable(true);
            hourSpinner.setPrefWidth(70);
            
            Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 
                    startDateTime != null ? startDateTime.getMinute() : LocalTime.now().getMinute());
            minuteSpinner.setEditable(true);
            minuteSpinner.setPrefWidth(70);
            
            HBox timeBox = new HBox(5, hourSpinner, new Label(":"), minuteSpinner);
            
            Spinner<Integer> durationSpinner = new Spinner<>(10, 180, assessment.getDurationMinutes());
            durationSpinner.setEditable(true);
            
            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Difficulty:"), 0, 2);
            grid.add(difficultyCombo, 1, 2);
            grid.add(new Label("Date:"), 0, 3);
            grid.add(datePicker, 1, 3);
            grid.add(new Label("Time:"), 0, 4);
            grid.add(timeBox, 1, 4);
            grid.add(new Label("Duration (minutes):"), 0, 5);
            grid.add(durationSpinner, 1, 5);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);
            
            // Convert the result to an assessment when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Get form values
                        String title = titleField.getText();
                        String description = descriptionArea.getText();
                        
                        // Validate input
                        if (title.trim().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                                    "Title is required", 
                                    "Please enter a title for the assessment.");
                            return null;
                        }
                        
                        // Create start date time
                        LocalDate date = datePicker.getValue();
                        LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
                        LocalDateTime startDate = LocalDateTime.of(date, time);
                        
                        // Calculate end date time based on duration
                        LocalDateTime endDate = startDate.plusMinutes(durationSpinner.getValue());
                        
                        // Update assessment
                        assessment.setTitle(title);
                        assessment.setDescription(description);
                        assessment.setStartDateTime(startDate);
                        assessment.setEndDateTime(endDate);
                        assessment.setDurationMinutes(durationSpinner.getValue());
                        
                        // Save to database
                        boolean success = AssessmentService.updateAssessment(assessment);
                        
                        if (success) {
                            LOGGER.log(Level.INFO, "Assessment updated successfully: " + title);
                            return assessment;
                        } else {
                            LOGGER.log(Level.SEVERE, "Failed to update assessment: " + title);
                            showAlert(Alert.AlertType.ERROR, "Error", 
                                    "Failed to update assessment", 
                                    "An error occurred while updating the assessment.");
                            return null;
                        }
                        
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error updating assessment", e);
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                "Failed to update assessment", 
                                "An error occurred: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            // Show the dialog and handle the result
            Optional<Assessment> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                // Refresh the assessments table
                loadAssessments(unit);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing assessment", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to edit assessment", 
                    "An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Shows the dashboard content.
     */
    @FXML
    private void showDashboard() {
        dashboardContent.setVisible(true);
        assessmentsContent.setVisible(false);
        analyticsContent.setVisible(false);
        reportsContent.setVisible(false);
        settingsContent.setVisible(false);
        
        resetNavigationIcons();
        dashboardIcon.setIconColor(Color.valueOf("#2196F3"));
    }
    
    /**
     * Shows the assessments content.
     */
    @FXML
    private void showAssessments() {
        dashboardContent.setVisible(false);
        assessmentsContent.setVisible(true);
        analyticsContent.setVisible(false);
        reportsContent.setVisible(false);
        settingsContent.setVisible(false);
        
        resetNavigationIcons();
        assessmentsIcon.setIconColor(Color.valueOf("#2196F3"));
        
        // Load all assessments for this lecturer
        loadAssessments(null);
    }
    
    /**
     * Shows the analytics content.
     */
    @FXML
    private void showAnalytics() {
        LOGGER.log(Level.INFO, "Showing analytics view");
        
        dashboardContent.setVisible(false);
        assessmentsContent.setVisible(false);
        analyticsContent.setVisible(true);
        reportsContent.setVisible(false);
        settingsContent.setVisible(false);
        
        // If analytics content is not yet initialized, load the performance view
        if (analyticsContent.getChildren().isEmpty()) {
            try {
                LOGGER.log(Level.INFO, "Loading lecturer-performance-view.fxml");
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/lecturer-performance-view.fxml"));
                Parent analyticsView = loader.load();
                
                LecturerPerformanceController controller = loader.getController();
                LOGGER.log(Level.INFO, "Setting current lecturer: " + (currentLecturer != null ? currentLecturer.getFullName() : "null"));
                controller.setCurrentLecturer(currentLecturer);
                controller.loadPerformanceData();
                
                analyticsContent.getChildren().clear(); // Clear any existing children
                analyticsContent.getChildren().add(analyticsView);
                VBox.setVgrow(analyticsView, Priority.ALWAYS);
                
                LOGGER.log(Level.INFO, "Analytics view loaded successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading analytics view", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load analytics view", 
                        "An error occurred while loading the analytics view: " + e.getMessage());
            }
        } else {
            LOGGER.log(Level.INFO, "Analytics view already loaded");
        }
        
        resetNavigationIcons();
        analyticsIcon.setIconColor(Color.valueOf("#2196F3"));
    }
    
    /**
     * Shows the reports content.
     */
    @FXML
    private void showReports() {
        dashboardContent.setVisible(false);
        assessmentsContent.setVisible(false);
        analyticsContent.setVisible(false);
        reportsContent.setVisible(true);
        settingsContent.setVisible(false);
        
        resetNavigationIcons();
        reportsIcon.setIconColor(Color.valueOf("#2196F3"));
        
        // Initialize the proctoring violations table if not already done
        if (proctoringViolationsTable.getColumns().isEmpty()) {
            setupProctoringViolationsTable();
        }
        
        // Load proctoring violations
        loadProctoringViolations();
        
        // Initialize filter combo boxes if not already done
        if (proctoringTimeFilterComboBox.getItems().isEmpty()) {
            proctoringTimeFilterComboBox.getItems().addAll(
                "All Time",
                "Today",
                "This Week",
                "This Month"
            );
            proctoringTimeFilterComboBox.getSelectionModel().selectFirst();
            
            proctoringTimeFilterComboBox.setOnAction(e -> loadProctoringViolations());
        }
        
        // Initialize unit filter if not already done
        if (proctoringUnitFilterComboBox.getItems().isEmpty()) {
            // Add "All Units" option
            Unit allUnits = new Unit();
            allUnits.setUnitCode("ALL");
            allUnits.setUnitName("All Units");
            
            proctoringUnitFilterComboBox.getItems().add(allUnits);
            proctoringUnitFilterComboBox.getItems().addAll(assignedUnits);
            proctoringUnitFilterComboBox.getSelectionModel().selectFirst();
            
            proctoringUnitFilterComboBox.setConverter(new StringConverter<Unit>() {
                @Override
                public String toString(Unit unit) {
                    if (unit == null) {
                        return "";
                    }
                    if ("ALL".equals(unit.getUnitCode())) {
                        return "All Units";
                    }
                    return unit.getUnitCode() + " - " + unit.getUnitName();
                }
                
                @Override
                public Unit fromString(String string) {
                    return null;
                }
            });
            
            proctoringUnitFilterComboBox.setOnAction(e -> loadProctoringViolations());
        }
        
        // Update the title
        Text header = new Text("Proctoring Violations");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Update the description
        Text description = new Text("This table shows window focus violations detected during assessments. " +
                                   "These occur when students switch away from the assessment window, " +
                                   "which may indicate potential cheating attempts.");
        description.setStyle("-fx-font-size: 14px; -fx-fill: #555;");
        description.setWrappingWidth(600);
        
        // Clear and update the container
        if (reportDescriptionsContainer != null) {
            reportDescriptionsContainer.getChildren().clear();
            reportDescriptionsContainer.getChildren().addAll(header, description);
            
            // Add a legend for severity levels
            HBox legend = new HBox(10);
            legend.setPadding(new Insets(10, 0, 20, 0));
            legend.setAlignment(Pos.CENTER_LEFT);
            
            // High severity
            VBox highBox = new VBox(5);
            Rectangle highRect = new Rectangle(20, 20);
            highRect.setFill(Color.valueOf("#ffebee"));
            highRect.setStroke(Color.valueOf("#f44336"));
            Label highLabel = new Label("High Severity (>5 min)");
            highBox.getChildren().addAll(highRect, highLabel);
            
            // Medium severity
            VBox mediumBox = new VBox(5);
            Rectangle mediumRect = new Rectangle(20, 20);
            mediumRect.setFill(Color.valueOf("#fff8e1"));
            mediumRect.setStroke(Color.valueOf("#ffc107"));
            Label mediumLabel = new Label("Medium Severity (1-5 min)");
            mediumBox.getChildren().addAll(mediumRect, mediumLabel);
            
            // Low severity
            VBox lowBox = new VBox(5);
            Rectangle lowRect = new Rectangle(20, 20);
            lowRect.setFill(Color.valueOf("#f1f8e9"));
            lowRect.setStroke(Color.valueOf("#8bc34a"));
            Label lowLabel = new Label("Low Severity (<1 min)");
            lowBox.getChildren().addAll(lowRect, lowLabel);
            
            legend.getChildren().addAll(highBox, mediumBox, lowBox);
            reportDescriptionsContainer.getChildren().add(legend);
        }
    }
    
    /**
     * Sets up the proctoring violations table with columns and cell factories.
     */
    private void setupProctoringViolationsTable() {
        LOGGER.log(Level.INFO, "Setting up proctoring violations table");
        
        // Get the existing columns from FXML
        if (proctoringViolationsTable.getColumns().size() >= 6) {
            LOGGER.log(Level.INFO, "Using existing columns from FXML");
            
            // Get columns by index
            TableColumn<ProctoringViolation, String> studentColumn = (TableColumn<ProctoringViolation, String>) proctoringViolationsTable.getColumns().get(0);
            TableColumn<ProctoringViolation, String> assessmentColumn = (TableColumn<ProctoringViolation, String>) proctoringViolationsTable.getColumns().get(1);
            TableColumn<ProctoringViolation, String> unitColumn = (TableColumn<ProctoringViolation, String>) proctoringViolationsTable.getColumns().get(2);
            TableColumn<ProctoringViolation, String> startTimeColumn = (TableColumn<ProctoringViolation, String>) proctoringViolationsTable.getColumns().get(3);
            TableColumn<ProctoringViolation, String> durationColumn = (TableColumn<ProctoringViolation, String>) proctoringViolationsTable.getColumns().get(4);
            TableColumn<ProctoringViolation, String> severityColumn = (TableColumn<ProctoringViolation, String>) proctoringViolationsTable.getColumns().get(5);
            
            // Set cell value factories for each column
            studentColumn.setCellValueFactory(cellData -> {
                String name = cellData.getValue().getStudentName();
                LOGGER.log(Level.FINE, "Student name for row: {0}", name);
                return new SimpleStringProperty(name != null ? name : "Unknown");
            });
            
            assessmentColumn.setCellValueFactory(cellData -> {
                String title = cellData.getValue().getAssessmentTitle();
                LOGGER.log(Level.FINE, "Assessment title for row: {0}", title);
                return new SimpleStringProperty(title != null ? title : "Unknown");
            });
            
            unitColumn.setCellValueFactory(cellData -> {
                String unit = cellData.getValue().getUnitCode();
                LOGGER.log(Level.FINE, "Unit code for row: {0}", unit);
                return new SimpleStringProperty(unit != null ? unit : "N/A");
            });
            
            startTimeColumn.setCellValueFactory(cellData -> {
                LocalDateTime time = cellData.getValue().getStartTime();
                if (time != null) {
                    return new SimpleStringProperty(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    return new SimpleStringProperty("Unknown");
                }
            });
            
            durationColumn.setCellValueFactory(cellData -> {
                String duration = cellData.getValue().getFormattedDuration();
                LOGGER.log(Level.FINE, "Duration for row: {0}", duration);
                return new SimpleStringProperty(duration);
            });
            
            severityColumn.setCellValueFactory(cellData -> {
                String severity = cellData.getValue().getSeverityLevel();
                LOGGER.log(Level.FINE, "Severity for row: {0}", severity);
                return new SimpleStringProperty(severity);
            });
            
            // Set cell factory for severity column
            severityColumn.setCellFactory(column -> new TableCell<ProctoringViolation, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        
                        // Set background color based on severity
                        if ("HIGH".equals(item)) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f;");
                        } else if ("MEDIUM".equals(item)) {
                            setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;");
                        } else {
                            setStyle("-fx-background-color: #f1f8e9; -fx-text-fill: #33691e;");
                        }
                    }
                }
            });
        } else {
            // If columns aren't defined in FXML, create them programmatically
            LOGGER.log(Level.INFO, "Creating columns programmatically");
            
            // Clear existing columns
            proctoringViolationsTable.getColumns().clear();
            
            // Student column
            TableColumn<ProctoringViolation, String> studentColumn = new TableColumn<>("Student");
            studentColumn.setCellValueFactory(cellData -> {
                String name = cellData.getValue().getStudentName();
                return new SimpleStringProperty(name != null ? name : "Unknown");
            });
            studentColumn.setPrefWidth(150);
            
            // Assessment column
            TableColumn<ProctoringViolation, String> assessmentColumn = new TableColumn<>("Assessment");
            assessmentColumn.setCellValueFactory(cellData -> {
                String title = cellData.getValue().getAssessmentTitle();
                return new SimpleStringProperty(title != null ? title : "Unknown");
            });
            assessmentColumn.setPrefWidth(200);
            
            // Unit column
            TableColumn<ProctoringViolation, String> unitColumn = new TableColumn<>("Unit");
            unitColumn.setCellValueFactory(cellData -> {
                String unit = cellData.getValue().getUnitCode();
                return new SimpleStringProperty(unit != null ? unit : "N/A");
            });
            unitColumn.setPrefWidth(80);
            
            // Start time column
            TableColumn<ProctoringViolation, String> startTimeColumn = new TableColumn<>("Start Time");
            startTimeColumn.setCellValueFactory(cellData -> {
                LocalDateTime time = cellData.getValue().getStartTime();
                if (time != null) {
                    return new SimpleStringProperty(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    return new SimpleStringProperty("Unknown");
                }
            });
            startTimeColumn.setPrefWidth(150);
            
            // Duration column
            TableColumn<ProctoringViolation, String> durationColumn = new TableColumn<>("Duration");
            durationColumn.setCellValueFactory(cellData -> {
                String duration = cellData.getValue().getFormattedDuration();
                return new SimpleStringProperty(duration);
            });
            durationColumn.setPrefWidth(100);
            
            // Severity column
            TableColumn<ProctoringViolation, String> severityColumn = new TableColumn<>("Severity");
            severityColumn.setCellValueFactory(cellData -> {
                String severity = cellData.getValue().getSeverityLevel();
                return new SimpleStringProperty(severity);
            });
            severityColumn.setPrefWidth(80);
            severityColumn.setCellFactory(column -> new TableCell<ProctoringViolation, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        
                        // Set background color based on severity
                        if ("HIGH".equals(item)) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f;");
                        } else if ("MEDIUM".equals(item)) {
                            setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f57f17;");
                        } else {
                            setStyle("-fx-background-color: #f1f8e9; -fx-text-fill: #33691e;");
                        }
                    }
                }
            });
            
            // Add columns to table
            proctoringViolationsTable.getColumns().addAll(
                studentColumn, assessmentColumn, unitColumn, startTimeColumn, durationColumn, severityColumn
            );
        }
        
        // Set row factory to color rows based on severity
        proctoringViolationsTable.setRowFactory(tv -> new TableRow<ProctoringViolation>() {
            @Override
            protected void updateItem(ProctoringViolation item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String severity = item.getSeverityLevel();
                    
                    if ("HIGH".equals(severity)) {
                        setStyle("-fx-background-color: rgba(255, 235, 238, 0.3);");
                    } else if ("MEDIUM".equals(severity)) {
                        setStyle("-fx-background-color: rgba(255, 248, 225, 0.3);");
                    } else {
                        setStyle("-fx-background-color: rgba(241, 248, 233, 0.3);");
                    }
                }
            }
        });
        
        // Add double-click handler to view details
        proctoringViolationsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && proctoringViolationsTable.getSelectionModel().getSelectedItem() != null) {
                ProctoringViolation selectedViolation = proctoringViolationsTable.getSelectionModel().getSelectedItem();
                LOGGER.log(Level.INFO, "Viewing details for violation ID: {0}", selectedViolation.getViolationId());
                viewViolationDetails(selectedViolation);
            }
        });
        
        LOGGER.log(Level.INFO, "Proctoring violations table setup complete with {0} columns", proctoringViolationsTable.getColumns().size());
    }
    
    /**
     * Displays details of a proctoring violation.
     * 
     * @param violation The violation to view
     */
    private void viewViolationDetails(ProctoringViolation violation) {
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Proctoring Violation Details");
            dialog.setHeaderText("Violation Details");
            
            // Set the button types
            ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButtonType);
            
            // Create the content grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Add violation details
            int row = 0;
            
            grid.add(new Label("Violation ID:"), 0, row);
            grid.add(new Label(String.valueOf(violation.getViolationId())), 1, row++);
            
            grid.add(new Label("Student:"), 0, row);
            grid.add(new Label(violation.getStudentName() + " (" + violation.getStudentId() + ")"), 1, row++);
            
            grid.add(new Label("Assessment:"), 0, row);
            grid.add(new Label(violation.getAssessmentTitle()), 1, row++);
            
            grid.add(new Label("Unit:"), 0, row);
            grid.add(new Label(violation.getUnitCode()), 1, row++);
            
            grid.add(new Label("Start Time:"), 0, row);
            grid.add(new Label(violation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), 1, row++);
            
            grid.add(new Label("End Time:"), 0, row);
            grid.add(new Label(violation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), 1, row++);
            
            grid.add(new Label("Duration:"), 0, row);
            grid.add(new Label(violation.getFormattedDuration()), 1, row++);
            
            grid.add(new Label("Severity:"), 0, row);
            Label severityLabel = new Label(violation.getSeverityLevel());
            if ("HIGH".equals(violation.getSeverityLevel())) {
                severityLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            } else if ("MEDIUM".equals(violation.getSeverityLevel())) {
                severityLabel.setStyle("-fx-text-fill: #f57f17; -fx-font-weight: bold;");
            } else {
                severityLabel.setStyle("-fx-text-fill: #33691e; -fx-font-weight: bold;");
            }
            grid.add(severityLabel, 1, row++);
            
            dialog.getDialogPane().setContent(grid);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing violation details", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to view violation details", 
                    "An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Loads proctoring violations from the database based on the selected filters.
     */
    private void loadProctoringViolations() {
        LOGGER.log(Level.INFO, "Starting to load proctoring violations");
        try {
            // Get selected filters
            String timeFilter = proctoringTimeFilterComboBox.getValue();
            Unit selectedUnit = proctoringUnitFilterComboBox.getValue();
            String unitFilter = (selectedUnit != null && !"ALL".equals(selectedUnit.getUnitCode())) 
                              ? selectedUnit.getUnitCode() : null;
            
            LOGGER.log(Level.INFO, "Filters applied - Time: {0}, Unit: {1}", new Object[]{timeFilter, unitFilter});
            
            // Create a list to hold all violations
            ObservableList<ProctoringViolation> violations = FXCollections.observableArrayList();
            
            // Only load from the simple table as per user request
            loadViolationsFromSimpleTable(violations, timeFilter, unitFilter);
            
            // Log the number of violations found
            LOGGER.log(Level.INFO, "Found {0} violations from focus_violations_simple table", violations.size());
            
            // Debug: Log each violation in the list
            for (int i = 0; i < violations.size(); i++) {
                ProctoringViolation v = violations.get(i);
                LOGGER.log(Level.INFO, "Violation #{0}: ID={1}, Student={2}, Assessment={3}", 
                          new Object[]{i+1, v.getViolationId(), v.getStudentName(), v.getAssessmentTitle()});
            }
            
            // Ensure the table is initialized
            if (proctoringViolationsTable.getColumns().isEmpty()) {
                setupProctoringViolationsTable();
            }
            
            // Update the table
            Platform.runLater(() -> {
                LOGGER.log(Level.INFO, "Setting items on table: {0} violations", violations.size());
                proctoringViolationsTable.getItems().clear();
                proctoringViolationsTable.setItems(violations);
                proctoringViolationsTable.refresh();
                LOGGER.log(Level.INFO, "Table items count after setting: {0}", 
                          proctoringViolationsTable.getItems().size());
            });
            
            // Update the total violations label
            if (proctoringTotalViolationsLabel != null) {
                proctoringTotalViolationsLabel.setText("Total Violations: " + violations.size());
            }
            
            // Set placeholder text if no violations found
            if (violations.isEmpty()) {
                LOGGER.log(Level.INFO, "No violations found, setting placeholder text");
                proctoringViolationsTable.setPlaceholder(new Label("No proctoring violations detected yet. The system is ready to track violations."));
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading proctoring violations", e);
            
            // Set an empty list and a message
            ObservableList<ProctoringViolation> emptyList = FXCollections.observableArrayList();
            proctoringViolationsTable.setItems(emptyList);
            proctoringViolationsTable.setPlaceholder(new Label("Could not load proctoring violations. The system is being set up."));
            
            if (proctoringTotalViolationsLabel != null) {
                proctoringTotalViolationsLabel.setText("Total Violations: 0");
            }
        }
    }
    
    /**
     * Loads violations from the simple focus_violations_simple table.
     * 
     * @param violations The list to add violations to
     * @param timeFilter The time filter to apply
     * @param unitFilter The unit filter to apply
     * @throws SQLException If a database error occurs
     */
    private void loadViolationsFromSimpleTable(ObservableList<ProctoringViolation> violations, 
                                              String timeFilter, String unitFilter) throws SQLException {
        // Check if the focus_violations_simple table exists
        boolean tableExists = false;
        try {
            String checkTableSql = "SHOW TABLES LIKE 'focus_violations_simple'";
            LOGGER.log(Level.INFO, "Checking if focus_violations_simple table exists with query: {0}", checkTableSql);
            ResultSet rs = DatabaseUtil.executeQuery(checkTableSql);
            tableExists = rs.next();
            LOGGER.log(Level.INFO, "focus_violations_simple table exists: {0}", tableExists);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking if focus_violations_simple table exists", e);
            return;
        }
        
        if (!tableExists) {
            LOGGER.log(Level.INFO, "focus_violations_simple table does not exist, attempting to create it");
            try {
                // Create the table if it doesn't exist
                String createTableSql = "CREATE TABLE IF NOT EXISTS focus_violations_simple (" +
                                       "violation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                       "assessment_id VARCHAR(50) NOT NULL, " +
                                       "student_id VARCHAR(50) NOT NULL, " +
                                       "student_username VARCHAR(100), " +
                                       "start_time DATETIME NOT NULL, " +
                                       "end_time DATETIME NOT NULL, " +
                                       "duration_seconds BIGINT NOT NULL)";
                LOGGER.log(Level.INFO, "Creating focus_violations_simple table with query: {0}", createTableSql);
                DatabaseUtil.executeUpdate(createTableSql);
                LOGGER.log(Level.INFO, "focus_violations_simple table created successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to create focus_violations_simple table", e);
            }
            return;
        }
        
        // Debug: Get count of records in the table
        try {
            String countSql = "SELECT COUNT(*) FROM focus_violations_simple";
            ResultSet countRs = DatabaseUtil.executeQuery(countSql);
            if (countRs.next()) {
                int count = countRs.getInt(1);
                LOGGER.log(Level.INFO, "Total records in focus_violations_simple table: {0}", count);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting records in focus_violations_simple table", e);
        }
        
        // Build the query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM focus_violations_simple WHERE 1=1 ");
        
        // Add time filter
        if (!"All Time".equals(timeFilter)) {
            LocalDateTime startDate = null;
            
            if ("Today".equals(timeFilter)) {
                startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            } else if ("This Week".equals(timeFilter)) {
                startDate = LocalDateTime.now().minusDays(7);
            } else if ("This Month".equals(timeFilter)) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            
            if (startDate != null) {
                queryBuilder.append("AND start_time >= '")
                           .append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                           .append("' ");
            }
        }
        
        // Order by start time descending (most recent first)
        queryBuilder.append("ORDER BY start_time DESC ");
        
        // Log the final query
        String finalQuery = queryBuilder.toString();
        LOGGER.log(Level.INFO, "Executing query: {0}", finalQuery);
        
        // Execute the query
        try (ResultSet rs = DatabaseUtil.executeQuery(finalQuery)) {
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                String assessmentId = rs.getString("assessment_id");
                String studentId = rs.getString("student_id");
                String studentName = rs.getString("student_username");
                
                LOGGER.log(Level.INFO, "Found violation - Assessment ID: {0}, Student: {1}", 
                          new Object[]{assessmentId, studentName});
                
                // Create a violation object with the data we have
                ProctoringViolation violation = new ProctoringViolation(
                    rs.getInt("violation_id"),
                    assessmentId,
                    "Assessment " + assessmentId,  // Placeholder title
                    studentId,
                    studentName != null ? studentName : studentId,
                    "N/A",  // Placeholder unit code
                    rs.getTimestamp("start_time").toLocalDateTime(),
                    rs.getTimestamp("end_time").toLocalDateTime(),
                    rs.getLong("duration_seconds")
                );
                
                violations.add(violation);
            }
            LOGGER.log(Level.INFO, "Query returned {0} rows", rowCount);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error querying focus_violations_simple table: {0}", e.getMessage());
            LOGGER.log(Level.WARNING, "SQL State: {0}, Error Code: {1}", new Object[]{e.getSQLState(), e.getErrorCode()});
            LOGGER.log(Level.WARNING, "Stack trace:", e);
        }
    }
    
    /**
     * Adds a time filter to the query builder.
     * 
     * @param queryBuilder The query builder to add the filter to
     * @param timeFilter The time filter to apply
     */
    private void addTimeFilter(StringBuilder queryBuilder, String timeFilter) {
        if (!"All Time".equals(timeFilter)) {
            LocalDateTime startDate = null;
            
            if ("Today".equals(timeFilter)) {
                startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            } else if ("This Week".equals(timeFilter)) {
                startDate = LocalDateTime.now().minusDays(7);
            } else if ("This Month".equals(timeFilter)) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            
            if (startDate != null) {
                queryBuilder.append("AND start_time >= '")
                           .append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                           .append("' ");
            }
        }
    }
    
    /**
     * Resets all navigation icons to their default style.
     */
    private void resetNavigationIcons() {
        dashboardIcon.setIconColor(Color.BLACK);
        assessmentsIcon.setIconColor(Color.BLACK);
        analyticsIcon.setIconColor(Color.BLACK);
        reportsIcon.setIconColor(Color.BLACK);
        settingsIcon.setIconColor(Color.BLACK);
    }
    
    /**
     * Handles logout.
     */
    @FXML
    private void handleLogout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/login-view.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) dashboardIcon.getScene().getWindow();
            
            // Create new scene and set it on the stage
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/main.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Kenyatta University CAT System - Login");
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Failed to logout", 
                "An error occurred while trying to logout: " + e.getMessage());
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
     * Generates a report file based on the selected report type.
     * 
     * @param reportType The type of report to generate
     * @return The path to the generated report file
     */
    private String generateReportFile(String reportType) {
        try {
            // Create reports directory if it doesn't exist
            String userDir = System.getProperty("user.dir");
            File reportsDir = new File(userDir, "reports");
            
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
                LOGGER.info("Created reports directory at: " + reportsDir.getAbsolutePath());
            }
            
            // Generate a unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String safeReportName = reportType.replaceAll("\\s+", "_").toLowerCase();
            String filename = safeReportName + "_" + timestamp + ".html";
            File reportFile = new File(reportsDir, filename);
            
            // Generate report content based on type
            String reportContent = "";
            
            switch (reportType) {
                case "Student Performance Report":
                    reportContent = generateStudentPerformanceReport();
                    break;
                case "Assessment Results Summary":
                    reportContent = generateAssessmentResultsReport();
                    break;
                case "Unit Progress Report":
                    reportContent = generateUnitProgressReport();
                    break;
                case "Question Analysis Report":
                    reportContent = generateQuestionAnalysisReport();
                    break;
                case "Attendance Report":
                    reportContent = generateAttendanceReport();
                    break;
                default:
                    reportContent = "<h1>Report Not Implemented</h1><p>The selected report type is not yet implemented.</p>";
            }
            
            // Write the report to file
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(reportContent);
            }
            
            LOGGER.info("Generated report file: " + reportFile.getAbsolutePath());
            return reportFile.getAbsolutePath();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report file", e);
            return null;
        }
    }
    
    /**
     * Saves a report execution to the database.
     * 
     * @param reportType The type of report
     * @param reportFilePath The path to the generated report file
     */
    private void saveReportExecutionToDatabase(String reportType, String reportFilePath) {
        try {
            // Find the report ID for this report type
            String reportId = null;
            String findReportQuery = "SELECT report_id FROM reports WHERE report_name LIKE ? LIMIT 1";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(findReportQuery)) {
                stmt.setString(1, "%" + reportType + "%");
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    reportId = rs.getString("report_id");
                    LOGGER.info("Found report ID: " + reportId + " for report type: " + reportType);
                } else {
                    // If no matching report found, create a new one
                    reportId = UUID.randomUUID().toString();
                    String createReportQuery = "INSERT INTO reports (report_id, report_name, report_type, description, query_template, created_by, is_system) " +
                                               "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement insertStmt = DatabaseUtil.prepareStatement(createReportQuery)) {
                        insertStmt.setString(1, reportId);
                        insertStmt.setString(2, reportType);
                        insertStmt.setString(3, reportType.toUpperCase().replace(" ", "_"));
                        insertStmt.setString(4, "Generated report: " + reportType);
                        insertStmt.setString(5, "SELECT 1"); // Placeholder query
                        insertStmt.setString(6, currentLecturer.getUserId());
                        insertStmt.setBoolean(7, false); // Set is_system flag to false for lecturer-generated reports
                        insertStmt.executeUpdate();
                        LOGGER.info("Created new report with ID: " + reportId + " for report type: " + reportType);
                    }
                }
            }
            
            // Save the report execution
            if (reportId != null) {
                String executionId = UUID.randomUUID().toString();
                String saveExecutionQuery = "INSERT INTO report_executions (execution_id, report_id, executed_by, result_file_path) " +
                                           "VALUES (?, ?, ?, ?)";
                
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(saveExecutionQuery)) {
                    stmt.setString(1, executionId);
                    stmt.setString(2, reportId);
                    stmt.setString(3, currentLecturer.getUserId());
                    stmt.setString(4, reportFilePath);
                    stmt.executeUpdate();
                    LOGGER.info("Saved report execution with ID: " + executionId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error saving report execution to database", e);
        }
    }
    
    /**
     * Handles generating a report.
     */
    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeComboBox.getValue();
        if (reportType == null || reportType.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Report Type Selected", 
                     "Please select a report type to generate.");
            return;
        }
        
        try {
            String reportFilePath = generateReportFile(reportType);
            if (reportFilePath != null) {
                saveReportExecutionToDatabase(reportType, reportFilePath);
                loadProctoringViolations();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report Generated", 
                         "The report has been generated successfully.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Report Generation Failed", 
                     "Failed to generate report: " + e.getMessage());
        }
    }
    
    /**
     * Generates a student performance report.
     * 
     * @return HTML content for the report
     */
    private String generateStudentPerformanceReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Student Performance Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;} h1{color:#2196F3;} table{border-collapse:collapse;width:100%;} ");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background-color:#f2f2f2;}</style></head>");
        html.append("<body><h1>Student Performance Report</h1>");
        html.append("<p>Generated by: ").append(currentLecturer.getFullName()).append("</p>");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        
        try {
            // Get all students enrolled in units taught by this lecturer
            String query = "SELECT u.user_id, u.full_name, su.unit_code, un.unit_name " +
                          "FROM users u " +
                          "JOIN student_unit_enrollments su ON u.user_id = su.student_id " +
                          "JOIN units un ON su.unit_code = un.unit_code " +
                          "WHERE u.role = 'STUDENT' " +
                          "ORDER BY u.full_name";
            
            html.append("<h2>Student Enrollments</h2>");
            html.append("<table><tr><th>Student Name</th><th>Unit Code</th><th>Unit Name</th></tr>");
            
            try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    html.append("<tr>");
                    html.append("<td>").append(rs.getString("full_name")).append("</td>");
                    html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                    html.append("<td>").append(rs.getString("unit_name")).append("</td>");
                    html.append("</tr>");
                }
            }
            
            html.append("</table>");
            
            // Get assessment results
            String assessmentQuery = "SELECT u.full_name, a.title, sa.score, a.unit_code " +
                                    "FROM student_assessments sa " +
                                    "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                                    "JOIN users u ON sa.student_id = u.user_id " +
                                    "WHERE sa.status = 'COMPLETED' " +
                                    "ORDER BY u.full_name, a.title";
            
            html.append("<h2>Assessment Results</h2>");
            html.append("<table><tr><th>Student Name</th><th>Unit Code</th><th>Assessment</th><th>Score</th></tr>");
            
            try (ResultSet rs = DatabaseUtil.executeQuery(assessmentQuery)) {
                while (rs.next()) {
                    html.append("<tr>");
                    html.append("<td>").append(rs.getString("full_name")).append("</td>");
                    html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                    html.append("<td>").append(rs.getString("title")).append("</td>");
                    html.append("<td>").append(rs.getDouble("score")).append("</td>");
                    html.append("</tr>");
                }
            }
            
            html.append("</table>");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating student performance report", e);
            html.append("<p>Error generating report: ").append(e.getMessage()).append("</p>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Generates an assessment results report.
     * 
     * @return HTML content for the report
     */
    private String generateAssessmentResultsReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Assessment Results Summary</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;} h1{color:#2196F3;} h2{color:#0D47A1;margin-top:30px;} ");
        html.append("table{border-collapse:collapse;width:100%;margin-bottom:20px;} ");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background-color:#f2f2f2;} ");
        html.append(".proctoring-section{background-color:#f8f9fa;padding:15px;border-left:4px solid #ff9800;margin:20px 0;} ");
        html.append(".violation-high{background-color:#ffebee;} ");
        html.append(".violation-medium{background-color:#fff8e1;} ");
        html.append(".violation-low{background-color:#f1f8e9;} ");
        html.append("</style></head>");
        html.append("<body><h1>Assessment Results Summary</h1>");
        html.append("<p>Generated by: ").append(currentLecturer.getFullName()).append("</p>");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        
        try {
            // Add a dedicated section for proctoring data
            html.append("<div class='proctoring-section'>");
            html.append("<h2>Proctoring Violations Summary</h2>");
            html.append("<p>This section shows window focus violations detected during assessments. These occur when students switch away from the assessment window, " +
                       "which may indicate potential cheating attempts.</p>");
            
            String proctoringQuery = "SELECT a.assessment_id, a.title, a.unit_code, " +
                                    "COUNT(DISTINCT fv.student_id) as students_with_violations, " +
                                    "COUNT(fv.violation_id) as total_violations, " +
                                    "SUM(fv.duration_seconds) as total_duration " +
                                    "FROM assessments a " +
                                    "LEFT JOIN focus_violations fv ON a.assessment_id = fv.assessment_id " +
                                    "GROUP BY a.assessment_id, a.title, a.unit_code " +
                                    "HAVING total_violations > 0 " +
                                    "ORDER BY total_violations DESC";
            
            try (ResultSet rs = DatabaseUtil.executeQuery(proctoringQuery)) {
                boolean hasViolations = false;
                
                html.append("<table>");
                html.append("<tr><th>Assessment</th><th>Unit</th><th>Students with Violations</th><th>Total Violations</th><th>Total Time Out of Focus</th></tr>");
                
                while (rs.next()) {
                    hasViolations = true;
                    String assessmentId = rs.getString("assessment_id");
                    String title = rs.getString("title");
                    String unitCode = rs.getString("unit_code");
                    int studentsWithViolations = rs.getInt("students_with_violations");
                    int totalViolations = rs.getInt("total_violations");
                    long totalDuration = rs.getLong("total_duration");
                    
                    html.append("<tr>");
                    html.append("<td>").append(title).append("</td>");
                    html.append("<td>").append(unitCode).append("</td>");
                    html.append("<td>").append(studentsWithViolations).append("</td>");
                    html.append("<td>").append(totalViolations).append("</td>");
                    html.append("<td>").append(formatDuration(totalDuration)).append("</td>");
                    html.append("</tr>");
                }
                
                if (!hasViolations) {
                    html.append("<tr><td colspan='5'>No proctoring violations detected in any assessments.</td></tr>");
                }
                
                html.append("</table>");
            } catch (SQLException e) {
                html.append("<p>Error retrieving proctoring data: ").append(e.getMessage()).append("</p>");
                LOGGER.log(Level.WARNING, "Error retrieving proctoring data", e);
            }
            
            // Add detailed proctoring violations by student
            html.append("<h3>Detailed Proctoring Violations by Student</h3>");
            
            String detailedQuery = "SELECT u.user_id, u.full_name, a.title, a.unit_code, " +
                                  "COUNT(fv.violation_id) as violation_count, " +
                                  "SUM(fv.duration_seconds) as total_duration, " +
                                  "MAX(fv.duration_seconds) as max_duration " +
                                  "FROM focus_violations fv " +
                                  "JOIN users u ON fv.student_id = u.user_id " +
                                  "JOIN assessments a ON fv.assessment_id = a.assessment_id " +
                                  "GROUP BY u.user_id, u.full_name, a.title, a.unit_code " +
                                  "ORDER BY total_duration DESC";
            
            try (ResultSet rs = DatabaseUtil.executeQuery(detailedQuery)) {
                boolean hasDetailedViolations = false;
                
                html.append("<table>");
                html.append("<tr><th>Student</th><th>Assessment</th><th>Unit</th><th>Violations</th><th>Total Time</th><th>Longest Violation</th></tr>");
                
                while (rs.next()) {
                    hasDetailedViolations = true;
                    String studentName = rs.getString("full_name");
                    String title = rs.getString("title");
                    String unitCode = rs.getString("unit_code");
                    int violationCount = rs.getInt("violation_count");
                    long totalDuration = rs.getLong("total_duration");
                    long maxDuration = rs.getLong("max_duration");
                    
                    // Determine violation severity
                    String rowClass = "";
                    if (totalDuration > 300) { // More than 5 minutes
                        rowClass = "violation-high";
                    } else if (totalDuration > 60) { // More than 1 minute
                        rowClass = "violation-medium";
                    } else {
                        rowClass = "violation-low";
                    }
                    
                    html.append("<tr class='").append(rowClass).append("'>");
                    html.append("<td>").append(studentName).append("</td>");
                    html.append("<td>").append(title).append("</td>");
                    html.append("<td>").append(unitCode).append("</td>");
                    html.append("<td>").append(violationCount).append("</td>");
                    html.append("<td>").append(formatDuration(totalDuration)).append("</td>");
                    html.append("<td>").append(formatDuration(maxDuration)).append("</td>");
                    html.append("</tr>");
                }
                
                if (!hasDetailedViolations) {
                    html.append("<tr><td colspan='6'>No detailed proctoring violations found.</td></tr>");
                }
                
                html.append("</table>");
            } catch (SQLException e) {
                html.append("<p>Error retrieving detailed proctoring data: ").append(e.getMessage()).append("</p>");
                LOGGER.log(Level.WARNING, "Error retrieving detailed proctoring data", e);
            }
            
            html.append("</div>");
            
            // Get assessment statistics
            String query = "SELECT a.assessment_id, a.title, a.unit_code, COUNT(sa.student_assessment_id) as attempts, " +
                          "AVG(sa.score) as avg_score, MIN(sa.score) as min_score, MAX(sa.score) as max_score " +
                          "FROM assessments a " +
                          "LEFT JOIN student_assessments sa ON a.assessment_id = sa.assessment_id " +
                          "GROUP BY a.assessment_id, a.title, a.unit_code " +
                          "ORDER BY a.unit_code, a.title";
            
            html.append("<h2>Assessment Statistics</h2>");
            html.append("<table><tr><th>Unit Code</th><th>Assessment</th><th>Attempts</th><th>Avg Score</th><th>Min Score</th><th>Max Score</th></tr>");
            
            try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    html.append("<tr>");
                    html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                    html.append("<td>").append(rs.getString("title")).append("</td>");
                    html.append("<td>").append(rs.getInt("attempts")).append("</td>");
                    
                    double avgScore = rs.getDouble("avg_score");
                    if (rs.wasNull()) {
                        html.append("<td>N/A</td>");
                    } else {
                        html.append("<td>").append(String.format("%.2f", avgScore)).append("</td>");
                    }
                    
                    html.append("<td>").append(String.format("%.2f", rs.getDouble("min_score"))).append("</td>");
                    html.append("<td>").append(String.format("%.2f", rs.getDouble("max_score"))).append("</td>");
                    html.append("</tr>");
                }
            }
            
            html.append("</table>");
            
            // Add detailed student performance
            html.append("<h2>Detailed Student Performance</h2>");
            query = "SELECT sa.student_id, u.full_name, a.assessment_id, a.title, a.unit_code, " +
                   "sa.score, sa.total_possible, sa.start_time, sa.end_time " +
                   "FROM student_assessments sa " +
                   "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                   "JOIN users u ON sa.student_id = u.user_id " +
                   "WHERE sa.status = 'COMPLETED' " +
                   "ORDER BY a.unit_code, a.title, u.full_name";
            
            html.append("<table><tr><th>Unit</th><th>Assessment</th><th>Student</th><th>Score</th><th>Percentage</th><th>Time Spent</th></tr>");
            
            try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    int attempts = rs.getInt("attempts");
                    int correct = rs.getInt("correct_answers");
                    double percentage = (double) correct / attempts * 100;
                    
                    String studentId = rs.getString("student_id");
                    String assessmentId = rs.getString("assessment_id");
                    String studentName = rs.getString("full_name");
                    double score = rs.getDouble("score");
                    double totalPossible = rs.getDouble("total_possible");
                    double percentageScore = (totalPossible > 0) ? (score / totalPossible * 100) : 0;
                    
                    LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                    LocalDateTime endTime = rs.getTimestamp("end_time") != null ? 
                                          rs.getTimestamp("end_time").toLocalDateTime() : 
                                          LocalDateTime.now();
                    
                    long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
                    String timeSpent = formatDuration(seconds);
                    
                    html.append("<tr>");
                    html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                    html.append("<td>").append(rs.getString("title")).append("</td>");
                    html.append("<td>").append(studentName).append(" (").append(studentId).append(")").append("</td>");
                    html.append("<td>").append(String.format("%.1f / %.1f", score, totalPossible)).append("</td>");
                    html.append("<td>").append(String.format("%.1f%%", percentageScore)).append("</td>");
                    html.append("<td>").append(timeSpent).append("</td>");
                    html.append("</tr>");
                }
            }
            
            html.append("</table>");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating assessment results report", e);
            html.append("<p>Error generating report: ").append(e.getMessage()).append("</p>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Generates a unit progress report.
     * 
     * @return The HTML content of the report
     */
    private String generateUnitProgressReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Unit Progress Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;} h1{color:#2196F3;} table{border-collapse:collapse;width:100%;} ");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background-color:#f2f2f2;}</style></head>");
        html.append("<body><h1>Unit Progress Report</h1>");
        html.append("<p>Generated by: ").append(currentLecturer.getFullName()).append("</p>");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        
        try {
            // Get unit statistics with a query that includes student_unit_enrollments
            String query = "SELECT u.unit_code, u.unit_name, COUNT(DISTINCT sue.student_id) as enrolled_students, " +
                          "COUNT(DISTINCT a.assessment_id) as assessments, COUNT(DISTINCT t.topic_id) as topics " +
                          "FROM units u " +
                          "LEFT JOIN student_unit_enrollments sue ON u.unit_code = sue.unit_code " +
                          "LEFT JOIN assessments a ON u.unit_code = a.unit_code " +
                          "LEFT JOIN topics t ON u.unit_code = t.unit_code " +
                          "GROUP BY u.unit_code, u.unit_name " +
                          "ORDER BY u.unit_code";
            
            html.append("<h2>Unit Statistics</h2>");
            html.append("<table><tr><th>Unit Code</th><th>Unit Name</th><th>Enrolled Students</th><th>Assessments</th><th>Topics</th></tr>");
            
            try {
                ResultSet rs = DatabaseUtil.executeQuery(query);
                boolean hasData = false;
                
                while (rs.next()) {
                    hasData = true;
                    html.append("<tr>");
                    html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                    html.append("<td>").append(rs.getString("unit_name")).append("</td>");
                    html.append("<td>").append(rs.getInt("enrolled_students")).append("</td>");
                    html.append("<td>").append(rs.getInt("assessments")).append("</td>");
                    html.append("<td>").append(rs.getInt("topics")).append("</td>");
                    html.append("</tr>");
                }
                
                // If no data was found, try a fallback query without student_unit_enrollments
                if (!hasData) {
                    LOGGER.info("No data found with student_unit_enrollments, trying fallback query");
                    tryFallbackUnitQuery(html);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error executing primary unit query: " + e.getMessage());
                // Try fallback query if the primary query fails
                tryFallbackUnitQuery(html);
            }
            
            html.append("</table>");
            
            // Add assessment statistics section
            addAssessmentStatistics(html);
            
            // Add topic coverage section
            addTopicCoverage(html);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating unit progress report", e);
            html.append("<p>Error generating report: ").append(e.getMessage()).append("</p>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Tries a fallback query for unit statistics that doesn't rely on student_unit_enrollments
     * 
     * @param html The HTML StringBuilder to append results to
     */
    private void tryFallbackUnitQuery(StringBuilder html) {
        String fallbackQuery = "SELECT u.unit_code, u.unit_name, " +
                              "0 as enrolled_students, " + // Default to 0 for enrolled students
                              "COUNT(DISTINCT a.assessment_id) as assessments, " +
                              "COUNT(DISTINCT t.topic_id) as topics " +
                              "FROM units u " +
                              "LEFT JOIN assessments a ON u.unit_code = a.unit_code " +
                              "LEFT JOIN topics t ON u.unit_code = t.unit_code " +
                              "GROUP BY u.unit_code, u.unit_name " +
                              "ORDER BY u.unit_code";
        
        try (ResultSet rs = DatabaseUtil.executeQuery(fallbackQuery)) {
            while (rs.next()) {
                html.append("<tr>");
                html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                html.append("<td>").append(rs.getString("unit_name")).append("</td>");
                html.append("<td>").append(rs.getInt("enrolled_students")).append(" (estimated)</td>");
                html.append("<td>").append(rs.getInt("assessments")).append("</td>");
                html.append("<td>").append(rs.getInt("topics")).append("</td>");
                html.append("</tr>");
            }
            LOGGER.info("Fallback unit query executed successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing fallback unit query", e);
            html.append("<tr><td colspan='5'>Error retrieving unit statistics: ").append(e.getMessage()).append("</td></tr>");
        }
    }
    
    /**
     * Adds assessment statistics to the unit progress report
     * 
     * @param html The HTML StringBuilder to append results to
     */
    private void addAssessmentStatistics(StringBuilder html) {
        html.append("<h2>Assessment Statistics</h2>");
        html.append("<table><tr><th>Unit Code</th><th>Assessment Title</th><th>Type</th><th>Submissions</th><th>Average Score</th></tr>");
        
        String query = "SELECT u.unit_code, a.title, a.unit_code, " +
                      "COUNT(sa.student_assessment_id) as submissions, " +
                      "AVG(sa.score) as avg_score " +
                      "FROM assessments a " +
                      "LEFT JOIN student_assessments sa ON a.assessment_id = sa.assessment_id " +
                      "JOIN units u ON a.unit_code = u.unit_code " +
                      "GROUP BY u.unit_code, a.assessment_id, a.title " +
                      "ORDER BY u.unit_code, a.title";
        
        try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
            while (rs.next()) {
                html.append("<tr>");
                html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                html.append("<td>").append(rs.getString("title")).append("</td>");
                html.append("<td>").append(rs.getString("type")).append("</td>");
                html.append("<td>").append(rs.getInt("submissions")).append("</td>");
                
                double avgScore = rs.getDouble("avg_score");
                if (rs.wasNull()) {
                    html.append("<td>N/A</td>");
                } else {
                    html.append("<td>").append(String.format("%.2f", avgScore)).append("</td>");
                }
                
                html.append("</tr>");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error retrieving assessment statistics", e);
            html.append("<tr><td colspan='5'>Error retrieving assessment statistics: ").append(e.getMessage()).append("</td></tr>");
        }
        
        html.append("</table>");
    }
    
    /**
     * Adds topic coverage statistics to the unit progress report
     * 
     * @param html The HTML StringBuilder to append results to
     */
    private void addTopicCoverage(StringBuilder html) {
        html.append("<h2>Topic Coverage</h2>");
        html.append("<table><tr><th>Unit Code</th><th>Topic</th><th>Questions</th><th>Assessments</th></tr>");
        
        String query = "SELECT u.unit_code, t.topic_name, COUNT(DISTINCT q.question_id) as questions, " +
                      "COUNT(DISTINCT aq.assessment_id) as assessments " +
                      "FROM units u " +
                      "JOIN topics t ON u.unit_code = t.unit_code " +
                      "LEFT JOIN questions q ON t.topic_name = q.topic AND u.unit_code = q.unit_code " +
                      "LEFT JOIN assessment_questions aq ON q.question_id = aq.question_id " +
                      "GROUP BY u.unit_code, t.topic_name " +
                      "ORDER BY u.unit_code, t.topic_name";
        
        try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
            while (rs.next()) {
                html.append("<tr>");
                html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                html.append("<td>").append(rs.getString("topic_name")).append("</td>");
                html.append("<td>").append(rs.getInt("questions")).append("</td>");
                html.append("<td>").append(rs.getInt("assessments")).append("</td>");
                html.append("</tr>");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error retrieving topic coverage", e);
            html.append("<tr><td colspan='4'>Error retrieving topic coverage: ").append(e.getMessage()).append("</td></tr>");
        }
        
        html.append("</table>");
    }
    
    /**
     * Gets units assigned to a lecturer.
     * 
     * @param lecturerId The lecturer ID
     * @return List of units assigned to the lecturer
     * @throws SQLException If a database error occurs
     */
    private List<Unit> getUnitsByLecturerId(String lecturerId) throws SQLException {
        List<Unit> units = new ArrayList<>();
        
        // Check if lecturers table exists
        String checkLecturersTableQuery = "SHOW TABLES LIKE 'lecturers'";
        try (PreparedStatement checkStmt = DatabaseUtil.prepareStatement(checkLecturersTableQuery)) {
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next()) {
                // Lecturers table doesn't exist, create it
                LOGGER.info("Creating lecturers table");
                String createTableQuery = "CREATE TABLE lecturers (" +
                        "staff_id VARCHAR(20) PRIMARY KEY, " +
                        "user_id VARCHAR(36) NOT NULL, " +
                        "department VARCHAR(50), " +
                        "office VARCHAR(50), " +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                        ")";
                try (PreparedStatement createStmt = DatabaseUtil.prepareStatement(createTableQuery)) {
                    createStmt.executeUpdate();
                }
            }
        }
        
        // Check if units table exists
        String checkUnitsTableQuery = "SHOW TABLES LIKE 'units'";
        try (PreparedStatement checkStmt = DatabaseUtil.prepareStatement(checkUnitsTableQuery)) {
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next()) {
                // Table doesn't exist, create it
                LOGGER.info("Creating units table");
                String createTableQuery = "CREATE TABLE units (" +
                        "unit_code VARCHAR(20) PRIMARY KEY, " +
                        "unit_name VARCHAR(100) NOT NULL, " +
                        "department VARCHAR(50) NOT NULL, " +
                        "credit_hours INT NOT NULL, " +
                        "description TEXT, " +
                        "lecturer_id VARCHAR(36) NOT NULL" +
                        ")";
                try (PreparedStatement createStmt = DatabaseUtil.prepareStatement(createTableQuery)) {
                    createStmt.executeUpdate();
                }
            }
        }
        
        // Get the lecturer's staff ID from the lecturers table or create it if not exists
        String staffId = null;
        String getStaffIdQuery = "SELECT staff_id FROM lecturers WHERE user_id = ?";
        try (PreparedStatement staffStmt = DatabaseUtil.prepareStatement(getStaffIdQuery)) {
            staffStmt.setString(1, lecturerId);
            ResultSet staffRs = staffStmt.executeQuery();
            if (staffRs.next()) {
                staffId = staffRs.getString("staff_id");
                LOGGER.info("Found staff ID: " + staffId + " for lecturer ID: " + lecturerId);
            } else {
                // If no staff ID found, create one
                staffId = "STAFF" + lecturerId.substring(3); // Convert from LEC#### to STAFF####
                LOGGER.info("Creating new staff ID: " + staffId + " for lecturer ID: " + lecturerId);
                String insertStaffQuery = "INSERT INTO lecturers (staff_id, user_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = DatabaseUtil.prepareStatement(insertStaffQuery)) {
                    insertStmt.setString(1, staffId);
                    insertStmt.setString(2, lecturerId);
                    insertStmt.executeUpdate();
                }
            }
        }
        
        // Check if there are any units assigned to this lecturer
        String countUnitsQuery = "SELECT COUNT(*) FROM units WHERE lecturer_id = ?";
        int unitCount = 0;
        try (PreparedStatement countStmt = DatabaseUtil.prepareStatement(countUnitsQuery)) {
            countStmt.setString(1, staffId);
            ResultSet countRs = countStmt.executeQuery();
            if (countRs.next()) {
                unitCount = countRs.getInt(1);
            }
        }
        
        // If no units found for this lecturer, add sample units
        if (unitCount == 0 && staffId != null) {
            LOGGER.info("No units found for lecturer " + lecturerId + " with staff ID " + staffId + ", adding sample units");
            
            // Add some sample units for the current lecturer
            String insertUnitQuery = "INSERT INTO units (unit_code, unit_name, department, credit_hours, description, lecturer_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = DatabaseUtil.prepareStatement(insertUnitQuery)) {
                // Unit 1
                insertStmt.setString(1, "CS101");
                insertStmt.setString(2, "Introduction to Computer Science");
                insertStmt.setString(3, "Computer Science");
                insertStmt.setInt(4, 3);
                insertStmt.setString(5, "An introductory course to computer science principles");
                insertStmt.setString(6, staffId);
                insertStmt.executeUpdate();
                
                // Unit 2
                insertStmt.setString(1, "CS201");
                insertStmt.setString(2, "Data Structures and Algorithms");
                insertStmt.setString(3, "Computer Science");
                insertStmt.setInt(4, 4);
                insertStmt.setString(5, "A comprehensive study of data structures and algorithms");
                insertStmt.setString(6, staffId);
                insertStmt.executeUpdate();
                
                // Unit 3
                insertStmt.setString(1, "CS301");
                insertStmt.setString(2, "Database Systems");
                insertStmt.setString(3, "Information Technology");
                insertStmt.setInt(4, 3);
                insertStmt.setString(5, "Design and implementation of database systems");
                insertStmt.setString(6, staffId);
                insertStmt.executeUpdate();
            }
        }
        
        // Query to get units assigned to the lecturer
        if (staffId != null) {
            String query = "SELECT * FROM units WHERE lecturer_id = ?";
            LOGGER.info("Querying units with lecturer_id = " + staffId);
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, staffId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Unit unit = new Unit();
                    unit.setUnitCode(rs.getString("unit_code"));
                    unit.setUnitName(rs.getString("unit_name"));
                    unit.setDepartment(rs.getString("department"));
                    unit.setCreditHours(rs.getInt("credit_hours"));
                    unit.setDescription(rs.getString("description"));
                    unit.setLecturerId(rs.getString("lecturer_id"));
                    units.add(unit);
                    LOGGER.info("Added unit: " + unit.getUnitCode() + " - " + unit.getUnitName());
                }
            }
        } else {
            LOGGER.warning("No staff ID found for lecturer with user ID: " + lecturerId);
        }
        
        return units;
    }
    
    /**
     * Gets the enrollment count for a unit.
     * 
     * @param unitCode The unit code
     * @return The enrollment count
     * @throws SQLException If a database error occurs
     */
    private int getEnrollmentCountForUnit(String unitCode) throws SQLException {
        int enrollmentCount = 0;
        String query = "SELECT COUNT(*) FROM enrollments WHERE unit_code = ?";
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                enrollmentCount = rs.getInt(1);
            }
        }
        return enrollmentCount;
    }
    
    /**
     * Gets the formal assessment count for a unit.
     * 
     * @param unitCode The unit code
     * @return The formal assessment count
     * @throws SQLException If a database error occurs
     */
    private int getFormalAssessmentCountForUnit(String unitCode) throws SQLException {
        int formalAssessmentCount = 0;
        String query = "SELECT COUNT(*) FROM assessments WHERE unit_code = ? AND is_practice = 0";
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                formalAssessmentCount = rs.getInt(1);
            }
        }
        return formalAssessmentCount;
    }
    
    /**
     * Gets the practice assessment count for a unit.
     * 
     * @param unitCode The unit code
     * @return The practice assessment count
     * @throws SQLException If a database error occurs
     */
    private int getPracticeAssessmentCountForUnit(String unitCode) throws SQLException {
        int practiceAssessmentCount = 0;
        String query = "SELECT COUNT(*) FROM assessments WHERE unit_code = ? AND is_practice = 1";
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                practiceAssessmentCount = rs.getInt(1);
            }
        }
        return practiceAssessmentCount;
    }
    
    /**
     * Gets the average completion rate for a unit.
     * 
     * @param unitCode The unit code
     * @param enrollmentCount The enrollment count
     * @param formalAssessmentCount The formal assessment count
     * @return The average completion rate
     * @throws SQLException If a database error occurs
     */
    private double getAverageCompletionRateForUnit(String unitCode, int enrollmentCount, int formalAssessmentCount) throws SQLException {
        double averageCompletionRate = 0.0;
        String query = "SELECT AVG((sa.attempt_id / (? * ?)) * 100) as avg_completion_rate " +
                      "FROM student_assessments sa " +
                      "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                      "WHERE a.unit_code = ? AND sa.status = 'SUBMITTED' AND a.is_practice = 0";
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setInt(1, enrollmentCount);
            stmt.setInt(2, formalAssessmentCount);
            stmt.setString(3, unitCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                averageCompletionRate = rs.getDouble("avg_completion_rate");
                if (rs.wasNull()) {
                    averageCompletionRate = 0.0;
                }
            }
        }
        return averageCompletionRate;
    }
    
    /**
     * Generates a question analysis report.
     * 
     * @return HTML content for the report
     */
    private String generateQuestionAnalysisReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Question Analysis Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;} h1{color:#2196F3;} table{border-collapse:collapse;width:100%;} ");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background-color:#f2f2f2;}</style></head>");
        html.append("<body><h1>Question Analysis Report</h1>");
        html.append("<p>Generated by: ").append(currentLecturer.getFullName()).append("</p>");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        
        try {
            // Get question statistics
            String query = "SELECT q.question_id, q.question_text, q.topic, q.difficulty, q.unit_code, " +
                          "COUNT(sa.answer_id) as attempts, " +
                          "SUM(CASE WHEN sa.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers " +
                          "FROM questions q " +
                          "LEFT JOIN student_answers sa ON q.question_id = sa.question_id " +
                          "GROUP BY q.question_id, q.question_text, q.topic, q.difficulty, q.unit_code " +
                          "ORDER BY q.unit_code, q.topic, q.difficulty";
            
            html.append("<h2>Question Statistics</h2>");
            html.append("<table><tr><th>Unit Code</th><th>Topic</th><th>Difficulty</th><th>Question</th><th>Attempts</th><th>Correct</th><th>Success Rate</th></tr>");
            
            try (ResultSet rs = DatabaseUtil.executeQuery(query)) {
                while (rs.next()) {
                    int attempts = rs.getInt("attempts");
                    int correct = rs.getInt("correct_answers");
                    double successRate = attempts > 0 ? (double) correct / attempts * 100 : 0;
                    
                    html.append("<tr>");
                    html.append("<td>").append(rs.getString("unit_code")).append("</td>");
                    html.append("<td>").append(rs.getString("topic")).append("</td>");
                    html.append("<td>").append(rs.getString("difficulty")).append("</td>");
                    html.append("<td>").append(rs.getString("question_text")).append("</td>");
                    html.append("<td>").append(attempts).append("</td>");
                    html.append("<td>").append(correct).append("</td>");
                    html.append("<td>").append(String.format("%.2f%%", successRate)).append("</td>");
                    html.append("</tr>");
                }
            }
            
            html.append("</table>");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating question analysis report", e);
            html.append("<p>Error generating report: ").append(e.getMessage()).append("</p>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Generates an attendance report.
     * 
     * @return HTML content for the report
     */
    private String generateAttendanceReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Attendance Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;} h1{color:#2196F3;} table{border-collapse:collapse;width:100%;} ");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background-color:#f2f2f2;}</style></head>");
        html.append("<body><h1>Attendance Report</h1>");
        html.append("<p>Generated by: ").append(currentLecturer.getFullName()).append("</p>");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        
        html.append("<h2>Student Attendance</h2>");
        html.append("<p>This report provides an overview of student attendance. Attendance data is not currently tracked in the system.</p>");
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Formats a duration in seconds to a human-readable string.
     * 
     * @param seconds The duration in seconds
     * @return A formatted string (e.g. "2m 30s")
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        
        if (minutes < 60) {
            return minutes + "m " + remainingSeconds + "s";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        return hours + "h " + remainingMinutes + "m " + remainingSeconds + "s";
    }
    
    /**
     * Shows the settings view.
     */
    @FXML
    private void showSettings() {
        dashboardContent.setVisible(false);
        assessmentsContent.setVisible(false);
        analyticsContent.setVisible(false);
        reportsContent.setVisible(false);
        settingsContent.setVisible(true);
        
        resetNavigationIcons();
        settingsIcon.setIconColor(Color.valueOf("#2196F3"));
        
        // Load current lecturer settings
        nameField.setText(currentLecturer.getFullName());
        emailField.setText(currentLecturer.getEmail());
        
        LOGGER.info("Showing settings view");
    }
    
    /**
     * Handles saving user settings.
     */
    @FXML
    private void handleSaveSettings() {
        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Missing Information", 
                    "Please fill in all required fields.");
            return;
        }
        
        // Check if changing password
        if (!currentPassword.isEmpty()) {
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Missing Password", 
                        "Please fill in all password fields to change your password.");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Password Mismatch", 
                        "New password and confirmation do not match.");
                return;
            }
            
            // Validate current password
            try {
                boolean passwordValid = DatabaseUtil.validatePassword(currentLecturer.getUserId(), currentPassword);
                if (!passwordValid) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid Password", 
                            "The current password you entered is incorrect.");
                    return;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error validating password", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Database Error", 
                        "An error occurred while validating your password: " + e.getMessage());
                return;
            }
        }
        
        try {
            // Update user information in the database
            String updateQuery = "UPDATE users SET full_name = ?, email = ? WHERE user_id = ?";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(updateQuery)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, currentLecturer.getUserId());
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update password if requested
                    if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                        String updatePasswordQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";
                        try (PreparedStatement pstmt = DatabaseUtil.prepareStatement(updatePasswordQuery)) {
                            // In a real application, you would hash the password
                            pstmt.setString(1, newPassword);
                            pstmt.setString(2, currentLecturer.getUserId());
                            pstmt.executeUpdate();
                        }
                    }
                    
                    // Update current lecturer object
                    currentLecturer.setFullName(name);
                    currentLecturer.setEmail(email);
                    
                    // Update UI
                    welcomeText.setText("Welcome, " + name);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Settings Updated", 
                            "Your profile information has been updated successfully.");
                    
                    // Clear password fields
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", 
                            "Failed to update your profile information.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user settings", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", 
                    "An error occurred while updating your settings: " + e.getMessage());
        }
    }
    
    /**
     * Adds a report description to the container.
     * 
     * @param container The container to add the description to
     * @param title The report title
     * @param description The report description
     */
    private void addReportDescription(VBox container, String title, String description) {
        VBox reportBox = new VBox(5);
        reportBox.setPadding(new Insets(10));
        reportBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-background-radius: 5;");
        
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Text descText = new Text(description);
        descText.setStyle("-fx-font-size: 12px;");
        descText.setWrappingWidth(600);
        
        reportBox.getChildren().addAll(titleText, descText);
        container.getChildren().add(reportBox);
    }

}
