package com.rh.contollers;

import com.rh.models.Employe;
import com.rh.models.Departement;
import com.rh.services.ExportExcelServiceUnifie;
import com.rh.services.ServiceAvance;
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
import java.util.List;
import java.util.Locale;

public class DetailSalaireController {

    @FXML
    private TableView<EmployeSalaire> tableView;

    @FXML
    private TableColumn<EmployeSalaire, String> colNom;
    @FXML
    private TableColumn<EmployeSalaire, String> colRib;
    @FXML
    private TableColumn<EmployeSalaire, Double> colMontant;

    @FXML
    private Label lblTitre;
    @FXML
    private Label lblTotalGeneral;
    @FXML
    private Label lblTotalBas;
    @FXML
    private Label lblNbEmployes;
    @FXML
    private Label lblCentre;

    // Variable statique pour recevoir le centre depuis DepenseController
    private static String centreActuel = "Centre d'appel B2B";

    public static void setCentre(String centre) {
        centreActuel = centre;
    }

    private ServiceAvance service;
    private ObservableList<EmployeSalaire> employes = FXCollections.observableArrayList();
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du contrôleur DetailSalaire ===");
            System.out.println("Centre sélectionné : " + centreActuel);

            service = new ServiceAvance();

            // ── Configurer les colonnes ──
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colRib.setCellValueFactory(new PropertyValueFactory<>("rib"));
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));

            // Formater la colonne montant
            colMontant.setCellFactory(column -> new TableCell<EmployeSalaire, Double>() {
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

            // Ajouter le double-clic pour modifier
            ajouterDoubleClic();

            // Afficher le centre
            lblCentre.setText("Centre : " + centreActuel);
            lblTitre.setText("💼  Détails des Salaires - " + centreActuel);

            // Charger les données selon le centre
            chargerDonnees(centreActuel);

            // Lier les données à la table
            tableView.setItems(employes);

            // Calculer et afficher le total
            calculerTotal();

            // Afficher le nombre d'employés
            lblNbEmployes.setText(String.valueOf(employes.size()));

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
                EmployeSalaire employe = tableView.getSelectionModel().getSelectedItem();
                if (employe != null && !employe.getNom().equals("Aucun employé dans ce département")) {
                    ouvrirFormulaireModification(employe);
                }
            }
        });
    }

    private void ouvrirFormulaireModification(EmployeSalaire employe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/modifier_salaire.fxml"));
            VBox root = loader.load();

            ModifierSalaireController controller = loader.getController();
            controller.setEmploye(employe);
            controller.setOnModificationReussie(() -> {
                // Rafraîchir les données après modification
                calculerTotal();
                lblNbEmployes.setText(String.valueOf(employes.size()));
                tableView.refresh();
            });

            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/rh/styles/main.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier le salaire");
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

    private void chargerDonnees(String centre) {
        try {
            System.out.println("Chargement des employés pour le centre : " + centre);

            // Récupérer le département correspondant au centre
            Departement departement = null;
            for (Departement d : Departement.values()) {
                if (d.toString().equals(centre)) {
                    departement = d;
                    break;
                }
            }

            if (departement == null) {
                System.err.println("Département non trouvé pour le centre : " + centre);
                employes.clear();
                employes.add(new EmployeSalaire("Aucun département trouvé", "", 0.00));
                tableView.refresh();
                return;
            }

            // Récupérer les employés du département
            List<Employe> employesList = service.getEmployesByDepartement(centre);
            System.out.println("Nombre d'employés trouvés : " + employesList.size());

            // Vider la liste et ajouter les nouveaux employés
            employes.clear();

            for (Employe e : employesList) {
                double salaire = e.getSalaireMensuelNet() > 0 ? e.getSalaireMensuelNet() : 2500.00;
                System.out.println("Employé trouvé : " + e.getNom() + " " + e.getPrenom() + " - Salaire: " + salaire);
                employes.add(new EmployeSalaire(
                        e.getNom() + " " + e.getPrenom(),
                        e.getnRIB() != null ? e.getnRIB() : "Non renseigné",
                        salaire
                ));
            }

            // Si aucun employé trouvé, afficher un message
            if (employes.isEmpty()) {
                System.out.println("Aucun employé trouvé pour le département " + centre);
                employes.add(new EmployeSalaire("Aucun employé dans ce département", "", 0.00));
            }

            tableView.refresh();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des employés : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calculerTotal() {
        double total = 0.0;
        for (EmployeSalaire e : employes) {
            total += e.getMontant();
        }
        String totalFormate = format.format(total) + " DT";
        lblTotalGeneral.setText(totalFormate);
        lblTotalBas.setText(totalFormate);
    }

    @FXML
    private void onRetour() {
        MainController.getInstance().loadPage("depense", "Gestion des Dépenses");
    }

    @FXML
    private void onExporter() {
        if (employes.isEmpty()) {
            showAlert("Attention", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        try {
            ExportExcelServiceUnifie exportService = new ExportExcelServiceUnifie();
            Stage stage = (Stage) tableView.getScene().getWindow();
            boolean success = exportService.exporterSalaires(employes, stage, centreActuel);

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
    private void onActualiser() {
        chargerDonnees(centreActuel);
        calculerTotal();
        lblNbEmployes.setText(String.valueOf(employes.size()));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Classe interne pour les données ──────────────────────────────────────

    public static class EmployeSalaire {
        private String nom;
        private String rib;
        private double montant;

        public EmployeSalaire(String nom, String rib, double montant) {
            this.nom = nom;
            this.rib = rib;
            this.montant = montant;
        }

        public String getNom() { return nom; }
        public String getRib() { return rib; }
        public double getMontant() { return montant; }

        public void setMontant(double montant) { this.montant = montant; }
    }
}