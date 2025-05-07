import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.*;

public class ReceptionnisteController {

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> idCol;
    @FXML private TableColumn<Patient, String> nomCol;
    @FXML private TableColumn<Patient, String> prenomCol;
    @FXML private TableColumn<Patient, String> emailCol;

    @FXML private VBox formulaireAjout;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private Connection conn;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "a!y!a!boutahli12");
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            showMessage("❌ Erreur de connexion : " + e.getMessage());
        }

        rafraichirListe();
    }

    @FXML
    public void rafraichirListe() {
        ObservableList<Patient> list = FXCollections.observableArrayList();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id, nom, prenom, email FROM utilisateurs WHERE role = 'patient'");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email")
                ));
            }
            patientTable.setItems(list);
            showMessage("✔️ Liste mise à jour");
        } catch (SQLException e) {
            showMessage("❌ " + e.getMessage());
        }
    }

    @FXML
    public void ajouterPatient() {
        formulaireAjout.setVisible(true);
        formulaireAjout.setManaged(true);
    }

    @FXML
    private void confirmerAjoutPatient() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showMessage("❌ Veuillez remplir tous les champs !");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, SHA2(?, 256), 'patient')"
        )) {
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.executeUpdate();

            showMessage("✅ Patient ajouté avec succès !");
            rafraichirListe();

            formulaireAjout.setVisible(false);
            formulaireAjout.setManaged(false);
            nomField.clear();
            prenomField.clear();
            emailField.clear();
            passwordField.clear();

        } catch (SQLException e) {
            showMessage("❌ Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    public void supprimerPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("❌ Sélectionnez un patient !");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM utilisateurs WHERE id = ?")) {
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            rafraichirListe();
            showMessage("🗑️ Patient supprimé.");
        } catch (SQLException e) {
            showMessage("❌ Erreur suppression : " + e.getMessage());
        }
    }

    @FXML
    public void annulerAjoutPatient() {
        formulaireAjout.setVisible(false);
        formulaireAjout.setManaged(false);

        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        showMessage("ℹ️ Ajout annulé.");
    }

    @FXML
    public void rechercherPatient() {
        String search = searchField.getText().trim();
        if (search.isEmpty()) {
            showMessage("❌ Veuillez entrer un nom ou un ID !");
            return;
        }

        ObservableList<Patient> list = FXCollections.observableArrayList();
        String query = "SELECT id, nom, prenom, email FROM utilisateurs WHERE role = 'patient' AND (LOWER(nom) LIKE ? OR id = ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + search.toLowerCase() + "%");

            try {
                stmt.setInt(2, Integer.parseInt(search));
            } catch (NumberFormatException e) {
                stmt.setInt(2, -1);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email")
                ));
            }

            patientTable.setItems(list);
            showMessage("🔍 Résultat mis à jour.");
        } catch (SQLException e) {
            showMessage("❌ Erreur recherche : " + e.getMessage());
        }
    }

    @FXML
    public void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilReceptionniste.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) patientTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil Réceptionniste");
            stage.show();
        } catch (Exception e) {
            showMessage("❌ Erreur retour au menu : " + e.getMessage());
        }
    }

    private void showMessage(String msg) {
        messageLabel.setText(msg);
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> messageLabel.setText(""));
        pause.play();
    }
}
