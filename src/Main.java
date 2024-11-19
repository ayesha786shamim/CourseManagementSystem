import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Check database connection
        if (DatabaseConnection.isConnected()) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
                primaryStage.setTitle("COURSE MANAGEMENT SYSTEM");
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Database connection failed. Application will not start.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
