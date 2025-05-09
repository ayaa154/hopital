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
import java.sql.ResultSet;
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
                showAlert("Please fill in all fields.");
                return;
            }

            // 💡 Vérifie que la date/heure est au moins 24h à l'avance
            java.time.LocalDateTime selectedDateTime = date.atTime(
                    Integer.parseInt(heure.split(":")[0]),
                    Integer.parseInt(heure.split(":")[1])
            );

            if (selectedDateTime.isBefore(java.time.LocalDateTime.now().plusHours(24))) {
                showAlert("Appointments must be scheduled at least 24 hours in advance.");
                return;
            }

            String dateHeure = date + " " + heure; // exemple : "2025-05-10 09:00"

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // 🔍 Vérifier s'il y a déjà un RDV pour ce médecin à cette date/heure
            String checkSql = "SELECT COUNT(*) FROM rendez_vous WHERE medecin_id = ? AND date_heure = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, medecinId);
            checkStmt.setString(2, dateHeure);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            checkStmt.close();

            if (count > 0) {
                showAlert("This doctor already has an appointment at this time. Please choose a different time.");
                conn.close();
                return;
            }

            // ✅ Si libre, insérer le rendez-vous
            String sql = "INSERT INTO rendez_vous (patient_id, medecin_id, date_heure, motif, statut) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            stmt.setInt(2, medecinId);
            stmt.setString(3, dateHeure);
            stmt.setString(4, motif);
            stmt.setString(5, statut);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Appointment successfully added!");
                if (rendezVousController != null) {
                    rendezVousController.rafraichirTableau();
                    rendezVousController.revenirVuePrincipale();
                }
            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            showAlert("Patient ID and Doctor ID must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error while adding the appointment.");
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
