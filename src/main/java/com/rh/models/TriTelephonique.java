package com.rh.models;

public class TriTelephonique {
    private int idCand;
    private String nom;
    private String prenom;
    private String telephone;
    private String experience;
    private String poste;
    private String commentaire;
    private String statut;

    // ── Constructeurs ──

    public TriTelephonique() {}

    public TriTelephonique(int idCand, String nom, String prenom, String telephone,
                           String experience, String poste, String commentaire, String statut) {
        this.idCand = idCand;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.experience = experience;
        this.poste = poste;
        this.commentaire = commentaire;
        this.statut = statut;
    }

    // ── Getters et Setters ──

    public int getIdCand() {
        return idCand;
    }

    public void setIdCand(int idCand) {
        this.idCand = idCand;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // ── Méthode pour le nom complet ──

    public String getNomComplet() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }

    @Override
    public String toString() {
        return "TriTelephonique{" +
                "idCand=" + idCand +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", experience='" + experience + '\'' +
                ", poste='" + poste + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}