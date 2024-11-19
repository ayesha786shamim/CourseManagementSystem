import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class registerCoursesCoordinatorController {

    @FXML
    private ChoiceBox<String> semesterChoiceBox;
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

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void initialize() {
        // Initialize the semester choice box
        semesterChoiceBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8");
        semesterChoiceBox.setValue(""); // Set default value

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
                int departmentId = rs.getInt("departmentId");
                String role = rs.getString("Role_name");
                System.out.println("User ID: " + userId + ", Department ID: " + departmentId + ", Role: " + role); // Debugging
                if ("student".equalsIgnoreCase(role)) {
                    return new Pair<>(departmentId, role);
                } else {
                    showAlert("Error", "Invalid Role", "The user entered is not a student.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "User Not Found", "The user entered could not be found.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
        }
        return new Pair<>(-1, ""); // Return a default value or handle the case when departmentId is not found
    }


    private void fetchAndDisplayCourses(String selectedSemester, int departmentId) {
        courseTable.getItems().clear();
        if (departmentId != -1) {
            String sql = "SELECT * FROM COURSE WHERE Semester = ? AND departmentId = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, selectedSemester);
                pstmt.setInt(2, departmentId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int courseId = rs.getInt("CourseId");
                    String courseName = rs.getString("Course_name");
                    int credits = rs.getInt("Credits");
                    // Include departmentId when creating the Course object
                    courseTable.getItems().add(new Course(courseId, courseName, credits, false, departmentId));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void registerCourses() {
        String selectedSemester = semesterChoiceBox.getValue();
        ObservableList<Course> selectedCourses = courseTable.getItems().filtered(Course::isSelected);

        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = Integer.parseInt(userIdField.getText().trim());
            int currentSemester = getCurrentSemester(conn, userId);
            int coordinatorDepartmentId = getCoordinatorDepartmentId(loggedInUserId);

            System.out.println("Coordinator Department ID: " + coordinatorDepartmentId); // Debugging

            if (!isNextSemester(currentSemester, selectedSemester)) {
                showAlert("Error", "Invalid Semester", "You can only register for the next semester, which is " + (currentSemester + 1) + ".", Alert.AlertType.ERROR);
                return;
            }

            for (Course course : selectedCourses) {
                System.out.println("Course Department ID: " + course.getDepartmentId()); // Debugging
                if (course.getDepartmentId() == coordinatorDepartmentId && !isCourseAlreadyRegistered(conn, userId, course.getCourseId())) {
                    String sql = "INSERT INTO STUDENT_COURSE (UserId, CourseId, Registration_date, Status) VALUES (?, ?, GETDATE(), 'Registered')";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, userId);
                        pstmt.setInt(2, course.getCourseId());
                        pstmt.executeUpdate();
                    }
                } else {
                    showAlert("Error", "Registration Error", "Some courses are either already registered or do not belong to the coordinator's department.", Alert.AlertType.ERROR);
                    return;
                }
            }

            showAlert("Success", "Courses Registered", "Courses have been registered successfully.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
        }
    }


    private int getCurrentSemester(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT MAX(c.Semester) AS LastSemester " +
                "FROM STUDENT_COURSE sc " +
                "JOIN COURSE c ON sc.CourseId = c.CourseId " +
                "WHERE sc.UserId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int lastSemester = rs.getInt("LastSemester");
                return lastSemester; // Return the last registered semester
            } else {
                return 0; // If no semesters registered, return 0
            }
        }
    }

    private boolean isNextSemester(int currentSemester, String selectedSemester) {
        if (selectedSemester.isEmpty()) {
            return false;
        }

        int selectedSemesterInt = Integer.parseInt(selectedSemester);
        return currentSemester == 0 || selectedSemesterInt == currentSemester + 1;
    }

    private boolean isCourseAlreadyRegistered(Connection conn, int studentId, int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT_COURSE WHERE UserId = ? AND CourseId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
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
        String selectedSemester = semesterChoiceBox.getValue();

        if (userId.isEmpty() || selectedSemester.isEmpty()) {
            showAlert("Error", "Input Error", "Please enter the student ID and select a semester.", Alert.AlertType.ERROR);
            return;
        }

        try {
            int userIntId = Integer.parseInt(userId);
            Pair<Integer, String> studentDepartmentRole = getDepartmentIdAndRole(userIntId);
            int studentDepartmentId = studentDepartmentRole.getKey();
            String role = studentDepartmentRole.getValue();

            if ("student".equalsIgnoreCase(role)) {
                int coordinatorDepartmentId = getCoordinatorDepartmentId(loggedInUserId);
                if (studentDepartmentId == coordinatorDepartmentId) {
                    fetchAndDisplayCourses(selectedSemester, coordinatorDepartmentId);
                } else {
                    showAlert("Error", "Department Mismatch", "The student does not belong to the same department as the coordinator.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Role Error", "The entered user ID does not belong to a student.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Input Error", "Please enter a valid user ID.", Alert.AlertType.ERROR);
        }
    }

    private int getCoordinatorDepartmentId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT departmentId FROM [USER] WHERE userId = ?")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("departmentId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
        }
        return -1; // Return a default value or handle the case when departmentId is not found
    }


    public static class Course {
        private final Integer courseId;
        private final String courseName;
        private final Integer credits;
        private final BooleanProperty selected;
        private final Integer departmentId; // New attribute

        public Course(Integer courseId, String courseName, Integer credits, boolean selected, Integer departmentId) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.credits = credits;
            this.selected = new SimpleBooleanProperty(selected);
            this.departmentId = departmentId; // Initialize the new attribute
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

        public Integer getDepartmentId() {
            return departmentId; // Getter for the new attribute
        }
    }

}

