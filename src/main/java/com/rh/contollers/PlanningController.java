package com.rh.contollers;

import com.rh.components.NotificationToast;
import com.rh.models.Planning;
import com.rh.models.Planning.PrioritePlanning;
import com.rh.models.Planning.TypePlanning;
import com.rh.services.EmailService;
import com.rh.services.GoogleCalendarService;
import com.rh.services.PlanningService;
import com.rh.services.TimerService;
import com.rh.utils.EmailConfig;
import com.rh.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PlanningController implements Initializable {

    @FXML private GridPane calendrierGrid;
    @FXML private Label lblMoisAnnee;
    @FXML private Label lblJourSelectionne;
    @FXML private VBox eventListContainer;
    @FXML private Button btnNouveau;
    @FXML private StackPane rootStackPane;
    @FXML private ScrollPane mainScrollPane;

    private final PlanningService service = new PlanningService();
    private final EmailService emailService = new EmailService();
    private final GoogleCalendarService googleCalendarService = new GoogleCalendarService();

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private List<Planning> allEvents;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        // 🔥 Configuration du ScrollPane principal
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        loadMonth();
        updateEventList();
        startRappelTimer();
    }

    // ── Chargement du mois ──────────────────────────────────────────────────

    private void loadMonth() {
        lblMoisAnnee.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + currentMonth.getYear());
        calendrierGrid.getChildren().clear();
        calendrierGrid.getColumnConstraints().clear();

        // 7 colonnes de taille égale
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setFillWidth(true);
            calendrierGrid.getColumnConstraints().add(cc);
        }

        LocalDate debut = currentMonth.atDay(1);
        LocalDate fin = currentMonth.atEndOfMonth();
        allEvents = service.getByPeriode(debut, fin);

        int firstDayOfMonth = currentMonth.atDay(1).getDayOfWeek().getValue();
        int daysInMonth = currentMonth.lengthOfMonth();

        int startOffset = firstDayOfMonth - 1;
        int row = 0;
        int col = 0;

        // Jours vides
        for (int i = 0; i < startOffset; i++) {
            VBox emptyCell = createDayCell(null);
            calendrierGrid.add(emptyCell, col, row);
            col++;
        }

        // Jours du mois
        for (int day = 1; day <= daysInMonth; day++) {
            if (col == 7) {
                col = 0;
                row++;
            }

            LocalDate date = currentMonth.atDay(day);
            boolean hasEvents = allEvents.stream().anyMatch(e -> e.getDateEvenement().equals(date));
            VBox dayCell = createDayCell(date, hasEvents);
            calendrierGrid.add(dayCell, col, row);
            col++;
        }

        // RowConstraints pour que les cellules s'agrandissent
        calendrierGrid.getRowConstraints().clear();
        for (int r = 0; r <= row; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            rc.setMinHeight(80);
            calendrierGrid.getRowConstraints().add(rc);
        }

        // 🔥 Hauteur minimale du GridPane pour le scroll
        int totalRows = row + 1;
        double minHeight = totalRows * 85;
        calendrierGrid.setMinHeight(minHeight);
        calendrierGrid.setPrefHeight(minHeight);
    }

    // ── Création d'une cellule jour ─────────────────────────────────────────

    private VBox createDayCell(LocalDate date, boolean hasEvents) {
        VBox cell = new VBox();
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#F0EAE4;-fx-border-width:1;-fx-background-radius:8;-fx-border-radius:8;");
        cell.setMinHeight(70);
        cell.setPrefHeight(85);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setFillWidth(true);
        cell.setMaxWidth(Double.MAX_VALUE);

        if (date != null) {
            Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dayLabel.setStyle("-fx-font-family:'Poppins';-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#333333;-fx-padding:4 0 0 6;");

            if (date.equals(selectedDate)) {
                cell.setStyle("-fx-background-color:#FFF0F0;-fx-border-color:#75070C;-fx-border-width:2;-fx-background-radius:8;-fx-border-radius:8;");
                dayLabel.setStyle("-fx-font-family:'Poppins';-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#75070C;-fx-padding:4 0 0 6;");
            }

            if (date.equals(LocalDate.now()) && !date.equals(selectedDate)) {
                cell.setStyle("-fx-background-color:#F0F5E8;-fx-border-color:#4F6815;-fx-border-width:2;-fx-background-radius:8;-fx-border-radius:8;");
                dayLabel.setStyle("-fx-font-family:'Poppins';-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#4F6815;-fx-padding:4 0 0 6;");
            }

            if (date.getDayOfWeek().getValue() >= 6 && !date.equals(selectedDate) && !date.equals(LocalDate.now())) {
                dayLabel.setStyle("-fx-font-family:'Poppins';-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#CCCCCC;-fx-padding:4 0 0 6;");
            }

            cell.getChildren().add(dayLabel);

            if (hasEvents) {
                HBox dots = new HBox(4);
                dots.setAlignment(Pos.CENTER_LEFT);
                dots.setStyle("-fx-padding:2 0 0 6;");

                List<Planning> dayEvents = allEvents.stream()
                        .filter(e -> e.getDateEvenement().equals(date))
                        .toList();

                int count = Math.min(dayEvents.size(), 3);
                for (int i = 0; i < count; i++) {
                    Label dot = new Label("●");
                    Planning evt = dayEvents.get(i);
                    String color = switch (evt.getPriorite()) {
                        case URGENTE -> "#75070C";
                        case HAUTE -> "#C8A000";
                        case MOYENNE -> "#4F6815";
                        default -> "#888888";
                    };
                    dot.setStyle("-fx-text-fill:" + color + ";-fx-font-size:10;");
                    dots.getChildren().add(dot);
                }

                if (dayEvents.size() > 3) {
                    Label more = new Label("+" + (dayEvents.size() - 3));
                    more.setStyle("-fx-text-fill:#999999;-fx-font-size:7;-fx-font-family:'Poppins';");
                    dots.getChildren().add(more);
                }

                cell.getChildren().add(dots);
            }

            cell.setOnMouseClicked(e -> {
                selectedDate = date;
                loadMonth();
                updateEventList();
            });

        } else {
            Label emptyLabel = new Label("");
            cell.getChildren().add(emptyLabel);
            cell.setStyle("-fx-background-color:#F9F6F2;-fx-border-color:#F0EAE4;-fx-border-width:1;-fx-background-radius:8;-fx-border-radius:8;");
        }

        VBox.setVgrow(cell, Priority.ALWAYS);
        return cell;
    }

    private VBox createDayCell(LocalDate date) {
        return createDayCell(date, false);
    }

    // ── Événements du jour ──────────────────────────────────────────────────

    private void updateEventList() {
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }

        String jourSemaine = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        lblJourSelectionne.setText("📅 " + selectedDate.format(FMT_DATE) + " — " + jourSemaine);

        eventListContainer.getChildren().clear();

        List<Planning> dayEvents = allEvents.stream()
                .filter(e -> e.getDateEvenement().equals(selectedDate))
                .toList();

        if (dayEvents.isEmpty()) {
            Label noEvent = new Label("  Aucun événement ce jour");
            noEvent.setStyle("-fx-font-family:'Poppins';-fx-font-size:11;-fx-text-fill:#999999;-fx-padding:2 0 0 0;");
            eventListContainer.getChildren().add(noEvent);
        } else {
            for (Planning p : dayEvents) {
                HBox eventItem = createEventItem(p);
                eventListContainer.getChildren().add(eventItem);
            }
        }
    }

    private HBox createEventItem(Planning p) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#F0EAE4;-fx-border-width:0 0 1 0;-fx-padding:4 4 4 4;");
        item.setMaxWidth(Double.MAX_VALUE);

        Label colorDot = new Label("●");
        String color = switch (p.getPriorite()) {
            case URGENTE -> "#75070C";
            case HAUTE -> "#C8A000";
            case MOYENNE -> "#4F6815";
            default -> "#888888";
        };
        colorDot.setStyle("-fx-text-fill:" + color + ";-fx-font-size:12;");

        Label heure = new Label(p.getHeureStr());
        heure.setStyle("-fx-font-family:'Poppins';-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:#666666;-fx-min-width:60;");

        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-family:'Poppins';-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#1A1A1A;");
        titre.setMaxWidth(Double.MAX_VALUE);

        Label type = new Label(p.getTypeLabel());
        type.setStyle("-fx-font-family:'Poppins';-fx-font-size:8;-fx-text-fill:#FFFFFF;-fx-background-color:#75070C;-fx-background-radius:10;-fx-padding:1 8 1 8;");

        Button btnEdit = new Button("✏️");
        btnEdit.setStyle("-fx-font-size:10;-fx-background-color:transparent;-fx-text-fill:#888888;-fx-cursor:hand;");
        btnEdit.setOnAction(e -> openEditForm(p));

        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-font-size:10;-fx-background-color:transparent;-fx-text-fill:#888888;-fx-cursor:hand;");
        btnDelete.setOnAction(e -> deleteEvent(p));

        Label syncLabel = new Label("☁️");
        syncLabel.setStyle("-fx-font-size:10;-fx-text-fill:#4285F4;");
        Tooltip.install(syncLabel, new Tooltip("Synchronisé avec Google Calendar"));

        item.getChildren().addAll(colorDot, heure, titre, new Region(), type, syncLabel, btnEdit, btnDelete);
        HBox.setHgrow(titre, Priority.ALWAYS);

        return item;
    }

    // ── Formulaire en popup ─────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        openPopup(null);
    }

    private void openEditForm(Planning p) {
        openPopup(p);
    }

    private void openPopup(Planning p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rh/views/ajout_event.fxml"));
            Parent root = loader.load();

            AjoutEventController ctrl = loader.getController();
            ctrl.setParent(this);
            ctrl.setRootStackPane(rootStackPane);

            if (p != null) {
                ctrl.setPlanning(p);
            } else if (selectedDate != null) {
                ctrl.setDate(selectedDate);
            }

            Stage stage = new Stage();
            stage.setTitle(p == null ? "➕ Nouvel événement" : "✏️ Modifier l'événement");
            stage.setScene(new Scene(root, 520, 620));
            stage.initModality(Modality.APPLICATION_MODAL);

            if (calendrierGrid.getScene() != null) {
                stage.initOwner(calendrierGrid.getScene().getWindow());
            }

            ctrl.setStage(stage);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Timer pour les rappels ─────────────────────────────────────────────

    private void startRappelTimer() {
        TimerService.startTimer(() -> {
            checkRappels();
        }, 60000);
    }

    // ── Vérification des rappels ───────────────────────────────────────────

    private void checkRappels() {
        List<Planning> soonEvents = service.getSoon();

        for (Planning p : soonEvents) {
            if (!p.isRappelEnvoye()) {
                p.setRappelEnvoye(true);
                service.update(p);

                if (EmailConfig.EMAIL_ENABLED) {
                    String destinataire = EmailConfig.DESTINATAIRE_DEFAUT;
                    if (SessionManager.getInstance().getCurrentUser() != null) {
                        destinataire = SessionManager.getInstance().getCurrentUser().getEmail();
                    }
                    emailService.envoyerRappel(destinataire, p);
                }

                if (rootStackPane != null) {
                    String titre = "🔔 " + p.getTitre();
                    String message = "Dans 15 minutes ! 📅 " + p.getDateStr() + " ⏰ " + p.getHeureStr();
                    NotificationToast.show(
                            rootStackPane,
                            titre,
                            message,
                            p.getPriorite() == PrioritePlanning.URGENTE ? "urgent" : "normal"
                    );
                }
            }
        }
    }

    // ── Navigation mois ─────────────────────────────────────────────────────

    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        loadMonth();
        if (selectedDate != null) {
            selectedDate = currentMonth.atDay(Math.min(selectedDate.getDayOfMonth(), currentMonth.lengthOfMonth()));
            updateEventList();
        }
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        loadMonth();
        if (selectedDate != null) {
            selectedDate = currentMonth.atDay(Math.min(selectedDate.getDayOfMonth(), currentMonth.lengthOfMonth()));
            updateEventList();
        }
    }

    @FXML
    private void handleToday() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        loadMonth();
        updateEventList();
    }

    // ── Suppression ──────────────────────────────────────────────────────────

    private void deleteEvent(Planning p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer : " + p.getTitre() + " ?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                service.delete(p.getId());
                loadMonth();
                updateEventList();
            }
        });
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    public void refresh() {
        loadMonth();
        updateEventList();
    }

    public void stop() {
        TimerService.stopTimer();
    }
}