package com.rh.contollers;

import com.rh.models.Departement;
import com.rh.services.TauxChangeService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DepenseController {

    @FXML
    private ComboBox<String> comboCentre;

    @FXML
    private ComboBox<String> comboDevise;

    @FXML
    private Label lblTotalGeneral;

    @FXML
    private Label lblTauxDate;

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

    // Données en EUR (devise de base)
    private double montantLoyerEUR = 1250.00;
    private double montantServicesEUR = 890.00;
    private double montantFournituresEUR = 450.00;
    private double montantTransportEUR = 320.00;
    private double montantSalairesEUR = 4500.00;
    private double montantAutreEUR = 150.00;

    private int countLoyer = 3;
    private int countServices = 2;
    private int countFournitures = 4;
    private int countTransport = 2;
    private int countSalaires = 1;
    private int countAutre = 1;

    private String centreActuel = "Centre d'appel B2B";
    private String deviseActuelle = "TND";
    private TauxChangeService tauxService;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);
    private Timeline refreshTimer;

    @FXML
    public void initialize() {
        // Initialiser le service de taux
        tauxService = new TauxChangeService();

        // Configurer le ComboBox des centres
        configurerCentres();

        // Configurer le ComboBox des devises
        configurerDevises();

        // Charger les données initiales
        chargerDonnees(centreActuel);

        // Mettre à jour les montants
        mettreAJourMontants();

        // Démarrer le rafraîchissement automatique des taux (toutes les 5 minutes)
        demarrerRafraichissementTaux();

        System.out.println("✅ DepenseController initialisé - Devise: " + deviseActuelle);
    }

    private void configurerCentres() {
        if (comboCentre != null) {
            comboCentre.setItems(FXCollections.observableArrayList(
                    "Centre d'appel B2B",
                    "Centre d'appel B2C",
                    "Bureau d'étude",
                    "Administration"
            ));
            comboCentre.setValue("Centre d'appel B2B");
            comboCentre.setOnAction(e -> {
                centreActuel = comboCentre.getValue();
                chargerDonnees(centreActuel);
                mettreAJourMontants();
            });
        }
    }

    private void configurerDevises() {
        if (comboDevise != null) {
            comboDevise.setItems(FXCollections.observableArrayList(
                    "TND", "EUR", "USD", "GBP", "CHF", "JPY", "CAD", "DZD", "MAD"
            ));
            comboDevise.setValue("TND");
            comboDevise.setOnAction(e -> {
                deviseActuelle = comboDevise.getValue();
                mettreAJourMontants();
                mettreAJourTauxDate();
            });
        }
    }

    private void demarrerRafraichissementTaux() {
        refreshTimer = new Timeline(new KeyFrame(Duration.minutes(5), e -> {
            tauxService.getTaux(); // Rafraîchir le cache
            Platform.runLater(() -> {
                mettreAJourMontants();
                mettreAJourTauxDate();
                System.out.println("🔄 Taux de change actualisés");
            });
        }));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }

    private void mettreAJourTauxDate() {
        if (lblTauxDate != null) {
            lblTauxDate.setText("Taux mis à jour : " + tauxService.getDerniereMajFormatee());
        }
    }

    private void chargerDonnees(String centre) {
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
    }

    private void setMontants(double loyer, double services, double fournitures,
                             double transport, double salaires, double autre,
                             int countLoyer, int countServices, int countFournitures,
                             int countTransport, int countSalaires, int countAutre) {

        this.montantLoyerEUR = loyer;
        this.montantServicesEUR = services;
        this.montantFournituresEUR = fournitures;
        this.montantTransportEUR = transport;
        this.montantSalairesEUR = salaires;
        this.montantAutreEUR = autre;

        this.countLoyer = countLoyer;
        this.countServices = countServices;
        this.countFournitures = countFournitures;
        this.countTransport = countTransport;
        this.countSalaires = countSalaires;
        this.countAutre = countAutre;

        // Mettre à jour les compteurs
        lblCountLoyer.setText(countLoyer + " dépense(s) enregistrée(s)");
        lblCountServices.setText(countServices + " dépense(s) enregistrée(s)");
        lblCountFournitures.setText(countFournitures + " dépense(s) enregistrée(s)");
        lblCountTransport.setText(countTransport + " dépense(s) enregistrée(s)");
        lblCountSalaires.setText(countSalaires + " dépense(s) enregistrée(s)");
        lblCountAutre.setText(countAutre + " dépense(s) enregistrée(s)");
    }

    /**
     * Met à jour les montants avec la devise sélectionnée
     */
    private void mettreAJourMontants() {
        try {
            // Convertir les montants
            double loyer = convertirMontant(montantLoyerEUR);
            double services = convertirMontant(montantServicesEUR);
            double fournitures = convertirMontant(montantFournituresEUR);
            double transport = convertirMontant(montantTransportEUR);
            double salaires = convertirMontant(montantSalairesEUR);
            double autre = convertirMontant(montantAutreEUR);

            String symbole = tauxService.getSymboleDevise(deviseActuelle);

            // Formater et afficher
            lblMontantLoyer.setText(format.format(loyer) + " " + symbole);
            lblMontantServices.setText(format.format(services) + " " + symbole);
            lblMontantFournitures.setText(format.format(fournitures) + " " + symbole);
            lblMontantTransport.setText(format.format(transport) + " " + symbole);
            lblMontantSalaires.setText(format.format(salaires) + " " + symbole);
            lblMontantAutre.setText(format.format(autre) + " " + symbole);

            // Total général
            double total = loyer + services + fournitures + transport + salaires + autre;
            lblTotalGeneral.setText(format.format(total) + " " + symbole);

            // Mettre à jour la date des taux
            mettreAJourTauxDate();

            System.out.println("💱 Montants convertis en " + deviseActuelle + " - Total: " + format.format(total) + " " + symbole);

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion des montants : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convertit un montant de EUR vers la devise actuelle
     */
    private double convertirMontant(double montantEUR) {
        if (deviseActuelle.equals("EUR")) {
            return montantEUR;
        }

        Map<String, Double> taux = tauxService.getTaux();
        Double tauxDevise = taux.get(deviseActuelle);

        if (tauxDevise != null) {
            return montantEUR * tauxDevise;
        }

        return montantEUR; // Fallback
    }

    /**
     * Actualise manuellement les taux
     */
    @FXML
    private void onActualiserTaux() {
        try {
            tauxService.getTaux(); // Forcer la mise à jour
            mettreAJourMontants();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Taux de change");
            alert.setHeaderText("✅ Taux actualisés");
            alert.setContentText("Les taux de change ont été mis à jour avec succès.");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("❌ Erreur de mise à jour");
            alert.setContentText("Impossible de mettre à jour les taux : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onVoirDetailsSalaires() {
        DetailSalaireController.setCentre(centreActuel);
        MainController.getInstance().loadPage("detail_salaire", "Détails des Salaires");
    }

    public String getCentreActuel() {
        return centreActuel;
    }

    /**
     * Nettoyage des ressources
     */
    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}