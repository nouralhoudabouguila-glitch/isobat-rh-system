package com.rh.contollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label     headerTitle;
    @FXML private ImageView logoImg;

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

    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnEmploye, btnPresence, btnConges,
                btnPlanning, btnFournisseur, btnFacture, btnDepense, btnParametres);
        loadLogo();
        loadView("dashboard", "Tableau de bord", btnDashboard);
    }

    // ── Chargement du logo ───────────────────────────────────────────────────

    private void loadLogo() {
        InputStream is = getClass().getResourceAsStream("/com/rh/images/logo-Isobat.png");
        if (is != null) {
            logoImg.setImage(new Image(is));
        }
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
