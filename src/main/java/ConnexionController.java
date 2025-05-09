import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class ConnexionController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void verifierConnexion() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Veuillez remplir tous les champs");
            return;
        }

        // Utilisation de try-with-resources pour une meilleure gestion des ressources
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = SHA2(?, 256)")) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    int userId = rs.getInt("id");
                    handleUserRole(role, userId);
                } else {
                    showAlert("Email ou mot de passe incorrect !");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur inattendue: " + e.getMessage());
        }
    }

    private void handleUserRole(String role, int userId) throws Exception {
        switch (role.toLowerCase()) {
            case "receptionniste":
                loadAndShowView("AccueilReceptionniste.fxml", "Accueil Réceptionniste", null);
                break;

            case "patient":
                FXMLLoader loaderPatient = loadAndShowView("profil_patient.fxml", "Page Patient", null);
                ProfilPatientController patientController = loaderPatient.getController();
                patientController.setUserId(userId);
                break;

            case "admin":
                loadAndShowView("admin_user_management.fxml", "Gestion Utilisateurs (Admin)", null);
                break;

            case "medecin":
                FXMLLoader loaderMedecin = loadAndShowView("medecin_dashboard.fxml", "Tableau de bord Médecin", null);
                MedecinDashboardController medecinController = loaderMedecin.getController();
                medecinController.setMedecinId(userId); // << C’est ici qu’on passe l’ID
                break;

            default:
                showAlert("Rôle non pris en charge.");
                return;
        }

        // Fermer la fenêtre actuelle après redirection
        ((Stage) emailField.getScene().getWindow()).close();
    }


    private FXMLLoader loadAndShowView(String fxmlFile, String title, Object controller) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        if (controller != null) {
            loader.setController(controller);
        }
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
        return loader;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}