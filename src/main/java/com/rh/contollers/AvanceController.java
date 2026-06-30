package com.rh.contollers;

import com.rh.models.Avance;
import com.rh.services.ServiceAvance;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AvanceController {

    // ── TableView ──
    @FXML
    private TableView<Avance> tableView;

    @FXML
    private TableColumn<Avance, String> colEmploye;
    @FXML
    private TableColumn<Avance, String> colDepartement;
    @FXML
    private TableColumn<Avance, Double> colMontant;
    @FXML
    private TableColumn<Avance, String> colMotif;
    @FXML
    private TableColumn<Avance, String> colDate;
    @FXML
    private TableColumn<Avance, String> colStatut;
    @FXML
    private TableColumn<Avance, String> colCommentaire;
    @FXML
    private TableColumn<Avance, Void> colActions;

    // ── Labels des statistiques ──
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

    // ── Champs de recherche et filtres ──
    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFiltreStatut;

    // ── Services et données ──
    private ServiceAvance service;
    private ObservableList<Avance> avances = FXCollections.observableArrayList();
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur Avance ===");

            service = new ServiceAvance();

            // ── Configurer les colonnes ──
            // Colonne EMPLOYÉ
            colEmploye.setCellValueFactory(cellData -> {
                Avance a = cellData.getValue();
                if (a.getEmploye() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            a.getEmploye().getPrenom() + " " + a.getEmploye().getNom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Colonne DÉPARTEMENT
            colDepartement.setCellValueFactory(cellData -> {
                Avance a = cellData.getValue();
                if (a.getEmploye() != null && a.getEmploye().getDepartement() != null) {
                    // Afficher le label (ex: "Centre d'appel B2B") au lieu du nom enum
                    return new javafx.beans.property.SimpleStringProperty(
                            a.getEmploye().getDepartement().toString()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Colonne MONTANT
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setCellFactory(column -> new TableCell<Avance, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(format.format(item) + " DT");
                        setAlignment(Pos.CENTER_RIGHT);
                    }
                }
            });

            // Colonne MOTIF
            colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

            // Colonne DATE
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemandeFormatee"));

            // Colonne STATUT
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statutLabel"));

            // Colonne COMMENTAIRE
            colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

            // Styliser la colonne statut
            styliserStatuts();

            // Ajouter les boutons d'action (uniquement Valider, Refuser, Supprimer - pas Modifier)
            ajouterBoutonsActions();

            // ── Configurer le filtre par statut ──
            if (comboFiltreStatut != null) {
                comboFiltreStatut.setItems(FXCollections.observableArrayList(
                        "Tous", "En attente", "Validée", "Refusée"
                ));
                comboFiltreStatut.setValue("Tous");
            }

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(avances);

            // ── Mettre à jour les statistiques ──
            mettreAJourStatistiques();

            // ── Écouteurs ──

            // Recherche en temps réel
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        rechercherAvances(newValue);
                    }
                });
            }

            // Filtre par statut
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

    // ── Style des statuts ──

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<Avance, String>() {
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

                    if ("Validée".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #4F6815; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("En attente".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #FFEDAB; -fx-text-fill: #7a5c00; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("Refusée".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #75070C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else {
                        badge.setStyle("-fx-background-color: #F0E6DA; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    // ── Boutons d'action (uniquement Valider, Refuser, Supprimer) ──

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<Avance, Void>() {
            private final Button btnValider = new Button("✅");
            private final Button btnRefuser = new Button("❌");
            private final Button btnSupprimer = new Button("🗑️");
            private final HBox container = new HBox(4, btnValider, btnRefuser, btnSupprimer);

            {
                btnValider.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #e8f0d8; -fx-background-radius: 4; -fx-cursor: hand;");
                btnValider.setTooltip(new Tooltip("Valider la demande"));

                btnRefuser.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #fde8e9; -fx-background-radius: 4; -fx-cursor: hand;");
                btnRefuser.setTooltip(new Tooltip("Refuser la demande"));

                btnSupprimer.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #fde8e9; -fx-background-radius: 4; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer la demande"));

                btnValider.setOnAction(event -> {
                    Avance avance = getTableView().getItems().get(getIndex());
                    validerAvance(avance);
                });

                btnRefuser.setOnAction(event -> {
                    Avance avance = getTableView().getItems().get(getIndex());
                    refuserAvance(avance);
                });

                btnSupprimer.setOnAction(event -> {
                    Avance avance = getTableView().getItems().get(getIndex());
                    supprimerAvance(avance);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Avance avance = getTableView().getItems().get(getIndex());
                    String statut = avance.getStatutLabel();

                    // Afficher les boutons selon le statut
                    if ("En attente".equals(statut)) {
                        btnValider.setVisible(true);
                        btnRefuser.setVisible(true);
                    } else {
                        btnValider.setVisible(false);
                        btnRefuser.setVisible(false);
                    }

                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    // ── Ouvrir formulaire d'ajout ──

    @FXML
    private void onNouvelleDemande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/ajouter_avance.fxml"));
            VBox root = loader.load();

            AjouterAvanceController controller = loader.getController();
            controller.setOnAjoutReussi(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Nouvelle demande d'avance");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout.", Alert.AlertType.ERROR);
        }
    }

    // ── Actions sur les avances ──

    private void validerAvance(Avance avance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Valider la demande d'avance");
        alert.setContentText("Êtes-vous sûr de vouloir valider cette demande d'avance ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = service.validerAvance(avance.getIdAvance());
                if (success) {
                    avance.setStatut("Validée");
                    avance.setDateValidation(java.time.LocalDate.now());
                    tableView.refresh();
                    mettreAJourStatistiques();
                    showAlert("Succès", "Demande validée avec succès !", Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la validation : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void refuserAvance(Avance avance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Refuser la demande d'avance");
        alert.setContentText("Êtes-vous sûr de vouloir refuser cette demande d'avance ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = service.refuserAvance(avance.getIdAvance());
                if (success) {
                    avance.setStatut("Refusée");
                    avance.setDateValidation(java.time.LocalDate.now());
                    tableView.refresh();
                    mettreAJourStatistiques();
                    showAlert("Succès", "Demande refusée avec succès !", Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors du refus : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void supprimerAvance(Avance avance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la demande d'avance");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette demande d'avance ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(avance);
                chargerDonnees();
                showAlert("Succès", "Demande supprimée avec succès !", Alert.AlertType.INFORMATION);
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
            List<Avance> liste = service.getAll();
            System.out.println("Nombre d'avances récupérées : " + liste.size());

            avances.setAll(liste);
            mettreAJourStatistiques();
            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void rechercherAvances(String keyword) {
        try {
            avances.setAll(service.search(keyword));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    private void filtrerParStatut(String statut) {
        try {
            avances.setAll(service.filterByStatut(statut));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du filtrage", Alert.AlertType.ERROR);
        }
    }

    // ── Mise à jour des statistiques ──

    private void mettreAJourStatistiques() {
        int enAttente = 0;
        int validees = 0;
        int refusees = 0;
        double totalAvance = 0.0;

        for (Avance a : avances) {
            String statut = a.getStatutLabel();
            if ("En attente".equals(statut)) {
                enAttente++;
            } else if ("Validée".equals(statut)) {
                validees++;
                totalAvance += a.getMontant();
            } else if ("Refusée".equals(statut)) {
                refusees++;
            }
        }

        lblEnAttente.setText(String.valueOf(enAttente));
        lblValidees.setText(String.valueOf(validees));
        lblRefusees.setText(String.valueOf(refusees));
        lblTotalAvance.setText(String.format("%.0f DT", totalAvance));
        lblTotalDemandes.setText(String.valueOf(avances.size()));
    }

    // ── Actions ──

    @FXML
    private void onRetour() {
        MainController.loadPage("paie", "Gestion de la Paie");
    }

    @FXML
    private void onExporter() {
        showAlert("Information", "Fonctionnalité d'export à implémenter.", Alert.AlertType.INFORMATION);
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