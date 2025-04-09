package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.Assessment;
import book.javafx.kenyattacatsystem.models.Lecturer;
import book.javafx.kenyattacatsystem.models.Unit;
import book.javafx.kenyattacatsystem.services.AssessmentService;
import book.javafx.kenyattacatsystem.services.PerformanceAnalyticsService;
import book.javafx.kenyattacatsystem.services.UnitService;
import book.javafx.kenyattacatsystem.utils.AlertUtil;
import book.javafx.kenyattacatsystem.utils.ChartUtil;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the lecturer performance analytics view.
 */
public class LecturerPerformanceController {
    private static final Logger LOGGER = Logger.getLogger(LecturerPerformanceController.class.getName());
    
    @FXML private TabPane performanceTabPane;
    
    // Unit selection
    @FXML private ComboBox<String> unitComboBox;
    
    // Overview tab
    @FXML private Label averageScoreLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalAssessmentsLabel;
    @FXML private PieChart assessmentTypeDistributionChart;
    @FXML private BarChart<String, Number> assessmentPerformanceChart;
    
    // Student Performance tab
    @FXML private TableView<Map<String, Object>> studentPerformanceTable;
    @FXML private TableColumn<Map<String, Object>, String> studentNameColumn;
    @FXML private TableColumn<Map<String, Object>, Number> studentAssessmentCountColumn;
    @FXML private TableColumn<Map<String, Object>, Number> studentAvgScoreColumn;
    @FXML private BarChart<String, Number> topStudentsChart;
    
    // Assessment Details tab
    @FXML private ComboBox<Map<String, Object>> assessmentComboBox;
    @FXML private Label assessmentTitleLabel;
    @FXML private Label assessmentAvgScoreLabel;
    @FXML private Label assessmentHighScoreLabel;
    @FXML private Label assessmentLowScoreLabel;
    @FXML private Label assessmentStudentCountLabel;
    @FXML private TableView<Map<String, Object>> assessmentQuestionsTable;
    @FXML private TableColumn<Map<String, Object>, String> questionTextColumn;
    @FXML private TableColumn<Map<String, Object>, String> questionTypeColumn;
    @FXML private TableColumn<Map<String, Object>, Double> questionDifficultyColumn;
    @FXML private TableColumn<Map<String, Object>, Double> successRateColumn;
    @FXML private BarChart<String, Number> questionPerformanceChart;
    
    private Lecturer currentLecturer;
    private PerformanceAnalyticsService performanceService;
    private Map<String, Object> currentUnitPerformance;
    private Map<String, Object> currentAssessmentPerformance;
    
