import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class coordinatorController {

    @FXML
    private AnchorPane profilePane; // The right pane

    @FXML
    private Button allocateCoursesButton;

    private int loggedInUserId; // User ID of the logged-in user

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    @FXML
    private void registerUser() {
        loadRegisterUserView();
    }
    private void loadRegisterUserView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerUser.fxml"));
            Pane content = loader.load();
            profilePane.getChildren().setAll(content);
            registerUserController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void registerCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerCoursesCoordinator.fxml"));
            Pane content = loader.load();
            registerCoursesCoordinatorController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadProfile() {
        viewProfile();
    }


    // Method to load the profile information
    private void viewProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profileView.fxml"));
            Pane content = loader.load(); // Load the FXML file

            // Add the loaded content to the right pane
            profilePane.getChildren().setAll(content);

            // Pass the logged-in user ID to the controller
            profileViewController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            controller.fetchAndDisplayProfileData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewGPA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("viewGPAcoordinator.fxml"));
            Pane content = loader.load(); // Load the FXML file

            // Pass the logged-in user ID to the controller
            viewGPAcoordinatorController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);

            // Add the loaded content to the profile pane
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewWarning() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("viewWarningsCoordinator.fxml"));
            Pane content = loader.load(); // Load the FXML file
            viewWarningCoordinatorController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);

            // Add the loaded content to the profile pane
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void viewCourseListing() {
        loadView("courseListingCoordinator.fxml");
    }
    @FXML
    private void viewCompletedCreditHours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("completedCreditsCoordinator.fxml"));
            Pane content = loader.load();
            completedCreditsCoordinatorController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void allocateCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allocateCourse.fxml"));
            Pane content = loader.load();
            allocateCourseController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void manageProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageProfile.fxml"));
            Pane content = loader.load();
            manageProfileController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);

            // Add the loaded content to the profile pane
            profilePane.getChildren().setAll(content);
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

    public void printTranscript(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("printTranscriptCoordinator.fxml"));
            Pane content = loader.load();
            printTranscriptCoordinatorController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void batchwiseGPA(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("batchwiseGPA.fxml"));
            Pane content = loader.load(); // Load the FXML file
            batchwiseGPAcontroller controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void batchwiseCreditHour(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("batchwiseCredithour.fxml"));
            Pane content = loader.load(); // Load the FXML file
            batchwiseCredithourController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void batchwiseWarning(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("batchwiseWarning.fxml"));
            Pane content = loader.load(); // Load the FXML file
            batchwiseWarningController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void batchwiseUnregisteredStudent(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("batchwiseUnregisteredStudent.fxml"));
            Pane content = loader.load(); // Load the FXML file
            batchwiseUnregisteredStudentController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void warnings(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("warning.fxml"));
            Pane content = loader.load(); // Load the FXML file
            warningController controller = loader.getController();
            controller.setLoggedInUserId(loggedInUserId);
            profilePane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




