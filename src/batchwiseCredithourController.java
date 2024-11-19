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

public class batchwiseCredithourController {
    @FXML
    private ChoiceBox<String> choiceBoxBatch;
    @FXML
    private TableView<CreditHour> tableViewCredithour;
    @FXML
    private TableColumn<CreditHour, Integer> columnUserId;
    @FXML
    private TableColumn<CreditHour, String> columnStudentName;
    @FXML
    private TableColumn<CreditHour, Integer> completedCredits;

    private int loggedInUserId;

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    @FXML
    public void initialize() {
        columnUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        columnStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        completedCredits.setCellValueFactory(new PropertyValueFactory<>("completedCredits"));
    }

    @FXML
    public void onEnterButtonClicked(ActionEvent event) {
        String selectedBatch = choiceBoxBatch.getValue();
        if (selectedBatch != null) {
            populateTableWithCompletedCredits(selectedBatch);
        }
    }

    private void populateTableWithCompletedCredits(String batch) {
        ObservableList<CreditHour> creditsList = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Query to get the completed credits of students from the same department and batch
            String getCompletedCreditsQuery = "SELECT U.userId, U.First_name + ' ' + U.Last_name AS StudentName, " +
                    "SUM(C.Credits) AS CompletedCredits FROM [USER] U " +
                    "JOIN STUDENT_COURSE SC ON U.userId = SC.UserId " +
                    "JOIN COURSE C ON SC.CourseId = C.CourseId " +
                    "JOIN BATCH B ON U.userId = B.UserId " +
                    "WHERE B.Batch = ? AND SC.Status = 'Registered' " +
                    "GROUP BY U.userId, U.First_name, U.Last_name";
            try (PreparedStatement getCompletedCreditsStmt = connection.prepareStatement(getCompletedCreditsQuery)) {
                getCompletedCreditsStmt.setString(1, batch);
                ResultSet completedCreditsRs = getCompletedCreditsStmt.executeQuery();
                while (completedCreditsRs.next()) {
                    int userId = completedCreditsRs.getInt("userId");
                    String studentName = completedCreditsRs.getString("StudentName");
                    int completedCredits = completedCreditsRs.getInt("CompletedCredits");

                    creditsList.add(new CreditHour(userId, studentName, completedCredits));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }

        tableViewCredithour.setItems(creditsList);
    }

    public static class CreditHour {
        private final int userId;
        private final String studentName;
        private final int completedCredits;

        public CreditHour(int userId, String studentName, int completedCredits) {
            this.userId = userId;
            this.studentName = studentName;
            this.completedCredits = completedCredits;
        }

        public int getUserId() {
            return userId;
        }

        public String getStudentName() {
            return studentName;
        }

        public int getCompletedCredits() {
            return completedCredits;
        }
    }
}
