import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.security.MessageDigest;

public class AdminUserManagement extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private Connection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminUserManagement().setVisible(true));
    }

    public AdminUserManagement() {
        setTitle("Gestion des Utilisateurs - Admin");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Connexion
        connectToDatabase();

        // Table
        model = new DefaultTableModel(new Object[]{"ID", "Nom", "Prénom", "Email", "Rôle"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Boutons
        JButton addBtn = new JButton("Ajouter");
        JButton editBtn = new JButton("Modifier");
        JButton deleteBtn = new JButton("Supprimer");
        JButton refreshBtn = new JButton("Actualiser");

        JPanel panel = new JPanel();
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        panel.add(refreshBtn);
        add(panel, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> showUserDialog(false));
        editBtn.addActionListener(e -> showUserDialog(true));
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadUsers());

        // Chargement initial
        loadUsers();
    }
    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/hopital";  // Remplacez par l'URL de votre base de données
        String user = "root";  // Remplacez par votre nom d'utilisateur MySQL
        String password = "Meryemechiguerr";  // Remplacez par votre mot de passe MySQL

        try {
            // Chargement du driver MySQL (pour les versions anciennes de Java)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Tentative de connexion à la base de données
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie à la base de données !");

        } catch (ClassNotFoundException e) {
            // En cas d'erreur liée au chargement du driver
            showError("Erreur de chargement du driver MySQL: " + e.getMessage());
        } catch (SQLException e) {
            // En cas d'erreur SQL
            showError("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }


    private void loadUsers() {
        model.setRowCount(0);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM utilisateurs")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role")
                });
            }
        } catch (SQLException e) {
            showError("Erreur chargement: " + e.getMessage());
        }
    }
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            showError("Erreur de hachage du mot de passe : " + e.getMessage());
            return null;
        }
    }


    private void showUserDialog(boolean editMode) {
        int selectedRow = table.getSelectedRow();
        if (editMode && selectedRow == -1) {
            showError("Sélectionnez un utilisateur à modifier.");
            return;
        }

        JTextField nomField = new JTextField(editMode ? model.getValueAt(selectedRow, 1).toString() : "", 20);
        JTextField prenomField = new JTextField(editMode ? model.getValueAt(selectedRow, 2).toString() : "", 20);
        JTextField emailField = new JTextField(editMode ? model.getValueAt(selectedRow, 3).toString() : "", 20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"admin", "medecin", "receptionniste", "patient"});

        if (editMode) {
            roleBox.setSelectedItem(model.getValueAt(selectedRow, 4).toString());
        }

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Nom:"));
        panel.add(nomField);
        panel.add(new JLabel("Prénom:"));
        panel.add(prenomField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Mot de passe:"));
        panel.add(passwordField);
        panel.add(new JLabel("Rôle:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel,
                editMode ? "Modifier Utilisateur" : "Ajouter Utilisateur", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String role = roleBox.getSelectedItem().toString();

            try {
                if (editMode) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    String query = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, role = ?" +
                            (password.isEmpty() ? "" : ", mot_de_passe = ?") + " WHERE id = ?";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, nom);
                    stmt.setString(2, prenom);
                    stmt.setString(3, email);
                    stmt.setString(4, role);
                    int index = 5;
                    if (!password.isEmpty()) {
                        stmt.setString(index++, hashPassword(password));
<<<<<<< HEAD
=======

>>>>>>> 4e2887cf27a69e1c9b2b01e71ef268bf36467db4
                    }
                    stmt.setInt(index, id);
                    stmt.executeUpdate();
                } else {
                    // Ajouter l'utilisateur à la table utilisateurs
                    PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)");
                    stmt.setString(1, nom);
                    stmt.setString(2, prenom);
                    stmt.setString(3, email);
                    stmt.setString(4, hashPassword(password));
<<<<<<< HEAD
=======

>>>>>>> 4e2887cf27a69e1c9b2b01e71ef268bf36467db4
                    stmt.setString(5, role);
                    stmt.executeUpdate();

                    // Récupérer l'ID de l'utilisateur nouvellement ajouté
                    Statement stmtId = connection.createStatement();
                    ResultSet rs = stmtId.executeQuery("SELECT LAST_INSERT_ID()");
                    if (rs.next()) {
                        int newUserId = rs.getInt(1);

                        // Si le rôle est patient, ajouter dans la table patients
                        if (role.equals("patient")) {
                            PreparedStatement patientStmt = connection.prepareStatement(
                                    "INSERT INTO patients (id, antecedents) VALUES (?, ?)");
                            patientStmt.setInt(1, newUserId);
                            patientStmt.setString(2, ""); // Valeur vide pour les antécédents, à modifier selon l'entrée
                            patientStmt.executeUpdate();
                        }
                        // Si le rôle est médecin, ajouter dans la table medecins
                        else if (role.equals("medecin")) {
                            PreparedStatement medecinStmt = connection.prepareStatement(
                                    "INSERT INTO medecins (id, specialite) VALUES (?, ?)");
                            medecinStmt.setInt(1, newUserId);
                            medecinStmt.setString(2, ""); // Valeur vide pour la spécialité, à modifier selon l'entrée
                            medecinStmt.executeUpdate();
                        }
                    }
                }
                loadUsers();
            } catch (SQLException ex) {
                showError("Erreur : " + ex.getMessage());
            }
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Sélectionnez un utilisateur à supprimer.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de l'utilisateur ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) model.getValueAt(selectedRow, 0);
            try {
                PreparedStatement stmt = connection.prepareStatement("DELETE FROM utilisateurs WHERE id = ?");
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadUsers();
            } catch (SQLException e) {
                showError("Erreur suppression : " + e.getMessage());
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}