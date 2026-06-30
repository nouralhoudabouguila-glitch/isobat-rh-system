package com.rh.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Avance {

    // ── Enum pour le statut ──
    public enum StatutAvance {
        EN_ATTENTE("En attente"),
        VALIDEE("Validée"),
        REFUSEE("Refusée");

        private final String label;

        StatutAvance(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static StatutAvance fromString(String text) {
            for (StatutAvance statut : StatutAvance.values()) {
                if (statut.label.equalsIgnoreCase(text)) {
                    return statut;
                }
            }
            return EN_ATTENTE;
        }
    }

    // ── Attributs ──

    private int idAvance;
    private int idEmploye;
    private Employe employe;
    private double montant;
    private String motif;
    private LocalDate dateDemande;
    private LocalDate dateValidation;
    private StatutAvance statut;
    private String commentaire;

    // ── Constructeurs ──

    public Avance() {}

    public Avance(int idAvance, int idEmploye, double montant, String motif,
                  LocalDate dateDemande, LocalDate dateValidation,
                  StatutAvance statut, String commentaire) {
        this.idAvance = idAvance;
        this.idEmploye = idEmploye;
        this.montant = montant;
        this.motif = motif;
        this.dateDemande = dateDemande;
        this.dateValidation = dateValidation;
        this.statut = statut;
        this.commentaire = commentaire;
    }

    public Avance(int idEmploye, double montant, String motif,
                  LocalDate dateDemande, StatutAvance statut, String commentaire) {
        this.idEmploye = idEmploye;
        this.montant = montant;
        this.motif = motif;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.commentaire = commentaire;
    }

    public Avance(int idEmploye, double montant, String motif, String commentaire) {
        this.idEmploye = idEmploye;
        this.montant = montant;
        this.motif = motif;
        this.dateDemande = LocalDate.now();
        this.statut = StatutAvance.EN_ATTENTE;
        this.commentaire = commentaire;
    }

    // ── Getters et Setters ──

    public int getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(int idAvance) {
        this.idAvance = idAvance;
    }

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
        if (employe != null) {
            this.idEmploye = employe.getId();
        }
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDate dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public StatutAvance getStatut() {
        return statut;
    }

    public void setStatut(StatutAvance statut) {
        this.statut = statut;
    }

    public void setStatut(String statut) {
        this.statut = StatutAvance.fromString(statut);
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    // ── Méthodes utilitaires ──

    public String getNomEmploye() {
        if (employe != null) {
            return employe.getNom() + " " + employe.getPrenom();
        }
        return "Employé #" + idEmploye;
    }

    public String getNomCompletEmploye() {
        if (employe != null) {
            return employe.getNom() + " " + employe.getPrenom();
        }
        return "";
    }

    public String getDateDemandeFormatee() {
        if (dateDemande != null) {
            return dateDemande.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    public String getDateValidationFormatee() {
        if (dateValidation != null) {
            return dateValidation.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    public String getStatutLabel() {
        return statut != null ? statut.toString() : "";
    }

    @Override
    public String toString() {
        return "Avance{" +
                "idAvance=" + idAvance +
                ", idEmploye=" + idEmploye +
                ", montant=" + montant +
                ", motif='" + motif + '\'' +
                ", dateDemande=" + dateDemande +
                ", dateValidation=" + dateValidation +
                ", statut=" + statut +
                ", commentaire='" + commentaire + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avance avance = (Avance) o;
        return idAvance == avance.idAvance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAvance);
    }
}