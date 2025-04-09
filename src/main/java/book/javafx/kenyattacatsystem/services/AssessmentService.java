package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Assessment;
import book.javafx.kenyattacatsystem.models.Question;
import book.javafx.kenyattacatsystem.models.Topic;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for assessment-related operations.
 * Handles creating, retrieving, and managing assessments.
 */
public class AssessmentService {
    private static final Logger LOGGER = Logger.getLogger(AssessmentService.class.getName());

    /**
     * Creates a new assessment.
     *
     * @param title               The assessment title
     * @param description         The assessment description
     * @param unitCode            The unit code
     * @param createdBy           The creator's user ID
     * @param startDateTime       The start date and time
     * @param endDateTime         The end date and time
     * @param durationMinutes     The duration in minutes
     * @param isPractice          Whether this is a practice test
     * @param allowOfflineAttempt Whether students can download and take offline
     * @return The newly created assessment, or null if creation fails
     */
    public static Assessment createAssessment(String title, String description, String unitCode,
                                              String createdBy, LocalDateTime startDateTime,
                                              LocalDateTime endDateTime, int durationMinutes,
                                              boolean isPractice, boolean allowOfflineAttempt) {
        LOGGER.log(Level.INFO, "Creating assessment: title={0}, unitCode={1}, createdBy={2}, isPractice={3}", 
                  new Object[]{title, unitCode, createdBy, isPractice});
        
        // Ensure title is uppercase with no spaces for aesthetics
        title = title.toUpperCase().replaceAll("\\s+", "");
        LOGGER.log(Level.INFO, "Formatted title: {0}", title);
        
        String assessmentId = UUID.randomUUID().toString();
        LOGGER.log(Level.INFO, "Generated assessment ID: {0}", assessmentId);
        
        String query = "INSERT INTO assessments (assessment_id, title, description, unit_code, created_by, "
                + "start_date_time, end_date_time, duration_minutes, is_practice, allow_offline_attempt, total_marks) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessmentId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, unitCode);
            stmt.setString(5, createdBy);
            stmt.setObject(6, startDateTime);
            stmt.setObject(7, endDateTime);
            stmt.setInt(8, durationMinutes);
            stmt.setBoolean(9, isPractice);
            stmt.setBoolean(10, allowOfflineAttempt);
            stmt.setInt(11, 0); // Initial total marks is 0, will be updated when questions are added

            if (stmt.executeUpdate() > 0) {
                LOGGER.log(Level.INFO, "Assessment record created successfully in database");
                Assessment assessment = new Assessment(assessmentId, title, unitCode, createdBy, durationMinutes, isPractice);
                assessment.setDescription(description);
                assessment.setStartDateTime(startDateTime);
                assessment.setEndDateTime(endDateTime);
                assessment.setAllowOfflineAttempt(allowOfflineAttempt);
                return assessment;
            } else {
                LOGGER.log(Level.WARNING, "No rows affected when creating assessment record");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating assessment: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "SQL State: {0}, Error Code: {1}", new Object[]{e.getSQLState(), e.getErrorCode()});
            LOGGER.log(Level.SEVERE, "Full exception: ", e);
        }

