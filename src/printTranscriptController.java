import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class printTranscriptController {

    @FXML
    private Label creditHoursLabel;

    @FXML
    private Button printButton;

    private int loggedInUserId;

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        checkCreditHours();
    }

    private void checkCreditHours() {
        String query = "SELECT SUM(Credits) AS TotalCredits FROM COURSE c JOIN STUDENT_COURSE sc ON c.CourseId = sc.CourseId WHERE sc.UserId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalCredits = rs.getInt("TotalCredits");
                if (totalCredits >= 140) {
                    // Show print button
                    printButton.setVisible(true); // This should now work
                    printButton.setOnAction(e -> printTranscript());
                } else {
                    // Display message about incomplete credit hours
                    creditHoursLabel.setText("Your credit hours are not yet completed.");
                    creditHoursLabel.setVisible(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void printTranscript() {
        // Create a PrinterJob
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(printButton.getScene().getWindow())) {
            // Print the transcript
            printerJob.endJob();
        }
        System.out.println("Printing transcript...");
    }
}
