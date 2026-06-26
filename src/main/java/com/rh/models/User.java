package com.rh.models;

import java.time.LocalDateTime;

public class User {

    public enum Role {
        ADMIN("Administrateur"),
        RESPONSABLE_RH("Responsable RH");

        private final String label;
        Role(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private Role role;
    private boolean actif;
    private String telephone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() { this.actif = true; }

    public User(String nom, String prenom, String email,
                String motDePasse, Role role, String telephone) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.telephone = telephone;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    public String getNomComplet() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }

    public String getInitiales() {
        String i = "";
        if (nom != null && !nom.isEmpty()) i += nom.charAt(0);
        if (prenom != null && !prenom.isEmpty()) i += prenom.charAt(0);
        return i.toUpperCase();
    }
}