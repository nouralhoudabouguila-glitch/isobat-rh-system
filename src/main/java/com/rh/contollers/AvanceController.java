package com.rh.contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AvanceController {

    @FXML
    private TableView<DemandeAvance> tableView;

    @FXML
    private TableColumn<DemandeAvance, String> colEmploye;
    @FXML
    private TableColumn<DemandeAvance, Double> colMontant;
    @FXML
    private TableColumn<DemandeAvance, String> colMotif;
    @FXML
    private TableColumn<DemandeAvance, String> colDate;
    @FXML
    private TableColumn<DemandeAvance, String> colStatut;
    @FXML
    private TableColumn<DemandeAvance, Void> colActions;

    // Labels des statistiques
    @FXML
    private Label lblEnAttente;
    @FXML
    private Label lblValidees;
    @FXML
    private Label lblRefusees;
    @FXML
    private Label lblTotalAvance;
    @FXML
    private Label lblTotalDemandes;

    private ObservableList<DemandeAvance> demandes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colEmploye.setCellValueFactory(new PropertyValueFactory<>("employe"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Styliser la colonne statut
        styliserStatuts();

        // Ajouter les boutons d'action
        ajouterBoutonsActions();

        // Charger les données d'exemple
        chargerDonneesExemple();

        // Lier les données à la table
        tableView.setItems(demandes);

        // Mettre à jour les statistiques
        mettreAJourStatistiques();
    }

    private void chargerDonneesExemple() {
        demandes.addAll(
                new DemandeAvance("Mohamed Bouzid", 250.00, "Frais médicaux", "18/06/2026", "En attente"),
                new DemandeAvance("Lina Trabelsi", 500.00, "Urgence familiale", "20/06/2026", "En attente"),
                new DemandeAvance("Ahmed Ben Ali", 800.00, "Rentrée scolaire", "21/06/2026", "En attente"),
                new DemandeAvance("Sara Chaabane", 200.00, "Réparation voiture", "12/06/2026", "Validée"),
                new DemandeAvance("Mohamed Bouzid", 300.00, "Voyage personnel", "03/03/2026", "Refusée"),
                new DemandeAvance("Nadia Chaoui", 450.00, "Achat mobilier", "15/06/2026", "Validée"),
                new DemandeAvance("Youssef Hamdi", 600.00, "Formation professionnelle", "10/06/2026", "En attente"),
                new DemandeAvance("Imane Mokded", 350.00, "Frais de santé", "08/06/2026", "Validée"),
                new DemandeAvance("Ichraf Karoui", 150.00, "Transport", "05/06/2026", "Refusée"),
                new DemandeAvance("Feriel Touati", 400.00, "Urgence familiale", "02/06/2026", "Validée"),
                new DemandeAvance("Hannachi Hanen", 275.00, "Fournitures scolaires", "28/05/2026", "Validée"),
                new DemandeAvance("Khadhraoui Olfa", 520.00, "Réparation maison", "25/05/2026", "En attente"),
                new DemandeAvance("Zeineb Alibi", 180.00, "Frais médicaux", "20/05/2026", "Validée"),
                new DemandeAvance("Wided Bouali", 320.00, "Achat ordinateur", "18/05/2026", "Refusée"),
                new DemandeAvance("Ahmed Karim", 750.00, "Voyage familial", "15/05/2026", "Validée"),
                new DemandeAvance("Sara Benali", 280.00, "Formation", "12/05/2026", "En attente"),
                new DemandeAvance("Mohamed Amine", 430.00, "Frais de santé", "10/05/2026", "Validée"),
                new DemandeAvance("Nadia Chaoui", 190.00, "Transport", "08/05/2026", "Validée"),
                new DemandeAvance("Youssef Hamdi", 560.00, "Urgence familiale", "05/05/2026", "Refusée")
        );
    }

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<DemandeAvance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(javafx.geometry.Pos.CENTER);

                    String styleClass = "";
                    String statutLower = item.toLowerCase();

                    if (statutLower.contains("en attente")) {
                        styleClass = "statut-en-attente";
                    } else if (statutLower.contains("validée") || statutLower.contains("validee")) {
                        styleClass = "statut-validee";
                    } else if (statutLower.contains("refusée") || statutLower.contains("refusee")) {
                        styleClass = "statut-refusee";
                    }

                    if (!styleClass.isEmpty()) {
                        Label label = new Label(item);
                        label.getStyleClass().add(styleClass);
                        label.setAlignment(javafx.geometry.Pos.CENTER);
                        setGraphic(label);
                        setText(null);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<DemandeAvance, Void>() {
            private final javafx.scene.control.Button btnValider = new javafx.scene.control.Button("✅ Valider");
            private final javafx.scene.control.Button btnRefuser = new javafx.scene.control.Button("❌ Refuser");

            {
                btnValider.getStyleClass().add("btn-secondary");
                btnValider.setStyle("-fx-font-size: 10px; -fx-padding: 2 8;");
                btnValider.setOnAction(event -> {
                    DemandeAvance demande = getTableView().getItems().get(getIndex());
                    demande.setStatut("Validée");
                    tableView.refresh();
                    mettreAJourStatistiques();
                });

                btnRefuser.getStyleClass().add("btn-back");
                btnRefuser.setStyle("-fx-font-size: 10px; -fx-padding: 2 8; -fx-text-fill: #75070C;");
                btnRefuser.setOnAction(event -> {
                    DemandeAvance demande = getTableView().getItems().get(getIndex());
                    demande.setStatut("Refusée");
                    tableView.refresh();
                    mettreAJourStatistiques();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DemandeAvance demande = getTableView().getItems().get(getIndex());
                    String statut = demande.getStatut();

                    if ("En attente".equals(statut)) {
                        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5, btnValider, btnRefuser);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER);
                        setGraphic(hbox);
                    } else {
                        Label label = new Label("✔ Traité");
                        label.setStyle("-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-style: italic;");
                        label.setAlignment(javafx.geometry.Pos.CENTER);
                        setGraphic(label);
                    }
                }
            }
        });
    }

    private void mettreAJourStatistiques() {
        int enAttente = 0;
        int validees = 0;
        int refusees = 0;
        double totalAvance = 0.0;

        for (DemandeAvance d : demandes) {
            String statut = d.getStatut();
            if ("En attente".equals(statut)) {
                enAttente++;
            } else if ("Validée".equals(statut)) {
                validees++;
                totalAvance += d.getMontant();
            } else if ("Refusée".equals(statut)) {
                refusees++;
            }
        }

        lblEnAttente.setText(String.valueOf(enAttente));
        lblValidees.setText(String.valueOf(validees));
        lblRefusees.setText(String.valueOf(refusees));
        lblTotalAvance.setText(String.format("%.0f DT", totalAvance));
        lblTotalDemandes.setText(String.valueOf(demandes.size()));
    }

    @FXML
    private void onRetour() {
        MainController.loadPage("paie", "Gestion de la Paie");
    }

    // ── Classe interne pour les données ──────────────────────────────────────

    public static class DemandeAvance {
        private final String employe;
        private final double montant;
        private final String motif;
        private final String date;
        private String statut;

        public DemandeAvance(String employe, double montant, String motif, String date, String statut) {
            this.employe = employe;
            this.montant = montant;
            this.motif = motif;
            this.date = date;
            this.statut = statut;
        }

        public String getEmploye() { return employe; }
        public double getMontant() { return montant; }
        public String getMotif() { return motif; }
        public String getDate() { return date; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
    }
}