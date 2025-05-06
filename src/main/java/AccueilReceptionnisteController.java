import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AccueilReceptionnisteController {

    @FXML
    public void ouvrirGestionPatients() {
        chargerPage("Receptionniste.fxml");
    }

    @FXML
    public void ouvrirGestionRdv() {
        chargerPage("RendezVous.fxml");
    }

    @FXML
    public void ouvrirPlanning() {
        chargerPage("Planning.fxml");
    }

    private void chargerPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();

            // Obtenir la scène active depuis un composant visible
            Stage stage = (Stage) Stage.getWindows()
                    .filtered(window -> window.isShowing())
                    .get(0);

            stage.setScene(new Scene(root));
            stage.setTitle("Interface - " + fxmlFile.replace(".fxml", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
