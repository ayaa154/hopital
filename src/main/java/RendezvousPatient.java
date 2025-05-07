public class RendezvousPatient {
    private String dateHeure;
    private String motif;
    private String statut;

    public RendezvousPatient(String dateHeure, String motif, String statut) {
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.statut = statut;
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