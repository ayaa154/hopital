import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class ProfilPatientController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private TextArea txtAntecedents;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    private Connection connection;
    private int patientId;

    // Constantes pour les requêtes SQL
    private static final String SELECT_PATIENT_INFO_QUERY = "SELECT u.nom, u.prenom, u.email, u.telephone, u.adresse, p.antecedents FROM utilisateurs u JOIN patients p ON u.id = p.id WHERE u.id = ?";
    private static final String UPDATE_PATIENT_INFO_QUERY = "UPDATE utilisateurs u JOIN patients p ON u.id = p.id SET u.nom = ?, u.prenom = ?, u.email = ?, u.telephone = ?, u.adresse = ?, p.antecedents = ? WHERE u.id = ?";
    private static final String UPDATE_PASSWORD_QUERY = "UPDATE utilisateurs SET mot_de_passe = SHA2(?, 256) WHERE id = ?";

    // Méthode pour définir l'ID du patient
    public void setPatientId(int patientId) {
        this.patientId = patientId;
        loadPatientInfo(); // Charge les données immédiatement
    }

    @FXML
    public void initialize() {
        connectToDatabase(); // Établit la connexion à la base de données
    }

    // Connexion à la base de données
    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost/hopital",
                    "root",
                    "Meryemechiguerr");
            System.out.println("Connexion à la base de données réussie");
        } catch (SQLException e) {
            showAlert("Échec de la connexion à la base de données");
            e.printStackTrace();
        }
    }

    // Chargement des informations du patient
    private void loadPatientInfo() {
        if (connection == null) {
            showAlert("Pas de connexion à la base de données");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(SELECT_PATIENT_INFO_QUERY)) {
            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Récupération des données
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String email = rs.getString("email");
                    String telephone = rs.getString("telephone");
                    String adresse = rs.getString("adresse");
                    String antecedents = rs.getString("antecedents");

                    // Mise à jour de l'interface utilisateur
                    Platform.runLater(() -> {
                        txtNom.setText(nom != null ? nom : "");
                        txtPrenom.setText(prenom != null ? prenom : "");
                        txtEmail.setText(email != null ? email : "");
                        txtTelephone.setText(telephone != null ? telephone : "");
                        txtAdresse.setText(adresse != null ? adresse : "");
                        txtAntecedents.setText(antecedents != null ? antecedents : "");
                    });
                } else {
                    showAlert("Aucun patient trouvé avec cet ID");
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des données");
            e.printStackTrace();
        }
    }

    // Mise à jour des informations
    @FXML
    private void handleUpdate() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() || txtEmail.getText().isEmpty()) {
            showAlert("Nom, Prénom et Email sont obligatoires");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PATIENT_INFO_QUERY)) {
            stmt.setString(1, txtNom.getText());
            stmt.setString(2, txtPrenom.getText());
            stmt.setString(3, txtEmail.getText());
            stmt.setString(4, txtTelephone.getText());
            stmt.setString(5, txtAdresse.getText());
            stmt.setString(6, txtAntecedents.getText());
            stmt.setInt(7, patientId);

            int rowsUpdated = stmt.executeUpdate();
            showAlert(rowsUpdated > 0 ? "Informations mises à jour avec succès" : "Aucune modification effectuée");
        } catch (SQLException e) {
            showAlert("Erreur lors de la mise à jour");
            e.printStackTrace();
        }
    }

    // Changement de mot de passe
    @FXML
    private void handleChangePassword() {
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert("Les champs ne peuvent pas être vides");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert("Les mots de passe ne correspondent pas");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PASSWORD_QUERY)) {
            stmt.setString(1, newPass);
            stmt.setInt(2, patientId);

            int rowsUpdated = stmt.executeUpdate();
            showAlert(rowsUpdated > 0 ? "Mot de passe mis à jour" : "Échec de la mise à jour du mot de passe");

            if (rowsUpdated > 0) {
                txtNewPassword.clear();
                txtConfirmPassword.clear();
            }
        } catch (SQLException e) {
            showAlert("Erreur lors du changement de mot de passe");
            e.printStackTrace();
        }
    }

    // Fermeture de la fenêtre
    @FXML
    private void handleClose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    // Redirection vers d'autres pages
    @FXML private void handleProfilPatient() {
        changeScene("profil_patient.fxml", patientId);
    }

    @FXML private void handleMesRendezVous() {
        changeScene("MesRendezVous.fxml", patientId);
    }

    @FXML private void handleDossierMedical() {
        changeScene("DossierMedical.fxml", patientId);
    }

    private void changeScene(String fxmlFile, int patientId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ProfilPatientController) {
                ((ProfilPatientController) controller).setPatientId(patientId);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle scène");
            stage.show();

            Stage currentStage = (Stage) txtNom.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            showAlert("Erreur lors de l'ouverture de la nouvelle scène");
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
