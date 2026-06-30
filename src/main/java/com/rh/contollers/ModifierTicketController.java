package com.rh.contollers;

import com.rh.models.TicketEmploye;
import com.rh.services.ServiceTicket;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class ModifierTicketController {

    @FXML
    private TextField txtEmploye;
    @FXML
    private TextField txtDepartement;
    @FXML
    private TextField txtMois;
    @FXML
    private TextField txtAnnee;
    @FXML
    private TextField txtMontantParTicket;
    @FXML
    private TextField txtNbJours;
    @FXML
    private TextField txtTotal;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ServiceTicket service;
    private TicketEmploye ticketActuel;
    private Runnable onModificationReussie;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    @FXML
    public void initialize() {
        service = new ServiceTicket();

        txtMontantParTicket.textProperty().addListener((obs, oldVal, newVal) -> calculerTotal());
        txtNbJours.textProperty().addListener((obs, oldVal, newVal) -> calculerTotal());
    }

    private void calculerTotal() {
        try {
            double montant = Double.parseDouble(txtMontantParTicket.getText().replace(",", "."));
            int nbJours = Integer.parseInt(txtNbJours.getText());
            double total = montant * nbJours;
            txtTotal.setText(format.format(total));
        } catch (NumberFormatException e) {
            txtTotal.setText("");
        }
    }

    public void setTicket(TicketEmploye ticket) {
        this.ticketActuel = ticket;

        if (ticket.getEmploye() != null) {
            txtEmploye.setText(ticket.getEmploye().getNom() + " " + ticket.getEmploye().getPrenom());
            if (ticket.getEmploye().getDepartement() != null) {
                txtDepartement.setText(ticket.getEmploye().getDepartement().toString());
            }
        }

        txtMois.setText(ticket.getMois());
        txtAnnee.setText(String.valueOf(ticket.getAnnee()));
        txtMontantParTicket.setText(format.format(ticket.getMontantParTicket()));
        txtNbJours.setText(String.valueOf(ticket.getNbJours()));
        txtTotal.setText(format.format(ticket.getTotal()));
    }

    @FXML
    private void onModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            double montant = Double.parseDouble(txtMontantParTicket.getText().replace(",", "."));
            int nbJours = Integer.parseInt(txtNbJours.getText());

            ticketActuel.setMontantParTicket(montant);
            ticketActuel.setNbJours(nbJours);

            service.update(ticketActuel);

            showAlert("Succès", "Ticket modifié avec succès !", Alert.AlertType.INFORMATION);

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

        try {
            double montant = Double.parseDouble(txtMontantParTicket.getText().replace(",", "."));
            if (montant <= 0) {
                erreurs.append("• Le montant doit être supérieur à 0\n");
            }
        } catch (NumberFormatException e) {
            erreurs.append("• Le montant n'est pas valide\n");
        }

        try {
            int nbJours = Integer.parseInt(txtNbJours.getText());
            if (nbJours <= 0) {
                erreurs.append("• Le nombre de jours doit être supérieur à 0\n");
            }
            if (nbJours > 31) {
                erreurs.append("• Le nombre de jours ne peut pas dépasser 31\n");
            }
        } catch (NumberFormatException e) {
            erreurs.append("• Le nombre de jours n'est pas valide\n");
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