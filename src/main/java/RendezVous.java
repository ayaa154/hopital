public class RendezVous {
    private int id;
    private String patient;
    private String medecin;
    private String dateHeure;
    private String motif;
    private String statut;

    public RendezVous(int id, String patient, String medecin, String dateHeure, String motif, String statut) {
        this.id = id;
        this.patient = patient;
        this.medecin = medecin;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.statut = statut;
    }

    public int getId() { return id; }
    public String getPatient() { return patient; }
    public String getMedecin() { return medecin; }
    public String getDateHeure() { return dateHeure; }
    public String getMotif() { return motif; }
    public String getStatut() { return statut; }
}
