package com.rh.services;

import com.rh.models.Employe;
import com.rh.models.SoldeConge;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class SoldeCongeService {

    private final Connection cnx;
    private static final double SOLDE_ANNUEL = 18.0;
    private static final double ACQUISITION_PAR_MOIS = SOLDE_ANNUEL / 12; // 1.5 jours par mois

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

    // ── GET SOLDE SPÉCIFIQUE ─────────────────────────────────────────────────

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

    // ── GET ALL ──────────────────────────────────────────────────────────────

    public List<SoldeConge> getAllAnnee(int annee) {
        List<SoldeConge> list = new ArrayList<>();
        String sql = "SELECT sc.*, e.nom, e.prenom, e.profil, e.dateEmbauche FROM solde_conge sc " +
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

    // ── CONSOMMER ─────────────────────────────────────────────────────────────

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

    // ── RESTITUER ─────────────────────────────────────────────────────────────

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

    // ── CALCUL DU SOLDE SELON LA DATE D'EMBAUCHE ────────────────────────────

    /**
     * Calcule le solde de congés pour un employé selon sa date d'embauche.
     * Règle ISOBAT : 1.5 jour par mois travaillé dans l'année.
     *
     * @param emp L'employé
     * @param annee L'année de référence
     * @return Le solde calculé (arrondi à 0.5 près)
     */
    public double calculerSolde(Employe emp, int annee) {
        if (emp.getDateEmbauche() == null) {
            return 0;
        }

        LocalDate dateEmbauche = emp.getDateEmbauche();
        int moisEmbauche = dateEmbauche.getMonthValue();
        int anneeEmbauche = dateEmbauche.getYear();

        // Si l'employé a été embauché après le 1er juillet, il n'a pas de congé cette année
        // (Règle ISOBAT : les congés sont pris en Août et Décembre)
        if (anneeEmbauche == annee && moisEmbauche > 6) {
            return 0;
        }

        // Si l'employé a été embauché l'année précédente ou avant, il a 18 jours
        if (anneeEmbauche < annee) {
            return SOLDE_ANNUEL;
        }

        // Calcul des mois travaillés (de la date d'embauche à Juin)
        int moisTravailles = 0;
        if (anneeEmbauche == annee) {
            // De Janvier à Juin (période d'acquisition)
            for (int m = moisEmbauche; m <= 6; m++) {
                // Vérifier si l'employé a travaillé tout le mois
                // (on considère que si embauché après le 15, le mois ne compte pas)
                if (dateEmbauche.getDayOfMonth() <= 15) {
                    moisTravailles++;
                } else {
                    // Si embauché après le 15, on ne compte que le mois suivant
                    if (m > moisEmbauche) {
                        moisTravailles++;
                    }
                }
            }
        }

        double solde = moisTravailles * ACQUISITION_PAR_MOIS;

        // Arrondir à 0.5 près (ex: 4.5, 6.0, 7.5)
        solde = Math.round(solde * 2) / 2.0;

        return Math.min(solde, SOLDE_ANNUEL);
    }

    /**
     * Initialise le solde pour un employé pour une année donnée.
     * Utilise la date d'embauche pour calculer le solde.
     */
    public void initSolde(Employe emp, int annee) {
        // Ne pas initialiser pour les employés qui n'ont pas encore de droit
        if (emp.getDateEmbauche() == null) {
            return;
        }

        double solde = calculerSolde(emp, annee);

        // Pour les autres types de congés (maladie, exceptionnel, etc.)
        // On initialise avec des valeurs par défaut (à ajuster selon les règles de l'entreprise)
        String[] types = {"ANNUEL", "MALADIE", "EXCEPTIONNEL"};
        double[] soldes = {solde, 15, 5}; // MALADIE: 15 jours, EXCEPTIONNEL: 5 jours

        for (int i = 0; i < types.length; i++) {
            SoldeConge existing = getSolde(emp.getId(), types[i], annee);
            if (existing == null) {
                SoldeConge s = new SoldeConge(emp, annee, types[i], soldes[i]);
                add(s);
            }
        }
    }

    /**
     * Initialise les soldes pour tous les employés pour une année donnée.
     * À appeler en début d'année ou lors de l'ajout d'un nouvel employé.
     */
    public void initSoldesAnnuels(Employe emp, int annee) {
        initSolde(emp, annee);
    }

    // ── MAP ───────────────────────────────────────────────────────────────────

    private SoldeConge map(ResultSet rs, int employeId) throws SQLException {
        SoldeConge s = new SoldeConge();
        s.setId(rs.getInt("id"));
        Employe emp = new Employe();
        emp.setId(employeId);
        try { emp.setNom(rs.getString("nom")); } catch (Exception ignored) {}
        try { emp.setPrenom(rs.getString("prenom")); } catch (Exception ignored) {}
        try {
            Date d = rs.getDate("dateEmbauche");
            if (d != null) emp.setDateEmbauche(d.toLocalDate());
        } catch (Exception ignored) {}
        s.setEmploye(emp);
        s.setAnnee(rs.getInt("annee"));
        s.setTypeConge(rs.getString("type_conge"));
        s.setSoldeInitial(rs.getDouble("solde_initial"));
        s.setSoldeConsomme(rs.getDouble("solde_consomme"));
        s.setSoldeRestant(rs.getDouble("solde_restant"));
        return s;
    }
}