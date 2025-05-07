import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProfilPatientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/profil_patient.fxml"));
        primaryStage.setTitle("Profil Patient - JavaFX");
        primaryStage.setScene(new Scene(root, 850, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
