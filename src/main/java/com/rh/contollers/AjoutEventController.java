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

public class AjoutEventController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private TextField tfTitre;
    @FXML private TextField tfDescription;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeureDebut;
    @FXML private TextField tfHeureFin;
    @FXML private TextField tfLieu;
    @FXML private ComboBox<TypePlanning> cbType;
    @FXML private ComboBox<PrioritePlanning> cbPriorite;
    @FXML private Label errTitre;
    @FXML private Label errDate;
    @FXML private Label errForm;

    private final PlanningService service = new PlanningService();
    private Planning planning;
    private PlanningController parent;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbType.getItems().setAll(TypePlanning.values());
        cbPriorite.getItems().setAll(PrioritePlanning.values());
        cbType.setValue(TypePlanning.AUTRE);
        cbPriorite.setValue(PrioritePlanning.MOYENNE);
        dpDate.setValue(LocalDate.now());
    }

    public void setParent(PlanningController parent) {
        this.parent = parent;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPlanning(Planning p) {
        if (p == null) return;
        this.planning = p;
        lblTitle.setText("✏️ Modifier l'événement");
        tfTitre.setText(p.getTitre());
        tfDescription.setText(p.getDescription());
        dpDate.setValue(p.getDateEvenement());
        tfHeureDebut.setText(p.getHeureDebutStr());
        tfHeureFin.setText(p.getHeureFinStr());
        tfLieu.setText(p.getLieu());
        cbType.setValue(p.getType());
        cbPriorite.setValue(p.getPriorite());
    }

    public void setDate(LocalDate date) {
        if (date != null) {
            dpDate.setValue(date);
        }
    }

    @FXML
    private void handleSave() {
        clearErrors();

        boolean ok = true;

        if (tfTitre.getText().trim().isEmpty()) {
            errTitre.setText("Le titre est obligatoire");
            errTitre.setManaged(true);
            ok = false;
        }

        if (dpDate.getValue() == null) {
            errDate.setText("La date est obligatoire");
            errDate.setManaged(true);
            ok = false;
        }

        if (!ok) return;

        Planning p = planning != null ? planning : new Planning();
        p.setTitre(tfTitre.getText().trim());
        p.setDescription(tfDescription.getText().trim());
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

        if (parent != null) {
            parent.refresh();
        }
        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
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
        errForm.setVisible(false);
        errForm.setManaged(false);
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }
}