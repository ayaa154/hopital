import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;

public class DossierMedicalController {

    @FXML private TableView<DossierEntry> dossierTable;
    @FXML private TableColumn<DossierEntry, String> dateCol;
    @FXML private TableColumn<DossierEntry, String> diagnosticCol;
    @FXML private TableColumn<DossierEntry, String> traitementCol;
    @FXML private TableColumn<DossierEntry, String> medecinCol;

    private int userId;

    // 🔗 Méthode locale pour obtenir la connexion
    private Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost/hopital";
        String user = "root";
        String password = "a!y!a!boutahli12";
        return DriverManager.getConnection(url, user, password);
    }

    public void initialize() {
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        diagnosticCol.setCellValueFactory(cellData -> cellData.getValue().diagnosticProperty());
        traitementCol.setCellValueFactory(cellData -> cellData.getValue().traitementProperty());
        medecinCol.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());
    }

    public void setUserId(int id) {
        this.userId = id;
        loadDossierMedical(); // Charger les données
    }

    private void loadDossierMedical() {
        dossierTable.getItems().clear();

        try (Connection con = getConnection()) {

            String query = "SELECT c.date_consultation, c.diagnostic, c.traitement, u.nom AS medecin_nom, u.prenom AS medecin_prenom " +
                    "FROM consultations c " +
                    "JOIN dossiers_medicaux d ON c.dossier_id = d.id " +
                    "JOIN utilisateurs u ON c.medecin_id = u.id " +
                    "WHERE d.patient_id = ?"+
           " ORDER BY c.date_consultation DESC";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String date = rs.getString("date_consultation");
                String diagnostic = rs.getString("diagnostic");
                String traitement = rs.getString("traitement");
                String medecin = rs.getString("medecin_nom") + " " + rs.getString("medecin_prenom");

                dossierTable.getItems().add(new DossierEntry(date, diagnostic, traitement, medecin));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRendezVousButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesRendezVous.fxml"));
            Parent root = loader.load();
            MesRendezVousController ctrl = loader.getController();
            ctrl.setUserId(userId);
            Stage stage = (Stage) dossierTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfilButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profil_patient.fxml"));
            Parent root = loader.load();
            ProfilPatientController ctrl = loader.getController();
            ctrl.setUserId(userId);
            Stage stage = (Stage) dossierTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDossierButton() {
        // Rester sur la même page
    }
    @FXML
    private void handlePrescription() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PrescriptionPatient.fxml"));
            Parent root = loader.load(); // ✅ plus sûr
            PrescriptionPatientController controller = loader.getController();
            controller.setUserId(userId);  // Transmet l'ID du patient
            Stage stage = (Stage) dossierTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Connexion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Connexion");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) dossierTable.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }



}
