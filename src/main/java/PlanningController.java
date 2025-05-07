import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class PlanningController {


    @FXML
    private TableView<PlanningItem> planningTable;
    @FXML
    private TableColumn<PlanningItem, String> medecinCol;
    @FXML
    private TableColumn<PlanningItem, String> jourCol;
    @FXML
    private TableColumn<PlanningItem, String> heureDebutCol;

    @FXML private ListView<String> disponibilitesList;

    @FXML
    private ComboBox<String> medecinCombo;




    @FXML
    private BorderPane rootPane;


    private final String DB_URL = "jdbc:mysql://localhost/hopital";
    private final String USER = "root";
    private final String PASSWORD = "a!y!a!boutahli12";

    @FXML
    public void initialize() {
        medecinCol.setCellValueFactory(new PropertyValueFactory<>("medecin"));
        jourCol.setCellValueFactory(new PropertyValueFactory<>("jour"));
        heureDebutCol.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));

        remplirComboMedecins();


    }

    private void remplirComboMedecins() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT nom, prenom FROM utilisateurs WHERE role = 'medecin'");
            List<String> medecins = new ArrayList<>();
            while (rs.next()) {
                medecins.add(rs.getString("nom") + " " + rs.getString("prenom"));
            }
            medecinCombo.setItems(FXCollections.observableArrayList(medecins));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilReceptionniste.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow(); // <- ici on utilise rootPane
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private DatePicker datePicker;

    @FXML
    public void filtrerParDate() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;

        String dateStr = selectedDate.toString(); // Format : "2025-05-08"
        planningTable.getItems().clear();

        String sql = """
        SELECT CONCAT(u.nom, ' ', u.prenom) AS medecin, 
               DATE(rv.date_heure) AS jour,
               TIME(rv.date_heure) AS heure
        FROM rendez_vous rv
        JOIN utilisateurs u ON rv.medecin_id = u.id
        WHERE DATE(rv.date_heure) = ?
        ORDER BY rv.date_heure
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dateStr);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String medecin = rs.getString("medecin");
                String jour = rs.getString("jour");
                String heure = rs.getString("heure");

                planningTable.getItems().add(new PlanningItem(
                        medecin, jour, heure // même heure pour début/fin
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void filtrerParMedecinEtDate() {
        String medecinNom = medecinCombo.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (medecinNom == null) return;

        planningTable.getItems().clear();
        disponibilitesList.getItems().clear(); // on vide aussi la liste des créneaux dispo

        StringBuilder sql = new StringBuilder("""
        SELECT CONCAT(u.nom, ' ', u.prenom) AS medecin, rv.date_heure
        FROM rendez_vous rv
        JOIN utilisateurs u ON rv.medecin_id = u.id
        WHERE CONCAT(u.nom, ' ', u.prenom) = ?
    """);

        if (selectedDate != null) {
            sql.append(" AND DATE(rv.date_heure) = ?");
        }

        sql.append(" ORDER BY rv.date_heure");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql.toString(),
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            stmt.setString(1, medecinNom);
            if (selectedDate != null) {
                stmt.setDate(2, Date.valueOf(selectedDate));
            }

            ResultSet rs = stmt.executeQuery();

            Set<String> heuresOccupees = new HashSet<>();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_heure");
                LocalDate date = ts.toLocalDateTime().toLocalDate();
                String heure = ts.toLocalDateTime().toLocalTime().toString().substring(0, 5);

                planningTable.getItems().add(new PlanningItem(
                        medecinNom,
                        date.toString(),
                        heure
                ));

                heuresOccupees.add(heure);
            }

            // Créneaux standards disponibles
            List<String> tousLesCreneaux = Arrays.asList(
                    "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                    "11:00", "11:30", "13:00", "13:30", "14:00", "14:30",
                    "15:00", "15:30", "16:00", "16:30"
            );

            List<String> disponibles = new ArrayList<>();
            for (String creneau : tousLesCreneaux) {
                if (!heuresOccupees.contains(creneau)) {
                    disponibles.add(creneau);
                }
            }

            disponibilitesList.setItems(FXCollections.observableArrayList(disponibles));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}