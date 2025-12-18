package mv.sdd.sim;

import mv.sdd.io.Action;
import mv.sdd.io.ActionType;
import mv.sdd.model.Client;
import mv.sdd.model.Commande;
import mv.sdd.model.EtatClient;
import mv.sdd.model.MenuPlat;
import mv.sdd.model.Stats;
import mv.sdd.utils.Constantes;
import mv.sdd.utils.Logger;
import mv.sdd.utils.Formatter;


import java.util.*;

public class Restaurant {
    private final Map<Integer, Client> clients = new HashMap<>();
    private final Queue<Commande> commandesEnFile = new LinkedList<>();
    private final List<Commande> commandesEnPreparation = new ArrayList<>();
    private final List<Commande> commandesPretes = new ArrayList<>();
    private final Logger logger;
    private final Stats stats;
    private int tempsSimule = 0;
    private boolean serviceEnCours = false;

    public Restaurant(Logger logger, Stats stats) {
        this.logger = logger;
        this.stats = stats;
    }

    public void executerAction(Action action) {
        if (action.getType() == ActionType.DEMARRER_SERVICE) {
            demarrerService(action.getParam1(), action.getParam2());
        } else if (action.getType() == ActionType.AJOUTER_CLIENT) {
            ajouterClient(action.getParam1(), action.getParam3(), action.getParam2());
        } else if (action.getType() == ActionType.PASSER_COMMANDE) {
            passerCommande(action.getParam1(), MenuPlat.valueOf(action.getParam3()));
        } else if (action.getType() == ActionType.AVANCER_TEMPS) {
            avancerTemps(action.getParam1());
        } else if (action.getType() == ActionType.AFFICHER_ETAT) {
            afficherEtat();
        } else if (action.getType() == ActionType.AFFICHER_STATS) {
            afficherStatistiques();
        } else if (action.getType() == ActionType.QUITTER) {
            arreterService();
        }
    }

    public void demarrerService(int dureeMax, int nbCuisiniers) {
        if (!serviceEnCours) {
            serviceEnCours = true;
            logger.logLine("Service démarré pour " + dureeMax + " minutes avec " + nbCuisiniers + " cuisinier(s).");
        }
    }

    public void avancerTemps(int minutes) {
        if (serviceEnCours) {
            for (int i = 0; i < minutes; i++) {
                tick();
            }
            tempsSimule += minutes;
            logger.logLine("Temps avancé de " + minutes + " minutes. Temps actuel : " + tempsSimule);
        } else {
            logger.logLine("Erreur : Service non démarré.");
        }
    }

    public void arreterService() {
        serviceEnCours = false;
        logger.logLine("Service arrêté.");
    }

    public void tick() {
        diminuerTempsRestantCommandes();
        diminuerPatienceClients();
    }

    public void ajouterClient(int id, String nom, int patienceInitiale) {
        if (!clients.containsKey(id)) {
            Client client = new Client(id, nom, patienceInitiale);
            clients.put(id, client);
            stats.incrementerTotalClients();
            logger.logLine(Formatter.eventArriveeClient(tempsSimule, client));
        } else {
            logger.logLine("Erreur : Client avec l'ID " + id + " existe déjà.");
        }
    }

    public void passerCommande(int idClient, MenuPlat codePlat) {
        Client client = clients.get(idClient);
        if (client != null) {
            Commande commande = client.getCommande();
            if (commande == null) {
                commande = new Commande(client, codePlat);
                commandesEnFile.add(commande);
                client.setCommande(commande);
                stats.incrementerVentesParPlat(codePlat);
                logger.logLine(Formatter.eventCommandeCree(tempsSimule, commande.getId(), client, codePlat));
            } else {
                commande.ajouterPlat(codePlat);
                stats.incrementerVentesParPlat(codePlat);
                logger.logLine("Client #" + idClient + " a ajouté : " + codePlat + " à sa commande.");
            }
        } else {
            logger.logLine("Erreur : Client #" + idClient + " introuvable.");
        }
    }

