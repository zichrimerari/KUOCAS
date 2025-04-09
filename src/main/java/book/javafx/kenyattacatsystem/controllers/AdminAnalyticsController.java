package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.Admin;
import book.javafx.kenyattacatsystem.models.Report;
import book.javafx.kenyattacatsystem.models.Unit;
import book.javafx.kenyattacatsystem.services.PerformanceAnalyticsService;
import book.javafx.kenyattacatsystem.services.ReportService;
import book.javafx.kenyattacatsystem.services.UnitService;
import book.javafx.kenyattacatsystem.services.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the admin analytics dashboard.
 */
public class AdminAnalyticsController {
    private static final Logger LOGGER = Logger.getLogger(AdminAnalyticsController.class.getName());
    
    // Filter controls
    @FXML private ComboBox<String> departmentFilterComboBox;
    @FXML private ComboBox<String> unitFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button applyFiltersButton;
    
    // System Overview tab
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalLecturersLabel;
    @FXML private Label totalUnitsLabel;
    @FXML private Label totalAssessmentsLabel;
    @FXML private Label avgSystemScoreLabel;
    @FXML private PieChart userDistributionChart;
    @FXML private LineChart<String, Number> systemActivityChart;
    
    // Department Performance tab
    @FXML private TableView<Map<String, Object>> departmentPerformanceTable;
    @FXML private TableColumn<Map<String, Object>, String> departmentNameColumn;
    @FXML private TableColumn<Map<String, Object>, Number> departmentStudentCountColumn;
    @FXML private TableColumn<Map<String, Object>, Number> departmentUnitCountColumn;
    @FXML private TableColumn<Map<String, Object>, Number> departmentAvgScoreColumn;
    @FXML private BarChart<String, Number> departmentPerformanceChart;
    
    // Unit Performance tab
    @FXML private TableView<Map<String, Object>> unitPerformanceTable;
    @FXML private TableColumn<Map<String, Object>, String> unitCodeColumn;
    @FXML private TableColumn<Map<String, Object>, String> unitNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> unitDepartmentColumn;
    @FXML private TableColumn<Map<String, Object>, Number> unitStudentCountColumn;
    @FXML private TableColumn<Map<String, Object>, Number> unitAvgScoreColumn;
    @FXML private BarChart<String, Number> unitPerformanceChart;
    
    // Student Performance tab
    @FXML private TableView<Map<String, Object>> studentPerformanceTable;
    @FXML private TableColumn<Map<String, Object>, String> studentNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> studentDepartmentColumn;
    @FXML private TableColumn<Map<String, Object>, Number> studentAssessmentCountColumn;
    @FXML private TableColumn<Map<String, Object>, Number> studentAvgScoreColumn;
    @FXML private BarChart<String, Number> studentPerformanceChart;
    
    // Reports tab
    @FXML private ComboBox<Report> reportTypeComboBox;
    @FXML private ComboBox<String> reportFormatComboBox;
    @FXML private Button generateReportButton;
    @FXML private TableView<Map<String, Object>> recentReportsTable;
    @FXML private TableColumn<Map<String, Object>, String> reportNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> reportGeneratedByColumn;
    @FXML private TableColumn<Map<String, Object>, String> reportDateColumn;
    @FXML private TableColumn<Map<String, Object>, Button> reportActionsColumn;
    
    private Admin currentAdmin;
    private Map<String, Object> analyticsData;
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private String filterDepartment;
    private String filterUnit;
    
    /**
     * Initialize the controller.
     */
    @FXML
    private void initialize() {
        // Initialize date pickers with default values (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        
        // Initialize filter combo boxes
        departmentFilterComboBox.getItems().add("All Departments");
        unitFilterComboBox.getItems().add("All Units");
        
        // Set up table columns
        setupTableColumns();
        
        // Set up report format combo box
        reportFormatComboBox.setItems(FXCollections.observableArrayList(
                "PDF", "Excel", "CSV", "HTML"
        ));
        reportFormatComboBox.getSelectionModel().selectFirst();
        
        // Set up listeners
        applyFiltersButton.setOnAction(event -> applyFilters());
        generateReportButton.setOnAction(event -> generateReport());
    }
    
