package com.rh.services;

import com.rh.models.Employe;
import com.rh.models.SoldeConge;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SoldeCongeService {

    private final Connection cnx;

    public SoldeCongeService() {
        this.cnx = IsobatDB.getInstance().getCnx();
    }

    // ── ADD ─────────────────────────────────────────────────────────────────

    public void add(SoldeConge s) {
        String sql = "INSERT INTO solde_conge (employe_id, annee, type_conge, " +
                "solde_initial, solde_consomme, solde_restant) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getEmploye().getId());
            ps.setInt(2, s.getAnnee());
            ps.setString(3, s.getTypeConge());
            ps.setDouble(4, s.getSoldeInitial());
            ps.setDouble(5, s.getSoldeConsomme());
            ps.setDouble(6, s.getSoldeRestant());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── GET BY EMPLOYE + ANNEE ───────────────────────────────────────────────

    public List<SoldeConge> getByEmploye(int employeId, int annee) {
        List<SoldeConge> list = new ArrayList<>();
        String sql = "SELECT * FROM solde_conge WHERE employe_id=? AND annee=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ps.setInt(2, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs, employeId));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET SOLDE SPECIFIQUE ─────────────────────────────────────────────────

    public SoldeConge getSolde(int employeId, String typeConge, int annee) {
        String sql = "SELECT * FROM solde_conge WHERE employe_id=? AND type_conge=? AND annee=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ps.setString(2, typeConge);
            ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs, employeId);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET ALL (tous employés, année courante) ──────────────────────────────

    public List<SoldeConge> getAllAnnee(int annee) {
        List<SoldeConge> list = new ArrayList<>();
        String sql = "SELECT sc.*, e.nom, e.prenom, e.profil FROM solde_conge sc " +
                "JOIN employe e ON sc.employe_id = e.id WHERE sc.annee=? ORDER BY e.nom";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs, rs.getInt("employe_id")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    public void update(SoldeConge s) {
        String sql = "UPDATE solde_conge SET solde_initial=?, solde_consomme=?, solde_restant=? " +
                "WHERE employe_id=? AND type_conge=? AND annee=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, s.getSoldeInitial());
            ps.setDouble(2, s.getSoldeConsomme());
            ps.setDouble(3, s.getSoldeRestant());
            ps.setInt(4, s.getEmploye().getId());
            ps.setString(5, s.getTypeConge());
            ps.setInt(6, s.getAnnee());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── CONSOMMER (appelé quand demande approuvée, mode = SOLDE) ─────────────

    public void consommer(int employeId, String typeConge, int annee, double jours) {
        String sql = "UPDATE solde_conge SET solde_consomme = solde_consomme + ?, " +
                "solde_restant = solde_restant - ? " +
                "WHERE employe_id=? AND type_conge=? AND annee=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, jours); ps.setDouble(2, jours);
            ps.setInt(3, employeId); ps.setString(4, typeConge); ps.setInt(5, annee);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── RESTITUER (appelé quand demande annulée/supprimée) ───────────────────

    public void restituer(int employeId, String typeConge, int annee, double jours) {
        String sql = "UPDATE solde_conge SET solde_consomme = GREATEST(0, solde_consomme - ?), " +
                "solde_restant = LEAST(solde_initial, solde_restant + ?) " +
                "WHERE employe_id=? AND type_conge=? AND annee=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, jours); ps.setDouble(2, jours);
            ps.setInt(3, employeId); ps.setString(4, typeConge); ps.setInt(5, annee);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── INIT SOLDES ANNUELS ──────────────────────────────────────────────────

    /**
     * Initialise le solde annuel pour un employé (appelé en début d'année ou
     * à l'embauche).
     *
     * Règle ISOBAT : le congé ANNUEL est toujours de 18 jours/an, quel que
     * soit l'employé. (Les autres types ci-dessous sont indicatifs — à
     * adapter/retirer si votre convention ne les suit pas via ce mécanisme
     * de solde.)
     */
    public void initSoldesAnnuels(Employe emp, int annee) {
        String[] types  = {"ANNUEL", "MALADIE", "EXCEPTIONNEL"};
        double[] soldes = {18,        15,         5};
        for (int i = 0; i < types.length; i++) {
            SoldeConge existing = getSolde(emp.getId(), types[i], annee);
            if (existing == null) {
                SoldeConge s = new SoldeConge(emp, annee, types[i], soldes[i]);
                add(s);
            }
        }
    }

    // ── MAP ResultSet ────────────────────────────────────────────────────────

    private SoldeConge map(ResultSet rs, int employeId) throws SQLException {
        SoldeConge s = new SoldeConge();
        s.setId(rs.getInt("id"));
        Employe emp = new Employe();
        emp.setId(employeId);
        try { emp.setNom(rs.getString("nom")); } catch (Exception ignored) {}
        try { emp.setPrenom(rs.getString("prenom")); } catch (Exception ignored) {}
        s.setEmploye(emp);
        s.setAnnee(rs.getInt("annee"));
        s.setTypeConge(rs.getString("type_conge"));
        s.setSoldeInitial(rs.getDouble("solde_initial"));
        s.setSoldeConsomme(rs.getDouble("solde_consomme"));
        s.setSoldeRestant(rs.getDouble("solde_restant"));
        return s;
    }
}