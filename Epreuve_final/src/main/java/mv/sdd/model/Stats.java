package mv.sdd.model;

import mv.sdd.utils.Constantes;

import java.util.HashMap;
import java.util.Map;

public class Stats {
    private final Horloge horloge;
    private int totalClients = 0;
    private int nbServis = 0;
    private int nbFaches = 0;
    private double chiffreAffaires = 0;
    private final Map<MenuPlat, Integer> ventesParPlat = new HashMap<>();

    public Stats(Horloge horloge) {
        this.horloge = horloge;
    }

    public int getTotalClients() {
        return totalClients;
    }

    public int getNbServis() {
        return nbServis;
    }

    public int getNbFaches() {
        return nbFaches;
    }

    public double getChiffreAffaires() {
        return chiffreAffaires;
    }

    public Map<MenuPlat, Integer> getVentesParPlat() {
        return ventesParPlat;
    }
    public void incrementerTotalClients() {
        totalClients++;
    }

    public void incrementerNbServis() {
        nbServis++;
    }

    public void incrementerNbFaches() {
        nbFaches++;
    }

    public void incrementerChiffreAffaires(double montant) {
        this.chiffreAffaires += montant;
    }
    public void incrementerVentesParPlat(MenuPlat codePlat) {
        ventesParPlat.put(codePlat, ventesParPlat.getOrDefault(codePlat, 0) + 1);
    }

    public static String statsPlatLine(MenuPlat codePlat, int quantite) {
        return "\n" + "\t\t" + codePlat + " : " + quantite;
    }

    public String toString() {
        String chaine = String.format(
                Constantes.STATS_GENERAL,
                horloge.getTempsSimule(),
                totalClients,
                nbServis,
                nbFaches,
                chiffreAffaires
        );
        for (Map.Entry<MenuPlat, Integer> entry : ventesParPlat.entrySet()) {
            MenuPlat codePlat = entry.getKey();
            int quantite = entry.getValue();
            chaine += statsPlatLine(codePlat, quantite);
        }
        return chaine;
    }
}
