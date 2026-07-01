package com.rh.contollers;

import com.rh.models.Facture;
import com.rh.models.Fournisseur;
import com.rh.services.ExportExcelServiceUnifie;
import com.rh.services.ServiceFacture;
import com.rh.services.ServiceFournisseur;
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
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FactureController {

    @FXML
    private TableView<Facture> tableView;

    @FXML
    private TableColumn<Facture, String> colNumero;
    @FXML
    private TableColumn<Facture, String> colFournisseur;
    @FXML
    private TableColumn<Facture, Double> colMontantHt;
    @FXML
    private TableColumn<Facture, Double> colMontantTva;
    @FXML
    private TableColumn<Facture, Double> colMontantTtc;
    @FXML
    private TableColumn<Facture, String> colDateFacture;
    @FXML
    private TableColumn<Facture, String> colDateEcheance;
    @FXML
    private TableColumn<Facture, String> colStatut;
    @FXML
    private TableColumn<Facture, String> colModePaiement;
    @FXML
    private TableColumn<Facture, Void> colActions;

    @FXML
    private Label lblTotalFactures;
    @FXML
    private Label lblMontantTotal;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFiltreStatut;

    private ServiceFacture service;
    private ServiceFournisseur serviceFournisseur;
    private ObservableList<Facture> factures = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur Facture ===");

            service = new ServiceFacture();
            serviceFournisseur = new ServiceFournisseur();

            // ── Configurer les colonnes ──
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroFacture"));
            colFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));

            // Colonne MONTANT HT avec formatage
            colMontantHt.setCellValueFactory(new PropertyValueFactory<>("montantHt"));
            colMontantHt.setCellFactory(column -> new TableCell<Facture, Double>() {
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

            // Colonne TVA avec formatage
            colMontantTva.setCellValueFactory(new PropertyValueFactory<>("montantTva"));
            colMontantTva.setCellFactory(column -> new TableCell<Facture, Double>() {
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

            // Colonne MONTANT TTC avec formatage
            colMontantTtc.setCellValueFactory(new PropertyValueFactory<>("montantTtc"));
            colMontantTtc.setCellFactory(column -> new TableCell<Facture, Double>() {
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

            colDateFacture.setCellValueFactory(new PropertyValueFactory<>("dateFactureFormatee"));
            colDateEcheance.setCellValueFactory(new PropertyValueFactory<>("dateEcheanceFormatee"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statutLabel"));
            colModePaiement.setCellValueFactory(new PropertyValueFactory<>("modePaiementLabel"));

            // Styliser la colonne statut
            styliserStatuts();

            // Configurer la colonne Actions
            configurerColonneActions();

            // Ajouter le double-clic pour modifier
            ajouterDoubleClic();

            // ── Configurer le filtre par statut ──
            if (comboFiltreStatut != null) {
                comboFiltreStatut.setItems(FXCollections.observableArrayList(
                        "Tous", "En attente", "Payée", "Annulée"
                ));
                comboFiltreStatut.setValue("Tous");
            }

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(factures);

            // ── Mettre à jour les statistiques ──
            mettreAJourStatistiques();

            // ── Écouteurs ──
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        rechercherFactures(newValue);
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

    // ── Style des statuts ──

    private void styliserStatuts() {
        colStatut.setCellFactory(column -> new TableCell<Facture, String>() {
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

                    if ("Payée".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #4F6815; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("En attente".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #FFEDAB; -fx-text-fill: #7a5c00; -fx-font-weight: bold; -fx-padding: 4 14; -fx-background-radius: 12; -fx-font-size: 11px;");
                    } else if ("Annulée".equalsIgnoreCase(item)) {
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

    // ── Configuration de la colonne Actions ──

    private void configurerColonneActions() {
        colActions.setCellFactory(column -> new TableCell<Facture, Void>() {
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnSupprimer.setStyle("-fx-font-size: 14px; -fx-padding: 4 8; -fx-background-color: #fde8e9; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer la facture"));

                btnSupprimer.setOnAction(event -> {
                    Facture facture = getTableView().getItems().get(getIndex());
                    supprimerFacture(facture);
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

    // ── Double-clic pour modifier ──

    private void ajouterDoubleClic() {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Facture facture = tableView.getSelectionModel().getSelectedItem();
                if (facture != null) {
                    ouvrirFormulaireModification(facture);
                }
            }
        });
    }

    // ── Ouvrir formulaire d'ajout ──

    @FXML
    private void onAjouterFacture() {
        ouvrirFormulaireAjout();
    }

    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/ajouter_facture.fxml"));
            VBox root = loader.load();

            AjouterFactureController controller = loader.getController();

            // Charger la liste des fournisseurs pour le ComboBox
            List<Fournisseur> fournisseurs = serviceFournisseur.getAll();
            controller.setFournisseurs(fournisseurs);

            controller.setOnAjoutReussi(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Ajouter une facture");
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

    // ── Ouvrir formulaire de modification ──

    private void ouvrirFormulaireModification(Facture facture) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_facture.fxml"));
            VBox root = loader.load();

            ModifierFactureController controller = loader.getController();

            // Charger la liste des fournisseurs pour le ComboBox
            List<Fournisseur> fournisseurs = serviceFournisseur.getAll();
            controller.setFournisseurs(fournisseurs);

            controller.setFacture(facture);
            controller.setOnModificationReussie(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier une facture");
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

    // ── Suppression ──

    private void supprimerFacture(Facture facture) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la facture");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la facture " + facture.getNumeroFacture() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(facture);
                chargerDonnees();
                showAlert("Succès", "Facture supprimée avec succès !", Alert.AlertType.INFORMATION);
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
            List<Facture> liste = service.getAll();
            System.out.println("Nombre de factures récupérées : " + liste.size());

            factures.setAll(liste);
            mettreAJourStatistiques();
            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void rechercherFactures(String keyword) {
        try {
            factures.setAll(service.search(keyword));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    private void filtrerParStatut(String statut) {
        try {
            factures.setAll(service.filterByStatut(statut));
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du filtrage", Alert.AlertType.ERROR);
        }
    }

    // ── Mise à jour des statistiques ──

    private void mettreAJourStatistiques() {
        int total = factures.size();
        double montantTotal = 0;

        for (Facture f : factures) {
            montantTotal += f.getMontantTtc();
        }

        if (lblTotalFactures != null) {
            lblTotalFactures.setText(String.valueOf(total));
        }

        if (lblMontantTotal != null) {
            lblMontantTotal.setText(String.format("%.2f DT", montantTotal));
        }
    }

    // ── Actions ──

    @FXML
    private void onExporter() {
        if (factures.isEmpty()) {
            showAlert("Attention", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        try {
            ExportExcelServiceUnifie exportService = new ExportExcelServiceUnifie();
            Stage stage = (Stage) tableView.getScene().getWindow();
            boolean success = exportService.exporterFactures(factures, stage);

            if (success) {
                showAlert("Succès", "Exportation réussie ! Le fichier va s'ouvrir automatiquement.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'exportation : " + e.getMessage(), Alert.AlertType.ERROR);
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

    // ── Méthodes utilitaires ──

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}