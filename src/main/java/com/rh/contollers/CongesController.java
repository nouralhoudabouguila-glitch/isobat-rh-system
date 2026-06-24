package com.rh.contollers;

import com.rh.models.DemandeConge;
import com.rh.models.DemandeConge.Statut;
import com.rh.services.DemandeCongeService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CongesController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private ComboBox<String> cbFilterStatut;
    @FXML private ComboBox<String> cbFilterType;
    @FXML private Label lblCount;
    @FXML private Label kpiAttente;
    @FXML private Label kpiApprouve;
    @FXML private Label kpiRefuse;
    @FXML private Label kpiTotal;

    private final DemandeCongeService service = new DemandeCongeService();
    private ObservableList<DemandeConge> masterList = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        loadData();
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private void setupFilters() {
        cbFilterStatut.getItems().addAll(
                "Tous les statuts", "En attente", "Approuvé", "Refusé", "Annulé"
        );
        cbFilterStatut.setValue("Tous les statuts");

        cbFilterType.getItems().addAll(
                "Tous les types", "Congé annuel", "Congé maladie",
                "Congé exceptionnel", "Sans solde", "Récupération"
        );
        cbFilterType.setValue("Tous les types");

        cbFilterStatut.valueProperty().addListener((obs, o, n) -> applyFilter());
        cbFilterType.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    // ── Données ──────────────────────────────────────────────────────────────

    public void loadData() {
        masterList.setAll(service.getAll());
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
        String statut = cbFilterStatut.getValue();
        String type   = cbFilterType.getValue();

        List<DemandeConge> filtered = masterList.stream().filter(d -> {
            boolean matchStatut = statut == null || statut.equals("Tous les statuts")
                    || d.getStatut().getLabel().equals(statut);
            boolean matchType = type == null || type.equals("Tous les types")
                    || (d.getTypeConge() != null && d.getTypeConge().getLabel().equals(type));
            return matchStatut && matchType;
        }).toList();

        renderCards(filtered);
        lblCount.setText(filtered.size() + " demande(s)");
    }

    // ── Rendu Cards ──────────────────────────────────────────────────────────

    private void renderCards(List<DemandeConge> list) {
        cardsContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Aucune demande trouvée");
            empty.setStyle("-fx-font-family:'Poppins';-fx-font-size:14;-fx-text-fill:#AAAAAA;-fx-padding:40;");
            cardsContainer.getChildren().add(empty);
            return;
        }
        for (DemandeConge d : list) cardsContainer.getChildren().add(buildCard(d));
    }

    private VBox buildCard(DemandeConge d) {
        // Couleurs selon statut
        String[] colors = statutColors(d.getStatut());
        String color = colors[0]; // couleur principale
        String light = colors[1]; // couleur claire

        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setStyle(
                "-fx-background-color:#FFFFFF;-fx-background-radius:14;" +
                        "-fx-border-color:#E0D0BE;-fx-border-radius:14;-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"
        );

        // Bande colorée en haut
        HBox band = new HBox();
        band.setPrefHeight(5);
        band.setStyle("-fx-background-color:" + color + ";-fx-background-radius:14 14 0 0;");
        card.getChildren().add(band);

        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));

        // Ligne 1 : Nom employé + badge statut
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);

        VBox empInfo = new VBox(2);
        empInfo.setMinWidth(0);
        HBox.setHgrow(empInfo, Priority.ALWAYS);
        Label nomLbl = new Label(d.getNomEmploye());
        nomLbl.setStyle("-fx-font-family:'Poppins';-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#222222;");
        Label posteLbl = new Label(d.getPosteEmploye());
        posteLbl.setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#888888;");
        empInfo.getChildren().addAll(nomLbl, posteLbl);

        Label statutBadge = new Label(d.getStatut().getLabel());
        statutBadge.setStyle(
                "-fx-background-color:" + light + ";-fx-text-fill:" + color + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:4 10 4 10;"
        );
        row1.getChildren().addAll(empInfo, statutBadge);

        // Séparateur
        Pane sep1 = new Pane(); sep1.setPrefHeight(1);
        sep1.setStyle("-fx-background-color:#F5F0EA;");

        // Ligne 2 : Type de congé + Nombre de jours
        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(d.getTypeConge() != null ? d.getTypeConge().getLabel() : "—");
        typeBadge.setStyle(
                "-fx-background-color:#E6F1FB;-fx-text-fill:#0C447C;" +
                        "-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:4 10 4 10;"
        );
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label joursLbl = new Label(d.getNombreJours() + " jour" + (d.getNombreJours() > 1 ? "s" : ""));
        joursLbl.setStyle(
                "-fx-background-color:" + light + ";-fx-text-fill:" + color + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;" +
                        "-fx-background-radius:8;-fx-padding:4 12 4 12;"
        );
        row2.getChildren().addAll(typeBadge, sp, joursLbl);

        // Ligne 3 : Dates
        HBox row3 = new HBox(6);
        row3.setAlignment(Pos.CENTER_LEFT);
        Label dateLbl = new Label("📅 " +
                (d.getDateDebut() != null ? d.getDateDebut().format(FMT) : "—") +
                "  →  " +
                (d.getDateFin() != null ? d.getDateFin().format(FMT) : "—")
        );
        dateLbl.setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#555555;");
        row3.getChildren().add(dateLbl);

        // Ligne 4 : Remplaçant (si renseigné)
        if (d.getRemplacant() != null && !d.getRemplacant().isEmpty()) {
            Label remLbl = new Label("👤 Remplaçant : " + d.getRemplacant());
            remLbl.setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#888888;");
            body.getChildren().add(remLbl);
        }

        // Séparateur 2
        Pane sep2 = new Pane(); sep2.setPrefHeight(1);
        sep2.setStyle("-fx-background-color:#F5F0EA;");

        // Ligne 5 : Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Bouton Modifier
        Button btnEdit = new Button("✏  Modifier");
        btnEdit.setStyle(
                "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                        "-fx-background-color:#FFF8F0;-fx-text-fill:#854F0B;" +
                        "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:6 12 6 12;"
        );
        btnEdit.setOnAction(e -> openForm(d));

        // Bouton Approuver (seulement si en attente)
        if (d.getStatut() == Statut.EN_ATTENTE) {
            Button btnOk = new Button("✔");
            btnOk.setStyle(
                    "-fx-font-family:'Poppins';-fx-font-size:13;" +
                            "-fx-background-color:#F0F5E8;-fx-text-fill:#4F6815;" +
                            "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:6 10 6 10;"
            );
            btnOk.setOnAction(e -> handleApprouver(d));

            Button btnNo = new Button("✕");
            btnNo.setStyle(
                    "-fx-font-family:'Poppins';-fx-font-size:13;" +
                            "-fx-background-color:#FFF0F0;-fx-text-fill:#75070C;" +
                            "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:6 10 6 10;"
            );
            btnNo.setOnAction(e -> handleRefuser(d));
            actions.getChildren().addAll(btnEdit, btnOk, btnNo);
        } else {
            Button btnDel = new Button("🗑");
            btnDel.setStyle(
                    "-fx-font-family:'Poppins';-fx-font-size:13;" +
                            "-fx-background-color:#FFF0F0;-fx-text-fill:#75070C;" +
                            "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:6 10 6 10;"
            );
            btnDel.setOnAction(e -> handleDelete(d));
            actions.getChildren().addAll(btnEdit, btnDel);
        }

        body.getChildren().addAll(row1, sep1, row2, row3, sep2, actions);
        card.getChildren().add(body);
        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() { openForm(null); }

    private void openForm(DemandeConge demande) {
        try {
            // ← ADAPTE CE CHEMIN selon ton projet
            URL url = getClass().getResource("/com/rh/views/conge_form.fxml");
            if (url == null) { System.err.println("conge_form.fxml introuvable !"); return; }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            CongeFormController ctrl = loader.getController();
            ctrl.setParent(this);
            if (demande != null) ctrl.setDemande(demande);

            Stage stage = new Stage();
            stage.setTitle(demande == null ? "Nouvelle demande de congé" : "Modifier la demande");
            stage.setScene(new Scene(root, 820, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (cardsContainer.getScene() != null)
                stage.initOwner(cardsContainer.getScene().getWindow());
            stage.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleApprouver(DemandeConge d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'approbation");
        confirm.setHeaderText("Approuver le congé de " + d.getNomEmploye() + " ?");
        confirm.setContentText(d.getNombreJours() + " jours seront déduits du solde.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = service.approuver(d);
            if (!ok) {
                Alert err = new Alert(Alert.AlertType.WARNING);
                err.setTitle("Solde insuffisant");
                err.setHeaderText("Impossible d'approuver");
                err.setContentText("Le solde restant de " + d.getNomEmploye() +
                        " est insuffisant pour ce congé.");
                err.showAndWait();
            }
            loadData();
        }
    }

    private void handleRefuser(DemandeConge d) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Refuser la demande");
        dialog.setHeaderText("Motif de refus pour " + d.getNomEmploye());
        dialog.setContentText("Commentaire :");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motif -> {
            service.refuser(d, motif);
            loadData();
        });
    }

    private void handleDelete(DemandeConge d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer la demande");
        alert.setHeaderText("Supprimer la demande de " + d.getNomEmploye() + " ?");
        alert.setContentText("Cette action est irréversible.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(d);
            loadData();
        }
    }

    public void refresh() { loadData(); }

    // ── Helper couleurs ───────────────────────────────────────────────────────

    private String[] statutColors(Statut statut) {
        return switch (statut) {
            case APPROUVE    -> new String[]{"#4F6815", "#F0F5E8"};
            case REFUSE      -> new String[]{"#75070C", "#FFF0F0"};
            case ANNULE      -> new String[]{"#888888", "#F5F5F5"};
            default          -> new String[]{"#C8A000", "#FFFBEE"}; // EN_ATTENTE
        };
    }
}
