package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Report;
import book.javafx.kenyattacatsystem.models.ReportExecution;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Service class for handling report operations.
 * Provides methods for creating, retrieving, and executing reports.
 */
public class ReportService {
    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
    
    /**
     * Creates a new report.
     *
     * @param report The report to create
     * @return True if the report was created successfully, false otherwise
     */
    public static boolean createReport(Report report) {
        String sql = "INSERT INTO reports (report_id, report_name, report_type, description, " +
                "query_template, parameters, created_by, creation_date, is_system) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Generate a unique ID if not provided
            if (report.getReportId() == null || report.getReportId().isEmpty()) {
                report.setReportId(UUID.randomUUID().toString());
            }
            
            pstmt.setString(1, report.getReportId());
            pstmt.setString(2, report.getReportName());
            pstmt.setString(3, report.getReportType());
            pstmt.setString(4, report.getDescription());
            pstmt.setString(5, report.getQueryTemplate());
            pstmt.setString(6, report.getParameters());
            pstmt.setString(7, report.getCreatedBy());
            pstmt.setTimestamp(8, report.getCreationDate() != null ? 
                    Timestamp.valueOf(report.getCreationDate()) : 
                    Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setBoolean(9, report.isSystem());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating report", e);
            return false;
        }
    }
    
    /**
     * Updates an existing report.
     *
     * @param report The report to update
     * @return True if the report was updated successfully, false otherwise
     */
    public static boolean updateReport(Report report) {
        String sql = "UPDATE reports SET report_name = ?, report_type = ?, description = ?, " +
                "query_template = ?, parameters = ? WHERE report_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, report.getReportName());
            pstmt.setString(2, report.getReportType());
            pstmt.setString(3, report.getDescription());
            pstmt.setString(4, report.getQueryTemplate());
            pstmt.setString(5, report.getParameters());
            pstmt.setString(6, report.getReportId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating report", e);
            return false;
        }
    }
    
