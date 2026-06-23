package com.rh.models;

public class SoldeConge {

    private int id;
    private Employe employe;
    private int annee;
    private String typeConge;
    private double soldeInitial;
    private double soldeConsomme;
    private double soldeRestant;

    public SoldeConge() {}

    public SoldeConge(Employe employe, int annee, String typeConge, double soldeInitial) {
        this.employe = employe;
        this.annee = annee;
        this.typeConge = typeConge;
        this.soldeInitial = soldeInitial;
        this.soldeConsomme = 0;
        this.soldeRestant = soldeInitial;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public String getTypeConge() { return typeConge; }
    public void setTypeConge(String typeConge) { this.typeConge = typeConge; }

    public double getSoldeInitial() { return soldeInitial; }
    public void setSoldeInitial(double soldeInitial) { this.soldeInitial = soldeInitial; }

    public double getSoldeConsomme() { return soldeConsomme; }
    public void setSoldeConsomme(double soldeConsomme) { this.soldeConsomme = soldeConsomme; }

    public double getSoldeRestant() { return soldeRestant; }
    public void setSoldeRestant(double soldeRestant) { this.soldeRestant = soldeRestant; }

    /** Recalcule le solde restant automatiquement */
    public void recalculer() {
        this.soldeRestant = this.soldeInitial - this.soldeConsomme;
    }

    /** Consomme des jours et recalcule */
    public void consommer(double jours) {
        this.soldeConsomme += jours;
        recalculer();
    }

    /** Restitue des jours (annulation) et recalcule */
    public void restituer(double jours) {
        this.soldeConsomme = Math.max(0, this.soldeConsomme - jours);
        recalculer();
    }
}