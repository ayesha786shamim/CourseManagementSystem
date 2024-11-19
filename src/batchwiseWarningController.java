import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class batchwiseWarningController {
    @FXML
    private ChoiceBox<String> choiceBoxBatch;
    @FXML
    private TableView<Warning> tableViewWarnings;
    @FXML
    private TableColumn<Warning, Integer> columnUserId;
    @FXML
    private TableColumn<Warning, String> columnStudentName;
    @FXML
    private TableColumn<Warning, String> columnWarningMessage;
    @FXML
    private TableColumn<Warning, String> columnDate;

    private int loggedInUserId;
    private int coordinatorDepartmentId;

    // Set the logged-in user ID from the dashboard
    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        fetchCoordinatorDepartmentId();
    }

    // Initialize method to set up the table columns
    @FXML
    public void initialize() {
        columnUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        columnStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        columnWarningMessage.setCellValueFactory(new PropertyValueFactory<>("warningMessage"));
        columnDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Initialize choice box with batch years
        choiceBoxBatch.setItems(FXCollections.observableArrayList("2020", "2021", "2022", "2023", "2024"));
    }

    // Method to fetch the department ID of the logged-in coordinator
    private void fetchCoordinatorDepartmentId() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT departmentId FROM [USER] WHERE userId = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, loggedInUserId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    coordinatorDepartmentId = rs.getInt("departmentId");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
    }

    // Method to handle the ENTER button click
    @FXML
    public void onEnterButtonClicked(ActionEvent event) {
        String selectedBatch = choiceBoxBatch.getValue();
        if (selectedBatch != null) {
            populateTableWithWarnings(selectedBatch);
        }
    }

    // Method to populate the TableView with warnings from the selected batch
    private void populateTableWithWarnings(String batch) {
        ObservableList<Warning> warningsList = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT W.UserId, U.First_name + ' ' + U.Last_name AS StudentName, " +
                    "W.Warning_message, W.Warning_date " +
                    "FROM WARNING W " +
                    "JOIN [USER] U ON W.UserId = U.userId " +
                    "WHERE U.departmentId = ? AND W.Batch = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, coordinatorDepartmentId);
                stmt.setString(2, batch);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int userId = rs.getInt("UserId");
                    String studentName = rs.getString("StudentName");
                    String warningMessage = rs.getString("Warning_message");
                    String date = rs.getString("Warning_date");

                    warningsList.add(new Warning(userId, studentName, warningMessage, date));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }

        tableViewWarnings.setItems(warningsList);
    }

    // Warning class to represent a warning entry
    public static class Warning {
        private final int userId;
        private final String studentName;
        private final String warningMessage;
        private final String date;

        public Warning(int userId, String studentName, String warningMessage, String date) {
            this.userId = userId;
            this.studentName = studentName;
            this.warningMessage = warningMessage;
            this.date = date;
        }

        public int getUserId() {
            return userId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public String getDate() {
            return date;
        }
    }
}
