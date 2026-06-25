package com.rh.models;

import java.time.LocalDate;
import java.util.Objects;

public class ParticipationFormation {

    private int idParticipation;
    private Employe employe;          // Référence à l'employé
    private int idEmploye;            // ID de l'employé (pour la base de données)
    private LocalDate dateFormation;
    private String formation;
    private StatutFormation statut;

    // ── Enum pour les statuts de formation ──
    public enum StatutFormation {
        PRESENTE("Présente"),
        ABSENTE("Absente"),
        FPF("FPF");  // Formation Professionnelle Financée

        private final String label;

        StatutFormation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static StatutFormation fromString(String text) {
            for (StatutFormation statut : StatutFormation.values()) {
                if (statut.label.equalsIgnoreCase(text)) {
                    return statut;
                }
            }
            return PRESENTE; // Valeur par défaut
        }
    }

    // ── Constructeurs ──

    public ParticipationFormation() {}

    public ParticipationFormation(int idParticipation, Employe employe, LocalDate dateFormation,
                                  String formation, StatutFormation statut) {
        this.idParticipation = idParticipation;
        this.employe = employe;
        this.idEmploye = (employe != null) ? employe.getId() : 0;
        this.dateFormation = dateFormation;
        this.formation = formation;
        this.statut = statut;
    }

    public ParticipationFormation(int idParticipation, int idEmploye, LocalDate dateFormation,
                                  String formation, StatutFormation statut) {
        this.idParticipation = idParticipation;
        this.idEmploye = idEmploye;
        this.dateFormation = dateFormation;
        this.formation = formation;
        this.statut = statut;
    }

    public ParticipationFormation(Employe employe, LocalDate dateFormation,
                                  String formation, StatutFormation statut) {
        this.employe = employe;
        this.idEmploye = (employe != null) ? employe.getId() : 0;
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

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
        if (employe != null) {
            this.idEmploye = employe.getId();
        }
    }

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
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

    public StatutFormation getStatut() {
        return statut;
    }

    public void setStatut(StatutFormation statut) {
        this.statut = statut;
    }

    public void setStatut(String statut) {
        this.statut = StatutFormation.fromString(statut);
    }

    // ── Méthodes utilitaires ──

    public String getNomCompletEmploye() {
        if (employe != null) {
            return employe.getNom() + " " + employe.getPrenom();
        }
        return "Employé inconnu";
    }

    public String getStatutLabel() {
        return (statut != null) ? statut.toString() : "";
    }

    public String getDepartementEmploye() {
        if (employe != null && employe.getDepartement() != null) {
            return employe.getDepartement().toString();
        }
        return "";
    }

    @Override
    public String toString() {
        return "ParticipationFormation{" +
                "idParticipation=" + idParticipation +
                ", idEmploye=" + idEmploye +
                ", dateFormation=" + dateFormation +
                ", formation='" + formation + '\'' +
                ", statut=" + statut +
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