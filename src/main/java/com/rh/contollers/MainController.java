package com.rh.contollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    private static MainController instance;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label headerTitle;

    @FXML
    private Button btnRecrutement;

    @FXML
    private VBox subMenuRecrutement;

    private PaieController paieController;

    @FXML
    public void initialize() {
        instance = this;

        // Configurer le sous-menu Recrutement
        if (btnRecrutement != null && subMenuRecrutement != null) {
            btnRecrutement.setOnAction(e -> {
                boolean visible = subMenuRecrutement.isVisible();
                subMenuRecrutement.setVisible(!visible);
                subMenuRecrutement.setManaged(!visible);
                btnRecrutement.setText(visible ? "🧑‍💼   Recrutement  ▶" : "🧑‍💼   Recrutement  ▼");
            });
        }

        // Charger la page par défaut
        loadPage("dashboard", "Tableau de bord");
    }

    public static MainController getInstance() {
        return instance;
    }

    /**
     * Charge une page dans le conteneur principal (méthode d'instance)
     */
    public void loadPage(String page, String title) {
        try {
            String fxmlPath = "/com/rh/views/" + page + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            // Mettre à jour le titre
            if (headerTitle != null) {
                headerTitle.setText(title);
            }

            // Stocker le contrôleur si c'est la page Paie
            if ("paie".equals(page)) {
                paieController = loader.getController();
            }

            // Charger le contenu
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
            }

            System.out.println("Page chargée : " + page);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page " + page + " : " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Erreur : contentArea est null. Vérifiez l'ID dans le FXML.");
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit la page Paie si elle est affichée
     */
    public void rafraichirPaie() {
        if (paieController != null) {
            System.out.println("Rafraîchissement de la page Paie...");
            paieController.actualiser();
        }
    }

    // ── Méthodes de navigation pour les boutons du sidebar ──

    @FXML
    private void onDashboard() {
        loadPage("dashboard", "Tableau de bord");
    }

    @FXML
    private void onEmploye() {
        loadPage("employe", "Gestion des Employés");
    }

    @FXML
    private void onPresence() {
        loadPage("presence", "Gestion des Présences");
    }

    @FXML
    private void onConges() {
        loadPage("conges", "Gestion des Congés");
    }

    @FXML
    private void onPlanning() {
        loadPage("planning", "Planning");
    }

    @FXML
    private void onRecrutement() {
        if (subMenuRecrutement != null) {
            boolean visible = subMenuRecrutement.isVisible();
            subMenuRecrutement.setVisible(!visible);
            subMenuRecrutement.setManaged(!visible);
            btnRecrutement.setText(visible ? "🧑‍💼   Recrutement  ▶" : "🧑‍💼   Recrutement  ▼");
        }
    }

    @FXML
    private void onTriTelephonique() {
        loadPage("tri_telephonique", "Tri Téléphonique");
    }

    @FXML
    private void onEntretienPhysique() {
        loadPage("entretien_physique", "Entretien Physique");
    }

    @FXML
    private void onSessionFormation() {
        loadPage("session_formation", "Session de Formation");
    }

    @FXML
    private void onPaie() {
        loadPage("paie", "Gestion de la Paie");
    }

    @FXML
    private void onAvance() {
        loadPage("avance", "Demandes d'avance sur salaire");
    }

    @FXML
    private void onTicket() {
        loadPage("ticket", "Tickets Restaurant");
    }

    @FXML
    private void onRetenue() {
        loadPage("retenue", "Gestion des Retenues");
    }

    @FXML
    private void onDepense() {
        loadPage("depense", "Gestion des Dépenses");
    }

    @FXML
    private void onFacture() {
        loadPage("facture", "Gestion des Factures");
    }

    @FXML
    private void onFournisseur() {
        loadPage("fournisseur", "Gestion des Fournisseurs");
    }

    @FXML
    private void onParametres() {
        loadPage("parametres", "Paramètres");
    }

    @FXML
    private void onLogout() {
        System.out.println("Déconnexion");
    }
}