package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.Lecturer;
import book.javafx.kenyattacatsystem.models.ReportExecution;
import book.javafx.kenyattacatsystem.models.ReportFile;
import book.javafx.kenyattacatsystem.models.Unit;
import book.javafx.kenyattacatsystem.services.ReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Reports View.
 * Handles displaying and managing generated reports.
 */
public class ReportsViewController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ReportsViewController.class.getName());
    
    @FXML
    private TableView<ReportFile> reportsTableView;
    
    @FXML
    private ComboBox<String> reportTypeComboBox;
    
    @FXML
    private ComboBox<Unit> unitSelector;
    
    @FXML
    private VBox reportsContainer;
    
    private ObservableList<ReportFile> generatedReports = FXCollections.observableArrayList();
    private Lecturer currentLecturer;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the reports table
        setupReportsTable();
        
        // Initialize report types
        setupReportTypes();
    }
    
    /**
     * Sets the current lecturer.
     * 
     * @param lecturer The current lecturer
     */
    public void setCurrentLecturer(Lecturer lecturer) {
        this.currentLecturer = lecturer;
        LOGGER.info("Set current lecturer: " + (lecturer != null ? lecturer.getFullName() : "null"));
        
        // Load reports for this lecturer
        loadReports();
    }
    
    /**
     * Sets the available units for the unit selector.
     * 
     * @param units The list of units
     */
    public void setUnits(ObservableList<Unit> units) {
        unitSelector.setItems(units);
        unitSelector.setCellFactory(param -> new ListCell<Unit>() {
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
        
        unitSelector.setButtonCell(new ListCell<Unit>() {
            @Override
            protected void updateItem(Unit unit, boolean empty) {
                super.updateItem(unit, empty);
                
                if (empty || unit == null) {
                    setText("Select Unit");
                } else {
                    setText(unit.getUnitCode() + " - " + unit.getUnitName());
                }
            }
        });
    }
    
    /**
     * Sets up the reports table with columns and cell factories.
     */
    private void setupReportsTable() {
        // Create table columns
        TableColumn<ReportFile, String> nameColumn = new TableColumn<>("Report Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReportName()));
        nameColumn.setPrefWidth(200);
        
        TableColumn<ReportFile, String> dateColumn = new TableColumn<>("Generated Date");
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        dateColumn.setPrefWidth(150);
        
        TableColumn<ReportFile, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileType()));
        typeColumn.setPrefWidth(80);
        
        TableColumn<ReportFile, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button openButton = new Button("Open");
            private final Button exportButton = new Button("Export");
            
            {
                openButton.getStyleClass().add("small-button");
                exportButton.getStyleClass().add("small-button");
                
                openButton.setOnAction(event -> {
                    ReportFile report = getTableView().getItems().get(getIndex());
                    openReportFile(report);
                });
                
                exportButton.setOnAction(event -> {
                    ReportFile report = getTableView().getItems().get(getIndex());
                    // Handle export action
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(5, openButton, exportButton);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
        
        reportsTableView.getColumns().addAll(nameColumn, dateColumn, typeColumn, actionsColumn);
        reportsTableView.setItems(generatedReports);
    }
    
    /**
     * Sets up the report types combo box.
     */
    private void setupReportTypes() {
        reportTypeComboBox.getItems().setAll(
            "Student Performance Report",
            "Assessment Results Summary",
            "Unit Progress Report",
            "Question Analysis Report",
            "Attendance Report"
        );
    }
    
    /**
     * Loads reports for the current lecturer.
     */
    public void loadReports() {
        if (currentLecturer == null) {
            LOGGER.warning("Cannot load reports: current lecturer is null");
            return;
        }
        
        try {
            // Clear existing reports
            generatedReports.clear();
            
            // Get report executions from the database
            List<ReportExecution> executions = ReportService.getReportExecutionsByLecturer(currentLecturer.getUserId());
            LOGGER.info("Retrieved " + executions.size() + " report executions for lecturer: " + currentLecturer.getUserId());
            
            // Convert to ReportFile objects
            for (ReportExecution execution : executions) {
                String filePath = execution.getResultFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        String reportName = execution.getReportName();
                        LocalDateTime date = execution.getExecutionDate();
                        String fileType = getFileExtension(file).toUpperCase();
                        
                        ReportFile reportFile = new ReportFile(reportName, filePath, date, fileType);
                        generatedReports.add(reportFile);
                        LOGGER.info("Added report: " + reportName + " from file: " + file.getName());
                    } else {
                        LOGGER.warning("Report file not found: " + filePath);
                    }
                }
            }
            
            // Update the table view
            reportsTableView.setItems(null);
            reportsTableView.setItems(generatedReports);
            LOGGER.info("Updated reports table with " + generatedReports.size() + " reports");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading reports", e);
        }
    }
    
    /**
     * Handles generating a new report.
     */
    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeComboBox.getValue();
        Unit selectedUnit = unitSelector.getValue();
        
        if (reportType == null || reportType.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Report Type Selected", 
                    "Please select a report type before generating a report.");
            return;
        }
        
        // TODO: Implement report generation logic
        
        // After generating the report, reload the reports list
        loadReports();
    }
    
    /**
     * Opens a report file.
     * 
     * @param report The report to open
     */
    private void openReportFile(ReportFile report) {
        try {
            File file = new File(report.getFilePath());
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "File Not Found", 
                        "The report file could not be found at: " + report.getFilePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not open report file", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could Not Open File", 
                    "An error occurred while trying to open the report file: " + e.getMessage());
        }
    }
    
    /**
     * Gets the file extension from a file.
     * 
     * @param file The file
     * @return The file extension
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param type The alert type
     * @param title The alert title
     * @param header The alert header
     * @param content The alert content
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
