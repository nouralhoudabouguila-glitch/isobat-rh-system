package com.rh.contollers;

import com.rh.models.TicketEmploye;
import com.rh.services.ServiceTicket;
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

public class TicketController {

    @FXML
    private TableView<TicketEmploye> tableView;

    @FXML
    private TableColumn<TicketEmploye, String> colNom;
    @FXML
    private TableColumn<TicketEmploye, String> colPrenom;
    @FXML
    private TableColumn<TicketEmploye, String> colDepartement;
    @FXML
    private TableColumn<TicketEmploye, Integer> colNbJours;
    @FXML
    private TableColumn<TicketEmploye, Double> colMontant;
    @FXML
    private TableColumn<TicketEmploye, Double> colTotal;
    @FXML
    private TableColumn<TicketEmploye, Void> colActions;

    @FXML
    private Label lblTotalTickets;
    @FXML
    private Label lblTotalMontant;
    @FXML
    private Label lblTotalBas;
    @FXML
    private Label lblNbEmployes;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboMois;
    @FXML
    private ComboBox<Integer> comboAnnee;

    private ServiceTicket service;
    private ObservableList<TicketEmploye> tickets = FXCollections.observableArrayList();
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur Ticket ===");

            service = new ServiceTicket();

            // ── Configurer les colonnes ──
            colNom.setCellValueFactory(cellData -> {
                TicketEmploye t = cellData.getValue();
                if (t.getEmploye() != null) {
                    return new javafx.beans.property.SimpleStringProperty(t.getEmploye().getNom());
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            colPrenom.setCellValueFactory(cellData -> {
                TicketEmploye t = cellData.getValue();
                if (t.getEmploye() != null) {
                    return new javafx.beans.property.SimpleStringProperty(t.getEmploye().getPrenom());
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            colDepartement.setCellValueFactory(cellData -> {
                TicketEmploye t = cellData.getValue();
                if (t.getEmploye() != null && t.getEmploye().getDepartement() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            t.getEmploye().getDepartement().toString()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            colNbJours.setCellValueFactory(new PropertyValueFactory<>("nbJours"));
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montantParTicket"));
            colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

            // Formater les colonnes montant
            colMontant.setCellFactory(column -> new TableCell<TicketEmploye, Double>() {
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

            colTotal.setCellFactory(column -> new TableCell<TicketEmploye, Double>() {
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

            // Ajouter les boutons d'action
            ajouterBoutonsActions();

            // ── Configurer les filtres ──
            configurerFiltres();

            // ── Charger les données du mois en cours ──
            chargerDonneesParDefaut();

            // ── Lier les données à la table ──
            tableView.setItems(tickets);

            // ── Mettre à jour les statistiques ──
            mettreAJourStatistiques();

            // ── Écouteur pour la recherche ──
            if (txtRecherche != null) {
                txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        filtrerParMoisAnnee();
                    } else {
                        rechercherTickets(newValue);
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
        // Mois
        if (comboMois != null) {
            comboMois.setItems(FXCollections.observableArrayList(
                    "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
            ));
            // Sélectionner le mois en cours
            String moisActuel = LocalDate.now().getMonth().toString();
            comboMois.setValue(moisActuel);
            comboMois.setOnAction(e -> filtrerParMoisAnnee());
        }

        // Année
        if (comboAnnee != null) {
            int anneeActuelle = LocalDate.now().getYear();
            comboAnnee.setItems(FXCollections.observableArrayList(anneeActuelle - 2, anneeActuelle - 1, anneeActuelle, anneeActuelle + 1, anneeActuelle + 2));
            comboAnnee.setValue(anneeActuelle);
            comboAnnee.setOnAction(e -> filtrerParMoisAnnee());
        }
    }

    private void chargerDonneesParDefaut() {
        // Charger les tickets du mois et année en cours
        if (comboMois != null && comboAnnee != null) {
            String mois = comboMois.getValue();
            Integer annee = comboAnnee.getValue();
            if (mois != null && annee != null) {
                try {
                    List<TicketEmploye> liste = service.findByMoisAnnee(mois, annee);
                    tickets.setAll(liste);
                    System.out.println("Tickets chargés pour " + mois + " " + annee + " : " + liste.size());

                    // Si aucun ticket trouvé, afficher un message
                    if (liste.isEmpty()) {
                        System.out.println("Aucun ticket trouvé pour " + mois + " " + annee);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement : " + e.getMessage());
                }
            }
        }
    }

    private void filtrerParMoisAnnee() {
        if (comboMois != null && comboAnnee != null) {
            String mois = comboMois.getValue();
            Integer annee = comboAnnee.getValue();
            if (mois != null && annee != null) {
                try {
                    // Filtrer par mois et année
                    List<TicketEmploye> liste = service.findByMoisAnnee(mois, annee);
                    tickets.setAll(liste);
                    mettreAJourStatistiques();
                    System.out.println("Filtrage appliqué : " + mois + " " + annee + " - " + liste.size() + " tickets");
                } catch (Exception e) {
                    System.err.println("Erreur lors du filtrage : " + e.getMessage());
                }
            }
        }
    }

    // ── Boutons d'action ──

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(param -> new TableCell<TicketEmploye, Void>() {
            private final Button btnModifier = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");
            private final HBox container = new HBox(4, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #FFEDAB; -fx-background-radius: 4; -fx-cursor: hand;");
                btnModifier.setTooltip(new Tooltip("Modifier le ticket"));

                btnSupprimer.setStyle("-fx-font-size: 13px; -fx-padding: 2 5; -fx-background-color: #fde8e9; -fx-background-radius: 4; -fx-cursor: hand;");
                btnSupprimer.setTooltip(new Tooltip("Supprimer le ticket"));

                btnModifier.setOnAction(event -> {
                    TicketEmploye ticket = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(ticket);
                });

                btnSupprimer.setOnAction(event -> {
                    TicketEmploye ticket = getTableView().getItems().get(getIndex());
                    supprimerTicket(ticket);
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

    // ── Modifier un ticket ──

    private void ouvrirFormulaireModification(TicketEmploye ticket) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_ticket.fxml"));
            VBox root = loader.load();

            ModifierTicketController controller = loader.getController();
            controller.setTicket(ticket);
            controller.setOnModificationReussie(() -> {
                filtrerParMoisAnnee();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier un ticket");
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

    // ── Supprimer un ticket ──

    private void supprimerTicket(TicketEmploye ticket) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le ticket");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce ticket ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(ticket);
                filtrerParMoisAnnee();
                showAlert("Succès", "Ticket supprimé avec succès !", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression : " + e.getMessage());
                showAlert("Erreur", "Erreur lors de la suppression.", Alert.AlertType.ERROR);
            }
        }
    }

    // ── Générer les tickets ──

    @FXML
    private void onGenererTickets() {
        if (comboMois != null && comboAnnee != null) {
            String mois = comboMois.getValue();
            Integer annee = comboAnnee.getValue();

            if (mois == null || annee == null) {
                showAlert("Erreur", "Veuillez sélectionner un mois et une année.", Alert.AlertType.WARNING);
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Générer les tickets");
            alert.setContentText("Voulez-vous générer automatiquement les tickets pour " + mois + " " + annee + " ?\n\n" +
                    "Les tickets seront générés pour tous les employés ayant au moins 2 mois d'ancienneté.\n" +
                    "Montants par département :\n" +
                    "- Centre d'appel B2B/B2C : 15,00 DT\n" +
                    "- Bureau d'étude : 11,00 DT\n" +
                    "- Administration : 30,00 DT");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    int nbGeneres = service.genererTicketsPourMois(mois, annee);

                    if (nbGeneres > 0) {
                        showAlert("Succès", nbGeneres + " ticket(s) généré(s) avec succès !", Alert.AlertType.INFORMATION);
                        filtrerParMoisAnnee();
                    } else {
                        showAlert("Information", "Aucun nouveau ticket généré. Les tickets existent peut-être déjà pour cette période.", Alert.AlertType.INFORMATION);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la génération : " + e.getMessage());
                    showAlert("Erreur", "Erreur lors de la génération des tickets : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    // ── Recherche ──

    private void rechercherTickets(String keyword) {
        try {
            // Rechercher uniquement dans les tickets du mois sélectionné
            List<TicketEmploye> liste = service.search(keyword);
            // Filtrer par mois et année
            String mois = comboMois.getValue();
            Integer annee = comboAnnee.getValue();
            if (mois != null && annee != null) {
                liste.removeIf(t -> !t.getMois().equals(mois) || t.getAnnee() != annee);
            }
            tickets.setAll(liste);
            mettreAJourStatistiques();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    // ── Mise à jour des statistiques ──

    private void mettreAJourStatistiques() {
        int totalTickets = 0;
        double totalMontant = 0.0;

        for (TicketEmploye t : tickets) {
            totalTickets += t.getNbJours();
            totalMontant += t.getTotal();
        }

        String totalFormate = format.format(totalMontant) + " DT";

        if (lblTotalTickets != null) {
            lblTotalTickets.setText(String.valueOf(totalTickets));
        }
        if (lblTotalMontant != null) {
            lblTotalMontant.setText(totalFormate);
        }
        if (lblTotalBas != null) {
            lblTotalBas.setText(totalFormate);
        }
        if (lblNbEmployes != null) {
            lblNbEmployes.setText(String.valueOf(tickets.size()));
        }
    }

    // ── Actions ──

    @FXML
    private void onRetour() {
        MainController.loadPage("paie", "Gestion de la Paie");
    }

    @FXML
    private void onAjouterTicket() {
        showAlert("Information", "Fonctionnalité d'ajout manuel à implémenter.\nUtilisez 'Générer les tickets' pour une génération automatique.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onExporter() {
        showAlert("Information", "Fonctionnalité d'export à implémenter.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onFiltrer() {
        filtrerParMoisAnnee();
    }

    @FXML
    private void onReinitialiser() {
        if (txtRecherche != null) {
            txtRecherche.clear();
        }
        if (comboMois != null) {
            comboMois.setValue(LocalDate.now().getMonth().toString());
        }
        if (comboAnnee != null) {
            comboAnnee.setValue(LocalDate.now().getYear());
        }
        filtrerParMoisAnnee();
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