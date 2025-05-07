import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MesRendezVousController {

    @FXML private TableView<RendezVous> tableRendezVous;
    @FXML private TableColumn<RendezVous, String> dateColumn;
    @FXML private TableColumn<RendezVous, String> medecinColumn;
    @FXML private TableColumn<RendezVous, String> specialiteColumn;
    @FXML private TableColumn<RendezVous, String> motifColumn;
    @FXML private TableColumn<RendezVous, String> statutColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;
    @FXML private ComboBox<String> cbStatut;

    private Connection connection;
    private int patientId;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static class RendezVous {
        private final SimpleStringProperty dateHeure;
        private final SimpleStringProperty medecin;
        private final SimpleStringProperty specialite;
        private final SimpleStringProperty motif;
        private final SimpleStringProperty statut;
        private final int id;

        public RendezVous(String dateHeure, String medecin, String specialite,
                          String motif, String statut, int id) {
            this.dateHeure = new SimpleStringProperty(dateHeure);
            this.medecin = new SimpleStringProperty(medecin);
            this.specialite = new SimpleStringProperty(specialite);
            this.motif = new SimpleStringProperty(motif);
            this.statut = new SimpleStringProperty(statut);
            this.id = id;
        }

        // Getters standards
        public String getDateHeure() { return dateHeure.get(); }
        public String getMedecin() { return medecin.get(); }
        public String getSpecialite() { return specialite.get(); }
        public String getMotif() { return motif.get(); }
        public String getStatut() { return statut.get(); }
        public int getId() { return id; }

        // Getters des propriétés
        public SimpleStringProperty dateHeureProperty() { return dateHeure; }
        public SimpleStringProperty medecinProperty() { return medecin; }
        public SimpleStringProperty specialiteProperty() { return specialite; }
        public SimpleStringProperty motifProperty() { return motif; }
        public SimpleStringProperty statutProperty() { return statut; }
    }

    @FXML
    public void initialize() {
        // Configuration des colonnes avec lambda (approche moderne)
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateHeureProperty());
        medecinColumn.setCellValueFactory(cellData -> cellData.getValue().medecinProperty());
        specialiteColumn.setCellValueFactory(cellData -> cellData.getValue().specialiteProperty());
        motifColumn.setCellValueFactory(cellData -> cellData.getValue().motifProperty());
        statutColumn.setCellValueFactory(cellData -> cellData.getValue().statutProperty());

        // Configuration de la colonne d'actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnAnnuler = new Button("Annuler");
            private final HBox buttons = new HBox(5, btnModifier, btnAnnuler);

            {
                // Style des boutons
                btnModifier.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnAnnuler.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                // Actions des boutons
                btnModifier.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    modifierRendezVous(rdv);
                });

                btnAnnuler.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    annulerRendezVous(rdv.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    public void initData(int patientId) {
        this.patientId = patientId;
        connectToDatabase();
        loadRendezVous();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost/hopital",
                    "root",
                    "a!y!a!boutahli12");
        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private void loadRendezVous() {
        ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();

        String query = "SELECT r.id, r.date_heure, r.motif, r.statut, " +
                "CONCAT(u.prenom, ' ', u.nom) AS medecin, m.specialite " +
                "FROM rendez_vous r " +
                "JOIN utilisateurs u ON r.medecin_id = u.id " +
                "JOIN medecins m ON u.id = m.id " +
                "WHERE r.patient_id = ? " +
                "ORDER BY r.date_heure DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDateTime dateHeure = rs.getTimestamp("date_heure").toLocalDateTime();
                String dateFormatted = dateHeure.format(formatter);

                rendezVousList.add(new RendezVous(
                        dateFormatted,
                        rs.getString("medecin"),
                        rs.getString("specialite"),
                        rs.getString("motif"),
                        rs.getString("statut"),
                        rs.getInt("id")
                ));
            }

            tableRendezVous.setItems(rendezVousList);
        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des rendez-vous");
            e.printStackTrace();
        }
    }

    private void modifierRendezVous(RendezVous rdv) {
        // Implémentez la modification du rendez-vous
        showAlert("Modification du RDV #" + rdv.getId());
    }

    private void annulerRendezVous(int idRendezVous) {
        // Implémentez l'annulation du rendez-vous
        showAlert("Annulation du RDV #" + idRendezVous);
    }

    @FXML
    private void handleFiltrer() {
        String statut = cbStatut.getValue();
        if (statut == null || "Tous".equals(statut)) {
            loadRendezVous();
        } else {
            ObservableList<RendezVous> filteredList = FXCollections.observableArrayList();
            for (RendezVous rdv : tableRendezVous.getItems()) {
                if (rdv.getStatut().equalsIgnoreCase(statut)) {
                    filteredList.add(rdv);
                }
            }
            tableRendezVous.setItems(filteredList);
        }
    }

    @FXML
    private void handleActualiser() {
        loadRendezVous();
    }

    @FXML
    private void handlePrendreRendezVous() {
        // Implémentez la prise de rendez-vous
        showAlert("Prise de nouveau rendez-vous");
    }

    @FXML
    private void handleProfilPatient() {
        // Navigation vers le profil patient
    }

    @FXML
    private void handleDossierMedical() {
        // Navigation vers le dossier médical
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}