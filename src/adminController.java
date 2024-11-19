import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.*;

public class adminController {

    @FXML
    private TextField emailField, passwordField, firstNameField, lastNameField, usernameField, phoneNumberField, addressField;
    @FXML
    private ChoiceBox<String> departmentIdChoiceBox;
    @FXML
    private CheckBox teacherCheckBox, coordinatorCheckBox;
    @FXML
    private Pane rightPane;

    @FXML
    public void initialize() {
        populateDepartmentChoiceBox();
        rightPane.setVisible(false);
    }

    private void populateDepartmentChoiceBox() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Department_name FROM DEPARTMENT")) {
            ObservableList<String> departments = FXCollections.observableArrayList();
            while (rs.next()) {
                departments.add(rs.getString("Department_name"));
            }
            departmentIdChoiceBox.setItems(departments);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while populating department choice box: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        phoneNumberField.clear();
        addressField.clear();
        departmentIdChoiceBox.setValue(null);
        teacherCheckBox.setSelected(false);
        coordinatorCheckBox.setSelected(false);
    }

    @FXML
    private void register(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();
        String departmentName = departmentIdChoiceBox.getValue();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || departmentName == null) {
            showAlert("Error", "Validation Error", "Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Get the departmentId from the department name
            int departmentId = getDepartmentId(connection, departmentName);
            // Insert the user information into the USER table
            int userId = insertUser(connection, username, password, firstName, lastName, email, phoneNumber, address, departmentId);
            // Insert the roles into the USER_ROLE table
            if (teacherCheckBox.isSelected()) {
                addUserRole(connection, userId, "TEACHER");
            }
            if (coordinatorCheckBox.isSelected()) {
                addUserRole(connection, userId, "COORDINATOR");
            }

            showAlert("Success", "Registration Successful", "User registration successful.", Alert.AlertType.INFORMATION);

            // Clear the fields
            clearFields();

            // Hide the right pane
            rightPane.setVisible(false);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error", "An error occurred while registering user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int getDepartmentId(Connection connection, String departmentName) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT departmentId FROM DEPARTMENT WHERE Department_name = ?")) {
            pstmt.setString(1, departmentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("departmentId");
            } else {
                throw new SQLException("Department not found: " + departmentName);
            }
        }
    }

    private int insertUser(Connection connection, String username, String password, String firstName, String lastName,
                           String email, String phoneNumber, String address, int departmentId) throws SQLException {
        String query = "INSERT INTO [USER] (Username, Password, First_name, Last_name, Email, Phone_number, " +
                        "Address, DepartmentId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setString(6, phoneNumber);
            pstmt.setString(7, address);
            pstmt.setInt(8, departmentId);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve user ID.");
            }
        }
    }

    private void addUserRole(Connection connection, int userId, String roleName) throws SQLException {
        // Get the roleId from the role name
        int roleId = getRoleId(connection, roleName);

        // Insert the user-role relationship into the USER_ROLE table
        String query = "INSERT INTO USER_ROLE (UserId, RoleId) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            pstmt.executeUpdate();
        }
    }

    private int getRoleId(Connection connection, String roleName) throws SQLException {
        String query = "SELECT roleId FROM ROLE WHERE Role_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, roleName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("roleId");
            } else {
                throw new SQLException("Role not found: " + roleName);
            }
        }
    }

    private void showAlert(String title, String headerText, String contentText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    @FXML
    private void showRegisterPane(ActionEvent event) {
        // Hide the right pane
        rightPane.setVisible(true);
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            AnchorPane loginPane = loader.load();
            rightPane.getScene().setRoot(loginPane);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Logout Error", "An error occurred while logging out: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setLoggedInUserId(int userId) {
    }
}
