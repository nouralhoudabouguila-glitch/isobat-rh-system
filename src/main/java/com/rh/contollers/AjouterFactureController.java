package com.rh.contollers;

import com.rh.models.Facture;
import com.rh.models.Facture.ModePaiement;
import com.rh.models.Facture.StatutFacture;
import com.rh.models.Fournisseur;
import com.rh.services.ServiceFacture;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class AjouterFactureController {

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
    private Button btnAjouter;
    @FXML
    private Button btnAnnuler;

    private ServiceFacture service;
    private Runnable onAjoutReussi;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        service = new ServiceFacture();

        // Configurer les ComboBox
        comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(
                "En attente", "Payée", "Annulée"
        ));
        comboStatut.setValue("En attente");

        comboModePaiement.setItems(javafx.collections.FXCollections.observableArrayList(
                "Espèces", "Chèque", "Virement", "Carte bancaire"
        ));
        comboModePaiement.setValue("Virement");

        // Définir la date par défaut
        datePickerFacture.setValue(LocalDate.now());
        datePickerEcheance.setValue(LocalDate.now().plusDays(30));

        // Calcul automatique du TTC
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
            // Ignorer les erreurs de parsing
        }
    }

    private double parseMontant(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            // Remplacer la virgule par un point pour le parsing
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

    @FXML
    private void onAjouter() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            Fournisseur fournisseur = comboFournisseur.getValue();

            Facture facture = new Facture();
            facture.setNumeroFacture(txtNumeroFacture.getText().trim());
            facture.setIdFournisseur(fournisseur.getIdFournisseur());
            facture.setMontantHt(parseMontant(txtMontantHt.getText()));
            facture.setMontantTva(parseMontant(txtMontantTva.getText()));
            facture.setMontantTtc(parseMontant(txtMontantTtc.getText()));
            facture.setDateFacture(datePickerFacture.getValue());
            facture.setDateEcheance(datePickerEcheance.getValue());
            facture.setStatut(comboStatut.getValue());
            facture.setModePaiement(comboModePaiement.getValue());
            facture.setCommentaire(txtCommentaire.getText().trim());

            service.add(facture);

            showAlert("Succès", "Facture ajoutée avec succès !", Alert.AlertType.INFORMATION);

            if (onAjoutReussi != null) {
                onAjoutReussi.run();
            }

            fermerFenetre();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
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

    public void setOnAjoutReussi(Runnable callback) {
        this.onAjoutReussi = callback;
    }
}