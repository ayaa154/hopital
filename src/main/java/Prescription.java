

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Prescription {
    private final StringProperty medicament;
    private final StringProperty posologie;
    private final StringProperty duree;
    private final StringProperty date;
    private final StringProperty medecin;

    public Prescription(String medicament, String posologie, String duree, String date, String medecin) {
        this.medicament = new SimpleStringProperty(medicament);
        this.posologie = new SimpleStringProperty(posologie);
        this.duree = new SimpleStringProperty(duree);
        this.date = new SimpleStringProperty(date);
        this.medecin = new SimpleStringProperty(medecin);
    }

    public StringProperty medicamentProperty() { return medicament; }
    public StringProperty posologieProperty() { return posologie; }
    public StringProperty dureeProperty() { return duree; }
    public StringProperty dateProperty() { return date; }
    public StringProperty medecinProperty() { return medecin; }
}

