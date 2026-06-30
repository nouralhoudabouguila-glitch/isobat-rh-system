package com.rh.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TicketEmploye {

    private int idTicket;
    private int idEmploye;
    private Employe employe;
    private String mois;
    private int annee;
    private int nbJours;
    private double montantParTicket;
    private double total;
    private LocalDateTime dateCreation;

    // ── Constructeurs ──

    public TicketEmploye() {}

    public TicketEmploye(int idTicket, int idEmploye, String mois, int annee,
                         int nbJours, double montantParTicket, double total,
                         LocalDateTime dateCreation) {
        this.idTicket = idTicket;
        this.idEmploye = idEmploye;
        this.mois = mois;
        this.annee = annee;
        this.nbJours = nbJours;
        this.montantParTicket = montantParTicket;
        this.total = total;
        this.dateCreation = dateCreation;
    }

    public TicketEmploye(int idEmploye, String mois, int annee, int nbJours, double montantParTicket) {
        this.idEmploye = idEmploye;
        this.mois = mois;
        this.annee = annee;
        this.nbJours = nbJours;
        this.montantParTicket = montantParTicket;
        this.total = nbJours * montantParTicket;
        this.dateCreation = LocalDateTime.now();
    }

    // ── Getters et Setters ──

    public int getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(int idTicket) {
        this.idTicket = idTicket;
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

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public int getNbJours() {
        return nbJours;
    }

    public void setNbJours(int nbJours) {
        this.nbJours = nbJours;
        this.total = nbJours * this.montantParTicket;
    }

    public double getMontantParTicket() {
        return montantParTicket;
    }

    public void setMontantParTicket(double montantParTicket) {
        this.montantParTicket = montantParTicket;
        this.total = this.nbJours * montantParTicket;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // ── Méthodes utilitaires ──

    public String getNomCompletEmploye() {
        if (employe != null) {
            return employe.getNom() + " " + employe.getPrenom();
        }
        return "Employé #" + idEmploye;
    }

    public String getDateCreationFormatee() {
        if (dateCreation != null) {
            return dateCreation.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
        return "";
    }

    public String getMoisAnnee() {
        return mois + " " + annee;
    }

    @Override
    public String toString() {
        return "TicketEmploye{" +
                "idTicket=" + idTicket +
                ", idEmploye=" + idEmploye +
                ", mois='" + mois + '\'' +
                ", annee=" + annee +
                ", nbJours=" + nbJours +
                ", montantParTicket=" + montantParTicket +
                ", total=" + total +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEmploye that = (TicketEmploye) o;
        return idTicket == that.idTicket;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTicket);
    }
}