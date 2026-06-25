package com.rh.contollers;

import com.rh.models.EntretienPhysique;
import com.rh.services.ExportEntretienExcelService;
import com.rh.services.ServiceEntretienPhysique;
import com.rh.services.ServiceTriTelephonique;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EntretienPhysiqueController {

    @FXML
    private TableView<EntretienPhysique> tableView;

    @FXML
    private TableColumn<EntretienPhysique, String> colNom;
    @FXML
    private TableColumn<EntretienPhysique, String> colDateRdv;
    @FXML
    private TableColumn<EntretienPhysique, String> colPoste;
    @FXML
    private TableColumn<EntretienPhysique, String> colStatut;
    @FXML
    private TableColumn<EntretienPhysique, Void> colActions;

    @FXML
    private Label lblTotalCandidats;
    @FXML
    private Label lblRetenus;
    @FXML
    private Label lblAbsents;
    @FXML
    private Label lblReportes;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFiltreStatut;

    private ServiceEntretienPhysique service;
    private ServiceTriTelephonique serviceTriTelephonique;
    private ObservableList<EntretienPhysique> entretiens = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur EntretienPhysique ===");

            // Initialiser les services
            service = new ServiceEntretienPhysique();
            serviceTriTelephonique = new ServiceTriTelephonique();

            // ── Configurer les colonnes ──
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
            colDateRdv.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
            colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Styliser la colonne statut
            styliserStatuts();

            // Configurer la colonne Actions (uniquement supprimer)
            configurerColonneActions();

            // Ajouter le double-clic pour modifier
            ajouterDoubleClic();

            // ── Configurer le filtre par statut ──
            if (comboFiltreStatut != null) {
                comboFiltreStatut.setItems(FXCollections.observableArrayList(
                        "Tous", "En attente", "Retenue", "Absente", "Reporté"
                ));
                comboFiltreStatut.setValue("Tous");
            }

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(entretiens);

            // ── Mettre à jour les statistiques ──
            mettreAJourStatistiques();

            // ── Écouteurs ──
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        rechercherEntretiens(newValue);
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

    // ── Double-clic pour modifier ──

    private void ajouterDoubleClic() {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                EntretienPhysique entretien = tableView.getSelectionModel().getSelectedItem();
                if (entretien != null) {
                    ouvrirFormulaireModification(entretien);
                }
            }
        });
    }

    private void ouvrirFormulaireModification(EntretienPhysique entretien) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_entretien.fxml"));
            VBox root = loader.load();

            ModifierEntretienController controller = loader.getController();
            controller.setEntretien(entretien);
            controller.setOnModificationReussie(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier un entretien");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification.", Alert.AlertType.ERROR);
        }
    }

    // ── Configuration de la colonne Actions (uniquement supprimer) ──

    private void configurerColonneActions() {
        colActions.setCellFactory(column -> new TableCell<EntretienPhysique, Void>() {
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnSupprimer.setStyle("-fx-font-size: 14px; -fx-padding: 4 8; -fx-background-color: #fde8e9; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer l'entretien"));

                btnSupprimer.setOnAction(event -> {
                    EntretienPhysique entretien = getTableView().getItems().get(getIndex());
                    supprimerEntretien(entretien);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSupprimer);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    // ── Suppression d'un entretien ──

    private void supprimerEntretien(EntretienPhysique entretien) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'entretien");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'entretien de " + entretien.getNomComplet() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(entretien);
                chargerDonnees();
                showAlert("Succès", "Entretien supprimé avec succès !", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression : " + e.getMessage());
                showAlert("Erreur", "Erreur lors de la suppression.", Alert.AlertType.ERROR);
            }
        }
    }

    // ── Chargement des données ──

    private void chargerDonnees() {
        try {
            System.out.println("Chargement des données depuis la BD...");
            List<EntretienPhysique> liste = service.getAll();
            System.out.println("Nombre d'entretiens récupérés : " + liste.size());

            entretiens.setAll(liste);
            mettreAJourStatistiques();
            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void rechercherEntretiens(String keyword) {
        try {
            entretiens.setAll(service.search(keyword));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    private void filtrerParStatut(String statut) {
        try {
            entretiens.setAll(service.filterByStatut(statut));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du filtrage", Alert.AlertType.ERROR);
        }
    }

    // ── Style des statuts ──

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<EntretienPhysique, String>() {
            private final Label badge = new Label();

            {
                badge.setAlignment(Pos.CENTER);
                badge.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String statut = item;
                    badge.setText(statut);

                    if (statut.contains("Retenue") || statut.contains("Retenu")) {
                        badge.setStyle("-fx-background-color: #4F6815; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if (statut.contains("Absent") || statut.contains("Absente")) {
                        badge.setStyle("-fx-background-color: #75070C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if (statut.contains("Reporté") || statut.contains("Reporte")) {
                        badge.setStyle("-fx-background-color: #FFEDAB; -fx-text-fill: #7a5c00; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else {
                        badge.setStyle("-fx-background-color: #F0E6DA; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    // ── Statistiques ──

    private void mettreAJourStatistiques() {
        int total = entretiens.size();
        int retenus = 0;
        int absents = 0;
        int reportes = 0;

        for (EntretienPhysique e : entretiens) {
            String statut = e.getStatut();
            if (statut != null) {
                if (statut.contains("Retenue") || statut.contains("Retenu")) {
                    retenus++;
                } else if (statut.contains("Absent") || statut.contains("Absente")) {
                    absents++;
                } else if (statut.contains("Reporté") || statut.contains("Reporte")) {
                    reportes++;
                }
            }
        }

        lblTotalCandidats.setText(String.valueOf(total));
        lblRetenus.setText(String.valueOf(retenus));
        lblAbsents.setText(String.valueOf(absents));
        lblReportes.setText(String.valueOf(reportes));
    }

    // ── Actions boutons ──

    @FXML
    private void onAjouterEntretien() {
        showAlert("Information", "Fonctionnalité d'ajout à implémenter.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onExporter() {
        if (entretiens.isEmpty()) {
            showAlert("Attention", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        try {
            ExportEntretienExcelService exportService = new ExportEntretienExcelService();
            Stage stage = (Stage) tableView.getScene().getWindow();
            boolean success = exportService.exporterVersExcel(entretiens, stage);

            if (success) {
                showAlert("Succès", "Exportation réussie ! Le fichier va s'ouvrir automatiquement.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "L'exportation a été annulée ou a échoué.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'exportation : " + e.getMessage(), Alert.AlertType.ERROR);
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

    // ── Méthodes utilitaires ──

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}