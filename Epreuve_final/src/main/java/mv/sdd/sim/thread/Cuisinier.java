package mv.sdd.sim.thread;

import mv.sdd.model.Commande;
import mv.sdd.sim.Restaurant;

public class Cuisinier implements Runnable {
    private final Restaurant restaurant;
    private final boolean enFonctionnement = true;

    public Cuisinier(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public void run() {
        while (enFonctionnement) {
            Commande commande = restaurant.retirerProchaineCommande();
            if (commande != null) {
                commande.demarrerPreparation();
                restaurant.ajouterCommandeEnPreparation(commande);

                while (!commande.estTermineeParTemps()) {
                    try {
                        Thread.sleep(1000);
                        commande.decrementerTempsRestant();
                    } catch (InterruptedException e) {
                        System.err.println("Thread interrompu");
                    }
                }
                restaurant.marquerCommandeCommePrete(commande);
            }
        }
    }
}