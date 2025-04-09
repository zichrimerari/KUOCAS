package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Question;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for question-related operations.
 * Handles creating, retrieving, and managing questions for assessments.
 */
public class QuestionService {
    private static final Logger LOGGER = Logger.getLogger(QuestionService.class.getName());

    /**
     * Creates a new question.
     *
     * @param questionText   The question text
     * @param questionType   The question type (MULTIPLE_CHOICE, SHORT_ANSWER, LIST_BASED)
     * @param options        The options for multiple-choice questions
     * @param correctAnswers The correct answers
     * @param marks          The marks for this question
     * @param unitCode       The unit code
     * @param createdBy      The creator's user ID
     * @param approved       Whether the question is approved
     * @param topic          The topic of the question
     * @param difficulty     The difficulty level of the question (EASY, MEDIUM, HARD)
     * @return The newly created question, or null if creation fails
     */
    public static Question createQuestion(String questionText, String questionType,
                                          List<String> options, List<String> correctAnswers,
                                          int marks, String unitCode, String createdBy,
                                          boolean approved, String topic, String difficulty) {
        String questionId = UUID.randomUUID().toString();

        // Convert lists to comma-separated strings for storage
        String optionsStr = options != null ? String.join(",", options) : "";
        String correctAnswersStr = correctAnswers != null ? String.join(",", correctAnswers) : "";

        // If difficulty is not provided, determine it based on marks
        if (difficulty == null || difficulty.isEmpty()) {
            if (marks <= 2) {
                difficulty = Question.DIFFICULTY_EASY;
            } else if (marks <= 4) {
                difficulty = Question.DIFFICULTY_MEDIUM;
            } else {
                difficulty = Question.DIFFICULTY_HARD;
            }
        }

        String query = "INSERT INTO questions (question_id, question_text, question_type, options, "
                + "correct_answers, marks, unit_code, created_by, approved, topic, difficulty) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, questionId);
            stmt.setString(2, questionText);
            stmt.setString(3, questionType);
            stmt.setString(4, optionsStr);
            stmt.setString(5, correctAnswersStr);
            stmt.setInt(6, marks);
            stmt.setString(7, unitCode);
            stmt.setString(8, createdBy);
            stmt.setBoolean(9, approved);
            stmt.setString(10, topic);
            stmt.setString(11, difficulty);

            if (stmt.executeUpdate() > 0) {
                Question question = new Question(questionId, questionText, questionType, 
                                               topic, difficulty, marks, unitCode, createdBy);

                if (options != null) {
                    for (String option : options) {
                        question.addOption(option);
                    }
                }

                if (correctAnswers != null) {
                    for (String answer : correctAnswers) {
                        question.addCorrectAnswer(answer);
                    }
                }

                question.setApproved(approved);
                return question;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating question", e);
        }

        return null;
    }

    /**
     * Creates a new question (overloaded method for backward compatibility).
     *
     * @param questionText   The question text
     * @param questionType   The question type (MULTIPLE_CHOICE, SHORT_ANSWER, LIST_BASED)
     * @param options        The options for multiple-choice questions
     * @param correctAnswers The correct answers
     * @param marks          The marks for this question
     * @param unitCode       The unit code
     * @param createdBy      The creator's user ID
     * @param approved       Whether the question is approved
     * @param topic          The topic of the question
     * @return The newly created question, or null if creation fails
     */
    public static Question createQuestion(String questionText, String questionType,
                                          List<String> options, List<String> correctAnswers,
                                          int marks, String unitCode, String createdBy,
                                          boolean approved, String topic) {
        // Determine difficulty based on marks
        String difficulty;
        if (marks <= 2) {
            difficulty = Question.DIFFICULTY_EASY;
        } else if (marks <= 4) {
            difficulty = Question.DIFFICULTY_MEDIUM;
        } else {
            difficulty = Question.DIFFICULTY_HARD;
        }
        
        return createQuestion(questionText, questionType, options, correctAnswers, 
                             marks, unitCode, createdBy, approved, topic, difficulty);
    }

    /**
     * Gets a question by ID.
     *
     * @param questionId The question ID
     * @return The question, or null if not found
     */
    public static Question getQuestionById(String questionId) {
        String query = "SELECT * FROM questions WHERE question_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Question question = new Question(
                        rs.getString("question_id"),
                        rs.getString("question_text"),
                        rs.getString("question_type"),
                        rs.getInt("marks"),
                        rs.getString("unit_code"),
                        rs.getString("created_by")
                );

