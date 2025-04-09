package book.javafx.kenyattacatsystem.controllers;

import book.javafx.kenyattacatsystem.models.Assessment;
import book.javafx.kenyattacatsystem.models.Question;
import book.javafx.kenyattacatsystem.models.StudentAssessmentAttempt;
import book.javafx.kenyattacatsystem.models.StudentResponse;
import book.javafx.kenyattacatsystem.services.AssessmentService;
import book.javafx.kenyattacatsystem.services.QuestionService;
import book.javafx.kenyattacatsystem.services.StudentAssessmentService;
import book.javafx.kenyattacatsystem.utils.AlertUtil;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import book.javafx.kenyattacatsystem.utils.ProctoringUtil;
//import book.javafx.kenyattacatsystem.utils.NavigationUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the assessment taking view.
 * Handles displaying questions, collecting student responses, and submitting the assessment.
 */
public class AssessmentTakingController implements Initializable {
    // Header elements
    @FXML
    private Text assessmentTitleText;
    @FXML
    private Label timerLabel;
    @FXML
    private Label unitLabel;
    @FXML
    private Label totalQuestionsLabel;
    @FXML
    private Label totalMarksLabel;

    // Question navigation
    @FXML
    private VBox questionNavContainer;

    // Question display
    @FXML
    private Label questionNumberLabel;
    @FXML
    private Label questionMarksLabel;
    @FXML
    private Text questionTextLabel;

    // Answer containers
    @FXML
    private VBox answerContainer;
    @FXML
    private VBox multipleChoiceContainer;
    @FXML
    private VBox shortAnswerContainer;
    @FXML
    private VBox listBasedContainer;
    @FXML
    private TextField shortAnswerField;
    @FXML
    private TextArea listAnswerField;

    // Navigation buttons
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button submitButton;

    // Assessment data
    private Assessment assessment;
    private List<Question> questions;
    private StudentAssessmentAttempt attempt;
    private int currentQuestionIndex = 0;
    private ToggleGroup answerGroup;
    private List<RadioButton> optionButtons;
    private String studentId;

    // Timer
    private Timeline timer;
    private int secondsRemaining;
    private boolean isTimeUp = false;

    private StudentAssessmentService studentAssessmentService;
    private static final Logger LOGGER = Logger.getLogger(AssessmentTakingController.class.getName());

    // Proctoring
    private ProctoringUtil proctoringUtil;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the toggle group for multiple choice questions
        answerGroup = new ToggleGroup();
        optionButtons = new ArrayList<>();
        
