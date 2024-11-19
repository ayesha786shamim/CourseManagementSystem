import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class manageProfileController {

    @FXML
    private TextField userIdField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField addressField;

    private int loggedInUserId;
    private int coordinatorDepartmentId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
        this.coordinatorDepartmentId = getCoordinatorDepartmentId(userId);
    }

    @FXML
    private void initialize() {
        // Optional initialization code
    }

    @FXML
    private void enter() {
        String userId = userIdField.getText();
        if (userId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "User ID cannot be empty.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM [USER] u " +
                    "JOIN USER_ROLE ur ON u.userId = ur.UserId " +
                    "WHERE u.userId = ? AND u.DepartmentId = ? AND ur.RoleId NOT IN (SELECT roleId FROM ROLE WHERE Role_name IN ('coordinator', 'admin'))";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, Integer.parseInt(userId));
            preparedStatement.setInt(2, coordinatorDepartmentId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                emailField.setText(resultSet.getString("Email"));
                passwordField.setText(resultSet.getString("Password"));
                firstNameField.setText(resultSet.getString("First_name"));
                lastNameField.setText(resultSet.getString("Last_name"));
                usernameField.setText(resultSet.getString("Username"));
                phoneNumberField.setText(resultSet.getString("Phone_number"));
                addressField.setText(resultSet.getString("Address"));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found or does not belong to your department.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void edit() {
        String userId = userIdField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();

        if (userId.isEmpty() || email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to edit this user's information?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.showAndWait();

        if (confirmationAlert.getResult() == ButtonType.YES) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String updateQuery = "UPDATE [USER] SET Email = ?, Password = ?, First_name = ?, Last_name = ?, Username = ?, " +
                                     "Phone_number = ?, Address = ? WHERE userId = ?";

                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, firstName);
                preparedStatement.setString(4, lastName);
                preparedStatement.setString(5, username);
                preparedStatement.setString(6, phoneNumber);
                preparedStatement.setString(7, address);
                preparedStatement.setInt(8, Integer.parseInt(userId));

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User information updated successfully.");
                    // Navigate back to coordinator dashboard
                    cancel();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user information.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    private void cancel() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("coordinatorDashboard.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCoordinatorDepartmentId(int coordinatorUserId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT DepartmentId FROM [USER] WHERE UserId = ? AND DepartmentId IS NOT NULL";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, coordinatorUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("DepartmentId");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Coordinator not found or department not assigned.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }

        return -1; // Return an invalid department ID if not found
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
