package com.rh.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Retenue {

    // ── Attributs ──

    private int idRetenue;
    private int idEmploye;
    private Employe employe;
    private double montant;
    private String motif;
    private LocalDate dateRetenue;
    private String commentaire;
    private LocalDateTime dateCreation;

    // ── Constructeurs ──

    public Retenue() {}

    public Retenue(int idRetenue, int idEmploye, double montant, String motif,
                   LocalDate dateRetenue, String commentaire, LocalDateTime dateCreation) {
        this.idRetenue = idRetenue;
        this.idEmploye = idEmploye;
        this.montant = montant;
        this.motif = motif;
        this.dateRetenue = dateRetenue;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
    }

    public Retenue(int idEmploye, double montant, String motif, LocalDate dateRetenue, String commentaire) {
        this.idEmploye = idEmploye;
        this.montant = montant;
        this.motif = motif;
        this.dateRetenue = dateRetenue;
        this.commentaire = commentaire;
        this.dateCreation = LocalDateTime.now();
    }

    // ── Getters et Setters ──

    public int getIdRetenue() {
        return idRetenue;
    }

    public void setIdRetenue(int idRetenue) {
        this.idRetenue = idRetenue;
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

    public LocalDate getDateRetenue() {
        return dateRetenue;
    }

    public void setDateRetenue(LocalDate dateRetenue) {
        this.dateRetenue = dateRetenue;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
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

    public String getDateRetenueFormatee() {
        if (dateRetenue != null) {
            return dateRetenue.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    public String getDateCreationFormatee() {
        if (dateCreation != null) {
            return dateCreation.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
        return "";
    }

    @Override
    public String toString() {
        return "Retenue{" +
                "idRetenue=" + idRetenue +
                ", idEmploye=" + idEmploye +
                ", montant=" + montant +
                ", motif='" + motif + '\'' +
                ", dateRetenue=" + dateRetenue +
                ", commentaire='" + commentaire + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Retenue retenue = (Retenue) o;
        return idRetenue == retenue.idRetenue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRetenue);
    }
}