package mv.sdd.model;

import mv.sdd.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private static int nbCmd = 0;
    private final Client client;
    private final List<MenuPlat> plats = new ArrayList<>();
    private EtatCommande etat = EtatCommande.EN_ATTENTE;
    private int tempsRestant;

    public Commande() {
        this.id = nbCmd++;
        this.client = null;
    }
    public Commande(Client client, MenuPlat plat) {
        this.id = ++nbCmd;
        this.client = client;
        this.ajouterPlat((plat));
    }

    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public EtatCommande getEtat() {
        return etat;
    }

    public int getTempsRestant() {
        return tempsRestant;
    }

    public void setEtat(EtatCommande etat) {
        this.etat = etat;
    }

    public List<MenuPlat> getPlats() {
        return plats;
    }

    public void ajouterPlat(MenuPlat plat) {
        plats.add(plat);
        tempsRestant += Constantes.MENU.get(plat).getTempsPreparation();

    }

    public boolean demarrerPreparation(){
        if (etat == EtatCommande.EN_ATTENTE) {
            etat = EtatCommande.EN_PREPARATION;
            return true;
        }
        return false;
    }

    public void decrementerTempsRestant(){
        if (tempsRestant > 0) {
            tempsRestant--;
            if (tempsRestant == 0) {
                etat = EtatCommande.PRETE;
            }
        }
    }

    public boolean estTermineeParTemps(){
        return tempsRestant == 0 && etat == EtatCommande.PRETE;
    }

    public double calculerMontant(){
        double total = 0.0;
        for (MenuPlat plat : plats) {
            total += Constantes.MENU.get(plat).getPrix();
        }
        return total;
    }
}
