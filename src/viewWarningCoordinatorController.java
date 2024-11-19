import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class viewWarningCoordinatorController {

    @FXML
    private TableView<WarningData> warningTable;

    @FXML
    private TableColumn<WarningData, String> warningColumn;

    @FXML
    private TableColumn<WarningData, Date> dateColumn;

    @FXML
    private TextField userIdField;

    private ObservableList<WarningData> warningDataList = FXCollections.observableArrayList();

    private int loggedInUserId; // ID of the logged-in coordinator

    @FXML
    public void initialize() {
        warningColumn.setCellValueFactory(new PropertyValueFactory<>("warning"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    @FXML
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

            loadWarningData(Integer.parseInt(userId));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadWarningData(int userId) {
        String sql = "SELECT Warning_message, Warning_date FROM WARNING WHERE UserId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            warningDataList.clear();
            while (rs.next()) {
                String warningMessage = rs.getString("Warning_message");
                Date warningDate = rs.getDate("Warning_date");
                warningDataList.add(new WarningData(warningMessage, warningDate));
            }

            warningTable.setItems(warningDataList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class WarningData {
        private final String warning;
        private final Date date;

        public WarningData(String warning, Date date) {
            this.warning = warning;
            this.date = date;
        }

        public String getWarning() {
            return warning;
        }

        public Date getDate() {
            return date;
        }
    }
}

