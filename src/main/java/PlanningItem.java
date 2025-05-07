public class PlanningItem {
    private final String medecin;
    private final String jour;
    private final String heureDebut;
    private final String heureFin;

    public PlanningItem(String medecin, String jour, String heureDebut, String heureFin) {
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
