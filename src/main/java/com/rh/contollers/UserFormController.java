package com.rh.contollers;

import com.rh.models.User;
import com.rh.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private PasswordField pfPassword;
    @FXML private ComboBox<User.Role> cbRole;
    @FXML private ToggleButton tbActif;
    @FXML private ToggleButton tbInactif;
    @FXML private Label lblTitle;
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;
    @FXML private Label errRole;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private final UserService service = new UserService();
    private User editing = null;
    private UsersController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Rôles
        cbRole.getItems().setAll(User.Role.values());

        // Groupe pour le statut
        ToggleGroup statusGroup = new ToggleGroup();
        tbActif.setToggleGroup(statusGroup);
        tbInactif.setToggleGroup(statusGroup);
        tbActif.setSelected(true); // Par défaut actif

        // Styles des toggles
        tbActif.selectedProperty().addListener((o, was, is) ->
                tbActif.setStyle(is ?
                        "-fx-background-color:#4F6815;-fx-text-fill:#FFFFFF;-fx-border-color:#4F6815;" :
                        "-fx-background-color:#FFFFFF;-fx-text-fill:#555555;-fx-border-color:#E0D0BE;"));
        tbInactif.selectedProperty().addListener((o, was, is) ->
                tbInactif.setStyle(is ?
                        "-fx-background-color:#75070C;-fx-text-fill:#FFFFFF;-fx-border-color:#75070C;" :
                        "-fx-background-color:#FFFFFF;-fx-text-fill:#555555;-fx-border-color:#E0D0BE;"));
    }

    public void setParent(UsersController parent) {
        this.parent = parent;
    }

    public void setUser(User user) {
        this.editing = user;
        if (user == null) return;

        lblTitle.setText("Modifier l'utilisateur");
        tfNom.setText(user.getNom());
        tfPrenom.setText(user.getPrenom());
        tfEmail.setText(user.getEmail());
        tfTelephone.setText(user.getTelephone());
        cbRole.setValue(user.getRole());

        if (user.isActif()) {
            tbActif.setSelected(true);
        } else {
            tbInactif.setSelected(true);
        }

        // Cacher le champ mot de passe en modification
        pfPassword.setVisible(false);
        pfPassword.setManaged(false);
    }

    @FXML
    private void handleSave() {
        clearErrors();

        boolean ok = true;

        if (tfNom.getText().trim().isEmpty()) {
            errNom.setText("Nom obligatoire");
            errNom.setManaged(true);
            ok = false;
        }

        if (tfPrenom.getText().trim().isEmpty()) {
            errPrenom.setText("Prénom obligatoire");
            errPrenom.setManaged(true);
            ok = false;
        }

        String email = tfEmail.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errEmail.setText("Email invalide");
            errEmail.setManaged(true);
            ok = false;
        }

        if (cbRole.getValue() == null) {
            errRole.setText("Rôle obligatoire");
            errRole.setManaged(true);
            ok = false;
        }

        if (!ok) return;

        User user = editing != null ? editing : new User();
        user.setNom(tfNom.getText().trim());
        user.setPrenom(tfPrenom.getText().trim());
        user.setEmail(email);
        user.setTelephone(tfTelephone.getText().trim());
        user.setRole(cbRole.getValue());
        user.setActif(tbActif.isSelected());

        if (editing == null) {
            // Nouvel utilisateur
            String mdp = pfPassword.getText();
            if (mdp.length() < 6) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Erreur");
                warn.setHeaderText("Mot de passe trop court");
                warn.setContentText("Le mot de passe doit contenir au moins 6 caractères.");
                warn.showAndWait();
                return;
            }
            user.setMotDePasse(mdp);

            if (service.add(user)) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Succès");
                info.setHeaderText("Utilisateur créé");
                info.setContentText("L'utilisateur " + user.getNomComplet() + " a été créé avec succès.");
                info.showAndWait();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erreur");
                err.setHeaderText("Email déjà utilisé");
                err.setContentText("Cet email est déjà associé à un compte.");
                err.showAndWait();
                return;
            }
        } else {
            // Modification
            service.update(user);
        }

        if (parent != null) parent.refresh();
        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void clearErrors() {
        errNom.setText(""); errNom.setManaged(false);
        errPrenom.setText(""); errPrenom.setManaged(false);
        errEmail.setText(""); errEmail.setManaged(false);
        errRole.setText(""); errRole.setManaged(false);

        String base = "-fx-font-family:'Poppins';-fx-font-size:13;-fx-padding:10 12 10 12;" +
                "-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;";
        tfNom.setStyle(base);
        tfPrenom.setStyle(base);
        tfEmail.setStyle(base);
        tfTelephone.setStyle(base);
        pfPassword.setStyle(base);
    }

    private void close() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}