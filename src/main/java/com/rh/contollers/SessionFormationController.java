package com.rh.contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SessionFormationController {

    @FXML
    private TableView<CandidatFormation> tableView;

    @FXML
    private TableColumn<CandidatFormation, String> colNom;
    @FXML
    private TableColumn<CandidatFormation, String> colDateFormation;
    @FXML
    private TableColumn<CandidatFormation, String> colPoste;
    @FXML
    private TableColumn<CandidatFormation, String> colStatut;

    @FXML
    private Label lblTotalCandidats;
    @FXML
    private Label lblEnCours;
    @FXML
    private Label lblTermine;
    @FXML
    private Label lblAnnule;

    private ObservableList<CandidatFormation> candidats = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDateFormation.setCellValueFactory(new PropertyValueFactory<>("dateFormation"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Styliser la colonne statut
        styliserStatuts();

        // Ajouter des données statiques d'exemple
        ajouterDonneesExemple();

        // Lier les données à la table
        tableView.setItems(candidats);

        // Mettre à jour les statistiques
        mettreAJourStatistiques();
    }

    private void ajouterDonneesExemple() {
        // Données statiques d'exemple (à remplacer par les données de la BD)
        candidats.add(new CandidatFormation("Ahmed Karim", "15-06-2026", "Développeur", "En cours"));
        candidats.add(new CandidatFormation("Sara Benali", "15-06-2026", "RH Manager", "Terminé"));
        candidats.add(new CandidatFormation("Mohamed Amine", "22-06-2026", "Comptable", "En cours"));
        candidats.add(new CandidatFormation("Nadia Chaoui", "22-06-2026", "Technicienne", "Annulé"));
        candidats.add(new CandidatFormation("Youssef Hamdi", "29-06-2026", "Chef de projet", "En cours"));
        candidats.add(new CandidatFormation("Imane Mokded", "29-06-2026", "Téléopérateur", "Terminé"));
        candidats.add(new CandidatFormation("Ichraf Karoui", "06-07-2026", "Téléopérateur", "En cours"));
        candidats.add(new CandidatFormation("Feriel Touati", "06-07-2026", "Téléopérateur", "Annulé"));
        candidats.add(new CandidatFormation("Hannachi Hanen", "13-07-2026", "Téléopérateur", "En cours"));
        candidats.add(new CandidatFormation("Khadhraoui Olfa", "13-07-2026", "Téléopérateur", "Terminé"));
        candidats.add(new CandidatFormation("Zeineb Alibi", "20-07-2026", "Téléopérateur", "En cours"));
        candidats.add(new CandidatFormation("Wided Bouali", "20-07-2026", "Téléopérateur", "En cours"));
    }

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<CandidatFormation, String>() {
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

                    // Appliquer le style selon le statut
                    String styleClass = "";
                    String statutLower = item.toLowerCase();

                    if (statutLower.contains("en cours")) {
                        styleClass = "statut-en-cours";
                    } else if (statutLower.contains("terminé") || statutLower.contains("termine")) {
                        styleClass = "statut-termine";
                    } else if (statutLower.contains("annulé") || statutLower.contains("annule")) {
                        styleClass = "statut-annule";
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

    private void mettreAJourStatistiques() {
        int total = candidats.size();
        int enCours = 0;
        int termine = 0;
        int annule = 0;

        for (CandidatFormation c : candidats) {
            String statut = c.getStatut().toLowerCase();
            if (statut.contains("en cours")) {
                enCours++;
            } else if (statut.contains("terminé") || statut.contains("termine")) {
                termine++;
            } else if (statut.contains("annulé") || statut.contains("annule")) {
                annule++;
            }
        }

        lblTotalCandidats.setText(String.valueOf(total));
        lblEnCours.setText(String.valueOf(enCours));
        lblTermine.setText(String.valueOf(termine));
        lblAnnule.setText(String.valueOf(annule));
    }

    // ── Classe interne pour les données ──────────────────────────────────────

    public static class CandidatFormation {
        private final String nom;
        private final String dateFormation;
        private final String poste;
        private final String statut;

        public CandidatFormation(String nom, String dateFormation, String poste, String statut) {
            this.nom = nom;
            this.dateFormation = dateFormation;
            this.poste = poste;
            this.statut = statut;
        }

        public String getNom() { return nom; }
        public String getDateFormation() { return dateFormation; }
        public String getPoste() { return poste; }
        public String getStatut() { return statut; }
    }
}