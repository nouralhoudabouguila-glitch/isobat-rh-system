package com.rh.contollers;

import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.services.EmployeService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EmployeFormController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private DatePicker dpNaissance;
    @FXML private ChoiceBox<Departement> cbDepartement;
    @FXML private TextField tfProfil;
    @FXML private TextField tfTelephone;
    @FXML private TextField tfSalaire;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private final EmployeService service = new EmployeService();
    private EmployeController parent;
    private Employe editing;

    public void setParent(EmployeController parent) {
        this.parent = parent;
    }

    public void setEmploye(Employe e) {
        this.editing = e;
        if (e == null) return;
        tfNom.setText(e.getNom());
        tfPrenom.setText(e.getPrenom());
        dpNaissance.setValue(e.getDateNaissance());
        cbDepartement.setValue(e.getDepartement());
        tfProfil.setText(e.getProfil());
        tfTelephone.setText(e.getTelephone());
        tfSalaire.setText(String.valueOf(e.getSalaireMensuelNet()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbDepartement.getItems().setAll(Departement.values());

        btnCancel.setOnAction(e -> close());

        btnSave.setOnAction(e -> {
            if (editing == null) {
                Employe emp = new Employe();
                emp.setNom(tfNom.getText());
                emp.setPrenom(tfPrenom.getText());
                emp.setDateNaissance(dpNaissance.getValue());
                emp.setDepartement(cbDepartement.getValue());
                emp.setProfil(tfProfil.getText());
                emp.setTelephone(tfTelephone.getText());
                try {
                    emp.setSalaireMensuelNet(Double.parseDouble(tfSalaire.getText()));
                } catch (NumberFormatException ex) {
                    emp.setSalaireMensuelNet(0);
                }
                service.add(emp);
            } else {
                editing.setNom(tfNom.getText());
                editing.setPrenom(tfPrenom.getText());
                editing.setDateNaissance(dpNaissance.getValue());
                editing.setDepartement(cbDepartement.getValue());
                editing.setProfil(tfProfil.getText());
                editing.setTelephone(tfTelephone.getText());
                try {
                    editing.setSalaireMensuelNet(Double.parseDouble(tfSalaire.getText()));
                } catch (NumberFormatException ex) {
                    editing.setSalaireMensuelNet(0);
                }
                service.update(editing);
            }
            if (parent != null) parent.refreshCards();
            close();
        });
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}