                // Parse options and correct answers from comma-separated strings
                String optionsStr = rs.getString("options");
                if (optionsStr != null && !optionsStr.isEmpty()) {
                    String[] options = optionsStr.split(",");
                    for (String option : options) {
                        question.addOption(option.trim());
                    }
                }

                String correctAnswersStr = rs.getString("correct_answers");
                if (correctAnswersStr != null && !correctAnswersStr.isEmpty()) {
                    String[] correctAnswers = correctAnswersStr.split(",");
                    for (String answer : correctAnswers) {
                        question.addCorrectAnswer(answer.trim());
                    }
                }

                question.setApproved(rs.getBoolean("approved"));
                question.setFeedback(rs.getString("feedback"));
                question.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                question.setDifficulty(rs.getString("difficulty"));
                question.setTopic(rs.getString("topic"));

                return question;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting question by ID", e);
        }

        return null;
    }

    /**
     * Gets all questions for a unit.
     *
     * @param unitCode     The unit code
     * @param approvedOnly Whether to return only approved questions
     * @return A list of questions for the unit
     */
    public static List<Question> getQuestionsByUnit(String unitCode, boolean approvedOnly) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE unit_code = ?";

        if (approvedOnly) {
            query += " AND approved = 1";
        }

        query += " ORDER BY creation_date DESC";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Question question = new Question(
                        rs.getString("question_id"),
                        rs.getString("question_text"),
                        rs.getString("question_type"),
                        rs.getInt("marks"),
                        rs.getString("unit_code"),
                        rs.getString("created_by")
                );

                // Parse options and correct answers from comma-separated strings
                String optionsStr = rs.getString("options");
                if (optionsStr != null && !optionsStr.isEmpty()) {
                    String[] options = optionsStr.split(",");
                    for (String option : options) {
                        question.addOption(option.trim());
                    }
                }

                String correctAnswersStr = rs.getString("correct_answers");
                if (correctAnswersStr != null && !correctAnswersStr.isEmpty()) {
                    String[] correctAnswers = correctAnswersStr.split(",");
                    for (String answer : correctAnswers) {
                        question.addCorrectAnswer(answer.trim());
                    }
                }

                question.setApproved(rs.getBoolean("approved"));
                question.setFeedback(rs.getString("feedback"));
                question.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                question.setDifficulty(rs.getString("difficulty"));
                question.setTopic(rs.getString("topic"));

                questions.add(question);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting questions by unit", e);
        }

        return questions;
    }

    /**
     * Gets all questions created by a user.
     *
     * @param userId The user ID
     * @return A list of questions created by the user
     */
    public static List<Question> getQuestionsByCreator(String userId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE created_by = ? ORDER BY creation_date DESC";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Question question = new Question(
                        rs.getString("question_id"),
                        rs.getString("question_text"),
                        rs.getString("question_type"),
                        rs.getInt("marks"),
                        rs.getString("unit_code"),
                        rs.getString("created_by")
                );

                // Parse options and correct answers from comma-separated strings
                String optionsStr = rs.getString("options");
                if (optionsStr != null && !optionsStr.isEmpty()) {
                    String[] options = optionsStr.split(",");
                    for (String option : options) {
                        question.addOption(option.trim());
                    }
                }

                String correctAnswersStr = rs.getString("correct_answers");
                if (correctAnswersStr != null && !correctAnswersStr.isEmpty()) {
                    String[] correctAnswers = correctAnswersStr.split(",");
                    for (String answer : correctAnswers) {
                        question.addCorrectAnswer(answer.trim());
                    }
                }

                question.setApproved(rs.getBoolean("approved"));
                question.setFeedback(rs.getString("feedback"));
                question.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                question.setDifficulty(rs.getString("difficulty"));
                question.setTopic(rs.getString("topic"));

                questions.add(question);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting questions by creator", e);
        }

        return questions;
    }

    /**
     * Updates a question's details.
     *
     * @param question The question with updated details
     * @return True if the update was successful, false otherwise
     */
    public static boolean updateQuestion(Question question) {
        // Convert lists to comma-separated strings for storage
        String optionsStr = String.join(",", question.getOptions());
        String correctAnswersStr = String.join(",", question.getCorrectAnswers());

        String query = "UPDATE questions SET question_text = ?, question_type = ?, options = ?, "
                + "correct_answers = ?, marks = ?, approved = ?, feedback = ?, difficulty = ?, topic = ? WHERE question_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, question.getQuestionType());
            stmt.setString(3, optionsStr);
            stmt.setString(4, correctAnswersStr);
            stmt.setInt(5, question.getMarks());
            stmt.setBoolean(6, question.isApproved());
            stmt.setString(7, question.getFeedback());
            stmt.setString(8, question.getDifficulty());
            stmt.setString(9, question.getTopic());
            stmt.setString(10, question.getQuestionId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating question", e);
        }

        return false;
    }

    /**
     * Approves or rejects a question.
     *
     * @param questionId The question ID
     * @param approved   Whether the question is approved
     * @param feedback   Feedback on why the question was approved or rejected
     * @return True if the update was successful, false otherwise
     */
    public static boolean reviewQuestion(String questionId, boolean approved, String feedback) {
        String query = "UPDATE questions SET approved = ?, feedback = ? WHERE question_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setBoolean(1, approved);
            stmt.setString(2, feedback);
            stmt.setString(3, questionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reviewing question", e);
        }

        return false;
    }

    /**
     * Deletes a question.
     *
     * @param questionId The question ID
     * @return True if the deletion was successful, false otherwise
     */
    public static boolean deleteQuestion(String questionId) {
        String query = "DELETE FROM questions WHERE question_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, questionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting question", e);
        }

        return false;
    }

    /**
     * Gets pending questions that need approval.
     *
     * @param unitCode The unit code (optional, can be null to get all pending questions)
     * @return A list of pending questions
     */


    public static List<Question> getPendingQuestions(String unitCode) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE approved = 0";

        if (unitCode != null && !unitCode.isEmpty()) {
            query += " AND unit_code = ?";
        }

        query += " ORDER BY creation_date ASC";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            if (unitCode != null && !unitCode.isEmpty()) {
                stmt.setString(1, unitCode);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question(
                            rs.getString("question_id"),
                            rs.getString("question_text"),
                            rs.getString("question_type"),
                            rs.getInt("marks"),
                            rs.getString("unit_code"),
                            rs.getString("created_by")
                    );

                    // Parse options and correct answers from comma-separated strings
                    String optionsStr = rs.getString("options");
                    if (optionsStr != null && !optionsStr.isEmpty()) {
                        question.setOptions(Arrays.asList(optionsStr.split(",")));
                    }

                    String correctAnswersStr = rs.getString("correct_answers");
                    if (correctAnswersStr != null && !correctAnswersStr.isEmpty()) {
                        question.setCorrectAnswers(Arrays.asList(correctAnswersStr.split(",")));
                    }

                    question.setApproved(rs.getBoolean("approved"));
                    question.setFeedback(rs.getString("feedback"));
                    question.setDifficulty(rs.getString("difficulty"));
                    question.setTopic(rs.getString("topic"));

                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending questions: " + e.getMessage());
            e.printStackTrace();
        }

        return questions;
    }

    /**
     * Updates the topic of a question.
     *
     * @param questionId The question ID
     * @param topicName The topic name
     * @return True if the update was successful, false otherwise
     */
    public static boolean updateQuestionTopic(String questionId, String topicName) {
        String query = "UPDATE questions SET topic = ? WHERE question_id = ?";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, topicName);
            stmt.setString(2, questionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating question topic", e);
            return false;
        }
    }

    /**
     * Gets all questions for a specific assessment.
     *
     * @param assessmentId The assessment ID
     * @return A list of questions in the assessment
     */
    public static List<Question> getQuestionsByAssessment(String assessmentId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT q.* FROM questions q " +
                "JOIN assessment_questions aq ON q.question_id = aq.question_id " +
                "WHERE aq.assessment_id = ? " +
                "ORDER BY aq.question_order ASC";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question(
                            rs.getString("question_id"),
                            rs.getString("question_text"),
                            rs.getString("question_type"),
                            rs.getInt("marks"),
                            rs.getString("unit_code"),
                            rs.getString("created_by")
                    );

                    // Parse options and correct answers from comma-separated strings
                    String optionsStr = rs.getString("options");
                    if (optionsStr != null && !optionsStr.isEmpty()) {
                        String[] options = optionsStr.split(",");
                        for (String option : options) {
                            question.addOption(option.trim());
                        }
                    }

                    String correctAnswersStr = rs.getString("correct_answers");
                    if (correctAnswersStr != null && !correctAnswersStr.isEmpty()) {
                        String[] correctAnswers = correctAnswersStr.split(",");
                        for (String answer : correctAnswers) {
                            question.addCorrectAnswer(answer.trim());
                        }
                    }

                    question.setApproved(rs.getBoolean("approved"));
                    question.setFeedback(rs.getString("feedback"));
                    question.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                    question.setDifficulty(rs.getString("difficulty"));
                    question.setTopic(rs.getString("topic"));

                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting questions by assessment", e);
        }

        return questions;
    }
}
