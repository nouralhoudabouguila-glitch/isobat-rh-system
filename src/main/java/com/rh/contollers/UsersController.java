package com.rh.contollers;

import com.rh.models.User;
import com.rh.services.UserService;
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
import java.util.ResourceBundle;

public class UsersController implements Initializable {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colTelephone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, Void> colActions;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilterRole;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private Label lblCount;

    private final UserService service = new UserService();
    private ObservableList<User> masterList = FXCollections.observableArrayList();
    private FilteredList<User> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupFilters();
        loadData();
    }

    private void setupColumns() {
        // Nom complet
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomComplet()));
        colNom.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#222222;");
            }
        });

        // Email
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#555555;");
            }
        });

        // Téléphone
        colTelephone.setCellValueFactory(d -> {
            String tel = d.getValue().getTelephone();
            return new SimpleStringProperty(tel != null && !tel.isEmpty() ? tel : "—");
        });

        // Rôle
        colRole.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRole() != null ? d.getValue().getRole().getLabel() : "—"));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(v);
                String bg = v.equals("Administrateur") ? "#FFF0F0" : "#F0F5E8";
                String fg = v.equals("Administrateur") ? "#75070C" : "#4F6815";
                lbl.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                        "-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;" +
                        "-fx-background-radius:10;-fx-padding:3 10 3 10;");
                setGraphic(lbl); setText(null);
            }
        });

        // Statut (Actif/Inactif)
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isActif() ? "🟢 Actif" : "🔴 Inactif"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                String color = v.contains("Actif") ? "#4F6815" : "#75070C";
                setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                        "-fx-text-fill:" + color + ";-fx-alignment:CENTER;");
            }
        });

        // Actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = makeBtn("✏", "#FFF8F0", "#854F0B");
            private final Button btnToggle = makeBtn(
                    null, "#F0F5E8", "#4F6815");
            private final Button btnReset = makeBtn("🔑", "#FFFBEE", "#C8A000");
            private final Button btnDel = makeBtn("🗑", "#FFF0F0", "#75070C");

            {
                btnEdit.setOnAction(e -> openForm(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> toggleStatut(getTableView().getItems().get(getIndex())));
                btnReset.setOnAction(e -> resetPassword(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));

                Tooltip.install(btnEdit, new Tooltip("Modifier"));
                Tooltip.install(btnToggle, new Tooltip("Activer/Désactiver"));
                Tooltip.install(btnReset, new Tooltip("Réinitialiser le mot de passe"));
                Tooltip.install(btnDel, new Tooltip("Supprimer"));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());

                // Mettre à jour le texte du bouton toggle selon le statut
                btnToggle.setText(u.isActif() ? "🔴" : "🟢");
                btnToggle.setStyle("-fx-font-size:16;-fx-background-color:" +
                        (u.isActif() ? "#FFF0F0" : "#F0F5E8") +
                        ";-fx-text-fill:" + (u.isActif() ? "#75070C" : "#4F6815") +
                        ";-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;" +
                        "-fx-min-width:32;-fx-min-height:32;-fx-max-width:32;-fx-max-height:32;" +
                        "-fx-padding:0;-fx-alignment:CENTER;");

                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnEdit, btnToggle, btnReset, btnDel);
                setGraphic(box);
            }
        });
    }

    private void setupFilters() {
        cbFilterRole.getItems().addAll("Tous les rôles", "Administrateur", "Responsable RH");
        cbFilterRole.setValue("Tous les rôles");

        cbFilterStatus.getItems().addAll("Tous les statuts", "Actif", "Inactif");
        cbFilterStatus.setValue("Tous les statuts");

        tfSearch.textProperty().addListener((obs, o, n) -> applyFilter());
        cbFilterRole.valueProperty().addListener((obs, o, n) -> applyFilter());
        cbFilterStatus.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    public void loadData() {
        masterList.setAll(service.getAll());
        filteredList = new FilteredList<>(masterList, p -> true);
        tableUsers.setItems(filteredList);
        applyFilter();
    }

    private void applyFilter() {
        String search = tfSearch.getText() == null ? "" : tfSearch.getText().toLowerCase().trim();
        String role = cbFilterRole.getValue();
        String status = cbFilterStatus.getValue();

        filteredList.setPredicate(u -> {
            boolean ms = search.isEmpty()
                    || u.getNomComplet().toLowerCase().contains(search)
                    || u.getEmail().toLowerCase().contains(search);
            boolean mr = role == null || role.equals("Tous les rôles")
                    || (u.getRole() != null && u.getRole().getLabel().equals(role));
            boolean msr = status == null || status.equals("Tous les statuts")
                    || (status.equals("Actif") && u.isActif())
                    || (status.equals("Inactif") && !u.isActif());
            return ms && mr && msr;
        });
        lblCount.setText(filteredList.size() + " utilisateur(s)");
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    private void openForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rh/views/user_form.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            ctrl.setParent(this);
            if (user != null) ctrl.setUser(user);

            Stage stage = new Stage();
            stage.setTitle(user == null ? "Nouvel utilisateur" : "Modifier l'utilisateur");
            stage.setScene(new Scene(root, 500, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tableUsers.getScene() != null)
                stage.initOwner(tableUsers.getScene().getWindow());
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleStatut(User user) {
        boolean newActif = !user.isActif();
        service.toggleActif(user.getId(), newActif);
        user.setActif(newActif);
        loadData();
    }

    private void resetPassword(User user) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Réinitialiser le mot de passe");
        dlg.setHeaderText("Nouveau mot de passe pour " + user.getNomComplet());
        dlg.setContentText("Mot de passe :");
        dlg.showAndWait().ifPresent(mdp -> {
            if (mdp.length() < 6) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Erreur");
                warn.setHeaderText("Mot de passe trop court");
                warn.setContentText("Le mot de passe doit contenir au moins 6 caractères.");
                warn.showAndWait();
                return;
            }
            if (service.resetMotDePasse(user.getId(), mdp)) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Succès");
                info.setHeaderText("Mot de passe réinitialisé");
                info.setContentText("Le mot de passe de " + user.getNomComplet() + " a été réinitialisé.");
                info.showAndWait();
            }
        });
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer définitivement " + user.getNomComplet() + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Action irréversible");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                service.delete(user.getId());
                loadData();
            }
        });
    }

    private Button makeBtn(String text, String bg, String fg) {
        Button b = new Button(text != null ? text : "");
        b.setStyle("-fx-font-size:16;-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                "-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;" +
                "-fx-min-width:32;-fx-min-height:32;-fx-max-width:32;-fx-max-height:32;" +
                "-fx-padding:0;-fx-alignment:CENTER;");
        return b;
    }

    public void refresh() {
        loadData();
    }
}