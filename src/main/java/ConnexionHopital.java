import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class ConnexionHopital extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public ConnexionHopital() {
        setTitle("Connexion - Gestion d'Hôpital");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel emailLabel = new JLabel("Email :");
        emailLabel.setBounds(50, 40, 100, 30);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(150, 40, 180, 30);
        add(emailField);

        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setBounds(50, 90, 100, 30);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 90, 180, 30);
        add(passwordField);

        loginButton = new JButton("Connexion");
        loginButton.setBounds(130, 150, 120, 30);
        add(loginButton);

        loginButton.addActionListener(e -> verifierConnexion());

        setVisible(true);
    }

    private void verifierConnexion() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Utiliser try-with-resources pour une gestion correcte des ressources
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/hopital?useSSL=false&serverTimezone=UTC",
                "root",
                "ayabell2003&")) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = SHA2(?, 256)"
            );
            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    int userId = rs.getInt("id");

                    // Enregistrer l'ID du médecin dans la session
                    if (role.equalsIgnoreCase("medecin")) {
                        Session.setMedecinId(userId);
                    }

                    JOptionPane.showMessageDialog(this, "Connexion réussie ! Rôle : " + role);
                    dispose(); // Ferme la fenêtre de connexion

                    // Rediriger l'utilisateur vers la bonne interface
                    switch (role.toLowerCase()) {
                        case "admin":
                            new AdminUserManagement().setVisible(true);
                            break;
                        case "medecin":
                            dispose(); // Ferme la fenêtre Swing
                            MedecinApp.lancer(userId); // Démarre JavaFX proprement
                            break;



                        case "receptionniste":
                            JOptionPane.showMessageDialog(null, "Interface réceptionniste (à implémenter)");
                            break;
                        case "patient":
                            new ProfilPatient(userId).setVisible(true);
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "Rôle inconnu !");
                            break;
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Identifiants incorrects ou utilisateur inexistant.\n" +
                                    "Veuillez vérifier votre email et mot de passe.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur technique lors de la connexion :\n" + e.getMessage());
        }
    }


    public static void main(String[] args) {
        // Charger le driver JDBC
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Driver JDBC non trouvé !");
            return;
        }

        SwingUtilities.invokeLater(() -> new ConnexionHopital());
    }
}
