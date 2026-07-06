package com.rh.services;

import com.rh.models.Avance;
import com.rh.models.Retenue;
import com.rh.models.TicketEmploye;
import com.rh.utils.IsobatDB;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class ServicePaie {

    private Connection cnx;
    private ServiceAvance serviceAvance;
    private ServiceRetenue serviceRetenue;
    private ServiceTicket serviceTicket;

    public ServicePaie() {
        cnx = IsobatDB.getInstance().getCnx();
        serviceAvance = new ServiceAvance();
        serviceRetenue = new ServiceRetenue();
        serviceTicket = new ServiceTicket();
    }

    /**
     * Récupère les statistiques pour le mois en cours
     */
    public PaieStats getStatsMoisEnCours() {
        return getStatsMois(LocalDate.now());
    }

    /**
     * Récupère les statistiques pour un mois donné
     */
    public PaieStats getStatsMois(LocalDate date) {
        PaieStats stats = new PaieStats();

        String mois = getNomMois(date.getMonthValue());
        int annee = date.getYear();

        // ── Avances ──
        try {
            List<Avance> toutesAvances = serviceAvance.getAll();
            double totalAvancesValideesMois = 0;
            int nbAvancesMois = 0;
            int nbEnAttente = 0;
            int nbRefusees = 0;

            for (Avance a : toutesAvances) {
                if (a.getDateDemande() != null) {
                    // Compter toutes les demandes du mois
                    if (a.getDateDemande().getMonthValue() == date.getMonthValue() &&
                            a.getDateDemande().getYear() == date.getYear()) {
                        nbAvancesMois++;

                        if ("Validée".equals(a.getStatutLabel())) {
                            totalAvancesValideesMois += a.getMontant();
                        } else if ("En attente".equals(a.getStatutLabel())) {
                            nbEnAttente++;
                        } else if ("Refusée".equals(a.getStatutLabel())) {
                            nbRefusees++;
                        }
                    }
                }
            }

            stats.setAvanceMontant(totalAvancesValideesMois);
            stats.setAvanceCount(nbAvancesMois);
            stats.setAvanceEnAttente(nbEnAttente);
            stats.setAvanceRefusees(nbRefusees);
            stats.setAvanceValidees(nbAvancesMois - nbEnAttente - nbRefusees);

        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des avances : " + e.getMessage());
        }

        // ── Retenues ──
        try {
            List<Retenue> toutesRetenues = serviceRetenue.getAll();
            double totalRetenuesMois = 0;
            int nbRetenuesMois = 0;
            int nbEmployesConcernes = 0;

            for (Retenue r : toutesRetenues) {
                if (r.getDateRetenue() != null) {
                    if (r.getDateRetenue().getMonthValue() == date.getMonthValue() &&
                            r.getDateRetenue().getYear() == date.getYear()) {
                        totalRetenuesMois += r.getMontant();
                        nbRetenuesMois++;
                    }
                }
            }

            // Compter les employés distincts concernés
            nbEmployesConcernes = (int) toutesRetenues.stream()
                    .filter(r -> r.getDateRetenue() != null &&
                            r.getDateRetenue().getMonthValue() == date.getMonthValue() &&
                            r.getDateRetenue().getYear() == date.getYear())
                    .map(Retenue::getIdEmploye)
                    .distinct()
                    .count();

            stats.setRetenueMontant(totalRetenuesMois);
            stats.setRetenueCount(nbRetenuesMois);
            stats.setRetenueEmployes(nbEmployesConcernes);

        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des retenues : " + e.getMessage());
        }

        // ── Tickets ──
        try {
            List<TicketEmploye> ticketsMois = serviceTicket.findByMoisAnnee(mois, annee);
            int nbTickets = 0;
            int nbEmployes = ticketsMois.size();
            double totalTickets = 0;

            for (TicketEmploye t : ticketsMois) {
                nbTickets += t.getNbJours();
                totalTickets += t.getTotal();
            }

            stats.setTicketCount(nbTickets);
            stats.setTicketEmployes(nbEmployes);
            stats.setTicketMontant(totalTickets);

        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des tickets : " + e.getMessage());
        }

        // ── Autres modules (à implémenter plus tard) ──
        stats.setBulletinCount(0);
        stats.setPrimeMontant(0);
        stats.setPrimeCount(0);
        stats.setHeureSupMontant(0);
        stats.setHeureSupCount(0);

        return stats;
    }

    private String getNomMois(int numMois) {
        String[] mois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return mois[numMois - 1];
    }

    /**
     * Classe interne pour les statistiques de la paie
     */
    public static class PaieStats {
        // Avances
        private double avanceMontant;
        private int avanceCount;
        private int avanceEnAttente;
        private int avanceValidees;
        private int avanceRefusees;

        // Bulletins
        private int bulletinCount;

        // Retenues
        private double retenueMontant;
        private int retenueCount;
        private int retenueEmployes;

        // Primes
        private double primeMontant;
        private int primeCount;

        // Heures sup
        private double heureSupMontant;
        private int heureSupCount;

        // Tickets
        private int ticketCount;
        private int ticketEmployes;
        private double ticketMontant;

        // Getters et Setters
        public double getAvanceMontant() { return avanceMontant; }
        public void setAvanceMontant(double avanceMontant) { this.avanceMontant = avanceMontant; }
        public int getAvanceCount() { return avanceCount; }
        public void setAvanceCount(int avanceCount) { this.avanceCount = avanceCount; }
        public int getAvanceEnAttente() { return avanceEnAttente; }
        public void setAvanceEnAttente(int avanceEnAttente) { this.avanceEnAttente = avanceEnAttente; }
        public int getAvanceValidees() { return avanceValidees; }
        public void setAvanceValidees(int avanceValidees) { this.avanceValidees = avanceValidees; }
        public int getAvanceRefusees() { return avanceRefusees; }
        public void setAvanceRefusees(int avanceRefusees) { this.avanceRefusees = avanceRefusees; }

        public int getBulletinCount() { return bulletinCount; }
        public void setBulletinCount(int bulletinCount) { this.bulletinCount = bulletinCount; }

        public double getRetenueMontant() { return retenueMontant; }
        public void setRetenueMontant(double retenueMontant) { this.retenueMontant = retenueMontant; }
        public int getRetenueCount() { return retenueCount; }
        public void setRetenueCount(int retenueCount) { this.retenueCount = retenueCount; }
        public int getRetenueEmployes() { return retenueEmployes; }
        public void setRetenueEmployes(int retenueEmployes) { this.retenueEmployes = retenueEmployes; }

        public double getPrimeMontant() { return primeMontant; }
        public void setPrimeMontant(double primeMontant) { this.primeMontant = primeMontant; }
        public int getPrimeCount() { return primeCount; }
        public void setPrimeCount(int primeCount) { this.primeCount = primeCount; }

        public double getHeureSupMontant() { return heureSupMontant; }
        public void setHeureSupMontant(double heureSupMontant) { this.heureSupMontant = heureSupMontant; }
        public int getHeureSupCount() { return heureSupCount; }
        public void setHeureSupCount(int heureSupCount) { this.heureSupCount = heureSupCount; }

        public int getTicketCount() { return ticketCount; }
        public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }
        public int getTicketEmployes() { return ticketEmployes; }
        public void setTicketEmployes(int ticketEmployes) { this.ticketEmployes = ticketEmployes; }
        public double getTicketMontant() { return ticketMontant; }
        public void setTicketMontant(double ticketMontant) { this.ticketMontant = ticketMontant; }
    }
}