import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class allocateCourseController {

    @FXML
    private Button submitButton;
    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TextField userIdField;
    @FXML
    private Button registerButton;
    @FXML
    private TableColumn<Course, Integer> courseIdColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, Integer> creditsColumn;
    @FXML
    private TableColumn<Course, Boolean> selectColumn;

    private int loggedInUserId; // Assuming you have a way to set this value
    private int enteredUserId; // The user ID entered by the coordinator

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void initialize() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        // Initialize the select column with a button
        selectColumn.setCellFactory(col -> new TableCell<Course, Boolean>() {
            private final Button selectButton = new Button();

            {
                selectButton.setOnAction(event -> {
                    Course course = getTableView().getItems().get(getIndex());
                    course.setSelected(!course.isSelected());
                    selectButton.setText(course.isSelected() ? "Deselect" : "Select");
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Course course = getTableView().getItems().get(getIndex());
                    selectButton.setText(course.isSelected() ? "Deselect" : "Select");
                    setGraphic(selectButton);
                }
            }
        });

        courseTable.getColumns().clear();
        courseTable.getColumns().addAll(courseIdColumn, courseNameColumn, creditsColumn, selectColumn);

        registerButton.setOnAction(event -> registerCourses());
        submitButton.setOnAction(event -> handleSubmit());
    }

    private Pair<Integer, String> getDepartmentIdAndRole(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT u.departmentId, r.Role_name " +
                             "FROM [USER] u " +
                             "JOIN USER_ROLE ur ON u.userId = ur.UserId " +
                             "JOIN ROLE r ON ur.RoleId = r.roleId " +
                             "WHERE u.userId = ?")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("Role_name");
                int departmentId = rs.getInt("departmentId");
                return new Pair<>(departmentId, role);
            } else {
                showAlert("Error", "User Not Found", "The user entered could not be found.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
        }
        return new Pair<>(-1, ""); // Return a default value or handle the case when departmentId is not found
    }

    private void fetchAndDisplayCourses(int departmentId) {
        courseTable.getItems().clear();
        if (departmentId != -1) {
            String sql = "SELECT * FROM COURSE WHERE departmentId = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, departmentId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int courseId = rs.getInt("CourseId");
                    String courseName = rs.getString("Course_name");
                    int credits = rs.getInt("Credits");
                    courseTable.getItems().add(new Course(courseId, courseName, credits, false));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void registerCourses() {
        ObservableList<Course> selectedCourses = courseTable.getItems().filtered(Course::isSelected);
        List<String> alreadyAllocatedCourses = new ArrayList<>();
        List<String> successfullyAllocatedCourses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (Course course : selectedCourses) {
                // Check if the course is already allocated to the teacher
                String checkSql = "SELECT COUNT(*) FROM TEACHER_COURSE WHERE UserId = ? AND CourseId = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, enteredUserId);
                    checkStmt.setInt(2, course.getCourseId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Course is already allocated, add to already allocated list
                        alreadyAllocatedCourses.add(course.getCourseName());
                        continue;
                    }
                }

                String sql = "INSERT INTO TEACHER_COURSE (UserId, CourseId) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, enteredUserId);
                    pstmt.setInt(2, course.getCourseId());
                    pstmt.executeUpdate();
                    successfullyAllocatedCourses.add(course.getCourseName());
                }
            }

            // Show alert based on the results
            if (!successfullyAllocatedCourses.isEmpty()) {
                showAlert("Success", "Courses Allocated", "Courses have been allocated successfully: " + String.join(", ", successfullyAllocatedCourses), Alert.AlertType.INFORMATION);
            }

            if (!alreadyAllocatedCourses.isEmpty()) {
                showAlert("Warning", "Course Already Allocated", "The following courses were already allocated: " + String.join(", ", alreadyAllocatedCourses), Alert.AlertType.WARNING);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSubmit() {
        String userId = userIdField.getText().trim();

        if (userId.isEmpty()) {
            showAlert("Error", "Input Error", "Please enter the teacher ID.", Alert.AlertType.ERROR);
            return;
        }

        try {
            int userIntId = Integer.parseInt(userId);
            Pair<Integer, String> departmentRole = getDepartmentIdAndRole(userIntId);
            int departmentId = departmentRole.getKey();
            String role = departmentRole.getValue();

            Pair<Integer, String> coordinatorDepartmentRole = getDepartmentIdAndRole(loggedInUserId);
            int coordinatorDepartmentId = coordinatorDepartmentRole.getKey();

            // Debugging prints
            System.out.println("User ID: " + userIntId);
            System.out.println("User Department ID: " + departmentId);
            System.out.println("User Role: " + role);
            System.out.println("Coordinator Department ID: " + coordinatorDepartmentId);

            if ("teacher".equalsIgnoreCase(role)) {
                if (departmentId == coordinatorDepartmentId) {
                    enteredUserId = userIntId; // Store the entered user ID
                    fetchAndDisplayCourses(departmentId);
                } else {
                    showAlert("Error", "Department Mismatch", "The entered user ID does not belong to a teacher in your department.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Invalid Role", "The user entered is not a teacher.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Input Error", "Please enter a valid user ID.", Alert.AlertType.ERROR);
        }
    }

    public static class Course {
        private final Integer courseId;
        private final String courseName;
        private final Integer credits;
        private final BooleanProperty selected;

        public Course(Integer courseId, String courseName, Integer credits, boolean selected) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.credits = credits;
            this.selected = new SimpleBooleanProperty(selected);
        }

        public Integer getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public Integer getCredits() {
            return credits;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }
    }
}
