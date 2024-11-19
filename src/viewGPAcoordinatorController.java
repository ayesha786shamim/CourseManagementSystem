import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class viewGPAcoordinatorController {

    @FXML
    private TableView<GPAData> gpaTable;

    @FXML
    private TableColumn<GPAData, String> semesterColumn;

    @FXML
    private TableColumn<GPAData, Double> gpaColumn;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField cgpaField;

    private ObservableList<GPAData> gpaDataList = FXCollections.observableArrayList();

    private int loggedInUserId; // ID of the logged-in coordinator

    @FXML
    public void initialize() {
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        gpaColumn.setCellValueFactory(new PropertyValueFactory<>("gpa"));
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void onEnterButtonClicked() {
        String userId = userIdField.getText();
        if (userId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "User ID cannot be empty.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT departmentId FROM [USER] WHERE UserId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Coordinator not found.");
                return;
            }
            int coordinatorDepartmentId = rs.getInt("departmentId");

            pstmt.setInt(1, Integer.parseInt(userId));
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User ID not found.");
                return;
            }
            int userDepartmentId = rs.getInt("departmentId");

            if (coordinatorDepartmentId != userDepartmentId) {
                showAlert(Alert.AlertType.ERROR, "Error", "User does not belong to your department.");
                return;
            }

            loadGPAData(Integer.parseInt(userId));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGPAData(int userId) {
        String sql = "SELECT c.Semester, m.Marks, c.Credits " +
                "FROM STUDENT_COURSE sc " +
                "JOIN COURSE c ON sc.CourseId = c.CourseId " +
                "JOIN MARKS m ON sc.CourseId = m.CourseId " +
                "WHERE sc.UserId = ? AND m.UserId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            Map<String, Double> semesterGpaMap = new HashMap<>();
            Map<String, Integer> semesterCreditMap = new HashMap<>();

            while (rs.next()) {
                String semester = rs.getString("Semester");
                int marks = rs.getInt("Marks");
                int credits = rs.getInt("Credits");

                double gpa = calculateGPA(marks);

                semesterGpaMap.put(semester, semesterGpaMap.getOrDefault(semester, 0.0) + (gpa * credits));
                semesterCreditMap.put(semester, semesterCreditMap.getOrDefault(semester, 0) + credits);
            }

            double totalGpaSum = 0;
            int totalCredits = 0;

            gpaDataList.clear();
            for (Map.Entry<String, Double> entry : semesterGpaMap.entrySet()) {
                String semester = entry.getKey();
                double gpaSum = entry.getValue();
                int credits = semesterCreditMap.get(semester);

                double gpa = gpaSum / credits;
                totalGpaSum += gpaSum;
                totalCredits += credits;

                gpaDataList.add(new GPAData(semester, gpa));
            }

            gpaTable.setItems(gpaDataList);

            double cgpa = totalGpaSum / totalCredits;
            cgpaField.setText(String.format("%.2f", cgpa));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private double calculateGPA(int marks) {
        if (marks >= 85) return 4.0;
        else if (marks >= 80) return 3.7;
        else if (marks >= 75) return 3.3;
        else if (marks >= 70) return 3.0;
        else if (marks >= 65) return 2.7;
        else if (marks >= 60) return 2.3;
        else if (marks >= 55) return 2.0;
        else if (marks >= 50) return 1.7;
        else return 0.0; // Fail
    }



    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class GPAData {
        private final String semester;
        private final Double gpa;

        public GPAData(String semester, Double gpa) {
            this.semester = semester;
            this.gpa = gpa;
        }

        public String getSemester() {
            return semester;
        }

        public Double getGpa() {
            return gpa;
        }
    }
}
