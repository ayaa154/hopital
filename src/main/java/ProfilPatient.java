import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProfilPatient extends JFrame {
    private JTextField txtNom, txtPrenom, txtEmail, txtTelephone, txtAdresse;
    private JTextArea txtAntecedents;
    private JButton btnUpdate, btnClose;
    private Connection connection;
    private int patientId;

    // Ajout d'un panel de menu vertical
    private JPanel menuPanel;

    public ProfilPatient(int patientId) {
        this.patientId = patientId;
        initializeUI();
        connectToDatabase();
        loadPatientInfo();
    }

    private void initializeUI() {
        setTitle("Profil Patient");
        setSize(800, 600);  // Augmenter la taille de la fenêtre pour accueillir le menu
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel de menu vertical
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        menuPanel.setBackground(Color.LIGHT_GRAY);

        // Ajouter les éléments du menu
        JButton btnProfilPatient = new JButton("Profil Patient");
        btnProfilPatient.addActionListener(e -> JOptionPane.showMessageDialog(this, "Profil Patient"));
        menuPanel.add(btnProfilPatient);

        JButton btnMesRendezVous = new JButton("Mes Rendez-vous");
        btnMesRendezVous.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mes Rendez-vous"));
        menuPanel.add(btnMesRendezVous);

        JButton btnDossierMedical = new JButton("Dossier Médical");
        btnDossierMedical.addActionListener(e -> JOptionPane.showMessageDialog(this, "Dossier Médical"));
        menuPanel.add(btnDossierMedical);

        // Ajout du menu à la fenêtre principale
        add(menuPanel, BorderLayout.WEST);

        // Panel principal pour les informations du patient
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Profil Patient");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.add(new JLabel("Nom:"));
        txtNom = new JTextField();
        formPanel.add(txtNom);

        formPanel.add(new JLabel("Prénom:"));
        txtPrenom = new JTextField();
        formPanel.add(txtPrenom);

        formPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        formPanel.add(txtEmail);

        formPanel.add(new JLabel("Téléphone:"));
        txtTelephone = new JTextField();
        formPanel.add(txtTelephone);

        formPanel.add(new JLabel("Adresse:"));
        txtAdresse = new JTextField();
        formPanel.add(txtAdresse);

        panel.add(formPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Antécédents:"));
        txtAntecedents = new JTextArea(5, 20);
        txtAntecedents.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(txtAntecedents);
        panel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        btnUpdate = new JButton("Mettre à jour");
        btnUpdate.addActionListener(e -> updatePatientInfo());
        buttonPanel.add(btnUpdate);

        btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);

        panel.add(buttonPanel);
        add(panel, BorderLayout.CENTER);
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost/hopital",
                    "root",
                    "Meryemechiguerr");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion à la base de données: " + e.getMessage());
            dispose();
        }
    }

    private void loadPatientInfo() {
        String query = "SELECT u.nom, u.prenom, u.email, u.telephone, u.adresse, p.antecedents " +
                "FROM utilisateurs u " +
                "JOIN patients p ON u.id = p.id " +
                "WHERE u.id = ?";  // Recherche par ID d'utilisateur
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    txtNom.setText(rs.getString("nom"));
                    txtPrenom.setText(rs.getString("prenom"));
                    txtEmail.setText(rs.getString("email"));
                    txtTelephone.setText(rs.getString("telephone"));
                    txtAdresse.setText(rs.getString("adresse"));
                    txtAntecedents.setText(rs.getString("antecedents"));
                } else {
                    JOptionPane.showMessageDialog(this, "Patient non trouvé !");
                    dispose();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + e.getMessage());
        }
    }

    private void updatePatientInfo() {
        if (!validateFields()) return;

        String query = "UPDATE utilisateurs u JOIN patients p ON u.id = p.id SET " +
                "u.nom = ?, u.prenom = ?, u.email = ?, u.telephone = ?, " +
                "u.adresse = ?, p.antecedents = ? WHERE u.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, txtNom.getText());
            stmt.setString(2, txtPrenom.getText());
            stmt.setString(3, txtEmail.getText());
            stmt.setString(4, txtTelephone.getText());
            stmt.setString(5, txtAdresse.getText());
            stmt.setString(6, txtAntecedents.getText());
            stmt.setInt(7, patientId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Mise à jour réussie !");
            } else {
                JOptionPane.showMessageDialog(this, "Aucune modification effectuée.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                txtEmail.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Les champs Nom, Prénom et Email sont obligatoires");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProfilPatient(1).setVisible(true));
    }
}
