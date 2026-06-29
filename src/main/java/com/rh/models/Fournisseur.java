package com.rh.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Fournisseur {

    private int idFournisseur;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String pays;
    private String matriculeFiscal;
    private LocalDateTime dateCreation;

    // ── Constructeurs ──

    public Fournisseur() {
        this.dateCreation = LocalDateTime.now(); // Date par défaut
    }

    public Fournisseur(int idFournisseur, String nom, String email, String telephone,
                       String adresse, String ville, String pays, String matriculeFiscal,
                       LocalDateTime dateCreation) {
        this.idFournisseur = idFournisseur;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ville = ville;
        this.pays = pays;
        this.matriculeFiscal = matriculeFiscal;
        this.dateCreation = dateCreation != null ? dateCreation : LocalDateTime.now();
    }

    public Fournisseur(String nom, String email, String telephone, String adresse,
                       String ville, String pays, String matriculeFiscal) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ville = ville;
        this.pays = pays;
        this.matriculeFiscal = matriculeFiscal;
        this.dateCreation = LocalDateTime.now();
    }

    // ── Getters et Setters ──

    public int getIdFournisseur() {
        return idFournisseur;
    }

    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // ── Méthodes utilitaires ──

    public String getNomComplet() {
        return nom;
    }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "idFournisseur=" + idFournisseur +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", pays='" + pays + '\'' +
                ", matriculeFiscal='" + matriculeFiscal + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fournisseur that = (Fournisseur) o;
        return idFournisseur == that.idFournisseur;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFournisseur);
    }
}