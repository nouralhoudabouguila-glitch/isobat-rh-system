package com.rh.contollers;

import com.rh.models.TriTelephonique;
import com.rh.services.ServiceTriTelephonique;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AjouterCandidatController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtExperience;
    @FXML
    private TextField txtPoste;
    @FXML
    private TextArea txtCommentaire;
    @FXML
    private ComboBox<String> comboStatut;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnAnnuler;

    private ServiceTriTelephonique service;
    private Runnable onAjoutReussi;

    @FXML
    public void initialize() {
        service = new ServiceTriTelephonique();

        // Configurer le ComboBox des statuts
        comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(
                "En attente",
                "Accepté",
                "Pas accepté"
        ));
        comboStatut.setValue("En attente");

        // Ajouter des écouteurs pour la validation
        ajouterValidateurs();
    }

    private void ajouterValidateurs() {
        // Empêcher les espaces au début
        txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.startsWith(" ")) {
                txtNom.setText(newVal.trim());
            }
        });

        txtPrenom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.startsWith(" ")) {
                txtPrenom.setText(newVal.trim());
            }
        });

        // Formatage du téléphone
        txtTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                txtTelephone.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void onAjouter() {
        // Valider les champs
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Créer le candidat
            TriTelephonique candidat = new TriTelephonique();
            candidat.setNom(txtNom.getText().trim());
            candidat.setPrenom(txtPrenom.getText().trim());
            candidat.setTelephone(txtTelephone.getText().trim());
            candidat.setExperience(txtExperience.getText().trim());
            candidat.setPoste(txtPoste.getText().trim());
            candidat.setCommentaire(txtCommentaire.getText().trim());
            candidat.setStatut(comboStatut.getValue());

            // Ajouter à la base de données
            service.add(candidat);

            // Afficher un message de succès
            showAlert("Succès", "Candidat ajouté avec succès !", Alert.AlertType.INFORMATION);

            // Fermer la fenêtre et notifier le contrôleur parent
            if (onAjoutReussi != null) {
                onAjoutReussi.run();
            }

            fermerFenetre();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de l'ajout du candidat : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        // Réinitialiser les styles
        txtNom.setStyle("");
        txtPrenom.setStyle("");
        txtTelephone.setStyle("");
        txtPoste.setStyle("");

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            erreurs.append("• Le nom est obligatoire\n");
            txtNom.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        }

        if (txtPrenom.getText() == null || txtPrenom.getText().trim().isEmpty()) {
            erreurs.append("• Le prénom est obligatoire\n");
            txtPrenom.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        }

        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            erreurs.append("• Le téléphone est obligatoire\n");
            txtTelephone.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        } else if (txtTelephone.getText().trim().length() < 8) {
            erreurs.append("• Le téléphone doit contenir au moins 8 chiffres\n");
            txtTelephone.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        }

        if (txtPoste.getText() == null || txtPoste.getText().trim().isEmpty()) {
            erreurs.append("• Le poste est obligatoire\n");
            txtPoste.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        }

        if (erreurs.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes :\n\n" + erreurs.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void onAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Setter pour le callback ──

    public void setOnAjoutReussi(Runnable callback) {
        this.onAjoutReussi = callback;
    }
}