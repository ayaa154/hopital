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

import java.sql.*;

public class RendezVousController {

    @FXML
    private TableView<RendezVous> tableRdv;
    @FXML
    private TableColumn<RendezVous, Integer> idCol;
    @FXML
    private TableColumn<RendezVous, String> patientCol;
    @FXML
    private TableColumn<RendezVous, String> medecinCol;
    @FXML
    private TableColumn<RendezVous, String> dateCol;
    @FXML
    private TableColumn<RendezVous, String> motifCol;
    @FXML
    private TableColumn<RendezVous, String> statutCol;

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
            e.printStackTrace();
        }
    }

    @FXML
    private BorderPane rootPane;


    @FXML
    public void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilReceptionniste.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRdv.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutRdv.fxml"));
            Parent formAjout = loader.load();

            AjoutRdvController controller = loader.getController();
            controller.setRendezVousController(this);

            rootPane.setTop(null); // masque la ToolBar "Retour au menu"
            rootPane.setCenter(formAjout);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }



    @FXML
    public void modifierRdv() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Modification de RDV à implémenter", ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void annulerRdv() {
        RendezVous selection = tableRdv.getSelectionModel().getSelectedItem();

        if (selection == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un rendez-vous à annuler.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment annuler ce rendez-vous ?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirmation d'annulation");
        confirmation.showAndWait();

        if (confirmation.getResult() == ButtonType.YES) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12")) {
                String sql = "DELETE FROM rendez_vous WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selection.getId());
                stmt.executeUpdate();
                stmt.close();
                chargerDonneesRdv(); // met à jour le tableau
            } catch (Exception e) {
                e.printStackTrace();
                Alert erreur = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'annulation du rendez-vous.", ButtonType.OK);
                erreur.showAndWait();
            }
        }
    }


    @FXML
    public void rafraichirTableau() {
        chargerDonneesRdv(); // Recharge les données depuis la base
    }
    public void revenirVuePrincipale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RendezVous.fxml"));
            Parent principal = loader.load();

            RendezVousController controller = loader.getController();
            controller.rootPane = this.rootPane; // pour conserver le même root
            rootPane.setCenter(principal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}