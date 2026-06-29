package com.rh.contollers;

import com.rh.models.Fournisseur;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class FournisseurController {

    @FXML
    private TableView<Fournisseur> tableView;

    @FXML
    private TableColumn<Fournisseur, String> colNom;
    @FXML
    private TableColumn<Fournisseur, String> colEmail;
    @FXML
    private TableColumn<Fournisseur, String> colTelephone;
    @FXML
    private TableColumn<Fournisseur, String> colVille;
    @FXML
    private TableColumn<Fournisseur, String> colPays;
    @FXML
    private TableColumn<Fournisseur, String> colMatriculeFiscal;
    @FXML
    private TableColumn<Fournisseur, String> colDateCreation;
    @FXML
    private TableColumn<Fournisseur, Void> colActions;

    @FXML
    private Label lblTotalFournisseurs;

    @FXML
    private TextField txtRecherche;

    private ServiceFournisseur service;
    private ObservableList<Fournisseur> fournisseurs = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur Fournisseur ===");

            service = new ServiceFournisseur();

            // ── Configurer les colonnes ──
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
            colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
            colMatriculeFiscal.setCellValueFactory(new PropertyValueFactory<>("matriculeFiscal"));

            // Colonne DATE D'AJOUT avec formatage
            colDateCreation.setCellValueFactory(cellData -> {
                Fournisseur f = cellData.getValue();
                if (f.getDateCreation() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            f.getDateCreation().format(dateFormatter)
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Configurer la colonne Actions (uniquement supprimer)
            configurerColonneActions();

            // Ajouter le double-clic pour modifier
            ajouterDoubleClic();

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(fournisseurs);

            // ── Mettre à jour le compteur ──
            mettreAJourCompteur();

            // ── Écouteur pour la recherche ──
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        chargerDonnees();
                    } else {
                        rechercherFournisseurs(newValue);
                    }
                });
            }

            System.out.println("=== Initialisation terminée ===");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Configuration de la colonne Actions (uniquement supprimer) ──

    private void configurerColonneActions() {
        colActions.setCellFactory(column -> new TableCell<Fournisseur, Void>() {
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnSupprimer.setStyle("-fx-font-size: 14px; -fx-padding: 4 8; -fx-background-color: #fde8e9; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer le fournisseur"));

                btnSupprimer.setOnAction(event -> {
                    Fournisseur fournisseur = getTableView().getItems().get(getIndex());
                    supprimerFournisseur(fournisseur);
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
                Fournisseur fournisseur = tableView.getSelectionModel().getSelectedItem();
                if (fournisseur != null) {
                    ouvrirFormulaireModification(fournisseur);
                }
            }
        });
    }

    // ── Ouvrir formulaire d'ajout ──

    @FXML
    private void onAjouterFournisseur() {
        ouvrirFormulaireAjout();
    }

    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/ajouter_fournisseur.fxml"));
            VBox root = loader.load();

            AjouterFournisseurController controller = loader.getController();
            controller.setOnAjoutReussi(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Ajouter un fournisseur");
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

    // ── Ouvrir formulaire de modification (double-clic) ──

    private void ouvrirFormulaireModification(Fournisseur fournisseur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_fournisseur.fxml"));
            VBox root = loader.load();

            ModifierFournisseurController controller = loader.getController();
            controller.setFournisseur(fournisseur);
            controller.setOnModificationReussie(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier un fournisseur");
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

    private void supprimerFournisseur(Fournisseur fournisseur) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le fournisseur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le fournisseur " + fournisseur.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(fournisseur);
                chargerDonnees();
                showAlert("Succès", "Fournisseur supprimé avec succès !", Alert.AlertType.INFORMATION);
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
            List<Fournisseur> liste = service.getAll();
            System.out.println("Nombre de fournisseurs récupérés : " + liste.size());

            fournisseurs.setAll(liste);
            mettreAJourCompteur();
            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void rechercherFournisseurs(String keyword) {
        try {
            fournisseurs.setAll(service.search(keyword));
            mettreAJourCompteur();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    // ── Mise à jour du compteur ──

    private void mettreAJourCompteur() {
        if (lblTotalFournisseurs != null) {
            lblTotalFournisseurs.setText(fournisseurs.size() + " fournisseur(s)");
        }
    }

    // ── Actions ──

    @FXML
    private void onExporter() {
        showAlert("Information", "Fonctionnalité d'export à implémenter.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onReinitialiser() {
        if (txtRecherche != null) {
            txtRecherche.clear();
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