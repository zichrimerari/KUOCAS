package book.javafx.kenyattacatsystem.models;

import java.time.LocalDateTime;

/**
 * Represents a topic within a unit for categorizing questions.
 */
public class Topic {
    private String topicId;
    private String topicName;
    private String description;
    private String unitCode;
    private String unitName; // For display purposes
    private String createdBy;
    private LocalDateTime creationDate;
    private boolean selected; // For UI selection
    
    /**
     * Default constructor
     */
    public Topic() {
    }
    
    /**
     * Constructor with essential fields
     *
     * @param topicId     The topic ID
     * @param topicName   The topic name
     * @param unitCode    The unit code this topic belongs to
     * @param createdBy   The user ID who created this topic
     */
    public Topic(String topicId, String topicName, String unitCode, String createdBy) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.unitCode = unitCode;
        this.createdBy = createdBy;
        this.creationDate = LocalDateTime.now();
    }
    
    /**
     * Constructor with all fields
     *
     * @param topicId      The topic ID
     * @param topicName    The topic name
     * @param description  The topic description
     * @param unitCode     The unit code this topic belongs to
     * @param unitName     The unit name for display purposes
     * @param createdBy    The user ID who created this topic
     * @param creationDate The creation date
     */
    public Topic(String topicId, String topicName, String description, String unitCode, 
                String unitName, String createdBy, LocalDateTime creationDate) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.description = description;
        this.unitCode = unitCode;
        this.unitName = unitName;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
    }
    
    // Getters and Setters
    public String getTopicId() {
        return topicId;
    }
    
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
    
    public String getTopicName() {
        return topicName;
    }
    
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUnitCode() {
        return unitCode;
    }
    
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
    
    public String getUnitName() {
        return unitName;
    }
    
    public void setUnitName(String unitName) {
        this.unitName = unitName;
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
    
    /**
     * Gets whether this topic is selected in the UI.
     * 
     * @return True if selected, false otherwise
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Sets whether this topic is selected in the UI.
     * 
     * @param selected True to select, false to deselect
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public String toString() {
        return "Topic{" +
                "topicId='" + topicId + '\'' +
                ", topicName='" + topicName + '\'' +
                ", description='" + description + '\'' +
                ", unitCode='" + unitCode + '\'' +
                ", unitName='" + unitName + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", creationDate=" + creationDate +
                ", selected=" + selected +
                '}';
    }
}