    /**
     * Deletes a report.
     *
     * @param reportId The ID of the report to delete
     * @return True if the report was deleted successfully, false otherwise
     */
    public static boolean deleteReport(String reportId) {
        String sql = "DELETE FROM reports WHERE report_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reportId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting report", e);
            return false;
        }
    }
    
    /**
     * Gets a report by ID.
     *
     * @param reportId The ID of the report to get
     * @return The report, or null if not found
     */
    public static Report getReportById(String reportId) {
        String sql = "SELECT * FROM reports WHERE report_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reportId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting report by ID", e);
        }
        
        return null;
    }
    
    /**
     * Gets all reports.
     *
     * @return A list of all reports
     */
    public static List<Report> getAllReports() {
        String sql = "SELECT * FROM reports ORDER BY report_name";
        List<Report> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all reports", e);
        }
        
        return reports;
    }
    
    /**
     * Gets reports by type.
     *
     * @param reportType The type of reports to get
     * @return A list of reports of the specified type
     */
    public static List<Report> getReportsByType(String reportType) {
        String sql = "SELECT * FROM reports WHERE report_type = ? ORDER BY report_name";
        List<Report> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reportType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reports by type", e);
        }
        
        return reports;
    }
    
    /**
     * Retrieves all system reports.
     *
     * @return A list of system reports
     */
    public static List<Report> getSystemReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE is_system = true ORDER BY report_name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Report report = new Report();
                report.setReportId(rs.getString("report_id"));
                report.setReportName(rs.getString("report_name"));
                report.setReportType(rs.getString("report_type"));
                report.setDescription(rs.getString("description"));
                report.setQueryTemplate(rs.getString("query_template"));
                report.setParameters(rs.getString("parameters"));
                report.setCreatedBy(rs.getString("created_by"));
                report.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                report.setSystem(rs.getBoolean("is_system"));
                
                reports.add(report);
            }
            
            LOGGER.info("Retrieved " + reports.size() + " system reports");
            return reports;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving system reports", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves system analytics data.
     *
     * @param startDate The start date for analytics
     * @param endDate The end date for analytics
     * @param department The department to filter by (optional)
     * @param unitCode The unit code to filter by (optional)
     * @return A map containing system analytics data
     */
    public static Map<String, Object> getSystemAnalytics(LocalDate startDate, LocalDate endDate, 
                                                        String department, String unitCode) {
        Map<String, Object> analyticsData = new HashMap<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Get total users by role
            String userSql = "SELECT role, COUNT(*) as count FROM users " +
                             "WHERE created_at BETWEEN ? AND ? " +
                             (department != null && !department.isEmpty() ? "AND department = ? " : "") +
                             "GROUP BY role";
            
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                
                if (department != null && !department.isEmpty()) {
                    pstmt.setString(3, department);
                }
                
                ResultSet rs = pstmt.executeQuery();
                Map<String, Integer> usersByRole = new HashMap<>();
                
                while (rs.next()) {
                    usersByRole.put(rs.getString("role"), rs.getInt("count"));
                }
                
                analyticsData.put("usersByRole", usersByRole);
            }
            
            // Get assessment statistics
            String assessmentSql = "SELECT COUNT(*) as total, " +
                                  "SUM(CASE WHEN is_practice = true THEN 1 ELSE 0 END) as practice, " +
                                  "SUM(CASE WHEN is_practice = false THEN 1 ELSE 0 END) as formal " +
                                  "FROM assessments " +
                                  "WHERE created_at BETWEEN ? AND ? " +
                                  (unitCode != null && !unitCode.isEmpty() ? "AND unit_code = ? " : "");
            
            try (PreparedStatement pstmt = conn.prepareStatement(assessmentSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                
                if (unitCode != null && !unitCode.isEmpty()) {
                    pstmt.setString(3, unitCode);
                }
                
                ResultSet rs = pstmt.executeQuery();
                Map<String, Integer> assessmentStats = new HashMap<>();
                
                if (rs.next()) {
                    assessmentStats.put("total", rs.getInt("total"));
                    assessmentStats.put("practice", rs.getInt("practice"));
                    assessmentStats.put("formal", rs.getInt("formal"));
                }
                
                analyticsData.put("assessmentStats", assessmentStats);
            }
            
            // Get performance statistics
            String performanceSql = "SELECT AVG(score) as avg_score, " +
                                   "COUNT(DISTINCT student_id) as students_count " +
                                   "FROM student_assessments " +
                                   "WHERE end_time BETWEEN ? AND ? " +
                                   (unitCode != null && !unitCode.isEmpty() ? 
                                    "AND assessment_id IN (SELECT assessment_id FROM assessments WHERE unit_code = ?) " : "");
            
            try (PreparedStatement pstmt = conn.prepareStatement(performanceSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                
                if (unitCode != null && !unitCode.isEmpty()) {
                    pstmt.setString(3, unitCode);
                }
                
                ResultSet rs = pstmt.executeQuery();
                Map<String, Object> performanceStats = new HashMap<>();
                
                if (rs.next()) {
                    performanceStats.put("avgScore", rs.getDouble("avg_score"));
                    performanceStats.put("studentsCount", rs.getInt("students_count"));
                }
                
                analyticsData.put("performanceStats", performanceStats);
            }
            
            // Get report execution statistics
            String reportSql = "SELECT COUNT(*) as count, " +
                              "report_type, " +
                              "DATE_FORMAT(execution_time, '%Y-%m-%d') as date " +
                              "FROM report_executions " +
                              "WHERE execution_time BETWEEN ? AND ? " +
                              "GROUP BY report_type, DATE_FORMAT(execution_time, '%Y-%m-%d') " +
                              "ORDER BY date";
            
            try (PreparedStatement pstmt = conn.prepareStatement(reportSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                
                ResultSet rs = pstmt.executeQuery();
                List<Map<String, Object>> reportStats = new ArrayList<>();
                
                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", rs.getInt("count"));
                    stat.put("reportType", rs.getString("report_type"));
                    stat.put("date", rs.getString("date"));
                    reportStats.add(stat);
                }
                
                analyticsData.put("reportStats", reportStats);
            }
            
            LOGGER.info("Retrieved system analytics data");
            return analyticsData;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving system analytics", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Retrieves recent report executions.
     *
     * @param limit The maximum number of executions to retrieve
     * @return A list of recent report executions
     */
    public static List<Map<String, Object>> getRecentReportExecutions(int limit) {
        List<Map<String, Object>> executions = new ArrayList<>();
        String sql = "SELECT re.*, r.report_name " +
                    "FROM report_executions re " +
                    "JOIN reports r ON re.report_id = r.report_id " +
                    "ORDER BY re.execution_time DESC " +
                    "LIMIT ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> execution = new HashMap<>();
                execution.put("executionId", rs.getString("execution_id"));
                execution.put("reportId", rs.getString("report_id"));
                execution.put("reportName", rs.getString("report_name"));
                execution.put("executedBy", rs.getString("executed_by"));
                execution.put("executionTime", rs.getTimestamp("execution_time"));
                execution.put("parameters", rs.getString("parameters"));
                execution.put("outputFormat", rs.getString("output_format"));
                execution.put("outputPath", rs.getString("output_path"));
                execution.put("status", rs.getString("status"));
                
                executions.add(execution);
            }
            
            LOGGER.info("Retrieved " + executions.size() + " recent report executions");
            return executions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving recent report executions", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Executes a report with the specified parameters and saves it to the specified file path.
     *
     * @param reportId The ID of the report to execute
     * @param parameters The parameters for the report
     * @param outputPath The path to save the report output
     * @return True if the report was executed successfully, false otherwise
     */
    public static boolean executeReportToFile(String reportId, Map<String, Object> parameters, String outputPath) {
        try {
            // Get the report
            Report report = getReportById(reportId);
            if (report == null) {
                LOGGER.severe("Report not found with ID: " + reportId);
                return false;
            }
            
            // Create a report execution record
            ReportExecution execution = new ReportExecution();
            execution.setExecutionId(UUID.randomUUID().toString());
            execution.setReportId(reportId);
            execution.setExecutedBy("system"); // This should be replaced with the actual user ID
            execution.setExecutionDate(LocalDateTime.now());
            execution.setParameters(new JSONObject(parameters).toString());
            execution.setResultFilePath(outputPath);
            
            // Save the execution record
            saveReportExecution(execution);
            
            // Execute the report query with parameters
            String query = report.getQueryTemplate();
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query = query.replace(":" + entry.getKey(), entry.getValue().toString());
            }
            
            // Execute the query and get the results
            List<Map<String, Object>> results = executeReportQuery(query);
            
            // Generate the report file
            boolean success = generateReportFile(results, report.getReportType(), outputPath);
            
            return success;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing report", e);
            return false;
        }
    }
    
    /**
     * Executes a report query and returns the results.
     *
     * @param query The SQL query to execute
     * @return A list of result rows
     * @throws SQLException If a database error occurs
     */
    private static List<Map<String, Object>> executeReportQuery(String query) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                
                results.add(row);
            }
        }
        
        return results;
    }
    
    /**
     * Generates a report file from the results.
     *
     * @param results The report results
     * @param reportType The type of report
     * @param outputPath The path to save the report
     * @return True if the file was generated successfully, false otherwise
     */
    private static boolean generateReportFile(List<Map<String, Object>> results, String reportType, String outputPath) {
        try {
            File file = new File(outputPath);
            
            // Create parent directories if they don't exist
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            // Generate the file based on the report type
            switch (reportType.toLowerCase()) {
                case "csv":
                    return generateCsvReport(results, outputPath);
                case "pdf":
                    return generatePdfReport(results, outputPath);
                case "excel":
                    return generateExcelReport(results, outputPath);
                case "html":
                    return generateHtmlReport(results, outputPath);
                default:
                    LOGGER.warning("Unsupported report type: " + reportType);
                    return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report file", e);
            return false;
        }
    }
    
    /**
     * Generates a CSV report.
     *
     * @param results The report results
     * @param outputPath The path to save the report
     * @return True if the file was generated successfully, false otherwise
     */
    private static boolean generateCsvReport(List<Map<String, Object>> results, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            if (results.isEmpty()) {
                writer.write("No data available");
                return true;
            }
            
            // Write headers
            Map<String, Object> firstRow = results.get(0);
            boolean first = true;
            for (String header : firstRow.keySet()) {
                if (!first) {
                    writer.write(",");
                }
                writer.write("\"" + header + "\"");
                first = false;
            }
            writer.write("\n");
            
            // Write data rows
            for (Map<String, Object> row : results) {
                first = true;
                for (Object value : row.values()) {
                    if (!first) {
                        writer.write(",");
                    }
                    writer.write("\"" + (value != null ? value.toString() : "") + "\"");
                    first = false;
                }
                writer.write("\n");
            }
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating CSV report", e);
            return false;
        }
    }
    
    /**
     * Generates a PDF report.
     * 
     * @param results The report results
     * @param outputPath The path to save the report
     * @return True if the file was generated successfully, false otherwise
     */
    private static boolean generatePdfReport(List<Map<String, Object>> results, String outputPath) {
        try {
            // Create a new PDF document
            PDDocument document = new PDDocument();
            
            // Define page dimensions and margins
            float margin = 50;
            float yStart = 750;
            float tableWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float rowHeight = 20;
            float cellPadding = 5;
            int rowsPerPage = 25;
            
            // Get column names from the first row if there are results
            List<String> columns = new ArrayList<>();
            if (!results.isEmpty()) {
                Map<String, Object> firstRow = results.get(0);
                columns = new ArrayList<>(firstRow.keySet());
            }
            
            // Calculate column width - equal width for all columns
            float colWidth = tableWidth / Math.max(1, columns.size());
            
            // Process data in batches for pagination
            int totalRows = results.size();
            int processedRows = 0;
            
            while (processedRows < totalRows || (processedRows == 0 && results.isEmpty())) {
                // Add a new page for each batch
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                
                // Create a content stream for adding content to the page
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                
                // Add title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText("Kenyatta CAT System - Report");
                contentStream.endText();
                
                // Add timestamp
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yStart - 20);
                contentStream.showText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                contentStream.endText();
                
                // Add page number
                int pageNum = (processedRows / rowsPerPage) + 1;
                int totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);
                if (totalRows == 0) totalPages = 1;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(PDRectangle.A4.getWidth() - margin - 100, yStart - 20);
                contentStream.showText("Page " + pageNum + " of " + totalPages);
                contentStream.endText();
                
                float yPosition = yStart - 50;
                
                if (!results.isEmpty()) {
                    // Draw table header
                    // Draw header background
                    contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f); // Light gray
                    contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
                    
                    // Draw header text
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    float xPosition = margin + cellPadding;
                    
                    for (String column : columns) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(xPosition, yPosition - rowHeight + cellPadding);
                        
                        // Truncate header if too long
                        String displayText = column;
                        if (displayText.length() > 15) {
                            displayText = displayText.substring(0, 12) + "...";
                        }
                        
                        contentStream.showText(displayText);
                        contentStream.endText();
                        xPosition += colWidth;
                    }
                    
                    // Draw horizontal line below header
                    contentStream.setLineWidth(0.5f);
                    contentStream.moveTo(margin, yPosition - rowHeight);
                    contentStream.lineTo(margin + tableWidth, yPosition - rowHeight);
                    contentStream.stroke();
                    
                    yPosition -= rowHeight;
                    
                    // Draw data rows for this page
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    
                    int rowsOnThisPage = Math.min(rowsPerPage, totalRows - processedRows);
                    for (int i = 0; i < rowsOnThisPage; i++) {
                        Map<String, Object> row = results.get(processedRows + i);
                        
                        // Alternate row background for better readability
                        if (i % 2 == 1) {
                            contentStream.setNonStrokingColor(0.95f, 0.95f, 0.95f); // Very light gray
                            contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                            contentStream.fill();
                            contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
                        }
                        
                        xPosition = margin + cellPadding;
                        for (String column : columns) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(xPosition, yPosition - rowHeight + cellPadding);
                            
                            // Get value and convert to string, handling nulls
                            Object value = row.get(column);
                            String text = value != null ? value.toString() : "";
                            
                            // Truncate text if too long
                            if (text.length() > 15) {
                                text = text.substring(0, 12) + "...";
                            }
                            
                            contentStream.showText(text);
                            contentStream.endText();
                            xPosition += colWidth;
                        }
                        
                        // Draw horizontal line below row
                        contentStream.setLineWidth(0.2f);
                        contentStream.moveTo(margin, yPosition - rowHeight);
                        contentStream.lineTo(margin + tableWidth, yPosition - rowHeight);
                        contentStream.stroke();
                        
                        yPosition -= rowHeight;
                    }
                    
                    processedRows += rowsOnThisPage;
                } else {
                    // No data message
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition - 30);
                    contentStream.showText("No data available for this report.");
                    contentStream.endText();
                    
                    // Break the loop since we only need one page for no data
                    processedRows = 1;
                }
                
                // Close the content stream for this page
                contentStream.close();
            }
            
            // Save the document
            document.save(outputPath);
            document.close();
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating PDF report", e);
            return false;
        }
    }
    
    /**
     * Generates an Excel report.
     * 
     * @param results The report results
     * @param outputPath The path to save the report
     * @return True if the file was generated successfully, false otherwise
     */
    private static boolean generateExcelReport(List<Map<String, Object>> results, String outputPath) {
        // For now, we'll just create a simple text file with a note that this would be an Excel file
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Excel Report\n\n");
            writer.write("This is a placeholder for an Excel report. In a real implementation, this would generate an Excel file.\n\n");
            
            // Write the data as text
            writer.write("Data:\n");
            for (Map<String, Object> row : results) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    writer.write(entry.getKey() + ": " + (entry.getValue() != null ? entry.getValue().toString() : "null") + "\n");
                }
                writer.write("\n");
            }
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating Excel report", e);
            return false;
        }
    }
    
    /**
     * Generates an HTML report.
     * 
     * @param results The report results
     * @param outputPath The path to save the report
     * @return True if the file was generated successfully, false otherwise
     */
    private static boolean generateHtmlReport(List<Map<String, Object>> results, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("    <title>Report</title>\n");
            writer.write("    <style>\n");
            writer.write("        table { border-collapse: collapse; width: 100%; }\n");
            writer.write("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            writer.write("        th { background-color: #f2f2f2; }\n");
            writer.write("        tr:nth-child(even) { background-color: #f9f9f9; }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("    <h1>Report</h1>\n");
            
            if (results.isEmpty()) {
                writer.write("    <p>No data available</p>\n");
            } else {
                writer.write("    <table>\n");
                writer.write("        <tr>\n");
                
                // Write headers
                Map<String, Object> firstRow = results.get(0);
                for (String header : firstRow.keySet()) {
                    writer.write("            <th>" + header + "</th>\n");
                }
                writer.write("        </tr>\n");
                
                // Write data rows
                for (Map<String, Object> row : results) {
                    writer.write("        <tr>\n");
                    for (Object value : row.values()) {
                        writer.write("            <td>" + (value != null ? value.toString() : "") + "</td>\n");
                    }
                    writer.write("        </tr>\n");
                }
                
                writer.write("    </table>\n");
            }
            
            writer.write("</body>\n");
            writer.write("</html>\n");
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating HTML report", e);
            return false;
        }
    }
    
    /**
     * Saves a report execution record to the database.
     *
     * @param execution The report execution to save
     * @return True if the execution was saved successfully, false otherwise
     */
    private static boolean saveReportExecution(ReportExecution execution) {
        String sql = "INSERT INTO report_executions (execution_id, report_id, executed_by, " +
                    "execution_date, parameters, result_file_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, execution.getExecutionId());
            pstmt.setString(2, execution.getReportId());
            pstmt.setString(3, execution.getExecutedBy());
            pstmt.setTimestamp(4, Timestamp.valueOf(execution.getExecutionDate()));
            pstmt.setString(5, execution.getParameters());
            pstmt.setString(6, execution.getResultFilePath());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving report execution", e);
            return false;
        }
    }
    
    /**
     * Executes a report with the specified parameters.
     *
     * @param reportId The ID of the report to execute
     * @param parameters The parameters to use for execution
     * @param executedBy The ID of the user executing the report
     * @return The execution result, or null if execution failed
     */
    public static ReportExecution executeReport(String reportId, Map<String, Object> parameters, String executedBy) {
        Report report = getReportById(reportId);
        if (report == null) {
            LOGGER.log(Level.WARNING, "Report not found: " + reportId);
            return null;
        }
        
        // Convert parameters to JSON
        JSONObject paramsJson = new JSONObject(parameters);
        String paramsString = paramsJson.toString();
        
        // Extract the format parameter if available
        String reportFormat = "CSV"; // Default to CSV
        if (parameters.containsKey("format")) {
            reportFormat = String.valueOf(parameters.get("format"));
            // Remove format from parameters as it's not part of the SQL query
            parameters.remove("format");
        }
        
        // Prepare the SQL query with parameters
        String queryTemplate = report.getQueryTemplate();
        String finalQuery = replaceQueryParameters(queryTemplate, parameters);
        
        try {
            // Execute the query
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(finalQuery);
            
            // Convert ResultSet to List<Map> for more flexible processing
            List<Map<String, Object>> results = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                
                results.add(row);
            }
            
            // Generate report file based on format
            String outputFilePath;
            if ("PDF".equalsIgnoreCase(reportFormat)) {
                // Generate PDF report file path
                outputFilePath = "reports/" + report.getReportName() + "_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            } else {
                // Default to CSV file path
                outputFilePath = "reports/" + report.getReportName() + "_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            }
            
            // Ensure the directory exists
            File file = new File(outputFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            // Generate the report file
            boolean success;
            if ("PDF".equalsIgnoreCase(reportFormat)) {
                success = generatePdfReport(results, outputFilePath);
            } else {
                success = generateCsvReport(results, outputFilePath);
            }
            
            // If generation failed, return null
            if (!success) {
                LOGGER.log(Level.SEVERE, "Failed to generate report file");
                return null;
            }
            
            // Record the execution
            ReportExecution execution = new ReportExecution();
            execution.setExecutionId(UUID.randomUUID().toString());
            execution.setReportId(reportId);
            execution.setExecutedBy(executedBy);
            execution.setExecutionDate(LocalDateTime.now());
            execution.setParameters(paramsString);
            execution.setResultFilePath(outputFilePath);
            execution.setReport(report);
            
            // Save execution to database
            saveReportExecution(execution);
            
            return execution;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing report", e);
            return null;
        }
    }
    
    /**
     * Gets all executions for a report.
     *
     * @param reportId The ID of the report
     * @return A list of executions for the report
     */
    public static List<ReportExecution> getReportExecutions(String reportId) {
        String sql = "SELECT * FROM report_executions WHERE report_id = ? ORDER BY execution_date DESC";
        List<ReportExecution> executions = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, reportId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    executions.add(mapResultSetToReportExecution(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting report executions", e);
        }
        
        return executions;
    }
    
    /**
     * Retrieves report executions for a specific lecturer.
     *
     * @param lecturerId The ID of the lecturer
     * @return A list of report executions for the lecturer
     */
    public static List<ReportExecution> getReportExecutionsByLecturer(String lecturerId) {
        List<ReportExecution> executions = new ArrayList<>();
        
        // Log the lecturer ID for debugging
        LOGGER.info("Fetching report executions for lecturer: " + lecturerId);
        
        // First check if the table exists
        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            ResultSet tables = dbMetaData.getTables(null, null, "report_executions", null);
            
            if (!tables.next()) {
                LOGGER.warning("Table 'report_executions' does not exist in the database");
                // Try to create the table if it doesn't exist
                createReportExecutionsTable(conn);
            } else {
                LOGGER.info("Table 'report_executions' exists in the database");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking for report_executions table", e);
        }
        
        String sql = "SELECT re.*, r.report_name " +
                    "FROM report_executions re " +
                    "JOIN reports r ON re.report_id = r.report_id " +
                    "WHERE re.executed_by = ? " +
                    "ORDER BY re.execution_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, lecturerId);
            
            try {
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    ReportExecution execution = new ReportExecution();
                    execution.setExecutionId(rs.getString("execution_id"));
                    execution.setReportId(rs.getString("report_id"));
                    execution.setExecutedBy(rs.getString("executed_by"));
                    
                    Timestamp executionDate = rs.getTimestamp("execution_date");
                    if (executionDate != null) {
                        execution.setExecutionDate(executionDate.toLocalDateTime());
                    }
                    
                    execution.setParameters(rs.getString("parameters"));
                    execution.setResultFilePath(rs.getString("result_file_path"));
                    
                    // Add the report name from the join
                    execution.setReportName(rs.getString("report_name"));
                    
                    executions.add(execution);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error executing query: " + sql, e);
                LOGGER.log(Level.SEVERE, "SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
                
                // Try alternative column names if the first query fails
                tryAlternativeQuery(conn, lecturerId, executions);
            }
            
            LOGGER.info("Retrieved " + executions.size() + " report executions for lecturer: " + lecturerId);
            return executions;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving report executions for lecturer: " + lecturerId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Try an alternative query with different column names
     */
    private static void tryAlternativeQuery(Connection conn, String lecturerId, List<ReportExecution> executions) {
        // Try with execution_time instead of execution_date
        String altSql = "SELECT re.*, r.report_name " +
                       "FROM report_executions re " +
                       "JOIN reports r ON re.report_id = r.report_id " +
                       "WHERE re.executed_by = ? " +
                       "ORDER BY re.execution_time DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(altSql)) {
            pstmt.setString(1, lecturerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ReportExecution execution = new ReportExecution();
                execution.setExecutionId(rs.getString("execution_id"));
                execution.setReportId(rs.getString("report_id"));
                execution.setExecutedBy(rs.getString("executed_by"));
                
                Timestamp executionTime = rs.getTimestamp("execution_time");
                if (executionTime != null) {
                    execution.setExecutionDate(executionTime.toLocalDateTime());
                }
                
                execution.setParameters(rs.getString("parameters"));
                execution.setResultFilePath(rs.getString("output_path"));  // Try alternative column name
                
                // Add the report name from the join
                execution.setReportName(rs.getString("report_name"));
                
                executions.add(execution);
            }
            
            LOGGER.info("Retrieved " + executions.size() + " report executions using alternative query");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Alternative query also failed", e);
        }
    }
    
    /**
     * Creates the report_executions table if it doesn't exist
     */
    private static void createReportExecutionsTable(Connection conn) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS report_executions (" +
                              "execution_id VARCHAR(36) PRIMARY KEY, " +
                              "report_id VARCHAR(36) NOT NULL, " +
                              "executed_by VARCHAR(36) NOT NULL, " +
                              "execution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                              "parameters TEXT, " +
                              "result_file_path VARCHAR(255), " +
                              "FOREIGN KEY (report_id) REFERENCES reports(report_id), " +
                              "FOREIGN KEY (executed_by) REFERENCES users(user_id)" +
                              ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            LOGGER.info("Created report_executions table");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating report_executions table", e);
        }
    }
    
    /**
     * Maps a result set to a Report object.
     *
     * @param rs The result set
     * @return The Report object
     * @throws SQLException If a database access error occurs
     */
    private static Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getString("report_id"));
        report.setReportName(rs.getString("report_name"));
        report.setReportType(rs.getString("report_type"));
        report.setDescription(rs.getString("description"));
        report.setQueryTemplate(rs.getString("query_template"));
        report.setParameters(rs.getString("parameters"));
        report.setCreatedBy(rs.getString("created_by"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            report.setCreationDate(creationDate.toLocalDateTime());
        }
        
        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            report.setLastModified(lastModified.toLocalDateTime());
        }
        
        report.setSystem(rs.getBoolean("is_system"));
        
        return report;
    }
    
    /**
     * Maps a result set to a ReportExecution object.
     *
     * @param rs The result set
     * @return The ReportExecution object
     * @throws SQLException If a database access error occurs
     */
    private static ReportExecution mapResultSetToReportExecution(ResultSet rs) throws SQLException {
        ReportExecution execution = new ReportExecution();
        execution.setExecutionId(rs.getString("execution_id"));
        execution.setReportId(rs.getString("report_id"));
        execution.setExecutedBy(rs.getString("executed_by"));
        
        Timestamp executionDate = rs.getTimestamp("execution_date");
        if (executionDate != null) {
            execution.setExecutionDate(executionDate.toLocalDateTime());
        }
        
        execution.setParameters(rs.getString("parameters"));
        execution.setResultFilePath(rs.getString("result_file_path"));
        
        // Load the associated report
        execution.setReport(getReportById(execution.getReportId()));
        
        return execution;
    }
    
    /**
     * Replaces parameters in a query template with actual values.
     *
     * @param queryTemplate The query template
     * @param parameters The parameters
     * @return The final query with parameters replaced
     */
    private static String replaceQueryParameters(String queryTemplate, Map<String, Object> parameters) {
        String finalQuery = queryTemplate;
        
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = ":" + entry.getKey();
            Object value = entry.getValue();
            
            if (value == null) {
                // For NULL values, replace with SQL NULL
                finalQuery = finalQuery.replace(placeholder, "NULL");
            } else if (value instanceof String) {
                String strValue = (String) value;
                // Check if it's a date string in ISO format (yyyy-MM-dd)
                if (strValue.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                    finalQuery = finalQuery.replace(placeholder, "'" + strValue + "'");
                } else {
                    // Escape single quotes in string values
                    strValue = strValue.replace("'", "''");
                    finalQuery = finalQuery.replace(placeholder, "'" + strValue + "'");
                }
            } else if (value instanceof Date) {
                finalQuery = finalQuery.replace(placeholder, "'" + new java.sql.Date(((Date) value).getTime()) + "'");
            } else if (value instanceof LocalDate) {
                finalQuery = finalQuery.replace(placeholder, "'" + value + "'");
            } else if (value instanceof LocalDateTime) {
                finalQuery = finalQuery.replace(placeholder, "'" + value + "'");
            } else {
                finalQuery = finalQuery.replace(placeholder, String.valueOf(value));
            }
        }
        
        LOGGER.info("Final query: " + finalQuery);
        return finalQuery;
    }
    
    /**
     * Gets the file path for a report.
     *
     * @param reportName The report name
     * @return The file path or null if no matching file is found
     */
    public static String getReportFilePath(String reportName) {
        if (reportName == null || reportName.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot get report file path: report name is null or empty");
            return null;
        }
        
        // Create reports directory if it doesn't exist
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdir();
        }
        
        // Find the most recent report file
        File[] files = reportsDir.listFiles((dir, name) -> 
                name.startsWith(reportName.replaceAll("\\s+", "_")) && name.endsWith(".csv"));
        
        if (files != null && files.length > 0) {
            // Sort by last modified date (descending)
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            return files[0].getPath();
        }
        
        LOGGER.log(Level.WARNING, "No report files found for report name: {0}", reportName);
        return null;
    }
    
    /**
     * Downloads a report file.
     *
     * @param filePath The file path
     * @return True if the download was successful, false otherwise
     */
    public static boolean downloadReport(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot download report: file path is null or empty");
            return false;
        }
        
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // Open the file with the default application
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "", filePath);
                pb.start();
                return true;
            }
            LOGGER.log(Level.WARNING, "Report file does not exist: {0}", filePath);
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error downloading report", e);
            return false;
        }
    }
    
    /**
     * Generates a CSV file from a result set.
     *
     * @param rs The result set
     * @param reportName The name of the report
     * @return The file path or null if no matching file is found
     */
    private static String generateReportFile(ResultSet rs, String reportName) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Create reports directory if it doesn't exist
            String userDir = System.getProperty("user.dir");
            File reportsDir = new File(userDir, "reports");
            if (!reportsDir.exists()) {
                boolean created = reportsDir.mkdirs();
                if (!created) {
                    LOGGER.log(Level.WARNING, "Failed to create reports directory: " + reportsDir.getAbsolutePath());
                    // Try to create in temp directory as fallback
                    reportsDir = new File(System.getProperty("java.io.tmpdir"), "kenyatta_reports");
                    if (!reportsDir.exists()) {
                        reportsDir.mkdirs();
                    }
                }
            }
            
            LOGGER.info("Reports directory: " + reportsDir.getAbsolutePath());
            
            // Create file name with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String safeReportName = reportName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            File reportFile = new File(reportsDir, safeReportName + "_" + timestamp + ".csv");
            String fileName = reportFile.getAbsolutePath();
            
            LOGGER.info("Generating report file: " + fileName);
            
            try (FileWriter writer = new FileWriter(fileName)) {
                // Write header
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(metaData.getColumnName(i));
                    if (i < columnCount) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
                
                // Write data
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        if (value != null) {
                            // Escape quotes and commas
                            value = value.replace("\"", "\"\"");
                            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                                value = "\"" + value + "\"";
                            }
                        } else {
                            value = "";
                        }
                        writer.append(value);
                        if (i < columnCount) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }
            }
            
            return fileName;
            
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating report file", e);
            return null;
        }
    }
    
    /**
     * Creates default system reports.
     * This method should be called during system initialization.
     */
    public static void createDefaultReports() {
        // First, check if we have an admin user to assign as the creator
        String adminUserId = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE role = 'Admin' LIMIT 1")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                adminUserId = rs.getString("user_id");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding admin user for report creation", e);
        }
        
        // If no admin found, we can't create reports due to foreign key constraint
        if (adminUserId == null) {
            LOGGER.log(Level.WARNING, "No admin user found to assign as report creator. Reports will not be created.");
            return;
        }
        
        // First, delete all existing system reports
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Delete report executions first to avoid foreign key constraints
            try (PreparedStatement deleteExecStmt = conn.prepareStatement("DELETE FROM report_executions WHERE report_id IN (SELECT report_id FROM reports WHERE is_system = true)")) {
                int execsDeleted = deleteExecStmt.executeUpdate();
                LOGGER.info("Deleted " + execsDeleted + " report executions for system reports");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error deleting report executions: " + e.getMessage());
                // Continue anyway, the table might not exist yet
            }
            
            // Now delete the system reports
            try (PreparedStatement deleteReportStmt = conn.prepareStatement("DELETE FROM reports WHERE is_system = true")) {
                int reportsDeleted = deleteReportStmt.executeUpdate();
                LOGGER.info("Deleted " + reportsDeleted + " existing system reports");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting existing system reports", e);
            // Continue anyway to try to create the reports
        }
        
        // 1. Student Performance Report - Essential for academic monitoring
        Report studentPerformanceReport = new Report();
        studentPerformanceReport.setReportId(UUID.randomUUID().toString());
        studentPerformanceReport.setReportName("Student Performance Report");
        studentPerformanceReport.setReportType("PERFORMANCE");
        studentPerformanceReport.setDescription("Shows student performance in assessments");
        studentPerformanceReport.setQueryTemplate(
                "SELECT s.student_id, u.full_name, a.assessment_id, a.title, " +
                "sa.score, sa.total_possible, (sa.score / sa.total_possible * 100) AS percentage, " +
                "sa.start_time, sa.end_time, " +
                "TIMESTAMPDIFF(MINUTE, sa.start_time, sa.end_time) AS duration_minutes " +
                "FROM student_assessments sa " +
                "JOIN students s ON sa.student_id = s.student_id " +
                "JOIN users u ON s.user_id = u.user_id " +
                "JOIN assessments a ON sa.assessment_id = a.assessment_id " +
                "WHERE (:unit_code IS NULL OR a.unit_code = :unit_code) " +
                "AND (sa.start_time BETWEEN :start_date AND :end_date) " +
                "ORDER BY u.full_name, sa.start_time");
        studentPerformanceReport.setParameters("{\"unit_code\":null,\"start_date\":null,\"end_date\":null}");
        studentPerformanceReport.setCreatedBy(adminUserId);
        studentPerformanceReport.setCreationDate(LocalDateTime.now());
        studentPerformanceReport.setSystem(true);
        createReport(studentPerformanceReport);
        LOGGER.info("Created system report: Student Performance Report");
        
        // 2. Assessment Statistics Report - Essential for assessment evaluation
        Report assessmentStatsReport = new Report();
        assessmentStatsReport.setReportId(UUID.randomUUID().toString());
        assessmentStatsReport.setReportName("Assessment Statistics Report");
        assessmentStatsReport.setReportType("ASSESSMENT_STATS");
        assessmentStatsReport.setDescription("Shows statistics for assessments including average scores");
        assessmentStatsReport.setQueryTemplate(
                "SELECT a.assessment_id, a.title, a.unit_code, a.total_marks, " +
                "COUNT(sa.attempt_id) AS attempt_count, " +
                "AVG(sa.score) AS average_score, " +
                "MIN(sa.score) AS min_score, " +
                "MAX(sa.score) AS max_score, " +
                "AVG(TIMESTAMPDIFF(MINUTE, sa.start_time, sa.end_time)) AS avg_duration_minutes " +
                "FROM assessments a " +
                "LEFT JOIN student_assessments sa ON a.assessment_id = sa.assessment_id " +
                "WHERE (:unit_code IS NULL OR a.unit_code = :unit_code) " +
                "AND (:start_date IS NULL OR :end_date IS NULL OR " +
                "EXISTS (SELECT 1 FROM student_assessments sa2 WHERE sa2.assessment_id = a.assessment_id " +
                "AND sa2.start_time BETWEEN :start_date AND :end_date)) " +
                "GROUP BY a.assessment_id " +
                "ORDER BY a.assessment_id DESC");
        assessmentStatsReport.setParameters("{\"unit_code\":null,\"start_date\":null,\"end_date\":null}");
        assessmentStatsReport.setCreatedBy(adminUserId);
        assessmentStatsReport.setCreationDate(LocalDateTime.now());
        assessmentStatsReport.setSystem(true);
        createReport(assessmentStatsReport);
        LOGGER.info("Created system report: Assessment Statistics Report");
        
        // 3. Practice Test Performance Report - Important for tracking practice test usage
        Report practiceTestReport = new Report();
        practiceTestReport.setReportId(UUID.randomUUID().toString());
        practiceTestReport.setReportName("Practice Test Performance Report");
        practiceTestReport.setReportType("PRACTICE_PERFORMANCE");
        practiceTestReport.setDescription("Shows student performance in practice tests");
        practiceTestReport.setQueryTemplate(
                "SELECT pa.practice_id, s.student_id, u.full_name, pa.title, pa.unit_code, " +
                "pa.score, pa.total_possible, pa.percentage, pa.grade, " +
                "pa.start_time, pa.end_time, " +
                "TIMESTAMPDIFF(MINUTE, pa.start_time, pa.end_time) AS duration_minutes " +
                "FROM practice_assessments pa " +
                "JOIN students s ON pa.student_id = s.student_id " +
                "JOIN users u ON s.user_id = u.user_id " +
                "WHERE (:unit_code IS NULL OR pa.unit_code = :unit_code) " +
                "AND (pa.completion_date BETWEEN :start_date AND :end_date) " +
                "ORDER BY pa.completion_date DESC");
        practiceTestReport.setParameters("{\"unit_code\":null,\"start_date\":null,\"end_date\":null}");
        practiceTestReport.setCreatedBy(adminUserId);
        practiceTestReport.setCreationDate(LocalDateTime.now());
        practiceTestReport.setSystem(true);
        createReport(practiceTestReport);
        LOGGER.info("Created system report: Practice Test Performance Report");
        
        LOGGER.info("Default system reports created");
    }
    
    /**
     * Initializes default reports when the application starts.
     */
    public static void init() {
        createDefaultReports();
    }
}
