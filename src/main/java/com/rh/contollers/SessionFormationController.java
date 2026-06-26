package com.rh.contollers;

import com.rh.models.ParticipationFormation;
import com.rh.services.ExportSessionExcelService;
import com.rh.services.ParticipationFormationService;
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

public class SessionFormationController {

    @FXML
    private TableView<ParticipationFormation> tableView;

    @FXML
    private TableColumn<ParticipationFormation, String> colNom;
    @FXML
    private TableColumn<ParticipationFormation, String> colDateFormation;
    @FXML
    private TableColumn<ParticipationFormation, String> colPoste;
    @FXML
    private TableColumn<ParticipationFormation, String> colStatut;
    @FXML
    private TableColumn<ParticipationFormation, Void> colActions;

    @FXML
    private Label lblTotalCandidats;
    @FXML
    private Label lblEnCours;
    @FXML
    private Label lblTermine;
    @FXML
    private Label lblAnnule;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFiltreStatut;

    private ParticipationFormationService service;
    private ObservableList<ParticipationFormation> participations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur SessionFormation ===");

            service = new ParticipationFormationService();

            // ── Configurer les colonnes ──
            colNom.setCellValueFactory(cellData -> {
                ParticipationFormation p = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(p.getNomComplet());
            });

            colDateFormation.setCellValueFactory(cellData -> {
                ParticipationFormation p = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(p.getDateFormationFormatee());
            });

            colPoste.setCellValueFactory(new PropertyValueFactory<>("formation"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Styliser la colonne statut
            styliserStatuts();

            // Configurer la colonne Actions (supprimer)
            configurerColonneActions();

            // Ajouter le double-clic pour modifier
            ajouterDoubleClic();

            // ── Configurer le filtre par statut ──
            if (comboFiltreStatut != null) {
                comboFiltreStatut.setItems(FXCollections.observableArrayList(
                        "Tous", "Présente", "Absente", "FPF"
                ));
                comboFiltreStatut.setValue("Tous");
            }

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(participations);

            // ── Mettre à jour les statistiques ──
            mettreAJourStatistiques();

            // ── Écouteurs ──
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        rechercherParticipations(newValue);
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
                ParticipationFormation participation = tableView.getSelectionModel().getSelectedItem();
                if (participation != null) {
                    ouvrirFormulaireModification(participation);
                }
            }
        });
    }

    private void ouvrirFormulaireModification(ParticipationFormation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_participation.fxml"));
            VBox root = loader.load();

            ModifierParticipationController controller = loader.getController();
            controller.setParticipation(participation);
            controller.setOnModificationReussie(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier une participation");
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

    // ── Configuration de la colonne Actions (Supprimer) ──

    private void configurerColonneActions() {
        colActions.setCellFactory(column -> new TableCell<ParticipationFormation, Void>() {
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnSupprimer.setStyle("-fx-font-size: 14px; -fx-padding: 4 8; -fx-background-color: #fde8e9; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer la participation"));

                btnSupprimer.setOnAction(event -> {
                    ParticipationFormation participation = getTableView().getItems().get(getIndex());
                    supprimerParticipation(participation);
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

    // ── Suppression d'une participation ──

    private void supprimerParticipation(ParticipationFormation participation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la participation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la participation de " + participation.getNomComplet() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(participation);
                chargerDonnees();
                showAlert("Succès", "Participation supprimée avec succès !", Alert.AlertType.INFORMATION);
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
            List<ParticipationFormation> liste = service.getAll();
            System.out.println("Nombre de participations récupérées : " + liste.size());

            participations.setAll(liste);
            mettreAJourStatistiques();
            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base de données.");
        }
    }

    private void rechercherParticipations(String keyword) {
        try {
            participations.setAll(service.search(keyword));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche");
        }
    }

    private void filtrerParStatut(String statut) {
        try {
            participations.setAll(service.filterByStatut(statut));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du filtrage");
        }
    }

    // ── Style des statuts ──

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<ParticipationFormation, String>() {
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
                    badge.setText(item);

                    if ("Présente".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #4F6815; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("Absente".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #75070C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("FPF".equalsIgnoreCase(item)) {
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
        int total = participations.size();
        int presentes = 0;
        int absentes = 0;
        int fpf = 0;

        for (ParticipationFormation p : participations) {
            String statut = p.getStatut();
            if (statut != null) {
                if ("Présente".equalsIgnoreCase(statut)) {
                    presentes++;
                } else if ("Absente".equalsIgnoreCase(statut)) {
                    absentes++;
                } else if ("FPF".equalsIgnoreCase(statut)) {
                    fpf++;
                }
            }
        }

        lblTotalCandidats.setText(String.valueOf(total));
        lblEnCours.setText(String.valueOf(presentes));
        lblTermine.setText(String.valueOf(fpf));
        lblAnnule.setText(String.valueOf(absentes));
    }

    // ── Actions boutons ──

    @FXML
    private void onAjouterSession() {
        showAlert("Information", "Fonctionnalité d'ajout à implémenter.");
    }

    @FXML
    private void onExporter() {
        if (participations.isEmpty()) {
            showAlert("Attention", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        try {
            ExportSessionExcelService exportService = new ExportSessionExcelService();
            Stage stage = (Stage) tableView.getScene().getWindow();
            boolean success = exportService.exporterVersExcel(participations, stage);

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}