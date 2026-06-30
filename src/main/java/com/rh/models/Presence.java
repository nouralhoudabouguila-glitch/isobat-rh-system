package com.rh.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class Presence {

    public enum Statut {
        PRESENT("Présent"),
        ABSENT_JUSTIFIE("Absent justifié"),
        ABSENT_NON_JUSTIFIE("Absent N.J."),
        RETARD("Retard"),
        INCOMPLET("Incomplet"),
        TELETRAVAIL("Télétravail"),
        MISSION("Mission");

        private final String label;
        Statut(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum Source {
        IMPORT("Import Excel"),
        MANUEL("Saisie manuelle");

        private final String label;
        Source(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    // Heure limite pour considérer un retard (08h30)
    private static final LocalTime HEURE_LIMITE = LocalTime.of(8, 30);

    private int id;
    private Employe employe;
    private LocalDate datePresence;
    private LocalTime heureArrivee;
    private LocalTime debutPause;
    private LocalTime finPause;
    private LocalTime heureDepart;
    private double heuresTravaillees;
    private Statut statut;
    private Source source;
    private String commentaire;
    private Departement departement;

    public Presence() {
        this.source = Source.IMPORT;
        this.statut = Statut.PRESENT;
    }

    // ── Logique métier ────────────────────────────────────────────────────────

    /**
     * Calcule les heures travaillées :
     * (débutPause - heureArrivee) + (heureDepart - finPause)
     */
    public double calculerHeuresTravaillees() {
        if (heureArrivee == null || heureDepart == null) return 0;
        double total = 0;
        if (debutPause != null) {
            // Matin : arrivée → début pause
            Duration matin = Duration.between(heureArrivee, debutPause);
            total += matin.toMinutes();
        }
        if (finPause != null && heureDepart != null) {
            // Après-midi : fin pause → départ
            Duration aprem = Duration.between(finPause, heureDepart);
            total += aprem.toMinutes();
        }
        if (debutPause == null && finPause == null && heureDepart != null) {
            // Pas de pause enregistrée → total direct
            Duration direct = Duration.between(heureArrivee, heureDepart);
            total = direct.toMinutes();
        }
        this.heuresTravaillees = Math.round((total / 60.0) * 100.0) / 100.0;
        return this.heuresTravaillees;
    }

    /**
     * Détecte automatiquement le statut selon les données de pointage.
     */
    public Statut detecterStatut() {
        if (heureArrivee == null && heureDepart == null) {
            return Statut.ABSENT_NON_JUSTIFIE;
        }
        if (heureArrivee == null || heureDepart == null) {
            return Statut.INCOMPLET;
        }
        if (heureArrivee.isAfter(HEURE_LIMITE)) {
            return Statut.RETARD;
        }
        return Statut.PRESENT;
    }

    /**
     * Retourne "08:19" formatté ou "—" si null.
     */
    public String formatHeure(LocalTime t) {
        if (t == null) return "—";
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    public String getHeureArriveeStr()  { return formatHeure(heureArrivee); }
    public String getDebutPauseStr()    { return formatHeure(debutPause); }
    public String getFinPauseStr()      { return formatHeure(finPause); }
    public String getHeureDepartStr()   { return formatHeure(heureDepart); }

    public String getHeuresTravailleesStr() {
        if (heuresTravaillees <= 0) return "—";
        int h = (int) heuresTravaillees;
        int m = (int) Math.round((heuresTravaillees - h) * 60);
        return h + "h" + String.format("%02d", m);
    }

    public String getNomEmploye() {
        if (employe == null) return "—";
        return employe.getNom() + " " + employe.getPrenom();
    }

    public String getDepartementLabel() {
        if (departement == null && employe != null)
            return employe.getDepartement() != null ? employe.getDepartement().toString() : "—";
        return departement != null ? departement.toString() : "—";
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public LocalDate getDatePresence() { return datePresence; }
    public void setDatePresence(LocalDate datePresence) { this.datePresence = datePresence; }

    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public LocalTime getDebutPause() { return debutPause; }
    public void setDebutPause(LocalTime debutPause) { this.debutPause = debutPause; }

    public LocalTime getFinPause() { return finPause; }
    public void setFinPause(LocalTime finPause) { this.finPause = finPause; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public double getHeuresTravaillees() { return heuresTravaillees; }
    public void setHeuresTravaillees(double heuresTravaillees) { this.heuresTravaillees = heuresTravaillees; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }
}