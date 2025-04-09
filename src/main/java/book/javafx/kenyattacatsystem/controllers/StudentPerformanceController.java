package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.Student;
import book.javafx.kenyattacatsystem.services.PerformanceAnalyticsService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the student performance analytics view.
 */
public class StudentPerformanceController {
    private static final Logger LOGGER = Logger.getLogger(StudentPerformanceController.class.getName());
    
    @FXML private TabPane performanceTabPane;
    
    // Overview tab
    @FXML private Label overallScoreLabel;
    @FXML private Label formalTestsScoreLabel;
    @FXML private Label practiceTestsScoreLabel;
    @FXML private Label totalAssessmentsLabel;
    @FXML private PieChart testTypeDistributionChart;
    @FXML private BarChart<String, Number> unitPerformanceChart;
    
    // Formal Tests tab
    @FXML private TableView<Map<String, Object>> formalTestsTable;
    @FXML private TableColumn<Map<String, Object>, String> formalTestNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> formalTestUnitColumn;
    @FXML private TableColumn<Map<String, Object>, Number> formalTestScoreColumn;
    @FXML private TableColumn<Map<String, Object>, Number> formalTestPercentageColumn;
    @FXML private TableColumn<Map<String, Object>, String> formalTestDateColumn;
    @FXML private LineChart<String, Number> formalTestsProgressChart;
    
    // Practice Tests tab
    @FXML private TableView<Map<String, Object>> practiceTestsTable;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestUnitColumn;
    @FXML private TableColumn<Map<String, Object>, Number> practiceTestScoreColumn;
    @FXML private TableColumn<Map<String, Object>, Number> practiceTestPercentageColumn;
    @FXML private TableColumn<Map<String, Object>, String> practiceTestDateColumn;
    @FXML private LineChart<String, Number> practiceTestsProgressChart;
    @FXML private Label noPracticeTestsLabel;
    @FXML private PieChart practiceTestsChart;
    
    private Student currentStudent;
    private Map<String, Object> performanceData;
    private PerformanceAnalyticsService performanceService;
    