        // Initialize services
        studentAssessmentService = new StudentAssessmentService();
    }

    /**
     * Sets up the assessment view with the given assessment
     *
     * @param assessment The assessment to take
     * @param studentId  The ID of the student taking the assessment
     */
    public void setupAssessment(Assessment assessment, String studentId) {
        this.assessment = assessment;
        this.studentId = studentId;

        // Initialize proctoring
        proctoringUtil = new ProctoringUtil(assessment.getAssessmentId(), studentId);

        // Load questions for this assessment
        this.questions = assessment.getQuestions();

        // Debug: Log information about loaded questions
        LOGGER.info("Loaded assessment: " + assessment.getTitle() + " with " +
                (questions != null ? questions.size() : 0) + " questions");

        if (questions == null || questions.isEmpty()) {
            LOGGER.warning("No questions loaded for assessment: " + assessment.getAssessmentId());

            // Attempt to reload questions directly from the database
            List<String> questionIds = assessment.getQuestionIds();
            if (questionIds != null && !questionIds.isEmpty()) {
                LOGGER.info("Assessment has " + questionIds.size() + " question IDs. Attempting to load them directly.");
                this.questions = new ArrayList<>();
                for (String qId : questionIds) {
                    Question q = QuestionService.getQuestionById(qId);
                    if (q != null) {
                        this.questions.add(q);
                        LOGGER.info("Successfully loaded question: " + q.getQuestionId() + " - " + q.getQuestionText());
                    } else {
                        LOGGER.warning("Failed to load question with ID: " + qId);
                    }
                }
                LOGGER.info("Directly loaded " + this.questions.size() + " questions");
            } else {
                LOGGER.severe("Assessment has no question IDs: " + assessment.getAssessmentId());

                // Force reload question IDs from the database
                try {
                    String query = "SELECT question_id FROM assessment_questions WHERE assessment_id = ? ORDER BY question_order";
                    try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                        stmt.setString(1, assessment.getAssessmentId());
                        ResultSet rs = stmt.executeQuery();

                        List<String> loadedQuestionIds = new ArrayList<>();
                        while (rs.next()) {
                            loadedQuestionIds.add(rs.getString("question_id"));
                        }

                        if (!loadedQuestionIds.isEmpty()) {
                            LOGGER.info("Loaded " + loadedQuestionIds.size() + " question IDs from database");
                            assessment.setQuestionIds(loadedQuestionIds);

                            // Now try to load the questions again
                            this.questions = new ArrayList<>();
                            for (String qId : loadedQuestionIds) {
                                Question q = QuestionService.getQuestionById(qId);
                                if (q != null) {
                                    this.questions.add(q);
                                    LOGGER.info("Successfully loaded question: " + q.getQuestionId() + " - " + q.getQuestionText());
                                } else {
                                    LOGGER.warning("Failed to load question with ID: " + qId);
                                }
                            }
                            LOGGER.info("Directly loaded " + this.questions.size() + " questions after database query");
                        } else {
                            LOGGER.severe("No question IDs found in database for assessment: " + assessment.getAssessmentId());
                            createPlaceholderQuestion();
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.severe("Error loading question IDs from database: " + e.getMessage());
                    createPlaceholderQuestion();
                }
            }
        }

        // Set up the assessment UI
        setupAssessmentUI();

        // Display the first question
        if (!questions.isEmpty()) {
            displayQuestion(0);
        }

        // Attach focus monitoring to the scene
        Platform.runLater(() -> {
            if (timerLabel.getScene() != null) {
                proctoringUtil.attachFocusMonitoring(timerLabel.getScene());
                LOGGER.info("Proctoring initialized for assessment: " + assessment.getAssessmentId());
            }
        });
    }

    /**
     * Creates a placeholder question when no questions can be loaded
     */
    private void createPlaceholderQuestion() {
        // Create a placeholder question for testing if no questions are found
        Question placeholderQuestion = new Question(
                UUID.randomUUID().toString(),
                "This is a placeholder question. The actual questions could not be loaded.",
                Question.TYPE_MULTIPLE_CHOICE,
                "General",
                Question.DIFFICULTY_MEDIUM,
                1,
                assessment.getUnitCode(),
                "system"
        );
        placeholderQuestion.addOption("Option A");
        placeholderQuestion.addOption("Option B");
        placeholderQuestion.addOption("Option C");
        placeholderQuestion.addCorrectAnswer("Option A");

        this.questions = new ArrayList<>();
        this.questions.add(placeholderQuestion);
        LOGGER.info("Added placeholder question for testing");
    }

    /**
     * Sets up the assessment UI
     */
    private void setupAssessmentUI() {
        // Create a new attempt
        String attemptId = UUID.randomUUID().toString();
        this.attempt = new StudentAssessmentAttempt(attemptId, studentId, assessment.getAssessmentId());
        
        // Calculate total possible marks from the actual questions
        int totalPossible = 0;
        for (Question question : questions) {
            totalPossible += question.getMarks();
        }
        
        // Update the assessment's total marks and the attempt's total possible
        assessment.setTotalMarks(totalPossible);
        this.attempt.setTotalPossible(totalPossible);
        
        LOGGER.info("Calculated total possible marks: " + totalPossible + " from " + questions.size() + " questions");

        // Save the attempt to the database before starting focus tracking
        try {
            boolean saved = studentAssessmentService.saveStudentAssessmentAttempt(attempt);
            if (!saved) {
                LOGGER.warning("Failed to save assessment attempt to database. Focus tracking may not work properly.");
            } else {
                LOGGER.info("Assessment attempt saved to database with ID: " + attemptId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving assessment attempt to database", e);
        }

        // Set up the UI
        assessmentTitleText.setText(assessment.getTitle());
        unitLabel.setText(assessment.getUnitCode());
        totalQuestionsLabel.setText(String.valueOf(questions.size()));
        totalMarksLabel.setText(String.valueOf(totalPossible));

        // Set up question navigation
        setupQuestionNavigation();

        // Set up the timer
        setupTimer();

        // We need to wait until the scene is available before initializing the focus tracker
        Platform.runLater(() -> {
            try {
                // Get the current stage
                Stage stage = (Stage) assessmentTitleText.getScene().getWindow();
                if (stage != null) {
                    LOGGER.info("Assessment setup complete for student: " + studentId +
                            ", assessment: " + assessment.getAssessmentId() +
                            ", attempt: " + attempt.getAttemptId());
                } else {
                    LOGGER.warning("Could not get stage: stage is null");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during assessment setup", e);
            }
        });
    }

    /**
     * Sets up the question navigation sidebar
     */
    private void setupQuestionNavigation() {
        questionNavContainer.getChildren().clear();

        for (int i = 0; i < questions.size(); i++) {
            final int index = i;
            Button navButton = new Button("Q" + (i + 1));
            navButton.getStyleClass().add("question-nav-button");
            navButton.setMaxWidth(Double.MAX_VALUE);
            navButton.setOnAction(event -> displayQuestion(index));
            questionNavContainer.getChildren().add(navButton);
        }
    }

    /**
     * Sets up the timer for the assessment
     */
    private void setupTimer() {
        // Calculate seconds based on assessment duration
        secondsRemaining = assessment.getDurationMinutes() * 60;
        updateTimerDisplay();

        // Create and start the timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsRemaining--;
            updateTimerDisplay();

            if (secondsRemaining <= 0) {
                timer.stop();
                isTimeUp = true;
                handleTimeUp();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    /**
     * Updates the timer display
     */
    private void updateTimerDisplay() {
        int hours = secondsRemaining / 3600;
        int minutes = (secondsRemaining % 3600) / 60;
        int seconds = secondsRemaining % 60;

        timerLabel.setText(String.format("Time Remaining: %02d:%02d:%02d", hours, minutes, seconds));

        // Change color to red when less than 5 minutes remaining
        if (secondsRemaining < 300) {
            timerLabel.getStyleClass().add("timer-warning");
        }
    }

    /**
     * Handles when time is up
     */
    private void handleTimeUp() {
        // Show alert
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Time's Up");
        alert.setHeaderText("The assessment time has expired");
        alert.setContentText("Your assessment will be automatically submitted.");
        alert.showAndWait();

        // Submit the assessment
        handleSubmitAssessment(null);
    }

    /**
     * Displays a question at the given index
     *
     * @param index The index of the question to display
     */
    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            LOGGER.warning("Invalid question index: " + index + ", questions size: " + questions.size());
            return;
        }

        // Save the current response before changing questions
        saveCurrentResponse();

        currentQuestionIndex = index;
        Question question = questions.get(index);

        LOGGER.info("Displaying question " + (index + 1) + ": " + question.getQuestionId());
        LOGGER.info("Question text: " + question.getQuestionText());
        LOGGER.info("Question type: " + question.getQuestionType());

        // Update question display
        questionNumberLabel.setText("Question " + (index + 1));
        questionMarksLabel.setText("(" + question.getMarks() + " marks)");

        // Ensure question text is set and visible
        if (questionTextLabel != null) {
            questionTextLabel.setText(question.getQuestionText());
            questionTextLabel.setVisible(true);

            // Make sure the question text is not empty
            if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
                questionTextLabel.setText("Question text is missing. Please contact your instructor.");
                LOGGER.warning("Empty question text for question ID: " + question.getQuestionId());
            }
        } else {
            LOGGER.severe("questionTextLabel is null - UI component not initialized properly");
        }

        // Update navigation buttons
        previousButton.setDisable(index == 0);
        nextButton.setDisable(index == questions.size() - 1);

        // Update question navigation buttons to show which ones have been answered
        updateQuestionNavigation();

        // Set up the appropriate answer container based on question type
        setupAnswerContainer(question);

        // Load any existing response
        loadExistingResponse(question);
    }

    /**
     * Sets up the answer container based on question type
     *
     * @param question The question to display
     */
    private void setupAnswerContainer(Question question) {
        LOGGER.info("Setting up answer container for question type: " + question.getQuestionType());

        // Hide all containers first
        multipleChoiceContainer.setVisible(false);
        shortAnswerContainer.setVisible(false);
        listBasedContainer.setVisible(false);

        // Show the appropriate container based on question type
        switch (question.getQuestionType()) {
            case Question.TYPE_MULTIPLE_CHOICE -> {
                LOGGER.info("Displaying multiple choice container");
                multipleChoiceContainer.setVisible(true);
                setupMultipleChoiceOptions(question);
            }
            case Question.TYPE_SHORT_ANSWER -> {
                LOGGER.info("Displaying short answer container");
                shortAnswerContainer.setVisible(true);
                shortAnswerField.clear();
            }
            case Question.TYPE_LIST_BASED -> {
                LOGGER.info("Displaying list-based container");
                listBasedContainer.setVisible(true);
                listAnswerField.clear();
            }
            case Question.TYPE_TRUE_FALSE -> {
                LOGGER.info("Handling true/false question as multiple choice");
                multipleChoiceContainer.setVisible(true);
                // Create true/false options if not already in the question
                if (question.getOptions() == null || question.getOptions().isEmpty()) {
                    question.clearOptions();
                    question.addOption("True");
                    question.addOption("False");
                }
                setupMultipleChoiceOptions(question);
            }
            default -> {
                LOGGER.warning("Unknown question type: " + question.getQuestionType() +
                        ". Defaulting to multiple choice container.");
                multipleChoiceContainer.setVisible(true);
                // If no options are available, add some placeholder options
                if (question.getOptions() == null || question.getOptions().isEmpty()) {
                    question.clearOptions();
                    question.addOption("Option A");
                    question.addOption("Option B");
                    question.addOption("Option C");
                }
                setupMultipleChoiceOptions(question);
            }
        }

        // Verify that at least one container is visible
        if (!multipleChoiceContainer.isVisible() &&
                !shortAnswerContainer.isVisible() &&
                !listBasedContainer.isVisible()) {
            LOGGER.severe("No answer container is visible for question type: " + question.getQuestionType());
            // Force multiple choice container to be visible as a fallback
            multipleChoiceContainer.setVisible(true);
            if (question.getOptions() == null || question.getOptions().isEmpty()) {
                question.clearOptions();
                question.addOption("Option A");
                question.addOption("Option B");
                question.addOption("Option C");
            }
            setupMultipleChoiceOptions(question);
        }
    }

    /**
     * Sets up multiple choice options
     *
     * @param question The multiple choice question
     */
    private void setupMultipleChoiceOptions(Question question) {
        LOGGER.info("Setting up multiple choice options");

        // Clear existing options
        multipleChoiceContainer.getChildren().clear();
        optionButtons.clear();

        // Create radio buttons for each option
        List<String> options = question.getOptions();

        if (options == null || options.isEmpty()) {
            LOGGER.warning("No options available for multiple choice question: " + question.getQuestionId());
            // Add some default options as a fallback
            options = new ArrayList<>();
            options.add("Option A");
            options.add("Option B");
            options.add("Option C");
            LOGGER.info("Added default options as fallback");
        }

        LOGGER.info("Creating " + options.size() + " option buttons");

        for (int i = 0; i < options.size(); i++) {
            String optionText = options.get(i);
            RadioButton optionButton = new RadioButton(optionText);
            optionButton.setToggleGroup(answerGroup);
            optionButton.setWrapText(true);
            optionButton.setUserData(optionText); // Store the option text as user data
            optionButtons.add(optionButton);
            multipleChoiceContainer.getChildren().add(optionButton);

            LOGGER.info("Added option button: " + optionText);
        }

        // Ensure the container has children
        if (multipleChoiceContainer.getChildren().isEmpty()) {
            LOGGER.severe("Multiple choice container has no children after setup");
            Label errorLabel = new Label("Error: No options available for this question.");
            errorLabel.getStyleClass().add("error-label");
            multipleChoiceContainer.getChildren().add(errorLabel);
        }
    }

    /**
     * Loads an existing response for the current question if one exists
     *
     * @param question The current question
     */
    private void loadExistingResponse(Question question) {
        StudentResponse response = attempt.getResponse(question.getQuestionId());
        if (response == null) {
            return;
        }

        String responseText = response.getResponseText();

        switch (question.getQuestionType()) {
            case Question.TYPE_MULTIPLE_CHOICE -> {
                for (RadioButton button : optionButtons) {
                    if (button.getText().equals(responseText)) {
                        button.setSelected(true);
                        break;
                    }
                }
            }
            case Question.TYPE_SHORT_ANSWER -> shortAnswerField.setText(responseText);
            case Question.TYPE_LIST_BASED -> listAnswerField.setText(responseText);
        }
    }

    /**
     * Saves the current response before moving to another question
     */
    private void saveCurrentResponse() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        String responseText = getResponseText(question);

        // Only save if there's a response
        if (responseText != null && !responseText.isEmpty()) {
            String responseId = UUID.randomUUID().toString();
            StudentResponse response = new StudentResponse(
                    responseId,
                    attempt.getAttemptId(),
                    question.getQuestionId(),
                    responseText
            );

            attempt.getResponses().put(question.getQuestionId(), response);
        }
    }

    /**
     * Gets the response text based on question type
     *
     * @param question The question
     * @return The response text, or null if no response
     */
    private String getResponseText(Question question) {
        switch (question.getQuestionType()) {
            case Question.TYPE_MULTIPLE_CHOICE -> {
                Toggle selectedToggle = answerGroup.getSelectedToggle();
                if (selectedToggle != null) {
                    // For MCQs, store the exact text of the selected option to ensure
                    // it matches the format of the correct answer in the database
                    String selectedOption = selectedToggle.getUserData().toString();
                    System.out.println("Selected MCQ option: " + selectedOption);
                    return selectedOption;
                }
            }
            case Question.TYPE_SHORT_ANSWER -> {
                return shortAnswerField.getText().trim();
            }
            case Question.TYPE_LIST_BASED -> {
                return listAnswerField.getText().trim();
            }
        }
        return null;
    }

    /**
     * Updates the question navigation to show which questions have been answered
     */
    private void updateQuestionNavigation() {
        for (int i = 0; i < questions.size(); i++) {
            Button navButton = (Button) questionNavContainer.getChildren().get(i);

            // Reset styles
            navButton.getStyleClass().remove("question-nav-answered");
            navButton.getStyleClass().remove("question-nav-current");

            // Mark current question
            if (i == currentQuestionIndex) {
                navButton.getStyleClass().add("question-nav-current");
            }

            // Mark answered questions
            if (attempt.getResponse(questions.get(i).getQuestionId()) != null) {
                navButton.getStyleClass().add("question-nav-answered");
            }
        }
    }

    /**
     * Handles the previous question button
     */
    @FXML
    protected void handlePreviousQuestion(ActionEvent event) {
        if (currentQuestionIndex > 0) {
            displayQuestion(currentQuestionIndex - 1);
        }
    }

    /**
     * Handles the next question button
     */
    @FXML
    protected void handleNextQuestion(ActionEvent event) {
        if (currentQuestionIndex < questions.size() - 1) {
            displayQuestion(currentQuestionIndex + 1);
        }
    }

    /**
     * Handles the save answer button
     */
    @FXML
    protected void handleSaveAnswer(ActionEvent event) {
        saveCurrentResponse();
        updateQuestionNavigation();

        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Answer Saved");
        alert.setHeaderText("Response Saved");
        alert.setContentText("Your answer has been saved.");
        alert.showAndWait();
    }

    /**
     * Handles the submit assessment button
     */
    @FXML
    protected void handleSubmitAssessment(ActionEvent event) {
        try {
            // Confirm submission
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Submit Assessment");
            confirmAlert.setHeaderText("Are you sure you want to submit this assessment?");
            confirmAlert.setContentText("You will not be able to change your answers after submission.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Save current response before submitting
                saveCurrentResponse();
                
                // Update attempt status and end time
                attempt.setStatus("COMPLETED");
                attempt.setEndTime(LocalDateTime.now());
                
                // Calculate total possible marks
                int totalPossible = 0;
                for (Question question : questions) {
                    totalPossible += question.getMarks();
                }
                attempt.setTotalPossible(totalPossible);
                
                // Auto-grade multiple choice questions
                int score = autoGradeMultipleChoiceResponses();
                attempt.setScore(score);
                
                // Save the attempt
                saveAttempt(attempt);
                
                // Add proctoring information to the assessment results
                String proctoringInfo = "Focus violations: " + proctoringUtil.getViolationCount() +
                        ", Total time out of focus: " + proctoringUtil.getTotalViolationDuration() + " seconds";
                LOGGER.info("Proctoring summary: " + proctoringInfo);
                
                // Show results
                showResults();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting assessment", e);
            AlertUtil.showError("Error", "Could not submit assessment: " + e.getMessage());
        }
    }

    /**
     * Auto-grades multiple choice responses.
     *
     * @return The total score
     */
    private int autoGradeMultipleChoiceResponses() {
        int totalScore = 0;

        for (Question question : questions) {
            StudentResponse response = attempt.getResponse(question.getQuestionId());

            if (response != null && question.getQuestionType().equals(Question.TYPE_MULTIPLE_CHOICE)) {
                String selectedAnswer = response.getResponseText();
                
                // Check if the selected answer is correct
                boolean isCorrect = false;
                
                // Get the correct answers from the question
                List<String> correctAnswers = question.getCorrectAnswers();
                if (correctAnswers == null || correctAnswers.isEmpty()) {
                    // Fallback to single correct answer if list is empty
                    String correctAnswer = question.getCorrectAnswer();
                    if (correctAnswer != null && !correctAnswer.isEmpty()) {
                        correctAnswers = new ArrayList<>();
                        correctAnswers.add(correctAnswer);
                    }
                }
                
                // Check if the selected answer matches any correct answer
                if (correctAnswers != null && !correctAnswers.isEmpty()) {
                    for (String correctAnswer : correctAnswers) {
                        // Normalize both strings for comparison
                        String normalizedSelected = selectedAnswer.trim().toLowerCase();
                        String normalizedCorrect = correctAnswer.trim().toLowerCase();
                        
                        if (normalizedSelected.equals(normalizedCorrect)) {
                            isCorrect = true;
                            break;
                        }
                        
                        // Try removing any option prefixes like "A. ", "B. ", etc.
                        String cleanedSelected = normalizedSelected.replaceAll("^[a-z]\\. ", "").trim();
                        String cleanedCorrect = normalizedCorrect.replaceAll("^[a-z]\\. ", "").trim();
                        
                        if (cleanedSelected.equals(cleanedCorrect)) {
                            isCorrect = true;
                            break;
                        }
                    }
                }
                
                if (isCorrect) {
                    response.setMarksAwarded(question.getMarks());
                    response.setCorrect(true);
                    totalScore += question.getMarks();
                    LOGGER.info("Question " + question.getQuestionId() + " marked correct: " + question.getMarks() + " marks");
                } else {
                    response.setMarksAwarded(0);
                    response.setCorrect(false);
                    LOGGER.info("Question " + question.getQuestionId() + " marked incorrect");
                }

                // Update the response in the attempt
                attempt.getResponses().put(question.getQuestionId(), response);
            }
        }

        LOGGER.info("Auto-grading complete. Total score: " + totalScore);
        return totalScore;
    }

    /**
     * Saves the attempt to the database.
     *
     * @param attempt The attempt to save
     */
    private void saveAttempt(StudentAssessmentAttempt attempt) {
        try {
            // First check if the attempt exists in the database
            String checkSql = "SELECT attempt_id FROM student_assessments WHERE attempt_id = ?";
            boolean attemptExists = false;
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                
                stmt.setString(1, attempt.getAttemptId());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    attemptExists = rs.next();
                }
            }
            
            // If attempt doesn't exist, insert it first
            if (!attemptExists) {
                String insertSql = "INSERT INTO student_assessments (attempt_id, student_id, assessment_id, start_time, end_time, score, total_possible, status) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    
                    stmt.setString(1, attempt.getAttemptId());
                    stmt.setString(2, attempt.getStudentId());
                    stmt.setString(3, attempt.getAssessmentId());
                    stmt.setObject(4, attempt.getStartTime());
                    stmt.setObject(5, attempt.getEndTime());
                    stmt.setInt(6, attempt.getScore());
                    stmt.setInt(7, attempt.getTotalPossible());
                    stmt.setString(8, attempt.getStatus());
                    
                    stmt.executeUpdate();
                    LOGGER.info("Inserted new assessment attempt: " + attempt.getAttemptId());
                }
            } else {
                // Update existing attempt
                String updateSql = "UPDATE student_assessments SET end_time = ?, score = ?, status = ? WHERE attempt_id = ?";
                
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    
                    stmt.setObject(1, attempt.getEndTime());
                    stmt.setInt(2, attempt.getScore());
                    stmt.setString(3, attempt.getStatus());
                    stmt.setString(4, attempt.getAttemptId());
                    
                    stmt.executeUpdate();
                    LOGGER.info("Updated assessment attempt: " + attempt.getAttemptId());
                }
            }

            // Then save all responses
            for (StudentResponse response : attempt.getResponses().values()) {
                saveResponse(response);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving assessment attempt", e);
            throw new RuntimeException("Failed to save assessment attempt", e);
        }
    }

    /**
     * Saves a student response to the database.
     *
     * @param response The response to save
     */
    private void saveResponse(StudentResponse response) {
        try {
            String sql = "UPDATE student_responses SET response_text = ?, marks_awarded = ? WHERE response_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, response.getResponseText());
                stmt.setInt(2, response.getMarksAwarded());
                stmt.setString(3, response.getResponseId());

                int updated = stmt.executeUpdate();

                if (updated == 0) {
                    // Response doesn't exist, insert it
                    sql = "INSERT INTO student_responses (response_id, attempt_id, question_id, response_text, marks_awarded) VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
                        insertStmt.setString(1, response.getResponseId());
                        insertStmt.setString(2, response.getAttemptId());
                        insertStmt.setString(3, response.getQuestionId());
                        insertStmt.setString(4, response.getResponseText());
                        insertStmt.setInt(5, response.getMarksAwarded());

                        insertStmt.executeUpdate();
                    }
                }

                LOGGER.info("Saved response for question: " + response.getQuestionId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving student response", e);
            throw new RuntimeException("Failed to save student response", e);
        }
    }

    /**
     * Shows the assessment results
     */
    private void showResults() {
        try {
            // Load the assessment results view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/book/javafx/kenyattacatsystem/views/assessment-results-view.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the attempt data
            AssessmentResultsController controller = loader.getController();
            controller.setAttempt(attempt, questions, assessment);

            // Create a new stage for the results
            Stage resultsStage = new Stage();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/book/javafx/kenyattacatsystem/styles/main.css").toExternalForm());

            // Set up the stage
            resultsStage.setScene(scene);
            resultsStage.setTitle("Assessment Results");

            // Close the assessment taking window
            Stage currentStage = (Stage) submitButton.getScene().getWindow();
            currentStage.close();

            // Show the results stage
            resultsStage.show();
        } catch (IOException e) {
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error Loading Results");
            alert.setContentText("There was an error loading the assessment results: " + e.getMessage());
            alert.showAndWait();
        }
    }
}