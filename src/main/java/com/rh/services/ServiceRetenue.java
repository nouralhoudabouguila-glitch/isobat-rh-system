package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.models.Retenue;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceRetenue implements IServices<Retenue> {

    private Connection cnx;

    public ServiceRetenue() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    // ── CRUD ──

    @Override
    public void add(Retenue retenue) {
        String query = "INSERT INTO retenue (id_employe, montant, motif, date_retenue, commentaire, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, retenue.getIdEmploye());
            pstmt.setDouble(2, retenue.getMontant());
            pstmt.setString(3, retenue.getMotif());
            pstmt.setDate(4, Date.valueOf(retenue.getDateRetenue()));
            pstmt.setString(5, retenue.getCommentaire());
            pstmt.setTimestamp(6, Timestamp.valueOf(retenue.getDateCreation()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    retenue.setIdRetenue(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la retenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Retenue> getAll() {
        List<Retenue> retenues = new ArrayList<>();
        String query = "SELECT r.*, e.nom, e.prenom, e.profil, e.Departement FROM retenue r " +
                "LEFT JOIN employe e ON r.id_employe = e.id " +
                "ORDER BY r.id_retenue DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Retenue retenue = extractFromResultSet(rs);
                retenues.add(retenue);
            }

            System.out.println("Nombre de retenues récupérées : " + retenues.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des retenues : " + e.getMessage());
            e.printStackTrace();
        }

        return retenues;
    }

    @Override
    public void update(Retenue retenue) {
        String query = "UPDATE retenue SET id_employe = ?, montant = ?, motif = ?, " +
                "date_retenue = ?, commentaire = ? WHERE id_retenue = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, retenue.getIdEmploye());
            pstmt.setDouble(2, retenue.getMontant());
            pstmt.setString(3, retenue.getMotif());
            pstmt.setDate(4, Date.valueOf(retenue.getDateRetenue()));
            pstmt.setString(5, retenue.getCommentaire());
            pstmt.setInt(6, retenue.getIdRetenue());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la retenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Retenue retenue) {
        String query = "DELETE FROM retenue WHERE id_retenue = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, retenue.getIdRetenue());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la retenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Méthodes pour récupérer les employés ──

    public List<Employe> getEmployesByDepartement(String departement) {
        List<Employe> employes = new ArrayList<>();

        // Trouver le nom de l'enum correspondant
        String enumName = departement;
        for (Departement d : Departement.values()) {
            if (d.toString().equals(departement)) {
                enumName = d.name();
                break;
            }
        }

        String query = "SELECT * FROM employe WHERE Departement = ? ORDER BY nom, prenom";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, enumName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Employe employe = extractEmployeFromResultSet(rs);
                employes.add(employe);
            }

            System.out.println("Employés du département " + departement + " : " + employes.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des employés : " + e.getMessage());
            e.printStackTrace();
        }

        return employes;
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

    // ── Méthodes pour les retenues ──

    public Retenue findById(int id) {
        String query = "SELECT r.*, e.nom, e.prenom, e.profil, e.Departement FROM retenue r " +
                "LEFT JOIN employe e ON r.id_employe = e.id " +
                "WHERE r.id_retenue = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la retenue : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Retenue> findByEmploye(int idEmploye) {
        List<Retenue> retenues = new ArrayList<>();
        String query = "SELECT r.*, e.nom, e.prenom, e.profil, e.Departement FROM retenue r " +
                "LEFT JOIN employe e ON r.id_employe = e.id " +
                "WHERE r.id_employe = ? ORDER BY r.date_retenue DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, idEmploye);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                retenues.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par employé : " + e.getMessage());
            e.printStackTrace();
        }

        return retenues;
    }

    public List<Retenue> findByDateRange(LocalDate dateDebut, LocalDate dateFin) {
        List<Retenue> retenues = new ArrayList<>();
        String query = "SELECT r.*, e.nom, e.prenom, e.profil, e.Departement FROM retenue r " +
                "LEFT JOIN employe e ON r.id_employe = e.id " +
                "WHERE r.date_retenue BETWEEN ? AND ? ORDER BY r.date_retenue DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(dateDebut));
            pstmt.setDate(2, Date.valueOf(dateFin));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                retenues.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par date : " + e.getMessage());
            e.printStackTrace();
        }

        return retenues;
    }

    public List<Retenue> search(String keyword) {
        List<Retenue> retenues = new ArrayList<>();
        String query = "SELECT r.*, e.nom, e.prenom, e.profil, e.Departement FROM retenue r " +
                "LEFT JOIN employe e ON r.id_employe = e.id " +
                "WHERE e.nom LIKE ? OR e.prenom LIKE ? OR r.motif LIKE ? " +
                "ORDER BY r.date_retenue DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                retenues.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return retenues;
    }

    // ── Statistiques ──

    public int countAll() {
        String query = "SELECT COUNT(*) FROM retenue";

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

    public double getTotalRetenues() {
        String query = "SELECT SUM(montant) FROM retenue";

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

    // ── Méthodes d'extraction ──

    private Retenue extractFromResultSet(ResultSet rs) throws SQLException {
        Retenue retenue = new Retenue();
        retenue.setIdRetenue(rs.getInt("id_retenue"));
        retenue.setIdEmploye(rs.getInt("id_employe"));
        retenue.setMontant(rs.getDouble("montant"));
        retenue.setMotif(rs.getString("motif"));

        Date dateRetenue = rs.getDate("date_retenue");
        if (dateRetenue != null) {
            retenue.setDateRetenue(dateRetenue.toLocalDate());
        }

        retenue.setCommentaire(rs.getString("commentaire"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            retenue.setDateCreation(dateCreation.toLocalDateTime());
        } else {
            retenue.setDateCreation(LocalDateTime.now());
        }

        // Créer l'objet Employe
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

        retenue.setEmploye(employe);

        return retenue;
    }

    private Employe extractEmployeFromResultSet(ResultSet rs) throws SQLException {
        Employe employe = new Employe();
        employe.setId(rs.getInt("id"));
        employe.setNom(rs.getString("nom"));
        employe.setPrenom(rs.getString("prenom"));

        Date dateNaissance = rs.getDate("dateNaissance");
        if (dateNaissance != null) {
            employe.setDateNaissance(dateNaissance.toLocalDate());
        }

        employe.setSituationFamiliale(rs.getString("situationFamiliale"));
        employe.setCin(rs.getString("cin"));
        employe.setTelephone(rs.getString("telephone"));
        employe.setSalaireMensuelNet(rs.getDouble("salaireMensuelNet"));
        employe.setProfil(rs.getString("profil"));
        employe.setTypeContrat(rs.getString("typeContrat"));
        employe.setStatut(rs.getString("statut"));

        Date dateEmbauche = rs.getDate("dateEmbauche");
        if (dateEmbauche != null) {
            employe.setDateEmbauche(dateEmbauche.toLocalDate());
        }

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