    /**
     * Sets the current student for this controller.
     * 
     * @param student The student to set
     */
    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
    }
    
    /**
     * Loads performance data for the current student.
     * This method is called after setting the current student.
     */
    public void loadPerformanceData() {
        try {
            // Get the performance data
            performanceData = performanceService.getStudentPerformance(currentStudent.getUserId());
            
            // Update the UI with the data
            updateOverviewTab();
            updateFormalTestsTab();
            updatePracticeTestsTab();
            
            // Log success
            LOGGER.log(Level.INFO, "Performance data loaded successfully for student: {0}", currentStudent.getUserId());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading performance data", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load performance data", e.getMessage());
        }
    }
    
    /**
     * Initialize the controller.
     */
    @FXML
    private void initialize() {
        // Initialize service
        performanceService = new PerformanceAnalyticsService();
        
        // Configure formal tests table columns
        formalTestNameColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("title")));
        formalTestUnitColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("unitCode")));
        formalTestScoreColumn.setCellValueFactory(data -> {
            Integer score = (Integer) data.getValue().get("score");
            return new SimpleIntegerProperty(score != null ? score : 0);
        });
        formalTestPercentageColumn.setCellValueFactory(data -> {
            Double percentage = (Double) data.getValue().get("percentage");
            return new SimpleDoubleProperty(percentage != null ? percentage : 0);
        });
        formalTestDateColumn.setCellValueFactory(data -> {
            Timestamp timestamp = (Timestamp) data.getValue().get("endTime");
            return new SimpleStringProperty(timestamp != null ? 
                    timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
        });
        
        // Configure practice tests table columns
        practiceTestNameColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("title")));
        practiceTestUnitColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("unitCode")));
        practiceTestScoreColumn.setCellValueFactory(data -> {
            Object scoreObj = data.getValue().get("score");
            Integer score = (scoreObj instanceof Integer) ? (Integer) scoreObj : 
                           (scoreObj instanceof Double) ? ((Double) scoreObj).intValue() : 0;
            return new SimpleIntegerProperty(score);
        });
        practiceTestPercentageColumn.setCellValueFactory(data -> {
            Object percentObj = data.getValue().get("percentage");
            Double percentage = (percentObj instanceof Double) ? (Double) percentObj : 0.0;
            return new SimpleDoubleProperty(percentage);
        });
        practiceTestDateColumn.setCellValueFactory(data -> {
            Object timeObj = data.getValue().get("endTime");
            if (timeObj instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) timeObj;
                return new SimpleStringProperty(timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else {
                return new SimpleStringProperty("");
            }
        });
        
        // Format percentage columns
        formalTestPercentageColumn.setCellFactory(column -> new TableCell<Map<String, Object>, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item.doubleValue()));
                }
            }
        });
        
        practiceTestPercentageColumn.setCellFactory(column -> new TableCell<Map<String, Object>, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item.doubleValue()));
                }
            }
        });
        
        // Format score columns to show score/total
        formalTestScoreColumn.setCellFactory(column -> new TableCell<Map<String, Object>, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                    Integer total = (Integer) rowData.get("totalPossible");
                    setText(String.format("%d/%d", item.intValue(), total != null ? total : 0));
                }
            }
        });
        
        practiceTestScoreColumn.setCellFactory(column -> new TableCell<Map<String, Object>, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                    Object totalObj = rowData.get("totalPossible");
                    Integer total = (totalObj instanceof Integer) ? (Integer) totalObj : 
                                   (totalObj instanceof Double) ? ((Double) totalObj).intValue() : 0;
                    setText(String.format("%d/%d", item.intValue(), total));
                }
            }
        });
        
        // Hide the practice tests label initially
        if (noPracticeTestsLabel != null) {
            noPracticeTestsLabel.setVisible(false);
        }
    }
    
    /**
     * Load the student's performance data.
     *
     * @param student The student
     */
    public void loadStudentPerformance(Student student) {
        this.currentStudent = student;
        
        try {
            // Get performance data
            performanceData = performanceService.getStudentPerformance(student.getUserId());
            
            // Update overview tab
            updateOverviewTab();
            
            // Update formal tests tab
            updateFormalTestsTab();
            
            // Update practice tests tab
            updatePracticeTestsTab();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading student performance", e);
        }
    }
    
    /**
     * Update the overview tab.
     */
    private void updateOverviewTab() {
        if (performanceData == null) {
            return;
        }
        
        // Get assessment data
        List<Map<String, Object>> formalAssessments = (List<Map<String, Object>>) performanceData.get("formalAssessments");
        List<Map<String, Object>> practiceAssessments = (List<Map<String, Object>>) performanceData.get("practiceAssessments");
        
        // Calculate overall metrics
        double formalTotal = 0;
        double practiceTotal = 0;
        int formalCount = 0;
        int practiceCount = 0;
        
        if (formalAssessments != null) {
            for (Map<String, Object> assessment : formalAssessments) {
                Double percentage = (Double) assessment.get("percentage");
                if (percentage != null && !percentage.isNaN()) {
                    formalTotal += percentage;
                    formalCount++;
                }
            }
        }
        
        if (practiceAssessments != null) {
            for (Map<String, Object> assessment : practiceAssessments) {
                Double percentage = (Double) assessment.get("percentage");
                if (percentage != null && !percentage.isNaN()) {
                    practiceTotal += percentage;
                    practiceCount++;
                }
            }
        }
        
        double formalAvg = formalCount > 0 ? formalTotal / formalCount : 0;
        double practiceAvg = practiceCount > 0 ? practiceTotal / practiceCount : 0;
        double overallAvg = (formalCount + practiceCount) > 0 ? 
                (formalTotal + practiceTotal) / (formalCount + practiceCount) : 0;
        
        // Update labels
        overallScoreLabel.setText(String.format("%.1f%%", overallAvg));
        formalTestsScoreLabel.setText(String.format("%.1f%%", formalAvg));
        practiceTestsScoreLabel.setText(String.format("%.1f%%", practiceAvg));
        totalAssessmentsLabel.setText(String.valueOf(formalCount + practiceCount));
        
        // Update test type distribution chart
        testTypeDistributionChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Formal Tests", formalCount),
                new PieChart.Data("Practice Tests", practiceCount)
        ));
        
        // Update unit performance chart
        unitPerformanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Score (%)");
        
        // Calculate unit averages
        Map<String, List<Double>> unitScores = new HashMap<>();
        
        if (formalAssessments != null) {
            for (Map<String, Object> assessment : formalAssessments) {
                String unitCode = (String) assessment.get("unitCode");
                Double percentage = (Double) assessment.get("percentage");
                
                if (unitCode != null && percentage != null && !percentage.isNaN()) {
                    if (!unitScores.containsKey(unitCode)) {
                        unitScores.put(unitCode, new ArrayList<>());
                    }
                    unitScores.get(unitCode).add(percentage);
                }
            }
        }
        
        if (practiceAssessments != null) {
            for (Map<String, Object> assessment : practiceAssessments) {
                String unitCode = (String) assessment.get("unitCode");
                Double percentage = (Double) assessment.get("percentage");
                
                if (unitCode != null && percentage != null && !percentage.isNaN()) {
                    if (!unitScores.containsKey(unitCode)) {
                        unitScores.put(unitCode, new ArrayList<>());
                    }
                    unitScores.get(unitCode).add(percentage);
                }
            }
        }
        
        // Calculate averages and add to chart
        for (Map.Entry<String, List<Double>> entry : unitScores.entrySet()) {
            String unitCode = entry.getKey();
            List<Double> scores = entry.getValue();
            
            double sum = 0;
            for (Double score : scores) {
                sum += score;
            }
            double avg = sum / scores.size();
            
            series.getData().add(new XYChart.Data<>(unitCode, avg));
        }
        
        unitPerformanceChart.getData().add(series);
    }
    
    /**
     * Update the formal tests tab.
     */
    private void updateFormalTestsTab() {
        List<Map<String, Object>> formalAssessments = (List<Map<String, Object>>) performanceData.get("formalAssessments");
        
        if (formalAssessments != null && !formalAssessments.isEmpty()) {
            // Update table
            formalTestsTable.setItems(FXCollections.observableArrayList(formalAssessments));
            
            // Update progress chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Score (%)");
            
            // Sort by date (newest first)
            List<Map<String, Object>> sortedAssessments = new ArrayList<>(formalAssessments);
            sortedAssessments.sort((a, b) -> {
                Timestamp aTime = (Timestamp) a.get("endTime");
                Timestamp bTime = (Timestamp) b.get("endTime");
                return aTime.compareTo(bTime);
            });
            
            // Only show the last 10 assessments for clarity
            int startIndex = Math.max(0, sortedAssessments.size() - 10);
            for (int i = startIndex; i < sortedAssessments.size(); i++) {
                Map<String, Object> assessment = sortedAssessments.get(i);
                Timestamp timestamp = (Timestamp) assessment.get("endTime");
                String date = timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM-dd"));
                
                series.getData().add(new XYChart.Data<>(
                        date + " " + assessment.get("title"),
                        (Double) assessment.get("percentage")
                ));
            }
            
            formalTestsProgressChart.getData().clear();
            formalTestsProgressChart.getData().add(series);
        }
    }
    
    /**
     * Updates the practice tests tab with data.
     */
    private void updatePracticeTestsTab() {
        try {
            // Get practice tests
            List<Map<String, Object>> practiceTests = performanceService.getStudentPracticeTests(currentStudent.getUserId());
            
            LOGGER.log(Level.INFO, "Retrieved {0} practice tests for student {1}", 
                      new Object[]{practiceTests.size(), currentStudent.getUserId()});
            
            // Update the table
            practiceTestsTable.getItems().clear();
            
            if (practiceTests.isEmpty()) {
                noPracticeTestsLabel.setVisible(true);
                practiceTestsTable.setVisible(false);
                practiceTestsChart.setVisible(false);
                practiceTestsProgressChart.setVisible(false);
                LOGGER.log(Level.INFO, "No practice tests found for student {0}", currentStudent.getUserId());
                return;
            }
            
            noPracticeTestsLabel.setVisible(false);
            practiceTestsTable.setVisible(true);
            practiceTestsChart.setVisible(true);
            practiceTestsProgressChart.setVisible(true);
            
            // Add to table with ObservableArrayList for better binding
            ObservableList<Map<String, Object>> observableTests = FXCollections.observableArrayList(practiceTests);
            practiceTestsTable.setItems(observableTests);
            
            // Calculate average score
            double totalScore = 0;
            for (Map<String, Object> test : practiceTests) {
                Double percentage = (Double) test.get("percentage");
                if (percentage != null && !percentage.isNaN()) {
                    totalScore += percentage;
                }
            }
            
            double averageScore = practiceTests.isEmpty() ? 0 : totalScore / practiceTests.size();
            
            // Update the pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Correct", averageScore),
                    new PieChart.Data("Incorrect", 100 - averageScore)
            );
            
            practiceTestsChart.setData(pieChartData);
            
            // Apply colors
            pieChartData.get(0).getNode().setStyle("-fx-pie-color: #4CAF50;"); // Green for correct
            pieChartData.get(1).getNode().setStyle("-fx-pie-color: #F44336;"); // Red for incorrect
            
            // Add percentage labels to the chart
            for (final PieChart.Data data : practiceTestsChart.getData()) {
                Node node = data.getNode();
                
                Tooltip tooltip = new Tooltip(String.format("%.1f%%", data.getPieValue()));
                Tooltip.install(node, tooltip);
                
                // Add a label with the percentage inside each pie slice
                Text text = new Text(String.format("%.1f%%", data.getPieValue()));
                text.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                
                node.parentProperty().addListener((obs, oldParent, newParent) -> {
                    if (newParent != null) {
                        Group parent = (Group) newParent;
                        parent.getChildren().add(text);
                    }
                });
                
                node.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
                    text.setTranslateX(newBounds.getMinX() + newBounds.getWidth() / 2 - text.getLayoutBounds().getWidth() / 2);
                    text.setTranslateY(newBounds.getMinY() + newBounds.getHeight() / 2);
                });
            }
            
            // Update progress chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Score (%)");
            
            // Sort by date (newest first)
            List<Map<String, Object>> sortedTests = new ArrayList<>(practiceTests);
            sortedTests.sort((a, b) -> {
                Object aTimeObj = a.get("endTime");
                Object bTimeObj = b.get("endTime");
                
                if (aTimeObj instanceof Timestamp && bTimeObj instanceof Timestamp) {
                    Timestamp aTime = (Timestamp) aTimeObj;
                    Timestamp bTime = (Timestamp) bTimeObj;
                    return aTime.compareTo(bTime);
                }
                return 0;
            });
            
            // Only show the last 10 tests for clarity
            int startIndex = Math.max(0, sortedTests.size() - 10);
            for (int i = startIndex; i < sortedTests.size(); i++) {
                Map<String, Object> test = sortedTests.get(i);
                Object timeObj = test.get("endTime");
                String date = "";
                
                if (timeObj instanceof Timestamp) {
                    Timestamp timestamp = (Timestamp) timeObj;
                    date = timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM-dd"));
                }
                
                Object titleObj = test.get("title");
                String title = (titleObj instanceof String) ? (String) titleObj : "";
                
                Object percentObj = test.get("percentage");
                Double percentage = (percentObj instanceof Double) ? (Double) percentObj : 0.0;
                
                series.getData().add(new XYChart.Data<>(
                        date + " " + title,
                        percentage
                ));
            }
            
            practiceTestsProgressChart.getData().clear();
            practiceTestsProgressChart.getData().add(series);
            
            // Log success
            LOGGER.log(Level.INFO, "Practice tests tab updated with {0} tests", practiceTests.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating practice tests tab", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update practice tests tab", e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog with the given parameters.
     *
     * @param alertType The type of alert to show
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
