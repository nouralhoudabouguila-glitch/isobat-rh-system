package com.rh.contollers;

import com.rh.models.Departement;
import com.rh.models.Presence;
import com.rh.models.Presence.Statut;
import com.rh.services.PresenceService;
import com.rh.services.PresenceService.ImportResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PresenceController implements Initializable {

    @FXML private TableView<Presence> tablePresence;
    @FXML private TableColumn<Presence, String> colEmploye;
    @FXML private TableColumn<Presence, String> colDepartement;
    @FXML private TableColumn<Presence, String> colDate;
    @FXML private TableColumn<Presence, String> colArrivee;
    @FXML private TableColumn<Presence, String> colDebutPause;
    @FXML private TableColumn<Presence, String> colFinPause;
    @FXML private TableColumn<Presence, String> colDepart;
    @FXML private TableColumn<Presence, String> colHeures;
    @FXML private TableColumn<Presence, String> colStatut;
    @FXML private TableColumn<Presence, String> colSource;
    @FXML private TableColumn<Presence, Void> colActions;

    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbDepartement;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblCount;
    @FXML private Label kpiPresent;
    @FXML private Label kpiRetard;
    @FXML private Label kpiAbsent;
    @FXML private Label kpiTotal;

    private final PresenceService service = new PresenceService();
    private ObservableList<Presence> masterList = FXCollections.observableArrayList();
    private FilteredList<Presence> filteredList;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupFilters();
        setupDatePicker();
        loadData();
    }

    private void setupDatePicker() {
        dpDate.setValue(LocalDate.now());
        dpDate.valueProperty().addListener((obs, old, n) -> loadData());
    }

    private void setupColumns() {
        // Employé
        colEmploye.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomEmploye()));
        colEmploye.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Manrope';-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#222222;");
            }
        });

        // Département
        colDepartement.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartementLabel()));

        // Date
        colDate.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDatePresence();
            return new SimpleStringProperty(date != null ? date.format(FMT) : "—");
        });

        // Arrivée
        colArrivee.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHeureArriveeStr()));

        // Début Pause
        colDebutPause.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDebutPauseStr()));

        // Fin Pause
        colFinPause.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFinPauseStr()));

        // Départ
        colDepart.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHeureDepartStr()));

        // Heures travaillées
        colHeures.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHeuresTravailleesStr()));
        colHeures.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Manrope';-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#4F6815;-fx-alignment:CENTER;");
                setAlignment(Pos.CENTER);
            }
        });

        // Statut
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatut() != null ? d.getValue().getStatut().getLabel() : "—"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(v);
                String[] colors = getStatutColors(v);
                lbl.setStyle("-fx-background-color:" + colors[1] + ";-fx-text-fill:" + colors[0] + ";" +
                        "-fx-font-family:'Manrope';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:3 10 3 10;");
                setGraphic(lbl); setText(null);
            }
        });

        // Source
        colSource.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSource() != null ? d.getValue().getSource().getLabel() : "—"));
        colSource.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                String color = "Import Excel".equals(v) ? "#4F6815" : "#75070C";
                setStyle("-fx-font-family:'Manrope';-fx-font-size:11;-fx-text-fill:" + color + ";-fx-font-weight:bold;");
            }
        });

        // Actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = makeBtn("✏️", "#FFF8F0", "#854F0B");
            private final Button btnDel = makeBtn("🗑️", "#FFF0F0", "#75070C");

            {
                btnEdit.setOnAction(e -> openEditForm(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> deletePresence(getTableView().getItems().get(getIndex())));
                Tooltip.install(btnEdit, new Tooltip("Modifier"));
                Tooltip.install(btnDel, new Tooltip("Supprimer"));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnEdit, btnDel);
                setGraphic(box);
            }
        });
    }

    private void setupFilters() {
        // Départements
        cbDepartement.getItems().addAll("Tous les départements");
        for (Departement d : Departement.values()) {
            cbDepartement.getItems().add(d.toString());
        }
        cbDepartement.setValue("Tous les départements");

        // Statuts
        cbStatut.getItems().addAll("Tous les statuts");
        for (Statut s : Statut.values()) {
            cbStatut.getItems().add(s.getLabel());
        }
        cbStatut.setValue("Tous les statuts");

        cbDepartement.valueProperty().addListener((obs, o, n) -> applyFilter());
        cbStatut.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    public void loadData() {
        LocalDate date = dpDate.getValue();
        if (date == null) date = LocalDate.now();

        List<Presence> list = service.getByDate(date);
        masterList.setAll(list);
        filteredList = new FilteredList<>(masterList, p -> true);
        tablePresence.setItems(filteredList);
        updateKpis(date);
        applyFilter();
    }

    private void updateKpis(LocalDate date) {
        Map<Statut, Long> stats = service.getStatsJour(date);
        long present = stats.getOrDefault(Statut.PRESENT, 0L) +
                stats.getOrDefault(Statut.TELETRAVAIL, 0L) +
                stats.getOrDefault(Statut.MISSION, 0L);
        long retard = stats.getOrDefault(Statut.RETARD, 0L);
        long absent = stats.getOrDefault(Statut.ABSENT_JUSTIFIE, 0L) +
                stats.getOrDefault(Statut.ABSENT_NON_JUSTIFIE, 0L);
        long total = masterList.size();

        kpiPresent.setText(String.valueOf(present));
        kpiRetard.setText(String.valueOf(retard));
        kpiAbsent.setText(String.valueOf(absent));
        kpiTotal.setText(String.valueOf(total));
    }

    private void applyFilter() {
        String dept = cbDepartement.getValue();
        String statut = cbStatut.getValue();

        filteredList.setPredicate(p -> {
            boolean md = dept == null || dept.equals("Tous les départements")
                    || p.getDepartementLabel().equals(dept);
            boolean ms = statut == null || statut.equals("Tous les statuts")
                    || (p.getStatut() != null && p.getStatut().getLabel().equals(statut));
            return md && ms;
        });
        lblCount.setText(filteredList.size() + " présence(s)");
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    @FXML
    private void handleImporter() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un fichier Excel de badgeuse");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx", "*.xls")
        );

        Stage stage = (Stage) tablePresence.getScene().getWindow();
        File fichier = fileChooser.showOpenDialog(stage);

        if (fichier != null) {
            // Choix du département
            ChoiceDialog<String> deptDialog = new ChoiceDialog<>("TOUS", "TOUS");
            for (Departement d : Departement.values()) {
                deptDialog.getItems().add(d.toString());
            }
            deptDialog.setTitle("Import Excel");
            deptDialog.setHeaderText("Sélectionner le département");
            deptDialog.setContentText("Département :");

            deptDialog.showAndWait().ifPresent(deptStr -> {
                Departement dept = null;
                if (!"TOUS".equals(deptStr)) {
                    for (Departement d : Departement.values()) {
                        if (d.toString().equals(deptStr) || d.name().equals(deptStr)) {
                            dept = d;
                            break;
                        }
                    }
                }
                importerFichier(fichier, dept);
            });
        }
    }

    private void importerFichier(File fichier, Departement departement) {
        ImportResult result = service.importerExcel(fichier, departement);

        // Afficher le bilan
        StringBuilder msg = new StringBuilder();
        msg.append("📊 Bilan de l'import :\n\n");
        msg.append("✅ ").append(result.importees).append(" présences importées\n");
        msg.append("👥 ").append(result.employes).append(" employés concernés\n");
        msg.append("⚠️ ").append(result.anomalies.size()).append(" anomalies détectées\n");

        if (!result.anomalies.isEmpty()) {
            msg.append("\n📋 Anomalies :\n");
            for (String a : result.anomalies) {
                msg.append("  • ").append(a).append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Import terminé");
        alert.setHeaderText(null);
        alert.setContentText(msg.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();

        loadData();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void openEditForm(Presence p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rh/views/presence_form.fxml"));
            Parent root = loader.load();
            PresenceFormController ctrl = loader.getController();
            ctrl.setPresence(p);
            ctrl.setParent(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier la présence");
            stage.setScene(new Scene(root, 500, 550));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tablePresence.getScene() != null)
                stage.initOwner(tablePresence.getScene().getWindow());
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePresence(Presence p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette présence ?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la présence de " + p.getNomEmploye() + " du " + p.getDatePresence().format(FMT));
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                service.delete(p.getId());
                loadData();
            }
        });
    }

    public void refresh() {
        loadData();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Button makeBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size:14;-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                "-fx-background-radius:6;-fx-cursor:hand;-fx-border-color:transparent;" +
                "-fx-min-width:30;-fx-min-height:30;-fx-max-width:30;-fx-max-height:30;" +
                "-fx-padding:0;-fx-alignment:CENTER;");
        return b;
    }

    private String[] getStatutColors(String statut) {
        return switch (statut) {
            case "Présent" -> new String[]{"#4F6815", "#F0F5E8"};
            case "Retard" -> new String[]{"#C8A000", "#FFFBEE"};
            case "Absent justifié", "Mission" -> new String[]{"#854F0B", "#FFF8F0"};
            case "Incomplet" -> new String[]{"#888888", "#F5F5F5"};
            default -> new String[]{"#75070C", "#FFF0F0"};
        };
    }
}