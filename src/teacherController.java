import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class teacherController {

    @FXML
    private AnchorPane profilePane; // The right pane

    private int loggedInUserId; // User ID of the logged-in user

    public void setLoggedInUserId(int loggedInUserId) {

        this.loggedInUserId = loggedInUserId;
    }

    @FXML
    public void loadProfile() {

        viewProfile();
    }

    private void viewProfile() {
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
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            AnchorPane loginPane = loader.load(); // Load the FXML file
            profilePane.getScene().setRoot(loginPane); // Set the login pane as root
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCourseListing(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("courseListingTeacher.fxml"));
            Pane content = loader.load(); // Load the FXML file
            courseListingTeacherController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayAllCourses(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("courses.fxml"));
            Pane content = loader.load(); // Load the FXML file
            coursesController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


