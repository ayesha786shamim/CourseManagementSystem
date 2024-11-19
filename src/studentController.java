import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
public class studentController implements WarningObserver{
    @FXML
    private AnchorPane profilePane; // The right pane

    private int loggedInUserId;
    private WarningSubject warningSubject;

    @Override
    public void update(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.out.println("Observer pattern is working. Received message: " + message);
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        loadWarnings();
    }

    public void setWarningSubject(WarningSubject warningSubject) {
        this.warningSubject = warningSubject;
        this.warningSubject.addObserver(this);
    }

    private void loadWarnings() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) AS warningCount FROM WARNING WHERE UserId = ?")) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int warningCount = rs.getInt("warningCount");
                if (warningCount > 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText(null);
                    alert.setContentText("You have received a warning.");
                    alert.showAndWait();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: Unable to fetch warnings from database.");
        }
    }


    @FXML
    public void initialize() {
        profilePane.getChildren().clear();
        if (warningSubject == null) {
            warningSubject = new WarningSubject(); // Create a new instance if it's null
            warningSubject.addObserver(this); // Add the observer
        }
    }

    @FXML
    public void loadProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profileView.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            profileViewController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.fetchAndDisplayProfileData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewWarnings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("warningView.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            warningViewController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.fetchAndDisplayWarningData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void registerCourses() {
        int selectedSemester = 0;
        loadRegisterCoursesView(selectedSemester);
    }

    private void loadRegisterCoursesView(int selectedSemester) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerCourses.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            registerCoursesController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.fetchAndDisplayCourses(String.valueOf(selectedSemester));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void completedCreditHours() {
        System.out.println("Completed Credit Hours button pressed");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("creditHour.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            creditHourController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.displayCompletedCreditHours();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewGPA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GPAview.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            GPAviewController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.loadGPAData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewCourseListing() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("courseListing.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            courseListingController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            AnchorPane loginPane = loader.load();
            profilePane.getScene().setRoot(loginPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//public void print(ActionEvent actionEvent) {
//    try {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("printTranscript.fxml"));
//        Pane content = loader.load(); // Load the FXML file
//        printTranscriptController controller = loader.getController();
//        controller.setLoggedInUserId(loggedInUserId);
//        profilePane.getChildren().setAll(content);
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//}

    @FXML
    private void print(ActionEvent actionEvent) {
        try {
            TranscriptGenerator.generateTranscriptHTML(loggedInUserId);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("invalid student name")) {
                showErrorAlert("Error generating transcript", "The student's name is invalid or empty. Please check the database.");
            } else {
                showErrorAlert("Error generating transcript", e.getMessage());
            }
        }
    }




//    @FXML
//    private void print(ActionEvent actionEvent) {
//        try {
//            TranscriptGenerator.generateTranscriptHTML(loggedInUserId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (e.getMessage().contains("invalid student name")) {
//                showErrorAlert("Error generating transcript", "The student's name is invalid or empty. Please check the database.");
//            } else {
//                showErrorAlert("Error generating transcript", e.getMessage());
//            }
//        }
//    }


    private void showErrorAlert(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void coursesOffered(ActionEvent actionEvent) {
        try {
            if (loggedInUserId != 0) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("courses.fxml"));
                Pane content = loader.load();
                coursesController controller = loader.getController();
                controller.setLoggedInUserId(loggedInUserId);
                profilePane.getChildren().setAll(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}