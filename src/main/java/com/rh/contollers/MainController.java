package com.rh.contollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainController {

    private static MainController instance;

    @FXML private StackPane contentArea;
    @FXML private Label     headerTitle;
    @FXML private ImageView logoImg;
    @FXML private VBox      subMenuRecrutement;

    @FXML private Button btnDashboard;
    @FXML private Button btnEmploye;
    @FXML private Button btnPresence;
    @FXML private Button btnConges;
    @FXML private Button btnPlanning;
    @FXML private Button btnRecrutement;
    @FXML private Button btnPaie;
    @FXML private Button btnFournisseur;
    @FXML private Button btnFacture;
    @FXML private Button btnDepense;
    @FXML private Button btnParametres;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        instance = this;
        navButtons = List.of(btnDashboard, btnEmploye, btnPresence, btnConges,
                btnPlanning, btnRecrutement, btnPaie, btnFournisseur, btnFacture, btnDepense, btnParametres);
        loadLogo();
        loadView("dashboard", "Tableau de bord", btnDashboard);
    }

    // ── Navigation statique (depuis sous-pages) ──────────────────────────────

    public static void loadPage(String fxml, String title) {
        if (instance == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("/com/rh/views/" + fxml + ".fxml"));
            Node view = loader.load();
            instance.contentArea.getChildren().setAll(view);
            instance.headerTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Chargement du logo ───────────────────────────────────────────────────

    private void loadLogo() {
        InputStream is = getClass().getResourceAsStream("/com/rh/images/logo-Isobat.png");
        if (is != null) {
            logoImg.setImage(new Image(is));
        }
    }

    // ── Navigation handlers ──────────────────────────────────────────────────

    @FXML private void onDashboard()   { closeSubMenus(); loadView("dashboard",    "Tableau de bord",      btnDashboard);   }
    @FXML private void onEmploye()     { closeSubMenus(); loadView("employe",      "Employés",              btnEmploye);     }
    @FXML private void onPresence()    { closeSubMenus(); loadView("presence",     "Présence",              btnPresence);    }
    @FXML private void onConges()      { closeSubMenus(); loadView("conges",       "Congés",                btnConges);      }
    @FXML private void onPlanning()    { closeSubMenus(); loadView("planning",           "Planning",              btnPlanning);       }
    @FXML private void onRecrutement() {
        // Basculer le sous-menu
        boolean open = !subMenuRecrutement.isVisible();
        subMenuRecrutement.setVisible(open);
        subMenuRecrutement.setManaged(open);
        btnRecrutement.setText(open ? "🧑‍💼   Recrutement  ▼" : "🧑‍💼   Recrutement  ▶");
        if (open) setActiveButton(btnRecrutement);
    }
    @FXML private void onTriTelephonique()   { loadView("tri_telephonique",   "Tri Téléphonique",     btnRecrutement); }
    @FXML private void onEntretienPhysique() { loadView("entretien_physique", "Entretien Physique",   btnRecrutement); }
    @FXML private void onSessionFormation()  { loadView("session_formation",  "Session de Formation", btnRecrutement); }
    @FXML private void onPaie()        { closeSubMenus(); loadView("paie",             "Gestion de la Paie",   btnPaie);           }
    @FXML private void onFournisseur() { closeSubMenus(); loadView("fournisseur",  "Fournisseurs",          btnFournisseur); }
    @FXML private void onFacture()     { closeSubMenus(); loadView("facture",      "Factures",              btnFacture);     }
    @FXML private void onDepense()     { closeSubMenus(); loadView("depense",      "Dépenses",              btnDepense);     }
    @FXML private void onParametres()  { closeSubMenus(); loadView("parametres",   "Paramètres",            btnParametres);  }

    // ── Fermer tous les sous-menus ────────────────────────────────────────────

    private void closeSubMenus() {
        subMenuRecrutement.setVisible(false);
        subMenuRecrutement.setManaged(false);
        btnRecrutement.setText("🧑‍💼   Recrutement  ▶");
    }

    // ── Chargement d'une vue ─────────────────────────────────────────────────

    private void loadView(String viewName, String title, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rh/views/" + viewName + ".fxml"));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            headerTitle.setText(title);
            setActiveButton(activeBtn);
        } catch (IOException e) {
            e.printStackTrace();
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
