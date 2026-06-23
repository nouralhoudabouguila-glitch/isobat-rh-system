package com.rh.models;

import java.time.LocalDate;

public class DemandeConge {

    public enum TypeConge {
        ANNUEL("Congé annuel"),
        MALADIE("Congé maladie"),
        EXCEPTIONNEL("Congé exceptionnel"),
        SANS_SOLDE("Sans solde"),
        RECUPERATION("Récupération");

        private final String label;
        TypeConge(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    public enum Statut {
        EN_ATTENTE("En attente"),
        APPROUVE("Approuvé"),
        REFUSE("Refusé"),
        ANNULE("Annulé");

        private final String label;
        Statut(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    private int id;
    private Employe employe;
    private TypeConge typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreJours;
    private Statut statut;
    private String remplacant;
    private String commentaire;
    private LocalDate dateCreation;

    public DemandeConge() {
        this.statut = Statut.EN_ATTENTE;
        this.dateCreation = LocalDate.now();
    }

    public DemandeConge(Employe employe, TypeConge typeConge,
                        LocalDate dateDebut, LocalDate dateFin,
                        int nombreJours, String remplacant, String commentaire) {
        this();
        this.employe = employe;
        this.typeConge = typeConge;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombreJours = nombreJours;
        this.remplacant = remplacant;
        this.commentaire = commentaire;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public TypeConge getTypeConge() { return typeConge; }
    public void setTypeConge(TypeConge typeConge) { this.typeConge = typeConge; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public int getNombreJours() { return nombreJours; }
    public void setNombreJours(int nombreJours) { this.nombreJours = nombreJours; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getRemplacant() { return remplacant; }
    public void setRemplacant(String remplacant) { this.remplacant = remplacant; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    // Utilitaire : nom complet employé pour affichage
    public String getNomEmploye() {
        if (employe == null) return "—";
        return employe.getNom() + " " + employe.getPrenom();
    }

    public String getPosteEmploye() {
        if (employe == null) return "—";
        return employe.getProfil() != null ? employe.getProfil() : "—";
    }
}