public class RendezVous {
    private int id;
    private String nom, prenom, dateHeure, motif;

    public RendezVous(String nom, String prenom, String dateHeure, String motif, int id) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.id = id;
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getDateHeure() { return dateHeure; }
    public String getMotif() { return motif; }
    public int getId() { return id; }
}
