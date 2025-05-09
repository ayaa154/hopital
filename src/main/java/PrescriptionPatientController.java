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

    private ObservableList<Prescription> prescriptions;
    private int userId;  // On garde uniquement userId ici
    @FXML
    private Label welcomeLabel;
    @FXML
    public void initialize() {
        prescriptions = FXCollections.observableArrayList();  // Initialisation de la liste des prescriptions

        medicamentCol.setCellValueFactory(cellData -> cellData.getValue().medicamentProperty());
        posologieCol.setCellValueFactory(cellData -> cellData.getValue().posologieProperty());
        dureeCol.setCellValueFactory(cellData -> cellData.getValue().dureeProperty());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        medecinCol.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());

        loadPrescriptions();  // Chargement des prescriptions à l'initialisation
    }

    // Méthode pour définir l'ID de l'utilisateur
    public void setUserId(int userId) {
        this.userId = userId;  // Initialise userId
        loadPrescriptions();  // Recharger les prescriptions dès que l'ID utilisateur est défini
    }

    private void loadPrescriptions() {
        String url = "jdbc:mysql://localhost:3306/hopital";
        String user = "root";
        String password = "a!y!a!boutahli12";

        // Requête modifiée pour utiliser uniquement userId
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

        // Effacer les prescriptions précédentes avant de charger les nouvelles
        prescriptions.clear();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);  // Utilisation de userId
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
            showError("Erreur de base de données", "Impossible de charger les prescriptions.");
            e.printStackTrace();
        }
    }

    // Optionnel : méthode pour afficher une alerte d'erreur
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handledossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medical.fxml"));
            Parent root = loader.load();
            DossierMedicalController controller = loader.getController();

            controller.setUserId(userId); // Passe l'ID du patient au contrôleur
            Stage stage = (Stage) prescriptionTable.getScene().getWindow();
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
            Stage currentStage = (Stage) prescriptionTable.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d’ouvrir l’écran de connexion.");
        }
    }

    @FXML
    private void handleProfilButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profil_patient.fxml"));
            Parent root = loader.load();
            ProfilPatientController ctrl = loader.getController();
            ctrl.setUserId(userId);  // Passage de userId
            Stage stage = (Stage) prescriptionTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handlepre() {
        // Logique pour afficher le profil du patient
        System.out.println("Affichage du profil");
    }
    @FXML
    private void handleRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesRendezVous.fxml"));
            Parent root = loader.load();
            MesRendezVousController ctrl = loader.getController();
            ctrl.setUserId(userId);  // Passage de userId
            Stage stage = (Stage) prescriptionTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
