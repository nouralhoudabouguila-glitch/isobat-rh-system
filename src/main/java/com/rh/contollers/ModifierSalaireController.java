package com.rh.contollers;

import com.rh.models.Employe;
import com.rh.services.ServiceAvance;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class ModifierSalaireController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtRib;
    @FXML
    private TextField txtMontant;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private DetailSalaireController.EmployeSalaire employeActuel;
    private Runnable onModificationReussie;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        // Formatage du montant
        txtMontant.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*[.,]?\\d*")) {
                txtMontant.setText(oldVal);
            }
        });
    }

    public void setEmploye(DetailSalaireController.EmployeSalaire employe) {
        this.employeActuel = employe;

        // Remplir les champs
        txtNom.setText(employe.getNom());
        txtRib.setText(employe.getRib());
        txtMontant.setText(format.format(employe.getMontant()).replace(" ", ""));
    }

    @FXML
    private void onModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Récupérer le nouveau montant
            String montantStr = txtMontant.getText().trim().replace(",", ".");
            double nouveauMontant = Double.parseDouble(montantStr);

            // Mettre à jour le montant
            employeActuel.setMontant(nouveauMontant);

            // Afficher un message de succès
            showAlert("Succès", "Salaire modifié avec succès !", Alert.AlertType.INFORMATION);

            // Fermer la fenêtre et notifier le contrôleur parent
            if (onModificationReussie != null) {
                onModificationReussie.run();
            }

            fermerFenetre();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant n'est pas valide.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (txtMontant.getText() == null || txtMontant.getText().trim().isEmpty()) {
            erreurs.append("• Le montant est obligatoire\n");
            txtMontant.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            try {
                double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
                if (montant < 0) {
                    erreurs.append("• Le montant doit être supérieur ou égal à 0\n");
                    txtMontant.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
                } else {
                    txtMontant.setStyle("");
                }
            } catch (NumberFormatException e) {
                erreurs.append("• Le montant n'est pas valide\n");
                txtMontant.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
            }
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