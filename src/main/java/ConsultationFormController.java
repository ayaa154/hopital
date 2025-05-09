import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConsultationFormController {

    private int medecinId;
    private int rendezVousId;
    private int patientId;

    @FXML private TextArea tfDiagnostic;
    @FXML private TextArea tfTraitement;
    @FXML private TextArea taNotes;
    @FXML private Button btnAjouterConsultation;

    // Méthode pour définir l'id du médecin
    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    // Méthode pour définir l'id du rendez-vous
    public void setRendezVousId(int rendezVousId) {
        this.rendezVousId = rendezVousId;
    }

    // Méthode d'initialisation (aucune modification ici)
    @FXML
    public void initialize() {
        // Initialisation des champs si nécessaire (par exemple, des vérifications supplémentaires).
    }

    // Méthode pour ajouter la consultation dans la base de données
    @FXML
    private void ajouterConsultation() {
        String diagnostic = tfDiagnostic.getText().trim();
        String traitement = tfTraitement.getText().trim();
        String notes = taNotes.getText().trim();

        if (diagnostic.isEmpty() || traitement.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs incomplets");
            alert.setHeaderText(null);
            alert.setContentText("Le diagnostic et le traitement sont obligatoires.");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT patient_id FROM rendez_vous WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, rendezVousId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    patientId = rs.getInt("patient_id");
                    // Vérifier ou créer le dossier médical pour le patient
                    int dossierId = getOrCreateDossierId(conn, patientId);
                    if (dossierId == -1) {
                        showError("Erreur lors de la création ou récupération du dossier médical.");
                        return;
                    }
                    insertConsultation(dossierId, medecinId, diagnostic, traitement, notes);
                } else {
                    showError("Aucun rendez-vous trouvé.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout de la consultation.");
        }
    }

    // Méthode pour vérifier ou créer un dossier médical
    private int getOrCreateDossierId(Connection conn, int patientId) {
        String query = "SELECT id FROM dossiers_medicaux WHERE patient_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si le dossier n'existe pas, le créer
        String insert = "INSERT INTO dossiers_medicaux (patient_id) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, patientId);
            ps.executeUpdate();

            // Récupérer l'ID généré pour le dossier
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Méthode pour insérer la consultation dans la base de données
    private void insertConsultation(int dossierId, int medecinId, String diagnostic, String traitement, String notes) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String insert = "INSERT INTO consultations (dossier_id, medecin_id, date_consultation, diagnostic, traitement, notes) VALUES (?, ?, NOW(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, dossierId);
                ps.setInt(2, medecinId);
                ps.setString(3, diagnostic);
                ps.setString(4, traitement);
                ps.setString(5, notes);
                ps.executeUpdate();
            }

            // Fermer la fenêtre actuelle après l'ajout
            Stage stage = (Stage) btnAjouterConsultation.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'insertion de la consultation.");
        }
    }

    // Méthode pour afficher un message d'erreur
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour annuler l'ajout et fermer la fenêtre
    @FXML
    private void annuler() {
        Stage stage = (Stage) btnAjouterConsultation.getScene().getWindow();
        stage.close();
    }
}
