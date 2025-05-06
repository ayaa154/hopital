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
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = SHA2(?, 256)"
            );
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("id");

                switch (role.toLowerCase()) {
                    case "receptionniste":
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("AccueilReceptionniste.fxml"));
                        Parent root = loader.load();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Accueil Réceptionniste");
                        stage.show();
                        // Fermer la fenêtre actuelle
                        Stage currentStage = (Stage) emailField.getScene().getWindow();
                        currentStage.close();
                        break;
                    // Autres rôles si besoin...
                    default:
                        showAlert("Rôle non pris en charge.");
                        break;
                }
            } else {
                showAlert("Email ou mot de passe incorrect !");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de connexion !");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}
