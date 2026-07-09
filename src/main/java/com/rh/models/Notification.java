package com.rh.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int idNotification;
    private String type; // "FACTURE_ECHEANCE", "AVANCE_ATTENTE", "BULLETIN_PAIE", "VIREMENT_VALIDE"
    private String titre;
    private String message;
    private LocalDate dateNotification;
    private LocalDate dateEcheance;
    private boolean estLue;
    private String reference;
    private int idReference;
    private LocalDateTime dateCreation;
    private String priorite; // "info", "warning", "urgent", "danger", "success"
    private String actionLabel; // "Voir la facture", "Traiter", "Consulter", "Voir le détail"
    private String actionPage; // Page à charger

    public Notification() {}

    public Notification(String type, String titre, String message, LocalDate dateEcheance, String reference, int idReference) {
        this.type = type;
        this.titre = titre;
        this.message = message;
        this.dateEcheance = dateEcheance;
        this.reference = reference;
        this.idReference = idReference;
        this.dateNotification = LocalDate.now();
        this.estLue = false;
        this.dateCreation = LocalDateTime.now();
        this.priorite = "info";
        this.actionLabel = "Voir →";
        this.actionPage = "";
    }

    // Getters et Setters
    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDate getDateNotification() { return dateNotification; }
    public void setDateNotification(LocalDate dateNotification) { this.dateNotification = dateNotification; }
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }
    public boolean isEstLue() { return estLue; }
    public void setEstLue(boolean estLue) { this.estLue = estLue; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public int getIdReference() { return idReference; }
    public void setIdReference(int idReference) { this.idReference = idReference; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }
    public String getActionPage() { return actionPage; }
    public void setActionPage(String actionPage) { this.actionPage = actionPage; }

    public String getDateEcheanceFormatee() {
        return dateEcheance != null ? dateEcheance.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
    }

    public String getDateNotificationFormatee() {
        return dateNotification != null ? dateNotification.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
    }

    // Couleur selon la priorité
    public String getCouleurPriorite() {
        switch (priorite) {
            case "danger": return "#CC0000";
            case "urgent": return "#CC3300";
            case "warning": return "#CC6600";
            case "success": return "#4F6815";
            default: return "#336699";
        }
    }

    public String getFondPriorite() {
        switch (priorite) {
            case "danger": return "#FFE8E8";
            case "urgent": return "#FFF0E0";
            case "warning": return "#FFF8E1";
            case "success": return "#E8F0D8";
            default: return "#F0F4F8";
        }
    }

    public String getBadgePriorite() {
        switch (priorite) {
            case "danger": return "Urgence";
            case "urgent": return "Urgent";
            case "warning": return "Avertissement";
            case "success": return "Succès";
            default: return "Information";
        }
    }

    @Override
    public String toString() {
        return "Notification{" +
                "idNotification=" + idNotification +
                ", type='" + type + '\'' +
                ", titre='" + titre + '\'' +
                ", message='" + message + '\'' +
                ", estLue=" + estLue +
                '}';
    }


}