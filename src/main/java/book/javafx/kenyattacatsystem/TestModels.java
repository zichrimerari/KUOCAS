package book.javafx.kenyattacatsystem;

import book.javafx.kenyattacatsystem.models.Lecturer;
import book.javafx.kenyattacatsystem.models.Unit;
import book.javafx.kenyattacatsystem.models.Student;
import book.javafx.kenyattacatsystem.models.User;

/**
 * A simple test class to verify that the model classes can be imported correctly.
 */
public class TestModels {
    
    public static void main(String[] args) {
        // Create a lecturer
        Lecturer lecturer = new Lecturer();
        lecturer.setStaffId("L001");
        lecturer.setFullName("John Doe");
        lecturer.setDepartment("Computer Science");
        
        // Create a unit
        Unit unit = new Unit();
        unit.setUnitCode("CS101");
        unit.setUnitName("Introduction to Programming");
        unit.setDepartment("Computer Science");
        
        // Create a student
        Student student = new Student();
        student.setStudentId("S001");
        student.setFullName("Jane Smith");
        
        // Print details
        System.out.println("Lecturer: " + lecturer.getFullName() + " (" + lecturer.getStaffId() + ")");
        System.out.println("Unit: " + unit.getUnitName() + " (" + unit.getUnitCode() + ")");
        System.out.println("Student: " + student.getFullName() + " (" + student.getStudentId() + ")");
    }
}
