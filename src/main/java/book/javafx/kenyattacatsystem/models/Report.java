package book.javafx.kenyattacatsystem.models;

import java.time.LocalDateTime;

/**
 * Model class representing a report in the system.
 * Reports can be generated with various filters and parameters.
 */
public class Report {
    private String reportId;
    private String reportName;
    private String reportType;
    private String description;
    private String queryTemplate;
    private String parameters;
    private String createdBy;
    private LocalDateTime creationDate;
    private LocalDateTime lastModified;
    private boolean isSystem;

    /**
     * Default constructor
     */
    public Report() {
    }

    /**
     * Constructor with all fields
     */
    public Report(String reportId, String reportName, String reportType, String description,
                 String queryTemplate, String parameters, String createdBy,
                 LocalDateTime creationDate, LocalDateTime lastModified, boolean isSystem) {
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportType = reportType;
        this.description = description;
        this.queryTemplate = queryTemplate;
        this.parameters = parameters;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.lastModified = lastModified;
        this.isSystem = isSystem;
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
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

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    @Override
    public String toString() {
        return reportName;
    }
}
