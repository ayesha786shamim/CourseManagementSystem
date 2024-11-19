import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class batchwiseUnregisteredStudentController {

    @FXML
    private ChoiceBox<String> choiceBoxBatch;
    @FXML
    private ChoiceBox<String> choiceBoxSemester;
    @FXML
    private TableView<Student> tableViewWarnings;
    @FXML
    private TableColumn<Student, Integer> columnUserId;
    @FXML
    private TableColumn<Student, String> columnStudentName;
    @FXML
    private TableColumn<Student, String> columnCourseName;
    @FXML
    private Label noDataLabel;


    private void setupTableColumns() {
        columnUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        columnStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        columnCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
    }


    private Connection connection;
    private int coordinatorDepartmentId;
    private int loggedInUserId;

    public void initialize() {
        connection = DatabaseConnection.getConnection();
        loadCoordinatorDepartment();
        setupTableColumns();
    }

    private void loadCoordinatorDepartment() {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT departmentId FROM [USER] WHERE userId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                coordinatorDepartmentId = rs.getInt("departmentId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEnterButtonClicked(ActionEvent event) {
        String selectedBatch = choiceBoxBatch.getValue();
        String selectedSemester = choiceBoxSemester.getValue();

        if (selectedBatch == null || selectedSemester == null) {
            // Handle error
            return;
        }

        List<Student> unregisteredStudents = getUnregisteredStudents(selectedBatch, selectedSemester);
        if (unregisteredStudents.isEmpty()) {
            tableViewWarnings.setVisible(false);
            noDataLabel.setVisible(true);
        } else {
            tableViewWarnings.setItems(FXCollections.observableArrayList(unregisteredStudents));
            tableViewWarnings.setVisible(true);
            noDataLabel.setVisible(false);
        }
    }


    private List<Student> getUnregisteredStudents(String batch, String semester) {
        List<Student> unregisteredStudents = new ArrayList<>();

        try {
            String query = "SELECT u.userId, u.First_name, u.Last_name, c.Course_name " +
                    "FROM [USER] u " +
                    "JOIN BATCH b ON u.userId = b.UserId " +
                    "JOIN COURSE c ON c.departmentId = u.departmentId " +
                    "LEFT JOIN STUDENT_COURSE sc ON u.userId = sc.UserId AND c.CourseId = sc.CourseId " +
                    "WHERE b.Batch = ? AND c.Semester = ? AND sc.CourseId IS NULL AND u.departmentId = ?";

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, batch);
            pstmt.setString(2, semester);
            pstmt.setInt(3, coordinatorDepartmentId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("userId");
                String studentName = rs.getString("First_name") + " " + rs.getString("Last_name");
                String courseName = rs.getString("Course_name");

                unregisteredStudents.add(new Student(userId, studentName, courseName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unregisteredStudents;
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        loadCoordinatorDepartment();
    }

    // Inner class to represent a Student
    public static class Student {
        private final int userId;
        private final String studentName;
        private final String courseName;

        public Student(int userId, String studentName, String courseName) {
            this.userId = userId;
            this.studentName = studentName;
            this.courseName = courseName;
        }

        public int getUserId() {
            return userId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getCourseName() {
            return courseName;
        }
    }
}
