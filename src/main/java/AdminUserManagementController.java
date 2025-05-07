import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.Optional;

public class AdminUserManagementController {

    @FXML private TableView<User> table;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;

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

        connectToDatabase();
        loadUsers();
    }

    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/hopital";
        String user = "root";
        String password = "Meryemechiguerr";

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
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            showError("Erreur de chargement des utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        UserDialog.showUserDialog(false, connection, null);
        loadUsers();
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        User selectedUser = table.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Sélectionnez un utilisateur à modifier.");
            return;
        }
        UserDialog.showUserDialog(true, connection, selectedUser);
        loadUsers();
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
