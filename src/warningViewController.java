import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class warningViewController {

    @FXML
    private TableView<Warning> warningTable;
    @FXML
    private TableColumn<Warning, String> warningColumn;
    @FXML
    private TableColumn<Warning, String> dateColumn;

    private int loggedInUserId;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public void initialize() {
        warningColumn.setCellValueFactory(new PropertyValueFactory<>("warningMessage"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    public void fetchAndDisplayWarningData() {
        warningTable.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM WARNING WHERE UserId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String warningMessage = rs.getString("Warning_message");
                String date = rs.getString("Warning_date");
                warningTable.getItems().add(new Warning(warningMessage, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Warning {
        private final SimpleStringProperty warningMessage;
        private final SimpleStringProperty date;

        public Warning(String warningMessage, String date) {
            this.warningMessage = new SimpleStringProperty(warningMessage);
            this.date = new SimpleStringProperty(date);
        }

        public String getWarningMessage() {
            return warningMessage.get();
        }

        public String getDate() {
            return date.get();
        }
    }
}
