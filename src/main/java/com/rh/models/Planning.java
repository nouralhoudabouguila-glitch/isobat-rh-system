package com.rh.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Planning {

    public enum TypePlanning {
        REUNION("📅 Réunion"),
        RDV("🤝 Rendez-vous"),
        MISSION("📋 Mission"),
        FORMATION("📚 Formation"),
        AUTRE("📌 Autre");

        private final String label;
        TypePlanning(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum PrioritePlanning {
        BASSE("🟢 Basse"),
        MOYENNE("🟡 Moyenne"),
        HAUTE("🟠 Haute"),
        URGENTE("🔴 Urgente");

        private final String label;
        PrioritePlanning(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum StatutPlanning {
        PLANIFIE("📅 Planifié"),
        EN_COURS("⏳ En cours"),
        TERMINE("✅ Terminé"),
        ANNULE("❌ Annulé"),
        REPORTE("🔄 Reporté");

        private final String label;
        StatutPlanning(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    private int id;
    private String titre;
    private String description;
    private LocalDate dateEvenement;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String lieu;
    private TypePlanning type;
    private PrioritePlanning priorite;
    private StatutPlanning statut;
    private int rappelMinutesAvant;
    private boolean rappelEnvoye;
    private String commentaire;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public Planning() {
        this.statut = StatutPlanning.PLANIFIE;
        this.priorite = PrioritePlanning.MOYENNE;
        this.rappelMinutesAvant = 15;
        this.rappelEnvoye = false;
        this.dateCreation = LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public TypePlanning getType() { return type; }
    public void setType(TypePlanning type) { this.type = type; }

    public PrioritePlanning getPriorite() { return priorite; }
    public void setPriorite(PrioritePlanning priorite) { this.priorite = priorite; }

    public StatutPlanning getStatut() { return statut; }
    public void setStatut(StatutPlanning statut) { this.statut = statut; }

    public int getRappelMinutesAvant() { return rappelMinutesAvant; }
    public void setRappelMinutesAvant(int rappelMinutesAvant) { this.rappelMinutesAvant = rappelMinutesAvant; }

    public boolean isRappelEnvoye() { return rappelEnvoye; }
    public void setRappelEnvoye(boolean rappelEnvoye) { this.rappelEnvoye = rappelEnvoye; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    public String getDateStr() {
        return dateEvenement != null ? dateEvenement.format(FMT_DATE) : "—";
    }

    public String getHeureDebutStr() {
        return heureDebut != null ? heureDebut.format(FMT_HEURE) : "—";
    }

    public String getHeureFinStr() {
        return heureFin != null ? heureFin.format(FMT_HEURE) : "—";
    }

    public String getHeureStr() {
        if (heureDebut != null && heureFin != null) {
            return heureDebut.format(FMT_HEURE) + " - " + heureFin.format(FMT_HEURE);
        } else if (heureDebut != null) {
            return heureDebut.format(FMT_HEURE);
        }
        return "—";
    }

    public boolean isToday() {
        return dateEvenement != null && dateEvenement.equals(LocalDate.now());
    }

    public boolean isSoon() {
        if (dateEvenement == null) return false;
        LocalDate now = LocalDate.now();
        return dateEvenement.isAfter(now) && dateEvenement.isBefore(now.plusDays(3));
    }

    public boolean isPast() {
        if (dateEvenement == null) return false;
        return dateEvenement.isBefore(LocalDate.now());
    }

    public String getTypeLabel() {
        return type != null ? type.getLabel() : "—";
    }

    public String getPrioriteLabel() {
        return priorite != null ? priorite.getLabel() : "—";
    }

    public String getStatutLabel() {
        return statut != null ? statut.getLabel() : "—";
    }
}