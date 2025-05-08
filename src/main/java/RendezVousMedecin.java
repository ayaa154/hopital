public class RendezVousMedecin {
    private int id;

    private String nom, prenom, dateHeure, motif;

    public RendezVousMedecin(String nom, String prenom, String dateHeure, String motif, int id) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.id = id;
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }


    private String patient;
    private String medecin;


    private String statut;

    public RendezVousMedecin(int id, String patient, String medecin, String dateHeure, String motif, String statut) {
        this.id = id;
        this.patient = patient;
        this.medecin = medecin;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public String getPatient() {
        return patient;
    }

    public String getMedecin() {
        return medecin;
    }

    public String getDateHeure() {
        return dateHeure;
    }

    public String getMotif() {
        return motif;
    }

    public String getStatut() {
        return statut;
    }

}
