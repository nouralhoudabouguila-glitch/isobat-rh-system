package com.rh.contollers;

import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.models.Retenue;
import com.rh.services.ServiceRetenue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AjouterRetenueController {

    @FXML
    private ComboBox<String> comboDepartement;
    @FXML
    private ComboBox<Employe> comboEmploye;
    @FXML
    private TextField txtMontant;
    @FXML
    private TextField txtMotif;
    @FXML
    private DatePicker datePickerRetenue;
    @FXML
    private TextArea txtCommentaire;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnAnnuler;

    private ServiceRetenue service;
    private Runnable onAjoutReussi;

    @FXML
    public void initialize() {
        service = new ServiceRetenue();

        // Date par défaut
        datePickerRetenue.setValue(LocalDate.now());

        // Charger les départements
        chargerDepartements();

        // Écouteur pour le département
        comboDepartement.setOnAction(e -> {
            String departement = comboDepartement.getValue();
            if (departement != null && !departement.isEmpty()) {
                chargerEmployesParDepartement(departement);
            } else {
                comboEmploye.getItems().clear();
            }
        });

        // Configurer l'affichage des employés
        configurerComboEmploye();
    }

    private void chargerDepartements() {
        try {
            List<String> departements = service.getDepartements();
            comboDepartement.getItems().clear();
            comboDepartement.getItems().add("");

            // Convertir les noms d'enum en labels lisibles
            for (String dept : departements) {
                for (Departement d : Departement.values()) {
                    if (d.name().equals(dept)) {
                        comboDepartement.getItems().add(d.toString());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des départements : " + e.getMessage());
        }
    }

    private void chargerEmployesParDepartement(String departement) {
        try {
            List<Employe> liste = service.getEmployesByDepartement(departement);
            comboEmploye.getItems().clear();
            comboEmploye.getItems().addAll(liste);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des employés : " + e.getMessage());
        }
    }

    private void configurerComboEmploye() {
        comboEmploye.setCellFactory(param -> new ListCell<Employe>() {
            @Override
            protected void updateItem(Employe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom() + " (" + item.getProfil() + ")");
                }
            }
        });

        comboEmploye.setButtonCell(new ListCell<Employe>() {
            @Override
            protected void updateItem(Employe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
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
            Employe employe = comboEmploye.getValue();

            Retenue retenue = new Retenue();
            retenue.setIdEmploye(employe.getId());
            retenue.setMontant(Double.parseDouble(txtMontant.getText().replace(",", ".")));
            retenue.setMotif(txtMotif.getText().trim());
            retenue.setDateRetenue(datePickerRetenue.getValue());
            retenue.setCommentaire(txtCommentaire.getText().trim());
            // Initialiser la date de création
            retenue.setDateCreation(LocalDateTime.now());

            service.add(retenue);

            showAlert("Succès", "Retenue ajoutée avec succès !", Alert.AlertType.INFORMATION);

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

        if (comboDepartement.getValue() == null || comboDepartement.getValue().isEmpty()) {
            erreurs.append("• Veuillez sélectionner un département\n");
        }

        if (comboEmploye.getValue() == null) {
            erreurs.append("• Veuillez sélectionner un employé\n");
        }

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

        if (datePickerRetenue.getValue() == null) {
            erreurs.append("• La date est obligatoire\n");
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