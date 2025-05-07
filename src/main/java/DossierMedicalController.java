import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;

public class DossierMedicalController {

    @FXML private Label lblNom, lblPrenom, lblAge;
    @FXML private TextArea txtAntecedents, txtPrescriptions;
    @FXML private TableView<Consultation> tableConsultations;
    @FXML private TableColumn<Consultation, String> dateColumn, medecinColumn, diagnosticColumn;

    private Connection connection;
    private int patientId;

    public static class Consultation {
        private final StringProperty date;
        private final StringProperty medecin;
        private final StringProperty diagnostic;

        public Consultation(String date, String medecin, String diagnostic) {
            this.date = new SimpleStringProperty(date);
            this.medecin = new SimpleStringProperty(medecin);
            this.diagnostic = new SimpleStringProperty(diagnostic);
        }

        public String getDate() { return date.get(); }
        public String getMedecin() { return medecin.get(); }
        public String getDiagnostic() { return diagnostic.get(); }

        public StringProperty dateProperty() { return date; }
        public StringProperty medecinProperty() { return medecin; }
        public StringProperty diagnosticProperty() { return diagnostic; }
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
        loadPatientData();  // Charger les données patient
        loadConsultations();  // Charger les consultations
        loadPrescriptions();  // Charger les prescriptions
    }

    @FXML
    public void initialize() {
        // Configuration des colonnes avec lambda (méthode moderne)
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        medecinColumn.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());
        diagnosticColumn.setCellValueFactory(cellData -> cellData.getValue().diagnosticProperty());
    }

    public void initData(int patientId) {
        this.patientId = patientId;
        connectToDatabase();
        loadPatientData();
        loadConsultations();
        loadPrescriptions();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost/hopital",
                    "root",
                    "Meryemechiguerr");
        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private void loadPatientData() {
        String query = "SELECT u.nom, u.prenom, u.date_naissance, p.antecedents " +
                "FROM utilisateurs u JOIN patients p ON u.id = p.id WHERE u.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Récupération sécurisée des données avec vérification de null
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                Date dateNaissance = rs.getDate("date_naissance");
                String antecedents = rs.getString("antecedents");

                // Conversion sécurisée de la date
                LocalDate birthDate = dateNaissance != null ? dateNaissance.toLocalDate() : LocalDate.now();
                int age = Period.between(birthDate, LocalDate.now()).getYears();

                // Mise à jour de l'interface utilisateur dans le thread JavaFX
                Platform.runLater(() -> {
                    lblNom.setText(nom != null ? nom : "Non renseigné");
                    lblPrenom.setText(prenom != null ? prenom : "Non renseigné");
                    lblAge.setText(age + " ans");
                    txtAntecedents.setText(antecedents != null ? antecedents : "Aucun antécédent");
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans loadPatientData: " + e.getMessage());
            showAlert("Erreur lors du chargement des données patient");
        }
    }

    private void loadConsultations() {
        ObservableList<Consultation> consultations = FXCollections.observableArrayList();

        String query = "SELECT c.date_consultation, CONCAT(m.prenom, ' ', m.nom) AS medecin, c.diagnostic " +
                "FROM consultations c JOIN medecins m ON c.medecin_id = m.id " +
                "WHERE c.dossier_id IN (SELECT id FROM dossiers_medicaux WHERE patient_id = ?) " +
                "ORDER BY c.date_consultation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Récupération sécurisée avec valeurs par défaut
                String date = rs.getTimestamp("date_consultation") != null
                        ? rs.getTimestamp("date_consultation").toLocalDateTime().toLocalDate().toString()
                        : "Date inconnue";

                String medecin = rs.getString("medecin") != null
                        ? rs.getString("medecin")
                        : "Médecin non spécifié";

                String diagnostic = rs.getString("diagnostic") != null
                        ? rs.getString("diagnostic")
                        : "Aucun diagnostic";

                consultations.add(new Consultation(date, medecin, diagnostic));
            }

            tableConsultations.setItems(consultations);
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans loadConsultations: " + e.getMessage());
            showAlert("Erreur lors du chargement des consultations");
        }
    }

    private void loadPrescriptions() {
        StringBuilder prescriptions = new StringBuilder();

        String query = "SELECT p.medicament, p.posologie, p.duree " +
                "FROM prescriptions p " +
                "JOIN consultations c ON p.consultation_id = c.id " +
                "WHERE c.dossier_id IN (SELECT id FROM dossiers_medicaux WHERE patient_id = ?) " +
                "AND c.date_consultation = (SELECT MAX(date_consultation) FROM consultations WHERE dossier_id IN (SELECT id FROM dossiers_medicaux WHERE patient_id = ?))";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                prescriptions.append("• ")
                        .append(rs.getString("medicament"))
                        .append(" - ")
                        .append(rs.getString("posologie"))
                        .append(" (")
                        .append(rs.getString("duree"))
                        .append(")\n");
            }

            txtPrescriptions.setText(prescriptions.toString());
        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des prescriptions");
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleProfilPatient() {
        // Navigation vers le profil patient
    }

    @FXML
    private void handleMesRendezVous() {
        // Navigation vers les rendez-vous
    }

    @FXML
    private void handleDossierMedical() {
        // Rafraîchir les données
        loadPatientData();
        loadConsultations();
        loadPrescriptions();
    }
}
