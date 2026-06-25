package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.TriTelephonique;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTriTelephonique implements IServices<TriTelephonique> {

    private Connection cnx;

    public ServiceTriTelephonique() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    // ── CRUD ──

    @Override
    public void add(TriTelephonique candidat) {
        String query = "INSERT INTO tri_telephonique (nom, prenom, telephone, experience, poste, commentaire, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, candidat.getNom());
            pstmt.setString(2, candidat.getPrenom());
            pstmt.setString(3, candidat.getTelephone());
            pstmt.setString(4, candidat.getExperience());
            pstmt.setString(5, candidat.getPoste());
            pstmt.setString(6, candidat.getCommentaire());
            pstmt.setString(7, candidat.getStatut());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    candidat.setIdCand(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du candidat : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<TriTelephonique> getAll() {
        List<TriTelephonique> candidats = new ArrayList<>();
        String query = "SELECT * FROM tri_telephonique ORDER BY id_cand DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                TriTelephonique candidat = new TriTelephonique();
                candidat.setIdCand(rs.getInt("id_cand"));
                candidat.setNom(rs.getString("nom"));
                candidat.setPrenom(rs.getString("prenom"));
                candidat.setTelephone(rs.getString("telephone"));
                candidat.setExperience(rs.getString("experience"));
                candidat.setPoste(rs.getString("poste"));
                candidat.setCommentaire(rs.getString("commentaire"));

                // Gérer les valeurs NULL pour le statut
                String statut = rs.getString("statut");
                if (statut == null || statut.isEmpty()) {
                    statut = "En attente";
                }
                candidat.setStatut(statut);

                candidats.add(candidat);
            }

            System.out.println("Nombre de candidats récupérés : " + candidats.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des candidats : " + e.getMessage());
            e.printStackTrace();
        }

        return candidats;
    }

    @Override
    public void update(TriTelephonique candidat) {
        String query = "UPDATE tri_telephonique SET nom = ?, prenom = ?, telephone = ?, " +
                "experience = ?, poste = ?, commentaire = ?, statut = ? WHERE id_cand = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, candidat.getNom());
            pstmt.setString(2, candidat.getPrenom());
            pstmt.setString(3, candidat.getTelephone());
            pstmt.setString(4, candidat.getExperience());
            pstmt.setString(5, candidat.getPoste());
            pstmt.setString(6, candidat.getCommentaire());
            pstmt.setString(7, candidat.getStatut());
            pstmt.setInt(8, candidat.getIdCand());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du candidat : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(TriTelephonique candidat) {
        String query = "DELETE FROM tri_telephonique WHERE id_cand = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, candidat.getIdCand());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du candidat : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Méthodes supplémentaires ──

    public TriTelephonique findById(int id) {
        String query = "SELECT * FROM tri_telephonique WHERE id_cand = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                TriTelephonique candidat = new TriTelephonique();
                candidat.setIdCand(rs.getInt("id_cand"));
                candidat.setNom(rs.getString("nom"));
                candidat.setPrenom(rs.getString("prenom"));
                candidat.setTelephone(rs.getString("telephone"));
                candidat.setExperience(rs.getString("experience"));
                candidat.setPoste(rs.getString("poste"));
                candidat.setCommentaire(rs.getString("commentaire"));
                candidat.setStatut(rs.getString("statut"));
                return candidat;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du candidat : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<TriTelephonique> search(String keyword) {
        List<TriTelephonique> candidats = new ArrayList<>();
        String query = "SELECT * FROM tri_telephonique WHERE nom LIKE ? OR prenom LIKE ? OR telephone LIKE ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TriTelephonique candidat = new TriTelephonique();
                candidat.setIdCand(rs.getInt("id_cand"));
                candidat.setNom(rs.getString("nom"));
                candidat.setPrenom(rs.getString("prenom"));
                candidat.setTelephone(rs.getString("telephone"));
                candidat.setExperience(rs.getString("experience"));
                candidat.setPoste(rs.getString("poste"));
                candidat.setCommentaire(rs.getString("commentaire"));
                candidat.setStatut(rs.getString("statut"));
                candidats.add(candidat);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return candidats;
    }

    public List<TriTelephonique> filterByStatut(String statut) {
        List<TriTelephonique> candidats = new ArrayList<>();
        String query = "SELECT * FROM tri_telephonique WHERE statut = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TriTelephonique candidat = new TriTelephonique();
                candidat.setIdCand(rs.getInt("id_cand"));
                candidat.setNom(rs.getString("nom"));
                candidat.setPrenom(rs.getString("prenom"));
                candidat.setTelephone(rs.getString("telephone"));
                candidat.setExperience(rs.getString("experience"));
                candidat.setPoste(rs.getString("poste"));
                candidat.setCommentaire(rs.getString("commentaire"));
                candidat.setStatut(rs.getString("statut"));
                candidats.add(candidat);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            e.printStackTrace();
        }

        return candidats;
    }

    public int countByStatut(String statut) {
        String query = "SELECT COUNT(*) FROM tri_telephonique WHERE statut = ?";

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
}