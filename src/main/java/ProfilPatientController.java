import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ProfilPatientController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateNaissanceField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseField;
    @FXML private PasswordField motDePasseField;  // Champ mot de passe
    @FXML private Button modifierButton;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        loadPatientData();
    }

    private void loadPatientData() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT nom, prenom, email, date_naissance, telephone, adresse " +
                            "FROM utilisateurs WHERE id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                emailField.setText(rs.getString("email"));
                if (rs.getDate("date_naissance") != null) {
                    dateNaissanceField.setValue(rs.getDate("date_naissance").toLocalDate());
                }
                telephoneField.setText(rs.getString("telephone") != null ? rs.getString("telephone") : "");
                adresseField.setText(rs.getString("adresse") != null ? rs.getString("adresse") : "");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des données du patient");
        }
    }

    @FXML
    private void handleModifierButton() {
        // Récupérer les nouvelles valeurs des champs
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        LocalDate dateNaissance = dateNaissanceField.getValue();
        String telephone = telephoneField.getText();
        String adresse = adresseField.getText();
        String nouveauMotDePasse = motDePasseField.getText(); // Nouveau mot de passe

        // Validation des données (peut être améliorée)
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            showAlert("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Hachage du mot de passe si un nouveau mot de passe est fourni
        String motDePasseHache = null;
        if (!nouveauMotDePasse.isEmpty()) {
            motDePasseHache = PasswordUtil.hashPassword(nouveauMotDePasse);
        }

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "Meryemechiguerr");

            String updateQuery = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, date_naissance = ?, telephone = ?, adresse = ?";

            // Si un mot de passe est fourni, on l'ajoute à la requête de mise à jour
            if (motDePasseHache != null) {
                updateQuery += ", mot_de_passe = ?";
            }

            updateQuery += " WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, email);
            stmt.setDate(4, (dateNaissance != null) ? Date.valueOf(dateNaissance) : null);
            stmt.setString(5, telephone);
            stmt.setString(6, adresse);

            // Si un mot de passe a été saisi, on l'ajoute à la requête
            if (motDePasseHache != null) {
                stmt.setString(7, motDePasseHache); // Mot de passe haché
                stmt.setInt(8, userId);  // ID de l'utilisateur
            } else {
                stmt.setInt(7, userId);  // ID de l'utilisateur
            }

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                showAlert("Données mises à jour avec succès !");
            } else {
                showAlert("Erreur lors de la mise à jour des données.");
            }

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de la mise à jour des données.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleRendezVousButton() {
        try {
            // Charger le fichier FXML de la page MesRendezVous
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesRendezVous.fxml"));
            BorderPane root = loader.load();  // Charger un BorderPane ici

            // Obtenir le contrôleur de la page des rendez-vous
            MesRendezVousController rendezVousController = loader.getController();
            rendezVousController.setUserId(userId);  // Passer l'ID du patient pour charger ses rendez-vous

            // Créer la nouvelle scène
            Scene scene = new Scene(root);

            // Obtenir la fenêtre (Stage) actuelle et la mettre à jour
            Stage stage = (Stage) modifierButton.getScene().getWindow();
            stage.setScene(scene);  // Mettre à jour la scène avec la nouvelle page
            stage.show();  // Afficher la nouvelle scène

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des rendez-vous.");
        }
    }





    @FXML
    private void handleProfilButton() {
        // Logique pour afficher le profil du patient
        System.out.println("Affichage du profil");
    }
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Connexion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Connexion");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) modifierButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    @FXML
    private void handlePrescriptionButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PrescriptionPatient.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur de la page PrescriptionPatient
            PrescriptionPatientController prescriptionPatientController = loader.getController();
            prescriptionPatientController.setUserId(userId);  // Passer l'ID du patient

            // Créer et afficher la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage = (Stage) modifierButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void handleDossierButton() {
        try {
            // Charger le fichier FXML de la page Dossier Médical
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medical.fxml"));
            BorderPane root = loader.load();  // Charger un BorderPane ici


            // Obtenir le contrôleur de la page Dossier Médical
            DossierMedicalController dossierMedicalController = loader.getController();
            dossierMedicalController.setUserId(userId);  // Passer l'ID du patient pour charger ses informations médicales

            // Créer la nouvelle scène
            Scene scene = new Scene(root);

            // Obtenir la fenêtre (Stage) actuelle et la mettre à jour
            Stage stage = (Stage) modifierButton.getScene().getWindow();
            stage.setScene(scene);  // Mettre à jour la scène avec la nouvelle page
            stage.show();  // Afficher la nouvelle scène

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du dossier médical.");
        }
    }



}
