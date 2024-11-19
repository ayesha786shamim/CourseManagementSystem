import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class courseListingCoordinatorController {

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

    public void initialize() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        loadCourseData();
    }

    private void loadCourseData() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM COURSE")) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("CourseId");
                String courseName = rs.getString("Course_name");
                String semester = rs.getString("Semester");
                int credits = rs.getInt("Credits");

                courseListingTable.getItems().add(new Course(courseId, courseName, semester, credits));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

