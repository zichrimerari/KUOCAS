package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.Assessment;
import book.javafx.kenyattacatsystem.models.Question;
import book.javafx.kenyattacatsystem.models.StudentAssessmentAttempt;
import book.javafx.kenyattacatsystem.models.StudentResponse;
import book.javafx.kenyattacatsystem.services.PerformanceAnalyticsService;
import book.javafx.kenyattacatsystem.services.StudentAssessmentService;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the assessment results view
 */
public class AssessmentResultsController {
    private static final Logger LOGGER = Logger.getLogger(AssessmentResultsController.class.getName());
    
    @FXML private Label assessmentTitleLabel;
    @FXML private Label scoreLabel;
    @FXML private Label percentageLabel;
    @FXML private Label gradeLabel;
    @FXML private Button closeButton;
    @FXML private TableView<QuestionResult> resultsTable;
    @FXML private TableColumn<QuestionResult, String> questionColumn;
    @FXML private TableColumn<QuestionResult, String> yourAnswerColumn;
    @FXML private TableColumn<QuestionResult, String> correctAnswerColumn;
    @FXML private TableColumn<QuestionResult, String> marksColumn;
    @FXML private VBox feedbackContainer;
    @FXML private Accordion questionAccordion;
    
    private StudentAssessmentAttempt attempt;
    private List<Question> questions;
    private Assessment assessment;
    private StudentAssessmentService studentAssessmentService;
    
    /**
     * Sets the assessment attempt data
     *
     * @param attempt The student's assessment attempt
     * @param questions The list of questions in the assessment
     * @param assessment The assessment object
     */
    public void setAttempt(StudentAssessmentAttempt attempt, List<Question> questions, Assessment assessment) {
        this.attempt = attempt;
        this.questions = questions;
        this.assessment = assessment;
        
        displayResults();
    }
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
        
        studentAssessmentService = new StudentAssessmentService();
        