    public void ajouterCommandeEnPreparation(Commande commande) {
        commandesEnPreparation.add(commande);
        logger.logLine(Formatter.eventCommandeDebut(tempsSimule, commande.getId(), commande.getTempsRestant()));
    }

    public void marquerCommandeCommePrete(Commande commande) {
        if (commandesPretes.contains(commande)) {
            commandesPretes.remove(commande);
            Commande commandeClient = commande.getClient().getCommande();
            if (commandeClient != null && commandeClient.getId() == commande.getId()) {
                Client client = commande.getClient();
                client.setEtat(EtatClient.SERVI);
                stats.incrementerNbServis();
                stats.incrementerChiffreAffaires(commande.calculerMontant());
                logger.logLine(Formatter.eventCommandeTerminee(tempsSimule, commande.getId(), client));
            }
        } else {
            logger.logLine("Erreur : Commande #" + commande.getId() + " introuvable lorsque son état est marqué comme prête");
        }
    }

    public synchronized Commande retirerProchaineCommande() {
        return commandesEnFile.poll();
    }

    public void afficherEtat() {
        int nbClients = clients.size();
        int nbServis = 0;
        int nbFaches = 0;
        int nbCommandesEnPreparation = commandesEnPreparation.size();
        int nbCommandesEnFile = commandesEnFile.size();

        for (Client client : clients.values()) {
            if (client.getEtat() == EtatClient.SERVI) {
                nbServis++;
            } else if (client.getEtat() == EtatClient.PARTI_FACHE) {
                nbFaches++;
            }
        }

        logger.logLine(String.format("[t=%d] 👥%d 😋%d 😡%d 📥%d 🍳%d",
                tempsSimule,
                nbClients,
                nbServis,
                nbFaches,
                nbCommandesEnFile,
                nbCommandesEnPreparation
        ));

        for (Client client : clients.values()) {
            MenuPlat codePlat = null;
            Commande commande = client.getCommande();

            if (commande != null && !commande.getPlats().isEmpty()) {
                codePlat = commande.getPlats().get(0);
            }
            String clientLine = Formatter.clientLine(client, codePlat);
            logger.logLine(clientLine);
        }
    }

    public void afficherStatistiques() {
        int totalClients = stats.getTotalClients();
        int nbServis = stats.getNbServis();
        int nbFaches = stats.getNbFaches();
        double chiffreAffaires = stats.getChiffreAffaires();

        logger.logLine("📊 Stats");
        logger.logLine("Total clients : " + totalClients);
        logger.logLine("Clients servis : " + nbServis + " 😋");
        logger.logLine("Clients partis fâchés : " + nbFaches + " 😡");
        logger.logLine("Chiffre d'affaires : " + String.format("%.2f $", chiffreAffaires));
        logger.logLine("Plats vendus :");
        for (MenuPlat plat : Constantes.MENU.keySet()) {
            int quantite = stats.getVentesParPlat().getOrDefault(plat, 0);
            logger.logLine("\t" + plat.name() + " : " + quantite + " " + Formatter.emojiPlat(plat));
        }
    }

    private void diminuerPatienceClients() {
        for (Client client : clients.values()) {
            if (client.getEtat() == EtatClient.EN_ATTENTE) {
                client.diminuerPatience(1);
                if (client.getEtat() == EtatClient.PARTI_FACHE) {
                    stats.incrementerNbFaches();
                    logger.logLine(Formatter.eventClientFache(tempsSimule, client));
                }
            }
        }
    }

    private void diminuerTempsRestantCommandes() {
        Iterator<Commande> it = commandesEnPreparation.iterator();
        while (it.hasNext()) {
            Commande commande = it.next();
            commande.decrementerTempsRestant();
            if (commande.estTermineeParTemps()) {
                logger.logLine("Commande #" + commande.getId() + " est prête.");
                commandesPretes.add(commande);
                commande.getClient().setEtat(EtatClient.SERVI);
                stats.incrementerNbServis();
                stats.incrementerChiffreAffaires(commande.calculerMontant());
                it.remove();
            }
        }
    }
}