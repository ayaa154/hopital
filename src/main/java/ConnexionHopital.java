import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


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
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            // Connexion à la base MySQL
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/hopital", "root", "Meryemechiguerr");
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = SHA2(?, 256)"
            );
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("id"); // 🔹 Cette ligne récupère l'id du patient

                JOptionPane.showMessageDialog(this, "Connexion réussie ! Rôle : " + role);
                dispose(); // ferme la fenêtre actuelle

                switch (role.toLowerCase()) {
                    case "admin":
                        new AdminUserManagement().setVisible(true);
                        break;
                    case "medecin":
                        JOptionPane.showMessageDialog(null, "Redirection vers l'interface médecin (non encore implémentée)");
                        break;
                    case "receptionniste":
                        ReceptionnisteApp.lancer();
                        break;

                    case "patient":
                        new ProfilPatient(userId).setVisible(true); // Affiche le profil du patient connecté
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Rôle inconnu !");
                        break;
                }
            }
 else {
                JOptionPane.showMessageDialog(this, "Email ou mot de passe incorrect !");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base !");
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
