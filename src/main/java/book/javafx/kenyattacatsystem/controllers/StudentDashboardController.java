package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.*;
import book.javafx.kenyattacatsystem.services.*;
import book.javafx.kenyattacatsystem.utils.ChartUtil;
import book.javafx.kenyattacatsystem.controllers.AssessmentTakingController;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Controller for the student dashboard view.
 * Handles navigation between different sections and implements student-specific functionality.
 */
public class StudentDashboardController implements javafx.fxml.Initializable {
    private static final Logger LOGGER = Logger.getLogger(StudentDashboardController.class.getName());
    
    // Navigation Icons
    @FXML private FontIcon homeIcon;
    @FXML private FontIcon assessmentIcon;
    @FXML private FontIcon practiceIcon;
    @FXML private FontIcon contributionIcon;
    @FXML private FontIcon analyticsIcon;
    @FXML private FontIcon settingsIcon;
    @FXML private FontIcon logoutIcon;
    
    // Content Panes
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardContent;
    @FXML private VBox assessmentsContent;
    @FXML private VBox practiceContent;
    @FXML private VBox contributionsContent;
    @FXML private VBox analyticsContent;
    @FXML private VBox settingsContent;
    
    // Dashboard Elements
    @FXML private Text welcomeText;
    @FXML private Label dateTimeLabel;
    @FXML private GridPane unitsGrid;
    @FXML private Label statusLabel;
    @FXML private Label versionLabel;
    @FXML private Button enrollUnitButton;
    
    // Assessments Elements
    @FXML private ComboBox<String> assessmentUnitFilter;
    @FXML private ComboBox<String> assessmentStatusFilter;
    @FXML private TableView<Assessment> assessmentsTable;
    @FXML private TableColumn<Assessment, String> assessmentTitleColumn;
    @FXML private TableColumn<Assessment, String> assessmentUnitColumn;
    @FXML private TableColumn<Assessment, String> assessmentStartColumn;
    @FXML private TableColumn<Assessment, String> assessmentEndColumn;
    @FXML private TableColumn<Assessment, String> assessmentDurationColumn;
    @FXML private TableColumn<Assessment, String> assessmentStatusColumn;
    @FXML private Button viewAssessmentButton;
    @FXML private Button takeAssessmentButton;
    @FXML private Button downloadAssessmentButton;
    
    // Practice assessment UI elements
    @FXML private ComboBox<String> practiceUnitFilter;
    @FXML private ComboBox<String> practiceStatusFilter;
    @FXML private TableView<Assessment> practiceTable;
    @FXML private TableColumn<Assessment, String> practiceTitleColumn;
    @FXML private TableColumn<Assessment, String> practiceUnitColumn;
    @FXML private TableColumn<Assessment, String> practiceScoreColumn;
    @FXML private TableColumn<Assessment, String> practiceGradeColumn;
    @FXML private TableColumn<Assessment, String> practiceStatusColumn;
    @FXML private Button viewPracticeButton;
    @FXML private Button takePracticeButton;
    
    // Practice test creation form elements
    @FXML private TextField practiceTitleField;
    @FXML private ComboBox<Unit> practiceUnitComboBox;
    @FXML private ComboBox<String> practiceDifficultyComboBox;
    @FXML private ComboBox<String> practiceQuestionTypeComboBox;
    @FXML private Spinner<Integer> practiceQuestionCountSpinner;
    @FXML private Spinner<Integer> practiceDurationSpinner;
    @FXML private Button createPracticeButton;
    
    // Topic selection components
    @FXML private StackPane topicSelectionContainer;
    private CheckComboBox<Topic> topicCheckComboBox;
    
    // Contributions form elements
    @FXML private ComboBox<Unit> contributionUnitComboBox;
    @FXML private ComboBox<String> contributionQuestionTypeComboBox;
    @FXML private ComboBox<String> contributionDifficultyComboBox;
    @FXML private ComboBox<Topic> contributionTopicComboBox;
    @FXML private TextField contributionTopicField;
    @FXML private TextArea contributionQuestionTextField;
    @FXML private Spinner<Integer> contributionMarksSpinner;
    @FXML private Label optionsLabel;
    @FXML private VBox contributionOptionsVBox;
    @FXML private VBox optionsContainer;
    @FXML private Button addOptionButton;
    @FXML private Label correctAnswerLabel;
    @FXML private VBox contributionAnswersVBox;
    @FXML private TextField contributionAnswerField;
    @FXML private Button addAnswerButton;
    @FXML private ListView<String> contributionAnswersList;
    @FXML private Button clearContributionButton;
    @FXML private Button submitQuestionButton;
    
    // Settings Elements
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveSettingsButton;
    
    // Analytics section FXML components
    @FXML private StackPane performanceTrendChartContainer;
    @FXML private Label averageScoreLabel;
    @FXML private Label assessmentsCompletedLabel;
    @FXML private Label practiceTestsCompletedLabel;
    
    @FXML private TableView<Map<String, Object>> formalTestsTable;
    @FXML private TableColumn<Map<String, Object>, String> formalTestUnitColumn;
    @FXML private TableColumn<Map<String, Object>, String> formalTestTitleColumn;
    @FXML private TableColumn<Map<String, Object>, String> formalTestScoreColumn;
    @FXML private TableColumn<Map<String, Object>, String> formalTestDateColumn;
    @FXML private Label formalTestsAvgScoreLabel;
    @FXML private Label formalTestsHighestScoreLabel;
    @FXML private Label formalTestsCompletedLabel;
    
    @FXML private TableView<Map<String, Object>> practiceTestsAnalyticsTable;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestUnitColumn;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestTitleColumn;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestScoreColumn;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestDateColumn;
    @FXML private Label practiceTestsAvgScoreLabel;
    @FXML private Label practiceTestsHighestScoreLabel;
    @FXML private Label practiceTestsCompletedLabel2;
    
    // Services
    private UnitService unitService;
    private UserService userService;
    private AssessmentService assessmentService;
    private QuestionService questionService;
    private TopicService topicService;
    private PerformanceAnalyticsService performanceAnalyticsService;
    private StudentAssessmentService studentAssessmentService;
    
    // Current student data
    private Student currentStudent;
    private List<Unit> enrolledUnits;
    private ObservableList<Assessment> assessments;
    private Assessment selectedAssessment;
    
    // For MCQ options
    private ToggleGroup optionsToggleGroup;
    private List<TextField> optionFields;
    private List<RadioButton> optionRadioButtons;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initializing StudentDashboardController");
        
        // Initialize services
        unitService = new UnitService();
        userService = new UserService();
        assessmentService = new AssessmentService();
        questionService = new QuestionService();
        topicService = new TopicService();
        performanceAnalyticsService = new PerformanceAnalyticsService();
        studentAssessmentService = new StudentAssessmentService();
        
        // Initialize observable lists
        assessments = FXCollections.observableArrayList();
        enrolledUnits = FXCollections.observableArrayList();
        
        // Set up date and time display
        setupDateTimeDisplay();
        
        // Set up the assessments table
        setupAssessmentsTable();
        setupAssessmentFilters();
        setupAssessmentButtons();
        
        // Set up the practice test table and form
        setupPracticeTestTable();
        setupPracticeTestForm();
        setupPracticeFilters();
        setupPracticeButtons();
        
        // Set up the contributions form
        setupContributionsForm();
        
        // Set up enroll unit button
        if (enrollUnitButton != null) {
            enrollUnitButton.setOnAction(event -> openEnrollUnitDialog());
        }
        
        // Set version label
        versionLabel.setText("Version 1.0");
        
        // Set initial status
        statusLabel.setText("Ready");
        
