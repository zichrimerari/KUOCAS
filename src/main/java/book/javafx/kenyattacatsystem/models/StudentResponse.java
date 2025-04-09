package book.javafx.kenyattacatsystem.models;

/**
 * Represents a student's response to a question in an assessment.
 * Stores the response text, correctness, and marks awarded.
 */
public class StudentResponse {
    private String responseId;
    private String attemptId;
    private String questionId;
    private String responseText;
    private boolean isCorrect;
    private int marksAwarded;
    private String feedback;
    
    /**
     * Default constructor
     */
    public StudentResponse() {
        this.isCorrect = false;
        this.marksAwarded = 0;
    }
    
    /**
     * Constructor with essential fields
     *
     * @param responseId    The response ID
     * @param attemptId     The attempt ID
     * @param questionId    The question ID
     * @param responseText  The student's response text
     */
    public StudentResponse(String responseId, String attemptId, String questionId, String responseText) {
        this.responseId = responseId;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.responseText = responseText;
        this.isCorrect = false;
        this.marksAwarded = 0;
    }
    
    /**
     * Constructor with all fields
     *
     * @param responseId    The response ID
     * @param attemptId     The attempt ID
     * @param questionId    The question ID
     * @param responseText  The student's response text
     * @param isCorrect     Whether the response is correct
     * @param marksAwarded  The marks awarded for this response
     * @param feedback      Feedback on the response
     */
    public StudentResponse(String responseId, String attemptId, String questionId, String responseText,
                           boolean isCorrect, int marksAwarded, String feedback) {
        this.responseId = responseId;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.responseText = responseText;
        this.isCorrect = isCorrect;
        this.marksAwarded = marksAwarded;
        this.feedback = feedback;
    }
    
    /**
     * Evaluates the response against the correct answer
     *
     * @param correctAnswer The correct answer to compare against
     * @param questionType  The type of question (affects how comparison is done)
     * @param marks         The maximum marks for this question
     * @return True if the response is correct, false otherwise
     */
    public boolean evaluateResponse(String correctAnswer, String questionType, int marks) {
        if (responseText == null || correctAnswer == null) {
            this.isCorrect = false;
            this.marksAwarded = 0;
            return false;
        }

        System.out.println("Evaluating response for question type: " + questionType);
        System.out.println("Response text: '" + responseText + "'");
        System.out.println("Correct answer: '" + correctAnswer + "'");
        
        if (questionType.equals(Question.TYPE_MULTIPLE_CHOICE) || questionType.equals(Question.TYPE_SHORT_ANSWER)) {
            // Normalize both strings for comparison
            String normalizedResponse = normalizeText(responseText);
            String normalizedAnswer = normalizeText(correctAnswer);
            
            System.out.println("Normalized response: '" + normalizedResponse + "'");
            System.out.println("Normalized answer: '" + normalizedAnswer + "'");
            
            // Compare normalized strings
            this.isCorrect = normalizedResponse.equals(normalizedAnswer);
            this.marksAwarded = this.isCorrect ? marks : 0;
            
            // Calculate Levenshtein distance for near-matches
            int distance = calculateLevenshteinDistance(normalizedResponse, normalizedAnswer);
            System.out.println("Levenshtein distance: " + distance);
            
            System.out.println("Evaluation result: " + (this.isCorrect ? "CORRECT" : "INCORRECT") + 
                               ", Marks awarded: " + this.marksAwarded);
        } else if (questionType.equals(Question.TYPE_LIST_BASED)) {
            String[] correctItems = correctAnswer.split(",");
            String[] responseItems = responseText.split(",");
            
            int correctCount = 0;
            for (String responseItem : responseItems) {
                for (String correctItem : correctItems) {
                    if (responseItem.trim().equalsIgnoreCase(correctItem.trim())) {
                        correctCount++;
                        break;
                    }
                }
            }
            
            double percentageCorrect = (double) correctCount / correctItems.length;
            this.marksAwarded = (int) Math.round(percentageCorrect * marks);
            this.isCorrect = (this.marksAwarded == marks);
            
            System.out.println("List-based evaluation result: " + (this.isCorrect ? "CORRECT" : "INCORRECT") + 
                               ", Marks awarded: " + this.marksAwarded + " (" + (percentageCorrect * 100) + "%)");
        }
        
        return this.isCorrect;
    }
    
    // Getters and Setters
    public String getResponseId() {
        return responseId;
    }
    
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
    
    public String getAttemptId() {
        return attemptId;
    }
    
    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getResponseText() {
        return responseText;
    }
    
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    public int getMarksAwarded() {
        return marksAwarded;
    }
    
    public void setMarksAwarded(int marksAwarded) {
        this.marksAwarded = marksAwarded;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    /**
     * Normalizes text for comparison by:
     * - Converting to lowercase
     * - Removing extra whitespace
     * - Removing punctuation
     * - Standardizing common variations
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        
        // Convert to lowercase and trim
        String normalized = text.toLowerCase().trim();
        
        // Remove extra whitespace
        normalized = normalized.replaceAll("\\s+", " ");
        
        // Remove punctuation except decimal points in numbers
        normalized = normalized.replaceAll("(?<!\\d)[.,;:!?]", "");
        
        // Standardize common variations
        normalized = normalized.replace("'", "'")
                             .replace("‘", "'")
                             .replace("’", "'")
                             .replace("“", "\"")
                             .replace("”", "\"");
        
        return normalized;
    }
    
    /**
     * Calculates the Levenshtein distance between two strings.
     * This measures how many single-character edits are needed to change one string into another.
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}