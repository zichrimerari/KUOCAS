package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Report;
import book.javafx.kenyattacatsystem.models.ReportExecution;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for generating performance analytics reports.
 * Extends the functionality of ReportService with analytics-specific reports.
 */
public class PerformanceReportService {
    private static final Logger LOGGER = Logger.getLogger(PerformanceReportService.class.getName());
    
    /**
     * Generates a student performance report for a specific student.
     *
     * @param studentId The ID of the student
     * @param format The format of the report (PDF, CSV, EXCEL, HTML)
     * @return The path to the generated report file
     */
    public static String generateStudentPerformanceReport(String studentId, String format) {
        try {
            PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
            Map<String, Object> performanceData = analyticsService.getStudentPerformance(studentId);
            if (performanceData == null) {
                LOGGER.log(Level.WARNING, "No performance data found for student: " + studentId);
                return null;
            }
            
            // Get student details
            String studentName = UserService.getUserFullName(studentId);
            if (studentName == null) {
                studentName = "Unknown Student";
            }
            
            // Create reports directory if it doesn't exist
            String userDir = System.getProperty("user.dir");
            File reportsDir = new File(userDir, "reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }
            
            // Generate file name with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "Student_Performance_" + studentName.replaceAll("[^a-zA-Z0-9_\\-]", "_") + "_" + timestamp;
            
            // Generate report based on format
            String filePath;
            switch (format.toUpperCase()) {
                case "PDF":
                    filePath = generatePdfReport(performanceData, fileName, studentName, reportsDir);
                    break;
                case "EXCEL":
                    filePath = generateExcelReport(performanceData, fileName, studentName, reportsDir);
                    break;
                case "HTML":
                    filePath = generateHtmlReport(performanceData, fileName, studentName, reportsDir);
                    break;
                case "CSV":
                default:
                    filePath = generateCsvReport(performanceData, fileName, studentName, reportsDir);
                    break;
            }
            
            // Record the report execution
            recordReportExecution("STUDENT_PERFORMANCE", studentId, format, filePath);
            
            return filePath;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating student performance report", e);
            return null;
        }
    }
    
    /**
     * Generates a unit performance report.
     *
     * @param unitCode The code of the unit
     * @param format The format of the report (PDF, CSV, EXCEL, HTML)
     * @return The path to the generated report file
     */
    public static String generateUnitPerformanceReport(String unitCode, String format) {
        try {
            PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
            Map<String, Object> performanceData = analyticsService.getUnitPerformance(unitCode);
            if (performanceData == null) {
                LOGGER.log(Level.WARNING, "No performance data found for unit: " + unitCode);
                return null;
            }
            
            // Get unit details
            String unitName = UnitService.getUnitName(unitCode);
            if (unitName == null) {
                unitName = unitCode;
            }
            
            // Create reports directory if it doesn't exist
            String userDir = System.getProperty("user.dir");
            File reportsDir = new File(userDir, "reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }
            
            // Generate file name with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "Unit_Performance_" + unitCode + "_" + timestamp;
            
            // Generate report based on format
            String filePath;
            switch (format.toUpperCase()) {
                case "PDF":
                    filePath = generatePdfReport(performanceData, fileName, unitName, reportsDir);
                    break;
                case "EXCEL":
                    filePath = generateExcelReport(performanceData, fileName, unitName, reportsDir);
                    break;
                case "HTML":
                    filePath = generateHtmlReport(performanceData, fileName, unitName, reportsDir);
                    break;
                case "CSV":
                default:
                    filePath = generateCsvReport(performanceData, fileName, unitName, reportsDir);
                    break;
            }
            
            // Record the report execution
            recordReportExecution("UNIT_PERFORMANCE", unitCode, format, filePath);
            
            return filePath;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating unit performance report", e);
            return null;
        }
    }
    
    /**
     * Generates a department performance report.
     *
     * @param department The name of the department
     * @param format The format of the report (PDF, CSV, EXCEL, HTML)
     * @return The path to the generated report file
     */
    public static String generateDepartmentPerformanceReport(String department, String format) {
        try {
            PerformanceAnalyticsService analyticsService = new PerformanceAnalyticsService();
            Map<String, Object> performanceData = analyticsService.getDepartmentPerformance(department);
            if (performanceData == null) {
                LOGGER.log(Level.WARNING, "No performance data found for department: " + department);
                return null;
            }
            
            // Create reports directory if it doesn't exist
            String userDir = System.getProperty("user.dir");
            File reportsDir = new File(userDir, "reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }
            
            // Generate file name with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "Department_Performance_" + department.replaceAll("[^a-zA-Z0-9_\\-]", "_") + "_" + timestamp;
            
            // Generate report based on format
            String filePath;
            switch (format.toUpperCase()) {
                case "PDF":
                    filePath = generatePdfReport(performanceData, fileName, department, reportsDir);
                    break;
                case "EXCEL":
                    filePath = generateExcelReport(performanceData, fileName, department, reportsDir);
                    break;
                case "HTML":
                    filePath = generateHtmlReport(performanceData, fileName, department, reportsDir);
                    break;
                case "CSV":
                default:
                    filePath = generateCsvReport(performanceData, fileName, department, reportsDir);
                    break;
            }
            
            // Record the report execution
            recordReportExecution("DEPARTMENT_PERFORMANCE", department, format, filePath);
            
            return filePath;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating department performance report", e);
            return null;
        }
    }
    
    /**
     * Generates a CSV report from performance data.
     *
     * @param data The performance data
     * @param fileName The base file name
     * @param title The report title
     * @param reportsDir The directory to save the report
     * @return The path to the generated file
     */
    private static String generateCsvReport(Map<String, Object> data, String fileName, String title, File reportsDir) {
        File reportFile = new File(reportsDir, fileName + ".csv");
        String filePath = reportFile.getAbsolutePath();
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Performance Report: ").append(title).append("\n");
            writer.append("Generated: ").append(LocalDateTime.now().toString()).append("\n\n");
            
            // Write summary statistics
            writer.append("Summary Statistics\n");
            if (data.containsKey("overallScore")) {
                writer.append("Overall Score,").append(data.get("overallScore").toString()).append("%\n");
            }
            if (data.containsKey("totalAssessments")) {
                writer.append("Total Assessments,").append(data.get("totalAssessments").toString()).append("\n");
            }
            writer.append("\n");
            
            // Write assessment details if available
            if (data.containsKey("assessments")) {
                List<Map<String, Object>> assessments = (List<Map<String, Object>>) data.get("assessments");
                if (!assessments.isEmpty()) {
                    writer.append("Assessment Details\n");
                    
                    // Write header
                    writer.append("Title,Type,Date,Score,Percentage\n");
                    
                    // Write data
                    for (Map<String, Object> assessment : assessments) {
                        writer.append(assessment.getOrDefault("title", "").toString()).append(",");
                        writer.append(assessment.getOrDefault("type", "").toString()).append(",");
                        writer.append(assessment.getOrDefault("date", "").toString()).append(",");
                        writer.append(assessment.getOrDefault("score", "0").toString()).append(",");
                        writer.append(assessment.getOrDefault("percentage", "0").toString()).append("%\n");
                    }
                    writer.append("\n");
                }
            }
            
            // Write topic performance if available
            if (data.containsKey("topicPerformance")) {
                Map<String, Double> topicPerformance = (Map<String, Double>) data.get("topicPerformance");
                if (!topicPerformance.isEmpty()) {
                    writer.append("Topic Performance\n");
                    writer.append("Topic,Performance (%)\n");
                    
                    for (Map.Entry<String, Double> entry : topicPerformance.entrySet()) {
                        writer.append(entry.getKey()).append(",");
                        writer.append(String.format("%.2f", entry.getValue())).append("%\n");
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating CSV report", e);
            return null;
        }
        
        return filePath;
    }
    
    /**
     * Generates a PDF report from performance data.
     * This is a placeholder implementation. In a real application, you would use a PDF library.
     */
    private static String generatePdfReport(Map<String, Object> data, String fileName, String title, File reportsDir) {
        // Placeholder - in a real implementation, you would use a PDF library like iText or Apache PDFBox
        return generateCsvReport(data, fileName, title, reportsDir);
    }
    
    /**
     * Generates an Excel report from performance data.
     * This is a placeholder implementation. In a real application, you would use an Excel library.
     */
    private static String generateExcelReport(Map<String, Object> data, String fileName, String title, File reportsDir) {
        // Placeholder - in a real implementation, you would use an Excel library like Apache POI
        return generateCsvReport(data, fileName, title, reportsDir);
    }
    
    /**
     * Generates an HTML report from performance data.
     */
    private static String generateHtmlReport(Map<String, Object> data, String fileName, String title, File reportsDir) {
        File reportFile = new File(reportsDir, fileName + ".html");
        String filePath = reportFile.getAbsolutePath();
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("<!DOCTYPE html>\n");
            writer.append("<html>\n");
            writer.append("<head>\n");
            writer.append("  <title>Performance Report: ").append(title).append("</title>\n");
            writer.append("  <style>\n");
            writer.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.append("    h1, h2 { color: #3498db; }\n");
            writer.append("    table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n");
            writer.append("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            writer.append("    th { background-color: #3498db; color: white; }\n");
            writer.append("    tr:nth-child(even) { background-color: #f2f2f2; }\n");
            writer.append("    .summary-box { background-color: #f8f9fa; border: 1px solid #ddd; padding: 15px; margin-bottom: 20px; }\n");
            writer.append("  </style>\n");
            writer.append("</head>\n");
            writer.append("<body>\n");
            writer.append("  <h1>Performance Report: ").append(title).append("</h1>\n");
            writer.append("  <p>Generated: ").append(LocalDateTime.now().toString()).append("</p>\n");
            
            // Summary statistics
            writer.append("  <div class=\"summary-box\">\n");
            writer.append("    <h2>Summary Statistics</h2>\n");
            writer.append("    <table>\n");
            writer.append("      <tr><th>Metric</th><th>Value</th></tr>\n");
            if (data.containsKey("overallScore")) {
                writer.append("      <tr><td>Overall Score</td><td>").append(data.get("overallScore").toString()).append("%</td></tr>\n");
            }
            if (data.containsKey("totalAssessments")) {
                writer.append("      <tr><td>Total Assessments</td><td>").append(data.get("totalAssessments").toString()).append("</td></tr>\n");
            }
            writer.append("    </table>\n");
            writer.append("  </div>\n");
            
            // Assessment details if available
            if (data.containsKey("assessments")) {
                List<Map<String, Object>> assessments = (List<Map<String, Object>>) data.get("assessments");
                if (!assessments.isEmpty()) {
                    writer.append("  <h2>Assessment Details</h2>\n");
                    writer.append("  <table>\n");
                    writer.append("    <tr><th>Title</th><th>Type</th><th>Date</th><th>Score</th><th>Percentage</th></tr>\n");
                    
                    for (Map<String, Object> assessment : assessments) {
                        writer.append("    <tr>\n");
                        writer.append("      <td>").append(assessment.getOrDefault("title", "").toString()).append("</td>\n");
                        writer.append("      <td>").append(assessment.getOrDefault("type", "").toString()).append("</td>\n");
                        writer.append("      <td>").append(assessment.getOrDefault("date", "").toString()).append("</td>\n");
                        writer.append("      <td>").append(assessment.getOrDefault("score", "0").toString()).append("</td>\n");
                        writer.append("      <td>").append(assessment.getOrDefault("percentage", "0").toString()).append("%</td>\n");
                        writer.append("    </tr>\n");
                    }
                    writer.append("  </table>\n");
                }
            }
            
            // Topic performance if available
            if (data.containsKey("topicPerformance")) {
                Map<String, Double> topicPerformance = (Map<String, Double>) data.get("topicPerformance");
                if (!topicPerformance.isEmpty()) {
                    writer.append("  <h2>Topic Performance</h2>\n");
                    writer.append("  <table>\n");
                    writer.append("    <tr><th>Topic</th><th>Performance (%)</th></tr>\n");
                    
                    for (Map.Entry<String, Double> entry : topicPerformance.entrySet()) {
                        writer.append("    <tr>\n");
                        writer.append("      <td>").append(entry.getKey()).append("</td>\n");
                        writer.append("      <td>").append(String.format("%.2f", entry.getValue())).append("%</td>\n");
                        writer.append("    </tr>\n");
                    }
                    writer.append("  </table>\n");
                }
            }
            
            writer.append("</body>\n");
            writer.append("</html>");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating HTML report", e);
            return null;
        }
        
        return filePath;
    }
    
    /**
     * Records a report execution in the database.
     */
    private static void recordReportExecution(String reportType, String entityId, String format, String filePath) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO report_executions (execution_id, report_type, entity_id, format, file_path, execution_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)")) {
            
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, reportType);
            pstmt.setString(3, entityId);
            pstmt.setString(4, format);
            pstmt.setString(5, filePath);
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error recording report execution", e);
        }
    }
    
    /**
     * Gets recent report executions.
     *
     * @param limit The maximum number of executions to return
     * @return A list of recent report executions
     */
    public static List<Map<String, Object>> getRecentReportExecutions(int limit) {
        List<Map<String, Object>> executions = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM report_executions ORDER BY execution_date DESC LIMIT ?")) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> execution = new HashMap<>();
                    execution.put("executionId", rs.getString("execution_id"));
                    execution.put("reportType", rs.getString("report_type"));
                    execution.put("entityId", rs.getString("entity_id"));
                    execution.put("format", rs.getString("format"));
                    execution.put("filePath", rs.getString("file_path"));
                    execution.put("executionDate", rs.getTimestamp("execution_date").toLocalDateTime());
                    
                    executions.add(execution);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting recent report executions", e);
        }
        
        return executions;
    }
}
