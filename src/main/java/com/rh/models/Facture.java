package com.rh.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Facture {

    // ── Enum pour le statut ──
    public enum StatutFacture {
        EN_ATTENTE("En attente"),
        PAYEE("Payée"),
        ANNULEE("Annulée");

        private final String label;

        StatutFacture(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static StatutFacture fromString(String text) {
            for (StatutFacture statut : StatutFacture.values()) {
                if (statut.label.equalsIgnoreCase(text)) {
                    return statut;
                }
            }
            return EN_ATTENTE;
        }
    }

    // ── Enum pour le mode de paiement ──
    public enum ModePaiement {
        ESPECES("Espèces"),
        CHEQUE("Chèque"),
        VIREMENT("Virement"),
        CARTE_BANCAIRE("Carte bancaire");

        private final String label;

        ModePaiement(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static ModePaiement fromString(String text) {
            for (ModePaiement mode : ModePaiement.values()) {
                if (mode.label.equalsIgnoreCase(text)) {
                    return mode;
                }
            }
            return VIREMENT;
        }
    }

    // ── Attributs ──

    private int idFacture;
    private String numeroFacture;
    private int idFournisseur;
    private Fournisseur fournisseur;
    private double montantHt;
    private double montantTva;
    private double montantTtc;
    private LocalDate dateFacture;
    private LocalDate dateEcheance;
    private StatutFacture statut;
    private ModePaiement modePaiement;
    private String fichierPdf;
    private String commentaire;

    // ── Constructeurs ──

    public Facture() {}

    public Facture(int idFacture, String numeroFacture, int idFournisseur, double montantHt,
                   double montantTva, double montantTtc, LocalDate dateFacture,
                   LocalDate dateEcheance, StatutFacture statut, ModePaiement modePaiement,
                   String fichierPdf, String commentaire) {
        this.idFacture = idFacture;
        this.numeroFacture = numeroFacture;
        this.idFournisseur = idFournisseur;
        this.montantHt = montantHt;
        this.montantTva = montantTva;
        this.montantTtc = montantTtc;
        this.dateFacture = dateFacture;
        this.dateEcheance = dateEcheance;
        this.statut = statut;
        this.modePaiement = modePaiement;
        this.fichierPdf = fichierPdf;
        this.commentaire = commentaire;
    }

    public Facture(String numeroFacture, int idFournisseur, double montantHt,
                   double montantTva, double montantTtc, LocalDate dateFacture,
                   LocalDate dateEcheance, StatutFacture statut, ModePaiement modePaiement,
                   String commentaire) {
        this.numeroFacture = numeroFacture;
        this.idFournisseur = idFournisseur;
        this.montantHt = montantHt;
        this.montantTva = montantTva;
        this.montantTtc = montantTtc;
        this.dateFacture = dateFacture;
        this.dateEcheance = dateEcheance;
        this.statut = statut;
        this.modePaiement = modePaiement;
        this.commentaire = commentaire;
    }

    // ── Getters et Setters ──

    public int getIdFacture() {
        return idFacture;
    }

    public void setIdFacture(int idFacture) {
        this.idFacture = idFacture;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public int getIdFournisseur() {
        return idFournisseur;
    }

    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
        if (fournisseur != null) {
            this.idFournisseur = fournisseur.getIdFournisseur();
        }
    }

    public double getMontantHt() {
        return montantHt;
    }

    public void setMontantHt(double montantHt) {
        this.montantHt = montantHt;
    }

    public double getMontantTva() {
        return montantTva;
    }

    public void setMontantTva(double montantTva) {
        this.montantTva = montantTva;
    }

    public double getMontantTtc() {
        return montantTtc;
    }

    public void setMontantTtc(double montantTtc) {
        this.montantTtc = montantTtc;
    }

    public LocalDate getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public StatutFacture getStatut() {
        return statut;
    }

    public void setStatut(StatutFacture statut) {
        this.statut = statut;
    }

    public void setStatut(String statut) {
        this.statut = StatutFacture.fromString(statut);
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = ModePaiement.fromString(modePaiement);
    }

    public String getFichierPdf() {
        return fichierPdf;
    }

    public void setFichierPdf(String fichierPdf) {
        this.fichierPdf = fichierPdf;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    // ── Méthodes utilitaires ──

    public String getNomFournisseur() {
        if (fournisseur != null) {
            return fournisseur.getNom();
        }
        return "";
    }

    public String getDateFactureFormatee() {
        if (dateFacture != null) {
            return dateFacture.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    public String getDateEcheanceFormatee() {
        if (dateEcheance != null) {
            return dateEcheance.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    public String getStatutLabel() {
        return statut != null ? statut.toString() : "";
    }

    public String getModePaiementLabel() {
        return modePaiement != null ? modePaiement.toString() : "";
    }

    @Override
    public String toString() {
        return "Facture{" +
                "idFacture=" + idFacture +
                ", numeroFacture='" + numeroFacture + '\'' +
                ", idFournisseur=" + idFournisseur +
                ", montantHt=" + montantHt +
                ", montantTva=" + montantTva +
                ", montantTtc=" + montantTtc +
                ", dateFacture=" + dateFacture +
                ", dateEcheance=" + dateEcheance +
                ", statut=" + statut +
                ", modePaiement=" + modePaiement +
                ", fichierPdf='" + fichierPdf + '\'' +
                ", commentaire='" + commentaire + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Facture facture = (Facture) o;
        return idFacture == facture.idFacture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFacture);
    }
}