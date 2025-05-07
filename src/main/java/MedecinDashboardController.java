import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class MedecinDashboardController {

    private int medecinId;

    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<RendezVous> rendezvousTable;

    @FXML
    private TableColumn<RendezVous, String> colNom, colPrenom, colDateHeure, colMotif;

    @FXML
    private Button btnPrescription;

    public void setMedecinId(int id) {
        this.medecinId = id;
        chargerInfosMedecin();
        chargerRendezvous();
    }

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDateHeure.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
    }

    @FXML
    private void rafraichirRendezvous() {
        chargerRendezvous();
    }

    private void chargerInfosMedecin() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT nom, prenom FROM utilisateurs WHERE id = ? AND role = 'medecin'");
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                welcomeLabel.setText("Bonjour Docteur " + nom.toUpperCase() + " " + prenom);
            } else {
                welcomeLabel.setText("Médecin introuvable");
            }

        } catch (SQLException e) {
            welcomeLabel.setText("Erreur de connexion");
            e.printStackTrace();
        }
    }

    private void chargerRendezvous() {
        ObservableList<RendezVous> data = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT p.nom, p.prenom, r.date_heure, r.motif, r.id " +
                            "FROM rendez_vous r " +
                            "JOIN utilisateurs p ON r.patient_id = p.id " +
                            "WHERE r.medecin_id = ? AND r.statut = 'planifie' " +
                            "AND WEEK(r.date_heure, 1) = WEEK(CURDATE(), 1) " +
                            "AND YEAR(r.date_heure) = YEAR(CURDATE()) " +
                            "ORDER BY r.date_heure"
            );
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.add(new RendezVous(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("date_heure"),  // Vous pouvez formater la date si nécessaire ici
                        rs.getString("motif"),
                        rs.getInt("id") // Ajout ID du rendez-vous
                ));
            }

            if (data.isEmpty()) {
                data.add(new RendezVous("Aucun", "rendez-vous", "cette semaine", "", -1));
            }

            rendezvousTable.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirFenetrePrescription() {
        RendezVous selection = rendezvousTable.getSelectionModel().getSelectedItem();
        if (selection == null || selection.getId() == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun rendez-vous sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un rendez-vous pour rédiger une prescription.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PrescriptionForm.fxml"));
            Parent root = loader.load();

            PrescriptionFormController controller = loader.getController();
            controller.setMedecinId(medecinId);
            controller.setRendezVousId(selection.getId());  // Passer l'ID du rendez-vous sélectionné

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Prescription");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
