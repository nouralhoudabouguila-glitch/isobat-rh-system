package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.ParticipationFormation;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ParticipationFormationService implements IServices<ParticipationFormation> {

    private Connection cnx;

    public ParticipationFormationService() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    @Override
    public void add(ParticipationFormation participation) {
        String query = "INSERT INTO participation_formation (id_entretien, nom, prenom, poste, date_formation, formation, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, participation.getIdEntretien());
            pstmt.setString(2, participation.getNom());
            pstmt.setString(3, participation.getPrenom());
            pstmt.setString(4, participation.getPoste());
            pstmt.setDate(5, Date.valueOf(participation.getDateFormation()));
            pstmt.setString(6, participation.getFormation());
            pstmt.setString(7, participation.getStatut());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    participation.setIdParticipation(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la participation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<ParticipationFormation> getAll() {
        List<ParticipationFormation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation_formation ORDER BY date_formation DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ParticipationFormation participation = new ParticipationFormation();
                participation.setIdParticipation(rs.getInt("id_participation"));
                participation.setIdEntretien(rs.getInt("id_entretien"));
                participation.setNom(rs.getString("nom"));
                participation.setPrenom(rs.getString("prenom"));
                participation.setPoste(rs.getString("poste"));
                participation.setDateFormation(rs.getDate("date_formation").toLocalDate());
                participation.setFormation(rs.getString("formation"));
                participation.setStatut(rs.getString("statut"));
                participations.add(participation);
            }

            System.out.println("Nombre de participations récupérées : " + participations.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des participations : " + e.getMessage());
            e.printStackTrace();
        }

        return participations;
    }

    @Override
    public void update(ParticipationFormation participation) {
        String query = "UPDATE participation_formation SET id_entretien = ?, nom = ?, prenom = ?, " +
                "poste = ?, date_formation = ?, formation = ?, statut = ? WHERE id_participation = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, participation.getIdEntretien());
            pstmt.setString(2, participation.getNom());
            pstmt.setString(3, participation.getPrenom());
            pstmt.setString(4, participation.getPoste());
            pstmt.setDate(5, Date.valueOf(participation.getDateFormation()));
            pstmt.setString(6, participation.getFormation());
            pstmt.setString(7, participation.getStatut());
            pstmt.setInt(8, participation.getIdParticipation());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la participation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(ParticipationFormation participation) {
        String query = "DELETE FROM participation_formation WHERE id_participation = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, participation.getIdParticipation());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la participation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<ParticipationFormation> search(String keyword) {
        List<ParticipationFormation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation_formation WHERE nom LIKE ? OR prenom LIKE ? OR formation LIKE ? " +
                "ORDER BY date_formation DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ParticipationFormation participation = new ParticipationFormation();
                participation.setIdParticipation(rs.getInt("id_participation"));
                participation.setIdEntretien(rs.getInt("id_entretien"));
                participation.setNom(rs.getString("nom"));
                participation.setPrenom(rs.getString("prenom"));
                participation.setPoste(rs.getString("poste"));
                participation.setDateFormation(rs.getDate("date_formation").toLocalDate());
                participation.setFormation(rs.getString("formation"));
                participation.setStatut(rs.getString("statut"));
                participations.add(participation);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return participations;
    }

    public List<ParticipationFormation> filterByStatut(String statut) {
        List<ParticipationFormation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation_formation WHERE statut = ? ORDER BY date_formation DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ParticipationFormation participation = new ParticipationFormation();
                participation.setIdParticipation(rs.getInt("id_participation"));
                participation.setIdEntretien(rs.getInt("id_entretien"));
                participation.setNom(rs.getString("nom"));
                participation.setPrenom(rs.getString("prenom"));
                participation.setPoste(rs.getString("poste"));
                participation.setDateFormation(rs.getDate("date_formation").toLocalDate());
                participation.setFormation(rs.getString("formation"));
                participation.setStatut(rs.getString("statut"));
                participations.add(participation);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            e.printStackTrace();
        }

        return participations;
    }
}