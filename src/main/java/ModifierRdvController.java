import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class ModifierRdvController {

    @FXML private TextField rdvIdField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;

    private RendezVousController rendezVousController;

    public void setRendezVousController(RendezVousController controller) {
        this.rendezVousController = controller;
    }

    @FXML
    public void modifierRdv() {
        try {
            int rdvId = Integer.parseInt(rdvIdField.getText());
            LocalDate date = datePicker.getValue();
            String heure = heureCombo.getValue();

            if (date == null || heure == null) {
                showAlert("Veuillez sélectionner une date et une heure.");
                return;
            }

            String dateHeure = date + " " + heure;

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
            String sql = "UPDATE rendez_vous SET date_heure = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dateHeure);
            stmt.setInt(2, rdvId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Rendez-vous modifié !");
                rendezVousController.rafraichirTableau();
                rendezVousController.revenirVuePrincipale();
            } else {
                showAlert("Aucun RDV trouvé avec cet ID.");
            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            showAlert("ID invalide !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur SQL.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RendezVous.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rdvIdField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du retour à la vue principale.");
        }
    }


}
