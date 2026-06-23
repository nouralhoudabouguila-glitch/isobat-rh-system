package com.rh.contollers;

import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.services.EmployeService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EmployesController implements Initializable {

    @FXML private Label lblMatricule;
    @FXML private Label lblFormTitle;
    @FXML private ToggleButton tbSitCelib, tbSitMarie, tbSitDivorce, tbSitVeuf;
    @FXML private TextField tfNom, tfPrenom, tfCIN, tfTelephone, tfCNSS, tfProfil, tfSalaire, tfRIB;
    @FXML private DatePicker dpNaissance, dpEmbauche;
    @FXML private ToggleButton tbDeptB2B, tbDeptB2C, tbDeptBureau;
    @FXML private ToggleButton tbContratCDI, tbContratCDD, tbContratStage;
    @FXML private ToggleButton tbStatutActif, tbStatutInactif;
    @FXML private ComboBox<String> cbBanque;
    @FXML private Label errNom, errCIN, errTelephone, errDept, errEmbauche;
    @FXML private Button btnCancel, btnSaveAndAdd, btnCreate;

    private final EmployeService service = new EmployeService();
    private Employe editing = null;
    private EmployeController parent;

    // ── Styles constants ────────────────────────────────────────────────────
    private static final String SIT_OFF  = "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1.5;-fx-text-fill:#555555;-fx-cursor:hand;-fx-padding:7 12 7 12;";
    private static final String SIT_ON   = "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;-fx-background-color:#FFF0F0;-fx-border-color:#75070C;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1.5;-fx-text-fill:#75070C;-fx-cursor:hand;-fx-padding:7 12 7 12;";
    private static final String DEPT_OFF = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-text-fill:#555555;-fx-cursor:hand;-fx-padding:14 10 14 10;-fx-alignment:center;";
    private static final String DEPT_ON  = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-background-color:#75070C;-fx-border-color:#75070C;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-text-fill:#FFFFFF;-fx-cursor:hand;-fx-padding:14 10 14 10;-fx-alignment:center;";
    private static final String CT_OFF   = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-text-fill:#4F6815;-fx-cursor:hand;-fx-padding:8 22 8 22;";
    private static final String CT_ON    = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-background-color:#4F6815;-fx-border-color:#4F6815;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-text-fill:#FFFFFF;-fx-cursor:hand;-fx-padding:8 22 8 22;";
    private static final String ACT_OFF  = "-fx-font-family:'Poppins';-fx-background-color:#F0F5E8;-fx-border-color:#4F6815;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-cursor:hand;-fx-padding:8 20 8 20;";
    private static final String ACT_ON   = "-fx-font-family:'Poppins';-fx-background-color:#4F6815;-fx-border-color:#4F6815;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-cursor:hand;-fx-padding:8 20 8 20;";
    private static final String INACT_OFF= "-fx-font-family:'Poppins';-fx-background-color:#FFF0F0;-fx-border-color:#75070C;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-cursor:hand;-fx-padding:8 20 8 20;";
    private static final String INACT_ON = "-fx-font-family:'Poppins';-fx-background-color:#75070C;-fx-border-color:#75070C;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;-fx-cursor:hand;-fx-padding:8 20 8 20;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGroups();
        setupListeners();
        setupBanques();
        generateMatricule();
        // Defaults
        tbSitCelib.setSelected(true);
        tbStatutActif.setSelected(true);
        tbContratCDI.setSelected(true);
    }

    private void setupGroups() {
        ToggleGroup g1 = new ToggleGroup();
        tbSitCelib.setToggleGroup(g1); tbSitMarie.setToggleGroup(g1);
        tbSitDivorce.setToggleGroup(g1); tbSitVeuf.setToggleGroup(g1);

        ToggleGroup g2 = new ToggleGroup();
        tbDeptB2B.setToggleGroup(g2); tbDeptB2C.setToggleGroup(g2); tbDeptBureau.setToggleGroup(g2);
        tbDeptB2B.setUserData(Departement.CENTRE_APPEL_B2B);
        tbDeptB2C.setUserData(Departement.CENTRE_APPEL_B2C);
        tbDeptBureau.setUserData(Departement.BUREAU_ETUDE);

        ToggleGroup g3 = new ToggleGroup();
        tbContratCDI.setToggleGroup(g3); tbContratCDD.setToggleGroup(g3); tbContratStage.setToggleGroup(g3);

        ToggleGroup g4 = new ToggleGroup();
        tbStatutActif.setToggleGroup(g4); tbStatutInactif.setToggleGroup(g4);
    }

    /** Listeners qui changent le style inline quand selected change */
    private void setupListeners() {
        for (ToggleButton tb : new ToggleButton[]{tbSitCelib, tbSitMarie, tbSitDivorce, tbSitVeuf})
            tb.selectedProperty().addListener((o, was, is) -> tb.setStyle(is ? SIT_ON : SIT_OFF));

        for (ToggleButton tb : new ToggleButton[]{tbDeptB2B, tbDeptB2C, tbDeptBureau})
            tb.selectedProperty().addListener((o, was, is) -> tb.setStyle(is ? DEPT_ON : DEPT_OFF));

        for (ToggleButton tb : new ToggleButton[]{tbContratCDI, tbContratCDD, tbContratStage})
            tb.selectedProperty().addListener((o, was, is) -> tb.setStyle(is ? CT_ON : CT_OFF));

        tbStatutActif.selectedProperty().addListener((o, was, is) -> tbStatutActif.setStyle(is ? ACT_ON : ACT_OFF));
        tbStatutInactif.selectedProperty().addListener((o, was, is) -> tbStatutInactif.setStyle(is ? INACT_ON : INACT_OFF));
    }

    private void setupBanques() {
        cbBanque.getItems().addAll("STB", "BNA", "BIAT", "Attijari Bank", "BH Bank", "UIB", "Autre");
    }

    private void generateMatricule() {
        int last = service.getAll().stream().mapToInt(Employe::getId).max().orElse(0);
        lblMatricule.setText("EMP-0" + (last + 1));
    }

    public void setParent(EmployeController parent) { this.parent = parent; }

    public void setEmploye(Employe e) {
        this.editing = e;
        if (e == null) return;
        if (lblFormTitle != null) lblFormTitle.setText("Modifier — " + e.getNom() + " " + e.getPrenom());
        lblMatricule.setText("EMP-0" + e.getId());
        tfNom.setText(e.getNom()); tfPrenom.setText(e.getPrenom());
        dpNaissance.setValue(e.getDateNaissance());
        tfCIN.setText(e.getCin()); tfTelephone.setText(e.getTelephone());
        tfCNSS.setText(e.getnCNSS()); tfProfil.setText(e.getProfil());
        dpEmbauche.setValue(e.getDateEmbauche());
        tfSalaire.setText(String.valueOf(e.getSalaireMensuelNet()));
        tfRIB.setText(e.getnRIB());
        String sit = e.getSituationFamiliale();
        if (sit != null) {
            if (sit.contains("Marié")) tbSitMarie.setSelected(true);
            else if (sit.contains("Divorcé")) tbSitDivorce.setSelected(true);
            else if (sit.contains("Veuf")) tbSitVeuf.setSelected(true);
            else tbSitCelib.setSelected(true);
        }
        if (e.getDepartement() != null) switch (e.getDepartement()) {
            case CENTRE_APPEL_B2B -> tbDeptB2B.setSelected(true);
            case CENTRE_APPEL_B2C -> tbDeptB2C.setSelected(true);
            case BUREAU_ETUDE     -> tbDeptBureau.setSelected(true);
        }
        String c = e.getTypeContrat();
        if ("CDD".equals(c)) tbContratCDD.setSelected(true);
        else if ("Stage".equals(c)) tbContratStage.setSelected(true);
        else tbContratCDI.setSelected(true);
        if ("Inactif".equalsIgnoreCase(e.getStatut())) tbStatutInactif.setSelected(true);
        else tbStatutActif.setSelected(true);
    }

    @FXML private void handleCreate()     { handleSave(false); }
    @FXML private void handleSaveAndAdd() { handleSave(true);  }
    @FXML private void handleCancel()     { close(); }

    private void handleSave(boolean addAnother) {
        clearErrors();
        if (!validateFields()) return;
        Employe e = editing != null ? editing : new Employe();
        e.setNom(tfNom.getText().trim()); e.setPrenom(tfPrenom.getText().trim());
        e.setDateNaissance(dpNaissance.getValue());
        e.setSituationFamiliale(getSit());
        e.setCin(tfCIN.getText().trim()); e.setTelephone(tfTelephone.getText().trim());
        e.setnCNSS(tfCNSS.getText().trim());
        ToggleButton d = getSelected(tbDeptB2B, tbDeptB2C, tbDeptBureau);
        if (d != null && d.getUserData() instanceof Departement)
            e.setDepartement((Departement) d.getUserData());
        e.setProfil(tfProfil.getText().trim()); e.setDateEmbauche(dpEmbauche.getValue());
        try { e.setSalaireMensuelNet(Double.parseDouble(tfSalaire.getText().trim())); }
        catch (Exception ex) { e.setSalaireMensuelNet(0); }
        ToggleButton ct = getSelected(tbContratCDI, tbContratCDD, tbContratStage);
        e.setTypeContrat(ct != null ? ct.getText() : null);
        ToggleButton st = getSelected(tbStatutActif, tbStatutInactif);
        e.setStatut(st != null ? st.getText() : null);
        e.setnRIB(tfRIB.getText().trim());
        if (editing == null) service.add(e); else service.update(e);
        if (parent != null) parent.refreshCards();
        if (addAnother) resetForm(); else close();
    }

    private boolean validateFields() {
        boolean ok = true;
        String err = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-padding:9 12 9 12;-fx-background-color:#FFF5F5;-fx-border-color:#75070C;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;";
        if (tfNom.getText() == null || tfNom.getText().trim().isEmpty())
        { errNom.setText("Obligatoire"); tfNom.setStyle(err); ok = false; }
        if (tfCIN.getText() == null || !tfCIN.getText().trim().matches("\\d{8}"))
        { errCIN.setText("8 chiffres requis"); tfCIN.setStyle(err); ok = false; }
        if (tfTelephone.getText() == null || tfTelephone.getText().trim().isEmpty())
        { errTelephone.setText("Obligatoire"); tfTelephone.setStyle(err); ok = false; }
        if (getSelected(tbDeptB2B, tbDeptB2C, tbDeptBureau) == null)
        { errDept.setText("Choisissez un département"); ok = false; }
        if (dpEmbauche.getValue() == null)
        { errEmbauche.setText("Obligatoire"); ok = false; }
        return ok;
    }

    private void clearErrors() {
        errNom.setText(""); errCIN.setText(""); errTelephone.setText("");
        errDept.setText(""); errEmbauche.setText("");
        String base = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-padding:9 12 9 12;-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1;";
        tfNom.setStyle(base); tfCIN.setStyle(base); tfTelephone.setStyle(base);
    }

    private void resetForm() {
        editing = null;
        tfNom.clear(); tfPrenom.clear(); dpNaissance.setValue(null);
        tfCIN.clear(); tfTelephone.clear(); tfCNSS.clear();
        tfProfil.clear(); dpEmbauche.setValue(null); tfSalaire.clear(); tfRIB.clear();
        cbBanque.setValue(null);
        tbSitCelib.setSelected(true);
        tbDeptB2B.setSelected(false); tbDeptB2C.setSelected(false); tbDeptBureau.setSelected(false);
        tbContratCDI.setSelected(true); tbStatutActif.setSelected(true);
        if (lblFormTitle != null) lblFormTitle.setText("Nouvel employé");
        generateMatricule(); clearErrors();
    }

    private void close() { ((Stage) btnCancel.getScene().getWindow()).close(); }

    private String getSit() {
        if (tbSitMarie.isSelected())   return tbSitMarie.getText();
        if (tbSitDivorce.isSelected()) return tbSitDivorce.getText();
        if (tbSitVeuf.isSelected())    return tbSitVeuf.getText();
        return tbSitCelib.getText();
    }

    private ToggleButton getSelected(ToggleButton... buttons) {
        for (ToggleButton b : buttons) if (b.isSelected()) return b;
        return null;
    }
}
