import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class AjoutRdvController {

    @FXML
    private BorderPane ajoutPane;

    @FXML private TextField patientIdField;
    @FXML private TextField medecinIdField;
    @FXML private TextField motifField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;

    private final String URL = "jdbc:mysql://localhost/hopital";
    private final String USER = "root";
    private final String PASSWORD = "a!y!a!boutahli12";
    private RendezVousController rendezVousController;

    public void setRendezVousController(RendezVousController controller) {
        this.rendezVousController = controller;
    }

    @FXML
    public void creerRdv() {
        try {
            int patientId = Integer.parseInt(patientIdField.getText());
            int medecinId = Integer.parseInt(medecinIdField.getText());
            String motif = motifField.getText();
            String statut = "planifie";


            LocalDate date = datePicker.getValue();
            String heure = heureCombo.getValue();

            if (date == null || heure == null || motif.isEmpty()) {
                showAlert("Veuillez remplir tous les champs.");
                return;
            }

            String dateHeure = date + " " + heure; // ex : "2025-05-10 09:00"

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "INSERT INTO rendez_vous (patient_id, medecin_id, date_heure, motif, statut) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            stmt.setInt(2, medecinId);
            stmt.setString(3, dateHeure);
            stmt.setString(4, motif);
            stmt.setString(5, statut);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Rendez-vous ajouté avec succès !");
                if (rendezVousController != null) {
                    rendezVousController.rafraichirTableau(); // Recharge automatiquement
                    rendezVousController.revenirVuePrincipale();
                }

            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            showAlert("L'ID du patient et du médecin doivent être des nombres.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ajout du rendez-vous.");
        }
    }

    @FXML
    public void retour() {
        if (rendezVousController != null) {
            rendezVousController.revenirVuePrincipale();
        } else {
            showAlert("Erreur : contrôleur principal non défini.");
        }
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) patientIdField.getScene().getWindow();
        stage.close();
    }
}
