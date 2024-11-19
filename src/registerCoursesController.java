import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class registerCoursesController {

    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, Integer> courseIdColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, Integer> creditsColumn;
    @FXML
    private TableColumn<Course, Boolean> selectColumn;
    @FXML
    private ChoiceBox<String> semesterChoiceBox;
    @FXML
    private Button submitButton;

    private int loggedInUserId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void initialize() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        // Initialize the select column with a button
        selectColumn.setCellFactory(col -> new TableCell<Course, Boolean>() {
            private final Button selectButton = new Button("Select");

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Course course = getTableView().getItems().get(getIndex());
                    selectButton.setText(course.isSelected() ? "Deselect" : "Select");
                    selectButton.setOnAction(event -> {
                        course.setSelected(!course.isSelected());
                        selectButton.setText(course.isSelected() ? "Deselect" : "Select");
                    });
                    setGraphic(selectButton);
                }
            }
        });

        // Populate semester choice box
        ObservableList<String> semesters = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8");
        semesterChoiceBox.setItems(semesters);
    }

    public void fetchAndDisplayCourses(String selectedSemester) {
        courseTable.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM COURSE WHERE Semester = ?")) {
            pstmt.setString(1, selectedSemester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("CourseId");
                String courseName = rs.getString("Course_name");
                int credits = rs.getInt("Credits");
                courseTable.getItems().add(new Course(courseId, courseName, credits, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSubmit() {
        String selectedSemester = semesterChoiceBox.getValue();
        if (selectedSemester != null) {
            fetchAndDisplayCourses(selectedSemester);
        } else {
            // Handle error - No semester selected
        }
    }

    @FXML
    public void registerCourses() {
        String selectedSemester = semesterChoiceBox.getValue();
        ObservableList<Course> selectedCourses = courseTable.getItems().filtered(Course::isSelected);

        // Check if the selected semester is the next one
        if (isNextSemester(selectedSemester)) {
            // Insert selected courses into the STUDENT_COURSE table
            try (Connection conn = DatabaseConnection.getConnection()) {
                for (Course course : selectedCourses) {
                    // Check if the user is already registered for the course
                    if (isUserAlreadyRegistered(conn, loggedInUserId, course.getCourseId())) {
                        showAlert("Error", "Registration Error", "You are already registered for the course: " + course.getCourseName(), Alert.AlertType.ERROR);
                        continue;
                    }

                    // Insert the course registration
                    String sql = "INSERT INTO STUDENT_COURSE (UserId, CourseId, Registration_date, Status) " +
                                 "VALUES (?, ?, GETDATE(), 'Registered')";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, loggedInUserId);
                        pstmt.setInt(2, course.getCourseId());
                        pstmt.executeUpdate();
                    }
                }
                System.out.println("Courses registered successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle error
            }
        } else {
            showAlert("Error", "Error", "You can only register for courses in the next semester.", Alert.AlertType.ERROR);
        }
    }

    private boolean isNextSemester(String selectedSemester) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT MAX(Semester) AS MaxSemester FROM STUDENT_COURSE " +
                     "JOIN COURSE ON STUDENT_COURSE.CourseId = COURSE.CourseId " +
                     "WHERE UserId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int currentSemester = rs.getInt("MaxSemester");
                int selected = Integer.parseInt(selectedSemester);
                return currentSemester == selected - 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // If unable to determine, assume it's not the next semester
    }


    private boolean isUserAlreadyRegistered(Connection conn, int userId, int courseId) throws SQLException {
        String sql = "SELECT * FROM STUDENT_COURSE WHERE UserId = ? AND CourseId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // If a row is returned, the user is already registered
        }
    }

    private void showAlert(String title, String headerText, String contentText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static class Course {
        private final SimpleIntegerProperty courseId;
        private final SimpleStringProperty courseName;
        private final SimpleIntegerProperty credits;
        private final SimpleBooleanProperty selected;

        public Course(int courseId, String courseName, int credits, boolean selected) {
            this.courseId = new SimpleIntegerProperty(courseId);
            this.courseName = new SimpleStringProperty(courseName);
            this.credits = new SimpleIntegerProperty(credits);
            this.selected = new SimpleBooleanProperty(selected);
        }

        public int getCourseId() {
            return courseId.get();
        }

        public String getCourseName() {
            return courseName.get();
        }

        public int getCredits() {
            return credits.get();
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }
    }
}
