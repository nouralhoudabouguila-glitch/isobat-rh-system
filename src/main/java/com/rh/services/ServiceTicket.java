package com.rh.services;

import com.rh.interfaces.IServices;
import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.models.TicketEmploye;
import com.rh.utils.IsobatDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServiceTicket implements IServices<TicketEmploye> {

    private Connection cnx;

    public ServiceTicket() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    // ── CRUD ──

    @Override
    public void add(TicketEmploye ticket) {
        String query = "INSERT INTO ticket_employe (id_employe, mois, annee, nb_jours, montant_par_ticket, total, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, ticket.getIdEmploye());
            pstmt.setString(2, ticket.getMois());
            pstmt.setInt(3, ticket.getAnnee());
            pstmt.setInt(4, ticket.getNbJours());
            pstmt.setDouble(5, ticket.getMontantParTicket());
            pstmt.setDouble(6, ticket.getTotal());
            pstmt.setTimestamp(7, Timestamp.valueOf(ticket.getDateCreation()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ticket.setIdTicket(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<TicketEmploye> getAll() {
        List<TicketEmploye> tickets = new ArrayList<>();
        String query = "SELECT t.*, e.nom, e.prenom, e.profil, e.Departement, e.dateEmbauche FROM ticket_employe t " +
                "LEFT JOIN employe e ON t.id_employe = e.id " +
                "ORDER BY t.date_creation DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                TicketEmploye ticket = extractFromResultSet(rs);
                tickets.add(ticket);
            }

            System.out.println("Nombre de tickets récupérés : " + tickets.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tickets : " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    @Override
    public void update(TicketEmploye ticket) {
        String query = "UPDATE ticket_employe SET id_employe = ?, mois = ?, annee = ?, " +
                "nb_jours = ?, montant_par_ticket = ?, total = ? WHERE id_ticket = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, ticket.getIdEmploye());
            pstmt.setString(2, ticket.getMois());
            pstmt.setInt(3, ticket.getAnnee());
            pstmt.setInt(4, ticket.getNbJours());
            pstmt.setDouble(5, ticket.getMontantParTicket());
            pstmt.setDouble(6, ticket.getTotal());
            pstmt.setInt(7, ticket.getIdTicket());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(TicketEmploye ticket) {
        String query = "DELETE FROM ticket_employe WHERE id_ticket = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, ticket.getIdTicket());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du ticket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Méthodes pour récupérer les employés ──

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

    public List<Employe> getEmployesByDepartement(String departement) {
        List<Employe> employes = new ArrayList<>();

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

    public List<Employe> getEmployesAvecAnciennete(int moisMinimum) {
        List<Employe> employes = new ArrayList<>();
        String query = "SELECT * FROM employe WHERE dateEmbauche <= DATE_SUB(CURDATE(), INTERVAL ? MONTH) ORDER BY nom, prenom";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, moisMinimum);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Employe employe = extractEmployeFromResultSet(rs);
                employes.add(employe);
            }

            System.out.println("Employés avec " + moisMinimum + " mois d'ancienneté : " + employes.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des employés : " + e.getMessage());
            e.printStackTrace();
        }

        return employes;
    }

    public List<Employe> getEmployesEligiblesTickets() {
        return getEmployesAvecAnciennete(2);
    }

    // ── GÉNÉRATION AUTOMATIQUE DES TICKETS ──

    /**
     * Génère automatiquement les tickets pour tous les employés éligibles (2 mois d'ancienneté)
     * @param mois Le mois (ex: "Juin")
     * @param annee L'année (ex: 2026)
     * @return Le nombre de tickets générés
     */
    public int genererTicketsPourMois(String mois, int annee) {
        int ticketsGeneres = 0;

        try {
            // Récupérer les employés éligibles (2 mois d'ancienneté)
            List<Employe> employesEligibles = getEmployesEligiblesTickets();
            System.out.println("Employés éligibles trouvés : " + employesEligibles.size());

            // Vérifier si les tickets existent déjà pour ce mois/année
            List<TicketEmploye> ticketsExistants = findByMoisAnnee(mois, annee);

            for (Employe employe : employesEligibles) {
                // Vérifier si l'employé a déjà un ticket pour ce mois
                boolean existeDeja = false;
                for (TicketEmploye t : ticketsExistants) {
                    if (t.getIdEmploye() == employe.getId()) {
                        existeDeja = true;
                        break;
                    }
                }

                if (!existeDeja) {
                    // Calculer le montant selon le département
                    double montantParTicket = getMontantParDepartement(employe.getDepartement());
                    int nbJours = 22; // Nombre de jours travaillés par défaut
                    double total = nbJours * montantParTicket;

                    // Créer le ticket
                    TicketEmploye ticket = new TicketEmploye();
                    ticket.setIdEmploye(employe.getId());
                    ticket.setMois(mois);
                    ticket.setAnnee(annee);
                    ticket.setNbJours(nbJours);
                    ticket.setMontantParTicket(montantParTicket);
                    ticket.setTotal(total);
                    ticket.setDateCreation(LocalDateTime.now());

                    // Sauvegarder
                    add(ticket);
                    ticketsGeneres++;

                    System.out.println("Ticket généré pour : " + employe.getNom() + " " + employe.getPrenom() +
                            " - Montant: " + montantParTicket + " DT - Total: " + total + " DT");
                }
            }

            System.out.println("Total tickets générés : " + ticketsGeneres);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des tickets : " + e.getMessage());
            e.printStackTrace();
        }

        return ticketsGeneres;
    }

    /**
     * Récupère le montant par ticket selon le département
     */
    private double getMontantParDepartement(Departement departement) {
        if (departement == null) {
            return 8.50;
        }

        switch (departement) {
            case CENTRE_APPEL_B2B:
            case CENTRE_APPEL_B2C:
                return 15.00;
            case BUREAU_ETUDE:
                return 11.00;
            case ADMINISTRATION:
                return 30.00;
            default:
                return 8.50;
        }
    }

    // ── Méthodes pour les tickets ──

    public TicketEmploye findById(int id) {
        String query = "SELECT t.*, e.nom, e.prenom, e.profil, e.Departement, e.dateEmbauche FROM ticket_employe t " +
                "LEFT JOIN employe e ON t.id_employe = e.id " +
                "WHERE t.id_ticket = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du ticket : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<TicketEmploye> findByEmploye(int idEmploye) {
        List<TicketEmploye> tickets = new ArrayList<>();
        String query = "SELECT t.*, e.nom, e.prenom, e.profil, e.Departement, e.dateEmbauche FROM ticket_employe t " +
                "LEFT JOIN employe e ON t.id_employe = e.id " +
                "WHERE t.id_employe = ? ORDER BY t.date_creation DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, idEmploye);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tickets.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par employé : " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    public List<TicketEmploye> findByMoisAnnee(String mois, int annee) {
        List<TicketEmploye> tickets = new ArrayList<>();
        String query = "SELECT t.*, e.nom, e.prenom, e.profil, e.Departement, e.dateEmbauche FROM ticket_employe t " +
                "LEFT JOIN employe e ON t.id_employe = e.id " +
                "WHERE t.mois = ? AND t.annee = ? ORDER BY t.date_creation DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, mois);
            pstmt.setInt(2, annee);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tickets.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par mois/année : " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    public List<TicketEmploye> search(String keyword) {
        List<TicketEmploye> tickets = new ArrayList<>();
        String query = "SELECT t.*, e.nom, e.prenom, e.profil, e.Departement, e.dateEmbauche FROM ticket_employe t " +
                "LEFT JOIN employe e ON t.id_employe = e.id " +
                "WHERE e.nom LIKE ? OR e.prenom LIKE ? " +
                "ORDER BY t.date_creation DESC";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tickets.add(extractFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    public int countAll() {
        String query = "SELECT COUNT(*) FROM ticket_employe";

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

    // ── Méthodes d'extraction ──

    private TicketEmploye extractFromResultSet(ResultSet rs) throws SQLException {
        TicketEmploye ticket = new TicketEmploye();
        ticket.setIdTicket(rs.getInt("id_ticket"));
        ticket.setIdEmploye(rs.getInt("id_employe"));
        ticket.setMois(rs.getString("mois"));
        ticket.setAnnee(rs.getInt("annee"));
        ticket.setNbJours(rs.getInt("nb_jours"));
        ticket.setMontantParTicket(rs.getDouble("montant_par_ticket"));
        ticket.setTotal(rs.getDouble("total"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            ticket.setDateCreation(dateCreation.toLocalDateTime());
        }

        Employe employe = new Employe();
        employe.setId(rs.getInt("id_employe"));
        employe.setNom(rs.getString("nom"));
        employe.setPrenom(rs.getString("prenom"));
        employe.setProfil(rs.getString("profil"));

        Date dateEmbauche = rs.getDate("dateEmbauche");
        if (dateEmbauche != null) {
            employe.setDateEmbauche(dateEmbauche.toLocalDate());
        }

        String dept = rs.getString("Departement");
        if (dept != null) {
            for (Departement d : Departement.values()) {
                if (d.name().equals(dept)) {
                    employe.setDepartement(d);
                    break;
                }
            }
        }

        ticket.setEmploye(employe);

        return ticket;
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