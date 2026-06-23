package com.rh.contollers;
import javafx.scene.layout.FlowPane;
import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.services.EmployeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmployeController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilterDept;
    @FXML private Label lblCount;

    private final EmployeService service = new EmployeService();
    private ObservableList<Employe> masterList = FXCollections.observableArrayList();

    // Couleurs par département
    private static final String[] DEPT_COLORS  = {"#75070C", "#4F6815", "#854F0B"};
    private static final String[] DEPT_LIGHTS  = {"#FFF0F0", "#F0F5E8", "#FAEEDA"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbFilterDept.getItems().addAll(
                "Tous les départements",
                "Centre d'appel B2B",
                "Centre d'appel B2C",
                "Bureau d'étude"
        );
        cbFilterDept.setValue("Tous les départements");
        tfSearch.textProperty().addListener((obs, o, n) -> applyFilter());
        cbFilterDept.valueProperty().addListener((obs, o, n) -> applyFilter());
        loadData();
    }

    // ── Chargement ──────────────────────────────────────────────────────────

    public void loadData() {
        masterList.setAll(service.getAll());
        applyFilter();
    }

    private void applyFilter() {
        String search = tfSearch.getText() == null ? "" : tfSearch.getText().toLowerCase().trim();
        String dept   = cbFilterDept.getValue();

        List<Employe> filtered = masterList.stream().filter(emp -> {
            boolean matchSearch = search.isEmpty()
                    || (emp.getNom()    != null && emp.getNom().toLowerCase().contains(search))
                    || (emp.getPrenom() != null && emp.getPrenom().toLowerCase().contains(search))
                    || (emp.getProfil() != null && emp.getProfil().toLowerCase().contains(search));
            boolean matchDept = dept == null || dept.equals("Tous les départements")
                    || (emp.getDepartement() != null && deptLabel(emp.getDepartement()).equals(dept));
            return matchSearch && matchDept;
        }).toList();

        renderCards(filtered);
        lblCount.setText(filtered.size() + " employé(s)");
    }

    // ── Rendu des cards ──────────────────────────────────────────────────────

    private void renderCards(List<Employe> list) {
        cardsContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("Aucun employé trouvé");
            empty.setStyle("-fx-font-family:'Poppins';-fx-font-size:14;-fx-text-fill:#AAAAAA;-fx-padding:40;");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (Employe emp : list) {
            cardsContainer.getChildren().add(buildCard(emp));
        }
    }

    private VBox buildCard(Employe emp) {
        int deptIdx = emp.getDepartement() == null ? 0 :
                emp.getDepartement() == Departement.CENTRE_APPEL_B2B ? 0 :
                        emp.getDepartement() == Departement.CENTRE_APPEL_B2C ? 1 : 2;

        String color = DEPT_COLORS[deptIdx];
        String light = DEPT_LIGHTS[deptIdx];

        // ── Card container ──
        VBox card = new VBox(0);
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.setStyle(
                "-fx-background-color:#FFFFFF;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:#E0D0BE;" +
                        "-fx-border-radius:14;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"
        );

        // ── Bande colorée en haut ──
        HBox topBand = new HBox();
        topBand.setPrefHeight(6);
        topBand.setStyle("-fx-background-color:" + color + ";-fx-background-radius:14 14 0 0;");
        card.getChildren().add(topBand);

        // ── Body ──
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 16, 14, 16));

        // Avatar + Nom
        HBox nameRow = new HBox(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar cercle avec initiales
        String initials = "";
        if (emp.getNom()    != null && !emp.getNom().isEmpty())    initials += emp.getNom().charAt(0);
        if (emp.getPrenom() != null && !emp.getPrenom().isEmpty()) initials += emp.getPrenom().charAt(0);
        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color:" + light + ";" +
                        "-fx-background-radius:50%;" +
                        "-fx-border-color:" + color + ";" +
                        "-fx-border-radius:50%;" +
                        "-fx-border-width:2;" +
                        "-fx-min-width:46;-fx-min-height:46;-fx-max-width:46;-fx-max-height:46;"
        );
        Label avLbl = new Label(initials.toUpperCase());
        avLbl.setStyle("-fx-font-family:'Poppins';-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        avatar.getChildren().add(avLbl);

        VBox nameBox = new VBox(2);
        Label fullName = new Label(emp.getNom() + " " + emp.getPrenom());
        fullName.setStyle("-fx-font-family:'Poppins';-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#222222;");
        Label profil = new Label(emp.getProfil() != null ? emp.getProfil() : "—");
        profil.setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#888888;");
        nameBox.getChildren().addAll(fullName, profil);
        nameRow.getChildren().addAll(avatar, nameBox);

        // Badges : département + contrat + statut
        // APRÈS
        FlowPane badges = new FlowPane(6, 4);
        badges.setAlignment(Pos.CENTER_LEFT);

        // Badge département
        Label deptBadge = new Label(emp.getDepartement() != null ? deptLabel(emp.getDepartement()) : "—");
        deptBadge.setStyle(
                "-fx-background-color:" + light + ";-fx-text-fill:" + color + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:9;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:3 8 3 8;"
        );

        // Badge contrat
        String ct = emp.getTypeContrat() != null ? emp.getTypeContrat() : "—";
        String ctBg = ct.equals("CDI") ? "#E8F5E9" : ct.equals("CDD") ? "#FFF3E0" : "#F3E5F5";
        String ctFg = ct.equals("CDI") ? "#2E7D32" : ct.equals("CDD") ? "#E65100" : "#6A1B9A";
        Label contratBadge = new Label(ct);
        contratBadge.setStyle(
                "-fx-background-color:" + ctBg + ";-fx-text-fill:" + ctFg + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:9;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:3 8 3 8;"
        );

        // Badge statut
        boolean actif = "Actif".equalsIgnoreCase(emp.getStatut());
        Label statutBadge = new Label((actif ? "● " : "● ") + (emp.getStatut() != null ? emp.getStatut() : "—"));
        statutBadge.setStyle(
                "-fx-background-color:" + (actif ? "#E8F5E9" : "#FFF0F0") + ";" +
                        "-fx-text-fill:" + (actif ? "#2E7D32" : "#75070C") + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:9;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:3 8 3 8;"
        );
        badges.getChildren().addAll(deptBadge, contratBadge, statutBadge);

        // Séparateur
        Pane sep = new Pane();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:#F0E6DA;");

        // Infos : téléphone + matricule
        HBox infoRow = new HBox(10);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        Label telLbl = new Label("📞 " + (emp.getTelephone() != null ? emp.getTelephone() : "—"));
        telLbl.setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#555555;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label matLbl = new Label("EMP-0" + emp.getId());
        matLbl.setStyle(
                "-fx-background-color:#FFF0F0;-fx-text-fill:#75070C;" +
                        "-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:4;-fx-padding:2 7 2 7;"
        );
        infoRow.getChildren().addAll(telLbl, spacer, matLbl);

        // Boutons Modifier / Supprimer
        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏  Modifier");
        btnEdit.setStyle(
                "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                        "-fx-background-color:" + light + ";-fx-text-fill:" + color + ";" +
                        "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;" +
                        "-fx-padding:6 14 6 14;"
        );
        btnEdit.setOnAction(e -> openForm(emp));

        Button btnDel = new Button("🗑");
        btnDel.setStyle(
                "-fx-font-family:'Poppins';-fx-font-size:13;" +
                        "-fx-background-color:#FFF0F0;-fx-text-fill:#75070C;" +
                        "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;" +
                        "-fx-padding:6 10 6 10;"
        );
        btnDel.setOnAction(e -> handleDelete(emp));

        actionsRow.getChildren().addAll(btnEdit, btnDel);

        body.getChildren().addAll(nameRow, badges, sep, infoRow, actionsRow);
        card.getChildren().add(body);
        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() { openForm(null); }

    private void openForm(Employe employe) {
        try {
            // ← CHANGE CE CHEMIN selon où est ton fichier !
            URL fxmlUrl = getClass().getResource("/com/rh/views/employe_form.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML introuvable !");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            EmployesController ctrl = loader.getController();
            ctrl.setParent(this);
            if (employe != null) ctrl.setEmploye(employe);

            Stage stage = new Stage();
            stage.setTitle(employe == null ? "Ajouter un employé" : "Modifier — " + employe.getNom());
            stage.setScene(new Scene(root, 880, 740));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (cardsContainer.getScene() != null)
                stage.initOwner(cardsContainer.getScene().getWindow());
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleDelete(Employe emp) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer " + emp.getNom() + " " + emp.getPrenom() + " ?");
        alert.setContentText("Cette action est irréversible.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(emp);
            loadData();
        }
    }

    public void refreshCards() { loadData(); }

    private String deptLabel(Departement d) {
        return switch (d) {
            case CENTRE_APPEL_B2B -> "Centre d'appel B2B";
            case CENTRE_APPEL_B2C -> "Centre d'appel B2C";
            case BUREAU_ETUDE     -> "Bureau d'étude";
        };
    }
}
