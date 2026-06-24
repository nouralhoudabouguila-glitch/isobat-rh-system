package com.rh.contollers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

public class DashboardController {

    // ── KPI Labels ───────────────────────────────────────────────────────────
    @FXML private Label kpiEmployes;
    @FXML private Label kpiPresence;
    @FXML private Label kpiConges;
    @FXML private Label kpiPlanning;

    @FXML private Label trendEmployes;
    @FXML private Label trendPresence;
    @FXML private Label trendConges;
    @FXML private Label trendPlanning;

    // ── Charts ───────────────────────────────────────────────────────────────
    @FXML private BarChart<String, Number> presenceChart;
    @FXML private PieChart                 deptChart;

    @FXML
    public void initialize() {
        // Désactiver les animations
        presenceChart.setAnimated(false);
        deptChart.setAnimated(false);

        // TODO: Connecter à la base de données et charger les KPIs
        // kpiEmployes.setText(String.valueOf(employeService.count()));
        // kpiPresence.setText(String.valueOf(presenceService.countToday()));
        // kpiConges.setText(String.valueOf(congeService.countPending()));
        // kpiPlanning.setText(String.valueOf(planningService.countActive()));

        // TODO: Charger les données du graphique de présences hebdomadaires
        // loadPresenceChart();

        // TODO: Charger les données de répartition par département
        // loadDeptChart();
    }
}
