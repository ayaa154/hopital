import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AccueilReceptionnisteController {

    @FXML
    public void ouvrirGestionPatients() {
        chargerPage("Receptionniste.fxml");
    }

    @FXML
    public void ouvrirGestionRdv() {
        chargerPage("RendezVous.fxml"); // à créer plus tard
    }

    @FXML
    public void ouvrirPlanning() {
        chargerPage("Planning.fxml"); // à créer plus tard
    }

    private void chargerPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Interface");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
