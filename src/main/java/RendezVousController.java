import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.*;

public class RendezVousController {

    @FXML
    private TableView<RendezVous> tableRdv;
    @FXML private TableColumn<RendezVous, Integer> idCol;
    @FXML private TableColumn<RendezVous, String> patientCol;
    @FXML private TableColumn<RendezVous, String> medecinCol;
    @FXML private TableColumn<RendezVous, String> dateCol;
    @FXML private TableColumn<RendezVous, String> motifCol;
    @FXML private TableColumn<RendezVous, String> statutCol;
    @FXML private BorderPane rootPane;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;


    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patient"));
        medecinCol.setCellValueFactory(new PropertyValueFactory<>("medecin"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        motifCol.setCellValueFactory(new PropertyValueFactory<>("motif"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        chargerDonneesRdv();
    }

    private void chargerDonneesRdv() {
        ObservableList<RendezVous> liste = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
             Statement stmt = conn.createStatement()) {

            String sql = """
                SELECT rv.id, 
                       CONCAT(p.nom, ' ', p.prenom) AS patient,
                       CONCAT(m.nom, ' ', m.prenom) AS medecin,
                       rv.date_heure, rv.motif, rv.statut
                FROM rendez_vous rv
                JOIN utilisateurs p ON rv.patient_id = p.id
                JOIN utilisateurs m ON rv.medecin_id = m.id
                """;

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                liste.add(new RendezVous(
                        rs.getInt("id"),
                        rs.getString("patient"),
                        rs.getString("medecin"),
                        rs.getString("date_heure"),
                        rs.getString("motif"),
                        rs.getString("statut")
                ));
            }

            tableRdv.setItems(liste);
        } catch (SQLException e) {
            afficherMessage("❌ Erreur lors du chargement des rendez-vous.", true);
        }
    }

    @FXML
    public void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilReceptionniste.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRdv.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            afficherMessage("❌ Erreur lors du retour au menu.", true);
        }
    }

    @FXML
    public void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutRdv.fxml"));
            Parent formAjout = loader.load();

            AjoutRdvController controller = loader.getController();
            controller.setRendezVousController(this);

            // 👉 Vider tout sauf le center
            rootPane.setTop(null);
            rootPane.setLeft(null);
            rootPane.setRight(null);
            rootPane.setBottom(null);

            rootPane.setCenter(formAjout);
        } catch (Exception e) {
            afficherMessage("❌ Erreur ouverture formulaire ajout.", true);
        }
    }


    @FXML
    public void ouvrirFormulaireModification() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierRdv.fxml"));
            Parent modificationForm = loader.load();

            ModifierRdvController controller = loader.getController();
            controller.setRendezVousController(this);

            rootPane.setCenter(modificationForm);
        } catch (Exception e) {
            afficherMessage("❌ Erreur ouverture formulaire modification.", true);
        }
    }

    @FXML
    public void modifierRdv() {
        afficherMessage("ℹ️ Modification de RDV à implémenter.", false);
    }

    @FXML
    public void annulerRdv() {
        RendezVous selection = tableRdv.getSelectionModel().getSelectedItem();

        if (selection == null) {
            afficherMessage("⚠️ Sélectionnez un rendez-vous à annuler.", true);
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12")) {
            String sql = "DELETE FROM rendez_vous WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selection.getId());
            stmt.executeUpdate();
            stmt.close();
            chargerDonneesRdv();
            afficherMessage("✅ Rendez-vous annulé avec succès.", false);
        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'annulation.", true);
        }
    }

    @FXML
    public void rafraichirTableau() {
        chargerDonneesRdv();
        afficherMessage("✅ Données rafraîchies.", false);
    }

    public void revenirVuePrincipale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RendezVous.fxml"));
            Parent principal = loader.load();

            RendezVousController controller = loader.getController();
            controller.rootPane = this.rootPane; // garder le même border pane
            rootPane.setCenter(principal); // remettre seulement le contenu central
        } catch (Exception e) {
            afficherMessage("❌ Erreur retour vue principale.", true);
        }
    }


    @FXML
    public void rechercherRdv() {
        String filtre = searchField.getText().trim().toLowerCase();
        if (filtre.isEmpty()) {
            chargerDonneesRdv();
            return;
        }

        ObservableList<RendezVous> liste = FXCollections.observableArrayList();

        String sql = """
        SELECT rv.id, 
               CONCAT(p.nom, ' ', p.prenom) AS patient,
               CONCAT(m.nom, ' ', m.prenom) AS medecin,
               rv.date_heure, rv.motif, rv.statut
        FROM rendez_vous rv
        JOIN utilisateurs p ON rv.patient_id = p.id
        JOIN utilisateurs m ON rv.medecin_id = m.id
        WHERE LOWER(CONCAT(p.nom, ' ', p.prenom)) LIKE ?
           OR LOWER(CONCAT(m.nom, ' ', m.prenom)) LIKE ?
           OR rv.id = ?
    """;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + filtre + "%");
            stmt.setString(2, "%" + filtre + "%");

            try {
                stmt.setInt(3, Integer.parseInt(filtre));
            } catch (NumberFormatException e) {
                stmt.setInt(3, -1); // impossible ID
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                liste.add(new RendezVous(
                        rs.getInt("id"),
                        rs.getString("patient"),
                        rs.getString("medecin"),
                        rs.getString("date_heure"),
                        rs.getString("motif"),
                        rs.getString("statut")
                ));
            }

            tableRdv.setItems(liste);

        } catch (SQLException e) {
            afficherMessage("❌ Erreur recherche RDV.", true);
        }
    }


    private void afficherMessage(String message, boolean isError) {
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        messageLabel.setText(message);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> messageLabel.setText(""));
        pause.play();
    }
}
