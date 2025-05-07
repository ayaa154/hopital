import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MedecinApp extends Application {

    public static int medecinId;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("medecin_dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        MedecinDashboardController controller = loader.getController();
        controller.setMedecinId(medecinId);

        stage.setTitle("Dashboard Médecin");
        stage.setScene(scene);
        stage.show();
    }

    public static void lancer(int id) {
        medecinId = id;
        Application.launch();
    }
}
