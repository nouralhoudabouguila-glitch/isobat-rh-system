package com.rh.models;

import java.time.LocalDate;
import java.util.Objects;
import com.rh.models.Departement;

public class Employe {
    private int id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String situationFamiliale;
    private String cin;
    private String telephone;
    private double salaireMensuelNet;
    private String profil;
    private String typeContrat;
    private String statut;
    private LocalDate dateEmbauche;
    private String nRIB;
    private String nCNSS;
    private Departement departement;

    public Employe() {
    }

    public Employe(int id, String nom, String prenom, LocalDate dateNaissance, String situationFamiliale,
                   String cin, String telephone, double salaireMensuelNet, String profil, String typeContrat,
                   String statut, LocalDate dateEmbauche, String nRIB, String nCNSS, Departement departement) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.situationFamiliale = situationFamiliale;
        this.cin = cin;
        this.telephone = telephone;
        this.salaireMensuelNet = salaireMensuelNet;
        this.profil = profil;
        this.typeContrat = typeContrat;
        this.statut = statut;
        this.dateEmbauche = dateEmbauche;
        this.nRIB = nRIB;
        this.nCNSS = nCNSS;
        this.departement = departement;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getSituationFamiliale() {
        return situationFamiliale;
    }

    public void setSituationFamiliale(String situationFamiliale) {
        this.situationFamiliale = situationFamiliale;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getSalaireMensuelNet() {
        return salaireMensuelNet;
    }

    public void setSalaireMensuelNet(double salaireMensuelNet) {
        this.salaireMensuelNet = salaireMensuelNet;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getnRIB() {
        return nRIB;
    }

    public void setnRIB(String nRIB) {
        this.nRIB = nRIB;
    }

    public String getnCNSS() {
        return nCNSS;
    }

    public void setnCNSS(String nCNSS) {
        this.nCNSS = nCNSS;
    }

    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    @Override
    public String toString() {
        return "Employe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", situationFamiliale='" + situationFamiliale + '\'' +
                ", cin='" + cin + '\'' +
                ", telephone='" + telephone + '\'' +
                ", salaireMensuelNet=" + salaireMensuelNet +
                ", profil='" + profil + '\'' +
                ", typeContrat='" + typeContrat + '\'' +
                ", statut='" + statut + '\'' +
                ", dateEmbauche=" + dateEmbauche +
                ", nRIB='" + nRIB + '\'' +
                ", nCNSS='" + nCNSS + '\'' +
                ", departement=" + departement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employe employe = (Employe) o;
        return id == employe.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


