import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultationDossierController {

    private int patientId;
    private int medecinId;

    @FXML
    private TableView<DossierEntry> tableDossierMedical;

    @FXML
    private TableColumn<DossierEntry, String> colDateConsultation;

    @FXML
    private TableColumn<DossierEntry, String> colDiagnostic;

    @FXML
    private TableColumn<DossierEntry, String> colTraitement;

    @FXML
    private TableColumn<DossierEntry, String> colMedecin;

    @FXML
    private Label noRecordsLabel;

    @FXML
    public void initialize() {
        // Initialisation des colonnes de la TableView
        colDateConsultation.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colDiagnostic.setCellValueFactory(cellData -> cellData.getValue().diagnosticProperty());
        colTraitement.setCellValueFactory(cellData -> cellData.getValue().traitementProperty());
        colMedecin.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());
    }

    public void setPatientId(int id) {
        this.patientId = id;
        chargerDossierMedical();
    }

    public void setMedecinId(int id) {
        this.medecinId = id;
        System.out.println("Médecin ID défini: " + this.medecinId); // Debug
    }

    @FXML
    private void returnToDashboard() {
        try {
            if (this.medecinId <= 0) {
                showAlert("Erreur", "Aucun ID médecin défini.");
                return;
            }

            // Chargement du fichier FXML du dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("medecin_dashboard.fxml")); // ← remplace le chemin
            Parent root = loader.load();

            // Récupérer le contrôleur et transmettre l’ID médecin
            MedecinDashboardController controller = loader.getController();
            controller.setMedecinId(this.medecinId);

            // Changer la scène
            Stage currentStage = (Stage) tableDossierMedical.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le dashboard du médecin.\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur inattendue", e.getMessage());
        }
    }


    private void chargerDossierMedical() {
        tableDossierMedical.getItems().clear();

        if (patientId <= 0) {
            noRecordsLabel.setText("ID patient non valide");
            noRecordsLabel.setVisible(true);
            return;
        }

        final String DB_URL = "jdbc:mysql://localhost:3306/hopital";
        final String DB_USER = "root";
        final String DB_PASSWORD = "a!y!a!boutahli12";

        String sql = "SELECT c.date_consultation, c.diagnostic, c.traitement, u.nom, u.prenom " +
                "FROM consultations c " +
                "JOIN dossiers_medicaux d ON c.dossier_id = d.id " +
                "JOIN utilisateurs u ON c.medecin_id = u.id " +
                "WHERE d.patient_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            boolean hasRecords = false;

            while (rs.next()) {
                hasRecords = true;
                String date = rs.getString("date_consultation");
                String diagnostic = rs.getString("diagnostic");
                String traitement = rs.getString("traitement");
                String medecin = rs.getString("nom") + " " + rs.getString("prenom");

                tableDossierMedical.getItems().add(new DossierEntry(date, diagnostic, traitement, medecin));
            }

            noRecordsLabel.setText(hasRecords ? "" : "Aucun dossier médical trouvé");
            noRecordsLabel.setVisible(!hasRecords);

        } catch (SQLException e) {
            e.printStackTrace();
            noRecordsLabel.setText("Erreur de base de données");
            noRecordsLabel.setVisible(true);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}