import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class printTranscriptCoordinatorController {

    @FXML
    private TextField userIdField;

    @FXML
    private Label creditHoursLabel;

    @FXML
    private Button printButton;

    private int loggedInUserId; // Assuming this is the coordinator's user ID

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    private boolean hasEnoughCreditHours(int studentUserId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT SUM(Credits) AS TotalCredits FROM COURSE c JOIN STUDENT_COURSE sc ON c.CourseId = sc.CourseId WHERE sc.UserId = ?"
            );
            statement.setInt(1, studentUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalCredits = resultSet.getInt("TotalCredits");
                return totalCredits >= 140; // Return true if student has 140 or more credit hours
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void checkUserIdAndPrint() {
        int studentUserId;
        try {
            studentUserId = Integer.parseInt(userIdField.getText().trim()); // Assuming user ID is an integer
        } catch (NumberFormatException e) {
            creditHoursLabel.setText("Invalid user ID. Please enter a valid user ID.");
            userIdField.setText(""); // Clear the text field
            printButton.setVisible(false);
            return;
        }

        // Check if the student belongs to the same department as the coordinator
        if (belongsToSameDepartment(studentUserId)) {
            // Check if the student has enough credit hours
            if (hasEnoughCreditHours(studentUserId)) {
                printButton.setVisible(true);
                creditHoursLabel.setText("");
            } else {
                creditHoursLabel.setText("Student does not have enough credit hours.");
                printButton.setVisible(false);
            }
        } else {
            creditHoursLabel.setText("Student does not belong to your department.");
            printButton.setVisible(false);
        }
    }

    @FXML
    private void printTranscript(ActionEvent actionEvent) {
        // Extract student ID from the text field
        int studentUserId;
        try {
            studentUserId = Integer.parseInt(userIdField.getText().trim()); // Assuming user ID is an integer
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid User ID", "Please enter a valid user ID.");
            return;
        }

        // Generate transcript for the entered student ID
        try {
            TranscriptGenerator.generateTranscriptHTML(studentUserId);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("invalid student name")) {
                showErrorAlert("Error generating transcript", "The student's name is invalid or empty. Please check the database.");
            } else {
                showErrorAlert("Error generating transcript", e.getMessage());
            }
        }
    }


    private int getCoordinatorDepartmentId() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT departmentId FROM [USER] WHERE userId = ?"
            );
            statement.setInt(1, loggedInUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("departmentId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if department ID not found or an error occurred
    }

    private boolean belongsToSameDepartment(int studentUserId) {
        int coordinatorDepartmentId = getCoordinatorDepartmentId();
        if (coordinatorDepartmentId == -1) {
            return false; // Unable to get coordinator's department ID, return false
        }

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT departmentId FROM [USER] WHERE userId = ?"
            );
            statement.setInt(1, studentUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int studentDepartmentId = resultSet.getInt("departmentId");
                return studentDepartmentId == coordinatorDepartmentId; // True if student belongs to the same department as the coordinator
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if an error occurred or student's department ID not found
    }




    private void showErrorAlert(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
