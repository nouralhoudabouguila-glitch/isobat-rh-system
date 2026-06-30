package com.rh.contollers;

import com.rh.models.Planning;
import com.rh.models.Planning.PrioritePlanning;
import com.rh.models.Planning.TypePlanning;
import com.rh.services.PlanningService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class PlanningFormController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeureDebut;
    @FXML private TextField tfHeureFin;
    @FXML private TextField tfLieu;
    @FXML private ComboBox<TypePlanning> cbType;
    @FXML private ComboBox<PrioritePlanning> cbPriorite;
    @FXML private Label errTitre;
    @FXML private Label errDate;

    private final PlanningService service = new PlanningService();
    private Planning planning;
    private PlanningController parent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurer les ComboBox
        cbType.getItems().setAll(TypePlanning.values());
        cbPriorite.getItems().setAll(PrioritePlanning.values());

        // Valeurs par défaut
        cbType.setValue(TypePlanning.AUTRE);
        cbPriorite.setValue(PrioritePlanning.MOYENNE);
        dpDate.setValue(LocalDate.now());
    }

    public void setParent(PlanningController parent) {
        this.parent = parent;
    }

    public void setPlanning(Planning p) {
        if (p == null) {
            // Nouvel événement - date par défaut déjà définie
            return;
        }
        this.planning = p;
        lblTitle.setText("✏️ Modifier l'événement");

        tfTitre.setText(p.getTitre());
        taDescription.setText(p.getDescription());
        dpDate.setValue(p.getDateEvenement());
        tfHeureDebut.setText(p.getHeureDebutStr());
        tfHeureFin.setText(p.getHeureFinStr());
        tfLieu.setText(p.getLieu());
        cbType.setValue(p.getType());
        cbPriorite.setValue(p.getPriorite());
    }

    @FXML
    private void handleSave() {
        clearErrors();

        boolean ok = true;

        // Validation du titre
        if (tfTitre.getText().trim().isEmpty()) {
            errTitre.setText("Le titre est obligatoire");
            errTitre.setManaged(true);
            tfTitre.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        // Validation de la date
        if (dpDate.getValue() == null) {
            errDate.setText("La date est obligatoire");
            errDate.setManaged(true);
            dpDate.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        if (!ok) return;

        // Créer ou mettre à jour
        Planning p = planning != null ? planning : new Planning();
        p.setTitre(tfTitre.getText().trim());
        p.setDescription(taDescription.getText());
        p.setDateEvenement(dpDate.getValue());
        p.setHeureDebut(parseTime(tfHeureDebut.getText()));
        p.setHeureFin(parseTime(tfHeureFin.getText()));
        p.setLieu(tfLieu.getText().trim());
        p.setType(cbType.getValue());
        p.setPriorite(cbPriorite.getValue());

        if (planning == null) {
            service.add(p);
        } else {
            service.update(p);
        }

        if (parent != null) parent.refresh();
        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = timeStr.trim().split(":");
            return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            return null;
        }
    }

    private void clearErrors() {
        errTitre.setText("");
        errTitre.setManaged(false);
        errDate.setText("");
        errDate.setManaged(false);

        String base = "-fx-font-family:'Poppins';-fx-font-size:13;-fx-padding:10 12 10 12;" +
                "-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;";
        tfTitre.setStyle(base);
        dpDate.setStyle(base);
    }

    private void close() {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        stage.close();
    }
}