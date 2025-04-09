package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.*;
import book.javafx.kenyattacatsystem.services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for viewing an assessment.
 */
public class AssessmentViewController {
    
    @FXML private Label titleLabel;
    @FXML private Label unitLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label durationLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalMarksLabel;
    @FXML private TextArea descriptionArea;
    @FXML private VBox questionsContainer;
    
    private Assessment assessment;
    private Unit unit;
    
    /**
     * Initializes the controller with assessment data.
     * 
     * @param assessment The assessment to view
     * @param unit The unit the assessment belongs to
     */
    public void initData(Assessment assessment, Unit unit) {
        this.assessment = assessment;
        this.unit = unit;
        
        displayAssessmentDetails();
        displayQuestions();
    }
    
    /**
     * Displays the assessment details.
     */
    private void displayAssessmentDetails() {
        // Set title
        titleLabel.setText(assessment.getTitle());
        
        // Set unit
        unitLabel.setText(unit.getUnitCode() + " - " + unit.getUnitName());
        
        // Set date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        dateTimeLabel.setText(assessment.getStartDateTime().format(formatter));
        
        // Set duration
        int durationMinutes = assessment.getDurationMinutes();
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        durationLabel.setText(String.format("%d:%02d", hours, minutes));
        
        // Set status
        boolean isActive = assessment.isActive();
        statusLabel.setText(isActive ? "Published" : "Draft");
        statusLabel.getStyleClass().add(isActive ? "status-published" : "status-draft");
        
        // Set total questions and marks
        totalQuestionsLabel.setText(String.valueOf(assessment.getQuestions().size()));
        totalMarksLabel.setText(String.valueOf(assessment.getTotalMarks()));
        
        // Set description
        descriptionArea.setText(assessment.getDescription());
        descriptionArea.setEditable(false);
    }
    
    /**
     * Displays the assessment questions.
     */
    private void displayQuestions() {
        // Clear existing content
        questionsContainer.getChildren().clear();
        
        // Get the assessment questions
        List<Question> questions = assessment.getQuestions();
        
        // Display each question
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            
            // Create a container for this question
            VBox questionBox = new VBox(5);
            questionBox.getStyleClass().add("question-box");
            
            // Question number and text
            Label questionLabel = new Label((i + 1) + ". " + question.getText());
            questionLabel.getStyleClass().add("question-text");
            questionBox.getChildren().add(questionLabel);
            
            // Question type and marks
            Label infoLabel = new Label(String.format("Type: %s | Marks: %d | Difficulty: %s", 
                                                    question.getType(), question.getMarks(), 
                                                    question.getDifficulty()));
            infoLabel.getStyleClass().add("question-info");
            questionBox.getChildren().add(infoLabel);
            
            // For multiple choice questions, show options
            if (question.getType().equals(Question.TYPE_MULTIPLE_CHOICE)) {
                List<String> options = question.getOptions();
                for (int j = 0; j < options.size(); j++) {
                    String option = options.get(j);
                    Label optionLabel = new Label((char)('A' + j) + ". " + option);
                    optionLabel.getStyleClass().add("option-text");
                    questionBox.getChildren().add(optionLabel);
                }
            }
            
            // Add to container
            questionsContainer.getChildren().add(questionBox);
            
            // Add separator if not the last question
            if (i < questions.size() - 1) {
                Separator separator = new Separator();
                questionsContainer.getChildren().add(separator);
            }
        }
    }
    
    /**
     * Closes the view.
     */
    @FXML
    public void close() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Prints the assessment.
     */
    @FXML
    public void printAssessment() {
        // TODO: Implement printing functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print");
        alert.setHeaderText("Print Assessment");
        alert.setContentText("Printing functionality will be implemented in a future update.");
        alert.showAndWait();
    }
}
