import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AccueilReceptionnisteController {

    @FXML
    private Button btnGestionPatients; // ✅ LIGNE À AJOUTER

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
        chargerPage("Planning.fxml"); // à créer plus tard
    }



    private void chargerPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();

            Scene currentScene = btnGestionPatients.getScene(); // ⚠️ Utilise le bouton bien lié au FXML
            Stage currentStage = (Stage) currentScene.getWindow();

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Interface - " + fxmlFile.replace(".fxml", ""));
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
