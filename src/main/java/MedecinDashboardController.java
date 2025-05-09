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
    private int patientId;   // Ajout de patientId pour flexibilité
    private int userId;      // Ajout de userId pour flexibilité

    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<RendezVousMedecin> rendezvousTable;

    @FXML
    private TableColumn<RendezVousMedecin, String> colNom, colPrenom, colDateHeure, colMotif;

    // Setter pour le medecinId
    public void setMedecinId(int id) {
        this.medecinId = id;
        chargerInfosMedecin();
        chargerRendezvous();
    }

    // Setters pour gérer patientId et userId
    public void setPatientId(int id) {
        this.patientId = id;
        chargerRendezvous();  // Charger les rendez-vous pour un patient spécifique
    }

    public void setUserId(int id) {
        this.userId = id;
        chargerRendezvous();  // Charger les rendez-vous pour un utilisateur spécifique
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
            welcomeLabel.setText("Erreur de connexion à la base de données.");
            e.printStackTrace();
        }
    }

    @FXML
    private void seDeconnecter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Connexion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Connexion");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            ((Stage) welcomeLabel.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerRendezvous() {
        ObservableList<RendezVousMedecin> data = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT p.nom, p.prenom, r.date_heure, r.motif, r.id, r.patient_id " +
                            "FROM rendez_vous r " +
                            "JOIN utilisateurs p ON r.patient_id = p.id " +
                            "WHERE r.medecin_id = ? AND r.statut = 'planifie' " +
                            "AND WEEK(r.date_heure, 1) = WEEK(CURDATE(), 1) " +
                            "AND YEAR(r.date_heure) = YEAR(CURDATE()) " +
                            "ORDER BY r.date_heure"
            );
            // Vérification de quel ID utiliser
            if (patientId != 0) {
                stmt.setInt(1, patientId);  // Utilisation de patientId
            } else if (userId != 0) {
                stmt.setInt(1, userId);  // Utilisation de userId
            } else {
                stmt.setInt(1, medecinId);  // Utilisation de medecinId
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.add(new RendezVousMedecin(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("date_heure"),
                        rs.getString("motif"),
                        rs.getInt("id"),
                        rs.getInt("patient_id")
                ));
            }

            if (data.isEmpty()) {
                data.add(new RendezVousMedecin("Aucun", "rendez-vous", "cette semaine", "", -1, -1));
            }

            // Mettre à jour la TableView avec les données
            rendezvousTable.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirFenetrePrescription() {
        RendezVousMedecin selection = rendezvousTable.getSelectionModel().getSelectedItem();
        if (selection == null || selection.getId() == -1) {
            showAlert("Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous pour rédiger une prescription.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PrescriptionForm.fxml"));
            Parent root = loader.load();

            PrescriptionFormController controller = loader.getController();
            controller.setMedecinId(medecinId);
            controller.setRendezVousId(selection.getId());

            // Remplacer le contenu de la scène actuelle avec la nouvelle vue
            Stage stage = (Stage) welcomeLabel.getScene().getWindow(); // Récupérer la fenêtre actuelle
            stage.getScene().setRoot(root); // Remplacer le root de la scène par la nouvelle vue

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirFenetreConsultation() {
        RendezVousMedecin selection = rendezvousTable.getSelectionModel().getSelectedItem();
        if (selection == null || selection.getId() == -1) {
            showAlert("Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous pour ajouter une consultation.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AjouterConsultation.fxml"));
            Parent root = loader.load();

            ConsultationFormController controller = loader.getController();
            controller.setMedecinId(medecinId);
            controller.setRendezVousId(selection.getId());

            // Remplacer le contenu de la scène actuelle avec la nouvelle vue
            Stage stage = (Stage) welcomeLabel.getScene().getWindow(); // Récupérer la fenêtre actuelle
            stage.getScene().setRoot(root); // Remplacer le root de la scène par la nouvelle vue

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Nouvelle méthode pour voir le dossier médical du patient

    @FXML
    private void voirDossierMedical() {
        RendezVousMedecin selection = rendezvousTable.getSelectionModel().getSelectedItem();

        if (selection == null || selection.getId() == -1) {
            showAlert("Aucun rendez-vous sélectionné", "Veuillez sélectionner un rendez-vous pour voir le dossier médical.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ConsultationDossierView.fxml"));
            Parent root = loader.load();

            ConsultationDossierController controller = loader.getController();
            controller.setPatientId(selection.getPatientId());
            controller.setMedecinId(medecinId); // ✅ AJOUT ICI

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
