package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.EntretienPhysique;
import com.rh.models.ParticipationFormation;
import com.rh.models.TriTelephonique;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServiceTriTelephonique implements IServices<TriTelephonique> {

    private Connection cnx;
    private ServiceEntretienPhysique serviceEntretien;
    private ParticipationFormationService participationFormationService;

    public ServiceTriTelephonique() {
        cnx = IsobatDB.getInstance().getCnx();
        serviceEntretien = new ServiceEntretienPhysique();
        participationFormationService = new ParticipationFormationService();
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

    // ── Méthode pour ajouter un candidat retenu à la formation ──

    public boolean ajouterCandidatAFomation(TriTelephonique candidat, String dateFormation) {
        try {
            System.out.println("=== Ajout à la formation ===");
            System.out.println("Candidat : " + candidat.getNomComplet());
            System.out.println("Date formation : " + dateFormation);
            System.out.println("ID Candidat : " + candidat.getIdCand());

            // Récupérer l'entretien correspondant
            List<EntretienPhysique> entretiens = serviceEntretien.findByIdCandidat(candidat.getIdCand());
            if (entretiens.isEmpty()) {
                System.err.println("Aucun entretien trouvé pour le candidat ID: " + candidat.getIdCand());
                return false;
            }

            EntretienPhysique entretien = entretiens.get(0);
            System.out.println("Entretien trouvé - ID: " + entretien.getIdEntretien());

            // Créer une nouvelle participation
            ParticipationFormation participation = new ParticipationFormation();
            participation.setIdEntretien(entretien.getIdEntretien());
            participation.setNom(candidat.getNom());
            participation.setPrenom(candidat.getPrenom());
            participation.setPoste(candidat.getPoste());
            participation.setDateFormation(LocalDate.parse(dateFormation, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            participation.setFormation("Formation initiale");
            participation.setStatut("Présente");

            System.out.println("Participation créée : " + participation.toString());

            participationFormationService.add(participation);

            System.out.println("Participation ajoutée avec ID : " + participation.getIdParticipation());
            System.out.println("=== Fin de l'ajout à la formation ===");

            return true;

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout à la formation : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean ajouterCandidatAFomation(int idCandidat) {
        TriTelephonique candidat = findById(idCandidat);
        if (candidat == null) {
            return false;
        }

        String dateFormation = LocalDate.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        return ajouterCandidatAFomation(candidat, dateFormation);
    }

    // ── Méthodes d'acceptation et refus ──

    public boolean accepterCandidat(int idCandidat) {
        try {
            TriTelephonique candidat = findById(idCandidat);
            if (candidat == null) {
                System.err.println("Candidat non trouvé avec l'ID : " + idCandidat);
                return false;
            }

            List<EntretienPhysique> existants = serviceEntretien.findByIdCandidat(idCandidat);
            if (!existants.isEmpty()) {
                System.err.println("Ce candidat est déjà dans la liste des entretiens physiques");
                return false;
            }

            candidat.setStatut("Accepté");
            update(candidat);

            EntretienPhysique entretien = new EntretienPhysique();
            entretien.setIdCandidat(candidat.getIdCand());
            entretien.setNom(candidat.getNom());
            entretien.setPrenom(candidat.getPrenom());
            entretien.setPoste(candidat.getPoste());

            String dateRdv = LocalDateTime.now().plusDays(7)
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy à HH:mm"));
            entretien.setDateRdv(dateRdv);
            entretien.setStatut("En attente");

            serviceEntretien.add(entretien);

            System.out.println("Candidat accepté et ajouté à l'entretien physique : " + candidat.getNomComplet());
            return true;

        } catch (Exception e) {
            System.err.println("Erreur lors de l'acceptation du candidat : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean refuserCandidat(int idCandidat) {
        try {
            TriTelephonique candidat = findById(idCandidat);
            if (candidat == null) {
                return false;
            }

            candidat.setStatut("Pas accepté");
            update(candidat);

            System.out.println("Candidat refusé : " + candidat.getNomComplet());
            return true;

        } catch (Exception e) {
            System.err.println("Erreur lors du refus du candidat : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Méthode pour retenir un candidat (Entretien Physique → Formation) ──

    public boolean retenirCandidat(int idCandidat) {
        try {
            TriTelephonique candidat = findById(idCandidat);
            if (candidat == null) {
                System.err.println("Candidat non trouvé avec l'ID : " + idCandidat);
                return false;
            }

            System.out.println("=== Début retenirCandidat ===");
            System.out.println("Candidat : " + candidat.getNomComplet());

            // 1. Mettre à jour le statut du candidat dans tri_telephonique
            candidat.setStatut("Retenu");
            update(candidat);
            System.out.println("Statut mis à jour dans tri_telephonique : Retenu");

            // 2. Récupérer l'entretien correspondant
            List<EntretienPhysique> entretiens = serviceEntretien.findByIdCandidat(idCandidat);
            if (entretiens.isEmpty()) {
                System.err.println("Aucun entretien trouvé pour le candidat ID: " + idCandidat);
                return false;
            }
            EntretienPhysique entretien = entretiens.get(0);
            System.out.println("Entretien trouvé - ID: " + entretien.getIdEntretien());

            // 3. Mettre à jour le statut dans entretien_physique
            entretien.setStatut("Retenue");
            serviceEntretien.update(entretien);
            System.out.println("Statut mis à jour dans entretien_physique : Retenue");

            // 4. Ajouter à la formation
            String dateFormation = LocalDate.now().plusDays(7)
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            System.out.println("Date de formation : " + dateFormation);

            ParticipationFormation participation = new ParticipationFormation();
            participation.setIdEntretien(entretien.getIdEntretien());
            participation.setNom(candidat.getNom());
            participation.setPrenom(candidat.getPrenom());
            participation.setPoste(candidat.getPoste());
            participation.setDateFormation(LocalDate.parse(dateFormation, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            participation.setFormation("Formation initiale");
            participation.setStatut("Présente");

            participationFormationService.add(participation);
            System.out.println("Participation ajoutée avec ID : " + participation.getIdParticipation());

            System.out.println("=== Fin retenirCandidat ===");
            return true;

        } catch (Exception e) {
            System.err.println("Erreur lors du retenue du candidat : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Méthodes CRUD existantes ──

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