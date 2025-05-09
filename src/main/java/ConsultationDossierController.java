import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultationDossierController {

    private int patientId;

    @FXML
    private TableView<DossierEntry> tableDossierMedical;

    @FXML
    private TableColumn<DossierEntry, String> colDateConsultation;

    @FXML
    private TableColumn<DossierEntry, String> colDiagnostic;

    @FXML
    private TableColumn<DossierEntry, String> colTraitement;

    @FXML
    private TableColumn<DossierEntry, String> colMedecin;

    @FXML
    private Label noRecordsLabel;  // Label pour afficher un message lorsqu'il n'y a pas de dossiers

    @FXML
    public void initialize() {
        // Initialisation des colonnes de la TableView avec les propriétés liées de DossierEntry
        colDateConsultation.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colDiagnostic.setCellValueFactory(cellData -> cellData.getValue().diagnosticProperty());
        colTraitement.setCellValueFactory(cellData -> cellData.getValue().traitementProperty());
        colMedecin.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());
    }

    public void setPatientId(int id) {
        this.patientId = id;
        chargerDossierMedical();
    }

    private void chargerDossierMedical() {
        // Vider la table avant de recharger
        tableDossierMedical.getItems().clear();

        if (patientId <= 0) {
            // Si patientId n'est pas valide, afficher un message
            System.out.println("ID patient non valide : " + patientId);
            return;
        }

        System.out.println("Connexion à la base de données...");
        String url = "jdbc:mysql://localhost:3306/hopital"; // Changez selon votre config
        String user = "root";
        String password = "a!y!a!boutahli12";

        String sql = "SELECT c.date_consultation, c.diagnostic, c.traitement, u.nom AS medecin_nom, u.prenom AS medecin_prenom " +
                "FROM consultations c " +
                "JOIN dossiers_medicaux d ON c.dossier_id = d.id " +
                "JOIN utilisateurs u ON c.medecin_id = u.id " +
                "WHERE d.patient_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            System.out.println("Exécution de la requête SQL...");
            ResultSet rs = stmt.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;
                String date = rs.getString("date_consultation");
                String diagnostic = rs.getString("diagnostic");
                String traitement = rs.getString("traitement");
                String medecinNom = rs.getString("medecin_nom");
                String medecinPrenom = rs.getString("medecin_prenom");

                // Ajout d'une nouvelle entrée avec le nom complet du médecin
                tableDossierMedical.getItems().add(new DossierEntry(date, diagnostic, traitement, medecinNom + " " + medecinPrenom));
            }

            // Affichage d'un message s'il n'y a pas de dossiers
            if (!found) {
                noRecordsLabel.setText("Aucun dossier médical trouvé pour ce patient.");
                noRecordsLabel.setVisible(true);  // Afficher le label si aucun dossier trouvé
            } else {
                noRecordsLabel.setText("");  // Masquer le label si des résultats sont trouvés
                noRecordsLabel.setVisible(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur SQL : " + e.getMessage());
            noRecordsLabel.setText("Erreur lors de la récupération des dossiers.");
            noRecordsLabel.setVisible(true);  // Afficher un message d'erreur
        }
    }
}
