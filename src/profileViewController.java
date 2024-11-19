import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class profileViewController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField departmentField;

    private int loggedInUserId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void fetchAndDisplayProfileData() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM [USER] WHERE UserId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("First_name") + " " + rs.getString("Last_name"));
                emailField.setText(rs.getString("Email"));
                phoneField.setText(rs.getString("Phone_number"));
                addressField.setText(rs.getString("Address"));

                // Fetch department name using departmentId
                int departmentId = rs.getInt("DepartmentId");
                String departmentName = fetchDepartmentName(departmentId);
                departmentField.setText(departmentName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String fetchDepartmentName(int departmentId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT Department_name FROM DEPARTMENT WHERE departmentId = ?")) {
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Department_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
