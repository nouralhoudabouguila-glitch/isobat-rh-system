package com.rh.contollers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

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

    @FXML
    public void initialize() {
        chargerDonneesExemple();
    }

    private void chargerDonneesExemple() {
        // Données d'exemple pour les cartes
        lblAvanceMontant.setText("4 850,00 DT");
        lblBulletinMontant.setText("12");
        lblPrimeMontant.setText("1 200,00 DT");
        lblHeureSupMontant.setText("45 h");
        lblTicketMontant.setText("120");
        lblRetenueMontant.setText("350,00 DT");

        lblAvanceCount.setText("19 demande(s) enregistrée(s)");
        lblBulletinCount.setText("12 bulletin(s) généré(s)");
        lblPrimeCount.setText("5 prime(s) accordée(s)");
        lblHeureSupCount.setText("8 saisie(s) enregistrée(s)");
        lblTicketCount.setText("120 ticket(s) attribué(s)");
        lblRetenueCount.setText("3 retenue(s) appliquée(s)");
    }

    // ── Navigation vers les pages de détails ──

    @FXML
    private void onVoirAvance() {
        MainController.loadPage("avance", "Demandes d'avance sur salaire");
    }

    @FXML
    private void onVoirBulletins() {
        // À implémenter
        System.out.println("Navigation vers Bulletins de paie");
    }

    @FXML
    private void onVoirPrimes() {
        // À implémenter
        System.out.println("Navigation vers Primes");
    }

    @FXML
    private void onVoirHeuresSup() {
        // À implémenter
        System.out.println("Navigation vers Heures supplémentaires");
    }

    @FXML
    private void onVoirTickets() {
        MainController.loadPage("ticket", "Tickets Restaurant");
    }

    @FXML
    private void onVoirRetenue() {
        MainController.loadPage("retenue", "Gestion des Retenues");
    }
}