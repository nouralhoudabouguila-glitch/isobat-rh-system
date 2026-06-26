package com.rh.services;

import com.rh.models.User;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Connection cnx;

    public UserService() {
        this.cnx = IsobatDB.getInstance().getCnx();
    }

    // ── AUTHENTIFICATION ──────────────────────────────────────────────────────

    /**
     * Vérifie email + mot de passe.
     * Retourne l'User si OK, null sinon.
     * Note : le mot de passe est stocké en clair pour ce projet.
     * En production, utiliser bcrypt.
     */
    public User login(String email, String motDePasse) {
        String sql = "SELECT * FROM users WHERE email = ? AND mot_de_passe = ? AND actif = 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Vérifie uniquement si l'email existe (pour la page "mot de passe oublié").
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── ADD ───────────────────────────────────────────────────────────────────

    public boolean add(User u) {
        // Vérifier si l'email est déjà utilisé
        if (emailExists(u.getEmail())) return false;

        String sql = "INSERT INTO users (nom, prenom, email, mot_de_passe, role, actif, telephone) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail().trim().toLowerCase());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole().name());
            ps.setBoolean(6, u.isActif());
            ps.setString(7, u.getTelephone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY nom, prenom";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── FIND BY ID ────────────────────────────────────────────────────────────

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean update(User u) {
        String sql = "UPDATE users SET nom=?, prenom=?, email=?, role=?, actif=?, telephone=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail().trim().toLowerCase());
            ps.setString(4, u.getRole().name());
            ps.setBoolean(5, u.isActif());
            ps.setString(6, u.getTelephone());
            ps.setInt(7, u.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── CHANGER MOT DE PASSE ─────────────────────────────────────────────────

    public boolean changerMotDePasse(int userId, String ancienMdp, String nouveauMdp) {
        // Vérifier l'ancien mot de passe
        String check = "SELECT id FROM users WHERE id=? AND mot_de_passe=?";
        try (PreparedStatement ps = cnx.prepareStatement(check)) {
            ps.setInt(1, userId);
            ps.setString(2, ancienMdp);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false; // Ancien MDP incorrect
        } catch (SQLException e) { e.printStackTrace(); return false; }

        String sql = "UPDATE users SET mot_de_passe=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouveauMdp);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Réinitialise le mot de passe sans vérifier l'ancien (action Admin).
     */
    public boolean resetMotDePasse(int userId, String nouveauMdp) {
        String sql = "UPDATE users SET mot_de_passe=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouveauMdp);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── ACTIVER / DÉSACTIVER ──────────────────────────────────────────────────

    public void toggleActif(int userId, boolean actif) {
        String sql = "UPDATE users SET actif=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, actif);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── MAP ───────────────────────────────────────────────────────────────────

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setActif(rs.getBoolean("actif"));
        u.setTelephone(rs.getString("telephone"));

        try {
            u.setRole(User.Role.valueOf(rs.getString("role")));
        } catch (Exception e) {
            u.setRole(User.Role.RESPONSABLE_RH);
        }

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());
        if (updated != null) u.setUpdatedAt(updated.toLocalDateTime());

        return u;
    }
}