        LOGGER.log(Level.WARNING, "Failed to create assessment, returning null");
        return null;
    }

    /**
     * Adds a question to an assessment.
     *
     * @param assessmentId The assessment ID
     * @param questionId   The question ID
     * @param order        The question order in the assessment
     * @return True if the question was added successfully, false otherwise
     */
    public static boolean addQuestionToAssessment(String assessmentId, String questionId, int order) {
        LOGGER.log(Level.INFO, "Adding question {0} to assessment {1} at order {2}", 
                  new Object[]{questionId, assessmentId, order});
        
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            LOGGER.log(Level.INFO, "Database connection established, autocommit set to false");

            // Add the question to the assessment
            String addQuery = "INSERT INTO assessment_questions (assessment_id, question_id, question_order) "
                    + "VALUES (?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(addQuery)) {
                stmt.setString(1, assessmentId);
                stmt.setString(2, questionId);
                stmt.setInt(3, order);
                int rowsAffected = stmt.executeUpdate();
                LOGGER.log(Level.INFO, "Added question to assessment_questions table, rows affected: {0}", rowsAffected);
            }

            // Get the question marks
            int questionMarks = 0;
            String marksQuery = "SELECT marks FROM questions WHERE question_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(marksQuery)) {
                stmt.setString(1, questionId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    questionMarks = rs.getInt("marks");
                    LOGGER.log(Level.INFO, "Retrieved marks for question {0}: {1}", new Object[]{questionId, questionMarks});
                } else {
                    LOGGER.log(Level.WARNING, "No marks found for question {0}", questionId);
                }
            }

            // Update the assessment's total marks
            String updateQuery = "UPDATE assessments SET total_marks = total_marks + ? WHERE assessment_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, questionMarks);
                stmt.setString(2, assessmentId);
                int rowsAffected = stmt.executeUpdate();
                LOGGER.log(Level.INFO, "Updated total marks for assessment {0}, added {1} marks, rows affected: {2}", 
                          new Object[]{assessmentId, questionMarks, rowsAffected});
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Transaction committed successfully");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding question to assessment: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "SQL State: {0}, Error Code: {1}", new Object[]{e.getSQLState(), e.getErrorCode()});
            LOGGER.log(Level.SEVERE, "Full exception: ", e);
            
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.INFO, "Transaction rolled back due to error");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction: {0}", ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
        }

        return false;
    }

    /**
     * Removes a question from an assessment.
     *
     * @param assessmentId The assessment ID
     * @param questionId   The question ID
     * @return True if the question was removed successfully, false otherwise
     */
    public static boolean removeQuestionFromAssessment(String assessmentId, String questionId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Get the question marks
            int questionMarks = 0;
            String marksQuery = "SELECT marks FROM questions WHERE question_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(marksQuery)) {
                stmt.setString(1, questionId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    questionMarks = rs.getInt("marks");
                }
            }

            // Remove the question from the assessment
            String removeQuery = "DELETE FROM assessment_questions WHERE assessment_id = ? AND question_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(removeQuery)) {
                stmt.setString(1, assessmentId);
                stmt.setString(2, questionId);
                stmt.executeUpdate();
            }

            // Update the assessment's total marks
            String updateQuery = "UPDATE assessments SET total_marks = total_marks - ? WHERE assessment_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, questionMarks);
                stmt.setString(2, assessmentId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing question from assessment", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
        }

        return false;
    }

    /**
     * Gets an assessment by ID.
     *
     * @param assessmentId The assessment ID
     * @return The assessment, or null if not found
     */
    public static Assessment getAssessmentById(String assessmentId) {
        String query = "SELECT * FROM assessments WHERE assessment_id = ?";
        Assessment assessment = null;

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                assessment = new Assessment(
                        rs.getString("assessment_id"),
                        rs.getString("title"),
                        rs.getString("unit_code"),
                        rs.getString("created_by"),
                        rs.getInt("duration_minutes"),
                        rs.getBoolean("is_practice")
                );

                assessment.setDescription(rs.getString("description"));
                assessment.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                assessment.setStartDateTime(rs.getObject("start_date_time", LocalDateTime.class));
                assessment.setEndDateTime(rs.getObject("end_date_time", LocalDateTime.class));
                assessment.setActive(rs.getBoolean("is_active"));
                assessment.setTotalMarks(rs.getInt("total_marks"));
                assessment.setAllowOfflineAttempt(rs.getBoolean("allow_offline_attempt"));

                // Update assessment status based on current time
                updateAssessmentStatusBasedOnTime(assessment);
                
                // Load questions for this assessment
                loadAssessmentQuestions(assessment);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting assessment by ID", e);
        }

        return assessment;
    }

    /**
     * Loads the questions for an assessment.
     *
     * @param assessment The assessment object
     */
    private static void loadAssessmentQuestions(Assessment assessment) {
        String query = "SELECT q.question_id FROM questions q "
                + "JOIN assessment_questions aq ON q.question_id = aq.question_id "
                + "WHERE aq.assessment_id = ? ORDER BY aq.question_order";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessment.getAssessmentId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                assessment.addQuestionId(rs.getString("question_id"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading assessment questions", e);
        }
    }

    /**
     * Gets all assessments for a unit.
     *
     * @param unitCode The unit code
     * @return A list of assessments for the unit
     */
    public static List<Assessment> getAssessmentsByUnit(String unitCode) {
        List<Assessment> assessments = new ArrayList<>();
        String query = "SELECT * FROM assessments WHERE unit_code = ? ORDER BY creation_date DESC";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Assessment assessment = new Assessment(
                        rs.getString("assessment_id"),
                        rs.getString("title"),
                        rs.getString("unit_code"),
                        rs.getString("created_by"),
                        rs.getInt("duration_minutes"),
                        rs.getBoolean("is_practice")
                );

                assessment.setDescription(rs.getString("description"));
                assessment.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                assessment.setStartDateTime(rs.getObject("start_date_time", LocalDateTime.class));
                assessment.setEndDateTime(rs.getObject("end_date_time", LocalDateTime.class));
                assessment.setActive(rs.getBoolean("is_active"));
                assessment.setTotalMarks(rs.getInt("total_marks"));
                assessment.setAllowOfflineAttempt(rs.getBoolean("allow_offline_attempt"));

                // Update assessment status based on current time
                updateAssessmentStatusBasedOnTime(assessment);
                
                assessments.add(assessment);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting active assessments for student", e);
        }

        return assessments;
    }

    /**
     * Gets all practice tests for a student based on their enrolled units.
     *
     * @param studentId The student ID
     * @return A list of practice tests for the student
     */
    public static List<Assessment> getPracticeTestsForStudent(String studentId) {
        List<Assessment> assessments = new ArrayList<>();
        String query = "SELECT a.* FROM assessments a "
                + "JOIN enrollments e ON a.unit_code = e.unit_code "
                + "WHERE e.student_id = ? AND a.is_practice = 1 ORDER BY a.creation_date DESC";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Assessment assessment = new Assessment(
                        rs.getString("assessment_id"),
                        rs.getString("title"),
                        rs.getString("unit_code"),
                        rs.getString("created_by"),
                        rs.getInt("duration_minutes"),
                        rs.getBoolean("is_practice")
                );

                assessment.setDescription(rs.getString("description"));
                assessment.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));
                assessment.setStartDateTime(rs.getObject("start_date_time", LocalDateTime.class));
                assessment.setEndDateTime(rs.getObject("end_date_time", LocalDateTime.class));
                assessment.setActive(rs.getBoolean("is_active"));
                assessment.setTotalMarks(rs.getInt("total_marks"));
                assessment.setAllowOfflineAttempt(rs.getBoolean("allow_offline_attempt"));

                // Update assessment status based on current time
                updateAssessmentStatusBasedOnTime(assessment);
                
                assessments.add(assessment);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting practice tests for student", e);
        }

        return assessments;
    }

    /**
     * Updates an assessment's details.
     *
     * @param assessment The assessment with updated details
     * @return True if the update was successful, false otherwise
     */
    public static boolean updateAssessment(Assessment assessment) {
        String query = "UPDATE assessments SET title = ?, description = ?, start_date_time = ?, "
                + "end_date_time = ?, duration_minutes = ?, is_active = ?, is_practice = ?, "
                + "allow_offline_attempt = ? WHERE assessment_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessment.getTitle());
            stmt.setString(2, assessment.getDescription());
            stmt.setObject(3, assessment.getStartDateTime());
            stmt.setObject(4, assessment.getEndDateTime());
            stmt.setInt(5, assessment.getDurationMinutes());
            stmt.setBoolean(6, assessment.isActive());
            stmt.setBoolean(7, assessment.isPractice());
            stmt.setBoolean(8, assessment.isAllowOfflineAttempt());
            stmt.setString(9, assessment.getAssessmentId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating assessment", e);
        }

        return false;
    }

    /**
     * Deletes an assessment.
     *
     * @param assessmentId The assessment ID
     * @return True if the deletion was successful, false otherwise
     */
    public static boolean deleteAssessment(String assessmentId) {
        String query = "DELETE FROM assessments WHERE assessment_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessmentId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting assessment", e);
        }

        return false;
    }

    /**
     * Gets all questions for an assessment.
     *
     * @param assessmentId The assessment ID
     * @return A list of questions for the assessment
     */
    public static List<Question> getQuestionsForAssessment(String assessmentId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT q.* FROM questions q "
                + "JOIN assessment_questions aq ON q.question_id = aq.question_id "
                + "WHERE aq.assessment_id = ? ORDER BY aq.question_order";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, assessmentId);
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

                // Parse options and correct answers from JSON or delimited string
                String optionsStr = rs.getString("options");
                if (optionsStr != null && !optionsStr.isEmpty()) {
                    // Simple parsing assuming comma-separated values
                    String[] options = optionsStr.split(",");
                    for (String option : options) {
                        question.addOption(option.trim());
                    }
                }

                String correctAnswersStr = rs.getString("correct_answers");
                if (correctAnswersStr != null && !correctAnswersStr.isEmpty()) {
                    // Simple parsing assuming comma-separated values
                    String[] correctAnswers = correctAnswersStr.split(",");
                    for (String answer : correctAnswers) {
                        question.addCorrectAnswer(answer.trim());
                    }
                }

                question.setApproved(rs.getBoolean("approved"));
                question.setFeedback(rs.getString("feedback"));
                question.setCreationDate(rs.getObject("creation_date", LocalDateTime.class));

                questions.add(question);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting questions for assessment", e);
        }

        return questions;
    }

    /**
     * Generates random questions for a practice test based on specified criteria.
     *
     * @param practiceTest  The practice test to populate with questions
     * @param unitCode      The unit code to filter questions by
     * @param questionType  The type of questions to include
     * @param difficulty    The difficulty level of questions
     * @param questionCount The number of questions to generate
     * @param selectedTopics The list of topics to filter questions by (optional)
     * @return True if questions were successfully generated, false otherwise
     */
    public boolean generateQuestionsForPracticeTest(Assessment practiceTest, String unitCode, 
                                                  String questionType, String difficulty, int questionCount,
                                                  List<Topic> selectedTopics) {
        try {
            // Debug logging
            LOGGER.log(Level.INFO, "Generating questions with: unitCode={0}, questionType={1}, difficulty={2}, questionCount={3}", 
                    new Object[]{unitCode, questionType, difficulty, questionCount});
            
            if (selectedTopics != null && !selectedTopics.isEmpty()) {
                StringBuilder topicNames = new StringBuilder();
                for (Topic topic : selectedTopics) {
                    topicNames.append(topic.getTopicName()).append(", ");
                }
                LOGGER.log(Level.INFO, "Selected topics: {0}", topicNames.toString());
            } else {
                LOGGER.log(Level.INFO, "No topics selected");
            }
            
            // Base query to get random questions matching the criteria
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM questions WHERE unit_code = ? ");
            
            // Add question type filter if specified
            if (questionType != null && !questionType.isEmpty() && 
                !questionType.equalsIgnoreCase("Any") && !questionType.equalsIgnoreCase("Mixed")) {
                // Convert "Multiple Choice" to "MULTIPLE_CHOICE" format used in the database
                String dbQuestionType = questionType.toUpperCase().replace(" ", "_");
                queryBuilder.append("AND question_type = ? ");
            }
            
            // Add difficulty filter if specified
            if (difficulty != null && !difficulty.isEmpty() && 
                !difficulty.equalsIgnoreCase("Any") && !difficulty.equalsIgnoreCase("Mixed")) {
                queryBuilder.append("AND difficulty = ? ");
            }
            
            // Add topic filter if topics are selected
            if (selectedTopics != null && !selectedTopics.isEmpty()) {
                queryBuilder.append("AND topic IN (");
                for (int i = 0; i < selectedTopics.size(); i++) {
                    queryBuilder.append("?");
                    if (i < selectedTopics.size() - 1) {
                        queryBuilder.append(",");
                    }
                }
                queryBuilder.append(") ");
            }
            
            // Add random ordering and limit
            queryBuilder.append("ORDER BY RAND() LIMIT ?");
            
            // Log the final query
            LOGGER.log(Level.INFO, "Query: {0}", queryBuilder.toString());
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(queryBuilder.toString(), 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                int paramIndex = 1;
                stmt.setString(paramIndex++, unitCode);
                LOGGER.log(Level.INFO, "Parameter {0}: unitCode = {1}", new Object[]{paramIndex-1, unitCode});
                
                if (questionType != null && !questionType.isEmpty() && 
                    !questionType.equalsIgnoreCase("Any") && !questionType.equalsIgnoreCase("Mixed")) {
                    // Convert "Multiple Choice" to "MULTIPLE_CHOICE" format used in the database
                    String dbQuestionType = questionType.toUpperCase().replace(" ", "_");
                    stmt.setString(paramIndex++, dbQuestionType);
                    LOGGER.log(Level.INFO, "Parameter {0}: questionType = {1}", new Object[]{paramIndex-1, dbQuestionType});
                }
                
                if (difficulty != null && !difficulty.isEmpty() && 
                    !difficulty.equalsIgnoreCase("Any") && !difficulty.equalsIgnoreCase("Mixed")) {
                    stmt.setString(paramIndex++, difficulty.toUpperCase());
                    LOGGER.log(Level.INFO, "Parameter {0}: difficulty = {1}", new Object[]{paramIndex-1, difficulty.toUpperCase()});
                }
                
                // Set topic parameters if topics are selected
                if (selectedTopics != null && !selectedTopics.isEmpty()) {
                    for (Topic topic : selectedTopics) {
                        stmt.setString(paramIndex++, topic.getTopicName());
                        LOGGER.log(Level.INFO, "Parameter {0}: topic = {1}", new Object[]{paramIndex-1, topic.getTopicName()});
                    }
                }
                
                stmt.setInt(paramIndex, questionCount);
                LOGGER.log(Level.INFO, "Parameter {0}: questionCount = {1}", new Object[]{paramIndex, questionCount});
                
                ResultSet rs = stmt.executeQuery();
                
                // Debug: Check if we got any results
                boolean hasResults = rs.next();
                if (!hasResults) {
                    LOGGER.log(Level.WARNING, "No questions found matching the criteria");
                    return false;
                }
                
                // Reset the result set cursor
                rs.beforeFirst();
                
                int addedQuestions = 0;
                while (rs.next()) {
                    String questionId = rs.getString("question_id");
                    practiceTest.addQuestion(questionId);
                    addedQuestions++;
                    LOGGER.log(Level.INFO, "Added question with ID: {0}", questionId);
                }
                
                // Check if we got enough questions
                if (addedQuestions < questionCount) {
                    LOGGER.log(Level.WARNING, "Could only find {0} questions out of {1} requested", 
                            new Object[]{addedQuestions, questionCount});
                    
                    // If we have some questions but not enough, we'll still return true
                    // but adjust the question count in the practice test
                    if (addedQuestions > 0) {
                        practiceTest.setQuestionCount(addedQuestions);
                        return true;
                    }
                    
                    return false;
                }
                
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating questions for practice test", e);
            return false;
        }
    }
    
    /**
     * Overloaded method for backward compatibility.
     * Generates random questions for a practice test based on specified criteria without topic filtering.
     *
     * @param practiceTest  The practice test to populate with questions
     * @param unitCode      The unit code to filter questions by
     * @param questionType  The type of questions to include
     * @param difficulty    The difficulty level of questions
     * @param questionCount The number of questions to generate
     * @return True if questions were successfully generated, false otherwise
     */
    public boolean generateQuestionsForPracticeTest(Assessment practiceTest, String unitCode, 
                                                  String questionType, String difficulty, int questionCount) {
        // Call the new method with null for selectedTopics
        return generateQuestionsForPracticeTest(practiceTest, unitCode, questionType, difficulty, questionCount, null);
    }
    
    /**
     * Saves a practice test to the database.
     *
     * @param practiceTest The practice test to save
     * @param username     The username of the student creating the test
     * @return True if the practice test was saved successfully, false otherwise
     */
    public boolean savePracticeTest(Assessment practiceTest, String username) {
        try {
            // Generate a unique ID for the assessment
            String assessmentId = UUID.randomUUID().toString();
            practiceTest.setAssessmentId(assessmentId);
            
            // Calculate total marks based on the questions
            int totalMarks = 0;
            for (String questionId : practiceTest.getQuestionIds()) {
                Question question = new QuestionService().getQuestionById(questionId);
                if (question != null) {
                    totalMarks += question.getMarks();
                }
            }
            practiceTest.setTotalMarks(totalMarks);
            
            // Update title to include difficulty and question type if not already included
            String title = practiceTest.getTitle();
            String difficulty = practiceTest.getDifficulty();
            String questionType = practiceTest.getQuestionType();
            
            if (difficulty != null && !difficulty.isEmpty() && !title.toLowerCase().contains(difficulty.toLowerCase())) {
                title += " (" + difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1).toLowerCase() + ")";
            }
            
            if (questionType != null && !questionType.isEmpty() && !title.toLowerCase().contains(questionType.toLowerCase().replace("_", " "))) {
                String formattedType = questionType.replace("_", " ");
                title += " - " + formattedType.substring(0, 1).toUpperCase() + formattedType.substring(1).toLowerCase();
            }
            
            practiceTest.setTitle(title);
            
            LOGGER.log(Level.INFO, "Saving practice test with ID: {0}, Total Marks: {1}, Title: {2}", 
                    new Object[]{assessmentId, totalMarks, title});
            
            // Insert into the assessments table
            String assessmentQuery = "INSERT INTO assessments (assessment_id, title, unit_code, created_by, "
                    + "start_date_time, end_date_time, duration_minutes, is_practice, is_active, total_marks) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(assessmentQuery)) {
                stmt.setString(1, assessmentId);
                stmt.setString(2, title);
                stmt.setString(3, practiceTest.getUnitCode());
                stmt.setString(4, username);
                stmt.setObject(5, practiceTest.getStartDateTime());
                stmt.setObject(6, practiceTest.getEndDateTime()); // May be null for practice tests
                stmt.setInt(7, practiceTest.getDurationMinutes());
                stmt.setBoolean(8, true); // is_practice
                stmt.setBoolean(9, true); // is_active
                stmt.setInt(10, totalMarks); // total_marks
                
                LOGGER.log(Level.INFO, "Executing SQL: {0}", assessmentQuery);
                
                if (stmt.executeUpdate() <= 0) {
                    LOGGER.log(Level.WARNING, "Failed to insert into assessments table");
                    return false;
                }
            }
            
            // Add questions to the assessment
            for (int i = 0; i < practiceTest.getQuestionIds().size(); i++) {
                String questionId = practiceTest.getQuestionIds().get(i);
                if (!addQuestionToAssessment(assessmentId, questionId, i + 1)) {
                    LOGGER.log(Level.WARNING, "Failed to add question {0} to practice test", questionId);
                }
            }
            
            // Create an initial entry in practice_assessments table to link the practice test to the student
            // This ensures the practice test appears in the student's list immediately after creation
            String practiceId = UUID.randomUUID().toString();
            String practiceQuery = "INSERT INTO practice_assessments (practice_id, assessment_id, student_id, title, " +
                    "unit_code, score, total_possible, percentage, grade, completion_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(practiceQuery)) {
                stmt.setString(1, practiceId);
                stmt.setString(2, assessmentId);
                stmt.setString(3, username);
                stmt.setString(4, title);
                stmt.setString(5, practiceTest.getUnitCode());
                stmt.setDouble(6, 0); // Initial score is 0
                stmt.setInt(7, totalMarks);
                stmt.setDouble(8, 0); // Initial percentage is 0
                stmt.setString(9, "N/A"); // Initial grade is N/A
                stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now())); // Current time as completion date
                stmt.setString(11, "CREATED"); // Status is CREATED
                
                LOGGER.log(Level.INFO, "Creating initial practice_assessments entry for practice test {0} and student {1}", 
                        new Object[]{assessmentId, username});
                
                if (stmt.executeUpdate() <= 0) {
                    LOGGER.log(Level.WARNING, "Failed to create initial entry in practice_assessments table");
                    // Continue anyway since the assessment was created successfully
                } else {
                    LOGGER.log(Level.INFO, "Successfully created initial entry in practice_assessments table");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error creating initial entry in practice_assessments table", e);
                // Continue anyway since the assessment was created successfully
            }
            
            LOGGER.log(Level.INFO, "Successfully saved practice test with ID: {0}", assessmentId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving practice test", e);
            return false;
        }
    }
    
    /**
     * Loads all practice tests for a specific student.
     *
     * @param username The username of the student
     * @return A list of practice tests
     */
    public List<Assessment> loadPracticeTests(String username) {
        List<Assessment> practiceTests = new ArrayList<>();
        
        try {
            // Get practice tests from the assessments table
            String query = "SELECT * FROM assessments WHERE is_practice = true AND created_by = ? ORDER BY start_date_time DESC";
            LOGGER.log(Level.INFO, "Executing query to load practice tests: {0}", query);
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Assessment assessment = new Assessment();
                    assessment.setAssessmentId(rs.getString("assessment_id"));
                    assessment.setTitle(rs.getString("title"));
                    assessment.setUnitCode(rs.getString("unit_code"));
                    assessment.setCreatedBy(rs.getString("created_by"));
                    
                    // Handle timestamp conversion properly
                    Timestamp startTimestamp = rs.getTimestamp("start_date_time");
                    if (startTimestamp != null) {
                        assessment.setStartDateTime(startTimestamp.toLocalDateTime());
                    }
                    
                    Timestamp endTimestamp = rs.getTimestamp("end_date_time");
                    if (endTimestamp != null) {
                        assessment.setEndDateTime(endTimestamp.toLocalDateTime());
                    }
                    
                    assessment.setDurationMinutes(rs.getInt("duration_minutes"));
                    assessment.setPractice(rs.getBoolean("is_practice"));
                    assessment.setActive(rs.getBoolean("is_active"));
                    assessment.setTotalMarks(rs.getInt("total_marks"));
                    
                    // Default values for practice tests
                    assessment.setStatus("NOT_STARTED");
                    
                    // Set difficulty and question type based on title or other logic if needed
                    String title = assessment.getTitle().toLowerCase();
                    if (title.contains("easy")) {
                        assessment.setDifficulty("EASY");
                    } else if (title.contains("medium")) {
                        assessment.setDifficulty("MEDIUM");
                    } else if (title.contains("hard")) {
                        assessment.setDifficulty("HARD");
                    } else {
                        assessment.setDifficulty("MEDIUM"); // Default
                    }
                    
                    if (title.contains("multiple choice")) {
                        assessment.setQuestionType("MULTIPLE_CHOICE");
                    } else if (title.contains("true/false") || title.contains("true false")) {
                        assessment.setQuestionType("TRUE_FALSE");
                    } else {
                        assessment.setQuestionType("MIXED"); // Default
                    }
                    
                    // Load questions for this practice test
                    loadQuestionsForAssessment(assessment);
                    
                    practiceTests.add(assessment);
                }
                
                LOGGER.log(Level.INFO, "Loaded {0} practice tests for student {1}", 
                        new Object[]{practiceTests.size(), username});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading practice tests: {0}", e.getMessage());
            e.printStackTrace();
        }
        
        return practiceTests;
    }

    /**
     * Loads questions for an assessment and adds them to the assessment object.
     *
     * @param assessment The assessment to load questions for
     */
    private void loadQuestionsForAssessment(Assessment assessment) {
        if (assessment == null || assessment.getAssessmentId() == null) {
            LOGGER.log(Level.WARNING, "Cannot load questions for null assessment or assessment with null ID");
            return;
        }
        
        // Get questions for this assessment
        List<Question> questions = getQuestionsForAssessment(assessment.getAssessmentId());
        
        // Clear existing question IDs
        assessment.getQuestionIds().clear();
        
        // Add question IDs to the assessment
        for (Question question : questions) {
            assessment.addQuestion(question.getQuestionId());
        }
        
        LOGGER.log(Level.INFO, "Loaded {0} questions for assessment {1}", 
                new Object[]{questions.size(), assessment.getAssessmentId()});
    }

    /**
     * Updates an assessment's status based on the current time.
     * If the current time is after the start time and before the end time, the assessment is set to active.
     * This method also updates the database to reflect the new status.
     *
     * @param assessment The assessment to update
     */
    public static void updateAssessmentStatusBasedOnTime(Assessment assessment) {
        LocalDateTime now = LocalDateTime.now();
        boolean shouldBeActive = false;
        
        // Check if the assessment should be active based on time
        if (assessment.getStartDateTime() != null && assessment.getEndDateTime() != null) {
            shouldBeActive = now.isAfter(assessment.getStartDateTime()) && now.isBefore(assessment.getEndDateTime());
            
            // Only update if the status needs to change
            if (shouldBeActive != assessment.isActive()) {
                LOGGER.log(Level.INFO, "Updating assessment status: {0}, ID: {1}, Current status: {2}, New status: {3}", 
                          new Object[]{assessment.getTitle(), assessment.getAssessmentId(), 
                                      assessment.isActive() ? "active" : "inactive", shouldBeActive ? "active" : "inactive"});
                
                // Update the assessment object
                assessment.setActive(shouldBeActive);
                
                // Update the database
                String query = "UPDATE assessments SET is_active = ? WHERE assessment_id = ?";
                try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                    stmt.setBoolean(1, shouldBeActive);
                    stmt.setString(2, assessment.getAssessmentId());
                    int rowsUpdated = stmt.executeUpdate();
                    
                    if (rowsUpdated > 0) {
                        LOGGER.log(Level.INFO, "Successfully updated assessment status in database");
                    } else {
                        LOGGER.log(Level.WARNING, "Failed to update assessment status in database");
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error updating assessment status", e);
                }
            }
        }
    }

    /**
     * Initializes a scheduler to periodically check and update assessment statuses.
     * This ensures that assessments are properly activated when their start time is reached,
     * even if no one is actively retrieving them.
     */
    public static void initializeAssessmentStatusScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        // Run the task every minute
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LOGGER.info("Running scheduled assessment status update check");
                updateAllAssessmentStatuses();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in scheduled assessment status update", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
        
        LOGGER.info("Assessment status scheduler initialized");
    }
    
    /**
     * Updates the status of all assessments in the database based on their start and end times.
     */
    private static void updateAllAssessmentStatuses() {
        String query = "SELECT assessment_id FROM assessments";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            int updatedCount = 0;
            while (rs.next()) {
                String assessmentId = rs.getString("assessment_id");
                Assessment assessment = getAssessmentById(assessmentId);
                
                if (assessment != null) {
                    boolean wasActive = assessment.isActive();
                    updateAssessmentStatusBasedOnTime(assessment);
                    
                    // Log if status changed
                    if (wasActive != assessment.isActive()) {
                        updatedCount++;
                    }
                }
            }
            
            if (updatedCount > 0) {
                LOGGER.info("Updated status for " + updatedCount + " assessments");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating all assessment statuses", e);
        }
    }

    /**
     * Debug method to check questions in the database.
     * This helps identify what questions are available and their attributes.
     * 
     * @param unitCode The unit code to check questions for
     */
    public static void debugCheckQuestions(String unitCode) {
        try {
            // Check what difficulty values are stored
            String difficultyQuery = "SELECT DISTINCT difficulty FROM questions WHERE unit_code = ?";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(difficultyQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                LOGGER.log(Level.INFO, "Available difficulty values for unit {0}:", unitCode);
                while (rs.next()) {
                    String difficulty = rs.getString("difficulty");
                    LOGGER.log(Level.INFO, "- {0}", difficulty);
                }
            }
            
            // Count questions by difficulty
            String countByDifficultyQuery = "SELECT difficulty, COUNT(*) as count FROM questions WHERE unit_code = ? GROUP BY difficulty";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(countByDifficultyQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                LOGGER.log(Level.INFO, "Question counts by difficulty for unit {0}:", unitCode);
                while (rs.next()) {
                    String difficulty = rs.getString("difficulty");
                    int count = rs.getInt("count");
                    LOGGER.log(Level.INFO, "- {0}: {1} questions", new Object[]{difficulty, count});
                }
            }
            
            // Check what question types are available
            String typeQuery = "SELECT DISTINCT question_type FROM questions WHERE unit_code = ?";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(typeQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                LOGGER.log(Level.INFO, "Available question types for unit {0}:", unitCode);
                while (rs.next()) {
                    String type = rs.getString("question_type");
                    LOGGER.log(Level.INFO, "- {0}", type);
                }
            }
            
            // Sample of actual questions
            String sampleQuery = "SELECT question_id, question_text, question_type, difficulty, topic FROM questions WHERE unit_code = ? LIMIT 5";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(sampleQuery)) {
                stmt.setString(1, unitCode);
                ResultSet rs = stmt.executeQuery();
                
                LOGGER.log(Level.INFO, "Sample questions for unit {0}:", unitCode);
                while (rs.next()) {
                    String id = rs.getString("question_id");
                    String text = rs.getString("question_text");
                    String type = rs.getString("question_type");
                    String difficulty = rs.getString("difficulty");
                    String topic = rs.getString("topic");
                    
                    LOGGER.log(Level.INFO, "Question ID: {0}", id);
                    LOGGER.log(Level.INFO, "- Text: {0}", text);
                    LOGGER.log(Level.INFO, "- Type: {0}", type);
                    LOGGER.log(Level.INFO, "- Difficulty: {0}", difficulty);
                    LOGGER.log(Level.INFO, "- Topic: {0}", topic);
                    LOGGER.log(Level.INFO, "---");
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error debugging questions", e);
        }
    }
}
