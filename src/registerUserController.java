import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.Year;

public class registerUserController {

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
    @FXML
    private CheckBox teacherCheckBox;
    @FXML
    private CheckBox studentCheckBox;
    @FXML
    private ChoiceBox<String> departmentIdChoiceBox;
    @FXML
    private Button submitButton;

    private Connection connection;
    private int coordinatorDepartmentId;
    private int loggedInUserId;

    public void initialize() {
        connection = DatabaseConnection.getConnection();

        // Load departments from the DEPARTMENT table
        loadDepartments();

        // Add listener to handle role selection logic
        setupRoleSelection();

        // Load coordinator's department
        loadCoordinatorDepartment();
    }

    private void loadDepartments() {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT departmentId, Department_name FROM DEPARTMENT")) {
            ResultSet rs = pstmt.executeQuery();
            ObservableList<String> departments = FXCollections.observableArrayList();
            while (rs.next()) {
                int departmentId = rs.getInt("departmentId");
                String departmentName = rs.getString("Department_name");
                if (departmentId == coordinatorDepartmentId) {
                    departments.add(departmentName);
                }
            }
            departmentIdChoiceBox.setItems(departments);

            // Select the coordinator's department if it exists
            if (coordinatorDepartmentId != 0) {
                departmentIdChoiceBox.getSelectionModel().select(departments.indexOf(getDepartmentName(coordinatorDepartmentId)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getDepartmentName(int departmentId) throws SQLException {
        String query = "SELECT Department_name FROM DEPARTMENT WHERE departmentId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Department_name");
            }
        }
        return null;
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        loadCoordinatorDepartment();
    }

    private void loadCoordinatorDepartment() {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT departmentId FROM [USER] WHERE userId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                coordinatorDepartmentId = rs.getInt("departmentId");
                // Load the department after fetching the department ID
                loadDepartments();
                // Select the department in the choice box
                departmentIdChoiceBox.getSelectionModel().select(departmentIdChoiceBox.getItems().indexOf(coordinatorDepartmentId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupRoleSelection() {
        teacherCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            studentCheckBox.setDisable(isNowSelected);
            if (!isNowSelected && !studentCheckBox.isSelected()) {
                teacherCheckBox.setSelected(true);
            }
        });

        studentCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            teacherCheckBox.setDisable(isNowSelected);
            if (!isNowSelected && !teacherCheckBox.isSelected()) {
                studentCheckBox.setSelected(true);
            }
        });
    }

    @FXML
    private void register(ActionEvent event) {
        // Get user input
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String address = addressField.getText().trim();
        String selectedDepartment = departmentIdChoiceBox.getValue();

        // Validate user input
        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || selectedDepartment == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }

        // Check if at least one role is selected
        if (!teacherCheckBox.isSelected() && !studentCheckBox.isSelected()) {
            showAlert(Alert.AlertType.ERROR, "Error", "At least one role must be selected.");
            return;
        }

        int departmentId = getDepartmentId(selectedDepartment);

        try {
            // Check if user with the same username or email already exists
            if (checkExistingUser(connection, username, email)) {
                showAlert(Alert.AlertType.ERROR, "Error", "User with the same username or email already exists.");
                return;
            }

            // Insert user into the database
            int userId = insertUser(connection, username, password, firstName, lastName, email, phoneNumber, address, departmentId);
            if (userId != -1) {
// Add user role
                if (teacherCheckBox.isSelected()) {
                    addUserRole(connection, userId, "teacher");
                }
                if (studentCheckBox.isSelected()) {
                    addUserRole(connection, userId, "student");
// Insert batch information for student role
                    insertStudentBatch(connection, userId);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");
                // Reset the fields
                resetFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while registering user.");
        }
    }

    private int getDepartmentId(String departmentName) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT departmentId FROM DEPARTMENT WHERE Department_name = ?")) {
            pstmt.setString(1, departmentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("departmentId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean checkExistingUser(Connection connection, String username, String email) throws SQLException {
        String query = "SELECT * FROM [USER] WHERE Username = ? OR Email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private int insertUser(Connection connection, String username, String password, String firstName, String lastName, String email, String phoneNumber, String address, int departmentId) throws SQLException {
        String query = "INSERT INTO [USER] (Username, Password, First_name, Last_name, Email, Phone_number, Address, departmentId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setString(6, phoneNumber);
            pstmt.setString(7, address);
            pstmt.setInt(8, departmentId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    private void addUserRole(Connection connection, int userId, String roleName) throws SQLException {
        String query = "INSERT INTO USER_ROLE (UserId, RoleId) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            int roleId = getRoleId(connection, roleName);
            if (roleId != -1) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, roleId);
                pstmt.executeUpdate();
            }
        }
    }

    private int getRoleId(Connection connection, String roleName) throws SQLException {
        String query = "SELECT roleId FROM ROLE WHERE Role_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, roleName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("roleId");
            }
        }
        return -1;
    }

    private void insertStudentBatch(Connection connection, int userId) throws SQLException {
        String query = "INSERT INTO BATCH (UserId, Batch) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            int currentYear = Year.now().getValue();
            pstmt.setInt(1, userId);
            pstmt.setString(2, String.valueOf(currentYear));
            pstmt.executeUpdate();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetFields() {
        usernameField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneNumberField.clear();
        addressField.clear();
        teacherCheckBox.setSelected(false);
        studentCheckBox.setSelected(false);
        departmentIdChoiceBox.getSelectionModel().selectFirst();
    }
}

