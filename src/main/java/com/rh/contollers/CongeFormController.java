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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CongeFormController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private ComboBox<Employe> cbEmploye;
    @FXML private Label lblPoste;
    @FXML private Label lblSoldeRestant;
    @FXML private Label lblSoldeInitial;
    @FXML private HBox soldeBox;

    @FXML private ToggleButton tbTypeAnnuel;
    @FXML private ToggleButton tbTypeMaladie;
    @FXML private ToggleButton tbTypeExceptionnel;
    @FXML private ToggleButton tbTypeSansSolde;
    @FXML private ToggleButton tbTypeRecup;

    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private Label lblNbJours;

    @FXML private TextField tfRemplacant;
    @FXML private ToggleButton tbStatutAttente;
    @FXML private ToggleButton tbStatutApprouve;
    @FXML private ToggleButton tbStatutRefuse;
    @FXML private TextArea taCommentaire;

    @FXML private Label errEmploye;
    @FXML private Label errType;
    @FXML private Label errDebut;
    @FXML private Label errFin;

    @FXML private Label lblFormTitle;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // ── Services ──────────────────────────────────────────────────────────────
    private final DemandeCongeService service = new DemandeCongeService();
    private final EmployeService employeService = new EmployeService();
    private final SoldeCongeService soldeService = new SoldeCongeService();

    // ── Attributs ─────────────────────────────────────────────────────────────
    private DemandeConge editing = null;
    private CongesController parent;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Styles ToggleButton ──────────────────────────────────────────────────
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

    // ── Initialize ────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupEmployeCombo();
        setupToggleGroups();
        setupToggleStyles();
        setupDateListeners();
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

        cbEmploye.valueProperty().addListener((obs, old, emp) -> {
            if (emp == null) {
                lblPoste.setText("—");
                soldeBox.setVisible(false);
                soldeBox.setManaged(false);
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
        for (ToggleButton tb : new ToggleButton[]{
                tbTypeAnnuel, tbTypeMaladie, tbTypeExceptionnel, tbTypeSansSolde, tbTypeRecup
        }) {
            tb.selectedProperty().addListener((o, was, is) -> tb.setStyle(is ? TB_ON : TB_OFF));
        }

        tbStatutAttente.selectedProperty().addListener((o, was, is) ->
                tbStatutAttente.setStyle(is ? STATUT_ATTENTE_ON : STATUT_ATTENTE_OFF));
        tbStatutApprouve.selectedProperty().addListener((o, was, is) ->
                tbStatutApprouve.setStyle(is ? STATUT_OK_ON : STATUT_OK_OFF));
        tbStatutRefuse.selectedProperty().addListener((o, was, is) ->
                tbStatutRefuse.setStyle(is ? STATUT_NO_ON : STATUT_NO_OFF));
    }

    private void setupDateListeners() {
        dpDebut.valueProperty().addListener((obs, o, n) -> updateJours());
        dpFin.valueProperty().addListener((obs, o, n) -> updateJours());
    }

    private void updateJours() {
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();
        if (debut != null && fin != null && !fin.isBefore(debut)) {
            int jours = DemandeCongeService.calculerJoursOuvrables(debut, fin);
            lblNbJours.setText(jours + " jour" + (jours > 1 ? "s" : ""));
        } else {
            lblNbJours.setText("—");
        }
    }

    /**
     * Rafraîchit le solde affiché en utilisant le calcul selon la date d'embauche
     */
    private void refreshSolde(Employe emp) {
        TypeConge type = getTypeConge();
        if (type == null) {
            soldeBox.setVisible(false);
            soldeBox.setManaged(false);
            return;
        }

        int annee = LocalDate.now().getYear();
        SoldeConge solde = soldeService.getSolde(emp.getId(), type.name(), annee);

        if (solde == null) {
            soldeService.initSoldesAnnuels(emp, annee);
            solde = soldeService.getSolde(emp.getId(), type.name(), annee);
        }

        if (solde != null) {
            double restant = solde.getSoldeRestant();
            lblSoldeRestant.setText((int) restant + " jours restants");
            lblSoldeInitial.setText("sur " + (int) solde.getSoldeInitial() + " initiaux");
            soldeBox.setVisible(true);
            soldeBox.setManaged(true);
        } else {
            soldeBox.setVisible(false);
            soldeBox.setManaged(false);
        }
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setParent(CongesController parent) {
        this.parent = parent;
    }

    public void setDemande(DemandeConge d) {
        this.editing = d;
        if (d == null) return;
        if (lblFormTitle != null) lblFormTitle.setText("Modifier la demande");

        cbEmploye.getItems().stream()
                .filter(e -> e.getId() == d.getEmploye().getId())
                .findFirst().ifPresent(cbEmploye::setValue);

        if (d.getTypeConge() != null) {
            switch (d.getTypeConge()) {
                case ANNUEL -> tbTypeAnnuel.setSelected(true);
                case MALADIE -> tbTypeMaladie.setSelected(true);
                case EXCEPTIONNEL -> tbTypeExceptionnel.setSelected(true);
                case SANS_SOLDE -> tbTypeSansSolde.setSelected(true);
                case RECUPERATION -> tbTypeRecup.setSelected(true);
            }
        }

        dpDebut.setValue(d.getDateDebut());
        dpFin.setValue(d.getDateFin());

        tfRemplacant.setText(d.getRemplacant() != null ? d.getRemplacant() : "");
        taCommentaire.setText(d.getCommentaire() != null ? d.getCommentaire() : "");

        if (d.getStatut() != null) {
            switch (d.getStatut()) {
                case APPROUVE -> tbStatutApprouve.setSelected(true);
                case REFUSE -> tbStatutRefuse.setSelected(true);
                default -> tbStatutAttente.setSelected(true);
            }
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
            if (d.getStatut() == Statut.APPROUVE) {
                int result = service.approuver(d);
                if (result == 1) {
                    showSoldeInsuffisantAlert(d);
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
    private void handleCancel() {
        close();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateFields() {
        boolean ok = true;

        if (cbEmploye.getValue() == null) {
            errEmploye.setText("Veuillez sélectionner un employé");
            ok = false;
        }
        if (getTypeConge() == null) {
            errType.setText("Veuillez choisir un type de congé");
            ok = false;
        }
        if (dpDebut.getValue() == null) {
            errDebut.setText("Date de début obligatoire");
            ok = false;
        }
        if (dpFin.getValue() == null) {
            errFin.setText("Date de fin obligatoire");
            ok = false;
        }
        if (dpDebut.getValue() != null && dpFin.getValue() != null
                && dpFin.getValue().isBefore(dpDebut.getValue())) {
            errFin.setText("La date de fin doit être après la date de début");
            ok = false;
        }

        // Vérification ISOBAT : les congés annuels doivent être en Août ou Décembre
        if (dpDebut.getValue() != null && dpFin.getValue() != null
                && getTypeConge() == TypeConge.ANNUEL) {
            LocalDate debut = dpDebut.getValue();
            LocalDate fin = dpFin.getValue();
            int moisDebut = debut.getMonthValue();
            int moisFin = fin.getMonthValue();

            boolean estEnAout = (moisDebut == 8 || moisFin == 8);
            boolean estEnDecembre = (moisDebut == 12 || moisFin == 12);

            if (!estEnAout && !estEnDecembre) {
                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Période de congé");
                warning.setHeaderText("⚠️ Période non standard");
                warning.setContentText("Selon la règle ISOBAT, les congés annuels sont pris en Août ou Décembre.\n" +
                        "La période demandée (" + debut.format(FMT_DATE) + " - " + fin.format(FMT_DATE) + ") est en dehors de ces mois.\n\n" +
                        "Souhaitez-vous continuer ?");
                ButtonType continuer = new ButtonType("Continuer quand même");
                ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                warning.getButtonTypes().setAll(continuer, annuler);

                var result = warning.showAndWait();
                if (result.isPresent() && result.get() == annuler) {
                    return false;
                }
            }
        }

        return ok;
    }

    private void clearErrors() {
        errEmploye.setText("");
        errType.setText("");
        errDebut.setText("");
        errFin.setText("");
    }

    private void close() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    // ── Alertes ───────────────────────────────────────────────────────────────

    private void showSoldeInsuffisantAlert(DemandeConge d) {
        Employe emp = d.getEmploye();
        Alert warn = new Alert(Alert.AlertType.WARNING);
        warn.setTitle("Solde insuffisant");
        warn.setHeaderText("Solde insuffisant");

        String msg = "L'employé " + emp.getNom() + " " + emp.getPrenom() + " n'a pas assez de jours de congé.\n\n";

        if (emp.getDateEmbauche() != null) {
            int annee = LocalDate.now().getYear();
            int moisEmbauche = emp.getDateEmbauche().getMonthValue();
            int anneeEmbauche = emp.getDateEmbauche().getYear();

            if (anneeEmbauche == annee && moisEmbauche > 6) {
                msg += "⚠️ Employé embauché après le 1er Juillet " + annee + ".\n";
                msg += "Selon la règle ISOBAT, il n'a pas droit aux congés cette année.\n";
                msg += "Il devra attendre l'année prochaine.";
            } else {
                SoldeConge solde = soldeService.getSolde(emp.getId(), d.getTypeConge().name(), annee);
                if (solde != null) {
                    msg += "Solde restant : " + (int) solde.getSoldeRestant() + " jours\n";
                    msg += "Jours demandés : " + d.getNombreJours() + " jours\n";
                    msg += "Il manque " + (d.getNombreJours() - (int) solde.getSoldeRestant()) + " jours.";
                }
            }
        }

        warn.setContentText(msg);
        warn.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TypeConge getTypeConge() {
        if (tbTypeAnnuel.isSelected()) return TypeConge.ANNUEL;
        if (tbTypeMaladie.isSelected()) return TypeConge.MALADIE;
        if (tbTypeExceptionnel.isSelected()) return TypeConge.EXCEPTIONNEL;
        if (tbTypeSansSolde.isSelected()) return TypeConge.SANS_SOLDE;
        if (tbTypeRecup.isSelected()) return TypeConge.RECUPERATION;
        return null;
    }

    private Statut getStatut() {
        if (tbStatutApprouve.isSelected()) return Statut.APPROUVE;
        if (tbStatutRefuse.isSelected()) return Statut.REFUSE;
        return Statut.EN_ATTENTE;
    }
}