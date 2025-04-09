package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Lecturer;
import book.javafx.kenyattacatsystem.models.Student;
import book.javafx.kenyattacatsystem.models.Unit;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for unit-related operations.
 * Handles unit creation, lecturer assignment, and student enrollment.
 */
public class UnitService {
    private static final Logger LOGGER = Logger.getLogger(UnitService.class.getName());

    /**
     * Creates a new unit in the database.
     *
     * @param unit The unit to create
     * @return True if the unit was created successfully, false otherwise
     */
    public static boolean createUnit(Unit unit) {
        String query = "INSERT INTO units (unit_code, unit_name, department, credit_hours, description, lecturer_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unit.getUnitCode());
            stmt.setString(2, unit.getUnitName());
            stmt.setString(3, unit.getDepartment());
            stmt.setInt(4, unit.getCreditHours());
            stmt.setString(5, unit.getDescription());
            stmt.setString(6, unit.getLecturerId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating unit", e);
            return false;
        }
    }

    /**
     * Updates an existing unit in the database.
     *
     * @param unit The unit to update
     * @return True if the unit was updated successfully, false otherwise
     */
    public static boolean updateUnit(Unit unit) {
        String query = "UPDATE units SET unit_name = ?, department = ?, credit_hours = ?, "
                + "description = ?, lecturer_id = ? WHERE unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unit.getUnitName());
            stmt.setString(2, unit.getDepartment());
            stmt.setInt(3, unit.getCreditHours());
            stmt.setString(4, unit.getDescription());
            stmt.setString(5, unit.getLecturerId());
            stmt.setString(6, unit.getUnitCode());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating unit", e);
            return false;
        }
    }

    /**
     * Deletes a unit from the database.
     *
     * @param unitCode The unit code
     * @return True if the unit was deleted successfully, false otherwise
     */
    public static boolean deleteUnit(String unitCode) {
        String query = "DELETE FROM units WHERE unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting unit", e);
            return false;
        }
    }

    /**
     * Deletes a unit from the database.
     *
     * @param unit The unit to delete
     * @return True if the unit was deleted successfully, false otherwise
     */
    public static boolean deleteUnit(Unit unit) {
        return deleteUnit(unit.getUnitCode());
    }

    /**
     * Gets a unit by its code.
     *
     * @param unitCode The unit code
     * @return The unit, or null if not found
     */
    public static Unit getUnitByCode(String unitCode) {
        String query = "SELECT * FROM units WHERE unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Unit unit = new Unit();
                unit.setUnitCode(rs.getString("unit_code"));
                unit.setUnitName(rs.getString("unit_name"));
                unit.setDepartment(rs.getString("department"));
                unit.setCreditHours(rs.getInt("credit_hours"));
                unit.setDescription(rs.getString("description"));
                unit.setLecturerId(rs.getString("lecturer_id"));

                // Load enrolled students
                loadEnrolledStudents(unit);

                return unit;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting unit by code", e);
        }

        return null;
    }

    /**
     * Gets a unit's name based on its unit code.
     *
     * @param unitCode The unit code
     * @return The unit name, or null if not found
     */
    public static String getUnitName(String unitCode) {
        Unit unit = getUnitByCode(unitCode);
        return unit != null ? unit.getUnitName() : null;
    }

    /**
     * Gets all units.
     *
     * @return A list of all units
     */
    public static List<Unit> getAllUnits() {
        List<Unit> units = new ArrayList<>();
        String query = "SELECT * FROM units ORDER BY unit_code";

        try {
            ResultSet rs = DatabaseUtil.executeQuery(query);

            while (rs.next()) {
                Unit unit = new Unit();
                unit.setUnitCode(rs.getString("unit_code"));
                unit.setUnitName(rs.getString("unit_name"));
                unit.setDepartment(rs.getString("department"));
                unit.setCreditHours(rs.getInt("credit_hours"));
                unit.setDescription(rs.getString("description"));
                unit.setLecturerId(rs.getString("lecturer_id"));

                // Load enrolled students
                loadEnrolledStudents(unit);

                units.add(unit);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all units", e);
        }

        return units;
    }

    /**
     * Gets units by department.
     *
     * @param department The department
     * @return A list of units in the department
     */
    public static List<Unit> getUnitsByDepartment(String department) {
        List<Unit> units = new ArrayList<>();
        String query = "SELECT * FROM units WHERE department = ? ORDER BY unit_code";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Unit unit = new Unit();
                unit.setUnitCode(rs.getString("unit_code"));
                unit.setUnitName(rs.getString("unit_name"));
                unit.setDepartment(rs.getString("department"));
                unit.setCreditHours(rs.getInt("credit_hours"));
                unit.setDescription(rs.getString("description"));
                unit.setLecturerId(rs.getString("lecturer_id"));

                // Load enrolled students
                loadEnrolledStudents(unit);

                units.add(unit);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting units by department", e);
        }

        return units;
    }

    /**
     * Gets units taught by a lecturer.
     *
     * @param lecturerId The lecturer ID
     * @return A list of units taught by the lecturer
     */
    public static List<Unit> getUnitsByLecturer(String lecturerId) {
        List<Unit> units = new ArrayList<>();
        String query = "SELECT * FROM units WHERE lecturer_id = ? ORDER BY unit_code";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, lecturerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Unit unit = new Unit();
                unit.setUnitCode(rs.getString("unit_code"));
                unit.setUnitName(rs.getString("unit_name"));
                unit.setDepartment(rs.getString("department"));
                unit.setCreditHours(rs.getInt("credit_hours"));
                unit.setDescription(rs.getString("description"));
                unit.setLecturerId(rs.getString("lecturer_id"));

                // Load enrolled students
                loadEnrolledStudents(unit);

                units.add(unit);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting units by lecturer", e);
        }

        return units;
    }

    /**
     * Loads enrolled students for a unit.
     *
     * @param unit The unit
     */
    private static void loadEnrolledStudents(Unit unit) {
        String query = "SELECT student_id FROM enrollments WHERE unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unit.getUnitCode());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                unit.addEnrolledStudent(rs.getString("student_id"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading enrolled students for unit", e);
        }
    }

    /**
     * Gets the count of students enrolled in a unit.
     *
     * @param unitCode The unit code
     * @return The number of enrolled students
     */
    public static int getEnrolledStudentsCount(String unitCode) {
        String query = "SELECT COUNT(*) FROM enrollments WHERE unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrolled students count", e);
        }

        return 0;
    }

    /**
     * Assigns a lecturer to a unit.
     *
     * @param unitCode   The unit code
     * @param lecturerId The lecturer ID
     * @return True if the assignment was successful, false otherwise
     */
    public static boolean assignLecturerToUnit(String unitCode, String lecturerId) {
        String query = "UPDATE units SET lecturer_id = ? WHERE unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, lecturerId);
            stmt.setString(2, unitCode);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update lecturer's taught units list
                Lecturer lecturer = UserService.getLecturerById(lecturerId);
                if (lecturer != null) {
                    lecturer.addTaughtUnit(unitCode);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning lecturer to unit", e);
        }

        return false;
    }

    /**
     * Assigns a lecturer to a unit.
     *
     * @param unit The unit to assign a lecturer to
     */
    public static void assignLecturerToUnit(Unit unit) {
        try {
            // Get list of lecturers
            List<Lecturer> lecturers = new ArrayList<>();
            String query = "SELECT l.staff_id, u.full_name, u.email " +
                    "FROM lecturers l JOIN users u ON l.user_id = u.user_id " +
                    "ORDER BY u.full_name";
            
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lecturer lecturer = new Lecturer();
                    lecturer.setStaffId(rs.getString("staff_id"));
                    lecturer.setFullName(rs.getString("full_name"));
                    lecturer.setEmail(rs.getString("email"));
                    lecturers.add(lecturer);
                }
            }
            
            // Create a dialog
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Assign Lecturer");
            dialog.setHeaderText("Assign Lecturer to Unit " + unit.getUnitCode());
            
            // Create the grid pane
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Create lecturer combo box
            ComboBox<Lecturer> lecturerComboBox = new ComboBox<>();
            lecturerComboBox.getItems().addAll(lecturers);
            lecturerComboBox.setPromptText("Select Lecturer");
            
            // Set current lecturer if any
            if (unit.getLecturerId() != null) {
                for (Lecturer lecturer : lecturers) {
                    if (lecturer.getStaffId().equals(unit.getLecturerId())) {
                        lecturerComboBox.setValue(lecturer);
                        break;
                    }
                }
            }
            
            // Add fields to grid
            grid.add(new Label("Lecturer:"), 0, 0);
            grid.add(lecturerComboBox, 1, 0);
            
            dialog.getDialogPane().setContent(grid);
            
            // Add buttons
            ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);
            
            // Enable/Disable assign button depending on whether a lecturer is selected
            Node assignButton = dialog.getDialogPane().lookupButton(assignButtonType);
            assignButton.setDisable(true);
            
            lecturerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> 
                assignButton.setDisable(newValue == null));
            
            // Convert result to lecturer ID
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    Lecturer selectedLecturer = lecturerComboBox.getValue();
                    return selectedLecturer != null ? selectedLecturer.getStaffId() : null;
                }
                return null;
            });
            
            // Show dialog and handle result
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(lecturerId -> {
                try {
                    // Update unit in database
                    String updateQuery = "UPDATE units SET lecturer_id = ? WHERE unit_code = ?";
                    try (PreparedStatement stmt = DatabaseUtil.prepareStatement(updateQuery)) {
                        stmt.setString(1, lecturerId);
                        stmt.setString(2, unit.getUnitCode());
                        stmt.executeUpdate();
                    }
                    
                    // Update unit object
                    unit.setLecturerId(lecturerId);
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Lecturer Assigned");
                    alert.setContentText("Lecturer has been assigned to unit " + unit.getUnitCode() + " successfully.");
                    alert.showAndWait();
                    
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error assigning lecturer to unit", e);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Database Error");
                    alert.setContentText("Failed to assign lecturer: " + e.getMessage());
                    alert.showAndWait();
                }
            });
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading lecturers", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("Failed to load lecturers: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Enrolls a student in a unit.
     * Checks if the student has fee clearance if required by system settings.
     * Also checks if the student has reached the maximum number of units per semester.
     *
     * @param studentId       The student ID
     * @param unitCode        The unit code
     * @param hasFeeClearance Whether the student has fee clearance
     * @return True if enrollment was successful, false otherwise
     */
    public static boolean enrollStudentInUnit(String studentId, String unitCode, boolean hasFeeClearance) {
        // Check if fee clearance is required
        if (SystemSettingsService.isFeeClearanceRequired() && !hasFeeClearance) {
            LOGGER.warning("Student " + studentId + " does not have fee clearance for unit " + unitCode);
            return false;
        }

        // Check if student has reached the maximum number of units per semester
        int maxUnits = SystemSettingsService.getMaxUnitsPerSemester();
        int currentUnits = getStudentEnrolledUnitsCount(studentId);

        if (currentUnits >= maxUnits) {
            LOGGER.warning("Student " + studentId + " has reached the maximum number of units per semester");
            return false;
        }

        // Check if student is already enrolled in the unit
        if (isStudentEnrolledInUnit(studentId, unitCode)) {
            LOGGER.warning("Student " + studentId + " is already enrolled in unit " + unitCode);
            return false;
        }

        // Enroll the student
        String query = "INSERT INTO enrollments (student_id, unit_code) VALUES (?, ?)";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, unitCode);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update student's enrolled units list
                Student student = UserService.getStudentById(studentId);
                if (student != null) {
                    student.addEnrolledUnit(unitCode);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error enrolling student in unit", e);
        }

        return false;
    }

    /**
     * Enrolls a student in a unit.
     * Simplified version that assumes fee clearance is true.
     *
     * @param studentId The student ID
     * @param unitCode  The unit code
     * @return True if enrollment was successful, false otherwise
     */
    public static boolean enrollStudentInUnit(String studentId, String unitCode) {
        return enrollStudentInUnit(studentId, unitCode, true);
    }

    /**
     * Unenrolls a student from a unit.
     *
     * @param studentId The student ID
     * @param unitCode  The unit code
     * @return True if unenrollment was successful, false otherwise
     */
    public static boolean unenrollStudentFromUnit(String studentId, String unitCode) {
        String query = "DELETE FROM enrollments WHERE student_id = ? AND unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, unitCode);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update student's enrolled units list
                Student student = UserService.getStudentById(studentId);
                if (student != null) {
                    student.unenrollFromUnit(unitCode);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error unenrolling student from unit", e);
        }

        return false;
    }

    /**
     * Checks if a student is enrolled in a unit.
     *
     * @param studentId The student ID
     * @param unitCode  The unit code
     * @return True if the student is enrolled, false otherwise
     */
    public static boolean isStudentEnrolledInUnit(String studentId, String unitCode) {
        String query = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, unitCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if student is enrolled in unit", e);
        }

        return false;
    }

    /**
     * Gets the number of units a student is enrolled in.
     *
     * @param studentId The student ID
     * @return The number of enrolled units
     */
    public static int getStudentEnrolledUnitsCount(String studentId) {
        String query = "SELECT COUNT(*) FROM enrollments WHERE student_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting student enrolled units count", e);
        }

        return 0;
    }

    /**
     * Gets students enrolled in a unit.
     *
     * @param unitCode The unit code
     * @return A list of enrolled students
     */
    public static List<Student> getEnrolledStudents(String unitCode) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT e.student_id FROM enrollments e WHERE e.unit_code = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                Student student = UserService.getStudentById(studentId);
                if (student != null) {
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrolled students", e);
        }

        return students;
    }
}