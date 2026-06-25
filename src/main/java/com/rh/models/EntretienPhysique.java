package com.rh.models;

import java.util.Objects;

public class EntretienPhysique {

    private int idEntretien;
    private int idCandidat;          // Référence à TriTelephonique.idCand
    private String nom;
    private String prenom;
    private String poste;
    private String dateRdv;
    private String statut;

    // ── Constructeurs ──

    public EntretienPhysique() {}

    public EntretienPhysique(int idEntretien, int idCandidat, String nom, String prenom,
                             String poste, String dateRdv, String statut) {
        this.idEntretien = idEntretien;
        this.idCandidat = idCandidat;
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.dateRdv = dateRdv;
        this.statut = statut;
    }

    public EntretienPhysique(int idCandidat, String nom, String prenom, String poste,
                             String dateRdv, String statut) {
        this.idCandidat = idCandidat;
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.dateRdv = dateRdv;
        this.statut = statut;
    }

    // ── Getters et Setters ──

    public int getIdEntretien() {
        return idEntretien;
    }

    public void setIdEntretien(int idEntretien) {
        this.idEntretien = idEntretien;
    }

    public int getIdCandidat() {
        return idCandidat;
    }

    public void setIdCandidat(int idCandidat) {
        this.idCandidat = idCandidat;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getDateRdv() {
        return dateRdv;
    }

    public void setDateRdv(String dateRdv) {
        this.dateRdv = dateRdv;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // ── Méthodes utilitaires ──

    public String getNomComplet() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }

    @Override
    public String toString() {
        return "EntretienPhysique{" +
                "idEntretien=" + idEntretien +
                ", idCandidat=" + idCandidat +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", poste='" + poste + '\'' +
                ", dateRdv='" + dateRdv + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntretienPhysique that = (EntretienPhysique) o;
        return idEntretien == that.idEntretien;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntretien);
    }
}