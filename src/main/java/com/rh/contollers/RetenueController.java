package com.rh.contollers;

import com.rh.models.Departement;
import com.rh.models.Retenue;
import com.rh.services.ServiceRetenue;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class RetenueController {

    // ── TableView ──
    @FXML
    private TableView<Retenue> tableView;

    @FXML
    private TableColumn<Retenue, String> colEmploye;
    @FXML
    private TableColumn<Retenue, String> colDepartement;
    @FXML
    private TableColumn<Retenue, Double> colMontant;
    @FXML
    private TableColumn<Retenue, String> colMotif;
    @FXML
    private TableColumn<Retenue, String> colDate;
    @FXML
    private TableColumn<Retenue, String> colCommentaire;
    @FXML
    private TableColumn<Retenue, Void> colActions;

    // ── Labels des statistiques ──
    @FXML
    private Label lblTotalRetenues;
    @FXML
    private Label lblMontantTotal;
    @FXML
    private Label lblNbEmployes;

    // ── Champs de recherche et filtres ──
    @FXML
    private TextField txtRecherche;
    @FXML
    private DatePicker datePickerFiltre;
    @FXML
    private ComboBox<String> comboDepartement;

    private ServiceRetenue service;
    private ObservableList<Retenue> retenues = FXCollections.observableArrayList();
    private ObservableList<Retenue> retenuesFiltrees = FXCollections.observableArrayList();
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur Retenue ===");

            service = new ServiceRetenue();

            // ── Configurer les colonnes ──
            colEmploye.setCellValueFactory(cellData -> {
                Retenue r = cellData.getValue();
                if (r.getEmploye() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            r.getEmploye().getPrenom() + " " + r.getEmploye().getNom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            colDepartement.setCellValueFactory(cellData -> {
                Retenue r = cellData.getValue();
                if (r.getEmploye() != null && r.getEmploye().getDepartement() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            r.getEmploye().getDepartement().toString()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setCellFactory(column -> new TableCell<Retenue, Double>() {
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

            colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateRetenueFormatee"));
            colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

            // Ajouter les boutons d'action
            ajouterBoutonsActions();

            // ── Configurer les filtres ──
            configurerFiltres();

            // ── Charger les données ──
            chargerDonnees();

            // ── Lier les données à la table ──
            tableView.setItems(retenuesFiltrees);

            // ── Mettre à jour les statistiques ──
            mettreAJourStatistiques();

            // ── Écouteurs ──

            // Recherche en temps réel
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        appliquerFiltres();
                    } else {
                        rechercherRetenues(newValue);
                    }
                });
            }

            System.out.println("=== Initialisation terminée ===");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurerFiltres() {
        // ── Date picker ──
        if (datePickerFiltre != null) {
            datePickerFiltre.setValue(LocalDate.now());
            datePickerFiltre.setOnAction(e -> appliquerFiltres());
        }

        // ── ComboBox départements ──
        if (comboDepartement != null) {
            try {
                List<String> departements = service.getDepartements();
                comboDepartement.getItems().clear();
                comboDepartement.getItems().add("Tous les départements");

                // Convertir les noms d'enum en labels lisibles
                for (String dept : departements) {
                    for (Departement d : Departement.values()) {
                        if (d.name().equals(dept)) {
                            comboDepartement.getItems().add(d.toString());
                            break;
                        }
                    }
                }
                comboDepartement.setValue("Tous les départements");
                comboDepartement.setOnAction(e -> appliquerFiltres());
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des départements : " + e.getMessage());
            }
        }
    }

    private void appliquerFiltres() {
        try {
            LocalDate dateFiltre = datePickerFiltre.getValue();
            String departement = comboDepartement.getValue();

            List<Retenue> liste = retenues.stream()
                    .filter(r -> {
                        // Filtrer par date
                        if (dateFiltre != null && !r.getDateRetenue().equals(dateFiltre)) {
                            return false;
                        }
                        // Filtrer par département
                        if (departement != null && !"Tous les départements".equals(departement)) {
                            if (r.getEmploye() == null || r.getEmploye().getDepartement() == null) {
                                return false;
                            }
                            return r.getEmploye().getDepartement().toString().equals(departement);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            retenuesFiltrees.setAll(liste);
            mettreAJourStatistiques();

            System.out.println("Filtrage appliqué - Date: " + dateFiltre + ", Département: " + departement + " - " + liste.size() + " retenues");

        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
        }
    }

    // ── Boutons d'action ──

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<Retenue, Void>() {
            private final Button btnModifier = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");
            private final HBox container = new HBox(4, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #FFEDAB; -fx-background-radius: 4; -fx-cursor: hand;");
                btnModifier.setTooltip(new Tooltip("Modifier la retenue"));

                btnSupprimer.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #fde8e9; -fx-background-radius: 4; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer la retenue"));

                btnModifier.setOnAction(event -> {
                    Retenue retenue = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(retenue);
                });

                btnSupprimer.setOnAction(event -> {
                    Retenue retenue = getTableView().getItems().get(getIndex());
                    supprimerRetenue(retenue);
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

    // ── Ouvrir formulaire d'ajout ──

    @FXML
    private void onAjouterRetenue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/ajouter_retenue.fxml"));
            VBox root = loader.load();

            AjouterRetenueController controller = loader.getController();
            controller.setOnAjoutReussi(() -> {
                chargerDonnees();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Ajouter une retenue");
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

    private void ouvrirFormulaireModification(Retenue retenue) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_retenue.fxml"));
            VBox root = loader.load();

            ModifierRetenueController controller = loader.getController();
            controller.setRetenue(retenue);
            controller.setOnModificationReussie(() -> {
                appliquerFiltres();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier une retenue");
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

    private void supprimerRetenue(Retenue retenue) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la retenue");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette retenue ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(retenue);
                chargerDonnees();
                showAlert("Succès", "Retenue supprimée avec succès !", Alert.AlertType.INFORMATION);
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
            List<Retenue> liste = service.getAll();
            System.out.println("Nombre de retenues récupérées : " + liste.size());

            retenues.setAll(liste);
            retenuesFiltrees.setAll(liste);
            mettreAJourStatistiques();
            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données depuis la base de données.", Alert.AlertType.ERROR);
        }
    }

    private void rechercherRetenues(String keyword) {
        try {
            List<Retenue> liste = service.search(keyword);

            // Appliquer les filtres supplémentaires
            LocalDate dateFiltre = datePickerFiltre.getValue();
            String departement = comboDepartement.getValue();

            List<Retenue> listeFiltree = liste.stream()
                    .filter(r -> {
                        if (dateFiltre != null && !r.getDateRetenue().equals(dateFiltre)) {
                            return false;
                        }
                        if (departement != null && !"Tous les départements".equals(departement)) {
                            if (r.getEmploye() == null || r.getEmploye().getDepartement() == null) {
                                return false;
                            }
                            return r.getEmploye().getDepartement().toString().equals(departement);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            retenuesFiltrees.setAll(listeFiltree);
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    // ── Statistiques ──

    private void mettreAJourStatistiques() {
        int total = retenuesFiltrees.size();
        double montantTotal = 0.0;

        for (Retenue r : retenuesFiltrees) {
            montantTotal += r.getMontant();
        }

        if (lblTotalRetenues != null) {
            lblTotalRetenues.setText(String.valueOf(total));
        }
        if (lblMontantTotal != null) {
            lblMontantTotal.setText(format.format(montantTotal) + " DT");
        }
        if (lblNbEmployes != null) {
            long nbEmployes = retenuesFiltrees.stream().map(Retenue::getIdEmploye).distinct().count();
            lblNbEmployes.setText(String.valueOf(nbEmployes));
        }
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
    private void onReinitialiser() {
        if (txtRecherche != null) {
            txtRecherche.clear();
        }
        if (datePickerFiltre != null) {
            datePickerFiltre.setValue(LocalDate.now());
        }
        if (comboDepartement != null) {
            comboDepartement.setValue("Tous les départements");
        }
        retenuesFiltrees.setAll(retenues);
        mettreAJourStatistiques();
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