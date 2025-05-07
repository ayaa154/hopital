public class PlanningItem {
    private final String medecin;
    private final String jour;
    private final String heureDebut;

    public PlanningItem(String medecin, String jour, String heureDebut) {
        this.medecin = medecin;
        this.jour = jour;
        this.heureDebut = heureDebut;
    }

    public String getMedecin() { return medecin; }
    public String getJour() { return jour; }
    public String getHeureDebut() { return heureDebut; }
}