    /**
     * Set up the table columns.
     */
    private void setupTableColumns() {
        // Department performance table
        departmentNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("departmentName")));
        
        departmentStudentCountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                (Integer) data.getValue().get("studentCount")));
        
        departmentUnitCountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                (Integer) data.getValue().get("unitCount")));
        
        departmentAvgScoreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                (Double) data.getValue().get("avgScore")));
        
        // Unit performance table
        unitCodeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("unitCode")));
        
        unitNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("unitName")));
        
        unitDepartmentColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("department")));
        
        unitStudentCountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                (Integer) data.getValue().get("studentCount")));
        
        unitAvgScoreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                (Double) data.getValue().get("avgScore")));
        
        // Student performance table
        studentNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("fullName")));
        
        studentDepartmentColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("department")));
        
        studentAssessmentCountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                (Integer) data.getValue().get("assessmentCount")));
        
        studentAvgScoreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                (Double) data.getValue().get("avgScore")));
        
        // Recent reports table
        reportNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("reportName")));
        
        reportGeneratedByColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("generatedBy")));
        
        reportDateColumn.setCellValueFactory(data -> {
            LocalDateTime dateTime = (LocalDateTime) data.getValue().get("generatedDate");
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        
        reportActionsColumn.setCellFactory(param -> new TableCell<Map<String, Object>, Button>() {
            private final Button viewButton = new Button("View");
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                    
                    viewButton.setOnAction(event -> {
                        Map<String, Object> report = getTableView().getItems().get(getIndex());
                        String filePath = (String) report.get("filePath");
                        
                        if (filePath != null && !filePath.isEmpty()) {
                            try {
                                File file = new File(filePath);
                                if (file.exists()) {
                                    // Open the report file with the default system application
                                    java.awt.Desktop.getDesktop().open(file);
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Error", "File Not Found", 
                                            "The report file could not be found.");
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Error opening report file", e);
                                showAlert(Alert.AlertType.ERROR, "Error", "Error Opening File", 
                                        "An error occurred while trying to open the report file: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Load the admin's data and initialize the analytics dashboard.
     *
     * @param admin The admin
     */
    public void loadAdminData(Admin admin) {
        this.currentAdmin = admin;
        
        try {
            // Load departments for filter
            List<String> departments = UserService.getAllDepartments();
            if (departments != null && !departments.isEmpty()) {
                departmentFilterComboBox.getItems().addAll(departments);
            }
            departmentFilterComboBox.getSelectionModel().selectFirst();
            
            // Load units for filter
            List<Unit> units = UnitService.getAllUnits();
            if (units != null && !units.isEmpty()) {
                List<String> unitCodes = new ArrayList<>();
                for (Unit unit : units) {
                    unitCodes.add(unit.getUnitCode());
                }
                unitFilterComboBox.getItems().addAll(unitCodes);
            }
            unitFilterComboBox.getSelectionModel().selectFirst();
            
            // Load report types
            List<Report> reportTypes = ReportService.getSystemReports();
            if (reportTypes != null && !reportTypes.isEmpty()) {
                reportTypeComboBox.setItems(FXCollections.observableArrayList(reportTypes));
                reportTypeComboBox.getSelectionModel().selectFirst();
            }
            
            // Apply initial filters to load data
            applyFilters();
            
            // Load recent reports
            loadRecentReports();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading admin data", e);
        }
    }
    
    /**
     * Apply the selected filters and refresh the analytics data.
     */
    private void applyFilters() {
        filterStartDate = startDatePicker.getValue();
        filterEndDate = endDatePicker.getValue();
        filterDepartment = departmentFilterComboBox.getValue();
        filterUnit = unitFilterComboBox.getValue();
        
        if (filterDepartment.equals("All Departments")) {
            filterDepartment = null;
        }
        
        if (filterUnit.equals("All Units")) {
            filterUnit = null;
        }
        
        try {
            // Load analytics data with filters
            analyticsData = ReportService.getSystemAnalytics(
                    filterStartDate, filterEndDate, filterDepartment, filterUnit);
            
            // Update all tabs with the new data
            updateSystemOverviewTab();
            updateDepartmentPerformanceTab();
            updateUnitPerformanceTab();
            updateStudentPerformanceTab();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying filters", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Error Applying Filters", 
                    "An error occurred while applying the filters: " + e.getMessage());
        }
    }
    
    /**
     * Update the system overview tab.
     */
    private void updateSystemOverviewTab() {
        if (analyticsData == null) {
            return;
        }
        
        // Update summary labels
        totalStudentsLabel.setText(String.valueOf(analyticsData.get("totalStudents")));
        totalLecturersLabel.setText(String.valueOf(analyticsData.get("totalLecturers")));
        totalUnitsLabel.setText(String.valueOf(analyticsData.get("totalUnits")));
        totalAssessmentsLabel.setText(String.valueOf(analyticsData.get("totalAssessments")));
        avgSystemScoreLabel.setText(String.format("%.1f%%", (Double) analyticsData.get("avgSystemScore")));
        
        // Update user distribution chart
        Map<String, Integer> userCounts = (Map<String, Integer>) analyticsData.get("userDistribution");
        
        userDistributionChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Students", userCounts.get("STUDENT")),
                new PieChart.Data("Lecturers", userCounts.get("LECTURER")),
                new PieChart.Data("Admins", userCounts.get("ADMIN"))
        ));
        
        // Update system activity chart
        List<Map<String, Object>> activityData = (List<Map<String, Object>>) analyticsData.get("systemActivity");
        
        if (activityData != null && !activityData.isEmpty()) {
            systemActivityChart.getData().clear();
            
            XYChart.Series<String, Number> assessmentSeries = new XYChart.Series<>();
            assessmentSeries.setName("Assessments");
            
            XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
            userSeries.setName("User Logins");
            
            for (Map<String, Object> activity : activityData) {
                String date = (String) activity.get("date");
                Integer assessmentCount = (Integer) activity.get("assessmentCount");
                Integer loginCount = (Integer) activity.get("loginCount");
                
                assessmentSeries.getData().add(new XYChart.Data<>(date, assessmentCount));
                userSeries.getData().add(new XYChart.Data<>(date, loginCount));
            }
            
            systemActivityChart.getData().addAll(assessmentSeries, userSeries);
        }
    }
    
    /**
     * Update the department performance tab.
     */
    private void updateDepartmentPerformanceTab() {
        if (analyticsData == null) {
            return;
        }
        
        // Update department performance table
        List<Map<String, Object>> departmentData = (List<Map<String, Object>>) analyticsData.get("departmentPerformance");
        
        if (departmentData != null && !departmentData.isEmpty()) {
            departmentPerformanceTable.setItems(FXCollections.observableArrayList(departmentData));
            
            // Update department performance chart
            departmentPerformanceChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average Score (%)");
            
            for (Map<String, Object> department : departmentData) {
                String name = (String) department.get("departmentName");
                Double avgScore = (Double) department.get("avgScore");
                
                if (avgScore != null && !avgScore.isNaN()) {
                    series.getData().add(new XYChart.Data<>(name, avgScore));
                }
            }
            
            departmentPerformanceChart.getData().add(series);
        }
    }
    
    /**
     * Update the unit performance tab.
     */
    private void updateUnitPerformanceTab() {
        if (analyticsData == null) {
            return;
        }
        
        // Update unit performance table
        List<Map<String, Object>> unitData = (List<Map<String, Object>>) analyticsData.get("unitPerformance");
        
        if (unitData != null && !unitData.isEmpty()) {
            unitPerformanceTable.setItems(FXCollections.observableArrayList(unitData));
            
            // Update unit performance chart
            unitPerformanceChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average Score (%)");
            
            // Sort units by average score (highest first)
            List<Map<String, Object>> sortedUnits = new ArrayList<>(unitData);
            sortedUnits.sort((a, b) -> {
                Double aScore = (Double) a.get("avgScore");
                Double bScore = (Double) b.get("avgScore");
                
                if (aScore == null || aScore.isNaN()) {
                    aScore = 0.0;
                }
                
                if (bScore == null || bScore.isNaN()) {
                    bScore = 0.0;
                }
                
                return Double.compare(bScore, aScore);
            });
            
            // Only show the top 10 units for clarity
            int limit = Math.min(10, sortedUnits.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> unit = sortedUnits.get(i);
                String code = (String) unit.get("unitCode");
                Double avgScore = (Double) unit.get("avgScore");
                
                if (avgScore != null && !avgScore.isNaN()) {
                    series.getData().add(new XYChart.Data<>(code, avgScore));
                }
            }
            
            unitPerformanceChart.getData().add(series);
        }
    }
    
    /**
     * Update the student performance tab.
     */
    private void updateStudentPerformanceTab() {
        if (analyticsData == null) {
            return;
        }
        
        // Update student performance table
        List<Map<String, Object>> studentData = (List<Map<String, Object>>) analyticsData.get("studentPerformance");
        
        if (studentData != null && !studentData.isEmpty()) {
            studentPerformanceTable.setItems(FXCollections.observableArrayList(studentData));
            
            // Update student performance chart
            studentPerformanceChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average Score (%)");
            
            // Sort students by average score (highest first)
            List<Map<String, Object>> sortedStudents = new ArrayList<>(studentData);
            sortedStudents.sort((a, b) -> {
                Double aScore = (Double) a.get("avgScore");
                Double bScore = (Double) b.get("avgScore");
                
                if (aScore == null || aScore.isNaN()) {
                    aScore = 0.0;
                }
                
                if (bScore == null || bScore.isNaN()) {
                    bScore = 0.0;
                }
                
                return Double.compare(bScore, aScore);
            });
            
            // Only show the top 10 students for clarity
            int limit = Math.min(10, sortedStudents.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> student = sortedStudents.get(i);
                String name = (String) student.get("fullName");
                Double avgScore = (Double) student.get("avgScore");
                
                if (avgScore != null && !avgScore.isNaN()) {
                    series.getData().add(new XYChart.Data<>(name, avgScore));
                }
            }
            
            studentPerformanceChart.getData().add(series);
        }
    }
    
    /**
     * Load recent reports.
     */
    private void loadRecentReports() {
        try {
            List<Map<String, Object>> recentReports = ReportService.getRecentReportExecutions(10);
            
            if (recentReports != null && !recentReports.isEmpty()) {
                recentReportsTable.setItems(FXCollections.observableArrayList(recentReports));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading recent reports", e);
        }
    }
    
    /**
     * Generate a report.
     */
    private void generateReport() {
        Report reportType = reportTypeComboBox.getValue();
        String format = reportFormatComboBox.getValue();
        
        if (reportType == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Report Type Selected", 
                    "Please select a report type before generating a report.");
            return;
        }
        
        try {
            // Create parameter map for the report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("startDate", filterStartDate);
            parameters.put("endDate", filterEndDate);
            parameters.put("department", filterDepartment);
            parameters.put("unit", filterUnit);
            parameters.put("format", format);
            parameters.put("generatedBy", currentAdmin.getUserId());
            
            // Show file chooser to select save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report");
            fileChooser.setInitialFileName(reportType.getReportName() + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            switch (format.toLowerCase()) {
                case "pdf":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                    break;
                case "excel":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
                    break;
                case "csv":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                    break;
                case "html":
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("HTML Files", "*.html"));
                    break;
            }
            
            File file = fileChooser.showSaveDialog(new Stage());
            
            if (file != null) {
                // Generate the report
                boolean success = ReportService.executeReportToFile(reportType.getReportId(), parameters, file.getAbsolutePath());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Report Generated", 
                            "The report has been generated successfully.");
                    
                    // Refresh recent reports
                    loadRecentReports();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Report Generation Failed", 
                            "An error occurred while generating the report.");
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Report Generation Failed", 
                    "An error occurred while generating the report: " + e.getMessage());
        }
    }
    
    /**
     * Show an alert dialog.
     *
     * @param alertType The type of alert
     * @param title The alert title
     * @param header The alert header
     * @param content The alert content
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
