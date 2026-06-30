package com.rh.services;

import com.rh.models.Planning;
import com.rh.models.Planning.PrioritePlanning;
import com.rh.models.Planning.StatutPlanning;
import com.rh.models.Planning.TypePlanning;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlanningService {

    private final Connection cnx;

    public PlanningService() {
        this.cnx = IsobatDB.getInstance().getCnx();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public void add(Planning p) {
        String sql = "INSERT INTO planning (titre, description, date_evenement, heure_debut, heure_fin, " +
                "lieu, type, priorite, statut, rappel_minutes_avant, commentaire) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());
            ps.setDate(3, p.getDateEvenement() != null ? Date.valueOf(p.getDateEvenement()) : null);
            ps.setTime(4, p.getHeureDebut() != null ? Time.valueOf(p.getHeureDebut()) : null);
            ps.setTime(5, p.getHeureFin() != null ? Time.valueOf(p.getHeureFin()) : null);
            ps.setString(6, p.getLieu());
            ps.setString(7, p.getType() != null ? p.getType().name() : null);
            ps.setString(8, p.getPriorite() != null ? p.getPriorite().name() : null);
            ps.setString(9, p.getStatut() != null ? p.getStatut().name() : null);
            ps.setInt(10, p.getRappelMinutesAvant());
            ps.setString(11, p.getCommentaire());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Planning> getAll() {
        List<Planning> list = new ArrayList<>();
        String sql = "SELECT * FROM planning ORDER BY date_evenement DESC, heure_debut";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Planning> getByDate(LocalDate date) {
        List<Planning> list = new ArrayList<>();
        String sql = "SELECT * FROM planning WHERE date_evenement = ? ORDER BY heure_debut";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Planning> getByPeriode(LocalDate debut, LocalDate fin) {
        List<Planning> list = new ArrayList<>();
        String sql = "SELECT * FROM planning WHERE date_evenement BETWEEN ? AND ? ORDER BY date_evenement, heure_debut";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Planning> getToday() {
        return getByDate(LocalDate.now());
    }

    public List<Planning> getSoon() {
        List<Planning> list = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate dans3Jours = now.plusDays(3);
        String sql = "SELECT * FROM planning WHERE date_evenement BETWEEN ? AND ? " +
                "AND statut != 'TERMINE' AND statut != 'ANNULE' ORDER BY date_evenement, heure_debut";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(now));
            ps.setDate(2, Date.valueOf(dans3Jours));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Planning findById(int id) {
        String sql = "SELECT * FROM planning WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public void update(Planning p) {
        String sql = "UPDATE planning SET titre=?, description=?, date_evenement=?, heure_debut=?, heure_fin=?, " +
                "lieu=?, type=?, priorite=?, statut=?, rappel_minutes_avant=?, commentaire=?, rappel_envoye=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());
            ps.setDate(3, p.getDateEvenement() != null ? Date.valueOf(p.getDateEvenement()) : null);
            ps.setTime(4, p.getHeureDebut() != null ? Time.valueOf(p.getHeureDebut()) : null);
            ps.setTime(5, p.getHeureFin() != null ? Time.valueOf(p.getHeureFin()) : null);
            ps.setString(6, p.getLieu());
            ps.setString(7, p.getType() != null ? p.getType().name() : null);
            ps.setString(8, p.getPriorite() != null ? p.getPriorite().name() : null);
            ps.setString(9, p.getStatut() != null ? p.getStatut().name() : null);
            ps.setInt(10, p.getRappelMinutesAvant());
            ps.setString(11, p.getCommentaire());
            ps.setBoolean(12, p.isRappelEnvoye());
            ps.setInt(13, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void delete(int id) {
        String sql = "DELETE FROM planning WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    public int countToday() {
        return getToday().size();
    }

    public int countSoon() {
        return getSoon().size();
    }

    public int countUrgent() {
        String sql = "SELECT COUNT(*) FROM planning WHERE priorite IN ('URGENTE', 'HAUTE') AND statut != 'TERMINE'";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── MAP ───────────────────────────────────────────────────────────────────

    private Planning map(ResultSet rs) throws SQLException {
        Planning p = new Planning();
        p.setId(rs.getInt("id"));
        p.setTitre(rs.getString("titre"));
        p.setDescription(rs.getString("description"));

        Date d = rs.getDate("date_evenement");
        if (d != null) p.setDateEvenement(d.toLocalDate());

        Time hd = rs.getTime("heure_debut");
        Time hf = rs.getTime("heure_fin");
        if (hd != null) p.setHeureDebut(hd.toLocalTime());
        if (hf != null) p.setHeureFin(hf.toLocalTime());

        p.setLieu(rs.getString("lieu"));

        try { p.setType(TypePlanning.valueOf(rs.getString("type"))); }
        catch (Exception e) { p.setType(TypePlanning.AUTRE); }

        try { p.setPriorite(PrioritePlanning.valueOf(rs.getString("priorite"))); }
        catch (Exception e) { p.setPriorite(PrioritePlanning.MOYENNE); }

        try { p.setStatut(StatutPlanning.valueOf(rs.getString("statut"))); }
        catch (Exception e) { p.setStatut(StatutPlanning.PLANIFIE); }

        p.setRappelMinutesAvant(rs.getInt("rappel_minutes_avant"));
        p.setRappelEnvoye(rs.getBoolean("rappel_envoye"));
        p.setCommentaire(rs.getString("commentaire"));

        Timestamp created = rs.getTimestamp("date_creation");
        Timestamp modified = rs.getTimestamp("date_modification");
        if (created != null) p.setDateCreation(created.toLocalDateTime());
        if (modified != null) p.setDateModification(modified.toLocalDateTime());

        return p;
    }
}