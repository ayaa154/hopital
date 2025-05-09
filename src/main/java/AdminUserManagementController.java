import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Optional;

public class AdminUserManagementController {

    @FXML private TableView<User> table;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    private Node mainContent;

    @FXML
    private BorderPane rootAdminPane;

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserForm.fxml"));
            Parent form = loader.load();

            UserFormController controller = loader.getController();

            mainContent = rootAdminPane.getCenter();
            Node topContent = rootAdminPane.getTop(); // <- sauvegarde le top

            rootAdminPane.setTop(null); // <- cache le titre

            controller.setUser(null, false, (savedUser, isEdit) -> {
                if (savedUser != null) {
                    insertUserIntoDatabase(savedUser);
                    loadUsers();
                }
                rootAdminPane.setCenter(mainContent);
                rootAdminPane.setTop(topContent); // <- restaure le titre
            });

            rootAdminPane.setCenter(form);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout de l'utilisateur.");
        }
    }



    // (Optionnel) Colonne mot de passe
    // @FXML private TableColumn<User, String> motDePasseColumn;

    private ObservableList<User> users;
    private Connection connection;

    public void initialize() {
        users = FXCollections.observableArrayList();
        table.setItems(users);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));


        // motDePasseColumn.setCellValueFactory(new PropertyValueFactory<>("motDePasse")); // optionnel

        connectToDatabase();
        loadUsers();
    }

    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/hopital";
        String user = "root";
        String password = "a!y!a!boutahli12";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            showError("Erreur de connexion : " + e.getMessage());
        }
    }

    private void loadUsers() {
        users.clear();
        String query = "SELECT * FROM utilisateurs";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("mot_de_passe") // colonne à ajouter en BDD si pas encore
                ));
            }
        } catch (SQLException e) {
            showError("Erreur de chargement des utilisateurs : " + e.getMessage());
        }
    }



    @FXML
    private void handleEdit() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            chargerFormulaireUtilisateur(true, selected);
        } else {
            showError("Sélectionnez un utilisateur.");
        }
    }
    private void chargerFormulaireUtilisateur(boolean edit, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserForm.fxml"));
            Parent form = loader.load();

            UserFormController controller = loader.getController();
            mainContent = rootAdminPane.getCenter();       // sauvegarde la vue principale
            Node topContent = rootAdminPane.getTop();      // sauvegarde le titre

            rootAdminPane.setTop(null);                    // cache le titre
            rootAdminPane.setCenter(form);                 // affiche le formulaire

            controller.setUser(user, edit, (savedUser, isEdit) -> {
                if (savedUser == null) {
                    rootAdminPane.setCenter(mainContent);  // retour sans enregistrement
                    rootAdminPane.setTop(topContent);      // restaure le titre
                    return;
                }

                if (isEdit) updateUserInDatabase(savedUser);
                else insertUserIntoDatabase(savedUser);

                loadUsers();
                rootAdminPane.setCenter(mainContent);      // retourne à la table
                rootAdminPane.setTop(topContent);          // restaure le titre
            });

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur ouverture du formulaire.");
        }
    }

    private void ouvrirFormulaireUtilisateur(boolean edit, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(user, edit, (savedUser, isEdit) -> {
                // Tu peux ici sauvegarder vers la base avec hash mot de passe si nécessaire
                if (isEdit) {
                    updateUserInDatabase(savedUser);
                } else {
                    insertUserIntoDatabase(savedUser);
                }
                loadUsers(); // refresh
            });

            Stage stage = new Stage();
            stage.setTitle(edit ? "Modifier Utilisateur" : "Ajouter Utilisateur");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l’ouverture du formulaire.");
        }
    }
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hachage", e);
        }
    }
    private void insertUserIntoDatabase(User user) {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, role, mot_de_passe) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setString(5, hashPassword(user.getMotDePasse()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout : " + e.getMessage());
        }
    }
    private void updateUserInDatabase(User user) {
        try {
            String sql;
            if (user.getMotDePasse() == null || user.getMotDePasse().isEmpty()) {
                sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, role = ? WHERE id = ?";
            } else {
                sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, role = ?, mot_de_passe = ? WHERE id = ?";
            }

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getNom());
                stmt.setString(2, user.getPrenom());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getRole());

                if (user.getMotDePasse() == null || user.getMotDePasse().isEmpty()) {
                    stmt.setInt(5, user.getId());
                } else {
                    stmt.setString(5, hashPassword(user.getMotDePasse()));
                    stmt.setInt(6, user.getId());
                }

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }






    @FXML
    private void seDeconnecter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Connexion.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la déconnexion.");
        }
    }




    @FXML
    private void handleDelete(ActionEvent event) {
        User selectedUser = table.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Sélectionnez un utilisateur à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteUser(selectedUser.getId());
            loadUsers();
        }
    }

    private void deleteUser(int userId) {
        String query = "DELETE FROM utilisateurs WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("Erreur de suppression : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
