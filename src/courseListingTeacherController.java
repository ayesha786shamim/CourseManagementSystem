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

public class courseListingTeacherController {

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

    private int loggedInUserId;

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        loadCourses();
    }

    @FXML
    private void initialize() {
        // Initialize the columns in the table
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        // Load courses for the logged-in teacher
        loadCourses();
    }

    private void loadCourses() {
        if (loggedInUserId == 0) {
            return;
        }

        List<Course> courses = new ArrayList<>();

        String query = "SELECT c.CourseId, c.Course_name, c.Semester, c.Credits " +
                "FROM COURSE c " +
                "JOIN TEACHER_COURSE tc ON c.CourseId = tc.CourseId " +
                "WHERE tc.UserId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("CourseId");
                String courseName = rs.getString("Course_name");
                String semester = rs.getString("Semester");
                int credits = rs.getInt("Credits");

                Course course = new Course(courseId, courseName, semester, credits);
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        courseListingTable.getItems().setAll(courses);
    }

    public static class Course {
        private int courseId;
        private String courseName;
        private String semester;
        private int credits;

        public Course(int courseId, String courseName, String semester, int credits) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.semester = semester;
            this.credits = credits;
        }

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getSemester() {
            return semester;
        }

        public void setSemester(String semester) {
            this.semester = semester;
        }

        public int getCredits() {
            return credits;
        }

        public void setCredits(int credits) {
            this.credits = credits;
        }
    }
}
