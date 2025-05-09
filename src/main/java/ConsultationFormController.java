import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class ConsultationFormController {

    private int medecinId;
    private int rendezVousId;
    private int patientId;

    @FXML private TextArea tfDiagnostic;
    @FXML private TextArea tfTraitement;
    @FXML private TextArea taNotes;
    @FXML private Button btnAjouterConsultation;

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public void setRendezVousId(int rendezVousId) {
        this.rendezVousId = rendezVousId;
    }

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    @FXML
    private void returnToDashboard(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("medecin_dashboard.fxml"));
            Parent dashboardPage = loader.load();

            // Récupérer le contrôleur et lui transmettre l'ID du médecin
            MedecinDashboardController dashboardController = loader.getController();
            dashboardController.setMedecinId(medecinId);

            // Changer de scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(dashboardPage));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner au tableau de bord.");
        }
    }



    @FXML
    private void ajouterConsultation() {
        String diagnostic = tfDiagnostic.getText().trim();
        String traitement = tfTraitement.getText().trim();
        String notes = taNotes.getText().trim();

        if (diagnostic.isEmpty() || traitement.isEmpty()) {
            showAlert("Champs incomplets", "Le diagnostic et le traitement sont obligatoires.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT patient_id FROM rendez_vous WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, rendezVousId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    patientId = rs.getInt("patient_id");
                    int dossierId = getOrCreateDossierId(conn, patientId);
                    if (dossierId == -1) {
                        showAlert("Erreur", "Impossible de créer ou récupérer le dossier médical.");
                        return;
                    }
                    insertConsultation(dossierId, medecinId, diagnostic, traitement, notes);
                } else {
                    showAlert("Erreur", "Rendez-vous introuvable.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de l'ajout.");
        }
    }

    private int getOrCreateDossierId(Connection conn, int patientId) {
        String check = "SELECT id FROM dossiers_medicaux WHERE patient_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insert = "INSERT INTO dossiers_medicaux (patient_id) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, patientId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void insertConsultation(int dossierId, int medecinId, String diagnostic, String traitement, String notes) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO consultations (dossier_id, medecin_id, date_consultation, diagnostic, traitement, notes) VALUES (?, ?, NOW(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, dossierId);
                ps.setInt(2, medecinId);
                ps.setString(3, diagnostic);
                ps.setString(4, traitement);
                ps.setString(5, notes);
                ps.executeUpdate();
            }

            showAlert("Succès", "Consultation enregistrée avec succès.");
            Stage stage = (Stage) btnAjouterConsultation.getScene().getWindow();


        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d’enregistrer la consultation.");
        }
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) btnAjouterConsultation.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
