public class RendezVousMedecin {
    private int id;
    private int patientId;
    private String nom;
    private String prenom;
    private String dateHeure;
    private String motif;

    // Constructeur complet (pour les cas où tu connais déjà l'id et le patientId)
    public RendezVousMedecin(String nom, String prenom, String dateHeure, String motif, int id, int patientId) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.id = id;
        this.patientId = patientId;
    }

    // Constructeur simplifié (pour la création d’un nouveau rendez-vous)
    public RendezVousMedecin(String nom, String prenom, String dateHeure, String motif) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.id = -1; // Utilisation de -1 pour indiquer que l'ID n'est pas encore défini
        this.patientId = -1; // De même, utilisation de -1 pour indiquer que l'ID du patient n'est pas défini
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getDateHeure() {
        return dateHeure;
    }

    public String getMotif() {
        return motif;
    }

    // Setters si besoin
    public void setId(int id) {
        this.id = id;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
}