        // Set up table columns
        questionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuestionText()));
        yourAnswerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getYourAnswer()));
        correctAnswerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorrectAnswer()));
        marksColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarks()));
    }
    
    /**
     * Displays the assessment results
     */
    private void displayResults() {
        if (attempt == null || questions == null || assessment == null) {
            return;
        }
        
        // Set the assessment title
        assessmentTitleLabel.setText(assessment.getTitle());
        
        // Calculate and display the score, percentage, and grade
        int score = attempt.getScore();
        
        // Use the attempt's total possible marks instead of the assessment's total marks
        // This ensures we're using the correct denominator for percentage calculation
        int totalPossible = attempt.getTotalPossible();
        
        // If totalPossible is still 0, calculate it from the questions
        if (totalPossible == 0) {
            for (Question question : questions) {
                totalPossible += question.getMarks();
            }
            attempt.setTotalPossible(totalPossible);
            LOGGER.log(Level.INFO, "Calculated totalPossible from questions: {0}", totalPossible);
        }
        
        // Log the values for debugging
        LOGGER.log(Level.INFO, "Results display - Score: {0}, Total Possible: {1}, Assessment Total Marks: {2}", 
                  new Object[]{score, totalPossible, assessment.getTotalMarks()});
        
        double percentage = (totalPossible > 0) ? ((double) score / totalPossible * 100) : 0;
        String grade = calculateGrade(percentage);
        
        scoreLabel.setText(score + " / " + totalPossible);
        percentageLabel.setText(String.format("%.1f%%", percentage));
        gradeLabel.setText(grade);
        
        // Save the assessment results to the database
        saveAssessmentResults();
        
        // Create a map of question ID to question for easier lookup
        Map<String, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));
        
        // Create a list of question results
        List<QuestionResult> results = new ArrayList<>();
        
        // Create accordion panes for detailed feedback
        questionAccordion.getPanes().clear();
        
        for (Question question : questions) {
            StudentResponse response = attempt.getResponse(question.getQuestionId());
            String yourAnswer = (response != null) ? response.getResponseText() : "No answer";
            String correctAnswer = String.join(", ", question.getCorrectAnswers());
            int marksAwarded = (response != null) ? response.getMarksAwarded() : 0;
            
            // Add to results table
            results.add(new QuestionResult(
                    question.getQuestionText(),
                    yourAnswer,
                    correctAnswer,
                    marksAwarded + " / " + question.getMarks()
            ));
            
            // Create detailed feedback in accordion
            TitledPane questionPane = createQuestionFeedbackPane(question, response);
            questionAccordion.getPanes().add(questionPane);
        }
        
        // Update the results table
        resultsTable.setItems(FXCollections.observableArrayList(results));
    }
    
    /**
     * Creates a titled pane with detailed feedback for a question
     *
     * @param question The question
     * @param response The student's response
     * @return A titled pane with detailed feedback
     */
    private TitledPane createQuestionFeedbackPane(Question question, StudentResponse response) {
        // Create a VBox to hold the feedback content
        VBox content = new VBox(10);
        content.getStyleClass().add("feedback-content");
        
        // Add question text
        Label questionTextLabel = new Label("Question: " + question.getQuestionText());
        questionTextLabel.setWrapText(true);
        questionTextLabel.getStyleClass().add("feedback-question");
        content.getChildren().add(questionTextLabel);
        
        // Add your answer
        String yourAnswer = (response != null) ? response.getResponseText() : "No answer";
        Label yourAnswerLabel = new Label("Your Answer: " + yourAnswer);
        yourAnswerLabel.setWrapText(true);
        yourAnswerLabel.getStyleClass().add("feedback-your-answer");
        content.getChildren().add(yourAnswerLabel);
        
        // Add correct answer
        String correctAnswer = String.join(", ", question.getCorrectAnswers());
        Label correctAnswerLabel = new Label("Correct Answer: " + correctAnswer);
        correctAnswerLabel.setWrapText(true);
        correctAnswerLabel.getStyleClass().add("feedback-correct-answer");
        content.getChildren().add(correctAnswerLabel);
        
        // Add marks
        int marksAwarded = (response != null) ? response.getMarksAwarded() : 0;
        Label marksLabel = new Label("Marks: " + marksAwarded + " / " + question.getMarks());
        marksLabel.getStyleClass().add("feedback-marks");
        content.getChildren().add(marksLabel);
        
        // Add feedback if available
        if (response != null && response.getFeedback() != null && !response.getFeedback().isEmpty()) {
            Label feedbackLabel = new Label("Feedback: " + response.getFeedback());
            feedbackLabel.setWrapText(true);
            feedbackLabel.getStyleClass().add("feedback-text");
            content.getChildren().add(feedbackLabel);
        }
        
        // Create the titled pane
        TitledPane pane = new TitledPane();
        pane.setText("Question " + (questions.indexOf(question) + 1));
        pane.setContent(content);
        
        // Set style based on correctness
        if (response != null && response.isCorrect()) {
            pane.getStyleClass().add("correct-question");
        } else {
            pane.getStyleClass().add("incorrect-question");
        }
        
        return pane;
    }
    
    /**
     * Saves the assessment results to the database
     */
    private void saveAssessmentResults() {
        // Only save if we have a valid attempt
        if (attempt == null || questions == null || assessment == null) {
            LOGGER.log(Level.WARNING, "Cannot save assessment results: attempt, questions, or assessment is null");
            return;
        }
        
        LOGGER.log(Level.INFO, "Saving assessment results for attempt {0}, assessment {1}, student {2}", 
                  new Object[]{attempt.getAttemptId(), attempt.getAssessmentId(), attempt.getStudentId()});
        
        // Save the attempt to the database
        boolean success = studentAssessmentService.saveStudentAssessment(attempt);
        
        if (success) {
            LOGGER.log(Level.INFO, "Successfully saved assessment results to database");
            
            // If this is a practice assessment, also save to the practice_assessments table
            try {
                // Check if this is a practice assessment
                String query = "SELECT is_practice, title, unit_code FROM assessments WHERE assessment_id = ?";
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                    stmt.setString(1, assessment.getAssessmentId());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next() && rs.getBoolean("is_practice")) {
                        // This is a practice assessment, save to practice_assessments table
                        String title = rs.getString("title");
                        String unitCode = rs.getString("unit_code");
                        
                        // Calculate percentage
                        int score = attempt.getScore();
                        int totalPossible = attempt.getTotalPossible();
                        double percentage = (totalPossible > 0) ? ((double) score / totalPossible * 100) : 0;
                        
                        // Use the PerformanceAnalyticsService to save the practice assessment result
                        PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
                        boolean practiceSuccess = analyticsService.savePracticeAssessmentResult(
                                attempt, title, unitCode, percentage);
                        
                        if (practiceSuccess) {
                            LOGGER.log(Level.INFO, "Successfully saved practice assessment result");
                        } else {
                            LOGGER.log(Level.WARNING, "Failed to save practice assessment result");
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error checking if assessment is practice or saving practice result", e);
            }
        } else {
            LOGGER.log(Level.WARNING, "Failed to save assessment results to database");
        }
    }
    
    /**
     * Calculates the grade based on the percentage
     *
     * @param percentage The percentage score
     * @return The grade (A, B, C, D, or F)
     */
    private String calculateGrade(double percentage) {
        if (percentage >= 80) {
            return "A";
        } else if (percentage >= 70) {
            return "B";
        } else if (percentage >= 60) {
            return "C";
        } else if (percentage >= 50) {
            return "D";
        } else {
            return "F";
        }
    }
    
    /**
     * Inner class to represent a question result for the table
     */
    private static class QuestionResult {
        private final String questionText;
        private final String yourAnswer;
        private final String correctAnswer;
        private final String marks;
        
        public QuestionResult(String questionText, String yourAnswer, String correctAnswer, String marks) {
            this.questionText = questionText;
            this.yourAnswer = yourAnswer;
            this.correctAnswer = correctAnswer;
            this.marks = marks;
        }
        
        public String getQuestionText() {
            return questionText;
        }
        
        public String getYourAnswer() {
            return yourAnswer;
        }
        
        public String getCorrectAnswer() {
            return correctAnswer;
        }
        
        public String getMarks() {
            return marks;
        }
    }
}
