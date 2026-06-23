package com.rh.services;

import com.rh.models.DemandeConge;
import com.rh.models.Employe;
import com.rh.models.SoldeConge;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandeCongeService {

    private final Connection cnx;
    private final EmployeService employeService;
    private final SoldeCongeService soldeService;

    public DemandeCongeService() {
        this.cnx = IsobatDB.getInstance().getCnx();
        this.employeService = new EmployeService();
        this.soldeService = new SoldeCongeService();
    }

    // ── ADD ─────────────────────────────────────────────────────────────────

    public void add(DemandeConge d) {
        String sql = "INSERT INTO demande_conge (employe_id, type_conge, date_debut, date_fin, " +
                "nombre_jours, statut, remplacant, commentaire, date_creation) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getEmploye().getId());
            ps.setString(2, d.getTypeConge().name());
            ps.setDate(3, Date.valueOf(d.getDateDebut()));
            ps.setDate(4, Date.valueOf(d.getDateFin()));
            ps.setInt(5, d.getNombreJours());
            ps.setString(6, d.getStatut().name());
            ps.setString(7, d.getRemplacant());
            ps.setString(8, d.getCommentaire());
            ps.setDate(9, d.getDateCreation() != null
                    ? Date.valueOf(d.getDateCreation()) : Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── GET ALL ─────────────────────────────────────────────────────────────

    public List<DemandeConge> getAll() {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge ORDER BY date_creation DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY EMPLOYE ───────────────────────────────────────────────────────

    public List<DemandeConge> getByEmploye(int employeId) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge WHERE employe_id=? ORDER BY date_creation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY STATUT ────────────────────────────────────────────────────────

    public List<DemandeConge> getByStatut(DemandeConge.Statut statut) {
        List<DemandeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM demande_conge WHERE statut=? ORDER BY date_creation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    public void update(DemandeConge d) {
        String sql = "UPDATE demande_conge SET employe_id=?, type_conge=?, date_debut=?, date_fin=?, " +
                "nombre_jours=?, statut=?, remplacant=?, commentaire=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, d.getEmploye().getId());
            ps.setString(2, d.getTypeConge().name());
            ps.setDate(3, Date.valueOf(d.getDateDebut()));
            ps.setDate(4, Date.valueOf(d.getDateFin()));
            ps.setInt(5, d.getNombreJours());
            ps.setString(6, d.getStatut().name());
            ps.setString(7, d.getRemplacant());
            ps.setString(8, d.getCommentaire());
            ps.setInt(9, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    public void delete(DemandeConge d) {
        // Si approuvée, restituer le solde avant suppression
        if (d.getStatut() == DemandeConge.Statut.APPROUVE) {
            soldeService.restituer(d.getEmploye().getId(),
                    d.getTypeConge().name(),
                    LocalDate.now().getYear(),
                    d.getNombreJours());
        }
        String sql = "DELETE FROM demande_conge WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── APPROUVER ────────────────────────────────────────────────────────────

    /**
     * Approuve une demande et décrémente le solde automatiquement.
     */
    public boolean approuver(DemandeConge d) {
        // Vérifie le solde disponible
        SoldeConge solde = soldeService.getSolde(
                d.getEmploye().getId(),
                d.getTypeConge().name(),
                LocalDate.now().getYear()
        );
        if (solde != null && solde.getSoldeRestant() < d.getNombreJours()) {
            return false; // Solde insuffisant
        }

        d.setStatut(DemandeConge.Statut.APPROUVE);
        update(d);

        // Décrémente le solde
        soldeService.consommer(
                d.getEmploye().getId(),
                d.getTypeConge().name(),
                LocalDate.now().getYear(),
                d.getNombreJours()
        );
        return true;
    }

    // ── REFUSER ──────────────────────────────────────────────────────────────

    public void refuser(DemandeConge d, String commentaire) {
        d.setStatut(DemandeConge.Statut.REFUSE);
        d.setCommentaire(commentaire);
        update(d);
    }

    // ── ANNULER ──────────────────────────────────────────────────────────────

    public void annuler(DemandeConge d) {
        // Si elle était approuvée, restituer le solde
        if (d.getStatut() == DemandeConge.Statut.APPROUVE) {
            soldeService.restituer(
                    d.getEmploye().getId(),
                    d.getTypeConge().name(),
                    LocalDate.now().getYear(),
                    d.getNombreJours()
            );
        }
        d.setStatut(DemandeConge.Statut.ANNULE);
        update(d);
    }

    // ── CALCULER JOURS ──────────────────────────────────────────────────────

    /**
     * Calcule le nombre de jours ouvrables entre deux dates (sans week-ends).
     */
    public static int calculerJoursOuvrables(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || fin.isBefore(debut)) return 0;
        int count = 0;
        LocalDate current = debut;
        while (!current.isAfter(fin)) {
            int dow = current.getDayOfWeek().getValue(); // 1=Lun ... 7=Dim
            if (dow < 6) count++; // Exclut samedi(6) et dimanche(7)
            current = current.plusDays(1);
        }
        return count;
    }

    // ── MAP ResultSet ────────────────────────────────────────────────────────

    private DemandeConge map(ResultSet rs) throws SQLException {
        DemandeConge d = new DemandeConge();
        d.setId(rs.getInt("id"));

        Employe emp = employeService.findById(rs.getInt("employe_id"));
        d.setEmploye(emp);

        try {
            d.setTypeConge(DemandeConge.TypeConge.valueOf(rs.getString("type_conge")));
        } catch (Exception e) {
            d.setTypeConge(DemandeConge.TypeConge.ANNUEL);
        }

        Date debut = rs.getDate("date_debut");
        Date fin   = rs.getDate("date_fin");
        if (debut != null) d.setDateDebut(debut.toLocalDate());
        if (fin   != null) d.setDateFin(fin.toLocalDate());

        d.setNombreJours(rs.getInt("nombre_jours"));

        try {
            d.setStatut(DemandeConge.Statut.valueOf(rs.getString("statut")));
        } catch (Exception e) {
            d.setStatut(DemandeConge.Statut.EN_ATTENTE);
        }

        d.setRemplacant(rs.getString("remplacant"));
        d.setCommentaire(rs.getString("commentaire"));

        Date created = rs.getDate("date_creation");
        if (created != null) d.setDateCreation(created.toLocalDate());

        return d;
    }
}