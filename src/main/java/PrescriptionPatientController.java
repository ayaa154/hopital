import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
public class PrescriptionPatientController {

    @FXML
    private TableView<Prescription> prescriptionTable;
    @FXML
    private TableColumn<Prescription, String> medicamentCol;
    @FXML
    private TableColumn<Prescription, String> posologieCol;
    @FXML
    private TableColumn<Prescription, String> dureeCol;
    @FXML
    private TableColumn<Prescription, String> dateCol;
    @FXML
    private TableColumn<Prescription, String> medecinCol;

    private ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();
    private int patientId; // Déclaré sans valeur initiale

    @FXML
    public void initialize() {
        medicamentCol.setCellValueFactory(cellData -> cellData.getValue().medicamentProperty());
        posologieCol.setCellValueFactory(cellData -> cellData.getValue().posologieProperty());
        dureeCol.setCellValueFactory(cellData -> cellData.getValue().dureeProperty());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        medecinCol.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());

        loadPrescriptions();
    }

    // Méthode pour définir l'ID du patient
    public void setPatientId(int patientId) {
        this.patientId = patientId;
        loadPrescriptions();  // Charger les prescriptions avec l'ID du patient
    }

    private void loadPrescriptions() {
        String url = "jdbc:mysql://localhost:3306/hopital";
        String user = "root";
        String password = "a!y!a!boutahli12";

        String query = """
        SELECT p.medicament, p.posologie, p.duree,
               rv.date_heure AS date_consultation,
               CONCAT(u.prenom, ' ', u.nom) AS medecin
        FROM prescriptions p
        JOIN rendez_vous rv ON p.rendez_vous_id = rv.id
        JOIN utilisateurs u ON p.medecin_id = u.id
        WHERE rv.patient_id = ?
        ORDER BY rv.date_heure DESC
    """;


        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);  // Utiliser l'ID du patient dynamique
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                prescriptions.add(new Prescription(
                        rs.getString("medicament"),
                        rs.getString("posologie"),
                        rs.getString("duree"),
                        rs.getString("date_consultation"),
                        rs.getString("medecin")
                ));
            }

            prescriptionTable.setItems(prescriptions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        switchScene("ProfilPatient.fxml");
    }

    @FXML
    private void handleRendezVous() {
        switchScene("MesRendezVous.fxml");
    }

    @FXML
    private void handleProfil() {
        switchScene("profil_patient.fxml");
    }

    @FXML
    private void handleDossier() {
        switchScene("dossier_medical.fxml");
    }

    private void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) prescriptionTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
