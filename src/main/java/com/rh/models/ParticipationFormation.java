package com.rh.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ParticipationFormation {

    private int idParticipation;
    private int idEntretien;
    private String nom;
    private String prenom;
    private String poste;
    private LocalDate dateFormation;
    private String formation;
    private String statut;

    // ── Constructeurs ──

    public ParticipationFormation() {}

    public ParticipationFormation(int idParticipation, int idEntretien, String nom, String prenom,
                                  String poste, LocalDate dateFormation, String formation, String statut) {
        this.idParticipation = idParticipation;
        this.idEntretien = idEntretien;
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.dateFormation = dateFormation;
        this.formation = formation;
        this.statut = statut;
    }

    public ParticipationFormation(int idEntretien, String nom, String prenom, String poste,
                                  LocalDate dateFormation, String formation, String statut) {
        this.idEntretien = idEntretien;
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.dateFormation = dateFormation;
        this.formation = formation;
        this.statut = statut;
    }

    // ── Getters et Setters ──

    public int getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
    }

    public int getIdEntretien() {
        return idEntretien;
    }

    public void setIdEntretien(int idEntretien) {
        this.idEntretien = idEntretien;
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

    public LocalDate getDateFormation() {
        return dateFormation;
    }

    public void setDateFormation(LocalDate dateFormation) {
        this.dateFormation = dateFormation;
    }

    public String getFormation() {
        return formation;
    }

    public void setFormation(String formation) {
        this.formation = formation;
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

    public String getDateFormationFormatee() {
        if (dateFormation != null) {
            return dateFormation.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    @Override
    public String toString() {
        return "ParticipationFormation{" +
                "idParticipation=" + idParticipation +
                ", idEntretien=" + idEntretien +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", poste='" + poste + '\'' +
                ", dateFormation=" + dateFormation +
                ", formation='" + formation + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipationFormation that = (ParticipationFormation) o;
        return idParticipation == that.idParticipation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParticipation);
    }
}