import javafx.beans.property.SimpleStringProperty;

public class DossierEntry {

    private SimpleStringProperty date;
    private SimpleStringProperty diagnostic;
    private SimpleStringProperty traitement;
    private SimpleStringProperty medecin;

    public DossierEntry(String date, String diagnostic, String traitement, String medecin) {
        this.date = new SimpleStringProperty(date);
        this.diagnostic = new SimpleStringProperty(diagnostic);
        this.traitement = new SimpleStringProperty(traitement);
        this.medecin = new SimpleStringProperty(medecin);
    }

    public String getDate() { return date.get(); }
    public String getDiagnostic() { return diagnostic.get(); }
    public String getTraitement() { return traitement.get(); }
    public String getMedecin() { return medecin.get(); }

    // Pour les liaisons JavaFX
    public SimpleStringProperty dateProperty() { return date; }
    public SimpleStringProperty diagnosticProperty() { return diagnostic; }
    public SimpleStringProperty traitementProperty() { return traitement; }
    public SimpleStringProperty medecinProperty() { return medecin; }
}
