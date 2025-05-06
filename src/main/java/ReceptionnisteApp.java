import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class ReceptionnisteApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Receptionniste.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Interface Réceptionniste");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void lancer() {
        Application.launch();
    }
}
