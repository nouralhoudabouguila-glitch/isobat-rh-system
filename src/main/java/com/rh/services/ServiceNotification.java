package com.rh.services;

import com.rh.models.Facture;
import com.rh.models.Notification;
import com.rh.models.Avance;
import com.rh.models.Retenue;
import com.rh.models.TicketEmploye;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceNotification {

    // Liste en mémoire avec gestion des événements
    private static List<Notification> notifications = new ArrayList<>();
    private static int dernierId = 0;

    // Listeners pour les changements
    private static List<Runnable> listeners = new ArrayList<>();

    private ServiceFacture serviceFacture;
    private ServiceAvance serviceAvance;
    private ServiceRetenue serviceRetenue;
    private ServiceTicket serviceTicket;

    public ServiceNotification() {
        serviceFacture = new ServiceFacture();
        serviceAvance = new ServiceAvance();
        serviceRetenue = new ServiceRetenue();
        serviceTicket = new ServiceTicket();
        // Générer les notifications au démarrage
        verifierEtGenererNotifications();
    }

    /**
     * Ajoute un listener pour être notifié des changements
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Notifie tous les listeners d'un changement
     */
    private void notifierChangement() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Erreur lors de la notification : " + e.getMessage());
            }
        }
    }

    /**
     * Actualise toutes les notifications
     */
    public void actualiser() {
        verifierEtGenererNotifications();
        notifierChangement();
    }

    /**
     * Génère toutes les notifications dynamiquement
     */
    public void verifierEtGenererNotifications() {
        // Nettoyer les anciennes notifications
        notifications.clear();
        dernierId = 0;

        // 1. Notifications des factures (échéances)
        genererNotificationsFactures();

        // 2. Notifications des avances en attente
        genererNotificationsAvances();

        // 3. Notifications des retenues
        genererNotificationsRetenues();

        // 4. Notifications des tickets restaurant
        genererNotificationsTickets();

        System.out.println("✅ " + notifications.size() + " notifications générées dynamiquement");
    }

    /**
     * Génère les notifications pour les factures
     */
    private void genererNotificationsFactures() {
        try {
            List<Facture> factures = serviceFacture.getAll();
            LocalDate aujourdhui = LocalDate.now();

            for (Facture facture : factures) {
                if (facture.getDateEcheance() == null) continue;

                LocalDate echeance = facture.getDateEcheance();
                long joursRestants = aujourdhui.until(echeance).getDays();

                String titre = "";
                String message = "";
                String priorite = "info";
                String actionLabel = "Voir la facture →";

                // Facture en retard
                if (joursRestants < 0 && joursRestants > -15) {
                    titre = "🔴 Facture en retard — " + facture.getNomFournisseur();
                    message = "La facture " + facture.getNumeroFacture() +
                            " d'un montant de " + String.format("%.2f", facture.getMontantTtc()) +
                            " DT est arrivée à échéance. Aucun paiement enregistré.";
                    priorite = "danger";
                }
                // Échéance dans 3 jours ou moins
                else if (joursRestants >= 0 && joursRestants <= 3) {
                    titre = "⚠️ Échéance dans " + joursRestants + " jours — " + facture.getNomFournisseur();
                    message = "La facture " + facture.getNumeroFacture() +
                            " de " + String.format("%.2f", facture.getMontantTtc()) +
                            " DT arrive à échéance le " + echeance.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            ". Pensez à planifier le paiement.";
                    priorite = joursRestants == 0 ? "urgent" : "warning";
                }
                // Échéance dans 5 jours
                else if (joursRestants >= 4 && joursRestants <= 7) {
                    titre = "📄 Échéance dans " + joursRestants + " jours — " + facture.getNomFournisseur();
                    message = "La facture " + facture.getNumeroFacture() +
                            " de " + String.format("%.2f", facture.getMontantTtc()) +
                            " DT arrive à échéance le " + echeance.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    priorite = "info";
                } else {
                    continue;
                }

                // Vérifier les doublons
                boolean existe = notifications.stream().anyMatch(n ->
                        "FACTURE_ECHEANCE".equals(n.getType()) &&
                                n.getReference().equals(facture.getNumeroFacture())
                );

                if (!existe) {
                    Notification notif = new Notification(
                            "FACTURE_ECHEANCE",
                            titre,
                            message,
                            echeance,
                            facture.getNumeroFacture(),
                            facture.getIdFacture()
                    );
                    notif.setPriorite(priorite);
                    notif.setActionLabel(actionLabel);
                    notif.setActionPage("facture");
                    notif.setIdNotification(++dernierId);
                    notifications.add(notif);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des notifications factures : " + e.getMessage());
        }
    }

    /**
     * Génère les notifications pour les avances en attente
     */
    private void genererNotificationsAvances() {
        try {
            List<Avance> avances = serviceAvance.getAll();

            for (Avance avance : avances) {
                // Seulement les avances en attente
                if (!"En attente".equals(avance.getStatutLabel())) continue;

                String nomEmploye = avance.getNomCompletEmploye();
                if (nomEmploye == null || nomEmploye.isEmpty()) continue;

                String titre = "📋 Demande d'avance en attente";
                String message = nomEmploye + " a soumis une demande d'avance de " +
                        String.format("%.2f", avance.getMontant()) +
                        " DT. En attente de validation RH.";

                boolean existe = notifications.stream().anyMatch(n ->
                        "AVANCE_ATTENTE".equals(n.getType()) &&
                                n.getIdReference() == avance.getIdAvance()
                );

                if (!existe) {
                    Notification notif = new Notification(
                            "AVANCE_ATTENTE",
                            titre,
                            message,
                            avance.getDateDemande(),
                            "AV-" + avance.getIdAvance(),
                            avance.getIdAvance()
                    );
                    notif.setPriorite("warning");
                    notif.setActionLabel("Traiter →");
                    notif.setActionPage("avance");
                    notif.setIdNotification(++dernierId);
                    notifications.add(notif);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des notifications avances : " + e.getMessage());
        }
    }

    /**
     * Génère les notifications pour les retenues
     */
    private void genererNotificationsRetenues() {
        try {
            List<Retenue> retenues = serviceRetenue.getAll();
            LocalDate aujourdhui = LocalDate.now();
            LocalDate debutMois = aujourdhui.withDayOfMonth(1);

            for (Retenue retenue : retenues) {
                if (retenue.getDateRetenue() == null) continue;
                if (retenue.getDateRetenue().isBefore(debutMois)) continue;

                String nomEmploye = retenue.getNomCompletEmploye();
                if (nomEmploye == null || nomEmploye.isEmpty()) continue;

                String titre = "🔒 Retenue appliquée — " + nomEmploye;
                String message = "Une retenue de " + String.format("%.2f", retenue.getMontant()) +
                        " DT a été appliquée à " + nomEmploye +
                        " pour le motif : " + retenue.getMotif();

                boolean existe = notifications.stream().anyMatch(n ->
                        "RETENUE_APPLIQUEE".equals(n.getType()) &&
                                n.getIdReference() == retenue.getIdRetenue()
                );

                if (!existe) {
                    Notification notif = new Notification(
                            "RETENUE_APPLIQUEE",
                            titre,
                            message,
                            retenue.getDateRetenue(),
                            "RET-" + retenue.getIdRetenue(),
                            retenue.getIdRetenue()
                    );
                    notif.setPriorite("info");
                    notif.setActionLabel("Voir →");
                    notif.setActionPage("retenue");
                    notif.setIdNotification(++dernierId);
                    notifications.add(notif);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des notifications retenues : " + e.getMessage());
        }
    }

    /**
     * Génère les notifications pour les tickets restaurant
     */
    private void genererNotificationsTickets() {
        try {
            LocalDate aujourdhui = LocalDate.now();

            // Vérifier si on est le 23 du mois
            if (aujourdhui.getDayOfMonth() != 23) {
                return;
            }

            String moisActuel = getNomMois(aujourdhui.getMonthValue());
            int anneeActuelle = aujourdhui.getYear();

            List<TicketEmploye> tickets = serviceTicket.findByMoisAnnee(moisActuel, anneeActuelle);

            if (tickets.isEmpty()) {
                return;
            }

            int totalTickets = 0;
            double montantTotal = 0;
            int nbEmployes = tickets.size();

            for (TicketEmploye t : tickets) {
                totalTickets += t.getNbJours();
                montantTotal += t.getTotal();
            }

            String titre = "🍽️ Tickets restaurant disponibles — " + moisActuel + " " + anneeActuelle;
            String message = "Les tickets restaurant du mois de " + moisActuel + " " + anneeActuelle +
                    " sont disponibles.\n" +
                    "Nombre de tickets : " + totalTickets + "\n" +
                    "Montant total : " + String.format("%.2f", montantTotal) + " DT\n" +
                    "Employés concernés : " + nbEmployes;

            boolean existe = notifications.stream().anyMatch(n ->
                    "TICKET_RESTAURANT".equals(n.getType()) &&
                            n.getReference().equals(moisActuel + " " + anneeActuelle)
            );

            if (!existe) {
                Notification notif = new Notification(
                        "TICKET_RESTAURANT",
                        titre,
                        message,
                        aujourdhui,
                        moisActuel + " " + anneeActuelle,
                        0
                );
                notif.setPriorite("info");
                notif.setActionLabel("Consulter →");
                notif.setActionPage("ticket");
                notif.setIdNotification(++dernierId);
                notifications.add(notif);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des notifications tickets : " + e.getMessage());
        }
    }

    private String getNomMois(int numMois) {
        String[] mois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return mois[numMois - 1];
    }

    // ── Méthodes CRUD avec notification ──

    public void ajouter(Notification notification) {
        notification.setIdNotification(++dernierId);
        notifications.add(notification);
        notifierChangement();
    }

    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public List<Notification> getNotificationsNonLues() {
        return notifications.stream()
                .filter(n -> !n.isEstLue())
                .collect(Collectors.toList());
    }

    public void marquerCommeLue(int idNotification) {
        notifications.stream()
                .filter(n -> n.getIdNotification() == idNotification)
                .findFirst()
                .ifPresent(n -> {
                    n.setEstLue(true);
                    notifierChangement();
                });
    }

    public void marquerToutCommeLu() {
        notifications.forEach(n -> n.setEstLue(true));
        notifierChangement();
    }

    public void supprimer(int idNotification) {
        notifications.removeIf(n -> n.getIdNotification() == idNotification);
        notifierChangement();
    }

    public int compterNonLues() {
        return (int) notifications.stream().filter(n -> !n.isEstLue()).count();
    }

    public void nettoyerNotifications() {
        notifications.clear();
        notifierChangement();
    }

    public void genererNotificationsDemo() {
        verifierEtGenererNotifications();
        notifierChangement();
    }
}