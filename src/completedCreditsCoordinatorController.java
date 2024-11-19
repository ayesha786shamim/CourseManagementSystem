import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class completedCreditsCoordinatorController {

    @FXML
    private TextField creditHoursField;

    @FXML
    private TextField userIdField;

    private int loggedInUserId; // ID of the logged-in coordinator

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    @FXML
    private void loadCreditHours() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT SUM(c.Credits) AS TotalCredits " +
                     "FROM STUDENT_COURSE sc JOIN COURSE c ON sc.CourseId = c.CourseId WHERE UserId = ?")) {

            int userId = Integer.parseInt(userIdField.getText());
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalCredits = rs.getInt("TotalCredits");
                creditHoursField.setText(String.valueOf(totalCredits));
            } else {
                showAlert("User has no registered courses.");
            }

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid User ID.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("An error occurred while retrieving data.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onEnterButtonClicked(ActionEvent actionEvent) {
        loadCreditHours();
    }
}
