package com.rh.contollers;

import com.rh.models.Presence;
import com.rh.models.Presence.Statut;
import com.rh.services.PresenceService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class PresenceFormController implements Initializable {

    @FXML private Label lblEmploye;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfArrivee;
    @FXML private TextField tfDebutPause;
    @FXML private TextField tfFinPause;
    @FXML private TextField tfDepart;
    @FXML private ComboBox<Statut> cbStatut;
    @FXML private TextArea taCommentaire;

    private final PresenceService service = new PresenceService();
    private Presence presence;
    private PresenceController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbStatut.getItems().setAll(Statut.values());
    }

    public void setPresence(Presence p) {
        this.presence = p;
        lblEmploye.setText(p.getNomEmploye());
        dpDate.setValue(p.getDatePresence());
        tfArrivee.setText(p.getHeureArriveeStr());
        tfDebutPause.setText(p.getDebutPauseStr());
        tfFinPause.setText(p.getFinPauseStr());
        tfDepart.setText(p.getHeureDepartStr());
        cbStatut.setValue(p.getStatut());
        taCommentaire.setText(p.getCommentaire());
    }

    public void setParent(PresenceController parent) {
        this.parent = parent;
    }

    @FXML
    private void handleSave() {
        try {
            presence.setDatePresence(dpDate.getValue());
            presence.setHeureArrivee(parseTime(tfArrivee.getText()));
            presence.setDebutPause(parseTime(tfDebutPause.getText()));
            presence.setFinPause(parseTime(tfFinPause.getText()));
            presence.setHeureDepart(parseTime(tfDepart.getText()));
            presence.setStatut(cbStatut.getValue());
            presence.setCommentaire(taCommentaire.getText());
            presence.calculerHeuresTravaillees();

            service.update(presence);
            if (parent != null) parent.refresh();
            close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Format d'heure invalide");
            alert.setContentText("Utilisez le format HH:MM (ex: 08:30)");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || "—".equals(timeStr.trim())) {
            return null;
        }
        try {
            String[] parts = timeStr.trim().split(":");
            return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Format d'heure invalide");
        }
    }

    private void close() {
        Stage stage = (Stage) dpDate.getScene().getWindow();
        stage.close();
    }
}