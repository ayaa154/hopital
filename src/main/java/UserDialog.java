import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class UserDialog {

    public static void showUserDialog(boolean isEdit, Connection connection, User userToEdit) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier Utilisateur" : "Ajouter Utilisateur");

        Label nomLabel = new Label("Nom:");
        TextField nomField = new TextField();
        Label prenomLabel = new Label("Prénom:");
        TextField prenomField = new TextField();
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        Label roleLabel = new Label("Rôle:");
        TextField roleField = new TextField();
        Label motDePasseLabel = new Label("Mot de passe:");
        PasswordField motDePasseField = new PasswordField();

        if (isEdit && userToEdit != null) {
            nomField.setText(userToEdit.getNom());
            prenomField.setText(userToEdit.getPrenom());
            emailField.setText(userToEdit.getEmail());
            roleField.setText(userToEdit.getRole());
            // Ne pas pré-remplir le mot de passe pour la sécurité
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nomLabel, 0, 0); grid.add(nomField, 1, 0);
        grid.add(prenomLabel, 0, 1); grid.add(prenomField, 1, 1);
        grid.add(emailLabel, 0, 2); grid.add(emailField, 1, 2);
        grid.add(roleLabel, 0, 3); grid.add(roleField, 1, 3);
        grid.add(motDePasseLabel, 0, 4); grid.add(motDePasseField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new User(
                        isEdit ? userToEdit.getId() : 0,
                        nomField.getText(),
                        prenomField.getText(),
                        emailField.getText(),
                        roleField.getText(),
                        motDePasseField.getText()
                );
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            try {
                if (isEdit) {
                    if (user.getMotDePasse() == null || user.getMotDePasse().isEmpty()) {
                        // Pas de mise à jour du mot de passe
                        String query = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, role = ? WHERE id = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(query)) {
                            stmt.setString(1, user.getNom());
                            stmt.setString(2, user.getPrenom());
                            stmt.setString(3, user.getEmail());
                            stmt.setString(4, user.getRole());
                            stmt.setInt(5, user.getId());
                            stmt.executeUpdate();
                        }
                    } else {
                        // Mise à jour avec un nouveau mot de passe haché
                        String hashed = hashPassword(user.getMotDePasse());
                        String query = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, role = ?, mot_de_passe = ? WHERE id = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(query)) {
                            stmt.setString(1, user.getNom());
                            stmt.setString(2, user.getPrenom());
                            stmt.setString(3, user.getEmail());
                            stmt.setString(4, user.getRole());
                            stmt.setString(5, hashed);
                            stmt.setInt(6, user.getId());
                            stmt.executeUpdate();
                        }
                    }
                } else {
                    String hashed = hashPassword(user.getMotDePasse());
                    String query = "INSERT INTO utilisateurs (nom, prenom, email, role, mot_de_passe) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setString(1, user.getNom());
                        stmt.setString(2, user.getPrenom());
                        stmt.setString(3, user.getEmail());
                        stmt.setString(4, user.getRole());
                        stmt.setString(5, hashed);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'enregistrement : " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    // Hachage SHA-256
    private static String hashPassword(String password) {
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
}
