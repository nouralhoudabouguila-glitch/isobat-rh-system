package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.Fournisseur;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceFournisseur implements IServices<Fournisseur> {

    private Connection cnx;

    public ServiceFournisseur() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    @Override
    public void add(Fournisseur fournisseur) {
        // Si la date de création est null, on la définit à maintenant
        if (fournisseur.getDateCreation() == null) {
            fournisseur.setDateCreation(LocalDateTime.now());
        }

        String query = "INSERT INTO fournisseur (nom, email, telephone, adresse, ville, pays, matricule_fiscal, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getEmail());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getAdresse());
            pstmt.setString(5, fournisseur.getVille());
            pstmt.setString(6, fournisseur.getPays());
            pstmt.setString(7, fournisseur.getMatriculeFiscal());
            pstmt.setTimestamp(8, Timestamp.valueOf(fournisseur.getDateCreation()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    fournisseur.setIdFournisseur(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Fournisseur> getAll() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseur ORDER BY id_fournisseur DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Fournisseur fournisseur = extractFromResultSet(rs);
                fournisseurs.add(fournisseur);
            }

            System.out.println("Nombre de fournisseurs récupérés : " + fournisseurs.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des fournisseurs : " + e.getMessage());
            e.printStackTrace();
        }

        return fournisseurs;
    }

    @Override
    public void update(Fournisseur fournisseur) {
        String query = "UPDATE fournisseur SET nom = ?, email = ?, telephone = ?, " +
                "adresse = ?, ville = ?, pays = ?, matricule_fiscal = ? WHERE id_fournisseur = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getEmail());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getAdresse());
            pstmt.setString(5, fournisseur.getVille());
            pstmt.setString(6, fournisseur.getPays());
            pstmt.setString(7, fournisseur.getMatriculeFiscal());
            pstmt.setInt(8, fournisseur.getIdFournisseur());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Fournisseur fournisseur) {
        String query = "DELETE FROM fournisseur WHERE id_fournisseur = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, fournisseur.getIdFournisseur());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Fournisseur findById(int id) {
        String query = "SELECT * FROM fournisseur WHERE id_fournisseur = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du fournisseur : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Fournisseur> search(String keyword) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseur WHERE nom LIKE ? OR email LIKE ? OR telephone LIKE ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                fournisseurs.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return fournisseurs;
    }

    private Fournisseur extractFromResultSet(ResultSet rs) throws SQLException {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setIdFournisseur(rs.getInt("id_fournisseur"));
        fournisseur.setNom(rs.getString("nom"));
        fournisseur.setEmail(rs.getString("email"));
        fournisseur.setTelephone(rs.getString("telephone"));
        fournisseur.setAdresse(rs.getString("adresse"));
        fournisseur.setVille(rs.getString("ville"));
        fournisseur.setPays(rs.getString("pays"));
        fournisseur.setMatriculeFiscal(rs.getString("matricule_fiscal"));

        // Gérer le cas où date_creation est NULL
        Timestamp timestamp = rs.getTimestamp("date_creation");
        if (timestamp != null) {
            fournisseur.setDateCreation(timestamp.toLocalDateTime());
        } else {
            fournisseur.setDateCreation(LocalDateTime.now());
        }

        return fournisseur;
    }
}