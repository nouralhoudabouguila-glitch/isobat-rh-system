package com.rh.contollers;

import com.rh.models.Facture;
import com.rh.models.Fournisseur;
import com.rh.services.ServiceFacture;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ModifierFactureController {

    @FXML
    private TextField txtNumeroFacture;
    @FXML
    private ComboBox<Fournisseur> comboFournisseur;
    @FXML
    private TextField txtMontantHt;
    @FXML
    private TextField txtMontantTva;
    @FXML
    private TextField txtMontantTtc;
    @FXML
    private DatePicker datePickerFacture;
    @FXML
    private DatePicker datePickerEcheance;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private ComboBox<String> comboModePaiement;
    @FXML
    private TextArea txtCommentaire;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ServiceFacture service;
    private Facture factureActuelle;
    private Runnable onModificationReussie;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        service = new ServiceFacture();

        comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(
                "En attente", "Payée", "Annulée"
        ));

        comboModePaiement.setItems(javafx.collections.FXCollections.observableArrayList(
                "Espèces", "Chèque", "Virement", "Carte bancaire"
        ));

        txtMontantHt.textProperty().addListener((obs, oldVal, newVal) -> {
            calculerTtc();
        });

        txtMontantTva.textProperty().addListener((obs, oldVal, newVal) -> {
            calculerTtc();
        });
    }

    private void calculerTtc() {
        try {
            double ht = parseMontant(txtMontantHt.getText());
            double tva = parseMontant(txtMontantTva.getText());
            double ttc = ht + tva;
            txtMontantTtc.setText(format.format(ttc));
        } catch (Exception e) {
            // Ignorer
        }
    }

    private double parseMontant(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            String cleanText = text.trim().replace(",", ".");
            return Double.parseDouble(cleanText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setFournisseurs(List<Fournisseur> fournisseurs) {
        comboFournisseur.setItems(javafx.collections.FXCollections.observableArrayList(fournisseurs));
        comboFournisseur.setCellFactory(param -> new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });
        comboFournisseur.setButtonCell(new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });
    }

    public void setFacture(Facture facture) {
        this.factureActuelle = facture;

        txtNumeroFacture.setText(facture.getNumeroFacture());

        if (facture.getFournisseur() != null) {
            comboFournisseur.setValue(facture.getFournisseur());
        }

        txtMontantHt.setText(format.format(facture.getMontantHt()));
        txtMontantTva.setText(format.format(facture.getMontantTva()));
        txtMontantTtc.setText(format.format(facture.getMontantTtc()));
        datePickerFacture.setValue(facture.getDateFacture());
        datePickerEcheance.setValue(facture.getDateEcheance());
        comboStatut.setValue(facture.getStatutLabel());
        comboModePaiement.setValue(facture.getModePaiementLabel());
        txtCommentaire.setText(facture.getCommentaire());
    }

    @FXML
    private void onModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            Fournisseur fournisseur = comboFournisseur.getValue();

            factureActuelle.setNumeroFacture(txtNumeroFacture.getText().trim());
            factureActuelle.setIdFournisseur(fournisseur.getIdFournisseur());
            factureActuelle.setMontantHt(parseMontant(txtMontantHt.getText()));
            factureActuelle.setMontantTva(parseMontant(txtMontantTva.getText()));
            factureActuelle.setMontantTtc(parseMontant(txtMontantTtc.getText()));
            factureActuelle.setDateFacture(datePickerFacture.getValue());
            factureActuelle.setDateEcheance(datePickerEcheance.getValue());
            factureActuelle.setStatut(comboStatut.getValue());
            factureActuelle.setModePaiement(comboModePaiement.getValue());
            factureActuelle.setCommentaire(txtCommentaire.getText().trim());

            service.update(factureActuelle);

            showAlert("Succès", "Facture modifiée avec succès !", Alert.AlertType.INFORMATION);

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

        if (txtNumeroFacture.getText() == null || txtNumeroFacture.getText().trim().isEmpty()) {
            erreurs.append("• Le numéro de facture est obligatoire\n");
        }

        if (comboFournisseur.getValue() == null) {
            erreurs.append("• Le fournisseur est obligatoire\n");
        }

        if (txtMontantHt.getText() == null || txtMontantHt.getText().trim().isEmpty()) {
            erreurs.append("• Le montant HT est obligatoire\n");
        } else {
            try {
                double ht = parseMontant(txtMontantHt.getText());
                if (ht <= 0) {
                    erreurs.append("• Le montant HT doit être supérieur à 0\n");
                }
            } catch (Exception e) {
                erreurs.append("• Le montant HT n'est pas valide\n");
            }
        }

        if (datePickerFacture.getValue() == null) {
            erreurs.append("• La date de facture est obligatoire\n");
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