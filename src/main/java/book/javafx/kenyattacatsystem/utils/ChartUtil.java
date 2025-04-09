package book.javafx.kenyattacatsystem.utils;

import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;

/**
 * Utility class for creating charts for performance analytics.
 */
public class ChartUtil {

    /**
     * Creates a pie chart for topic performance.
     *
     * @param topicPerformance Map of topic names to performance percentages
     * @return A configured PieChart
     */
    public static PieChart createTopicPieChart(Map<String, Double> topicPerformance) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Double> entry : topicPerformance.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + String.format("%.1f", entry.getValue()) + "%)", 
                                              entry.getValue()));
        }
        
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Topic Performance");
        chart.setLegendSide(Side.RIGHT);
        chart.setLabelsVisible(false);
        chart.setAnimated(true);
        
        // Add percentage labels
        pieChartData.forEach(data -> {
            String percentage = String.format("%.1f%%", data.getPieValue());
            Text text = new Text(percentage);
            text.setFont(Font.font("Arial", 10));
            data.getNode().setOnMouseEntered(event -> {
                text.setVisible(true);
            });
            data.getNode().setOnMouseExited(event -> {
                text.setVisible(false);
            });
        });
        
        return chart;
    }
    
    /**
     * Creates a bar chart for assessment performance.
     *
     * @param assessments List of assessment data
     * @return A configured BarChart
     */
    public static BarChart<String, Number> createAssessmentBarChart(List<Map<String, Object>> assessments) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("Assessment");
        yAxis.setLabel("Score (%)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Assessment Performance");
        barChart.setAnimated(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Score");
        
        for (Map<String, Object> assessment : assessments) {
            String title = (String) assessment.get("title");
            Double percentage = (Double) assessment.get("percentage");
            series.getData().add(new XYChart.Data<>(title, percentage));
        }
        
        barChart.getData().add(series);
        
        // Add custom styling
        for (XYChart.Series<String, Number> s : barChart.getData()) {
            for (XYChart.Data<String, Number> data : s.getData()) {
                Node node = data.getNode();
                
                // Color bars based on score
                double score = data.getYValue().doubleValue();
                if (score >= 80) {
                    node.setStyle("-fx-bar-fill: #2ecc71;"); // Green for high scores
                } else if (score >= 60) {
                    node.setStyle("-fx-bar-fill: #f39c12;"); // Orange for medium scores
                } else {
                    node.setStyle("-fx-bar-fill: #e74c3c;"); // Red for low scores
                }
                
                // Add hover effect
                node.setOnMouseEntered(event -> {
                    node.setStyle(node.getStyle() + "-fx-opacity: 0.8;");
                });
                node.setOnMouseExited(event -> {
                    node.setStyle(node.getStyle().replace("-fx-opacity: 0.8;", ""));
                });
            }
        }
        
        return barChart;
    }
    
    /**
     * Creates a line chart for trend analysis.
     *
     * @param assessments List of assessment data, ordered by date
     * @return A configured LineChart
     */
    public static LineChart<String, Number> createTrendLineChart(List<Map<String, Object>> assessments) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("Assessment Date");
        yAxis.setLabel("Score (%)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Performance Trend");
        lineChart.setAnimated(true);
        lineChart.setCreateSymbols(true);
        
        XYChart.Series<String, Number> formalSeries = new XYChart.Series<>();
        formalSeries.setName("Formal Tests");
        
        XYChart.Series<String, Number> practiceSeries = new XYChart.Series<>();
        practiceSeries.setName("Practice Tests");
        
        for (Map<String, Object> assessment : assessments) {
            String date = (String) assessment.get("date");
            Double percentage = (Double) assessment.get("percentage");
            String type = (String) assessment.get("type");
            
            if ("FORMAL".equalsIgnoreCase(type)) {
                formalSeries.getData().add(new XYChart.Data<>(date, percentage));
            } else if ("PRACTICE".equalsIgnoreCase(type)) {
                practiceSeries.getData().add(new XYChart.Data<>(date, percentage));
            }
        }
        
        if (!formalSeries.getData().isEmpty()) {
            lineChart.getData().add(formalSeries);
        }
        
        if (!practiceSeries.getData().isEmpty()) {
            lineChart.getData().add(practiceSeries);
        }
        
        // Add custom styling
        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            if (series.getName().equals("Formal Tests")) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-stroke: #3498db; -fx-stroke-width: 2px;");
                }
            } else if (series.getName().equals("Practice Tests")) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-stroke: #9b59b6; -fx-stroke-width: 2px;");
                }
            }
        }
        
        return lineChart;
    }
    
    /**
     * Creates a stacked bar chart for comparing multiple performance metrics.
     *
     * @param data Map containing the data for the chart
     * @param title The chart title
     * @param xAxisLabel The x-axis label
     * @param yAxisLabel The y-axis label
     * @return A configured StackedBarChart
     */
    public static StackedBarChart<String, Number> createStackedBarChart(
            Map<String, Map<String, Double>> data, 
            String title, 
            String xAxisLabel, 
            String yAxisLabel) {
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
        
        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle(title);
        
        // Create a series for each metric
        if (!data.isEmpty()) {
            Map<String, Double> firstEntry = data.values().iterator().next();
            
            for (String metricName : firstEntry.keySet()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(metricName);
                
                for (Map.Entry<String, Map<String, Double>> entry : data.entrySet()) {
                    String category = entry.getKey();
                    Double value = entry.getValue().get(metricName);
                    if (value != null) {
                        series.getData().add(new XYChart.Data<>(category, value));
                    }
                }
                
                stackedBarChart.getData().add(series);
            }
        }
        
        return stackedBarChart;
    }
    
    /**
     * Creates a bubble chart for comparing multiple dimensions of data.
     *
     * @param data List of data points with x, y, and size values
     * @param title The chart title
     * @param xAxisLabel The x-axis label
     * @param yAxisLabel The y-axis label
     * @return A configured BubbleChart
     */
    public static BubbleChart<Number, Number> createBubbleChart(
            List<Map<String, Object>> data,
            String title,
            String xAxisLabel,
            String yAxisLabel) {
        
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
        
        BubbleChart<Number, Number> bubbleChart = new BubbleChart<>(xAxis, yAxis);
        bubbleChart.setTitle(title);
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Performance Data");
        
        for (Map<String, Object> point : data) {
            Double x = (Double) point.get("x");
            Double y = (Double) point.get("y");
            Double size = (Double) point.get("size");
            String name = (String) point.get("name");
            
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(x, y, size);
            dataPoint.setExtraValue(name); // Store name for tooltip
            series.getData().add(dataPoint);
        }
        
        bubbleChart.getData().add(series);
        
        // Add tooltips and styling
        for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
            String name = (String) dataPoint.getExtraValue();
            
            dataPoint.getNode().setOnMouseEntered(event -> {
                dataPoint.getNode().setStyle("-fx-opacity: 0.8;");
                // In a real implementation, you would add a tooltip here
            });
            
            dataPoint.getNode().setOnMouseExited(event -> {
                dataPoint.getNode().setStyle("-fx-opacity: 1.0;");
            });
        }
        
        return bubbleChart;
    }
    
    /**
     * Creates a radar chart for comparing multiple metrics across categories.
     * Note: This is a placeholder as JavaFX doesn't have a built-in radar chart.
     * In a real implementation, you would use a third-party library or create a custom chart.
     *
     * @param data Map of categories to metric values
     * @param title The chart title
     * @return A placeholder Node (in a real implementation, this would return a radar chart)
     */
    public static Node createRadarChart(Map<String, Map<String, Double>> data, String title) {
        // Placeholder - in a real implementation, you would use a third-party library
        // or create a custom radar chart
        Text placeholder = new Text("Radar Chart: " + title + " (Not implemented)");
        placeholder.setFont(Font.font("Arial", 14));
        return placeholder;
    }
    
    /**
     * Creates a line chart for trend analysis with a specific data format.
     *
     * @param title The chart title
     * @param xAxisLabel The x-axis label
     * @param yAxisLabel The y-axis label
     * @param data List of data points
     * @param xField The field name for x-axis values
     * @param yField The field name for y-axis values
     * @return A configured LineChart
     */
    public static LineChart<String, Number> createLineChart(
            String title,
            String xAxisLabel,
            String yAxisLabel,
            List<Map<String, Object>> data,
            String xField,
            String yField) {
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        lineChart.setAnimated(true);
        lineChart.setCreateSymbols(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Performance");
        
        for (Map<String, Object> point : data) {
            String x = String.valueOf(point.get(xField));
            Number y = (Number) point.get(yField);
            series.getData().add(new XYChart.Data<>(x, y));
        }
        
        lineChart.getData().add(series);
        
        // Add custom styling
        for (XYChart.Data<String, Number> data1 : series.getData()) {
            Node node = data1.getNode();
            double value = data1.getYValue().doubleValue();
            
            // Color points based on score
            if (value >= 80) {
                node.setStyle("-fx-background-color: #2ecc71;"); // Green for high scores
            } else if (value >= 60) {
                node.setStyle("-fx-background-color: #f39c12;"); // Orange for medium scores
            } else {
                node.setStyle("-fx-background-color: #e74c3c;"); // Red for low scores
            }
        }
        
        return lineChart;
    }
    
    /**
     * Creates a pie chart for showing proportions.
     *
     * @param title The chart title
     * @param data Map of slice names to values
     * @return A configured PieChart
     */
    public static PieChart createPieChart(String title, Map<String, Double> data) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            pieChartData.add(new PieChart.Data(
                    entry.getKey() + " (" + String.format("%.1f", entry.getValue()) + "%)", 
                    entry.getValue()));
        }
        
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle(title);
        chart.setLegendSide(Side.RIGHT);
        chart.setLabelsVisible(true);
        chart.setAnimated(true);
        
        // Add custom styling
        int colorIndex = 0;
        String[] colors = {"#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6"};
        
        for (PieChart.Data slice : chart.getData()) {
            String color = colors[colorIndex % colors.length];
            slice.getNode().setStyle("-fx-pie-color: " + color + ";");
            colorIndex++;
        }
        
        return chart;
    }
}
