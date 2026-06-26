package com.rh.contollers;

import com.rh.models.User;
import com.rh.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label     headerTitle;
    @FXML private ImageView logoImg;
    @FXML private Label     avatarLabel;
    @FXML private Label     userNameLabel;
    @FXML private VBox      sidebar;

    @FXML private Button btnDashboard;
    @FXML private Button btnEmploye;
    @FXML private Button btnPresence;
    @FXML private Button btnConges;
    @FXML private Button btnPlanning;
    @FXML private Button btnFournisseur;
    @FXML private Button btnFacture;
    @FXML private Button btnDepense;
    @FXML private Button btnParametres;

    private List<Button> navButtons;
    private ContextMenu avatarMenu;
    private CustomMenuItem usersCustomItem; // Pour pouvoir le cacher

    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnEmploye, btnPresence, btnConges,
                btnPlanning, btnFournisseur, btnFacture, btnDepense, btnParametres);
        loadLogo();
        loadView("dashboard", "Tableau de bord", btnDashboard);

        updateUserInfo();
        createAvatarMenu();
    }

    // ── Chargement du logo ───────────────────────────────────────────────────

    private void loadLogo() {
        InputStream is = getClass().getResourceAsStream("/com/rh/images/logo-Isobat.png");
        if (is != null) {
            logoImg.setImage(new Image(is));
        }
    }

    // ── Mise à jour des infos utilisateur ──────────────────────────────────

    public void updateUserInfo() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String initiales = user.getInitiales();
            if (avatarLabel != null) {
                avatarLabel.setText(initiales);
                Tooltip.install(avatarLabel, new Tooltip(user.getNomComplet() + "\n" + user.getRole().getLabel()));
            }
            if (userNameLabel != null) {
                userNameLabel.setText(user.getNomComplet());
            }
        } else {
            if (avatarLabel != null) {
                avatarLabel.setText("?");
            }
            if (userNameLabel != null) {
                userNameLabel.setText("Invité");
            }
        }

        // Mettre à jour le menu si déjà créé
        if (avatarMenu != null) {
            updateAvatarMenuVisibility();
        }
    }

    // ── Création du menu contextuel de l'avatar ─────────────────────────────

    private void createAvatarMenu() {
        avatarMenu = new ContextMenu();
        avatarMenu.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-color: #E0D0BE; -fx-border-radius: 8; -fx-padding: 4;");

        // Item Profil
        Label profilLabel = new Label("👤  Mon profil");
        profilLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #333333; -fx-background-radius: 4;");
        profilLabel.setMaxWidth(Double.MAX_VALUE);
        profilLabel.setOnMouseEntered(e -> profilLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #333333; -fx-background-color: #F5F0EA; -fx-background-radius: 4;"));
        profilLabel.setOnMouseExited(e -> profilLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #333333; -fx-background-radius: 4;"));
        profilLabel.setOnMouseClicked(e -> showProfil());
        CustomMenuItem profilCustomItem = new CustomMenuItem(profilLabel, false);
        profilCustomItem.setHideOnClick(true);

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #F0E6DA;");
        CustomMenuItem sepItem = new CustomMenuItem(sep, false);
        sepItem.setDisable(true);

        // Item Gestion des utilisateurs (Admin uniquement)
        Label usersLabel = new Label("👥  Gestion des utilisateurs");
        usersLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #333333; -fx-background-radius: 4;");
        usersLabel.setMaxWidth(Double.MAX_VALUE);
        usersLabel.setOnMouseEntered(e -> usersLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #333333; -fx-background-color: #F5F0EA; -fx-background-radius: 4;"));
        usersLabel.setOnMouseExited(e -> usersLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #333333; -fx-background-radius: 4;"));
        usersLabel.setOnMouseClicked(e -> loadView("users", "Gestion des Utilisateurs", null));
        usersCustomItem = new CustomMenuItem(usersLabel, false);
        usersCustomItem.setHideOnClick(true);

        // Item Déconnexion
        Label logoutLabel = new Label("🚪  Déconnexion");
        logoutLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #75070C; -fx-background-radius: 4;");
        logoutLabel.setMaxWidth(Double.MAX_VALUE);
        logoutLabel.setOnMouseEntered(e -> logoutLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #75070C; -fx-background-color: #FFF0F0; -fx-background-radius: 4;"));
        logoutLabel.setOnMouseExited(e -> logoutLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-padding: 10 25 10 25; -fx-text-fill: #75070C; -fx-background-radius: 4;"));
        logoutLabel.setOnMouseClicked(e -> handleLogout());
        CustomMenuItem logoutCustomItem = new CustomMenuItem(logoutLabel, false);
        logoutCustomItem.setHideOnClick(true);

        // Ajouter les items au menu
        avatarMenu.getItems().addAll(profilCustomItem, sepItem, usersCustomItem, logoutCustomItem);

        // Cacher l'option Gestion des utilisateurs si non admin
        updateAvatarMenuVisibility();

        // Associer le menu à l'avatar
        if (avatarLabel != null) {
            avatarLabel.setOnMouseClicked(this::showAvatarMenu);
        }
    }

    // ── Mettre à jour la visibilité du menu ────────────────────────────────

    private void updateAvatarMenuVisibility() {
        if (usersCustomItem == null) return;

        User user = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = user != null && user.getRole() == User.Role.ADMIN;

        // 🔥 CORRECTION : Utiliser setVisible uniquement
        usersCustomItem.setVisible(isAdmin);
    }

    // ── Afficher le menu contextuel ─────────────────────────────────────────

    @FXML
    private void showAvatarMenu(MouseEvent event) {
        if (avatarMenu != null) {
            avatarMenu.show(avatarLabel, event.getScreenX(), event.getScreenY());
        }
    }

    // ── Afficher le profil ──────────────────────────────────────────────────

    private void showProfil() {
        loadView("profil", "Mon Profil", null);
    }

    // ── Déconnexion ──────────────────────────────────────────────────────────

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirm.setContentText("Vous serez redirigé vers la page de connexion.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Button yesButton = (Button) confirm.getDialogPane().lookupButton(ButtonType.YES);
        yesButton.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-color: #75070C; -fx-text-fill: #FFFFFF; " +
                "-fx-background-radius: 6px; -fx-padding: 6 20 6 20; -fx-cursor: hand;");

        Button noButton = (Button) confirm.getDialogPane().lookupButton(ButtonType.NO);
        noButton.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-color: #FFFFFF; -fx-text-fill: #555555; " +
                "-fx-border-color: #E0D0BE; -fx-border-radius: 6px; -fx-padding: 6 20 6 20; -fx-cursor: hand;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                SessionManager.getInstance().logout();

                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/rh/views/Login.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) avatarLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                    stage.setTitle("ISOBAT — Connexion");
                    stage.setMaximized(false);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ── Navigation handlers ──────────────────────────────────────────────────

    @FXML private void onDashboard()   { loadView("dashboard",    "Tableau de bord", btnDashboard);   }
    @FXML private void onEmploye()     { loadView("employe",      "Employés",         btnEmploye);     }
    @FXML private void onPresence()    { loadView("presence",     "Présence",          btnPresence);    }
    @FXML private void onConges()      { loadView("conges",       "Congés",            btnConges);      }
    @FXML private void onPlanning()    { loadView("planning",     "Planning",          btnPlanning);    }
    @FXML private void onFournisseur() { loadView("fournisseur",  "Fournisseurs",      btnFournisseur); }
    @FXML private void onFacture()     { loadView("facture",      "Factures",          btnFacture);     }
    @FXML private void onDepense()     { loadView("depense",      "Dépenses",          btnDepense);     }
    @FXML private void onParametres()  { loadView("parametres",   "Paramètres",        btnParametres);  }

    // ── Chargement d'une vue ─────────────────────────────────────────────────

    private void loadView(String viewName, String title, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rh/views/" + viewName + ".fxml"));
            Parent view = loader.load();

            // Charger le CSS
            String cssPath = "/com/rh/styles/main.css";
            if (getClass().getResource(cssPath) != null) {
                view.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            contentArea.getChildren().setAll(view);
            headerTitle.setText(title);
            if (activeBtn != null) setActiveButton(activeBtn);
        } catch (IOException e) {
            e.printStackTrace();
            headerTitle.setText("⚠ Erreur: " + viewName + ".fxml");
        }
    }

    // ── État actif du bouton ─────────────────────────────────────────────────

    private void setActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            btn.getStyleClass().removeAll("sidebar-btn-active");
            if (!btn.getStyleClass().contains("sidebar-btn")) {
                btn.getStyleClass().add("sidebar-btn");
            }
        }
        if (!activeBtn.getStyleClass().contains("sidebar-btn-active")) {
            activeBtn.getStyleClass().add("sidebar-btn-active");
        }
    }
}