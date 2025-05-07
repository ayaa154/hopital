public class Planning {
    private String medecin;
    private String jour;
    private String heureDebut;
    private String heureFin;

    public Planning(String medecin, String jour, String heureDebut, String heureFin) {
        this.medecin = medecin;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public String getMedecin() { return medecin; }
    public String getJour() { return jour; }
    public String getHeureDebut() { return heureDebut; }
    public String getHeureFin() { return heureFin; }
}
