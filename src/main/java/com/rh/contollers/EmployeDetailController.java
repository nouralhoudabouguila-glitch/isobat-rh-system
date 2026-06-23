package com.rh.contollers;

import com.rh.models.Employe;
import com.rh.services.EmployeService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EmployeDetailController implements Initializable {

    @FXML private Label lblName;
    @FXML private Label lblDept;
    @FXML private Label lblProfil;
    @FXML private Label lblTel;
    @FXML private Label lblSalaire;
    @FXML private Label lblDateEmbauche;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClose;

    private EmployeService service = new EmployeService();
    private Employe employe;
    private EmployeController parent;

    public void setEmploye(Employe e) {
        this.employe = e;
        populate();
    }

    public void setParent(EmployeController parent) { this.parent = parent; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnClose.setOnAction(e -> close());

        btnDelete.setOnAction(e -> {
            if (employe != null) {
                service.delete(employe);
                if (parent != null) parent.refreshCards();
                close();
            }
        });

        btnEdit.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/rh/views/employe_form.fxml"));
                javafx.scene.Parent root = loader.load();
                EmployeFormController controller = loader.getController();
                controller.setParent(parent);
                controller.setEmploye(employe);

                Stage stage = new Stage();
                stage.setTitle("Modifier un employé");
                stage.setScene(new javafx.scene.Scene(root));
                stage.initOwner(btnEdit.getScene().getWindow());
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.showAndWait();
                // refresh after edit
                if (parent != null) parent.refreshCards();
                close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void populate() {
        if (employe == null) return;
        lblName.setText(employe.getNom() + " " + employe.getPrenom());
        lblDept.setText("Département: " + (employe.getDepartement() != null ? employe.getDepartement().toString() : "-"));
        lblProfil.setText("Profil: " + (employe.getProfil() != null ? employe.getProfil() : "-"));
        lblTel.setText("Tel: " + (employe.getTelephone() != null ? employe.getTelephone() : "-"));
        lblSalaire.setText(String.format("Salaire: %.2f DH", employe.getSalaireMensuelNet()));
        lblDateEmbauche.setText("Embauché le: " + (employe.getDateEmbauche() != null ? employe.getDateEmbauche().toString() : "-"));
    }

    private void close() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}


