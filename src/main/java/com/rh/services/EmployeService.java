package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeService implements IServices<Employe> {

    private final Connection cnx;

    public EmployeService() {
        this.cnx = IsobatDB.getInstance().getCnx();
    }

    @Override
    public void add(Employe e) {
        String sql = "INSERT INTO employe (nom, prenom, dateNaissance, situationFamiliale, cin, telephone, salaireMensuelNet, profil, typeContrat, statut, dateEmbauche, nRIB, nCNSS, departement) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setDate(3, e.getDateNaissance() != null ? Date.valueOf(e.getDateNaissance()) : null);
            ps.setString(4, e.getSituationFamiliale());
            ps.setString(5, e.getCin());
            ps.setString(6, e.getTelephone());
            ps.setDouble(7, e.getSalaireMensuelNet());
            ps.setString(8, e.getProfil());
            ps.setString(9, e.getTypeContrat());
            ps.setString(10, e.getStatut());
            ps.setDate(11, e.getDateEmbauche() != null ? Date.valueOf(e.getDateEmbauche()) : null);
            ps.setString(12, e.getnRIB());
            ps.setString(13, e.getnCNSS());
            ps.setString(14, e.getDepartement() != null ? e.getDepartement().name() : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<Employe> getAll() {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employe";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Employe e = mapResultSet(rs);
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(Employe e) {
        String sql = "UPDATE employe SET nom=?, prenom=?, dateNaissance=?, situationFamiliale=?, cin=?, telephone=?, salaireMensuelNet=?, profil=?, typeContrat=?, statut=?, dateEmbauche=?, nRIB=?, nCNSS=?, departement=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setDate(3, e.getDateNaissance() != null ? Date.valueOf(e.getDateNaissance()) : null);
            ps.setString(4, e.getSituationFamiliale());
            ps.setString(5, e.getCin());
            ps.setString(6, e.getTelephone());
            ps.setDouble(7, e.getSalaireMensuelNet());
            ps.setString(8, e.getProfil());
            ps.setString(9, e.getTypeContrat());
            ps.setString(10, e.getStatut());
            ps.setDate(11, e.getDateEmbauche() != null ? Date.valueOf(e.getDateEmbauche()) : null);
            ps.setString(12, e.getnRIB());
            ps.setString(13, e.getnCNSS());
            ps.setString(14, e.getDepartement() != null ? e.getDepartement().name() : null);
            ps.setInt(15, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(Employe e) {
        try {
            String sqlSolde = "DELETE FROM solde_conge WHERE employe_id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sqlSolde)) {
                ps.setInt(1, e.getId());
                ps.executeUpdate();
            }

            String sqlPresence = "DELETE FROM presence WHERE employe_id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sqlPresence)) {
                ps.setInt(1, e.getId());
                ps.executeUpdate();
            }

            String sqlDemande = "DELETE FROM demande_conge WHERE employe_id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sqlDemande)) {
                ps.setInt(1, e.getId());
                ps.executeUpdate();
            }

            String sqlEmploye = "DELETE FROM employe WHERE id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sqlEmploye)) {
                ps.setInt(1, e.getId());
                ps.executeUpdate();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Employe findById(int id) {
        String sql = "SELECT * FROM employe WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 🔥 NOUVELLES MÉTHODES POUR L'IMPORT EXCEL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * 🔥 RECHERCHE D'UN EMPLOYÉ PAR SON NOM COMPLET (peu importe l'ordre)
     * Utile pour corriger les mappings d'import Excel
     *
     * Exemples: "Inesnssiri", "Ines nssiri", "Nssiri Ines" → tout fonctionne
     */
    public Employe findByNomPrenom(String nomComplet) {
        if (nomComplet == null || nomComplet.trim().isEmpty()) return null;

        String search = nomComplet.trim().replace(" ", "").toLowerCase();

        String sql = "SELECT * FROM employe WHERE LOWER(CONCAT(nom, prenom)) LIKE ? OR LOWER(CONCAT(prenom, nom)) LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String pattern = "%" + search + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 🔥 RECHERCHE D'UN EMPLOYÉ PAR SON NOM ET PRÉNOM SÉPARÉMENT
     */
    public Employe findByNomPrenom(String nom, String prenom) {
        if (nom == null || nom.trim().isEmpty()) return null;

        String sql = "SELECT * FROM employe WHERE LOWER(nom) LIKE ? AND LOWER(prenom) LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + nom.trim().toLowerCase() + "%");
            ps.setString(2, prenom != null ? "%" + prenom.trim().toLowerCase() + "%" : "%%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 🔥 RECHERCHE DES EMPLOYÉS PAR DÉPARTEMENT
     */
    public List<Employe> getByDepartement(Departement dept) {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employe WHERE departement = ? ORDER BY nom";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, dept.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 🔥 RÉCUPÉRER TOUS LES EMPLOYÉS DU CENTRE D'APPEL (ID 101 à 137)
     */
    public List<Employe> getCentreAppel() {
        return getByDepartement(Departement.CENTRE_APPEL_B2B);
    }

    /**
     * 🔥 RÉCUPÉRER TOUS LES EMPLOYÉS DU BUREAU D'ÉTUDE (ID 1 à 48)
     */
    public List<Employe> getBureauEtude() {
        return getByDepartement(Departement.BUREAU_ETUDE);
    }

    /**
     * 🔥 VÉRIFIER SI UN EMPLOYÉ EXISTE
     */
    public boolean exists(int id) {
        String sql = "SELECT 1 FROM employe WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 🔥 COMPTER LE NOMBRE TOTAL D'EMPLOYÉS
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM employe";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════════

    private Employe mapResultSet(ResultSet rs) throws SQLException {
        Employe e = new Employe();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));

        try {
            Date dNaiss = rs.getDate("dateNaissance");
            if (dNaiss != null && !dNaiss.toString().startsWith("0000")) {
                e.setDateNaissance(dNaiss.toLocalDate());
            }
        } catch (Exception ex) {
            // Ignorer - date invalide
        }

        e.setSituationFamiliale(rs.getString("situationFamiliale"));
        e.setCin(rs.getString("cin"));
        e.setTelephone(rs.getString("telephone"));
        e.setSalaireMensuelNet(rs.getDouble("salaireMensuelNet"));
        e.setProfil(rs.getString("profil"));
        e.setTypeContrat(rs.getString("typeContrat"));
        e.setStatut(rs.getString("statut"));

        try {
            Date dEmbauche = rs.getDate("dateEmbauche");
            if (dEmbauche != null && !dEmbauche.toString().startsWith("0000")) {
                e.setDateEmbauche(dEmbauche.toLocalDate());
            }
        } catch (Exception ex) {
            // Ignorer - date invalide
        }

        e.setnRIB(rs.getString("nRIB"));
        e.setnCNSS(rs.getString("nCNSS"));

        String dep = rs.getString("departement");
        if (dep != null) {
            try {
                e.setDepartement(Departement.valueOf(dep));
            } catch (IllegalArgumentException ex) {
                for (Departement d : Departement.values()) {
                    if (d.toString().equalsIgnoreCase(dep)) {
                        e.setDepartement(d);
                        break;
                    }
                }
            }
        }
        return e;
    }
}