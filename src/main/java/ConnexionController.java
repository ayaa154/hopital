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
                        Main.switchScene("/AccueilReceptionniste.fxml"); // ✅ remplace la scène dans la même fenêtre
                        break;
                    case "patient":
                        // Par exemple : Main.switchScene("/ProfilPatient.fxml");
                        break;
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
