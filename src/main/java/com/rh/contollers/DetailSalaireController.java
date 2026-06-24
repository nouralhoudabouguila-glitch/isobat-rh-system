package com.rh.contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DetailSalaireController {

    @FXML
    private TableView<EmployeSalaire> tableView;

    @FXML
    private TableColumn<EmployeSalaire, String> colNom;
    @FXML
    private TableColumn<EmployeSalaire, String> colRib;
    @FXML
    private TableColumn<EmployeSalaire, Double> colMontant;

    @FXML
    private Label lblTitre;
    @FXML
    private Label lblTotalGeneral;
    @FXML
    private Label lblTotalBas;
    @FXML
    private Label lblNbEmployes;
    @FXML
    private Label lblCentre;

    // Variable statique pour recevoir le centre depuis DepenseController
    private static String centreActuel = "Centre d'appel 1";

    public static void setCentre(String centre) {
        centreActuel = centre;
    }

    private ObservableList<EmployeSalaire> employes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colRib.setCellValueFactory(new PropertyValueFactory<>("rib"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));

        // Afficher le centre
        lblCentre.setText("Centre : " + centreActuel);
        lblTitre.setText("💼  Détails des Salaires - " + centreActuel);

        // Charger les données selon le centre
        chargerDonnees(centreActuel);

        // Lier les données à la table
        tableView.setItems(employes);

        // Calculer et afficher le total
        calculerTotal();

        // Afficher le nombre d'employés
        lblNbEmployes.setText(String.valueOf(employes.size()));
    }

    private void chargerDonnees(String centre) {
        // Données statiques d'exemple selon le centre
        // À remplacer par les données de la BD
        switch (centre) {
            case "Centre d'appel 1":
                employes.addAll(
                        new EmployeSalaire("Nima Fourit", "0514 020095 097", 2500.00),
                        new EmployeSalaire("Amel Mansou", "05 0172", 2200.00),
                        new EmployeSalaire("Chayla Boumaiza", "03 0000 1151", 2800.00),
                        new EmployeSalaire("Aya Boufouf", "01 0000 357", 2300.00),
                        new EmployeSalaire("Fatma Shabini", "01 0000 35831", 2100.00)
                );
                break;
            case "Centre d'appel 2":
                employes.addAll(
                        new EmployeSalaire("Amir Laiti", "01 0000 094 346", 2400.00),
                        new EmployeSalaire("Seif Benlansour", "01 0000 147 069", 2600.00),
                        new EmployeSalaire("Imen Ferjan", "01 0000 147 069", 2200.00),
                        new EmployeSalaire("Eisa Ghrib", "1700 0000 555 06", 2500.00)
                );
                break;
            case "Bureau d'étude":
                employes.addAll(
                        new EmployeSalaire("Hmachi Nail", "17 3594 99674 06", 3200.00),
                        new EmployeSalaire("Abdolssi Anour", "04 0000 1922 111", 3500.00),
                        new EmployeSalaire("Mohamed Amine", "01 0000 456 789", 3800.00),
                        new EmployeSalaire("Sara Benali", "02 0000 789 123", 3400.00),
                        new EmployeSalaire("Youssef Hamdi", "03 0000 234 567", 4200.00)
                );
                break;
            case "Administration":
                employes.addAll(
                        new EmployeSalaire("Nadia Chaoui", "01 0000 567 890", 3000.00),
                        new EmployeSalaire("Ahmed Karim", "02 0000 890 123", 2800.00),
                        new EmployeSalaire("Ichraf Karoui", "03 0000 345 678", 2600.00),
                        new EmployeSalaire("Feriel Touati", "04 0000 901 234", 2900.00)
                );
                break;
            default:
                employes.addAll(
                        new EmployeSalaire("Aucun employé", "", 0.00)
                );
                break;
        }
    }

    private void calculerTotal() {
        double total = 0.0;
        for (EmployeSalaire e : employes) {
            total += e.getMontant();
        }
        String totalFormate = String.format("%.3f DT", total);
        lblTotalGeneral.setText(totalFormate);
        lblTotalBas.setText(totalFormate);
    }

    @FXML
    private void onRetour() {
        MainController.loadPage("depense", "Gestion des Dépenses");
    }

    // ── Classe interne pour les données ──────────────────────────────────────

    public static class EmployeSalaire {
        private final String nom;
        private final String rib;
        private final double montant;

        public EmployeSalaire(String nom, String rib, double montant) {
            this.nom = nom;
            this.rib = rib;
            this.montant = montant;
        }

        public String getNom() { return nom; }
        public String getRib() { return rib; }
        public double getMontant() { return montant; }
    }
}