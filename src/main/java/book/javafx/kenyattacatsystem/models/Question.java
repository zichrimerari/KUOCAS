package book.javafx.kenyattacatsystem.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a question in the system.
 * Supports different question types: multiple-choice, short answer, and list-based.
 */
public class Question {
    // Question types
    public static final String TYPE_MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String TYPE_SHORT_ANSWER = "SHORT_ANSWER";
    public static final String TYPE_LIST_BASED = "LIST_BASED";
    public static final String TYPE_TRUE_FALSE = "TRUE_FALSE";
    
    // Difficulty levels
    public static final String DIFFICULTY_EASY = "EASY";
    public static final String DIFFICULTY_MEDIUM = "MEDIUM";
    public static final String DIFFICULTY_HARD = "HARD";
    
    private String questionId;
    private String questionText;
    private String questionType; // One of the TYPE_* constants
    private String topic; // Topic this question belongs to
    private String difficulty; // One of the DIFFICULTY_* constants
    private List<String> options; // For multiple-choice questions
    private List<String> correctAnswers; // Can be multiple for list-based questions
    private int marks;
    private String unitCode; // The unit this question belongs to
    private String createdBy; // User ID of the creator (lecturer or student)
    private LocalDateTime creationDate;
    private boolean approved; // Whether the question is approved by a lecturer
    private String feedback; // Feedback on why a question was rejected or approved
    
    /**
     * Default constructor
     */
    public Question() {
        this.options = new ArrayList<>();
        this.correctAnswers = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        this.approved = false;
    }
    
    /**
     * Constructor with minimal fields for service use
     *
     * @param questionId   The question ID
     * @param questionText The question text
     * @param questionType The question type
     * @param marks        The marks for this question
     * @param unitCode     The unit code
     * @param createdBy    The creator's user ID
     */
    public Question(String questionId, String questionText, String questionType, 
                   int marks, String unitCode, String createdBy) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks = marks;
        this.unitCode = unitCode;
        this.createdBy = createdBy;
        this.options = new ArrayList<>();
        this.correctAnswers = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        this.approved = false;
        this.difficulty = DIFFICULTY_MEDIUM; // Default difficulty
        this.topic = "General"; // Default topic
    }
    
    /**
     * Constructor with essential fields
     *
     * @param questionId   The question ID
     * @param questionText The question text
     * @param questionType The question type
     * @param topic        The topic
     * @param difficulty   The difficulty level
     * @param marks        The marks for this question
     * @param unitCode     The unit code
     * @param createdBy    The creator's user ID
     */
    public Question(String questionId, String questionText, String questionType, 
                   String topic, String difficulty, int marks, String unitCode, String createdBy) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.topic = topic;
        this.difficulty = difficulty;
        this.marks = marks;
        this.unitCode = unitCode;
        this.createdBy = createdBy;
        this.options = new ArrayList<>();
        this.correctAnswers = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        this.approved = false;
    }
    
    /**
     * Constructor with all fields
     *
     * @param questionId     The question ID
     * @param questionText   The question text
     * @param questionType   The question type
     * @param topic          The topic
     * @param difficulty     The difficulty level
     * @param options        The options for multiple-choice questions
     * @param correctAnswers The correct answers
     * @param marks          The marks for this question
     * @param unitCode       The unit code
     * @param createdBy      The creator's user ID
     * @param creationDate   The creation date
     * @param approved       Whether the question is approved
     * @param feedback       Feedback on the question
     */
    public Question(String questionId, String questionText, String questionType, 
                   String topic, String difficulty, List<String> options, List<String> correctAnswers, int marks, 
                   String unitCode, String createdBy, LocalDateTime creationDate, 
                   boolean approved, String feedback) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.topic = topic;
        this.difficulty = difficulty;
        this.options = options;
        this.correctAnswers = correctAnswers;
        this.marks = marks;
        this.unitCode = unitCode;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.approved = approved;
        this.feedback = feedback;
    }
    
    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public int getMarks() {
        return marks;
    }
    
    public void setMarks(int marks) {
        this.marks = marks;
    }
    
    public String getUnitCode() {
        return unitCode;
    }
    
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    /**
     * Gets the question text (alias for getQuestionText for compatibility)
     * 
     * @return The question text
     */
    public String getText() {
        return questionText;
    }
    
    /**
     * Gets the question type (alias for getQuestionType for compatibility)
     * 
     * @return The question type
     */
    public String getType() {
        return questionType;
    }
    
    /**
     * Gets the correct answer (for single-answer questions)
     * 
     * @return The first correct answer or null if none exists
     */
    public String getCorrectAnswer() {
        return correctAnswers != null && !correctAnswers.isEmpty() ? correctAnswers.get(0) : null;
    }
    
    /**
     * Adds an option to a multiple-choice question
     *
     * @param option The option text
     */
    public void addOption(String option) {
        if (!options.contains(option)) {
            options.add(option);
        }
    }
    
    /**
     * Removes an option from a multiple-choice question
     *
     * @param option The option text
     */
    public void removeOption(String option) {
        options.remove(option);
    }
    
    /**
     * Adds a correct answer
     *
     * @param answer The correct answer
     */
    public void addCorrectAnswer(String answer) {
        if (!correctAnswers.contains(answer)) {
            correctAnswers.add(answer);
        }
    }
    
    /**
     * Removes a correct answer
     *
     * @param answer The answer to remove
     */
    public void removeCorrectAnswer(String answer) {
        correctAnswers.remove(answer);
    }
    
    /**
     * Checks if a given answer is correct
     *
     * @param answer The answer to check
     * @return True if the answer is correct, false otherwise
     */
    public boolean isCorrectAnswer(String answer) {
        return correctAnswers.contains(answer);
    }
    
    /**
     * Checks if a list of answers is correct (for list-based questions)
     *
     * @param answers The list of answers to check
     * @return True if all answers are correct, false otherwise
     */
    public boolean areCorrectAnswers(List<String> answers) {
        return correctAnswers.containsAll(answers) && answers.size() == correctAnswers.size();
    }
    
    /**
     * Calculates the partial score for a list of answers (for list-based questions)
     *
     * @param answers The list of answers to check
     * @return The partial score based on correct answers
     */
    public int calculatePartialScore(List<String> answers) {
        if (questionType.equals(TYPE_LIST_BASED)) {
            int correctCount = 0;
            for (String answer : answers) {
                if (correctAnswers.contains(answer)) {
                    correctCount++;
                }
            }
            return (int) Math.round((double) correctCount / correctAnswers.size() * marks);
        } else if (questionType.equals(TYPE_MULTIPLE_CHOICE) || questionType.equals(TYPE_SHORT_ANSWER)) {
            return isCorrectAnswer(answers.get(0)) ? marks : 0;
        }
        return 0;
    }
    
    /**
     * Clears all options for this question
     */
    public void clearOptions() {
        if (this.options != null) {
            this.options.clear();
        } else {
            this.options = new ArrayList<>();
        }
    }
    
    @Override
    public String toString() {
        return "Question{" +
                "questionId='" + questionId + '\'' +
                ", questionText='" + questionText + '\'' +
                ", questionType='" + questionType + '\'' +
                ", topic='" + topic + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", options=" + options +
                ", correctAnswers=" + correctAnswers +
                ", marks=" + marks +
                ", unitCode='" + unitCode + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", approved=" + approved +
                '}';
    }
}