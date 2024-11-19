import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    private WarningSubject warningSubject;

    @FXML
    public void initialize() {
        populateRoles();
        warningSubject = new WarningSubject(); // Initialize WarningSubject
    }

    private void populateRoles() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT Role_name FROM ROLE");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                roleChoiceBox.getItems().add(rs.getString("Role_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: Unable to fetch roles from database.");
        }
    }

    public void login() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleChoiceBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT userId FROM [USER] WHERE Email = ? AND Password = ?");
             PreparedStatement roleStmt = conn.prepareStatement("SELECT roleId FROM ROLE WHERE Role_name = ?");
             PreparedStatement userRoleStmt = conn.prepareStatement("SELECT * FROM USER_ROLE WHERE userId = ? AND roleId = ?"))
        {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Invalid email or password.");
                return;
            }

            int userId = rs.getInt("userId");

            roleStmt.setString(1, role);
            rs = roleStmt.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Invalid role selected.");
                return;
            }

            int roleId = rs.getInt("roleId");

            userRoleStmt.setInt(1, userId);
            userRoleStmt.setInt(2, roleId);
            rs = userRoleStmt.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Authentication Failed", "User does not have the selected role.");
                return;
            }

            System.out.println("Login Successful: User exists in the database.");
            User user = new User();
            user.setUserId(userId);
            System.out.println("User ID set to: " + userId);
            openDashboard(role, userId);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while connecting to the database.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openDashboard(String role, int userId) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = null;
            if ("Student".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("studentDashboard.fxml"));
            } else if ("Teacher".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("teacherDashboard.fxml"));
            } else if ("Coordinator".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("coordinatorDashboard.fxml"));
            } else if ("Admin".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("adminDashboard.fxml"));
            }
            if (loader != null) {
                Pane root = loader.load();
                stage.setScene(new Scene(root));

                if ("Student".equalsIgnoreCase(role)) {
                    studentController controller = loader.getController();
                    controller.setLoggedInUserId(userId);
                    controller.setWarningSubject(warningSubject);
                    warningSubject.addObserver(controller);
                    controller.loadProfile();
                } else if ("Teacher".equalsIgnoreCase(role)) {
                    teacherController controller = loader.getController();
                    controller.setLoggedInUserId(userId);
                    controller.loadProfile();
                } else if ("Coordinator".equalsIgnoreCase(role)) {
                    coordinatorController controller = loader.getController();
                    controller.setLoggedInUserId(userId);
                } else if ("Admin".equalsIgnoreCase(role)) {
                    adminController controller = loader.getController();
                    controller.setLoggedInUserId(userId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading dashboard: " + e.getMessage());
        }
    }

//    private void openDashboard(String role, int userId) {
//        try {
//            Stage stage = (Stage) emailField.getScene().getWindow();
//            FXMLLoader loader = null;
//            if ("Student".equalsIgnoreCase(role)) {
//                loader = new FXMLLoader(getClass().getResource("studentDashboard.fxml"));
//            } else if ("Teacher".equalsIgnoreCase(role)) {
//                loader = new FXMLLoader(getClass().getResource("teacherDashboard.fxml"));
//            } else if ("Coordinator".equalsIgnoreCase(role)) {
//                loader = new FXMLLoader(getClass().getResource("coordinatorDashboard.fxml"));
//            } else if ("Admin".equalsIgnoreCase(role)) {
//                loader = new FXMLLoader(getClass().getResource("adminDashboard.fxml"));
//            }
//            if (loader != null) {
//                stage.setScene(new Scene(loader.load()));
//                if ("Teacher".equalsIgnoreCase(role)) {
//                    teacherController controller = loader.getController();
//                    controller.setLoggedInUserId(userId);  // Pass the userId to the controller
//                    controller.loadProfile();  // Ensure profile is loaded with correct userId
//                } else if ("Student".equalsIgnoreCase(role)) {
//                    loader = new FXMLLoader(getClass().getResource("studentDashboard.fxml"));
//                    studentController controller = loader.getController();
//                    controller.setLoggedInUserId(userId);
//                    controller.setWarningSubject(warningSubject); // Pass the WarningSubject
//                    warningSubject.addObserver(controller); // Register the observer
//                    controller.loadProfile();   // Ensure profile is loaded with correct userId
//                } else if ("Coordinator".equalsIgnoreCase(role)) {
//                    coordinatorController controller = loader.getController();
//                    controller.setLoggedInUserId(userId);  // Pass the userId to the controller
//                } else if ("Admin".equalsIgnoreCase(role)) {
//                    adminController controller = loader.getController();
//                    controller.setLoggedInUserId(userId);  // Pass the userId to the controller
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Error loading dashboard: " + e.getMessage());
//        }
//    }
}
