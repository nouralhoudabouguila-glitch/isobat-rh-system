package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.Facture;
import com.rh.models.Facture.ModePaiement;
import com.rh.models.Facture.StatutFacture;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceFacture implements IServices<Facture> {

    private Connection cnx;
    private ServiceFournisseur serviceFournisseur;

    public ServiceFacture() {
        cnx = IsobatDB.getInstance().getCnx();
        serviceFournisseur = new ServiceFournisseur();
    }

    @Override
    public void add(Facture facture) {
        String query = "INSERT INTO facture (numero_facture, id_fournisseur, montant_ht, montant_tva, " +
                "montant_ttc, date_facture, date_echeance, statut, mode_paiement, fichier_pdf, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setInt(2, facture.getIdFournisseur());
            pstmt.setDouble(3, facture.getMontantHt());
            pstmt.setDouble(4, facture.getMontantTva());
            pstmt.setDouble(5, facture.getMontantTtc());
            pstmt.setDate(6, Date.valueOf(facture.getDateFacture()));

            if (facture.getDateEcheance() != null) {
                pstmt.setDate(7, Date.valueOf(facture.getDateEcheance()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }

            pstmt.setString(8, facture.getStatutLabel());
            pstmt.setString(9, facture.getModePaiementLabel());
            pstmt.setString(10, facture.getFichierPdf());
            pstmt.setString(11, facture.getCommentaire());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    facture.setIdFacture(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Facture> getAll() {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture ORDER BY id_facture DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Facture facture = extractFromResultSet(rs);
                // Charger le fournisseur associé
                facture.setFournisseur(serviceFournisseur.findById(facture.getIdFournisseur()));
                factures.add(facture);
            }

            System.out.println("Nombre de factures récupérées : " + factures.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des factures : " + e.getMessage());
            e.printStackTrace();
        }

        return factures;
    }

    @Override
    public void update(Facture facture) {
        String query = "UPDATE facture SET numero_facture = ?, id_fournisseur = ?, montant_ht = ?, " +
                "montant_tva = ?, montant_ttc = ?, date_facture = ?, date_echeance = ?, " +
                "statut = ?, mode_paiement = ?, fichier_pdf = ?, commentaire = ? WHERE id_facture = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setInt(2, facture.getIdFournisseur());
            pstmt.setDouble(3, facture.getMontantHt());
            pstmt.setDouble(4, facture.getMontantTva());
            pstmt.setDouble(5, facture.getMontantTtc());
            pstmt.setDate(6, Date.valueOf(facture.getDateFacture()));

            if (facture.getDateEcheance() != null) {
                pstmt.setDate(7, Date.valueOf(facture.getDateEcheance()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }

            pstmt.setString(8, facture.getStatutLabel());
            pstmt.setString(9, facture.getModePaiementLabel());
            pstmt.setString(10, facture.getFichierPdf());
            pstmt.setString(11, facture.getCommentaire());
            pstmt.setInt(12, facture.getIdFacture());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Facture facture) {
        String query = "DELETE FROM facture WHERE id_facture = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, facture.getIdFacture());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Facture findById(int id) {
        String query = "SELECT * FROM facture WHERE id_facture = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Facture facture = extractFromResultSet(rs);
                facture.setFournisseur(serviceFournisseur.findById(facture.getIdFournisseur()));
                return facture;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la facture : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Facture> findByFournisseur(int idFournisseur) {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture WHERE id_fournisseur = ? ORDER BY date_facture DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, idFournisseur);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Facture facture = extractFromResultSet(rs);
                facture.setFournisseur(serviceFournisseur.findById(idFournisseur));
                factures.add(facture);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par fournisseur : " + e.getMessage());
            e.printStackTrace();
        }

        return factures;
    }

    public List<Facture> filterByStatut(String statut) {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture WHERE statut = ? ORDER BY date_facture DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Facture facture = extractFromResultSet(rs);
                facture.setFournisseur(serviceFournisseur.findById(facture.getIdFournisseur()));
                factures.add(facture);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            e.printStackTrace();
        }

        return factures;
    }

    public List<Facture> search(String keyword) {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT f.* FROM facture f " +
                "LEFT JOIN fournisseur fr ON f.id_fournisseur = fr.id_fournisseur " +
                "WHERE f.numero_facture LIKE ? OR fr.nom LIKE ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Facture facture = extractFromResultSet(rs);
                facture.setFournisseur(serviceFournisseur.findById(facture.getIdFournisseur()));
                factures.add(facture);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return factures;
    }

    private Facture extractFromResultSet(ResultSet rs) throws SQLException {
        Facture facture = new Facture();
        facture.setIdFacture(rs.getInt("id_facture"));
        facture.setNumeroFacture(rs.getString("numero_facture"));
        facture.setIdFournisseur(rs.getInt("id_fournisseur"));
        facture.setMontantHt(rs.getDouble("montant_ht"));
        facture.setMontantTva(rs.getDouble("montant_tva"));
        facture.setMontantTtc(rs.getDouble("montant_ttc"));

        Date dateFacture = rs.getDate("date_facture");
        if (dateFacture != null) {
            facture.setDateFacture(dateFacture.toLocalDate());
        }

        Date dateEcheance = rs.getDate("date_echeance");
        if (dateEcheance != null) {
            facture.setDateEcheance(dateEcheance.toLocalDate());
        }

        facture.setStatut(rs.getString("statut"));
        facture.setModePaiement(rs.getString("mode_paiement"));
        facture.setFichierPdf(rs.getString("fichier_pdf"));
        facture.setCommentaire(rs.getString("commentaire"));

        return facture;
    }
}