package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.EntretienPhysique;
import com.rh.models.TriTelephonique;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEntretienPhysique implements IServices<EntretienPhysique> {

    private Connection cnx;

    public ServiceEntretienPhysique() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    // ── CRUD ──

    @Override
    public void add(EntretienPhysique entretien) {
        String query = "INSERT INTO entretien_physique (id_candidat, nom, prenom, poste, date_rdv, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, entretien.getIdCandidat());
            pstmt.setString(2, entretien.getNom());
            pstmt.setString(3, entretien.getPrenom());
            pstmt.setString(4, entretien.getPoste());
            pstmt.setString(5, entretien.getDateRdv());
            pstmt.setString(6, entretien.getStatut());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    entretien.setIdEntretien(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'entretien : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<EntretienPhysique> getAll() {
        List<EntretienPhysique> entretiens = new ArrayList<>();
        String query = "SELECT * FROM entretien_physique ORDER BY id_entretien DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                EntretienPhysique entretien = extractFromResultSet(rs);
                entretiens.add(entretien);
            }

            System.out.println("Nombre d'entretiens récupérés : " + entretiens.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des entretiens : " + e.getMessage());
            e.printStackTrace();
        }

        return entretiens;
    }

    @Override
    public void update(EntretienPhysique entretien) {
        String query = "UPDATE entretien_physique SET id_candidat = ?, nom = ?, prenom = ?, " +
                "poste = ?, date_rdv = ?, statut = ? WHERE id_entretien = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, entretien.getIdCandidat());
            pstmt.setString(2, entretien.getNom());
            pstmt.setString(3, entretien.getPrenom());
            pstmt.setString(4, entretien.getPoste());
            pstmt.setString(5, entretien.getDateRdv());
            pstmt.setString(6, entretien.getStatut());
            pstmt.setInt(7, entretien.getIdEntretien());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'entretien : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(EntretienPhysique entretien) {
        String query = "DELETE FROM entretien_physique WHERE id_entretien = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, entretien.getIdEntretien());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'entretien : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Méthodes supplémentaires ──

    public EntretienPhysique findById(int id) {
        String query = "SELECT * FROM entretien_physique WHERE id_entretien = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'entretien : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<EntretienPhysique> search(String keyword) {
        List<EntretienPhysique> entretiens = new ArrayList<>();
        String query = "SELECT * FROM entretien_physique WHERE nom LIKE ? OR prenom LIKE ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                entretiens.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return entretiens;
    }

    public List<EntretienPhysique> filterByStatut(String statut) {
        List<EntretienPhysique> entretiens = new ArrayList<>();
        String query = "SELECT * FROM entretien_physique WHERE statut = ? ORDER BY id_entretien DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                entretiens.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            e.printStackTrace();
        }

        return entretiens;
    }

    public List<EntretienPhysique> findByIdCandidat(int idCandidat) {
        List<EntretienPhysique> entretiens = new ArrayList<>();
        String query = "SELECT * FROM entretien_physique WHERE id_candidat = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, idCandidat);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                entretiens.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par candidat : " + e.getMessage());
            e.printStackTrace();
        }

        return entretiens;
    }

    public int countByStatut(String statut) {
        String query = "SELECT COUNT(*) FROM entretien_physique WHERE statut = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // ── Méthode utilitaire ──

    private EntretienPhysique extractFromResultSet(ResultSet rs) throws SQLException {
        EntretienPhysique entretien = new EntretienPhysique();
        entretien.setIdEntretien(rs.getInt("id_entretien"));
        entretien.setIdCandidat(rs.getInt("id_candidat"));
        entretien.setNom(rs.getString("nom"));
        entretien.setPrenom(rs.getString("prenom"));
        entretien.setPoste(rs.getString("poste"));
        entretien.setDateRdv(rs.getString("date_rdv"));
        entretien.setStatut(rs.getString("statut"));
        return entretien;
    }
}