package book.javafx.kenyattacatsystem.models;

import java.time.LocalDateTime;

/**
 * Model class representing a report execution in the system.
 * Tracks when reports are generated and by whom.
 */
public class ReportExecution {
    private String executionId;
    private String reportId;
    private String executedBy;
    private LocalDateTime executionDate;
    private String parameters;
    private String resultFilePath;
    private String reportName; // Added field for UI display
    
    // Reference to the report (not stored in database)
    private Report report;

    /**
     * Default constructor
     */
    public ReportExecution() {
    }

    /**
     * Constructor with all fields
     */
    public ReportExecution(String executionId, String reportId, String executedBy,
                          LocalDateTime executionDate, String parameters, String resultFilePath) {
        this.executionId = executionId;
        this.reportId = reportId;
        this.executedBy = executedBy;
        this.executionDate = executionDate;
        this.parameters = parameters;
        this.resultFilePath = resultFilePath;
    }

    /**
     * Constructor with all fields including reportName
     */
    public ReportExecution(String executionId, String reportId, String executedBy,
                          LocalDateTime executionDate, String parameters, String resultFilePath, String reportName) {
        this.executionId = executionId;
        this.reportId = reportId;
        this.executedBy = executedBy;
        this.executionDate = executionDate;
        this.parameters = parameters;
        this.resultFilePath = resultFilePath;
        this.reportName = reportName;
    }

    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getResultFilePath() {
        return resultFilePath;
    }

    public void setResultFilePath(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public String toString() {
        return "Report execution on " + executionDate;
    }
}
