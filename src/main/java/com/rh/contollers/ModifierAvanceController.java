package com.rh.contollers;

import com.rh.models.Avance;
import com.rh.models.Employe;
import com.rh.services.ServiceAvance;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class ModifierAvanceController {

    @FXML
    private TextField txtEmploye;
    @FXML
    private TextField txtMontant;
    @FXML
    private TextField txtMotif;
    @FXML
    private DatePicker datePickerDemande;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private TextArea txtCommentaire;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ServiceAvance service;
    private Avance avanceActuelle;
    private Runnable onModificationReussie;

    @FXML
    public void initialize() {
        service = new ServiceAvance();

        // Configurer le ComboBox des statuts
        comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(
                "En attente",
                "Validée",
                "Refusée"
        ));
    }

    public void setAvance(Avance avance) {
        this.avanceActuelle = avance;

        // Remplir les champs avec les données
        if (avance.getEmploye() != null) {
            txtEmploye.setText(avance.getEmploye().getNom() + " " + avance.getEmploye().getPrenom());
        }

        txtMontant.setText(String.valueOf(avance.getMontant()));
        txtMotif.setText(avance.getMotif());
        datePickerDemande.setValue(avance.getDateDemande());
        comboStatut.setValue(avance.getStatutLabel());
        txtCommentaire.setText(avance.getCommentaire());
    }

    @FXML
    private void onModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Mettre à jour l'avance
            avanceActuelle.setMontant(Double.parseDouble(txtMontant.getText().replace(",", ".")));
            avanceActuelle.setMotif(txtMotif.getText().trim());
            avanceActuelle.setDateDemande(datePickerDemande.getValue());
            avanceActuelle.setStatut(comboStatut.getValue());
            avanceActuelle.setCommentaire(txtCommentaire.getText().trim());

            // Si le statut change, mettre à jour la date de validation
            if ("Validée".equals(comboStatut.getValue()) && avanceActuelle.getDateValidation() == null) {
                avanceActuelle.setDateValidation(LocalDate.now());
            } else if ("Refusée".equals(comboStatut.getValue()) && avanceActuelle.getDateValidation() == null) {
                avanceActuelle.setDateValidation(LocalDate.now());
            }

            service.update(avanceActuelle);

            showAlert("Succès", "Avance modifiée avec succès !", Alert.AlertType.INFORMATION);

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

        if (txtMontant.getText() == null || txtMontant.getText().trim().isEmpty()) {
            erreurs.append("• Le montant est obligatoire\n");
        } else {
            try {
                double montant = Double.parseDouble(txtMontant.getText().replace(",", "."));
                if (montant <= 0) {
                    erreurs.append("• Le montant doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("• Le montant n'est pas valide\n");
            }
        }

        if (txtMotif.getText() == null || txtMotif.getText().trim().isEmpty()) {
            erreurs.append("• Le motif est obligatoire\n");
        }

        if (datePickerDemande.getValue() == null) {
            erreurs.append("• La date de demande est obligatoire\n");
        }

        if (comboStatut.getValue() == null) {
            erreurs.append("• Le statut est obligatoire\n");
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

    public void setOnModificationReussie(Runnable callback) {
        this.onModificationReussie = callback;
    }
}