        // Set up logout button
        logoutIcon.setOnMouseClicked(event -> handleLogout(event));
    }
    
    /**
     * Sets the current student and loads their data.
     * 
     * @param student The current student
     */
    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        
        // Set welcome text
        welcomeText.setText("Welcome, " + student.getFullName());
        
        // Load enrolled units
        loadEnrolledUnits();
        
        // Load assessments
        loadAssessments();
        
        // Load practice tests
        loadPracticeTests();
        
        // Populate the settings form
        populateSettingsForm();
        
        // Update unit filters
        updateUnitFilter();
        
        // Update the contribution unit combobox
        updateContributionUnitComboBox();
        
        LOGGER.log(Level.INFO, "Student data loaded for {0}", student.getUsername());
    }
    
    /**
     * Loads the units the student is enrolled in.
     */
    private void loadEnrolledUnits() {
        if (currentStudent == null) {
            LOGGER.warning("Cannot load enrolled units: current student is null");
            return;
        }
        
        try {
            // Get the student's enrolled unit codes
            List<String> enrolledUnitCodes = currentStudent.getEnrolledUnits();
            enrolledUnits.clear();
            
            // Get the full unit details for each enrolled unit code
            for (String unitCode : enrolledUnitCodes) {
                Unit unit = UnitService.getUnitByCode(unitCode);
                if (unit != null) {
                    enrolledUnits.add(unit);
                }
            }
            
            LOGGER.info("Loaded " + enrolledUnits.size() + " enrolled units");
            
            // Update the practice test unit dropdown
            if (practiceUnitComboBox != null) {
                practiceUnitComboBox.setItems(FXCollections.observableArrayList(enrolledUnits));
            }
            
            // Populate the units grid
            populateUnitsGrid();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading enrolled units", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load enrolled units", e.getMessage());
        }
    }
    
    /**
     * Populates the units grid with cards for each enrolled unit.
     */
    private void populateUnitsGrid() {
        unitsGrid.getChildren().clear();
        
        int column = 0;
        int row = 0;
        
        for (Unit unit : enrolledUnits) {
            VBox unitCard = createUnitCard(unit);
            unitsGrid.add(unitCard, column, row);
            
            column++;
            if (column > 2) { // 3 cards per row
                column = 0;
                row++;
            }
        }
        
        if (enrolledUnits.isEmpty()) {
            Label emptyLabel = new Label("You are not enrolled in any units yet.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
            unitsGrid.add(emptyLabel, 0, 0);
        }
    }
    
    /**
     * Creates a card for a unit to display in the units grid.
     *
     * @param unit The unit to create a card for
     * @return A VBox containing the unit card
     */
    private VBox createUnitCard(Unit unit) {
        VBox card = new VBox();
        card.getStyleClass().add("unit-card");
        
        // Create the header with unit code and name
        HBox header = new HBox(10); // Changed from VBox to HBox with spacing
        header.getStyleClass().addAll("unit-card-header", getColorClass(unit.getUnitCode()));
        header.setAlignment(Pos.CENTER_LEFT); // Align content to the left
        
        // Create a VBox for unit code and name to stack them if needed
        VBox codeAndName = new VBox(5);
        
        Text titleText = new Text(unit.getUnitCode());
        titleText.getStyleClass().add("unit-card-title");
        
        Text subtitleText = new Text(unit.getUnitName());
        subtitleText.getStyleClass().add("unit-card-subtitle");
        subtitleText.setWrappingWidth(200);
        
        codeAndName.getChildren().addAll(titleText, subtitleText);
        header.getChildren().add(codeAndName);
        
        // Create the footer with lecturer information
        VBox footer = new VBox();
        footer.getStyleClass().add("unit-card-footer");
        
        // Get the full lecturer name using UserService if lecturerId is available
        String lecturerName = "Not assigned";
        if (unit.getLecturerId() != null && !unit.getLecturerId().isEmpty()) {
            Lecturer lecturer = UserService.getLecturerById(unit.getLecturerId());
            if (lecturer != null && lecturer.getFullName() != null) {
                lecturerName = lecturer.getFullName();
            }
        }
        
        Text lecturerText = new Text("Lecturer: " + lecturerName);
        lecturerText.getStyleClass().add("unit-card-lecturer");
        
        footer.getChildren().add(lecturerText);
        
        // Add header and footer to the card
        card.getChildren().addAll(header, footer);
        
        // Add click event to open unit details
        card.setOnMouseClicked(event -> openUnitDetails(unit));
        
        return card;
    }
    
    /**
     * Gets a color class for a unit card based on the unit code.
     * 
     * @param unitCode The unit code
     * @return A CSS class for the card color
     */
    private String getColorClass(String unitCode) {
        // Generate a consistent color based on the unit code
        int colorIndex = Math.abs(unitCode.hashCode() % 6) + 1;
        return "color-" + colorIndex;
    }
    
    /**
     * Opens the unit details view for a specific unit.
     *
     * @param unit The unit to show details for
     */
    private void openUnitDetails(Unit unit) {
        statusLabel.setText("Viewing details for " + unit.getUnitCode());
        // TODO: Implement unit details view
    }
    
    /**
     * Opens the dialog to enroll in new units.
     */
    private void openEnrollUnitDialog() {
        // Create a dialog
        Dialog<Unit> dialog = new Dialog<>();
        dialog.setTitle("Enroll in a Unit");
        dialog.setHeaderText("Select a unit to enroll in");
        
        // Set the button types
        ButtonType enrollButtonType = new ButtonType("Enroll", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(enrollButtonType, ButtonType.CANCEL);
        
        // Create the enrollment form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Get all available units
        List<Unit> availableUnits = UnitService.getAllUnits();
        
        // Filter out already enrolled units
        if (currentStudent != null && currentStudent.getEnrolledUnits() != null) {
            List<String> enrolledUnitCodes = currentStudent.getEnrolledUnits();
            availableUnits = availableUnits.stream()
                    .filter(unit -> !enrolledUnitCodes.contains(unit.getUnitCode()))
                    .collect(Collectors.toList());
        }
        
        // Create a combo box with available units
        ComboBox<Unit> unitComboBox = new ComboBox<>();
        unitComboBox.setPromptText("Select a unit");
        unitComboBox.getItems().addAll(availableUnits);
        unitComboBox.setCellFactory(param -> new ListCell<Unit>() {
            @Override
            protected void updateItem(Unit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUnitCode() + " - " + item.getUnitName());
                }
            }
        });
        unitComboBox.setButtonCell(new ListCell<Unit>() {
            @Override
            protected void updateItem(Unit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUnitCode() + " - " + item.getUnitName());
                }
            }
        });
        
        grid.add(new Label("Available Units:"), 0, 0);
        grid.add(unitComboBox, 1, 0);
        
        // Add unit details area
        TextArea unitDetailsArea = new TextArea();
        unitDetailsArea.setEditable(false);
        unitDetailsArea.setPrefHeight(100);
        unitDetailsArea.setWrapText(true);
        unitDetailsArea.setPromptText("Select a unit to see details");
        
        grid.add(new Label("Unit Details:"), 0, 1);
        grid.add(unitDetailsArea, 1, 1);
        
        // Update unit details when a unit is selected
        unitComboBox.setOnAction(event -> {
            Unit selectedUnit = unitComboBox.getValue();
            if (selectedUnit != null) {
                unitDetailsArea.setText(
                        "Unit Code: " + selectedUnit.getUnitCode() + "\n" +
                        "Unit Name: " + selectedUnit.getUnitName() + "\n" +
                        "Lecturer: " + selectedUnit.getLecturerName() + "\n" +
                        "Description: " + selectedUnit.getDescription()
                );
            } else {
                unitDetailsArea.setText("");
            }
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the unit field by default
        Platform.runLater(unitComboBox::requestFocus);
        
        // Convert the result to a unit when the enroll button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == enrollButtonType) {
                return unitComboBox.getValue();
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Unit> result = dialog.showAndWait();
        
        result.ifPresent(unit -> {
            try {
                // Enroll the student in the unit
                if (currentStudent != null) {
                    currentStudent.addEnrolledUnit(unit.getUnitCode());
                    UserService.saveUser(currentStudent);
                    
                    // Refresh the units grid
                    loadEnrolledUnits();
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Enrollment Successful", 
                            "Successfully Enrolled", 
                            "You have been enrolled in " + unit.getUnitCode() + " - " + unit.getUnitName());
                    
                    LOGGER.info("Student enrolled in unit: " + unit.getUnitCode());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error enrolling in unit", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Enrollment Error", 
                        "Failed to enroll in unit: " + e.getMessage());
            }
        });
    }
    
    /**
     * Populates the settings form with the current student's data.
     */
    private void populateSettingsForm() {
        if (currentStudent == null) {
            return;
        }
        
        fullNameField.setText(currentStudent.getFullName());
        emailField.setText(currentStudent.getEmail());
    }
    
    /**
     * Shows the dashboard content and updates navigation icons.
     *
     * @param event The mouse event
     */
    @FXML
    protected void showDashboard(MouseEvent event) {
        hideAllContent();
        dashboardContent.setVisible(true);
        updateNavigationIcons(homeIcon);
        statusLabel.setText("Dashboard");
    }
    
    /**
     * Shows the assessments content and updates navigation icons.
     *
     * @param event The mouse event
     */
    @FXML
    protected void showAssessments(MouseEvent event) {
        hideAllContent();
        assessmentsContent.setVisible(true);
        updateNavigationIcons(assessmentIcon);
        statusLabel.setText("Formal Assessments");
        
        // Load assessments for the student
        loadAssessments();
    }
    
    /**
     * Shows the practice tests content and updates navigation icons.
     *
     * @param event The mouse event
     */
    @FXML
    protected void showPracticeTests(MouseEvent event) {
        hideAllContent();
        practiceContent.setVisible(true);
        updateNavigationIcons(practiceIcon);
        statusLabel.setText("Practice Tests");
        
        // Load practice tests for the student
        loadPracticeTests();
    }
    
    /**
     * Shows the analytics section.
     */
    @FXML
    private void showAnalytics() {
        hideAllContent();
        analyticsContent.setVisible(true);
        updateNavigationIcons(analyticsIcon);
        loadAnalyticsData();
    }
    
    /**
     * Loads analytics data for the student.
     */
    private void loadAnalyticsData() {
        if (currentStudent == null) {
            LOGGER.warning("Cannot load analytics data: current student is null");
            return;
        }
        
        try {
            PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
            
            // Load overview data
            Map<String, Object> studentPerformance = analyticsService.getStudentPerformance(currentStudent.getStudentId());
            if (studentPerformance != null) {
                // Update overview metrics
                double avgScore = (double) studentPerformance.getOrDefault("averageScore", 0.0);
                int assessmentsCompleted = (int) studentPerformance.getOrDefault("assessmentsCompleted", 0);
                int practiceTestsCompleted = (int) studentPerformance.getOrDefault("practiceTestsCompleted", 0);
                
                averageScoreLabel.setText(String.format("%.1f%%", avgScore));
                assessmentsCompletedLabel.setText(String.valueOf(assessmentsCompleted));
                practiceTestsCompletedLabel.setText(String.valueOf(practiceTestsCompleted));
                
                // Create performance trend chart
                List<Map<String, Object>> performanceHistory = analyticsService.getStudentPerformanceHistory(currentStudent.getStudentId());
                if (performanceHistory != null && !performanceHistory.isEmpty()) {
                    createPerformanceTrendChart(performanceHistory);
                }
            }
            
            // Load formal tests data
            List<Map<String, Object>> formalTests = analyticsService.getStudentAssessments(currentStudent.getStudentId());
            if (formalTests != null && !formalTests.isEmpty()) {
                populateFormalTestsTable(formalTests);
                
                // Calculate metrics
                double avgFormalScore = formalTests.stream()
                        .mapToDouble(test -> (double) test.getOrDefault("score", 0.0))
                        .average()
                        .orElse(0.0);
                
                double highestFormalScore = formalTests.stream()
                        .mapToDouble(test -> (double) test.getOrDefault("score", 0.0))
                        .max()
                        .orElse(0.0);
                
                formalTestsAvgScoreLabel.setText(String.format("%.1f%%", avgFormalScore));
                formalTestsHighestScoreLabel.setText(String.format("%.1f%%", highestFormalScore));
                formalTestsCompletedLabel.setText(String.valueOf(formalTests.size()));
            }
            
            // Load practice tests data
            List<Map<String, Object>> practiceTests = analyticsService.getStudentPracticeTests(currentStudent.getStudentId());
            if (practiceTests != null && !practiceTests.isEmpty()) {
                // Filter out practice tests with CREATED status
                List<Map<String, Object>> completedPracticeTests = practiceTests.stream()
                        .filter(test -> !"CREATED".equals(test.get("status")))
                        .collect(Collectors.toList());
                
                if (!completedPracticeTests.isEmpty()) {
                    populatePracticeTestsTable(completedPracticeTests);
                    
                    // Calculate metrics
                    double avgPracticeScore = completedPracticeTests.stream()
                            .mapToDouble(test -> (double) test.getOrDefault("score", 0.0))
                            .average()
                            .orElse(0.0);
                    
                    double highestPracticeScore = completedPracticeTests.stream()
                            .mapToDouble(test -> (double) test.getOrDefault("score", 0.0))
                            .max()
                            .orElse(0.0);
                    
                    practiceTestsAvgScoreLabel.setText(String.format("%.1f%%", avgPracticeScore));
                    practiceTestsHighestScoreLabel.setText(String.format("%.1f%%", highestPracticeScore));
                    practiceTestsCompletedLabel2.setText(String.valueOf(completedPracticeTests.size()));
                } else {
                    // No completed practice tests
                    practiceTestsAvgScoreLabel.setText("N/A");
                    practiceTestsHighestScoreLabel.setText("N/A");
                    practiceTestsCompletedLabel2.setText("0");
                    practiceTestsAnalyticsTable.getItems().clear();
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading analytics data", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Analytics Error", 
                    "An error occurred while loading analytics data: " + e.getMessage());
        }
    }
    
    /**
     * Creates a performance trend chart based on the student's performance history.
     * 
     * @param performanceHistory List of performance data points
     */
    private void createPerformanceTrendChart(List<Map<String, Object>> performanceHistory) {
        // Clear previous chart if any
        performanceTrendChartContainer.getChildren().clear();
        
        // Create chart using ChartUtil
        LineChart<String, Number> chart = ChartUtil.createLineChart(
                "Performance Over Time", 
                "Date", 
                "Score (%)", 
                performanceHistory, 
                "date", 
                "score");
        
        // Add chart to container
        performanceTrendChartContainer.getChildren().add(chart);
    }
    
    /**
     * Populates the formal tests table with data.
     * 
     * @param formalTests List of formal test data
     */
    private void populateFormalTestsTable(List<Map<String, Object>> formalTests) {
        ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList(formalTests);
        
        // Clear previous data
        formalTestsTable.getItems().clear();
        
        // Set cell value factories
        formalTestUnitColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().get("unitCode"))));
        
        formalTestTitleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().get("title"))));
        
        formalTestScoreColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.format("%.1f%%", (double) cellData.getValue().get("score"))));
        
        formalTestDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().get("date"))));
        
        // Add data to table
        formalTestsTable.setItems(tableData);
    }
    
    /**
     * Populates the practice tests table with data.
     * 
     * @param practiceTests List of practice test data
     */
    private void populatePracticeTestsTable(List<Map<String, Object>> practiceTests) {
        ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList(practiceTests);
        
        // Clear previous data
        practiceTestsAnalyticsTable.getItems().clear();
        
        // Set cell value factories
        practiceTestUnitColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().get("unitCode"))));
        
        practiceTestTitleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.valueOf(cellData.getValue().get("title"))));
        
        practiceTestScoreColumn.setCellValueFactory(cellData -> {
            // First try to use the percentage field
            Object percentageObj = cellData.getValue().get("percentage");
            if (percentageObj != null) {
                double percentage = (double) percentageObj;
                return new SimpleStringProperty(String.format("%.1f%%", percentage));
            }
            
            // If percentage is not available, calculate it from score and totalPossible
            Object scoreObj = cellData.getValue().get("score");
            Object totalObj = cellData.getValue().get("totalPossible");
            
            if (scoreObj != null && totalObj != null) {
                double score = (double) scoreObj;
                double total = (double) totalObj;
                
                if (total > 0) {
                    double percentage = (score / total) * 100;
                    return new SimpleStringProperty(String.format("%.1f%%", percentage));
                }
            }
            
            return new SimpleStringProperty("N/A");
        });
        
        practiceTestDateColumn.setCellValueFactory(cellData -> {
            Object dateObj = cellData.getValue().get("date");
            if (dateObj == null) {
                Object completionDateObj = cellData.getValue().get("completion_date");
                if (completionDateObj == null) {
                    return new SimpleStringProperty("N/A");
                }
                return new SimpleStringProperty(String.valueOf(completionDateObj));
            }
            return new SimpleStringProperty(String.valueOf(dateObj));
        });
        
        // Add data to table
        practiceTestsAnalyticsTable.setItems(tableData);
    }
    
    /**
     * Sets up the assessments table with columns and selection listener.
     */
    private void setupAssessmentsTable() {
        // Set up table columns
        assessmentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        assessmentUnitColumn.setCellValueFactory(cellData -> {
            String unitCode = cellData.getValue().getUnitCode();
            Unit unit = UnitService.getUnitByCode(unitCode);
            return new SimpleStringProperty(unit != null ? unit.getUnitCode() : unitCode);
        });
        
        // Format dates for display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        assessmentStartColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartDateTime();
            return new SimpleStringProperty(startTime != null ? startTime.format(dateFormatter) : "N/A");
        });
        
        assessmentEndColumn.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndDateTime();
            return new SimpleStringProperty(endTime != null ? endTime.format(dateFormatter) : "N/A");
        });
        
        assessmentDurationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDurationMinutes() + " min"));
        assessmentStatusColumn.setCellValueFactory(cellData -> {
            Assessment assessment = cellData.getValue();
            LocalDateTime now = LocalDateTime.now();
            
            if (!assessment.isActive()) {
                return new SimpleStringProperty("Not Active");
            } else if (assessment.getStartDateTime() != null && now.isBefore(assessment.getStartDateTime())) {
                return new SimpleStringProperty("Upcoming");
            } else if (assessment.getEndDateTime() != null && now.isAfter(assessment.getEndDateTime())) {
                return new SimpleStringProperty("Closed");
            } else {
                return new SimpleStringProperty("Open");
            }
        });
        
        // Set up selection listener
        assessmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAssessment = newSelection;
            updateAssessmentButtons();
        });
    }
    
    /**
     * Sets up the assessment filters.
     */
    private void setupAssessmentFilters() {
        // Set up unit filter
        assessmentUnitFilter.getItems().add("All Units");
        assessmentUnitFilter.setValue("All Units");
        
        // Set up status filter
        assessmentStatusFilter.getItems().addAll("All Assessments", "Open", "Upcoming", "Closed");
        assessmentStatusFilter.setValue("All Assessments");
        
        // Add listeners to filters
        assessmentUnitFilter.setOnAction(event -> filterAssessments());
        assessmentStatusFilter.setOnAction(event -> filterAssessments());
    }
    
    /**
     * Sets up the assessment action buttons.
     */
    private void setupAssessmentButtons() {
        // Disable buttons initially
        viewAssessmentButton.setDisable(true);
        takeAssessmentButton.setDisable(true);
        downloadAssessmentButton.setDisable(true);
        
        // Set up button actions
        viewAssessmentButton.setOnAction(event -> viewAssessmentDetails());
        takeAssessmentButton.setOnAction(event -> takeAssessment());
        downloadAssessmentButton.setOnAction(event -> downloadAssessment());
    }
    
    /**
     * Loads assessments for the student's enrolled units.
     */
    private void loadAssessments() {
        if (currentStudent == null || enrolledUnits.isEmpty()) {
            assessments.clear();
            assessmentsTable.setItems(assessments);
            return;
        }
        
        try {
            // Get all assessments for the student's enrolled units
            List<Assessment> studentAssessments = new ArrayList<>();
            
            for (Unit unit : enrolledUnits) {
                List<Assessment> unitAssessments = AssessmentService.getAssessmentsByUnit(unit.getUnitCode());
                // Only add formal assessments (not practice tests)
                unitAssessments = unitAssessments.stream()
                        .filter(a -> !a.isPractice())
                        .collect(Collectors.toList());
                studentAssessments.addAll(unitAssessments);
            }
            
            // Update the observable list
            assessments.setAll(studentAssessments);
            
            // Update unit filter with enrolled units
            updateUnitFilter();
            
            // Apply filters
            filterAssessments();
            
            LOGGER.info("Loaded " + studentAssessments.size() + " assessments for student");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading assessments", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load assessments", e.getMessage());
        }
    }
    
    /**
     * Updates the unit filter with the student's enrolled units.
     */
    private void updateUnitFilter() {
        assessmentUnitFilter.getItems().clear();
        assessmentUnitFilter.getItems().add("All Units");
        
        for (Unit unit : enrolledUnits) {
            assessmentUnitFilter.getItems().add(unit.getUnitCode());
        }
        
        assessmentUnitFilter.setValue("All Units");
    }
    
    /**
     * Filters the assessments based on the selected unit and status.
     */
    private void filterAssessments() {
        if (assessments == null || assessments.isEmpty()) {
            assessmentsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        
        String unitFilter = assessmentUnitFilter.getValue();
        String statusFilter = assessmentStatusFilter.getValue();
        
        // Create a filtered list
        List<Assessment> filteredList = assessments.stream()
                .filter(assessment -> {
                    // Apply unit filter
                    boolean unitMatch = "All Units".equals(unitFilter) || 
                            assessment.getUnitCode().equals(unitFilter);
                    
                    // Apply status filter
                    boolean statusMatch = true;
                    if (!"All Assessments".equals(statusFilter)) {
                        LocalDateTime now = LocalDateTime.now();
                        
                        if ("Open".equals(statusFilter)) {
                            statusMatch = assessment.isActive() && 
                                    (assessment.getStartDateTime() == null || !now.isBefore(assessment.getStartDateTime())) &&
                                    (assessment.getEndDateTime() == null || !now.isAfter(assessment.getEndDateTime()));
                        } else if ("Upcoming".equals(statusFilter)) {
                            statusMatch = assessment.isActive() && 
                                    assessment.getStartDateTime() != null && 
                                    now.isBefore(assessment.getStartDateTime());
                        } else if ("Closed".equals(statusFilter)) {
                            statusMatch = !assessment.isActive() || 
                                    (assessment.getEndDateTime() != null && 
                                    now.isAfter(assessment.getEndDateTime()));
                        }
                    }
                    
                    return unitMatch && statusMatch;
                })
                .collect(Collectors.toList());
        
        // Update the table
        assessmentsTable.setItems(FXCollections.observableArrayList(filteredList));
        
        // Clear selection
        assessmentsTable.getSelectionModel().clearSelection();
        selectedAssessment = null;
        updateAssessmentButtons();
    }
    
    /**
     * Updates the assessment action buttons based on the selected assessment.
     */
    private void updateAssessmentButtons() {
        if (selectedAssessment == null) {
            viewAssessmentButton.setDisable(true);
            takeAssessmentButton.setDisable(true);
            downloadAssessmentButton.setDisable(true);
            return;
        }
        
        // Enable view button
        viewAssessmentButton.setDisable(false);
        
        // Check if assessment is available to take
        LocalDateTime now = LocalDateTime.now();
        boolean canTake = selectedAssessment.isActive() && 
                (selectedAssessment.getStartDateTime() == null || !now.isBefore(selectedAssessment.getStartDateTime())) &&
                (selectedAssessment.getEndDateTime() == null || !now.isAfter(selectedAssessment.getEndDateTime()));
        
        takeAssessmentButton.setDisable(!canTake);
        
        // Check if assessment can be downloaded for offline
        downloadAssessmentButton.setDisable(!selectedAssessment.isAllowOfflineAttempt() || !canTake);
    }
    
    /**
     * Opens the assessment details view.
     */
    private void viewAssessmentDetails() {
        if (selectedAssessment == null) {
            return;
        }
        
        statusLabel.setText("Viewing details for assessment: " + selectedAssessment.getTitle());
        
        // TODO: Implement assessment details view
        showAlert(Alert.AlertType.INFORMATION, "Assessment Details", 
                selectedAssessment.getTitle(), 
                "Unit: " + selectedAssessment.getUnitCode() + "\n" +
                "Description: " + selectedAssessment.getDescription() + "\n" +
                "Duration: " + selectedAssessment.getDurationMinutes() + " minutes\n" +
                "Total Marks: " + selectedAssessment.getTotalMarks());
    }
    
    /**
     * Opens the assessment taking view.
     */
    private void takeAssessment() {
        if (selectedAssessment == null) {
            return;
        }
        
        statusLabel.setText("Taking assessment: " + selectedAssessment.getTitle());
        
        try {
            // Load the assessment taking view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/assessment-taking-view.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set up the assessment
            AssessmentTakingController controller = loader.getController();
            controller.setupAssessment(selectedAssessment, currentStudent.getStudentId());
            
            // Create a new stage for the assessment
            Stage assessmentStage = new Stage();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/assessment.css").toExternalForm());
            
            // Set up the stage
            assessmentStage.setScene(scene);
            assessmentStage.setTitle("Taking Assessment: " + selectedAssessment.getTitle());
            assessmentStage.setMinWidth(800);
            assessmentStage.setMinHeight(600);
            
            // Show the assessment stage
            assessmentStage.show();
            
            // Log the action
            LOGGER.info("Student " + currentStudent.getStudentId() + " started assessment " + selectedAssessment.getAssessmentId());
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading assessment taking view", e);
            
            // Show error alert
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Error Loading Assessment", 
                    "There was an error loading the assessment: " + e.getMessage());
        }
    }
    
    /**
     * Downloads the assessment for offline use.
     */
    private void downloadAssessment() {
        if (selectedAssessment == null) {
            return;
        }
        
        statusLabel.setText("Downloading assessment: " + selectedAssessment.getTitle());
        
        // TODO: Implement assessment download functionality
        showAlert(Alert.AlertType.INFORMATION, "Download Assessment", 
                "Downloading " + selectedAssessment.getTitle(), 
                "This feature will be implemented in the next phase.");
    }
    
    /**
     * Sets up the practice test table.
     */
    private void setupPracticeTestTable() {
        practiceTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        practiceUnitColumn.setCellValueFactory(cellData -> {
            String unitCode = cellData.getValue().getUnitCode();
            Unit unit = UnitService.getUnitByCode(unitCode);
            return new SimpleStringProperty(unit != null ? unit.getUnitCode() : unitCode);
        });
        practiceScoreColumn.setCellValueFactory(cellData -> {
            Assessment assessment = cellData.getValue();
            
            // If the test has CREATED status, show N/A for percentage
            if ("CREATED".equals(assessment.getStatus())) {
                return new SimpleStringProperty("N/A");
            }
            
            double percentage = assessment.getPercentage();
            // If percentage is 0 but we have a score, calculate percentage
            if (percentage == 0 && assessment.getScore() > 0 && assessment.getTotalMarks() > 0) {
                percentage = (assessment.getScore() / assessment.getTotalMarks()) * 100;
            }
            return new SimpleStringProperty(String.format("%.1f%%", percentage));
        });
        practiceGradeColumn.setCellValueFactory(cellData -> {
            Assessment assessment = cellData.getValue();
            
            // If the test has CREATED status, show N/A for grade
            if ("CREATED".equals(assessment.getStatus())) {
                return new SimpleStringProperty("N/A");
            }
            
            // Use the grade field if it's set
            if (assessment.getGrade() != null && !assessment.getGrade().equals("N/A")) {
                return new SimpleStringProperty(assessment.getGrade());
            }
            
            // Otherwise calculate it from percentage
            double percentage = assessment.getPercentage();
            // If percentage is 0 but we have a score, calculate percentage
            if (percentage == 0 && assessment.getScore() > 0 && assessment.getTotalMarks() > 0) {
                percentage = (assessment.getScore() / assessment.getTotalMarks()) * 100;
            }
            
            if (percentage < 40) {
                return new SimpleStringProperty("F");
            } else if (percentage < 50) {
                return new SimpleStringProperty("D");
            } else if (percentage < 60) {
                return new SimpleStringProperty("C");
            } else if (percentage < 70) {
                return new SimpleStringProperty("B");
            } else if (percentage > 0) {
                return new SimpleStringProperty("A");
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        practiceStatusColumn.setCellValueFactory(cellData -> {
            Assessment assessment = cellData.getValue();
            
            // Use the status field if it's set to CREATED
            if ("CREATED".equals(assessment.getStatus())) {
                return new SimpleStringProperty("Created");
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            if (!assessment.isActive()) {
                return new SimpleStringProperty("Not Active");
            } else if (assessment.getStartDateTime() != null && now.isBefore(assessment.getStartDateTime())) {
                return new SimpleStringProperty("Upcoming");
            } else if (assessment.getEndDateTime() != null && now.isAfter(assessment.getEndDateTime())) {
                return new SimpleStringProperty("Closed");
            } else {
                return new SimpleStringProperty("Open");
            }
        });
        
        practiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAssessment = newSelection;
            updatePracticeButtons();
        });
    }
    
    /**
     * Sets up the practice test form.
     */
    private void setupPracticeTestForm() {
        // Set up unit combo box
        practiceUnitComboBox.setConverter(new StringConverter<Unit>() {
            @Override
            public String toString(Unit unit) {
                return unit != null ? unit.getUnitCode() + " - " + unit.getUnitName() : "";
            }

            @Override
            public Unit fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // Populate unit combo box with enrolled units
        practiceUnitComboBox.setItems(FXCollections.observableArrayList(enrolledUnits));
        
        // Set up difficulty combo box
        practiceDifficultyComboBox.setItems(FXCollections.observableArrayList(
                "Easy", "Medium", "Hard", "Mixed"
        ));
        
        // Set up question type combo box - removed True/False option
        practiceQuestionTypeComboBox.setItems(FXCollections.observableArrayList(
                "Multiple Choice", "Short Answer", "List Based", "Mixed"
        ));
        
        // Set up spinners
        SpinnerValueFactory<Integer> questionCountFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, 10);
        practiceQuestionCountSpinner.setValueFactory(questionCountFactory);
        
        SpinnerValueFactory<Integer> durationFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 30);
        practiceDurationSpinner.setValueFactory(durationFactory);
        
        // Set up create button action
        createPracticeButton.setOnAction(e -> createPracticeTest());
        
        // Set up topic selection components
        setupTopicSelection();
        
        // When unit is selected, load topics for that unit
        practiceUnitComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTopicsForUnit(newVal.getUnitCode());
            }
        });
    }
    
    /**
     * Sets up the topic selection components.
     */
    private void setupTopicSelection() {
        // Create the CheckComboBox for topic selection
        topicCheckComboBox = new CheckComboBox<>();
        topicCheckComboBox.setTitle("Select Topics");
        topicCheckComboBox.setPrefWidth(300.0);
        
        // Set converter for displaying topic names
        topicCheckComboBox.setConverter(new StringConverter<Topic>() {
            @Override
            public String toString(Topic topic) {
                return topic != null ? topic.getTopicName() : "";
            }

            @Override
            public Topic fromString(String string) {
                return null; // Not needed for this use case
            }
        });
        
        // Add the CheckComboBox to the container
        topicSelectionContainer.getChildren().add(topicCheckComboBox);
    }
    
    /**
     * Loads topics for the selected unit.
     * 
     * @param unitCode The unit code to load topics for
     */
    private void loadTopicsForUnit(String unitCode) {
        try {
            // Clear existing topics
            topicCheckComboBox.getItems().clear();
            
            // Load topics for the unit
            List<Topic> topics = TopicService.getTopicsByUnit(unitCode);
            
            // Add topics to the CheckComboBox
            topicCheckComboBox.getItems().addAll(topics);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading topics for unit", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to Load Topics", 
                    "Could not load topics for the selected unit.");
        }
    }
    
    /**
     * Sets up the practice test filters.
     */
    private void setupPracticeFilters() {
        practiceUnitFilter.getItems().add("All Units");
        practiceUnitFilter.setValue("All Units");
        practiceStatusFilter.getItems().addAll("All Assessments", "Open", "Upcoming", "Closed", "Created");
        practiceStatusFilter.setValue("All Assessments");
        
        practiceUnitFilter.setOnAction(event -> filterPracticeTests());
        practiceStatusFilter.setOnAction(event -> filterPracticeTests());
    }
    
    /**
     * Sets up the practice test buttons.
     */
    private void setupPracticeButtons() {
        viewPracticeButton.setOnAction(event -> viewPracticeTestDetails());
        viewPracticeButton.setDisable(true);
        takePracticeButton.setOnAction(event -> takePracticeTest());
        takePracticeButton.setDisable(true);
    }
    
    /**
     * Updates the practice test buttons based on the selected assessment.
     */
    private void updatePracticeButtons() {
        if (selectedAssessment == null) {
            viewPracticeButton.setDisable(true);
            takePracticeButton.setDisable(true);
            return;
        }
        
        // Get the status of the selected assessment
        String status = selectedAssessment.getStatus();
        
        // For completed tests, enable the view button and change its text to "Review Results"
        if ("COMPLETED".equals(status)) {
            viewPracticeButton.setText("Review Results");
            viewPracticeButton.setDisable(false);
            takePracticeButton.setDisable(true);
            return;
        } else {
            // For non-completed tests, set the button text back to "View Details"
            viewPracticeButton.setText("View Details");
            viewPracticeButton.setDisable(false);
        }
        
        // For practice tests with CREATED status, always allow taking them
        if ("CREATED".equals(status)) {
            takePracticeButton.setDisable(false);
            return;
        }
        
        // Check if practice test is available to take
        LocalDateTime now = LocalDateTime.now();
        boolean canTake = selectedAssessment.isActive() && 
                (selectedAssessment.getStartDateTime() == null || !now.isBefore(selectedAssessment.getStartDateTime())) &&
                (selectedAssessment.getEndDateTime() == null || !now.isAfter(selectedAssessment.getEndDateTime()));
        
        takePracticeButton.setDisable(!canTake);
    }
    
    /**
     * Creates a new practice test from the form input.
     */
    @FXML
    private void createPracticeTest() {
        // Validate form inputs
        if (practiceTitleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Title", 
                    "Please enter a title for the practice test.");
            return;
        }
        
        if (practiceUnitComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Unit", 
                    "Please select a unit for the practice test.");
            return;
        }
        
        if (practiceDifficultyComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Difficulty", 
                    "Please select a difficulty level for the practice test.");
            return;
        }
        
        if (practiceQuestionTypeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Question Type", 
                    "Please select a question type for the practice test.");
            return;
        }
        
        if (practiceQuestionCountSpinner.getValue() == null || practiceQuestionCountSpinner.getValue() < 1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Question Count", 
                    "Please enter a valid number of questions (minimum 1).");
            return;
        }
        
        try {
            // Debug: Check what questions are in the database for this unit
            String unitCode = practiceUnitComboBox.getValue().getUnitCode();
            AssessmentService.debugCheckQuestions(unitCode);
            
            // Create a new assessment object
            Assessment practiceTest = new Assessment();
            practiceTest.setTitle(practiceTitleField.getText());
            practiceTest.setUnitCode(unitCode);
            practiceTest.setDifficulty(practiceDifficultyComboBox.getValue());
            practiceTest.setQuestionType(practiceQuestionTypeComboBox.getValue());
            practiceTest.setQuestionCount(practiceQuestionCountSpinner.getValue());
            
            // Set practice test specific properties
            practiceTest.setPractice(true);
            practiceTest.setCreatedBy(currentStudent.getUserId());
            practiceTest.setStartDateTime(LocalDateTime.now());
            
            // Set duration based on number of questions (2 minutes per question)
            int durationMinutes = practiceQuestionCountSpinner.getValue() * 2;
            practiceTest.setDuration(durationMinutes);
            
            // Get selected topics from the CheckComboBox
            ObservableList<Topic> selectedTopics = FXCollections.observableArrayList();
            for (Topic topic : topicCheckComboBox.getCheckModel().getCheckedItems()) {
                selectedTopics.add(topic);
            }
            
            // Generate random questions for the practice test
            AssessmentService assessmentService = new AssessmentService();
            boolean success = assessmentService.generateQuestionsForPracticeTest(
                    practiceTest, 
                    practiceUnitComboBox.getValue().getUnitCode(), 
                    practiceQuestionTypeComboBox.getValue(),
                    practiceDifficultyComboBox.getValue(),
                    practiceQuestionCountSpinner.getValue(),
                    selectedTopics
            );
            
            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to Create Practice Test", 
                        "Could not generate enough questions for this practice test. Try different criteria.");
                return;
            }
            
            // Save the practice test
            assessmentService.savePracticeTest(practiceTest, currentStudent.getUserId());
            
            // Clear the form
            clearPracticeTestForm();
            
            // Refresh the practice tests list
            loadPracticeTests();
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Practice Test Created", 
                    "Your practice test has been created successfully. You can now take it from the practice tests list.");
            
        } catch (Exception e) {
            e.printStackTrace();
            
            // Show error alert
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to Create Practice Test", 
                    "An error occurred while creating the practice test: " + e.getMessage());
        }
    }
    
    /**
     * Clears the practice test form inputs.
     */
    private void clearPracticeTestForm() {
        practiceTitleField.clear();
        practiceUnitComboBox.getSelectionModel().clearSelection();
        practiceDifficultyComboBox.getSelectionModel().clearSelection();
        practiceQuestionTypeComboBox.getSelectionModel().clearSelection();
        practiceQuestionCountSpinner.getValueFactory().setValue(10);
        practiceDurationSpinner.getValueFactory().setValue(30);
        
        // Clear topic selection
        topicCheckComboBox.getCheckModel().clearChecks();
    }
    
    /**
     * Opens the practice test details view.
     */
    private void viewPracticeTestDetails() {
        if (selectedAssessment == null) {
            LOGGER.log(Level.WARNING, "No assessment selected");
            return;
        }
        
        LOGGER.log(Level.INFO, "Viewing details for practice test: {0}", selectedAssessment.getTitle());
        statusLabel.setText("Viewing details for practice test: " + selectedAssessment.getTitle());
        
        // Check if the practice test has been completed
        String status = selectedAssessment.getStatus();
        LOGGER.log(Level.INFO, "Practice test status: {0}", status);
        
        // Check if the practice test has been completed
        if ("COMPLETED".equals(status)) {
            try {
                // Get practice test data
                String practiceId = selectedAssessment.getPracticeId();
                String assessmentId = selectedAssessment.getAssessmentId();
                
                LOGGER.log(Level.INFO, "Practice ID: {0}, Assessment ID: {1}", new Object[]{practiceId, assessmentId});
                
                if (practiceId == null || practiceId.isEmpty()) {
                    LOGGER.log(Level.SEVERE, "Practice ID is null or empty");
                    showAlert(Alert.AlertType.ERROR, "Error", "Missing Practice ID", 
                            "The practice ID for this assessment is missing. Please contact support.");
                    return;
                }
                
                // Load the assessment results view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/assessment-results-view.fxml"));
                Parent root = loader.load();
                
                // Get questions for this assessment
                List<Question> questions = QuestionService.getQuestionsByAssessment(assessmentId);
                LOGGER.log(Level.INFO, "Retrieved {0} questions for assessment", questions.size());
                
                // Get student responses for this practice test
                List<StudentResponse> responses = studentAssessmentService.getStudentResponses(practiceId);
                LOGGER.log(Level.INFO, "Retrieved {0} responses for practice test", responses.size());
                
                // Create assessment object
                Assessment assessment = new Assessment();
                assessment.setAssessmentId(assessmentId);
                assessment.setTitle(selectedAssessment.getTitle());
                assessment.setUnitCode(selectedAssessment.getUnitCode());
                assessment.setTotalMarks((int)selectedAssessment.getTotalMarks());
                
                // Create attempt object
                StudentAssessmentAttempt attempt = new StudentAssessmentAttempt();
                attempt.setAttemptId(practiceId);
                attempt.setStudentId(currentStudent.getStudentId());
                attempt.setAssessmentId(assessmentId);
                attempt.setScore((int)selectedAssessment.getScore());
                attempt.setTotalPossible((int)selectedAssessment.getTotalMarks());
                
                // If we don't have any responses but we have a score, create dummy responses
                if (responses.isEmpty() && questions.size() > 0 && selectedAssessment.getScore() > 0) {
                    LOGGER.log(Level.INFO, "Creating dummy responses based on score");
                    
                    int totalScore = (int)selectedAssessment.getScore();
                    int totalQuestions = questions.size();
                    int correctAnswers = Math.min(totalScore, totalQuestions);
                    
                    for (int i = 0; i < totalQuestions; i++) {
                        Question question = questions.get(i);
                        StudentResponse response = new StudentResponse();
                        response.setResponseId(UUID.randomUUID().toString());
                        response.setAttemptId(practiceId);
                        response.setQuestionId(question.getQuestionId());
                        
                        // Mark as correct or incorrect based on score
                        boolean isCorrect = i < correctAnswers;
                        response.setCorrect(isCorrect);
                        
                        if (isCorrect) {
                            response.setMarksAwarded(question.getMarks());
                            response.setResponseText(question.getCorrectAnswer());
                            response.setFeedback("Correct answer!");
                        } else {
                            response.setMarksAwarded(0);
                            response.setResponseText("Unknown response");
                            response.setFeedback("Incorrect answer.");
                        }
                        
                        responses.add(response);
                    }
                    
                    LOGGER.log(Level.INFO, "Created {0} dummy responses", responses.size());
                }
                
                // Add responses to attempt
                for (StudentResponse response : responses) {
                    attempt.addResponse(response.getQuestionId(), response);
                }
                
                // Set up the controller
                AssessmentResultsController controller = loader.getController();
                controller.setAttempt(attempt, questions, assessment);
                
                // Show the results view
                Stage resultsStage = new Stage();
                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/main.css").toExternalForm());
                resultsStage.setScene(scene);
                resultsStage.setTitle("Practice Test Results");
                resultsStage.show();
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing practice test results", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load results", 
                        "There was an error loading the practice test results: " + e.getMessage());
            }
        } else {
            // For non-completed tests, show basic information
            showAlert(Alert.AlertType.INFORMATION, "Practice Test Details", 
                    selectedAssessment.getTitle(), 
                    "Unit: " + selectedAssessment.getUnitCode() + "\n" +
                    "Status: " + status + "\n" +
                    "Duration: " + selectedAssessment.getDurationMinutes() + " minutes\n" +
                    "Total Marks: " + selectedAssessment.getTotalMarks());
        }
    }
    
    /**
     * Opens the practice test taking view.
     */
    private void takePracticeTest() {
        if (selectedAssessment == null) {
            return;
        }
        
        statusLabel.setText("Taking practice test: " + selectedAssessment.getTitle());
        
        try {
            // Load the assessment taking view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/assessment-taking-view.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set up the assessment
            AssessmentTakingController controller = loader.getController();
            controller.setupAssessment(selectedAssessment, currentStudent.getStudentId());
            
            // Create a new stage for the assessment
            Stage assessmentStage = new Stage();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/assessment.css").toExternalForm());
            
            // Set up the stage
            assessmentStage.setScene(scene);
            assessmentStage.setTitle("Practice Test: " + selectedAssessment.getTitle());
            assessmentStage.setMaximized(true);
            
            // Show the assessment stage
            assessmentStage.show();
            
            // Close the current stage if needed
            // Stage currentStage = (Stage) takePracticeButton.getScene().getWindow();
            // currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            
            // Show error alert
            showAlert(Alert.AlertType.ERROR, "Error", "Error Loading Practice Test", 
                    "There was an error loading the practice test: " + e.getMessage());
        }
    }
    
    /**
     * Loads practice tests for the current student.
     */
    private void loadPracticeTests() {
        try {
            if (currentStudent == null) {
                LOGGER.log(Level.WARNING, "Cannot load practice tests: current student is null");
                return;
            }
            
            // Create PerformanceAnalyticsService to retrieve practice tests
            PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
            
            // Get practice tests from the analytics service
            List<Map<String, Object>> practiceTestsData = analyticsService.getStudentPracticeTests(currentStudent.getStudentId());
            LOGGER.log(Level.INFO, "Retrieved {0} practice tests from PerformanceAnalyticsService", practiceTestsData.size());
            
            // Convert the map data to Assessment objects
            List<Assessment> practiceTests = new ArrayList<>();
            
            for (Map<String, Object> testData : practiceTestsData) {
                Assessment assessment = new Assessment();
                assessment.setAssessmentId((String) testData.get("assessmentId"));
                assessment.setTitle((String) testData.get("title"));
                assessment.setUnitCode((String) testData.get("unitCode"));
                
                // Set the practice ID from the practice_assessments table
                if (testData.containsKey("practiceId")) {
                    assessment.setPracticeId((String) testData.get("practiceId"));
                }
                
                // Set status based on the status field from the database
                String status = (String) testData.get("status");
                if (status == null) {
                    status = "COMPLETED"; // Default if not specified
                }
                assessment.setStatus(status);
                
                // Set score if available
                if (testData.containsKey("score")) {
                    double score = ((Number) testData.get("score")).doubleValue();
                    assessment.setScore(score);
                }
                
                // Set duration if available
                if (testData.containsKey("durationMinutes")) {
                    int duration = ((Number) testData.get("durationMinutes")).intValue();
                    assessment.setDuration(duration);
                }
                
                // Store the percentage for display purposes
                if (testData.containsKey("percentage")) {
                    double percentage = ((Number) testData.get("percentage")).doubleValue();
                    assessment.setPercentage(percentage);
                }
                
                // Store the grade for display purposes
                if (testData.containsKey("grade")) {
                    String grade = (String) testData.get("grade");
                    assessment.setGrade(grade);
                }
                
                // Set practice flag
                assessment.setPractice(true);
                
                // Add to the list
                practiceTests.add(assessment);
            }
            
            // Update the table with the loaded practice tests
            practiceTable.setItems(FXCollections.observableArrayList(practiceTests));
            
            // Clear selection
            practiceTable.getSelectionModel().clearSelection();
            selectedAssessment = null;
            updatePracticeButtons();
            
            LOGGER.log(Level.INFO, "Loaded {0} practice tests for student {1}", 
                    new Object[]{practiceTests.size(), currentStudent.getStudentId()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading practice tests", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to Load Practice Tests", 
                    "An error occurred while loading practice tests: " + e.getMessage());
        }
    }
    
    /**
     * Filters the practice tests based on the selected unit and status.
     */
    private void filterPracticeTests() {
        // Get the selected unit and status
        String unitFilter = practiceUnitFilter.getValue();
        String statusFilter = practiceStatusFilter.getValue();
        
        // Get all practice tests for the student's enrolled units
        List<Assessment> practiceTests = new ArrayList<>();
        
        for (Unit unit : enrolledUnits) {
            List<Assessment> unitAssessments = AssessmentService.getAssessmentsByUnit(unit.getUnitCode());
            // Only add practice tests
            unitAssessments = unitAssessments.stream()
                    .filter(a -> a.isPractice())
                    .collect(Collectors.toList());
            practiceTests.addAll(unitAssessments);
        }
        
        // Create a filtered list
        List<Assessment> filteredList = practiceTests.stream()
                .filter(assessment -> {
                    // Apply unit filter
                    boolean unitMatch = "All Units".equals(unitFilter) || 
                            assessment.getUnitCode().equals(unitFilter);
                    
                    // Apply status filter
                    boolean statusMatch = true;
                    if (!"All Assessments".equals(statusFilter)) {
                        LocalDateTime now = LocalDateTime.now();
                        
                        if ("Open".equals(statusFilter)) {
                            statusMatch = assessment.isActive() && 
                                    (assessment.getStartDateTime() == null || !now.isBefore(assessment.getStartDateTime())) &&
                                    (assessment.getEndDateTime() == null || !now.isAfter(assessment.getEndDateTime()));
                        } else if ("Upcoming".equals(statusFilter)) {
                            statusMatch = assessment.isActive() && 
                                    assessment.getStartDateTime() != null && 
                                    now.isBefore(assessment.getStartDateTime());
                        } else if ("Closed".equals(statusFilter)) {
                            statusMatch = !assessment.isActive() || 
                                    (assessment.getEndDateTime() != null && 
                                    now.isAfter(assessment.getEndDateTime()));
                        } else if ("Created".equals(statusFilter)) {
                            statusMatch = "CREATED".equals(assessment.getStatus());
                        }
                    }
                    
                    return unitMatch && statusMatch;
                })
                .collect(Collectors.toList());
        
        // Update the table
        practiceTable.setItems(FXCollections.observableArrayList(filteredList));
        
        // Clear selection
        practiceTable.getSelectionModel().clearSelection();
        selectedAssessment = null;
        updatePracticeButtons();
    }
    
    /**
     * Sets up the contributions form with question types and difficulty levels.
     */
    private void setupContributionsForm() {
        try {
            // Set up question types
            contributionQuestionTypeComboBox.getItems().addAll(
                Question.TYPE_MULTIPLE_CHOICE,
                Question.TYPE_SHORT_ANSWER,
                Question.TYPE_LIST_BASED
            );
            
            // Set up difficulty levels
            contributionDifficultyComboBox.getItems().addAll(
                Question.DIFFICULTY_EASY,
                Question.DIFFICULTY_MEDIUM,
                Question.DIFFICULTY_HARD
            );
            
            // Set up topic combo box
            contributionTopicComboBox.setConverter(new StringConverter<Topic>() {
                @Override
                public String toString(Topic topic) {
                    return topic != null ? topic.getTopicName() : "";
                }

                @Override
                public Topic fromString(String string) {
                    return null; // Not needed for this use case
                }
            });
            
            // Force refresh of topics cache to ensure we have the latest data
            TopicService.getAllTopics();
            LOGGER.log(Level.INFO, "Refreshed topics cache during form setup");
            
            // Update unit combo box with enrolled units
            updateContributionUnitComboBox();
            
            // Set up unit combo box change listener to load topics
            contributionUnitComboBox.valueProperty().addListener((obs, oldUnit, newUnit) -> {
                if (newUnit != null) {
                    // Log the unit selection change
                    LOGGER.log(Level.INFO, "Unit selection changed to: " + newUnit.getUnitName() + " (" + newUnit.getUnitCode() + ")");
                    
                    // Force refresh of topics cache before loading topics for the selected unit
                    TopicService.getAllTopics();
                    
                    // Load topics for the selected unit
                    loadTopicsForContribution(newUnit.getUnitCode());
                } else {
                    contributionTopicComboBox.getItems().clear();
                }
            });
            
            // Set default values
            contributionQuestionTypeComboBox.setValue(Question.TYPE_MULTIPLE_CHOICE);
            contributionDifficultyComboBox.setValue(Question.DIFFICULTY_MEDIUM);
            
            // Initialize MCQ options
            optionsToggleGroup = new ToggleGroup();
            optionFields = new ArrayList<>();
            optionRadioButtons = new ArrayList<>();
            
            // Add listeners for question type changes to show/hide relevant fields
            contributionQuestionTypeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                updateContributionFormFields(newValue);
            });
            
            // Set up option and answer buttons
            addOptionButton.setOnAction(e -> addMCQOption());
            addAnswerButton.setOnAction(e -> addAnswer());
            
            // Set up clear and submit buttons
            clearContributionButton.setOnAction(e -> clearContributionForm());
            submitQuestionButton.setOnAction(e -> submitQuestion());
            
            // Initialize the form fields visibility
            updateContributionFormFields(Question.TYPE_MULTIPLE_CHOICE);
            
            LOGGER.log(Level.INFO, "Contributions form set up successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up contributions form", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Form Setup Error", 
                    "Could not set up the contributions form: " + e.getMessage());
        }
    }
    
    /**
     * Loads topics for the selected unit in the contribution form.
     * 
     * @param unitCode The unit code to load topics for
     */
    private void loadTopicsForContribution(String unitCode) {
        try {
            // Clear existing topics
            contributionTopicComboBox.getItems().clear();
            
            // Load topics for the unit - force a fresh database query
            List<Topic> topics = TopicService.getTopicsByUnit(unitCode);
            
            // Log the topics for debugging
            LOGGER.log(Level.INFO, "Loading topics for unit " + unitCode);
            for (Topic topic : topics) {
                LOGGER.log(Level.INFO, "Topic: " + topic.getTopicName());
            }
            
            // Add topics to the combo box
            contributionTopicComboBox.getItems().addAll(topics);
            
            // Select the first topic if available
            if (!topics.isEmpty()) {
                contributionTopicComboBox.setValue(topics.get(0));
            }
            
            LOGGER.log(Level.INFO, "Loaded " + topics.size() + " topics for unit " + unitCode);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading topics for unit " + unitCode, e);
        }
    }
    
    /**
     * Updates the visibility of form fields based on the selected question type.
     * 
     * @param questionType The selected question type
     */
    private void updateContributionFormFields(String questionType) {
        boolean isMultipleChoice = Question.TYPE_MULTIPLE_CHOICE.equals(questionType);
        boolean isListBased = Question.TYPE_LIST_BASED.equals(questionType);
        
        // Show/hide options section
        optionsLabel.setVisible(isMultipleChoice);
        contributionOptionsVBox.setVisible(isMultipleChoice);
        
        // Update correct answer label based on question type
        if (isListBased) {
            correctAnswerLabel.setText("Correct Answers (Multiple):");
        } else {
            correctAnswerLabel.setText("Correct Answer:");
        }
        
        // If switching to multiple choice, set up default options
        if (isMultipleChoice && optionsContainer.getChildren().isEmpty()) {
            // Add default options for multiple choice
            for (int i = 0; i < 4; i++) {
                addMCQOption();
            }
        }
    }
    
    /**
     * Adds a new MCQ option with a radio button for selecting the correct answer.
     */
    private void addMCQOption() {
        // Create an HBox to hold the radio button and text field
        HBox optionBox = new HBox(10);
        optionBox.setAlignment(Pos.CENTER_LEFT);
        
        // Create a label for the option (A, B, C, etc.)
        Label optionLabel = new Label(getNextOptionLabel());
        optionLabel.setMinWidth(25);
        optionLabel.setStyle("-fx-font-weight: bold;");
        
        // Create a radio button for selecting the correct answer
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(optionsToggleGroup);
        
        // Create a text field for the option text
        TextField optionField = new TextField();
        optionField.setPromptText("Enter option text");
        optionField.setPrefWidth(300);
        HBox.setHgrow(optionField, Priority.ALWAYS);
        
        // Create a remove button
        Button removeButton = new Button("✕");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> removeOption(optionBox, radioButton, optionField, optionLabel));
        
        // Add components to the option box
        optionBox.getChildren().addAll(optionLabel, radioButton, optionField, removeButton);
        
        // Add the option box to the container
        optionsContainer.getChildren().add(optionBox);
        
        // Keep track of the components
        optionRadioButtons.add(radioButton);
        optionFields.add(optionField);
        
        // Focus the new option field
        optionField.requestFocus();
    }
    
    /**
     * Gets the next option label (A, B, C, etc.) based on the current number of options.
     * 
     * @return The next option label
     */
    private String getNextOptionLabel() {
        int optionCount = optionsContainer.getChildren().size();
        // Convert to letter (0 = A, 1 = B, etc.)
        return String.valueOf((char)('A' + optionCount));
    }
    
    /**
     * Removes an MCQ option.
     */
    private void removeOption(HBox optionBox, RadioButton radioButton, TextField optionField, Label optionLabel) {
        // Remove from UI
        optionsContainer.getChildren().remove(optionBox);
        
        // Remove from tracking lists
        optionRadioButtons.remove(radioButton);
        optionFields.remove(optionField);
        
        // Update option labels
        updateOptionLabels();
    }
    
    /**
     * Updates the option labels (A, B, C, etc.) after an option is removed.
     */
    private void updateOptionLabels() {
        for (int i = 0; i < optionsContainer.getChildren().size(); i++) {
            HBox optionBox = (HBox) optionsContainer.getChildren().get(i);
            Label optionLabel = (Label) optionBox.getChildren().get(0);
            optionLabel.setText(String.valueOf((char)('A' + i)));
        }
    }
    
    /**
     * Adds an answer to the answers list.
     */
    private void addAnswer() {
        String answer = contributionAnswerField.getText().trim();
        if (answer.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Empty Answer", 
                    "Please enter an answer before adding it.");
            return;
        }
        
        contributionAnswersList.getItems().add(answer);
        contributionAnswerField.clear();
        contributionAnswerField.requestFocus();
    }
    
    /**
     * Validates the contribution form.
     * 
     * @return True if the form is valid, false otherwise
     */
    private boolean validateContributionForm() {
        // Check unit
        if (contributionUnitComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Missing Unit", 
                    "Please select a unit for the question.");
            return false;
        }
        
        // Check topic
        if (contributionTopicComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Missing Topic", 
                    "Please select a topic for the question.");
            return false;
        }
        
        // Check question text
        if (contributionQuestionTextField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Missing Question", 
                    "Please enter the question text.");
            return false;
        }
        
        // Check question type specific validation
        String questionType = contributionQuestionTypeComboBox.getValue();
        if (Question.TYPE_MULTIPLE_CHOICE.equals(questionType)) {
            // Check options
            if (optionFields.size() < 2) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Insufficient Options", 
                        "Multiple choice questions must have at least 2 options.");
                return false;
            }
            
            // Check if all options have text
            for (int i = 0; i < optionFields.size(); i++) {
                if (optionFields.get(i).getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Empty Option", 
                            "Option " + (i + 1) + " is empty. Please enter text for all options.");
                    return false;
                }
            }
            
            // Check if a correct answer is selected
            if (optionsToggleGroup.getSelectedToggle() == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Missing Correct Answer", 
                        "Please select the correct answer by clicking one of the radio buttons.");
                return false;
            }
        } else if (Question.TYPE_LIST_BASED.equals(questionType)) {
            // Check correct answers
            if (contributionAnswersList.getItems().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Missing Correct Answers", 
                        "Please specify at least one correct answer.");
                return false;
            }
        } else {
            // Short answer
            if (contributionAnswersList.getItems().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Missing Correct Answer", 
                        "Please specify the correct answer.");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Submits a new question.
     */
    private void submitQuestion() {
        if (!validateContributionForm()) {
            return;
        }
        
        try {
            // Get form values
            Unit unit = contributionUnitComboBox.getValue();
            String questionType = contributionQuestionTypeComboBox.getValue();
            String difficulty = contributionDifficultyComboBox.getValue();
            Topic selectedTopic = contributionTopicComboBox.getValue();
            String questionText = contributionQuestionTextField.getText().trim();
            int marks = contributionMarksSpinner.getValue();
            
            // Get options and correct answers
            List<String> options = new ArrayList<>();
            List<String> correctAnswers = new ArrayList<>();
            
            if (Question.TYPE_MULTIPLE_CHOICE.equals(questionType)) {
                // Get all options
                for (TextField field : optionFields) {
                    options.add(field.getText().trim());
                }
                
                // Get the selected correct answer
                int selectedIndex = optionRadioButtons.indexOf(optionsToggleGroup.getSelectedToggle());
                if (selectedIndex >= 0) {
                    correctAnswers.add(optionFields.get(selectedIndex).getText().trim());
                }
            } else {
                // For other question types, use the answers list
                correctAnswers.addAll(contributionAnswersList.getItems());
            }
            
            // Create the question
            Question createdQuestion = QuestionService.createQuestion(
                questionText, questionType, options, correctAnswers, 
                marks, unit.getUnitCode(), currentStudent.getUserId(), false,
                selectedTopic.getTopicName()
            );
            
            if (createdQuestion != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Question Submitted", 
                        "Your question has been successfully submitted.");
                clearContributionForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Submission Failed", 
                        "Failed to submit the question. Please try again.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting question", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Submission Error", 
                    "An error occurred while submitting the question: " + e.getMessage());
        }
    }
    
    /**
     * Clears the contribution form.
     */
    private void clearContributionForm() {
        contributionUnitComboBox.setValue(null);
        contributionQuestionTypeComboBox.setValue(Question.TYPE_MULTIPLE_CHOICE);
        contributionDifficultyComboBox.setValue(Question.DIFFICULTY_MEDIUM);
        contributionTopicComboBox.setValue(null);
        contributionQuestionTextField.clear();
        contributionMarksSpinner.getValueFactory().setValue(5);
        
        // Clear MCQ options
        optionsContainer.getChildren().clear();
        optionFields.clear();
        optionRadioButtons.clear();
        optionsToggleGroup.selectToggle(null);
        
        // Clear answers list
        contributionAnswersList.getItems().clear();
        contributionAnswerField.clear();
        
        // Re-initialize the form based on the question type
        updateContributionFormFields(Question.TYPE_MULTIPLE_CHOICE);
    }
    
    /**
     * Updates the contribution unit combobox with the student's enrolled units.
     */
    private void updateContributionUnitComboBox() {
        try {
            if (enrolledUnits == null || enrolledUnits.isEmpty()) {
                return;
            }
            
            // Clear existing items
            contributionUnitComboBox.getItems().clear();
            
            // Add enrolled units
            contributionUnitComboBox.getItems().addAll(enrolledUnits);
            
            // Set cell factory to display unit code and name
            contributionUnitComboBox.setCellFactory(new Callback<ListView<Unit>, ListCell<Unit>>() {
                @Override
                public ListCell<Unit> call(ListView<Unit> param) {
                    return new ListCell<Unit>() {
                        @Override
                        protected void updateItem(Unit item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText(null);
                            } else {
                                setText(item.getUnitCode() + " - " + item.getUnitName());
                            }
                        }
                    };
                }
            });
            
            // Set button cell to display unit code and name
            contributionUnitComboBox.setButtonCell(new ListCell<Unit>() {
                @Override
                protected void updateItem(Unit item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getUnitCode() + " - " + item.getUnitName());
                    }
                }
            });
            
            LOGGER.log(Level.INFO, "Contribution unit combobox updated with {0} units", enrolledUnits.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating contribution unit combobox", e);
        }
    }
    
    /**
     * Sets up the date and time display with a timeline animation.
     */
    private void setupDateTimeDisplay() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    
    /**
     * Shows an alert dialog.
     *
     * @param type    The alert type
     * @param title   The alert title
     * @param header  The alert header
     * @param content The alert content
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Hides all content sections in the dashboard.
     * This method is called before showing a specific section.
     */
    private void hideAllContent() {
        dashboardContent.setVisible(false);
        assessmentsContent.setVisible(false);
        practiceContent.setVisible(false);
        contributionsContent.setVisible(false);
        analyticsContent.setVisible(false);
        settingsContent.setVisible(false);
    }
    
    /**
     * Updates the navigation icons to highlight the active section.
     * 
     * @param activeIcon The icon of the active section
     */
    private void updateNavigationIcons(FontIcon activeIcon) {
        // Reset all icons to default style
        homeIcon.getStyleClass().remove("active-icon");
        assessmentIcon.getStyleClass().remove("active-icon");
        practiceIcon.getStyleClass().remove("active-icon");
        contributionIcon.getStyleClass().remove("active-icon");
        analyticsIcon.getStyleClass().remove("active-icon");
        settingsIcon.getStyleClass().remove("active-icon");
        logoutIcon.getStyleClass().remove("active-icon");
        
        // Add active style to the selected icon
        activeIcon.getStyleClass().add("active-icon");
    }
    
    /**
     * Shows the settings section.
     * 
     * @param event The mouse event
     */
    @FXML
    private void showSettings(MouseEvent event) {
        hideAllContent();
        settingsContent.setVisible(true);
        updateNavigationIcons(settingsIcon);
        populateSettingsForm();
    }
    
    /**
     * Shows the contributions section.
     * 
     * @param event The mouse event
     */
    @FXML
    private void showContributions(MouseEvent event) {
        hideAllContent();
        contributionsContent.setVisible(true);
        updateNavigationIcons(contributionIcon);
        updateContributionUnitComboBox();
    }
    
    /**
     * Handles the logout action when the logout icon is clicked.
     * 
     * @param event The mouse event
     */
    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout Confirmation");
            alert.setHeaderText("Logout from Kenyatta CAT System");
            alert.setContentText("Are you sure you want to logout?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Get the current stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                // Load the login view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/login-view.fxml"));
                Parent root = loader.load();
                
                // Create a new scene
                Scene scene = new Scene(root);
                
                // Add the CSS stylesheet
                scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/main.css").toExternalForm());
                
                // Set the scene to the stage
                currentStage.setScene(scene);
                currentStage.setTitle("Kenyatta CAT System - Login");
                currentStage.centerOnScreen();
                
                LOGGER.log(Level.INFO, "User logged out successfully");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during logout", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Logout Failed", 
                    "An error occurred while trying to logout. Please try again.");
        }
    }
}