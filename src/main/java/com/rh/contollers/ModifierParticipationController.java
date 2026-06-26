package com.rh.contollers;

import com.rh.models.ParticipationFormation;
import com.rh.services.ParticipationFormationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModifierParticipationController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtPoste;
    @FXML
    private DatePicker datePickerFormation;
    @FXML
    private TextField txtFormation;
    @FXML
    private ComboBox<String> comboStatut;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ParticipationFormationService service;
    private ParticipationFormation participationActuelle;
    private Runnable onModificationReussie;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML
    public void initialize() {
        service = new ParticipationFormationService();

        // Configurer le ComboBox des statuts
        comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(
                "Présente",
                "Absente",
                "FPF"
        ));
    }

    // ── Méthode pour charger les données de la participation ──

    public void setParticipation(ParticipationFormation participation) {
        this.participationActuelle = participation;

        // Remplir les champs avec les données
        txtNom.setText(participation.getNom());
        txtPrenom.setText(participation.getPrenom());
        txtPoste.setText(participation.getPoste());

        if (participation.getDateFormation() != null) {
            datePickerFormation.setValue(participation.getDateFormation());
        }

        txtFormation.setText(participation.getFormation());
        comboStatut.setValue(participation.getStatut());
    }

    @FXML
    private void onModifier() {
        // Valider les champs
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Mettre à jour la participation
            participationActuelle.setDateFormation(datePickerFormation.getValue());
            participationActuelle.setFormation(txtFormation.getText().trim());
            participationActuelle.setStatut(comboStatut.getValue());

            // Mettre à jour dans la base de données
            service.update(participationActuelle);

            // Afficher un message de succès
            showAlert("Succès", "Participation modifiée avec succès !", Alert.AlertType.INFORMATION);

            // Fermer la fenêtre et notifier le contrôleur parent
            if (onModificationReussie != null) {
                onModificationReussie.run();
            }

            fermerFenetre();

        } catch (Exception e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (datePickerFormation.getValue() == null) {
            erreurs.append("• La date de formation est obligatoire\n");
            datePickerFormation.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            datePickerFormation.setStyle("");
        }

        if (txtFormation.getText() == null || txtFormation.getText().trim().isEmpty()) {
            erreurs.append("• La formation est obligatoire\n");
            txtFormation.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            txtFormation.setStyle("");
        }

        if (comboStatut.getValue() == null) {
            erreurs.append("• Le statut est obligatoire\n");
            comboStatut.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            comboStatut.setStyle("");
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

    public void setOnModificationReussie(Runnable callback) {
        this.onModificationReussie = callback;
    }
}