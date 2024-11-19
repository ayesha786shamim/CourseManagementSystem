import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class creditHourController {

    private int userId;
    @FXML
    private TextField creditHoursField;

    public creditHourController() {
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCreditHoursField(TextField creditHoursField) {
        this.creditHoursField = creditHoursField;
    }

    public int getTotalCreditHours() {
        int totalCredits = 0;
        String sql = "SELECT SUM(C.Credits) AS TotalCredits " +
                "FROM STUDENT_COURSE SC " +
                "JOIN COURSE C ON SC.CourseId = C.CourseId " +
                "WHERE SC.UserId = ? AND SC.Status = 'Registered'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCredits = rs.getInt("TotalCredits");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalCredits;
    }

    public void displayCompletedCreditHours() {
        System.out.println("Displaying completed credit hours...");
        int totalCredits = getTotalCreditHours();
        creditHoursField.setText(String.valueOf(totalCredits));
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.userId = loggedInUserId;
    }
}
