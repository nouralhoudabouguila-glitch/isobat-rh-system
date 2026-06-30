package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.Avance;
import com.rh.models.Avance.StatutAvance;
import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceAvance implements IServices<Avance> {

    private Connection cnx;

    public ServiceAvance() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    // ── CRUD ──

    @Override
    public void add(Avance avance) {
        String query = "INSERT INTO avance (id_employe, montant, motif, date_demande, date_validation, statut, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, avance.getIdEmploye());
            pstmt.setDouble(2, avance.getMontant());
            pstmt.setString(3, avance.getMotif());
            pstmt.setDate(4, Date.valueOf(avance.getDateDemande()));

            if (avance.getDateValidation() != null) {
                pstmt.setDate(5, Date.valueOf(avance.getDateValidation()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setString(6, avance.getStatutLabel());
            pstmt.setString(7, avance.getCommentaire());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    avance.setIdAvance(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'avance : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Avance> getAll() {
        List<Avance> avances = new ArrayList<>();
        String query = "SELECT a.*, e.nom, e.prenom, e.profil, e.Departement FROM avance a " +
                "LEFT JOIN employe e ON a.id_employe = e.id " +
                "ORDER BY a.id_avance DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Avance avance = extractAvanceFromResultSet(rs);
                avances.add(avance);
            }

            System.out.println("Nombre d'avances récupérées : " + avances.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des avances : " + e.getMessage());
            e.printStackTrace();
        }

        return avances;
    }

    @Override
    public void update(Avance avance) {
        String query = "UPDATE avance SET id_employe = ?, montant = ?, motif = ?, " +
                "date_demande = ?, date_validation = ?, statut = ?, commentaire = ? " +
                "WHERE id_avance = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, avance.getIdEmploye());
            pstmt.setDouble(2, avance.getMontant());
            pstmt.setString(3, avance.getMotif());
            pstmt.setDate(4, Date.valueOf(avance.getDateDemande()));

            if (avance.getDateValidation() != null) {
                pstmt.setDate(5, Date.valueOf(avance.getDateValidation()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setString(6, avance.getStatutLabel());
            pstmt.setString(7, avance.getCommentaire());
            pstmt.setInt(8, avance.getIdAvance());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'avance : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Avance avance) {
        String query = "DELETE FROM avance WHERE id_avance = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, avance.getIdAvance());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'avance : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Méthodes pour récupérer les employés ──

    /**
     * Récupère les employés d'un département
     * @param departement Le nom du département (ex: "Centre d'appel B2B" ou "BUREAU_ETUDE")
     * @return Liste des employés complets
     */
    public List<Employe> getEmployesByDepartement(String departement) {
        List<Employe> employes = new ArrayList<>();

        // Si le paramètre est le label (ex: "Centre d'appel B2B"), trouver l'enum correspondant
        String enumName = departement;
        for (Departement d : Departement.values()) {
            if (d.toString().equals(departement)) {
                enumName = d.name();
                break;
            }
        }

        System.out.println("Recherche des employés pour : " + departement + " (nom enum: " + enumName + ")");

        String query = "SELECT * FROM employe WHERE Departement = ? ORDER BY nom, prenom";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, enumName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Employe employe = extractEmployeFromResultSet(rs);
                employes.add(employe);
            }

            System.out.println("Employés trouvés : " + employes.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des employés : " + e.getMessage());
            e.printStackTrace();
        }

        return employes;
    }

    /**
     * Récupère les employés d'un département (version avec enum)
     * @param departement Le département (enum)
     * @return Liste des employés du département
     */
    public List<Employe> getEmployesByDepartement(Departement departement) {
        return getEmployesByDepartement(departement.toString());
    }

    public Employe getEmployeById(int id) {
        String query = "SELECT * FROM employe WHERE id = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractEmployeFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'employé : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Employe> getAllEmployes() {
        List<Employe> employes = new ArrayList<>();
        String query = "SELECT * FROM employe ORDER BY nom, prenom";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Employe employe = extractEmployeFromResultSet(rs);
                employes.add(employe);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des employés : " + e.getMessage());
            e.printStackTrace();
        }

        return employes;
    }

    public List<String> getDepartements() {
        List<String> departements = new ArrayList<>();
        String query = "SELECT DISTINCT Departement FROM employe ORDER BY Departement";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                departements.add(rs.getString("Departement"));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des départements : " + e.getMessage());
            e.printStackTrace();
        }

        return departements;
    }

    // ── Méthodes pour les avances ──

    public Avance findById(int id) {
        String query = "SELECT a.*, e.nom, e.prenom, e.profil, e.Departement FROM avance a " +
                "LEFT JOIN employe e ON a.id_employe = e.id " +
                "WHERE a.id_avance = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAvanceFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'avance : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Avance> findByEmploye(int idEmploye) {
        List<Avance> avances = new ArrayList<>();
        String query = "SELECT a.*, e.nom, e.prenom, e.profil, e.Departement FROM avance a " +
                "LEFT JOIN employe e ON a.id_employe = e.id " +
                "WHERE a.id_employe = ? ORDER BY a.date_demande DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, idEmploye);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                avances.add(extractAvanceFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par employé : " + e.getMessage());
            e.printStackTrace();
        }

        return avances;
    }

    public List<Avance> filterByStatut(String statut) {
        List<Avance> avances = new ArrayList<>();
        String query = "SELECT a.*, e.nom, e.prenom, e.profil, e.Departement FROM avance a " +
                "LEFT JOIN employe e ON a.id_employe = e.id " +
                "WHERE a.statut = ? ORDER BY a.date_demande DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                avances.add(extractAvanceFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
            e.printStackTrace();
        }

        return avances;
    }

    public List<Avance> search(String keyword) {
        List<Avance> avances = new ArrayList<>();
        String query = "SELECT a.*, e.nom, e.prenom, e.profil, e.Departement FROM avance a " +
                "LEFT JOIN employe e ON a.id_employe = e.id " +
                "WHERE e.nom LIKE ? OR e.prenom LIKE ? OR a.motif LIKE ? " +
                "ORDER BY a.date_demande DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                avances.add(extractAvanceFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return avances;
    }

    public List<Avance> findByDateRange(LocalDate dateDebut, LocalDate dateFin) {
        List<Avance> avances = new ArrayList<>();
        String query = "SELECT a.*, e.nom, e.prenom, e.profil, e.Departement FROM avance a " +
                "LEFT JOIN employe e ON a.id_employe = e.id " +
                "WHERE a.date_demande BETWEEN ? AND ? ORDER BY a.date_demande DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(dateDebut));
            pstmt.setDate(2, Date.valueOf(dateFin));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                avances.add(extractAvanceFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par date : " + e.getMessage());
            e.printStackTrace();
        }

        return avances;
    }

    // ── Méthodes pour les statistiques ──

    public int countByStatut(String statut) {
        String query = "SELECT COUNT(*) FROM avance WHERE statut = ?";

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

    public double getTotalValide() {
        String query = "SELECT SUM(montant) FROM avance WHERE statut = 'Validée'";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du total : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public int countAll() {
        String query = "SELECT COUNT(*) FROM avance";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // ── Méthodes pour valider/refuser une demande ──

    public boolean validerAvance(int idAvance) {
        String query = "UPDATE avance SET statut = 'Validée', date_validation = ? WHERE id_avance = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, idAvance);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la validation de l'avance : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean refuserAvance(int idAvance) {
        String query = "UPDATE avance SET statut = 'Refusée', date_validation = ? WHERE id_avance = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, idAvance);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors du refus de l'avance : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Méthodes d'extraction ──

    private Avance extractAvanceFromResultSet(ResultSet rs) throws SQLException {
        Avance avance = new Avance();
        avance.setIdAvance(rs.getInt("id_avance"));
        avance.setIdEmploye(rs.getInt("id_employe"));
        avance.setMontant(rs.getDouble("montant"));
        avance.setMotif(rs.getString("motif"));

        Date dateDemande = rs.getDate("date_demande");
        if (dateDemande != null) {
            avance.setDateDemande(dateDemande.toLocalDate());
        }

        Date dateValidation = rs.getDate("date_validation");
        if (dateValidation != null) {
            avance.setDateValidation(dateValidation.toLocalDate());
        }

        avance.setStatut(rs.getString("statut"));
        avance.setCommentaire(rs.getString("commentaire"));

        Employe employe = new Employe();
        employe.setId(rs.getInt("id_employe"));
        employe.setNom(rs.getString("nom"));
        employe.setPrenom(rs.getString("prenom"));
        employe.setProfil(rs.getString("profil"));

        String dept = rs.getString("Departement");
        if (dept != null) {
            for (Departement d : Departement.values()) {
                if (d.name().equals(dept)) {
                    employe.setDepartement(d);
                    break;
                }
            }
        }

        avance.setEmploye(employe);

        return avance;
    }

    private Employe extractEmployeFromResultSet(ResultSet rs) throws SQLException {
        Employe employe = new Employe();
        employe.setId(rs.getInt("id"));
        employe.setNom(rs.getString("nom"));
        employe.setPrenom(rs.getString("prenom"));
        employe.setDateNaissance(rs.getDate("dateNaissance") != null ? rs.getDate("dateNaissance").toLocalDate() : null);
        employe.setSituationFamiliale(rs.getString("situationFamiliale"));
        employe.setCin(rs.getString("cin"));
        employe.setTelephone(rs.getString("telephone"));
        employe.setSalaireMensuelNet(rs.getDouble("salaireMensuelNet"));
        employe.setProfil(rs.getString("profil"));
        employe.setTypeContrat(rs.getString("typeContrat"));
        employe.setStatut(rs.getString("statut"));
        employe.setDateEmbauche(rs.getDate("dateEmbauche") != null ? rs.getDate("dateEmbauche").toLocalDate() : null);
        employe.setnRIB(rs.getString("nRIB"));
        employe.setnCNSS(rs.getString("nCNSS"));

        String dept = rs.getString("Departement");
        if (dept != null) {
            for (Departement d : Departement.values()) {
                if (d.name().equals(dept)) {
                    employe.setDepartement(d);
                    break;
                }
            }
        }

        return employe;
    }
}