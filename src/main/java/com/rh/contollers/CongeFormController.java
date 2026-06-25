package com.rh.contollers;

import com.rh.models.DemandeConge;
import com.rh.models.DemandeConge.TypeConge;
import com.rh.models.DemandeConge.Statut;
import com.rh.models.Employe;
import com.rh.models.SoldeConge;
import com.rh.services.DemandeCongeService;
import com.rh.services.EmployeService;
import com.rh.services.SoldeCongeService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class CongeFormController implements Initializable {

    // Employé
    @FXML private ComboBox<Employe> cbEmploye;
    @FXML private Label lblPoste;
    @FXML private Label lblSoldeRestant;
    @FXML private Label lblSoldeInitial;
    @FXML private HBox soldeBox;

    // Type de congé
    @FXML private ToggleButton tbTypeAnnuel;
    @FXML private ToggleButton tbTypeMaladie;
    @FXML private ToggleButton tbTypeExceptionnel;
    @FXML private ToggleButton tbTypeSansSolde;
    @FXML private ToggleButton tbTypeRecup;

    // Dates
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private Label lblNbJours;

    // Infos complémentaires
    @FXML private TextField tfRemplacant;
    @FXML private ToggleButton tbStatutAttente;
    @FXML private ToggleButton tbStatutApprouve;
    @FXML private ToggleButton tbStatutRefuse;
    @FXML private TextArea taCommentaire;

    // Labels erreur
    @FXML private Label errEmploye;
    @FXML private Label errType;
    @FXML private Label errDebut;
    @FXML private Label errFin;

    @FXML private Label lblFormTitle;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final DemandeCongeService service = new DemandeCongeService();
    private final EmployeService employeService = new EmployeService();
    private final SoldeCongeService soldeService = new SoldeCongeService();

    private DemandeConge editing = null;
    private CongesController parent;

    // Styles ToggleButton
    private static final String TB_OFF =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#555555;-fx-cursor:hand;-fx-padding:8 14 8 14;";

    private static final String TB_ON =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#75070C;-fx-border-color:#75070C;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#FFFFFF;-fx-cursor:hand;-fx-padding:8 14 8 14;";

    private static final String STATUT_ATTENTE_ON =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#C8A000;-fx-border-color:#C8A000;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#FFFFFF;-fx-cursor:hand;-fx-padding:8 12 8 12;";
    private static final String STATUT_ATTENTE_OFF =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#FFFBEE;-fx-border-color:#C8A000;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#C8A000;-fx-cursor:hand;-fx-padding:8 12 8 12;";
    private static final String STATUT_OK_ON =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#4F6815;-fx-border-color:#4F6815;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#FFFFFF;-fx-cursor:hand;-fx-padding:8 12 8 12;";
    private static final String STATUT_OK_OFF =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#F0F5E8;-fx-border-color:#4F6815;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#4F6815;-fx-cursor:hand;-fx-padding:8 12 8 12;";
    private static final String STATUT_NO_ON =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#75070C;-fx-border-color:#75070C;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#FFFFFF;-fx-cursor:hand;-fx-padding:8 12 8 12;";
    private static final String STATUT_NO_OFF =
            "-fx-font-family:'Poppins';-fx-font-size:11;-fx-font-weight:bold;" +
                    "-fx-background-color:#FFF0F0;-fx-border-color:#75070C;" +
                    "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;" +
                    "-fx-text-fill:#75070C;-fx-cursor:hand;-fx-padding:8 12 8 12;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupEmployeCombo();
        setupToggleGroups();
        setupToggleStyles();
        setupDateListeners();
        // Défaut : En attente
        tbStatutAttente.setSelected(true);
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupEmployeCombo() {
        List<Employe> employes = employeService.getAll();
        cbEmploye.getItems().addAll(employes);
        cbEmploye.setConverter(new StringConverter<>() {
            @Override public String toString(Employe e) {
                return e == null ? "" : e.getNom() + " " + e.getPrenom();
            }
            @Override public Employe fromString(String s) { return null; }
        });

        // Quand on sélectionne un employé → affiche le poste + le solde
        cbEmploye.valueProperty().addListener((obs, old, emp) -> {
            if (emp == null) {
                lblPoste.setText("—");
                soldeBox.setVisible(false); soldeBox.setManaged(false);
                return;
            }
            lblPoste.setText(emp.getProfil() != null ? emp.getProfil() : "—");
            refreshSolde(emp);
        });
    }

    private void setupToggleGroups() {
        ToggleGroup typeGroup = new ToggleGroup();
        tbTypeAnnuel.setToggleGroup(typeGroup);
        tbTypeMaladie.setToggleGroup(typeGroup);
        tbTypeExceptionnel.setToggleGroup(typeGroup);
        tbTypeSansSolde.setToggleGroup(typeGroup);
        tbTypeRecup.setToggleGroup(typeGroup);

        // Quand type change → refresh solde
        typeGroup.selectedToggleProperty().addListener((obs, old, n) -> {
            Employe emp = cbEmploye.getValue();
            if (emp != null) refreshSolde(emp);
        });

        ToggleGroup statutGroup = new ToggleGroup();
        tbStatutAttente.setToggleGroup(statutGroup);
        tbStatutApprouve.setToggleGroup(statutGroup);
        tbStatutRefuse.setToggleGroup(statutGroup);
    }

    private void setupToggleStyles() {
        // Types
        for (ToggleButton tb : new ToggleButton[]{
                tbTypeAnnuel, tbTypeMaladie, tbTypeExceptionnel, tbTypeSansSolde, tbTypeRecup
        }) tb.selectedProperty().addListener((o, was, is) -> tb.setStyle(is ? TB_ON : TB_OFF));

        // Statuts
        tbStatutAttente.selectedProperty().addListener((o, was, is) ->
                tbStatutAttente.setStyle(is ? STATUT_ATTENTE_ON : STATUT_ATTENTE_OFF));
        tbStatutApprouve.selectedProperty().addListener((o, was, is) ->
                tbStatutApprouve.setStyle(is ? STATUT_OK_ON : STATUT_OK_OFF));
        tbStatutRefuse.selectedProperty().addListener((o, was, is) ->
                tbStatutRefuse.setStyle(is ? STATUT_NO_ON : STATUT_NO_OFF));
    }

    /** Calcule automatiquement les jours ouvrables quand les dates changent */
    private void setupDateListeners() {
        dpDebut.valueProperty().addListener((obs, o, n) -> updateJours());
        dpFin.valueProperty().addListener((obs, o, n)   -> updateJours());
    }

    private void updateJours() {
        LocalDate debut = dpDebut.getValue();
        LocalDate fin   = dpFin.getValue();
        if (debut != null && fin != null && !fin.isBefore(debut)) {
            int jours = DemandeCongeService.calculerJoursOuvrables(debut, fin);
            lblNbJours.setText(jours + " jour" + (jours > 1 ? "s" : ""));
        } else {
            lblNbJours.setText("—");
        }
    }

    private void refreshSolde(Employe emp) {
        TypeConge type = getTypeConge();
        if (type == null) {
            soldeBox.setVisible(false); soldeBox.setManaged(false);
            return;
        }
        SoldeConge solde = soldeService.getSolde(
                emp.getId(), type.name(), LocalDate.now().getYear()
        );
        if (solde != null) {
            lblSoldeRestant.setText((int) solde.getSoldeRestant() + " jours restants");
            lblSoldeInitial.setText("sur 18 initiaux");
            soldeBox.setVisible(true); soldeBox.setManaged(true);
        } else {
            soldeBox.setVisible(false); soldeBox.setManaged(false);
        }
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setParent(CongesController parent) { this.parent = parent; }

    public void setDemande(DemandeConge d) {
        this.editing = d;
        if (d == null) return;
        if (lblFormTitle != null) lblFormTitle.setText("Modifier la demande");

        // Employé
        cbEmploye.getItems().stream()
                .filter(e -> e.getId() == d.getEmploye().getId())
                .findFirst().ifPresent(cbEmploye::setValue);

        // Type
        if (d.getTypeConge() != null) switch (d.getTypeConge()) {
            case ANNUEL       -> tbTypeAnnuel.setSelected(true);
            case MALADIE      -> tbTypeMaladie.setSelected(true);
            case EXCEPTIONNEL -> tbTypeExceptionnel.setSelected(true);
            case SANS_SOLDE   -> tbTypeSansSolde.setSelected(true);
            case RECUPERATION -> tbTypeRecup.setSelected(true);
        }

        // Dates
        dpDebut.setValue(d.getDateDebut());
        dpFin.setValue(d.getDateFin());

        // Remplaçant + Commentaire
        tfRemplacant.setText(d.getRemplacant() != null ? d.getRemplacant() : "");
        taCommentaire.setText(d.getCommentaire() != null ? d.getCommentaire() : "");

        // Statut
        if (d.getStatut() != null) switch (d.getStatut()) {
            case APPROUVE -> tbStatutApprouve.setSelected(true);
            case REFUSE   -> tbStatutRefuse.setSelected(true);
            default       -> tbStatutAttente.setSelected(true);
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        clearErrors();
        if (!validateFields()) return;

        DemandeConge d = editing != null ? editing : new DemandeConge();

        d.setEmploye(cbEmploye.getValue());
        d.setTypeConge(getTypeConge());
        d.setDateDebut(dpDebut.getValue());
        d.setDateFin(dpFin.getValue());
        d.setNombreJours(DemandeCongeService.calculerJoursOuvrables(
                dpDebut.getValue(), dpFin.getValue()));
        d.setRemplacant(tfRemplacant.getText().trim());
        d.setCommentaire(taCommentaire.getText().trim());
        d.setStatut(getStatut());

        if (editing == null) {
            service.add(d);
            // Si directement approuvé → décrémenter le solde
            if (d.getStatut() == Statut.APPROUVE) {
                int result = service.approuver(d);
                if (result == 1) {
                    Alert warn = new Alert(Alert.AlertType.WARNING);
                    warn.setTitle("Solde insuffisant");
                    warn.setHeaderText("Solde insuffisant");
                    warn.setContentText("Le solde de l'employé est insuffisant pour approuver cette demande.");
                    warn.showAndWait();
                    return;
                }
            }
        } else {
            service.update(d);
        }

        if (parent != null) parent.refresh();
        close();
    }

    @FXML
    private void handleCancel() { close(); }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateFields() {
        boolean ok = true;
        String errStyle = "-fx-font-family:'Poppins';-fx-font-size:12;-fx-padding:9 12 9 12;" +
                "-fx-background-color:#FFF5F5;-fx-border-color:#75070C;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;";

        if (cbEmploye.getValue() == null) {
            errEmploye.setText("Veuillez sélectionner un employé"); ok = false;
        }
        if (getTypeConge() == null) {
            errType.setText("Veuillez choisir un type de congé"); ok = false;
        }
        if (dpDebut.getValue() == null) {
            errDebut.setText("Date de début obligatoire"); ok = false;
        }
        if (dpFin.getValue() == null) {
            errFin.setText("Date de fin obligatoire"); ok = false;
        }
        if (dpDebut.getValue() != null && dpFin.getValue() != null
                && dpFin.getValue().isBefore(dpDebut.getValue())) {
            errFin.setText("La date de fin doit être après la date de début"); ok = false;
        }
        return ok;
    }

    private void clearErrors() {
        errEmploye.setText(""); errType.setText("");
        errDebut.setText(""); errFin.setText("");
    }

    private void close() { ((Stage) btnCancel.getScene().getWindow()).close(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TypeConge getTypeConge() {
        if (tbTypeAnnuel.isSelected())       return TypeConge.ANNUEL;
        if (tbTypeMaladie.isSelected())      return TypeConge.MALADIE;
        if (tbTypeExceptionnel.isSelected()) return TypeConge.EXCEPTIONNEL;
        if (tbTypeSansSolde.isSelected())    return TypeConge.SANS_SOLDE;
        if (tbTypeRecup.isSelected())        return TypeConge.RECUPERATION;
        return null;
    }

    private Statut getStatut() {
        if (tbStatutApprouve.isSelected()) return Statut.APPROUVE;
        if (tbStatutRefuse.isSelected())   return Statut.REFUSE;
        return Statut.EN_ATTENTE;
    }
}