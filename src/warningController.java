import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import java.sql.*;

public class warningController implements WarningObserver{

    @FXML
    private TextField studentIdField;
    @FXML
    private TextField warningMessage1;
    @FXML
    private TextField warningMessage2;
    @FXML
    private TextField warningMessage3;
    @FXML
    private Label statusLabel;
    @FXML
    private Button enterButton;
    @FXML
    private Button submitButton;

    private int loggedInUserId;
    private int coordinatorDepartmentId;
    private WarningSubject subject;

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        fetchCoordinatorDepartment();
    }

    public void setWarningSubject(WarningSubject subject) {
        this.subject = subject;
        this.subject.addObserver(this);
    }
    @FXML
    public void initialize() {
        subject.addObserver(this);
    }

    // Fetch coordinator department
    private void fetchCoordinatorDepartment() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT departmentId FROM [USER] WHERE userId = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, loggedInUserId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                coordinatorDepartmentId = resultSet.getInt("departmentId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to check if the user is a student
    private boolean isUserStudent(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT RoleId FROM USER_ROLE WHERE UserId = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int roleId = resultSet.getInt("RoleId");
                if (isRoleStudent(roleId)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to check if the role is student
    private boolean isRoleStudent(int roleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Role_name FROM ROLE WHERE roleId = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, roleId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String roleName = resultSet.getString("Role_name");
                return "Student".equalsIgnoreCase(roleName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to check if student belongs to the same department
    @FXML
    public void handleEnterButtonAction(ActionEvent event) {
        String studentIdText = studentIdField.getText().trim();
        if (studentIdText.isEmpty()) {
            statusLabel.setText("Student ID cannot be empty");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(studentIdText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Student ID format");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (!isUserStudent(studentId)) {
            statusLabel.setText("ID does not belong to a student");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (!isStudentInCoordinatorDepartment(studentId)) {
            statusLabel.setText("Student does not belong to the same department");
            statusLabel.setTextFill(Color.GREEN);
        } else {
            statusLabel.setText("Student verified and belongs to the department");
            statusLabel.setTextFill(Color.GREEN);
        }
    }

    // Check if student is in the same department
    private boolean isStudentInCoordinatorDepartment(int studentId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT departmentId FROM [USER] WHERE userId = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("departmentId") == coordinatorDepartmentId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Handle submit button action
    @FXML
    public void handleSubmitButtonAction(ActionEvent event) {
        String studentIdText = studentIdField.getText().trim();
        if (studentIdText.isEmpty()) {
            statusLabel.setText("Student ID cannot be empty");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(studentIdText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Student ID format");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (!isUserStudent(studentId)) {
            statusLabel.setText("Cannot submit. ID does not belong to a student");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (isStudentInCoordinatorDepartment(studentId)) {
            String message1 = warningMessage1.getText().trim();
            String message2 = warningMessage2.getText().trim();
            String message3 = warningMessage3.getText().trim();

            if (message1.isEmpty() && message2.isEmpty() && message3.isEmpty()) {
                statusLabel.setText("At least one warning message must be filled");
                statusLabel.setTextFill(Color.RED);
            } else {
                storeWarningInDatabase(studentId, message1, message2, message3);
                statusLabel.setText("Warning stored successfully");
                statusLabel.setTextFill(Color.GREEN);
            }
        } else {
            statusLabel.setText("Cannot submit. Student does not belong to the same department");
            statusLabel.setTextFill(Color.RED);
        }
    }

    // Store warning in the database
    private void storeWarningInDatabase(int studentId, String message1, String message2, String message3) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String batch = fetchStudentBatch(studentId);
            String warningMessage = message1 + " " + message2 + " " + message3;
            String query = "INSERT INTO WARNING (UserId, Warning_message, Batch, Warning_date) VALUES (?, ?, ?, GETDATE())";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            preparedStatement.setString(2, warningMessage.trim());
            preparedStatement.setString(3, batch);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Fetch student's batch
    private String fetchStudentBatch(int studentId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Batch FROM BATCH WHERE UserId = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("Batch");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Warning Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.out.println("Observer pattern is working. Received message: " + message);
    }
}
