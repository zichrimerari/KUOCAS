package book.javafx.kenyattacatsystem.models;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a generated report file.
 */
public class ReportFile {
    private String reportName;
    private String filePath;
    private LocalDateTime generatedDate;
    private String fileType;
    
    /**
     * Creates a new ReportFile instance.
     * 
     * @param reportName The name of the report
     * @param filePath The path to the report file
     * @param generatedDate The date and time when the report was generated
     * @param fileType The file type (e.g., HTML, PDF, CSV)
     */
    public ReportFile(String reportName, String filePath, LocalDateTime generatedDate, String fileType) {
        this.reportName = reportName;
        this.filePath = filePath;
        this.generatedDate = generatedDate;
        this.fileType = fileType;
    }
    
    /**
     * Creates a ReportFile instance from a file path.
     * 
     * @param reportName The name of the report
     * @param filePath The path to the report file
     * @return A new ReportFile instance
     */
    public static ReportFile fromFilePath(String reportName, String filePath) {
        File file = new File(filePath);
        String fileType = getFileExtension(file);
        LocalDateTime generatedDate = LocalDateTime.now();
        
        return new ReportFile(reportName, filePath, generatedDate, fileType);
    }
    
    /**
     * Gets the file extension from a file.
     * 
     * @param file The file
     * @return The file extension
     */
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Empty extension
        }
        return name.substring(lastIndexOf + 1).toUpperCase();
    }
    
    /**
     * Gets the name of the report.
     * 
     * @return The report name
     */
    public String getReportName() {
        return reportName;
    }
    
    /**
     * Gets the path to the report file.
     * 
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the date and time when the report was generated.
     * 
     * @return The generated date
     */
    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }
    
    /**
     * Gets the formatted date and time when the report was generated.
     * 
     * @return The formatted generated date
     */
    public String getFormattedDate() {
        return generatedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Gets the file type.
     * 
     * @return The file type
     */
    public String getFileType() {
        return fileType;
    }
    
    /**
     * Gets the file name without the path.
     * 
     * @return The file name
     */
    public String getFileName() {
        File file = new File(filePath);
        return file.getName();
    }
}
