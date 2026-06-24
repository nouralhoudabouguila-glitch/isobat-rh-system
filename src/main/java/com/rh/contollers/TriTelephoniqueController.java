package com.rh.contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class TriTelephoniqueController {

    @FXML
    private TableView<CandidatTri> tableView;

    @FXML
    private TableColumn<CandidatTri, String> colNom;
    @FXML
    private TableColumn<CandidatTri, String> colTel;
    @FXML
    private TableColumn<CandidatTri, String> colExperience;
    @FXML
    private TableColumn<CandidatTri, String> colPoste;
    @FXML
    private TableColumn<CandidatTri, String> colCommentaire;
    @FXML
    private TableColumn<CandidatTri, String> colStatut;

    @FXML
    private Label lblTotalCandidats;
    @FXML
    private ComboBox<Integer> comboLignesParPage;
    @FXML
    private Label lblPageActuelle;

    private ObservableList<CandidatTri> candidats = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colExperience.setCellValueFactory(new PropertyValueFactory<>("experience"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configurer le style du statut
        styliserStatuts();

        // Configurer la combo de lignes par page
        comboLignesParPage.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        comboLignesParPage.setValue(10);

        // Ajouter des données d'exemple
        ajouterDonneesExemple();

        // Lier les données à la table
        tableView.setItems(candidats);

        // Mettre à jour le compteur
        mettreAJourCompteur();
    }

    private void ajouterDonneesExemple() {
        // À remplacer par vos données provenant de la base de données
        candidats.add(new CandidatTri("Mohamed Amine Bejaoui", "52 800 489 / 21 688 540",
                "Responsable Technique Département Élec", "Téléopérateur", "", "Pas accepté"));
        candidats.add(new CandidatTri("Monthe Kamte Aurelien Wilfried", "27 726 657",
                "ACTIVE CONTACT (Fev. 2019 – Nov. 2021)", "Téléopérateur", "", "Accepté"));
        candidats.add(new CandidatTri("Bouzouita Ons", "92 310 242",
                "Ingénieure en génie civil", "Téléopérateur", "", "Pas accepté"));
        candidats.add(new CandidatTri("Ichraf Karoui", "22 918 580",
                "Télévendeuse - Téléopératrice (2 ans)", "Téléopérateur", "RDV", "Accepté"));
        candidats.add(new CandidatTri("Houda Rawan", "56 065 044",
                "Centre d'appels (7 mois)", "Téléopérateur", "", "Pas accepté"));
        candidats.add(new CandidatTri("Ajmi Nejib", "27 660 486 / 90 508 744",
                "Conseiller client Teleperformance (10 mois)", "Téléopérateur", "", "Pas accepté"));
        candidats.add(new CandidatTri("Imane Mokded", "55 917 772",
                "Télévendeur IT (9 mois)", "Téléopérateur", "RDV", "Accepté"));
        candidats.add(new CandidatTri("Ferchichi Aziza", "90 435 050",
                "S.J.L Tunisie", "Téléopérateur", "", "Pas accepté"));
        candidats.add(new CandidatTri("Nadine Ben Hassine", "24 122 832",
                "Téléopératrice", "Téléopérateur", "", "Pas accepté"));
    }

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<CandidatTri, String>() {
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
                    if ("Accepté".equalsIgnoreCase(item)) {
                        styleClass = "statut-accepte";
                    } else if ("Pas accepté".equalsIgnoreCase(item) || "Non accepté".equalsIgnoreCase(item)) {
                        styleClass = "statut-pas-accepte";
                    } else if ("En attente".equalsIgnoreCase(item)) {
                        styleClass = "statut-en-attente";
                    }

                    if (!styleClass.isEmpty()) {
                        // Créer un Label avec le style
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

    public static class CandidatTri {
        private final String nom;
        private final String telephone;
        private final String experience;
        private final String poste;
        private final String commentaire;
        private final String statut;

        public CandidatTri(String nom, String telephone, String experience, String poste, String commentaire, String statut) {
            this.nom = nom;
            this.telephone = telephone;
            this.experience = experience;
            this.poste = poste;
            this.commentaire = commentaire;
            this.statut = statut;
        }

        public String getNom() { return nom; }
        public String getTelephone() { return telephone; }
        public String getExperience() { return experience; }
        public String getPoste() { return poste; }
        public String getCommentaire() { return commentaire; }
        public String getStatut() { return statut; }
    }
}