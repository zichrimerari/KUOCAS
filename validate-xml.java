import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ValidateXML {
    public static void main(String[] args) {
        try {
            File xmlFile = new File("src/main/resources/book/javafx/kenyattacatsystem/views/student-dashboard.fxml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(xmlFile);
            System.out.println("XML file is valid");
        } catch (Exception e) {
            System.out.println("XML file is invalid: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
