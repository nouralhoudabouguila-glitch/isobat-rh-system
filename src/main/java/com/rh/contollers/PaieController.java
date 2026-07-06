package com.rh.contollers;

import com.rh.services.ServicePaie;
import com.rh.services.ServicePaie.PaieStats;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class PaieController {

    @FXML private Label lblAvanceMontant;
    @FXML private Label lblBulletinMontant;
    @FXML private Label lblPrimeMontant;
    @FXML private Label lblHeureSupMontant;
    @FXML private Label lblTicketMontant;
    @FXML private Label lblRetenueMontant;

    @FXML private Label lblAvanceCount;
    @FXML private Label lblBulletinCount;
    @FXML private Label lblPrimeCount;
    @FXML private Label lblHeureSupCount;
    @FXML private Label lblTicketCount;
    @FXML private Label lblRetenueCount;

    private ServicePaie servicePaie;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);
    private LocalDate moisActuel = LocalDate.now();

    @FXML
    public void initialize() {
        servicePaie = new ServicePaie();
        chargerStatistiques();
    }

    /**
     * Charge les statistiques depuis la base de données
     */
    public void chargerStatistiques() {
        try {
            System.out.println("=== Chargement des statistiques de paie ===");
            PaieStats stats = servicePaie.getStatsMois(moisActuel);

            // ── Avances ──
            lblAvanceMontant.setText(format.format(stats.getAvanceMontant()) + " DT");
            lblAvanceCount.setText(stats.getAvanceCount() + " demande(s) enregistrée(s)");
            System.out.println("Avances : " + stats.getAvanceCount() + " demandes, " + stats.getAvanceMontant() + " DT validées");

            // ── Bulletins ──
            lblBulletinMontant.setText(String.valueOf(stats.getBulletinCount()));
            lblBulletinCount.setText(stats.getBulletinCount() + " bulletin(s) généré(s)");

            // ── Retenues ──
            lblRetenueMontant.setText(format.format(stats.getRetenueMontant()) + " DT");
            lblRetenueCount.setText(stats.getRetenueCount() + " retenue(s) appliquée(s)");
            System.out.println("Retenues : " + stats.getRetenueCount() + " retenues, " + stats.getRetenueMontant() + " DT");

            // ── Primes ──
            lblPrimeMontant.setText(format.format(stats.getPrimeMontant()) + " DT");
            lblPrimeCount.setText(stats.getPrimeCount() + " prime(s) accordée(s)");

            // ── Heures supplémentaires ──
            lblHeureSupMontant.setText(String.valueOf((int)stats.getHeureSupMontant()) + " h");
            lblHeureSupCount.setText(stats.getHeureSupCount() + " saisie(s) enregistrée(s)");

            // ── Tickets ──
            lblTicketMontant.setText(String.valueOf(stats.getTicketCount()));
            lblTicketCount.setText(stats.getTicketEmployes() + " employé(s)");
            System.out.println("Tickets : " + stats.getTicketCount() + " tickets, " + stats.getTicketEmployes() + " employés");

            System.out.println("=== Statistiques chargées avec succès ===");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques : " + e.getMessage());
            e.printStackTrace();
            afficherDonneesParDefaut();
        }
    }

    private void afficherDonneesParDefaut() {
        lblAvanceMontant.setText("0,00 DT");
        lblAvanceCount.setText("0 demande(s) enregistrée(s)");
        lblBulletinMontant.setText("0");
        lblBulletinCount.setText("0 bulletin(s) généré(s)");
        lblRetenueMontant.setText("0,00 DT");
        lblRetenueCount.setText("0 retenue(s) appliquée(s)");
        lblPrimeMontant.setText("0,00 DT");
        lblPrimeCount.setText("0 prime(s) accordée(s)");
        lblHeureSupMontant.setText("0 h");
        lblHeureSupCount.setText("0 saisie(s) enregistrée(s)");
        lblTicketMontant.setText("0");
        lblTicketCount.setText("0 employé(s)");
    }

    /**
     * Actualise les statistiques (appelé depuis d'autres contrôleurs)
     */
    public void actualiser() {
        System.out.println("=== Actualisation des statistiques de paie ===");
        chargerStatistiques();
    }

    // ── Navigation vers les pages de détails ──

    @FXML
    private void onVoirAvance() {
        MainController.getInstance().loadPage("avance", "Demandes d'avance sur salaire");
    }

    @FXML
    private void onVoirBulletins() {
        System.out.println("Navigation vers Bulletins de paie");
    }

    @FXML
    private void onVoirPrimes() {
        System.out.println("Navigation vers Primes");
    }

    @FXML
    private void onVoirHeuresSup() {
        System.out.println("Navigation vers Heures supplémentaires");
    }

    @FXML
    private void onVoirTickets() {
        MainController.getInstance().loadPage("ticket", "Tickets Restaurant");
    }

    @FXML
    private void onVoirRetenue() {
        MainController.getInstance().loadPage("retenue", "Gestion des Retenues");
    }
}