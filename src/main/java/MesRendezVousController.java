import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.*;

public class MesRendezVousController {

    @FXML
    private TableView<RendezvousPatient> rendezVousTable;

    @FXML
    private TableColumn<RendezvousPatient, String> dateColumn;

    @FXML
    private TableColumn<RendezvousPatient, String> motifColumn;

    @FXML
    private TableColumn<RendezvousPatient, String> statutColumn;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        loadRendezVous();
    }

    private void loadRendezVous() {
        ObservableList<RendezvousPatient> rendezVousList = FXCollections.observableArrayList();

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");

            String query = "SELECT r.date_heure, r.motif, r.statut " +
                    "FROM rendez_vous r " +
                    "WHERE r.patient_id = ? " +
                    "ORDER BY r.date_heure DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dateHeure = rs.getTimestamp("date_heure").toString();
                String motif = rs.getString("motif");
                String statut = rs.getString("statut");

                rendezVousList.add(new RendezvousPatient(dateHeure, motif, statut));
            }

            dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
            motifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
            statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

            rendezVousTable.setItems(rendezVousList);

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des rendez-vous");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleRetourButton() {
        System.out.println("Retour à la page précédente");
    }

    public void initialize() {
        rendezVousTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }
        });
    }

    @FXML
    private void handleRendezVousButton() {
        System.out.println("Affichage des rendez-vous");
    }
    @FXML

    private void handleProfilButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profil_patient.fxml"));
            Parent root = loader.load(); // <-- correction ici
            ProfilPatientController controller = loader.getController();
            controller.setUserId(userId);
            Stage stage = (Stage) rendezVousTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du profil.");
        }
    }
    @FXML
    private void handleDossierButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medical.fxml"));
            Parent root = loader.load();
            DossierMedicalController controller = loader.getController();
            controller.setUserId(userId);
            Stage stage = (Stage) rendezVousTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du dossier médical.");
        }
    }
}