    /**
     * Initialize the controller.
     */
    @FXML
    private void initialize() {
        // Initialize service
        performanceService = new PerformanceAnalyticsService();
        
        // Make sure the root node fills the available space
        AnchorPane root = (AnchorPane) performanceTabPane.getParent().getParent();
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up listeners
        unitComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadUnitPerformanceData(newValue);
                    }
                });
        
        assessmentComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        String assessmentId = (String) newValue.get("assessmentId");
                        loadAssessmentPerformance(assessmentId);
                    }
                });
    }
    
    /**
     * Set up the table columns.
     */
    private void setupTableColumns() {
        // Student performance table
        studentNameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                (String) data.getValue().get("fullName")));
        
        studentAssessmentCountColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                (Integer) data.getValue().get("assessmentCount")));
        
        studentAvgScoreColumn.setCellValueFactory(data -> new SimpleDoubleProperty(
                (Double) data.getValue().get("avgPercentage")));
        
        // Assessment questions table
        questionTextColumn.setCellValueFactory(data -> new SimpleStringProperty(
                (String) data.getValue().get("questionText")));
        
        questionTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                (String) data.getValue().get("type")));
        
        questionDifficultyColumn.setCellValueFactory(data -> new SimpleDoubleProperty(
                (Double) data.getValue().get("difficulty")).asObject());
        
        successRateColumn.setCellValueFactory(data -> new SimpleDoubleProperty(
                (Double) data.getValue().get("successRate")).asObject());
    }
    
    /**
     * Sets the current lecturer for this controller.
     * 
     * @param lecturer The lecturer to set
     */
    public void setCurrentLecturer(Lecturer lecturer) {
        this.currentLecturer = lecturer;
        loadPerformanceData();
    }
    
    /**
     * Load performance data for the current lecturer.
     * This method is called after setting the current lecturer.
     */
    public void loadPerformanceData() {
        if (currentLecturer == null) {
            LOGGER.log(Level.WARNING, "Cannot load performance data: current lecturer is null");
            AlertUtil.showError("Error", "Current lecturer information is missing.");
            return;
        }
        
        try {
            // Test database connection by attempting to get a connection
            boolean connectionOk = false;
            try {
                DatabaseUtil.getConnection();
                connectionOk = true;
                LOGGER.log(Level.INFO, "Database connection test successful");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Database connection test failed", e);
                connectionOk = false;
            }
            
            if (!connectionOk) {
                LOGGER.log(Level.SEVERE, "Database connection test failed");
                AlertUtil.showError("Database Error", "Please check your database connection and try again.");
                return;
            }
            
            LOGGER.log(Level.INFO, "Loading performance data for lecturer: " + currentLecturer.getFullName() + " (ID: " + currentLecturer.getUserId() + ")");
            
            // First, get the lecturer's staff ID from the lecturers table
            String staffId = null;
            String getStaffIdQuery = "SELECT staff_id FROM lecturers WHERE user_id = ?";
            try (PreparedStatement staffStmt = DatabaseUtil.prepareStatement(getStaffIdQuery)) {
                staffStmt.setString(1, currentLecturer.getUserId());
                ResultSet staffRs = staffStmt.executeQuery();
                if (staffRs.next()) {
                    staffId = staffRs.getString("staff_id");
                    LOGGER.log(Level.INFO, "Found staff ID: " + staffId + " for lecturer ID: " + currentLecturer.getUserId());
                } else {
                    LOGGER.log(Level.WARNING, "No staff ID found for lecturer: " + currentLecturer.getUserId());
                    
                    // Create a staff ID if not found
                    staffId = "STAFF" + currentLecturer.getUserId().substring(3); // Convert from LEC#### to STAFF####
                    LOGGER.log(Level.INFO, "Creating new staff ID: " + staffId);
                    
                    String insertStaffQuery = "INSERT INTO lecturers (staff_id, user_id) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = DatabaseUtil.prepareStatement(insertStaffQuery)) {
                        insertStmt.setString(1, staffId);
                        insertStmt.setString(2, currentLecturer.getUserId());
                        insertStmt.executeUpdate();
                        LOGGER.log(Level.INFO, "Created new staff ID record in lecturers table");
                    }
                }
            }
            
            if (staffId == null) {
                LOGGER.log(Level.SEVERE, "Could not determine staff ID for lecturer");
                AlertUtil.showError("Error", "Could not determine staff ID for lecturer.");
                return;
            }
            
            // Get lecturer's units using the staff ID instead of user ID
            List<Unit> units = new ArrayList<>();
            String query = "SELECT * FROM units WHERE lecturer_id = ? ORDER BY unit_code";
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
                    LOGGER.log(Level.INFO, "Found unit: " + unit.getUnitCode() + " - " + unit.getUnitName());
                }
            }
            
            if (units.isEmpty()) {
                LOGGER.log(Level.WARNING, "No units found for lecturer with staff ID: " + staffId);
                
                // Check if there are any units in the database at all
                try {
                    String checkUnitsQuery = "SELECT COUNT(*) FROM units";
                    try (PreparedStatement stmt = DatabaseUtil.getConnection().prepareStatement(checkUnitsQuery)) {
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            int unitCount = rs.getInt(1);
                            LOGGER.log(Level.INFO, "Total units in database: " + unitCount);
                        }
                    }
                    
                    // Check the lecturer_id format in the units table
                    String checkLecturerUnitsQuery = "SELECT unit_code, lecturer_id FROM units LIMIT 10";
                    try (PreparedStatement stmt = DatabaseUtil.getConnection().prepareStatement(checkLecturerUnitsQuery)) {
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            String unitCode = rs.getString("unit_code");
                            String lecturerId = rs.getString("lecturer_id");
                            LOGGER.log(Level.INFO, "Unit: " + unitCode + ", Lecturer ID: " + lecturerId);
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error checking units in database", e);
                }
                
                AlertUtil.showWarning("No Units Found", "No units are assigned to this lecturer.");
                return;
            }
            
            LOGGER.log(Level.INFO, "Found " + units.size() + " units for lecturer: " + currentLecturer.getFullName());
            
            List<String> unitCodes = new ArrayList<>();
            for (Unit unit : units) {
                unitCodes.add(unit.getUnitCode());
                LOGGER.log(Level.INFO, "Unit found: " + unit.getUnitCode() + " - " + unit.getUnitName());
            }
            
            unitComboBox.setItems(FXCollections.observableArrayList(unitCodes));
            unitComboBox.getSelectionModel().selectFirst();
            
            // Load initial data for the first unit
            if (!unitCodes.isEmpty()) {
                String selectedUnit = unitComboBox.getValue();
                loadUnitPerformanceData(selectedUnit);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading performance data", e);
            AlertUtil.showError("Error", "Failed to load performance data: " + e.getMessage());
        }
    }
    
    /**
     * Load performance data for a specific unit.
     *
     * @param unitCode The unit code
     */
    private void loadUnitPerformanceData(String unitCode) {
        try {
            LOGGER.log(Level.INFO, "Loading performance data for unit: " + unitCode);
            
            // Get unit performance data
            currentUnitPerformance = performanceService.getUnitPerformance(unitCode);
            
            if (currentUnitPerformance == null) {
                LOGGER.log(Level.WARNING, "Received null performance data for unit: " + unitCode);
                AlertUtil.showError("Data Error", "Failed to retrieve performance data");
                return;
            }
            
            // Update UI components with the retrieved data
            updateOverviewTab();
            updateStudentPerformanceTab();
            updateAssessmentComboBox();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading unit performance data", e);
            AlertUtil.showError("Error", "Failed to load unit performance data: " + e.getMessage());
        }
    }
    
    /**
     * Load assessment performance data.
     * 
     * @param assessmentId The ID of the assessment
     */
    private void loadAssessmentPerformance(String assessmentId) {
        try {
            LOGGER.log(Level.INFO, "Loading assessment performance data for assessment: " + assessmentId);
            
            // Get assessment performance data
            currentAssessmentPerformance = performanceService.getAssessmentPerformance(assessmentId, null);
            
            if (currentAssessmentPerformance != null) {
                LOGGER.log(Level.INFO, "Received performance data for assessment: " + assessmentId);
                updateAssessmentDetailsTab();
            } else {
                LOGGER.log(Level.WARNING, "Received null performance data for assessment: " + assessmentId);
                AlertUtil.showWarning("No Data", "No performance data available for this assessment.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading assessment performance data", e);
            AlertUtil.showError("Error", "Failed to load assessment performance data: " + e.getMessage());
        }
    }
    
    /**
     * Update the overview tab with performance data.
     */
    private void updateOverviewTab() {
        if (currentUnitPerformance == null) {
            setDefaultOverviewValues();
            return;
        }
        
        try {
            // Get assessment performance data
            List<Map<String, Object>> assessmentPerformance = 
                    (List<Map<String, Object>>) currentUnitPerformance.get("assessmentPerformance");
            
            if (assessmentPerformance == null || assessmentPerformance.isEmpty()) {
                setDefaultOverviewValues();
                return;
            }
            
            // Calculate overall metrics
            int totalAssessments = assessmentPerformance.size();
            int formalCount = 0;
            int practiceCount = 0;
            double totalAvgScore = 0;
            
            for (Map<String, Object> assessment : assessmentPerformance) {
                Boolean isPractice = (Boolean) assessment.get("isPractice");
                Double avgPercentage = (Double) assessment.get("avgPercentage");
                
                if (avgPercentage != null && !avgPercentage.isNaN()) {
                    totalAvgScore += avgPercentage;
                    
                    if (isPractice != null && isPractice) {
                        practiceCount++;
                    } else {
                        formalCount++;
                    }
                }
            }
            
            // Calculate average score
            double overallAvgScore = 0;
            if (totalAssessments > 0) {
                overallAvgScore = totalAvgScore / totalAssessments;
            }
            
            // Get student count
            List<Map<String, Object>> studentPerformance = 
                    (List<Map<String, Object>>) currentUnitPerformance.get("studentPerformance");
            int studentCount = (studentPerformance != null) ? studentPerformance.size() : 0;
            
            // Update UI components
            averageScoreLabel.setText(String.format("%.1f%%", overallAvgScore));
            totalStudentsLabel.setText(String.valueOf(studentCount));
            totalAssessmentsLabel.setText(String.valueOf(totalAssessments));
            
            // Update assessment type distribution chart
            updateAssessmentTypeDistributionChart(formalCount, practiceCount);
            
            // Update assessment performance chart
            updateAssessmentPerformanceChart(assessmentPerformance);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating overview tab", e);
            setDefaultOverviewValues();
        }
    }
    
    /**
     * Set default values for the overview tab.
     */
    private void setDefaultOverviewValues() {
        averageScoreLabel.setText("0.0%");
        totalStudentsLabel.setText("0");
        totalAssessmentsLabel.setText("0");
        
        assessmentTypeDistributionChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("No Data", 1)
        ));
        
        assessmentPerformanceChart.getData().clear();
    }
    
    /**
     * Update the assessment type distribution chart.
     *
     * @param formalCount The number of formal assessments
     * @param practiceCount The number of practice assessments
     */
    private void updateAssessmentTypeDistributionChart(int formalCount, int practiceCount) {
        assessmentTypeDistributionChart.getData().clear();
        
        if (formalCount == 0 && practiceCount == 0) {
            assessmentTypeDistributionChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Assessments", 1)
            ));
            return;
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        if (formalCount > 0) {
            pieChartData.add(new PieChart.Data("Formal (" + formalCount + ")", formalCount));
        }
        
        if (practiceCount > 0) {
            pieChartData.add(new PieChart.Data("Practice (" + practiceCount + ")", practiceCount));
        }
        
        assessmentTypeDistributionChart.setData(pieChartData);
    }
    
    /**
     * Update the assessment performance chart.
     *
     * @param assessmentPerformance The assessment performance data
     */
    private void updateAssessmentPerformanceChart(List<Map<String, Object>> assessmentPerformance) {
        assessmentPerformanceChart.getData().clear();
        
        if (assessmentPerformance == null || assessmentPerformance.isEmpty()) {
            return;
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Score (%)");
        
        // Sort assessments by average percentage (descending)
        List<Map<String, Object>> sortedAssessments = new ArrayList<>(assessmentPerformance);
        sortedAssessments.sort((a1, a2) -> {
            Double p1 = (Double) a1.get("avgPercentage");
            Double p2 = (Double) a2.get("avgPercentage");
            
            if (p1 == null || p1.isNaN()) p1 = 0.0;
            if (p2 == null || p2.isNaN()) p2 = 0.0;
            
            return p2.compareTo(p1);
        });
        
        // Add top 10 assessments to the chart
        int count = 0;
        for (Map<String, Object> assessment : sortedAssessments) {
            if (count >= 10) break;
            
            String title = (String) assessment.get("title");
            Double avgPercentage = (Double) assessment.get("avgPercentage");
            
            if (title != null && avgPercentage != null && !avgPercentage.isNaN()) {
                // Truncate long titles
                String shortTitle = title.length() > 15 ? title.substring(0, 12) + "..." : title;
                
                series.getData().add(new XYChart.Data<>(shortTitle, avgPercentage));
                count++;
            }
        }
        
        assessmentPerformanceChart.getData().add(series);
        
        // Add custom styling
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            
            // Color bars based on score
            double score = data.getYValue().doubleValue();
            if (score >= 80) {
                node.setStyle("-fx-bar-fill: #2ecc71;"); // Green for high scores
            } else if (score >= 60) {
                node.setStyle("-fx-bar-fill: #f39c12;"); // Orange for medium scores
            } else {
                node.setStyle("-fx-bar-fill: #e74c3c;"); // Red for low scores
            }
        }
    }
    
    /**
     * Update the student performance tab.
     */
    private void updateStudentPerformanceTab() {
        if (currentUnitPerformance == null) {
            studentPerformanceTable.setItems(FXCollections.observableArrayList());
            topStudentsChart.getData().clear();
            return;
        }
        
        try {
            // Get student performance data
            List<Map<String, Object>> studentPerformance = 
                    (List<Map<String, Object>>) currentUnitPerformance.get("studentPerformance");
            
            if (studentPerformance == null || studentPerformance.isEmpty()) {
                studentPerformanceTable.setItems(FXCollections.observableArrayList());
                topStudentsChart.getData().clear();
                return;
            }
            
            // Update student performance table
            studentPerformanceTable.setItems(FXCollections.observableArrayList(studentPerformance));
            
            // Update top students chart
            updateTopStudentsChart(studentPerformance);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating student performance tab", e);
            studentPerformanceTable.setItems(FXCollections.observableArrayList());
            topStudentsChart.getData().clear();
        }
    }
    
    /**
     * Update the top students chart.
     *
     * @param studentPerformance The student performance data
     */
    private void updateTopStudentsChart(List<Map<String, Object>> studentPerformance) {
        topStudentsChart.getData().clear();
        
        if (studentPerformance == null || studentPerformance.isEmpty()) {
            return;
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Score (%)");
        
        // Sort students by average percentage (descending)
        List<Map<String, Object>> sortedStudents = new ArrayList<>(studentPerformance);
        sortedStudents.sort((s1, s2) -> {
            Double p1 = (Double) s1.get("avgPercentage");
            Double p2 = (Double) s2.get("avgPercentage");
            
            if (p1 == null || p1.isNaN()) p1 = 0.0;
            if (p2 == null || p2.isNaN()) p2 = 0.0;
            
            return p2.compareTo(p1);
        });
        
        // Add top 10 students to the chart
        int count = 0;
        for (Map<String, Object> student : sortedStudents) {
            if (count >= 10) break;
            
            String fullName = (String) student.get("fullName");
            Double avgPercentage = (Double) student.get("avgPercentage");
            Integer assessmentCount = (Integer) student.get("assessmentCount");
            
            if (fullName != null && avgPercentage != null && !avgPercentage.isNaN() && 
                    assessmentCount != null && assessmentCount > 0) {
                series.getData().add(new XYChart.Data<>(fullName, avgPercentage));
                count++;
            }
        }
        
        topStudentsChart.getData().add(series);
        
        // Add custom styling
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            
            // Color bars based on score
            double score = data.getYValue().doubleValue();
            if (score >= 80) {
                node.setStyle("-fx-bar-fill: #2ecc71;"); // Green for high scores
            } else if (score >= 60) {
                node.setStyle("-fx-bar-fill: #f39c12;"); // Orange for medium scores
            } else {
                node.setStyle("-fx-bar-fill: #e74c3c;"); // Red for low scores
            }
        }
    }
    
    /**
     * Update the assessment combo box.
     */
    private void updateAssessmentComboBox() {
        if (currentUnitPerformance == null) {
            assessmentComboBox.setItems(FXCollections.observableArrayList());
            return;
        }
        
        try {
            // Get assessment performance data
            List<Map<String, Object>> assessmentPerformance = 
                    (List<Map<String, Object>>) currentUnitPerformance.get("assessmentPerformance");
            
            if (assessmentPerformance == null || assessmentPerformance.isEmpty()) {
                assessmentComboBox.setItems(FXCollections.observableArrayList());
                return;
            }
            
            // Create list of assessments for the combo box
            List<Map<String, Object>> assessments = new ArrayList<>();
            
            for (Map<String, Object> assessment : assessmentPerformance) {
                Map<String, Object> assessmentMap = new HashMap<>();
                assessmentMap.put("assessmentId", assessment.get("assessmentId"));
                assessmentMap.put("title", assessment.get("title"));
                assessmentMap.put("isPractice", assessment.get("isPractice"));
                
                assessments.add(assessmentMap);
            }
            
            // Set up the combo box
            assessmentComboBox.setItems(FXCollections.observableArrayList(assessments));
            
            // Set up the cell factory to display the assessment title
            assessmentComboBox.setCellFactory(param -> new ListCell<Map<String, Object>>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String title = (String) item.get("title");
                        Boolean isPractice = (Boolean) item.get("isPractice");
                        
                        setText(title + (isPractice != null && isPractice ? " (Practice)" : ""));
                    }
                }
            });
            
            // Set up the button cell to display the selected assessment title
            assessmentComboBox.setButtonCell(new ListCell<Map<String, Object>>() {
                @Override
                protected void updateItem(Map<String, Object> item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String title = (String) item.get("title");
                        Boolean isPractice = (Boolean) item.get("isPractice");
                        
                        setText(title + (isPractice != null && isPractice ? " (Practice)" : ""));
                    }
                }
            });
            
            // Select the first assessment
            if (!assessments.isEmpty()) {
                assessmentComboBox.getSelectionModel().selectFirst();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating assessment combo box", e);
            assessmentComboBox.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Update the assessment details tab.
     */
    private void updateAssessmentDetailsTab() {
        if (currentAssessmentPerformance == null) {
            setDefaultAssessmentDetailsValues();
            return;
        }
        
        try {
            // Update assessment details labels
            String title = (String) currentAssessmentPerformance.get("title");
            Double avgScore = (Double) currentAssessmentPerformance.get("avgScore");
            Double highestScore = (Double) currentAssessmentPerformance.get("highestScore");
            Double lowestScore = (Double) currentAssessmentPerformance.get("lowestScore");
            Integer studentCount = (Integer) currentAssessmentPerformance.get("studentCount");
            
            assessmentTitleLabel.setText(title != null ? title : "-");
            assessmentAvgScoreLabel.setText(avgScore != null ? String.format("%.1f%%", avgScore) : "0.0%");
            assessmentHighScoreLabel.setText(highestScore != null ? String.format("%.1f%%", highestScore) : "0.0%");
            assessmentLowScoreLabel.setText(lowestScore != null ? String.format("%.1f%%", lowestScore) : "0.0%");
            assessmentStudentCountLabel.setText(studentCount != null ? String.valueOf(studentCount) : "0");
            
            // Update question performance table
            List<Map<String, Object>> questionPerformance = 
                    (List<Map<String, Object>>) currentAssessmentPerformance.get("questionPerformance");
            
            if (questionPerformance != null && !questionPerformance.isEmpty()) {
                assessmentQuestionsTable.setItems(FXCollections.observableArrayList(questionPerformance));
                
                // Update question performance chart
                updateQuestionPerformanceChart(questionPerformance);
            } else {
                assessmentQuestionsTable.setItems(FXCollections.observableArrayList());
                questionPerformanceChart.getData().clear();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating assessment details tab", e);
            setDefaultAssessmentDetailsValues();
        }
    }
    
    /**
     * Set default values for the assessment details tab.
     */
    private void setDefaultAssessmentDetailsValues() {
        assessmentTitleLabel.setText("-");
        assessmentAvgScoreLabel.setText("0.0%");
        assessmentHighScoreLabel.setText("0.0%");
        assessmentLowScoreLabel.setText("0.0%");
        assessmentStudentCountLabel.setText("0");
        
        assessmentQuestionsTable.setItems(FXCollections.observableArrayList());
        questionPerformanceChart.getData().clear();
    }
    
    /**
     * Update the question performance chart.
     *
     * @param questionPerformance The question performance data
     */
    private void updateQuestionPerformanceChart(List<Map<String, Object>> questionPerformance) {
        questionPerformanceChart.getData().clear();
        
        if (questionPerformance == null || questionPerformance.isEmpty()) {
            return;
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Success Rate (%)");
        
        // Sort questions by success rate (descending)
        List<Map<String, Object>> sortedQuestions = new ArrayList<>(questionPerformance);
        sortedQuestions.sort((q1, q2) -> {
            Double r1 = (Double) q1.get("successRate");
            Double r2 = (Double) q2.get("successRate");
            
            if (r1 == null || r1.isNaN()) r1 = 0.0;
            if (r2 == null || r2.isNaN()) r2 = 0.0;
            
            return r2.compareTo(r1);
        });
        
        // Add questions to the chart
        for (Map<String, Object> question : sortedQuestions) {
            String text = (String) question.get("questionText");
            Double successRate = (Double) question.get("successRate");
            
            if (text != null && successRate != null && !successRate.isNaN()) {
                // Truncate long question text for chart display
                String shortText = text.length() > 30 ? text.substring(0, 27) + "..." : text;
                
                series.getData().add(new XYChart.Data<>(shortText, successRate));
            }
        }
        
        questionPerformanceChart.getData().add(series);
        
        // Add custom styling
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            
            // Color bars based on success rate
            double rate = data.getYValue().doubleValue();
            if (rate >= 80) {
                node.setStyle("-fx-bar-fill: #2ecc71;"); // Green for high success rates
            } else if (rate >= 60) {
                node.setStyle("-fx-bar-fill: #f39c12;"); // Orange for medium success rates
            } else {
                node.setStyle("-fx-bar-fill: #e74c3c;"); // Red for low success rates
            }
        }
    }
}
