import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class courseListingController {

    @FXML
    private TableView<CourseListing> courseListingTable;
    @FXML
    private TableColumn<CourseListing, Integer> courseIdColumn;
    @FXML
    private TableColumn<CourseListing, String> courseNameColumn;
    @FXML
    private TableColumn<CourseListing, String> semesterColumn;
    @FXML
    private TableColumn<CourseListing, Integer> creditColumn;
    @FXML
    private TableColumn<CourseListing, String> statusColumn;

    private int loggedInUserId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void initialize() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Fetch and display courses for the logged-in user
        fetchAndDisplayCourses();
    }

    private void fetchAndDisplayCourses() {
        courseListingTable.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COURSE.CourseId, COURSE.Course_name, COURSE.Semester, COURSE.Credits," +
                    " STUDENT_COURSE.Status " +
                    "FROM STUDENT_COURSE " +
                    "INNER JOIN COURSE ON STUDENT_COURSE.CourseId = COURSE.CourseId " +
                    "WHERE STUDENT_COURSE.UserId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, loggedInUserId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int courseId = rs.getInt("CourseId");
                    String courseName = rs.getString("Course_name");
                    String semester = rs.getString("Semester");
                    int credits = rs.getInt("Credits");
                    String status = rs.getString("Status");
                    courseListingTable.getItems().add(new CourseListing(courseId, courseName, semester, credits, status));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class CourseListing {
        private final int courseId;
        private final String courseName;
        private final String semester;
        private final int credits;
        private final String status;

        public CourseListing(int courseId, String courseName, String semester, int credits, String status) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.semester = semester; // Corrected assignment
            this.credits = credits;
            this.status = status; // Corrected assignment
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

        public String getStatus() {
            return status;
        }
    }
}
