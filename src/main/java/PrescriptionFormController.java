import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrescriptionFormController {

    @FXML
    private TextField medicamentField;

    @FXML
    private TextArea posologieField;

    @FXML
    private TextField dureeField;

    private int medecinId;  // ID du médecin
    private int rendezVousId;  // ID du rendez-vous

    // Setter pour l'ID du médecin
    public void setMedecinId(int id) {
        this.medecinId = id;
    }

    // Setter pour l'ID du rendez-vous
    public void setRendezVousId(int id) {
        this.rendezVousId = id;
    }

    @FXML
    private void enregistrerPrescription() {
        // Récupérer les valeurs des champs
        String medicament = medicamentField.getText().trim();
        String posologie = posologieField.getText().trim();
        String duree = dureeField.getText().trim();

        // Vérification si les champs sont vides
        if (medicament.isEmpty() || posologie.isEmpty() || duree.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        // Requête SQL pour insérer la prescription
        String sql = "INSERT INTO prescriptions (medecin_id, rendez_vous_id, medicament, posologie, duree) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Préparer la requête avec les valeurs
            stmt.setInt(1, medecinId);  // ID du médecin
            stmt.setInt(2, rendezVousId);  // ID du rendez-vous
            stmt.setString(3, medicament);
            stmt.setString(4, posologie);
            stmt.setString(5, duree);

            // Exécuter l'insertion dans la base de données
            stmt.executeUpdate();

            // Afficher une alerte de succès
            showAlert("Succès", "Prescription enregistrée avec succès.");

            // Facultatif : fermer la fenêtre après l'enregistrement
            medicamentField.getScene().getWindow().hide();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer la prescription : " + e.getMessage());
        }
    }


    @FXML
    private void returnToDashboard() {
        try {
            // Charger la page medecin_dashboard.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("medecin_dashboard.fxml"));
            Parent dashboardPage = loader.load();

            // Récupérer le contrôleur du tableau de bord et passer l'ID du médecin
            MedecinDashboardController dashboardController = loader.getController();
            dashboardController.setMedecinId(medecinId);  // Passer l'ID du médecin connecté

            // Obtenez l'actuelle scène et remplacez le contenu par le tableau de bord
            Scene currentScene = medicamentField.getScene();
            currentScene.setRoot(dashboardPage);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord.");
        }
    }




    // Méthode pour afficher une alerte
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
