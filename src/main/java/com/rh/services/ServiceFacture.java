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
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceFacture implements IServices<Facture> {

    private Connection cnx;
    private ServiceFournisseur serviceFournisseur;
    private TauxChangeService tauxChangeService;

    // Devise de référence pour les calculs
    private static final String DEVISE_REFERENCE = "DT";

    public ServiceFacture() {
        cnx = IsobatDB.getInstance().getCnx();
        serviceFournisseur = new ServiceFournisseur();
        tauxChangeService = new TauxChangeService();
        verifierColonneDevise();
    }

    /**
     * Vérifie et ajoute la colonne devise si elle n'existe pas
     */
    private void verifierColonneDevise() {
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "facture", "devise");
            if (!columns.next()) {
                String query = "ALTER TABLE facture ADD COLUMN devise VARCHAR(10) DEFAULT 'DT'";
                try (Statement stmt = cnx.createStatement()) {
                    stmt.execute(query);
                    System.out.println("✅ Colonne 'devise' ajoutée à la table facture");

                    String updateQuery = "UPDATE facture SET devise = 'DT' WHERE devise IS NULL";
                    stmt.execute(updateQuery);
                    System.out.println("✅ Factures existantes mises à jour avec devise 'DT'");
                }
            } else {
                System.out.println("✅ Colonne 'devise' déjà présente dans la table facture");
            }
            columns.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la colonne devise : " + e.getMessage());
        }
    }

    @Override
    public void add(Facture facture) {
        if (facture.getDevise() == null || facture.getDevise().isEmpty()) {
            facture.setDevise("DT");
        }

        String query = "INSERT INTO facture (numero_facture, id_fournisseur, montant_ht, montant_tva, " +
                "montant_ttc, devise, date_facture, date_echeance, statut, mode_paiement, fichier_pdf, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setInt(2, facture.getIdFournisseur());
            pstmt.setDouble(3, facture.getMontantHt());
            pstmt.setDouble(4, facture.getMontantTva());
            pstmt.setDouble(5, facture.getMontantTtc());
            pstmt.setString(6, facture.getDevise());
            pstmt.setDate(7, Date.valueOf(facture.getDateFacture()));

            if (facture.getDateEcheance() != null) {
                pstmt.setDate(8, Date.valueOf(facture.getDateEcheance()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            pstmt.setString(9, facture.getStatutLabel());
            pstmt.setString(10, facture.getModePaiementLabel());
            pstmt.setString(11, facture.getFichierPdf());
            pstmt.setString(12, facture.getCommentaire());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    facture.setIdFacture(generatedKeys.getInt(1));
                }
            }

            System.out.println("✅ Facture ajoutée : " + facture.getNumeroFacture() + " - Devise: " + facture.getDevise());

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
        if (facture.getDevise() == null || facture.getDevise().isEmpty()) {
            facture.setDevise("DT");
        }

        String query = "UPDATE facture SET numero_facture = ?, id_fournisseur = ?, montant_ht = ?, " +
                "montant_tva = ?, montant_ttc = ?, devise = ?, date_facture = ?, date_echeance = ?, " +
                "statut = ?, mode_paiement = ?, fichier_pdf = ?, commentaire = ? WHERE id_facture = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setInt(2, facture.getIdFournisseur());
            pstmt.setDouble(3, facture.getMontantHt());
            pstmt.setDouble(4, facture.getMontantTva());
            pstmt.setDouble(5, facture.getMontantTtc());
            pstmt.setString(6, facture.getDevise());
            pstmt.setDate(7, Date.valueOf(facture.getDateFacture()));

            if (facture.getDateEcheance() != null) {
                pstmt.setDate(8, Date.valueOf(facture.getDateEcheance()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            pstmt.setString(9, facture.getStatutLabel());
            pstmt.setString(10, facture.getModePaiementLabel());
            pstmt.setString(11, facture.getFichierPdf());
            pstmt.setString(12, facture.getCommentaire());
            pstmt.setInt(13, facture.getIdFacture());

            pstmt.executeUpdate();
            System.out.println("✅ Facture mise à jour : " + facture.getNumeroFacture() + " - Devise: " + facture.getDevise());

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
            System.out.println("✅ Facture supprimée : " + facture.getNumeroFacture());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calcule le montant total en DT en convertissant toutes les devises
     */
    public double getMontantTotalEnDT() {
        List<Facture> factures = getAll();
        double total = 0;

        for (Facture f : factures) {
            String devise = f.getDevise() != null ? f.getDevise() : "DT";
            double montant = f.getMontantTtc();

            // Convertir en DT si ce n'est pas déjà la devise de référence
            if (!DEVISE_REFERENCE.equals(devise)) {
                montant = tauxChangeService.convertir(montant, devise, DEVISE_REFERENCE);
            }
            total += montant;
        }

        return total;
    }

    /**
     * Calcule les totaux par devise avec conversion en DT pour le total général
     */
    public Map<String, Object> getTotauxAvecConversion() {
        List<Facture> factures = getAll();

        // Totaux par devise (en devise d'origine)
        Map<String, Double> totauxParDevise = factures.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getDevise() != null ? f.getDevise() : "DT",
                        Collectors.summingDouble(Facture::getMontantTtc)
                ));

        // Total en DT (converti)
        double totalEnDT = 0;
        Map<String, Double> totauxConvertes = new java.util.HashMap<>();

        for (Map.Entry<String, Double> entry : totauxParDevise.entrySet()) {
            String devise = entry.getKey();
            double montant = entry.getValue();

            if (DEVISE_REFERENCE.equals(devise)) {
                totalEnDT += montant;
                totauxConvertes.put(devise, montant);
            } else {
                double converti = tauxChangeService.convertir(montant, devise, DEVISE_REFERENCE);
                totalEnDT += converti;
                totauxConvertes.put(devise, montant);
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totauxParDevise", totauxParDevise);
        result.put("totalEnDT", totalEnDT);
        result.put("totauxConvertes", totauxConvertes);
        result.put("nombreFactures", factures.size());

        return result;
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

    public List<Facture> filterByDevise(String devise) {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture WHERE devise = ? ORDER BY date_facture DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, devise);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Facture facture = extractFromResultSet(rs);
                facture.setFournisseur(serviceFournisseur.findById(facture.getIdFournisseur()));
                factures.add(facture);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage par devise : " + e.getMessage());
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

    public List<String> getDevisesUtilisees() {
        List<String> devises = new ArrayList<>();
        String query = "SELECT DISTINCT devise FROM facture WHERE devise IS NOT NULL ORDER BY devise";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                devises.add(rs.getString("devise"));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des devises : " + e.getMessage());
            e.printStackTrace();
        }

        return devises;
    }

    private Facture extractFromResultSet(ResultSet rs) throws SQLException {
        Facture facture = new Facture();
        facture.setIdFacture(rs.getInt("id_facture"));
        facture.setNumeroFacture(rs.getString("numero_facture"));
        facture.setIdFournisseur(rs.getInt("id_fournisseur"));
        facture.setMontantHt(rs.getDouble("montant_ht"));
        facture.setMontantTva(rs.getDouble("montant_tva"));
        facture.setMontantTtc(rs.getDouble("montant_ttc"));

        String devise = rs.getString("devise");
        facture.setDevise(devise != null ? devise : "DT");

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

    public String getDeviseReference() {
        return DEVISE_REFERENCE;
    }
}