import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class coursesController {

    private int loggedInUserId;

    @FXML
    private TableView<Course> courseListingTable;
    @FXML
    private TableColumn<Course, Integer> courseIdColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, String> semesterColumn;
    @FXML
    private TableColumn<Course, Integer> creditColumn;

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        fetchAndDisplayCourses();
    }

    private void fetchAndDisplayCourses() {
        List<Course> courses = getCoursesForUser(loggedInUserId);
        populateTable(courses);
    }

    private List<Course> getCoursesForUser(int userId) {
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT * FROM COURSE WHERE departmentId = " +
                             "(SELECT departmentId FROM [USER] WHERE userId = ?)"
             )) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int courseId = rs.getInt("CourseId");
                String courseName = rs.getString("Course_name");
                String semester = rs.getString("Semester");
                int credits = rs.getInt("Credits");
                courses.add(new Course(courseId, courseName, semester, credits));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    private void populateTable(List<Course> courses) {
        ObservableList<Course> courseList = FXCollections.observableArrayList(courses);
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        courseListingTable.setItems(courseList);
    }

    public static class Course {
        private final int courseId;
        private final String courseName;
        private final String semester;
        private final int credits;

        public Course(int courseId, String courseName, String semester, int credits) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.semester = semester;
            this.credits = credits;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getSemester() {
            return semester;
        }

        public int getCredits() {
            return credits;
        }
    }
}
