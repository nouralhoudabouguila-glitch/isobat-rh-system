package com.rh.contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class EntretienPhysiqueController {

    @FXML
    private TableView<CandidatEntretien> tableView;

    @FXML
    private TableColumn<CandidatEntretien, String> colNom;
    @FXML
    private TableColumn<CandidatEntretien, String> colDateRdv;
    @FXML
    private TableColumn<CandidatEntretien, String> colPoste;
    @FXML
    private TableColumn<CandidatEntretien, String> colStatut;

    @FXML
    private Label lblTotalCandidats;

    private ObservableList<CandidatEntretien> candidats = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDateRdv.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Styliser la colonne statut
        styliserStatuts();

        // Ajouter des données statiques d'exemple
        ajouterDonneesExemple();

        // Lier les données à la table
        tableView.setItems(candidats);

        // Mettre à jour le compteur
        mettreAJourCompteur();
    }

    private void ajouterDonneesExemple() {
        // Données statiques d'exemple (à remplacer par les données de la BD)
        candidats.add(new CandidatEntretien("Imane Mokded", "06-11-2025 à 11H", "Téléopérateur", "Reporté"));
        candidats.add(new CandidatEntretien("Ichraf Karoui", "05-11-2025 à 10:30 H", "Téléopérateur", "Absente"));
        candidats.add(new CandidatEntretien("Emna Bouiri", "05-11-2025 à 11h", "Téléopérateur", "Retenue - Refusé"));
        candidats.add(new CandidatEntretien("Jaouadi Tavssir", "05-11-2025 à 11:30h", "Téléopérateur", "Absente"));
        candidats.add(new CandidatEntretien("Baoueb Mustapha", "05-11-2025 à 14h", "Téléopérateur", "Absent"));
        candidats.add(new CandidatEntretien("Khadhraoui Olfa", "05-11-2025 à 11:30h", "Téléopérateur", "Retenue"));
        candidats.add(new CandidatEntretien("Zeineb Alibi", "06-11-2025 à 11H30", "Téléopérateur", "Absente"));
        candidats.add(new CandidatEntretien("Wided Bouali", "06-11-2025 à 10:30H", "Téléopérateur", "Absente"));
        candidats.add(new CandidatEntretien("Ghofrane Ben Amor", "06-11-2025 à 11:30H", "Téléopérateur", "Absente"));
        candidats.add(new CandidatEntretien("Hannachi Hanen", "06-11-2025 à 11:30H", "Téléopérateur", "Retenue"));
        candidats.add(new CandidatEntretien("Feriel Touati", "06-11-2025 à 14:00H", "Téléopérateur", "Retenue"));
        candidats.add(new CandidatEntretien("Ailani Henda", "06-11-2025 à 15:00h", "Téléopérateur", "Absente"));
    }

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<CandidatEntretien, String>() {
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

                    if (statutLower.contains("retenue") || statutLower.contains("retenu")) {
                        styleClass = "statut-retenue";
                    } else if (statutLower.contains("absent") || statutLower.contains("absente")) {
                        styleClass = "statut-absent";
                    } else if (statutLower.contains("reporté") || statutLower.contains("reporte")) {
                        styleClass = "statut-reporte";
                    } else if (statutLower.contains("refusé") || statutLower.contains("refuse")) {
                        styleClass = "statut-refuse";
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

    private void mettreAJourCompteur() {
        lblTotalCandidats.setText(candidats.size() + " candidat(s)");
    }

    // ── Classe interne pour les données ──────────────────────────────────────

    public static class CandidatEntretien {
        private final String nom;
        private final String dateRdv;
        private final String poste;
        private final String statut;

        public CandidatEntretien(String nom, String dateRdv, String poste, String statut) {
            this.nom = nom;
            this.dateRdv = dateRdv;
            this.poste = poste;
            this.statut = statut;
        }

        public String getNom() { return nom; }
        public String getDateRdv() { return dateRdv; }
        public String getPoste() { return poste; }
        public String getStatut() { return statut; }
    }
}