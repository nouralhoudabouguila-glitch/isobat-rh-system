package com.rh.contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TicketController {

    @FXML
    private TableView<TicketEmploye> tableView;

    @FXML
    private TableColumn<TicketEmploye, String> colNom;
    @FXML
    private TableColumn<TicketEmploye, String> colPrenom;
    @FXML
    private TableColumn<TicketEmploye, Integer> colNbJours;
    @FXML
    private TableColumn<TicketEmploye, Double> colMontant;
    @FXML
    private TableColumn<TicketEmploye, Double> colTotal;

    @FXML
    private Label lblTotalTickets;
    @FXML
    private Label lblTotalMontant;
    @FXML
    private Label lblTotalBas;
    @FXML
    private Label lblNbEmployes;

    private ObservableList<TicketEmploye> tickets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colNbJours.setCellValueFactory(new PropertyValueFactory<>("nbJours"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantParTicket"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Charger les données d'exemple
        chargerDonneesExemple();

        // Lier les données à la table
        tableView.setItems(tickets);

        // Mettre à jour les statistiques
        mettreAJourStatistiques();
    }

    private void chargerDonneesExemple() {
        tickets.addAll(
                new TicketEmploye("Ahmed", "Karim", 22, 8.50),
                new TicketEmploye("Sara", "Benali", 20, 8.50),
                new TicketEmploye("Mohamed", "Amine", 22, 8.50),
                new TicketEmploye("Nadia", "Chaoui", 18, 8.50),
                new TicketEmploye("Youssef", "Hamdi", 22, 8.50),
                new TicketEmploye("Imane", "Mokded", 20, 8.50),
                new TicketEmploye("Ichraf", "Karoui", 22, 8.50),
                new TicketEmploye("Feriel", "Touati", 19, 8.50),
                new TicketEmploye("Hannachi", "Hanen", 21, 8.50),
                new TicketEmploye("Khadhraoui", "Olfa", 22, 8.50),
                new TicketEmploye("Zeineb", "Alibi", 20, 8.50),
                new TicketEmploye("Wided", "Bouali", 22, 8.50),
                new TicketEmploye("Lina", "Trabelsi", 18, 8.50),
                new TicketEmploye("Ahmed", "Ben Ali", 22, 8.50),
                new TicketEmploye("Sara", "Chaabane", 20, 8.50)
        );
    }

    private void mettreAJourStatistiques() {
        int totalTickets = 0;
        double totalMontant = 0.0;

        for (TicketEmploye t : tickets) {
            totalTickets += t.getNbJours();
            totalMontant += t.getTotal();
        }

        String totalFormate = String.format("%.2f DT", totalMontant);

        lblTotalTickets.setText(String.valueOf(totalTickets));
        lblTotalMontant.setText(totalFormate);
        lblTotalBas.setText(totalFormate);
        lblNbEmployes.setText(String.valueOf(tickets.size()));
    }

    @FXML
    private void onRetour() {
        MainController.loadPage("paie", "Gestion de la Paie");
    }

    // ── Classe interne pour les données ──────────────────────────────────────

    public static class TicketEmploye {
        private final String nom;
        private final String prenom;
        private final int nbJours;
        private final double montantParTicket;
        private final double total;

        public TicketEmploye(String nom, String prenom, int nbJours, double montantParTicket) {
            this.nom = nom;
            this.prenom = prenom;
            this.nbJours = nbJours;
            this.montantParTicket = montantParTicket;
            this.total = nbJours * montantParTicket;
        }

        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public int getNbJours() { return nbJours; }
        public double getMontantParTicket() { return montantParTicket; }
        public double getTotal() { return total; }
    }
}