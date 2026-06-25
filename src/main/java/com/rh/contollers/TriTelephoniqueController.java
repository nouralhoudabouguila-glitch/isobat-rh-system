package com.rh.contollers;

import com.rh.models.TriTelephonique;
import com.rh.services.ExportExcelService;
import com.rh.services.ServiceTriTelephonique;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TriTelephoniqueController {

    // ── TableView ──
    @FXML
    private TableView<TriTelephonique> tableView;

    @FXML
    private TableColumn<TriTelephonique, String> colNom;
    @FXML
    private TableColumn<TriTelephonique, String> colTel;
    @FXML
    private TableColumn<TriTelephonique, String> colExperience;
    @FXML
    private TableColumn<TriTelephonique, String> colPoste;
    @FXML
    private TableColumn<TriTelephonique, String> colCommentaire;
    @FXML
    private TableColumn<TriTelephonique, String> colStatut;
    @FXML
    private TableColumn<TriTelephonique, Void> colActions;

    // ── Labels ──
    @FXML
    private Label lblTotalCandidats;
    @FXML
    private Label lblPageActuelle;

    // ── Champs de recherche et filtres ──
    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFiltreStatut;
    @FXML
    private ComboBox<Integer> comboLignesParPage;

    // ── Boutons de pagination ──
    @FXML
    private Button btnPagePrecedente;
    @FXML
    private Button btnPageSuivante;

    // ── Service ──
    private ServiceTriTelephonique service;

    // ── Données ──
    private ObservableList<TriTelephonique> tousLesCandidats = FXCollections.observableArrayList();
    private ObservableList<TriTelephonique> candidatsAffiches = FXCollections.observableArrayList();

    private int pageActuelle = 0;
    private int lignesParPage = 10;

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur TriTelephonique ===");

            // Initialiser le service
            service = new ServiceTriTelephonique();

            // ── Configurer les colonnes ──
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
            colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            colExperience.setCellValueFactory(new PropertyValueFactory<>("experience"));
            colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
            colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

            // Configurer la colonne Statut
            configurerColonneStatut();

            // Configurer la colonne Actions
            configurerColonneActions();

            // Ajouter le double-clic
            ajouterDoubleClic();

            // ── Configurer la combo de lignes par page ──
            if (comboLignesParPage != null) {
                comboLignesParPage.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
                comboLignesParPage.setValue(10);

                // Écouteur pour changer le nombre de lignes par page
                comboLignesParPage.setOnAction(e -> {
                    lignesParPage = comboLignesParPage.getValue();
                    pageActuelle = 0;
                    appliquerPagination();
                });
            }

            // ── Configurer le filtre par statut ──
            if (comboFiltreStatut != null) {
                comboFiltreStatut.setItems(FXCollections.observableArrayList("Tous", "Accepté", "Pas accepté", "En attente"));
                comboFiltreStatut.setValue("Tous");
            }

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(candidatsAffiches);

            // ── Mettre à jour le compteur ──
            mettreAJourCompteur();

            // ── Écouteurs ──
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        rechercherCandidats(newValue);
                    }
                });
            }

            if (comboFiltreStatut != null) {
                comboFiltreStatut.setOnAction(e -> {
                    String statut = comboFiltreStatut.getValue();
                    if ("Tous".equals(statut)) {
                        chargerDonnees();
                    } else {
                        filtrerParStatut(statut);
                    }
                });
            }

            System.out.println("=== Initialisation terminée ===");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Pagination ──

    private void appliquerPagination() {
        if (tousLesCandidats.isEmpty()) {
            candidatsAffiches.clear();
            mettreAJourCompteur();
            return;
        }

        int total = tousLesCandidats.size();
        int start = pageActuelle * lignesParPage;
        int end = Math.min(start + lignesParPage, total);

        if (start >= total && total > 0) {
            pageActuelle = (total - 1) / lignesParPage;
            start = pageActuelle * lignesParPage;
            end = Math.min(start + lignesParPage, total);
        }

        List<TriTelephonique> sousListe = new ArrayList<>(tousLesCandidats.subList(start, end));
        candidatsAffiches.setAll(sousListe);

        mettreAJourCompteur();
        mettreAJourPagination();
        tableView.refresh();
    }

    private void mettreAJourPagination() {
        int total = tousLesCandidats.size();
        int totalPages = (total + lignesParPage - 1) / lignesParPage;

        if (lblPageActuelle != null) {
            lblPageActuelle.setText((pageActuelle + 1) + "/" + Math.max(1, totalPages));
        }

        if (btnPagePrecedente != null) {
            btnPagePrecedente.setDisable(pageActuelle == 0);
        }

        if (btnPageSuivante != null) {
            btnPageSuivante.setDisable(pageActuelle >= totalPages - 1 || totalPages == 0);
        }
    }

    @FXML
    private void onPagePrecedente() {
        if (pageActuelle > 0) {
            pageActuelle--;
            appliquerPagination();
        }
    }

    @FXML
    private void onPageSuivante() {
        int total = tousLesCandidats.size();
        int totalPages = (total + lignesParPage - 1) / lignesParPage;
        if (pageActuelle < totalPages - 1) {
            pageActuelle++;
            appliquerPagination();
        }
    }

    // ── Actions sur les candidats ──

    private void accepterCandidat(TriTelephonique candidat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Accepter le candidat");
        alert.setContentText("Êtes-vous sûr de vouloir accepter " + candidat.getNomComplet() +
                " ?\n\nIl sera automatiquement ajouté à la liste des entretiens physiques.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = service.accepterCandidat(candidat.getIdCand());
                if (success) {
                    afficherSucces("Candidat accepté ! Il a été ajouté à l'entretien physique.");
                    chargerDonnees();
                } else {
                    afficherErreur("Erreur lors de l'acceptation du candidat.");
                }
            } catch (Exception e) {
                afficherErreur("Erreur : " + e.getMessage());
            }
        }
    }

    private void refuserCandidat(TriTelephonique candidat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Refuser le candidat");
        alert.setContentText("Êtes-vous sûr de vouloir refuser " + candidat.getNomComplet() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = service.refuserCandidat(candidat.getIdCand());
                if (success) {
                    afficherSucces("Candidat refusé.");
                    chargerDonnees();
                } else {
                    afficherErreur("Erreur lors du refus du candidat.");
                }
            } catch (Exception e) {
                afficherErreur("Erreur : " + e.getMessage());
            }
        }
    }

    // ── Configuration de la colonne Statut ──

    private void configurerColonneStatut() {
        colStatut.setCellFactory(column -> new TableCell<TriTelephonique, String>() {
            private final Label badge = new Label();

            {
                badge.setAlignment(Pos.CENTER);
                badge.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    TriTelephonique candidat = getTableView().getItems().get(getIndex());
                    String statut = (candidat != null && candidat.getStatut() != null && !candidat.getStatut().isEmpty())
                            ? candidat.getStatut()
                            : "En attente";

                    badge.setText(statut);

                    if ("Accepté".equalsIgnoreCase(statut)) {
                        badge.setStyle("-fx-background-color: #4F6815; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("Pas accepté".equalsIgnoreCase(statut)) {
                        badge.setStyle("-fx-background-color: #75070C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else {
                        badge.setStyle("-fx-background-color: #FFEDAB; -fx-text-fill: #7a5c00; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    // ── Configuration de la colonne Actions ──

    private void configurerColonneActions() {
        colActions.setCellFactory(column -> new TableCell<TriTelephonique, Void>() {
            private final Button btnAccepter = new Button("✅");
            private final Button btnRefuser = new Button("❌");
            private final Button btnSupprimer = new Button("🗑️");
            private final HBox container = new HBox(5, btnAccepter, btnRefuser, btnSupprimer);

            {
                btnAccepter.setStyle("-fx-font-size: 14px; -fx-padding: 4 6; -fx-background-color: transparent; -fx-cursor: hand;");
                btnRefuser.setStyle("-fx-font-size: 14px; -fx-padding: 4 6; -fx-background-color: transparent; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-font-size: 14px; -fx-padding: 4 6; -fx-background-color: transparent; -fx-cursor: hand;");

                btnAccepter.setTooltip(new Tooltip("Accepter le candidat"));
                btnRefuser.setTooltip(new Tooltip("Refuser le candidat"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer le candidat"));

                btnAccepter.setOnAction(event -> {
                    TriTelephonique candidat = getTableView().getItems().get(getIndex());
                    accepterCandidat(candidat);
                });

                btnRefuser.setOnAction(event -> {
                    TriTelephonique candidat = getTableView().getItems().get(getIndex());
                    refuserCandidat(candidat);
                });

                btnSupprimer.setOnAction(event -> {
                    TriTelephonique candidat = getTableView().getItems().get(getIndex());
                    supprimerCandidat(candidat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    // ── Double-clic pour modifier ──

    private void ajouterDoubleClic() {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TriTelephonique candidat = tableView.getSelectionModel().getSelectedItem();
                if (candidat != null) {
                    ouvrirFormulaireModification(candidat);
                }
            }
        });
    }

    private void ouvrirFormulaireModification(TriTelephonique candidat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_candidat.fxml"));
            VBox root = loader.load();

            ModifierCandidatController controller = loader.getController();
            controller.setCandidat(candidat);
            controller.setOnModificationReussie(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier un candidat");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
            afficherErreur("Impossible d'ouvrir le formulaire de modification.");
        }
    }

    // ── Chargement des données ──

    private void chargerDonnees() {
        try {
            System.out.println("Chargement des données depuis la BD...");
            java.util.List<TriTelephonique> liste = service.getAll();
            System.out.println("Nombre de candidats récupérés : " + liste.size());

            tousLesCandidats.setAll(liste);
            pageActuelle = 0;
            appliquerPagination();

            System.out.println("Candidats dans l'ObservableList : " + tousLesCandidats.size());

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Impossible de charger les données depuis la base de données.");
        }
    }

    private void rechercherCandidats(String keyword) {
        try {
            java.util.List<TriTelephonique> liste = service.search(keyword);
            tousLesCandidats.setAll(liste);
            pageActuelle = 0;
            appliquerPagination();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            afficherErreur("Erreur lors de la recherche");
        }
    }

    private void filtrerParStatut(String statut) {
        try {
            java.util.List<TriTelephonique> liste = service.filterByStatut(statut);
            tousLesCandidats.setAll(liste);
            pageActuelle = 0;
            appliquerPagination();
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            afficherErreur("Erreur lors du filtrage");
        }
    }

    // ── Actions ──

    private void supprimerCandidat(TriTelephonique candidat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le candidat");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + candidat.getNomComplet() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(candidat);
                tousLesCandidats.remove(candidat);
                appliquerPagination();
                afficherSucces("Candidat supprimé avec succès !");
            } catch (Exception e) {
                afficherErreur("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    // ── Mise à jour du compteur ──

    private void mettreAJourCompteur() {
        if (lblTotalCandidats != null) {
            int total = tousLesCandidats.size();
            int affiches = candidatsAffiches.size();
            lblTotalCandidats.setText(affiches + " / " + total + " candidat(s)");
        }
    }

    // ── Actions boutons ──

    @FXML
    private void onAjouterCandidat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/ajouter_candidat.fxml"));
            VBox root = loader.load();

            AjouterCandidatController controller = loader.getController();
            controller.setOnAjoutReussi(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Ajouter un candidat");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
            afficherErreur("Impossible d'ouvrir le formulaire d'ajout.");
        }
    }

    @FXML
    private void onExporter() {
        if (tousLesCandidats.isEmpty()) {
            afficherAvertissement("Aucune donnée à exporter.");
            return;
        }

        try {
            ExportExcelService exportService = new ExportExcelService();
            Stage stage = (Stage) tableView.getScene().getWindow();
            boolean success = exportService.exporterVersExcel(tousLesCandidats, stage);

            if (success) {
                afficherSucces("Exportation réussie ! Le fichier va s'ouvrir automatiquement.");
            } else {
                afficherErreur("L'exportation a été annulée ou a échoué.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur lors de l'exportation : " + e.getMessage());
        }
    }

    @FXML
    private void onFiltrer() {
        if (comboFiltreStatut != null) {
            String statut = comboFiltreStatut.getValue();
            if ("Tous".equals(statut)) {
                chargerDonnees();
            } else {
                filtrerParStatut(statut);
            }
        }
    }

    @FXML
    private void onReinitialiser() {
        if (txtRecherche != null) {
            txtRecherche.clear();
        }
        if (comboFiltreStatut != null) {
            comboFiltreStatut.setValue("Tous");
        }
        chargerDonnees();
    }

    // ── Méthodes d'affichage des alertes ──

    private void afficherInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherAvertissement(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}