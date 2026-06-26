package com.rh.contollers;

import com.rh.models.EntretienPhysique;
import com.rh.services.ServiceEntretienPhysique;
import com.rh.services.ServiceTriTelephonique;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ModifierEntretienController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtPoste;
    @FXML
    private TextField txtDateRdv;
    @FXML
    private ComboBox<String> comboStatut;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ServiceEntretienPhysique service;
    private ServiceTriTelephonique serviceTriTelephonique;
    private EntretienPhysique entretienActuel;
    private Runnable onModificationReussie;

    @FXML
    public void initialize() {
        service = new ServiceEntretienPhysique();
        serviceTriTelephonique = new ServiceTriTelephonique();

        // Configurer le ComboBox des statuts (sans "Retenue - Refusé")
        comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(
                "En attente",
                "Retenue",
                "Absente",
                "Reporté"
        ));
    }

    // ── Méthode pour charger les données de l'entretien ──

    public void setEntretien(EntretienPhysique entretien) {
        this.entretienActuel = entretien;

        // Remplir les champs avec les données
        txtNom.setText(entretien.getNom());
        txtPrenom.setText(entretien.getPrenom());
        txtPoste.setText(entretien.getPoste());
        txtDateRdv.setText(entretien.getDateRdv());
        comboStatut.setValue(entretien.getStatut());
    }

    @FXML
    private void onModifier() {
        // Valider les champs
        if (!validerFormulaire()) {
            return;
        }

        try {
            String ancienStatut = entretienActuel.getStatut();
            String nouveauStatut = comboStatut.getValue();

            System.out.println("=== Modification de l'entretien ===");
            System.out.println("Ancien statut : " + ancienStatut);
            System.out.println("Nouveau statut : " + nouveauStatut);
            System.out.println("ID Entretien : " + entretienActuel.getIdEntretien());
            System.out.println("ID Candidat : " + entretienActuel.getIdCandidat());

            // Mettre à jour l'entretien
            entretienActuel.setDateRdv(txtDateRdv.getText().trim());
            entretienActuel.setStatut(nouveauStatut);

            // Mettre à jour dans la base de données
            service.update(entretienActuel);
            System.out.println("Entretien mis à jour dans la BD");

            // Si le statut est "Retenue" et qu'il a changé, ajouter à la formation
            if ("Retenue".equals(nouveauStatut) && !"Retenue".equals(ancienStatut)) {
                System.out.println("Le statut est passé à Retenue - Ajout à la formation...");
                boolean success = serviceTriTelephonique.retenirCandidat(entretienActuel.getIdCandidat());
                if (success) {
                    System.out.println("Candidat ajouté à la formation avec succès !");
                    showAlert("Succès", "Candidat retenu et ajouté à la formation !", Alert.AlertType.INFORMATION);
                } else {
                    System.err.println("Erreur lors de l'ajout à la formation");
                    showAlert("Erreur", "Erreur lors de l'ajout à la formation.", Alert.AlertType.ERROR);
                }
            } else {
                System.out.println("Aucun changement de statut vers Retenue");
            }

            // Afficher un message de succès
            showAlert("Succès", "Entretien modifié avec succès !", Alert.AlertType.INFORMATION);

            // Fermer la fenêtre et notifier le contrôleur parent
            if (onModificationReussie != null) {
                onModificationReussie.run();
            }

            fermerFenetre();

        } catch (Exception e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (txtDateRdv.getText() == null || txtDateRdv.getText().trim().isEmpty()) {
            erreurs.append("• La date du rendez-vous est obligatoire\n");
            txtDateRdv.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            txtDateRdv.setStyle("");
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