package com.rh.contollers;

import com.rh.models.DemandeConge;
import com.rh.models.DemandeConge.Statut;
import com.rh.models.SoldeConge;
import com.rh.services.DemandeCongeService;
import com.rh.services.SoldeCongeService;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CongesController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private TableView<DemandeConge> tableConges;
    @FXML private TableColumn<DemandeConge, String> colNom;
    @FXML private TableColumn<DemandeConge, String> colPoste;
    @FXML private TableColumn<DemandeConge, String> colType;
    @FXML private TableColumn<DemandeConge, String> colDebut;
    @FXML private TableColumn<DemandeConge, String> colFin;
    @FXML private TableColumn<DemandeConge, String> colNbJours;
    @FXML private TableColumn<DemandeConge, String> colInitial;
    @FXML private TableColumn<DemandeConge, String> colConsomme;
    @FXML private TableColumn<DemandeConge, String> colRestant;
    @FXML private TableColumn<DemandeConge, Void>   colStatut;
    @FXML private TableColumn<DemandeConge, String> colRemplacant;
    @FXML private TableColumn<DemandeConge, String> colCommentaire;
    @FXML private TableColumn<DemandeConge, Void>   colActions;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilterType;
    @FXML private ComboBox<String> cbFilterStatut;
    @FXML private Label lblCount;
    @FXML private Label kpiAttente;
    @FXML private Label kpiApprouve;
    @FXML private Label kpiRefuse;
    @FXML private Label kpiTotal;

    // ── Services ──────────────────────────────────────────────────────────────
    private final DemandeCongeService service      = new DemandeCongeService();
    private final SoldeCongeService   soldeService = new SoldeCongeService();

    private ObservableList<DemandeConge> masterList = FXCollections.observableArrayList();
    private FilteredList<DemandeConge>   filteredList;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int SOLDE_INITIAL = 18;

    // ── Initialize ────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableStyle();
        setupColumns();
        setupFilters();
        loadData();
    }

    // ── Style tableau ─────────────────────────────────────────────────────────

    private void setupTableStyle() {
        tableConges.setStyle("-fx-background-color:transparent;");
        tableConges.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DemandeConge d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) { setStyle(""); return; }
                if (d.getStatut() == Statut.EN_ATTENTE) {
                    setStyle("-fx-background-color:#FDFAF7;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    // ── Colonnes ──────────────────────────────────────────────────────────────

    private void setupColumns() {

        // Nom et Prénom
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomEmploye()));
        colNom.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#222222;");
            }
        });

        // Poste
        colPoste.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPosteEmploye()));
        colPoste.setCellFactory(col -> textCell("#666666", false));

        // Type de congé
        colType.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTypeConge() != null ? d.getValue().getTypeConge().getLabel() : "—"));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null || "—".equals(v)) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(v);
                lbl.setStyle("-fx-background-color:" + typeBg(v) + ";-fx-text-fill:" + typeFg(v) + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:3 8 3 8;");
                setGraphic(lbl); setText(null);
            }
        });

        // Date début
        colDebut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDebut() != null ? d.getValue().getDateDebut().format(FMT) : "—"));
        colDebut.setCellFactory(col -> textCell("#555555", false));

        // Date fin
        colFin.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateFin() != null ? d.getValue().getDateFin().format(FMT) : "—"));
        colFin.setCellFactory(col -> textCell("#555555", false));

        // Nb jours
        colNbJours.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getNombreJours())));
        colNbJours.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v + "j");
                setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;" +
                        "-fx-text-fill:#75070C;-fx-alignment:CENTER;");
            }
        });

        // Solde initial — TOUJOURS 18
        colInitial.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(SOLDE_INITIAL)));
        colInitial.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;" +
                        "-fx-text-fill:#185FA5;-fx-alignment:CENTER;");
            }
        });

        // Solde consommé — depuis la BDD
        colConsomme.setCellValueFactory(d -> {
            DemandeConge dc = d.getValue();
            if (dc.getEmploye() == null) return new SimpleStringProperty("0");
            SoldeConge s = soldeService.getSolde(
                    dc.getEmploye().getId(),
                    dc.getTypeConge() != null ? dc.getTypeConge().name() : "ANNUEL",
                    LocalDate.now().getYear());
            double consomme = (s != null) ? s.getSoldeConsomme() : 0;
            return new SimpleStringProperty(String.valueOf((int) consomme));
        });
        colConsomme.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                int val = 0; try { val = Integer.parseInt(v); } catch (Exception ignored) {}
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;" +
                        "-fx-text-fill:" + (val > 0 ? "#854F0B" : "#AAAAAA") + ";-fx-alignment:CENTER;");
            }
        });

        // Solde restant — coloré selon niveau
        colRestant.setCellValueFactory(d -> {
            DemandeConge dc = d.getValue();
            if (dc.getEmploye() == null) return new SimpleStringProperty(String.valueOf(SOLDE_INITIAL));
            SoldeConge s = soldeService.getSolde(
                    dc.getEmploye().getId(),
                    dc.getTypeConge() != null ? dc.getTypeConge().name() : "ANNUEL",
                    LocalDate.now().getYear());
            double restant = (s != null) ? s.getSoldeRestant() : SOLDE_INITIAL;
            return new SimpleStringProperty(String.valueOf((int) restant));
        });
        colRestant.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                int val = SOLDE_INITIAL;
                try { val = Integer.parseInt(v); } catch (Exception ignored) {}
                String color = val >= 12 ? "#4F6815" : val >= 6 ? "#C8A000" : "#75070C";
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;" +
                        "-fx-text-fill:" + color + ";-fx-alignment:CENTER;");
            }
        });

        // Statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                DemandeConge d = getTableView().getItems().get(getIndex());
                if (d.getStatut() == null) { setGraphic(null); return; }
                String[] sc = statutColors(d.getStatut());
                Label lbl = new Label(d.getStatut().getLabel());
                lbl.setStyle("-fx-background-color:" + sc[1] + ";-fx-text-fill:" + sc[0] + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:4 10 4 10;");
                setGraphic(lbl); setText(null);
            }
        });

        // Remplaçant
        colRemplacant.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRemplacant() != null ? d.getValue().getRemplacant() : "—"));
        colRemplacant.setCellFactory(col -> textCell("#777777", true));

        // Commentaires
        colCommentaire.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCommentaire() != null ? d.getValue().getCommentaire() : ""));
        colCommentaire.setCellFactory(col -> textCell("#999999", true));

        // ── Actions ──────────────────────────────────────────────────────────
        colActions.setCellFactory(col -> new TableCell<>() {
            // 🔥 CORRECTION : Utiliser des caractères ASCII ou Unicode
            private final Button btnEdit = makeBtn("\u270F", "#FFF8F0", "#854F0B"); // ✏
            private final Button btnOk   = makeBtn("\u2714", "#F0F5E8", "#4F6815"); // ✔
            private final Button btnNo   = makeBtn("\u2716", "#FFF0F0", "#75070C"); // ✕
            private final Button btnDel  = makeBtn("\uD83D\uDDD1", "#FFF0F0", "#75070C"); // 🗑 ou utiliser "🗑" en texte

            {
                btnEdit.setOnAction(e -> openForm(getTableView().getItems().get(getIndex())));
                btnOk.setOnAction(e -> handleApprouver(getTableView().getItems().get(getIndex())));
                btnNo.setOnAction(e -> handleRefuser(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                // Ajouter des tooltips pour plus de clarté
                Tooltip.install(btnEdit, new Tooltip("Modifier"));
                Tooltip.install(btnOk, new Tooltip("Approuver"));
                Tooltip.install(btnNo, new Tooltip("Refuser"));
                Tooltip.install(btnDel, new Tooltip("Supprimer"));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                DemandeConge d = getTableView().getItems().get(getIndex());
                HBox box = new HBox(4);
                box.setAlignment(Pos.CENTER_LEFT);
                box.getChildren().add(btnEdit);
                if (d.getStatut() == Statut.EN_ATTENTE) {
                    box.getChildren().addAll(btnOk, btnNo);
                } else {
                    box.getChildren().add(btnDel);
                }
                setGraphic(box);
            }
        });
    }

    // ── Filtres ───────────────────────────────────────────────────────────────

    private void setupFilters() {
        cbFilterType.getItems().addAll("Tous les types", "Congé annuel", "Congé maladie",
                "Congé exceptionnel", "Sans solde", "Récupération");
        cbFilterType.setValue("Tous les types");

        cbFilterStatut.getItems().addAll("Tous les statuts", "En attente", "Approuvé", "Refusé", "Annulé");
        cbFilterStatut.setValue("Tous les statuts");

        tfSearch.textProperty().addListener((obs, o, n) -> applyFilter());
        cbFilterType.valueProperty().addListener((obs, o, n) -> applyFilter());
        cbFilterStatut.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    // ── Données ───────────────────────────────────────────────────────────────

    public void loadData() {
        masterList.setAll(service.getAll());
        filteredList = new FilteredList<>(masterList, p -> true);
        tableConges.setItems(filteredList);
        updateKpis();
        applyFilter();
    }

    private void updateKpis() {
        long attente  = masterList.stream().filter(d -> d.getStatut() == Statut.EN_ATTENTE).count();
        long approuve = masterList.stream().filter(d -> d.getStatut() == Statut.APPROUVE).count();
        long refuse   = masterList.stream().filter(d -> d.getStatut() == Statut.REFUSE).count();
        kpiAttente.setText(String.valueOf(attente));
        kpiApprouve.setText(String.valueOf(approuve));
        kpiRefuse.setText(String.valueOf(refuse));
        kpiTotal.setText(String.valueOf(masterList.size()));
    }

    private void applyFilter() {
        String search = tfSearch.getText() == null ? "" : tfSearch.getText().toLowerCase().trim();
        String type   = cbFilterType.getValue();
        String statut = cbFilterStatut.getValue();

        filteredList.setPredicate(d -> {
            boolean ms = search.isEmpty()
                    || d.getNomEmploye().toLowerCase().contains(search)
                    || d.getPosteEmploye().toLowerCase().contains(search);
            boolean mt = type == null || type.equals("Tous les types")
                    || (d.getTypeConge() != null && d.getTypeConge().getLabel().equals(type));
            boolean mst = statut == null || statut.equals("Tous les statuts")
                    || (d.getStatut() != null && d.getStatut().getLabel().equals(statut));
            return ms && mt && mst;
        });
        lblCount.setText(filteredList.size() + " demande(s)");
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() { openForm(null); }

    private void openForm(DemandeConge demande) {
        try {
            URL url = getClass().getResource("/com/rh/views/conge_form.fxml");
            if (url == null) { System.err.println("conge_form.fxml introuvable!"); return; }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            CongeFormController ctrl = loader.getController();
            ctrl.setParent(this);
            if (demande != null) ctrl.setDemande(demande);
            Stage stage = new Stage();
            stage.setTitle(demande == null ? "Nouvelle demande" : "Modifier la demande");
            stage.setScene(new Scene(root, 850, 720));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tableConges.getScene() != null)
                stage.initOwner(tableConges.getScene().getWindow());
            stage.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleApprouver(DemandeConge d) {
        SoldeConge s = soldeService.getSolde(
                d.getEmploye().getId(),
                d.getTypeConge() != null ? d.getTypeConge().name() : "ANNUEL",
                LocalDate.now().getYear());
        int restant = s != null ? (int) s.getSoldeRestant() : SOLDE_INITIAL;

        String msg = d.getNombreJours() + " jours seront déduits du solde.\n" +
                "Solde actuel : " + restant + " / " + SOLDE_INITIAL + " jours.\nConfirmer ?";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                msg, ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Approuver le congé");
        confirm.setHeaderText("Congé de " + d.getNomEmploye());
        confirm.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.YES) return;
            int result = service.approuver(d);
            if (result == 1) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Solde insuffisant");
                warn.setHeaderText("Solde de " + d.getNomEmploye() + " insuffisant");
                warn.setContentText("Solde restant : " + restant + " jours\n" +
                        "Jours demandés : " + d.getNombreJours() + " jours\n" +
                        "Impossible d'approuver cette demande.");
                warn.showAndWait();
            } else {
                loadData();
            }
        });
    }

    private void handleRefuser(DemandeConge d) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Refuser la demande");
        dlg.setHeaderText("Motif de refus — " + d.getNomEmploye());
        dlg.setContentText("Commentaire :");
        dlg.showAndWait().ifPresent(motif -> { service.refuser(d, motif); loadData(); });
    }

    private void handleDelete(DemandeConge d) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Action irréversible.", ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Supprimer");
        a.setHeaderText("Supprimer la demande de " + d.getNomEmploye() + " ?");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) { service.delete(d); loadData(); }
        });
    }

    public void refresh() { loadData(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Button makeBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size:12;-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                "-fx-background-radius:6;-fx-cursor:hand;-fx-border-color:transparent;" +
                "-fx-min-width:28;-fx-min-height:28;-fx-max-width:28;-fx-max-height:28;" +
                "-fx-padding:0;-fx-alignment:CENTER;");
        return b;
    }

    private TableCell<DemandeConge, String> textCell(String color, boolean italic) {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:" + color + ";" +
                        (italic ? "-fx-font-style:italic;" : ""));
            }
        };
    }

    private String[] statutColors(Statut s) {
        return switch (s) {
            case APPROUVE -> new String[]{"#4F6815", "#F0F5E8"};
            case REFUSE   -> new String[]{"#75070C", "#FFF0F0"};
            case ANNULE   -> new String[]{"#888888", "#F5F5F5"};
            default       -> new String[]{"#C8A000", "#FFFBEE"};
        };
    }

    private String typeBg(String type) {
        return switch (type) {
            case "Congé annuel"       -> "#E6F1FB";
            case "Congé maladie"      -> "#FFF0F0";
            case "Congé exceptionnel" -> "#F3E5F5";
            case "Sans solde"         -> "#FFF8F0";
            default                   -> "#F0F5E8";
        };
    }

    private String typeFg(String type) {
        return switch (type) {
            case "Congé annuel"       -> "#0C447C";
            case "Congé maladie"      -> "#75070C";
            case "Congé exceptionnel" -> "#6A1B9A";
            case "Sans solde"         -> "#854F0B";
            default                   -> "#4F6815";
        };
    }
}