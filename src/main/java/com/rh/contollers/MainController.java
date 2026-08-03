package com.rh.contollers;

import com.rh.models.Notification;
import com.rh.scheduler.NotificationScheduler;
import com.rh.services.ServiceNotification;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

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

    @FXML
    private Button btnNotifications;

    @FXML
    private Label lblNotifBadge;

    @FXML
    private Button btnChatbotHeader; // UNIQUEMENT le bouton du header

    private PaieController paieController;
    private ServiceNotification serviceNotification;
    private Timeline notificationTimer;
    private Stage panneauNotificationsStage;

    @FXML
    public void initialize() {
        instance = this;
        serviceNotification = new ServiceNotification();

        // Ajouter un listener pour les changements de notifications
        serviceNotification.addListener(() -> {
            Platform.runLater(() -> {
                mettreAJourBadgeNotifications();
                if (panneauNotificationsStage != null && panneauNotificationsStage.isShowing()) {
                    ouvrirPanneauNotifications();
                }
            });
        });

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

        // Configurer les notifications
        if (btnNotifications != null) {
            btnNotifications.setOnAction(e -> ouvrirPanneauNotifications());
        }

        // Configurer le bouton Chatbot dans le header
        if (btnChatbotHeader != null) {
            btnChatbotHeader.setOnAction(e -> loadPage("chatbot", "🤖 Assistant RH"));
        }

        mettreAJourBadgeNotifications();
        demarrerVerificationNotifications();
    }

    /**
     * Démarre la vérification périodique des notifications
     */
    private void demarrerVerificationNotifications() {
        // Vérification immédiate
        verifierNotifications();

        // Vérification toutes les 30 secondes
        notificationTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            serviceNotification.actualiser();
            Platform.runLater(() -> mettreAJourBadgeNotifications());
        }));
        notificationTimer.setCycleCount(Timeline.INDEFINITE);
        notificationTimer.play();

        System.out.println("🔄 Vérification des notifications démarrée (toutes les 30 secondes)");
    }

    /**
     * Vérifie les notifications
     */
    private void verifierNotifications() {
        new Thread(() -> {
            try {
                serviceNotification.verifierEtGenererNotifications();
                Platform.runLater(() -> {
                    mettreAJourBadgeNotifications();
                });
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification des notifications : " + e.getMessage());
            }
        }).start();
    }

    /**
     * Met à jour le badge de notification
     */
    public void mettreAJourBadgeNotifications() {
        if (lblNotifBadge == null) return;

        try {
            int nbNonLues = serviceNotification.compterNonLues();

            if (nbNonLues > 0) {
                lblNotifBadge.setText(String.valueOf(nbNonLues > 99 ? "99+" : nbNonLues));
                lblNotifBadge.setVisible(true);
                lblNotifBadge.setManaged(true);
                lblNotifBadge.setStyle(
                        "-fx-background-color: #75070C; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 9px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 20px; " +
                                "-fx-padding: 1 6 1 6; " +
                                "-fx-min-width: 18px; " +
                                "-fx-alignment: center;"
                );
            } else {
                lblNotifBadge.setVisible(false);
                lblNotifBadge.setManaged(false);
            }

            System.out.println("🔔 Notifications non lues : " + nbNonLues);

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du badge : " + e.getMessage());
        }
    }

    /**
     * Ouvre le panneau de notifications
     */
    @FXML
    private void onNotifications() {
        ouvrirPanneauNotifications();
    }

    /**
     * Crée et affiche le panneau de notifications
     */
    private void ouvrirPanneauNotifications() {
        try {
            List<Notification> notifications = serviceNotification.getAllNotifications();
            int nbNonLues = serviceNotification.compterNonLues();

            panneauNotificationsStage = new Stage();
            panneauNotificationsStage.setTitle("Notifications");
            panneauNotificationsStage.initModality(Modality.WINDOW_MODAL);
            panneauNotificationsStage.initOwner(contentArea.getScene().getWindow());
            panneauNotificationsStage.setResizable(false);
            panneauNotificationsStage.setWidth(700);
            panneauNotificationsStage.setHeight(600);

            VBox root = new VBox(10);
            root.setStyle("-fx-background-color: #F5F0EA; -fx-padding: 20;");
            root.setPrefWidth(680);

            // ── En-tête ──
            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblTitle = new Label("🔔 Notifications");
            lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            lblTitle.setTextFill(Color.web("#1A1A1A"));

            Label lblCount = new Label("(" + notifications.size() + " - " + nbNonLues + " non lue(s))");
            lblCount.setFont(Font.font("Segoe UI", 13));
            lblCount.setTextFill(Color.web(nbNonLues > 0 ? "#CC0000" : "#4F6815"));

            // Bouton Actualiser
            Button btnRefresh = new Button("🔄");
            btnRefresh.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
            btnRefresh.setTooltip(new Tooltip("Actualiser les notifications"));
            btnRefresh.setOnAction(e -> {
                serviceNotification.actualiser();
                panneauNotificationsStage.close();
                ouvrirPanneauNotifications();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnToutLu = new Button("✅ Tout marquer comme lu");
            btnToutLu.setStyle(
                    "-fx-background-color: #4F6815; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 6 14; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
            btnToutLu.setVisible(nbNonLues > 0);
            btnToutLu.setManaged(nbNonLues > 0);
            btnToutLu.setOnAction(e -> {
                serviceNotification.marquerToutCommeLu();
                panneauNotificationsStage.close();
                ouvrirPanneauNotifications();
            });

            Button btnFermer = new Button("✕");
            btnFermer.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand; -fx-text-fill: #666;");
            btnFermer.setOnAction(e -> panneauNotificationsStage.close());

            header.getChildren().addAll(lblTitle, lblCount, btnRefresh, spacer, btnToutLu, btnFermer);

            // ── Liste des notifications ──
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            VBox notifList = new VBox(12);
            notifList.setPadding(new Insets(5));
            notifList.setStyle("-fx-background-color: transparent;");

            if (notifications.isEmpty()) {
                VBox emptyBox = new VBox(15);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(80, 0, 80, 0));

                Label emptyIcon = new Label("📭");
                emptyIcon.setFont(Font.font(56));

                Label emptyText = new Label("Aucune notification");
                emptyText.setFont(Font.font("Segoe UI", 18));
                emptyText.setTextFill(Color.web("#888888"));

                emptyBox.getChildren().addAll(emptyIcon, emptyText);
                notifList.getChildren().add(emptyBox);
            } else {
                for (Notification notif : notifications) {
                    VBox notifCard = createNotificationCard(notif);
                    notifList.getChildren().add(notifCard);
                }
            }

            scrollPane.setContent(notifList);

            // ── Footer ──
            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setPadding(new Insets(10, 0, 0, 0));

            Label lblAutoRefresh = new Label("🔄 Actualisation automatique toutes les 30 secondes");
            lblAutoRefresh.setFont(Font.font("Segoe UI", 10));
            lblAutoRefresh.setTextFill(Color.web("#888888"));

            footer.getChildren().add(lblAutoRefresh);

            root.getChildren().addAll(header, new Separator(), scrollPane, footer);

            Scene scene = new Scene(root);
            panneauNotificationsStage.setScene(scene);
            panneauNotificationsStage.showAndWait();

            // Rafraîchir le badge à la fermeture
            mettreAJourBadgeNotifications();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du panneau : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée une carte de notification
     */
    private VBox createNotificationCard(Notification notif) {
        String bgColor = notif.getFondPriorite();
        String borderColor = notif.isEstLue() ? "#D4C8B8" : "#E0D8D0";
        double borderWidth = notif.isEstLue() ? 1 : 2;

        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-padding: 16 18 14 18;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: " + borderWidth + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        );
        card.setMaxWidth(Double.MAX_VALUE);

        // ── LIGNE 1 : Titre avec icône ──
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = getIconForType(notif);
        Label lblIcon = new Label(icon);
        lblIcon.setFont(Font.font(18));

        Label lblTitre = new Label(notif.getTitre());
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTitre.setStyle("-fx-text-fill: #1A1A1A !important;");
        lblTitre.setWrapText(true);
        HBox.setHgrow(lblTitre, Priority.ALWAYS);

        if (!notif.isEstLue()) {
            Label lblNonLu = new Label("● NON LU");
            lblNonLu.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
            lblNonLu.setStyle("-fx-text-fill: #CC0000 !important;");
            header.getChildren().add(lblNonLu);
        }

        header.getChildren().addAll(lblIcon, lblTitre);

        // ── LIGNE 2 : Message ──
        Label lblMessage = new Label(notif.getMessage());
        lblMessage.setFont(Font.font("Segoe UI", 13));
        lblMessage.setStyle("-fx-text-fill: #333333 !important;");
        lblMessage.setWrapText(true);

        // ── LIGNE 3 : Badges et actions ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(5, 0, 0, 0));

        Label badge = new Label(notif.getBadgePriorite());
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        String couleurBadge = notif.getCouleurPriorite();
        badge.setStyle(
                "-fx-background-color: " + couleurBadge + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 2 10;" +
                        "-fx-background-radius: 10;"
        );
        footer.getChildren().add(badge);

        Label sep1 = new Label("•");
        sep1.setStyle("-fx-text-fill: #CCCCCC;");
        footer.getChildren().add(sep1);

        String categorie = getCategorieForType(notif);
        Label lblCategorie = new Label(categorie);
        lblCategorie.setFont(Font.font("Segoe UI", 11));
        lblCategorie.setStyle("-fx-text-fill: #666666 !important;");
        footer.getChildren().add(lblCategorie);

        Label sep2 = new Label("•");
        sep2.setStyle("-fx-text-fill: #CCCCCC;");
        footer.getChildren().add(sep2);

        Label lblDate = new Label(notif.getDateNotificationFormatee());
        lblDate.setFont(Font.font("Segoe UI", 11));
        lblDate.setStyle("-fx-text-fill: #888888 !important;");
        footer.getChildren().add(lblDate);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAction = new Button(notif.getActionLabel());
        btnAction.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4F6815;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent;" +
                        "-fx-underline: true;"
        );
        final String actionPage = notif.getActionPage();
        btnAction.setOnAction(e -> {
            if (actionPage != null && !actionPage.isEmpty()) {
                loadPage(actionPage, "Gestion des " + actionPage);
            }
        });
        footer.getChildren().add(btnAction);

        // Bouton "Marquer comme lu" (si non lu)
        if (!notif.isEstLue()) {
            final int notifId = notif.getIdNotification();
            Button btnMarquerLu = new Button("✓ Marquer comme lu");
            btnMarquerLu.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #666666;" +
                            "-fx-font-size: 11px;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: transparent;"
            );
            btnMarquerLu.setOnAction(e -> {
                serviceNotification.marquerCommeLue(notifId);
                panneauNotificationsStage.close();
                ouvrirPanneauNotifications();
            });
            footer.getChildren().add(btnMarquerLu);
        }

        card.getChildren().addAll(header, lblMessage, footer);

        return card;
    }

    /**
     * Retourne l'icône selon le type
     */
    private String getIconForType(Notification notif) {
        switch (notif.getType()) {
            case "FACTURE_ECHEANCE": return "📄";
            case "AVANCE_ATTENTE": return "📋";
            case "RETENUE_APPLIQUEE": return "🔒";
            case "TICKET_RESTAURANT": return "🍽️";
            default: return "🔔";
        }
    }

    /**
     * Retourne la catégorie selon le type
     */
    private String getCategorieForType(Notification notif) {
        switch (notif.getType()) {
            case "FACTURE_ECHEANCE": return "Factures";
            case "AVANCE_ATTENTE": return "RH / Paie";
            case "RETENUE_APPLIQUEE": return "RH / Paie";
            case "TICKET_RESTAURANT": return "RH / Paie";
            default: return "Général";
        }
    }

    // ── Méthodes publiques ──

    public static MainController getInstance() {
        return instance;
    }

    public void actualiserNotifications() {
        serviceNotification.actualiser();
    }

    // ── Navigation ──

    /**
     * Charge une page dans le conteneur principal
     */
    public void loadPage(String page, String title) {
        try {
            String fxmlPath = "/com/rh/views/" + page + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            if (headerTitle != null) {
                headerTitle.setText(title);
            }

            if ("paie".equals(page)) {
                paieController = loader.getController();
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
            }

            System.out.println("Page chargée : " + page);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page " + page + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void rafraichirPaie() {
        if (paieController != null) {
            paieController.actualiser();
        }
    }

    // ── Navigation ──

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

    // 🤖 Méthode pour le Chatbot
    @FXML
    private void onChatbot() {
        loadPage("chatbot", "🤖 Assistant RH");
    }

    @FXML
    private void onLogout() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
        System.out.println("Déconnexion");
    }

    public void cleanup() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
    }
}