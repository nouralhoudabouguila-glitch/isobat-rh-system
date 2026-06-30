package com.rh.contollers;

import com.rh.models.Departement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class DepenseController {

    @FXML
    private ComboBox<String> comboCentre;

    @FXML
    private Label lblTotalGeneral;

    // Cartes de dépenses
    @FXML
    private VBox cardLoyer;
    @FXML
    private VBox cardServices;
    @FXML
    private VBox cardFournitures;
    @FXML
    private VBox cardTransport;
    @FXML
    private VBox cardSalaires;
    @FXML
    private VBox cardAutre;

    // Labels des montants
    @FXML
    private Label lblMontantLoyer;
    @FXML
    private Label lblMontantServices;
    @FXML
    private Label lblMontantFournitures;
    @FXML
    private Label lblMontantTransport;
    @FXML
    private Label lblMontantSalaires;
    @FXML
    private Label lblMontantAutre;

    // Labels des compteurs
    @FXML
    private Label lblCountLoyer;
    @FXML
    private Label lblCountServices;
    @FXML
    private Label lblCountFournitures;
    @FXML
    private Label lblCountTransport;
    @FXML
    private Label lblCountSalaires;
    @FXML
    private Label lblCountAutre;

    private String centreActuel = "Centre d'appel B2B";

    @FXML
    public void initialize() {
        // Configurer le ComboBox avec les labels des départements
        ObservableList<String> centres = FXCollections.observableArrayList();
        for (Departement dept : Departement.values()) {
            centres.add(dept.toString());
        }
        comboCentre.setItems(centres);
        comboCentre.setValue("Centre d'appel B2B");

        // Ajouter un écouteur pour le changement de centre
        comboCentre.setOnAction(e -> {
            centreActuel = comboCentre.getValue();
            chargerDonnees(centreActuel);
        });

        // Charger les données initiales
        chargerDonnees(centreActuel);
    }

    private void chargerDonnees(String centre) {
        // Données statiques d'exemple (à remplacer par les données de la BD)
        switch (centre) {
            case "Centre d'appel B2B":
                setMontants(1250.00, 890.00, 450.00, 320.00, 4500.00, 150.00, 3, 2, 4, 2, 1, 1);
                break;
            case "Centre d'appel B2C":
                setMontants(1250.00, 760.00, 380.00, 280.00, 3800.00, 120.00, 3, 2, 3, 2, 1, 1);
                break;
            case "Bureau d'étude":
                setMontants(1800.00, 1200.00, 650.00, 450.00, 6200.00, 220.00, 3, 3, 5, 3, 2, 2);
                break;
            case "Administration":
                setMontants(1500.00, 950.00, 520.00, 350.00, 5400.00, 180.00, 3, 2, 4, 2, 1, 1);
                break;
            default:
                setMontants(0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 0, 0, 0, 0, 0);
                break;
        }

        // Mettre à jour le total général
        double total = Double.parseDouble(lblMontantLoyer.getText().replace(" €", "").replace(",", ".")) +
                Double.parseDouble(lblMontantServices.getText().replace(" €", "").replace(",", ".")) +
                Double.parseDouble(lblMontantFournitures.getText().replace(" €", "").replace(",", ".")) +
                Double.parseDouble(lblMontantTransport.getText().replace(" €", "").replace(",", ".")) +
                Double.parseDouble(lblMontantSalaires.getText().replace(" €", "").replace(",", ".")) +
                Double.parseDouble(lblMontantAutre.getText().replace(" €", "").replace(",", "."));

        lblTotalGeneral.setText(String.format("%.2f €", total));
    }

    private void setMontants(double loyer, double services, double fournitures,
                             double transport, double salaires, double autre,
                             int countLoyer, int countServices, int countFournitures,
                             int countTransport, int countSalaires, int countAutre) {

        lblMontantLoyer.setText(String.format("%.2f €", loyer));
        lblMontantServices.setText(String.format("%.2f €", services));
        lblMontantFournitures.setText(String.format("%.2f €", fournitures));
        lblMontantTransport.setText(String.format("%.2f €", transport));
        lblMontantSalaires.setText(String.format("%.2f €", salaires));
        lblMontantAutre.setText(String.format("%.2f €", autre));

        lblCountLoyer.setText(countLoyer + " dépense(s) enregistrée(s)");
        lblCountServices.setText(countServices + " dépense(s) enregistrée(s)");
        lblCountFournitures.setText(countFournitures + " dépense(s) enregistrée(s)");
        lblCountTransport.setText(countTransport + " dépense(s) enregistrée(s)");
        lblCountSalaires.setText(countSalaires + " dépense(s) enregistrée(s)");
        lblCountAutre.setText(countAutre + " dépense(s) enregistrée(s)");
    }

    @FXML
    private void onVoirDetailsSalaires() {
        // Passer le centre actuel à la page de détails
        DetailSalaireController.setCentre(centreActuel);
        MainController.loadPage("detail_salaire", "Détails des Salaires");
    }

    public String getCentreActuel() {
        return centreActuel;
    }
}