package com.rh.services;

import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.models.Presence;
import com.rh.models.Presence.Statut;
import com.rh.utils.IsobatDB;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresenceService {

    private final Connection cnx;
    private final EmployeService employeService;

    public PresenceService() {
        this.cnx = IsobatDB.getInstance().getCnx();
        this.employeService = new EmployeService();
    }

    // ── ADD ───────────────────────────────────────────────────────────────────

    public void add(Presence p) {
        String sql = "INSERT INTO presence " +
                "(employe_id, date_presence, heure_arrivee, debut_pause, fin_pause, " +
                " heure_depart, heures_travaillees, statut, source, commentaire, departement) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "heure_arrivee=VALUES(heure_arrivee), debut_pause=VALUES(debut_pause), " +
                "fin_pause=VALUES(fin_pause), heure_depart=VALUES(heure_depart), " +
                "heures_travaillees=VALUES(heures_travaillees), statut=VALUES(statut), " +
                "source=VALUES(source), commentaire=VALUES(commentaire)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, p.getEmploye().getId());
            ps.setDate(2, Date.valueOf(p.getDatePresence()));
            ps.setTime(3, p.getHeureArrivee() != null ? Time.valueOf(p.getHeureArrivee()) : null);
            ps.setTime(4, p.getDebutPause() != null ? Time.valueOf(p.getDebutPause()) : null);
            ps.setTime(5, p.getFinPause() != null ? Time.valueOf(p.getFinPause()) : null);
            ps.setTime(6, p.getHeureDepart() != null ? Time.valueOf(p.getHeureDepart()) : null);
            ps.setDouble(7, p.calculerHeuresTravaillees());
            ps.setString(8, p.getStatut().name());
            ps.setString(9, p.getSource().name());
            ps.setString(10, p.getCommentaire());
            ps.setString(11, p.getDepartement() != null ? p.getDepartement().name() : null);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────

    public List<Presence> getAll() {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT * FROM presence ORDER BY date_presence DESC, employe_id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET PAR DEPARTEMENT ───────────────────────────────────────────────────

    public List<Presence> getByDepartement(Departement dept, LocalDate dateDebut, LocalDate dateFin) {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT p.* FROM presence p " +
                "JOIN employe e ON p.employe_id = e.id " +
                "WHERE e.departement = ? AND p.date_presence BETWEEN ? AND ? " +
                "ORDER BY p.date_presence DESC, e.nom";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, dept.name());
            ps.setDate(2, Date.valueOf(dateDebut));
            ps.setDate(3, Date.valueOf(dateFin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET PAR DATE ──────────────────────────────────────────────────────────

    public List<Presence> getByDate(LocalDate date) {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT * FROM presence WHERE date_presence = ? ORDER BY employe_id";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET PAR EMPLOYE + PERIODE ─────────────────────────────────────────────

    public List<Presence> getByEmployePeriode(int employeId, LocalDate debut, LocalDate fin) {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT * FROM presence WHERE employe_id=? AND date_presence BETWEEN ? AND ? ORDER BY date_presence";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ps.setDate(2, Date.valueOf(debut));
            ps.setDate(3, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public void update(Presence p) {
        String sql = "UPDATE presence SET heure_arrivee=?, debut_pause=?, fin_pause=?, " +
                "heure_depart=?, heures_travaillees=?, statut=?, source=?, commentaire=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTime(1, p.getHeureArrivee() != null ? Time.valueOf(p.getHeureArrivee()) : null);
            ps.setTime(2, p.getDebutPause() != null ? Time.valueOf(p.getDebutPause()) : null);
            ps.setTime(3, p.getFinPause() != null ? Time.valueOf(p.getFinPause()) : null);
            ps.setTime(4, p.getHeureDepart() != null ? Time.valueOf(p.getHeureDepart()) : null);
            ps.setDouble(5, p.calculerHeuresTravaillees());
            ps.setString(6, p.getStatut().name());
            ps.setString(7, Presence.Source.MANUEL.name());
            ps.setString(8, p.getCommentaire());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void delete(int id) {
        String sql = "DELETE FROM presence WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IMPORT EXCEL BADGEUSE
    // Format attendu (d'après la photo) :
    //   Ligne "ID" + numéro → identifiant employé (colonne B)
    //   Ligne "Nom" → nom employé (colonne I ou J)
    //   Lignes horaires → colonnes B,C,D,E = arrivee, debPause, finPause, depart
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Résultat d'un import Excel.
     */
    public static class ImportResult {
        public int total;
        public int importees;
        public int erreurs;
        public int employes;
        public List<String> anomalies = new ArrayList<>();
        public List<Presence> presences = new ArrayList<>();
    }

    /**
     * Importe le fichier Excel de la badgeuse.
     * Retourne un ImportResult avec le bilan.
     */
    public ImportResult importerExcel(File fichier, Departement departement) {
        ImportResult result = new ImportResult();

        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Cherche l'onglet "Stat. de présence" ou le premier onglet
            Sheet sheet = workbook.getSheet("Stat. de présence");
            if (sheet == null) sheet = workbook.getSheetAt(0);

            int employeIdActuel = -1;
            String nomEmployeActuel = null;
            int colBaseGauche = 1; // colonne B (index 1) = premier pointage

            for (Row row : sheet) {
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                String firstVal = getCellStringValue(firstCell);

                // ── Ligne ID ──────────────────────────────────────────────
                if ("ID".equalsIgnoreCase(firstVal)) {
                    Cell idCell = row.getCell(1);
                    if (idCell != null) {
                        try {
                            employeIdActuel = (int) idCell.getNumericCellValue();
                        } catch (Exception e) {
                            try { employeIdActuel = Integer.parseInt(getCellStringValue(idCell)); }
                            catch (Exception ex) { employeIdActuel = -1; }
                        }
                    }
                    continue;
                }

                // ── Ligne Nom ─────────────────────────────────────────────
                // Dans le sheet ISOBAT, "Nom" est en colonne H (index 7) ou I (8)
                Cell nomCell8 = row.getCell(8);
                if (nomCell8 != null && !getCellStringValue(nomCell8).isEmpty()
                        && !"Nom".equalsIgnoreCase(getCellStringValue(nomCell8))) {
                    nomEmployeActuel = getCellStringValue(nomCell8).trim();
                    continue;
                }

                // ── Lignes horaires ───────────────────────────────────────
                if (employeIdActuel > 0 && contientHoraires(row)) {
                    // Récupère l'employé depuis la BDD
                    Employe emp = employeService.findById(employeIdActuel);
                    if (emp == null) {
                        result.anomalies.add("Employé ID " + employeIdActuel + " introuvable en BDD");
                        result.erreurs++;
                        continue;
                    }

                    // Récupère les horaires de la ligne
                    // Colonnes B,C,D,E = index 1,2,3,4 (côté gauche du sheet)
                    LocalTime arrivee    = parseTimeFromCell(row.getCell(1));
                    LocalTime debPause   = parseTimeFromCell(row.getCell(2));
                    LocalTime finPause   = parseTimeFromCell(row.getCell(3));
                    LocalTime depart     = parseTimeFromCell(row.getCell(4));

                    // Si la ligne de gauche est vide, essaie colonnes H,I,J,K (index 7,8,9,10)
                    if (arrivee == null) {
                        arrivee  = parseTimeFromCell(row.getCell(7));
                        debPause = parseTimeFromCell(row.getCell(8));
                        finPause = parseTimeFromCell(row.getCell(9));
                        depart   = parseTimeFromCell(row.getCell(10));
                    }

                    if (arrivee == null && depart == null) continue;

                    Presence p = new Presence();
                    p.setEmploye(emp);
                    p.setDatePresence(LocalDate.now()); // sera précisée si la date est dans le fichier
                    p.setHeureArrivee(arrivee);
                    p.setDebutPause(debPause);
                    p.setFinPause(finPause);
                    p.setHeureDepart(depart);
                    p.setSource(Presence.Source.IMPORT);
                    p.setDepartement(departement != null ? departement : emp.getDepartement());

                    // Détecte le statut automatiquement
                    p.setStatut(p.detecterStatut());
                    p.calculerHeuresTravaillees();

                    // Signale les anomalies
                    if (p.getStatut() == Statut.RETARD)
                        result.anomalies.add("Retard : " + emp.getNom() + emp.getPrenom() + " — " + p.getHeureArriveeStr());
                    if (p.getStatut() == Statut.INCOMPLET)
                        result.anomalies.add("Incomplet : " + emp.getNom() + emp.getPrenom());

                    result.presences.add(p);
                    result.total++;
                }
            }

            // Sauvegarde tout en BDD
            for (Presence p : result.presences) {
                add(p);
                result.importees++;
            }

            // Compte les employés distincts
            result.employes = (int) result.presences.stream()
                    .map(p -> p.getEmploye().getId())
                    .distinct().count();

        } catch (Exception e) {
            e.printStackTrace();
            result.anomalies.add("Erreur lecture fichier : " + e.getMessage());
        }

        return result;
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    /**
     * Compte les présences du jour par statut pour le dashboard.
     */
    public Map<Statut, Long> getStatsJour(LocalDate date) {
        Map<Statut, Long> stats = new HashMap<>();
        String sql = "SELECT statut, COUNT(*) as total FROM presence WHERE date_presence=? GROUP BY statut";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    Statut s = Statut.valueOf(rs.getString("statut"));
                    stats.put(s, rs.getLong("total"));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    /**
     * Retourne le nombre de retards pour un employé sur un mois.
     */
    public int countRetards(int employeId, int mois, int annee) {
        String sql = "SELECT COUNT(*) FROM presence WHERE employe_id=? " +
                "AND MONTH(date_presence)=? AND YEAR(date_presence)=? AND statut='RETARD'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId); ps.setInt(2, mois); ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── MAP ResultSet ─────────────────────────────────────────────────────────

    private Presence map(ResultSet rs) throws SQLException {
        Presence p = new Presence();
        p.setId(rs.getInt("id"));

        Employe emp = employeService.findById(rs.getInt("employe_id"));
        p.setEmploye(emp);

        Date d = rs.getDate("date_presence");
        if (d != null) p.setDatePresence(d.toLocalDate());

        Time arr = rs.getTime("heure_arrivee");
        Time dep = rs.getTime("debut_pause");
        Time fin = rs.getTime("fin_pause");
        Time dpt = rs.getTime("heure_depart");
        if (arr != null) p.setHeureArrivee(arr.toLocalTime());
        if (dep != null) p.setDebutPause(dep.toLocalTime());
        if (fin != null) p.setFinPause(fin.toLocalTime());
        if (dpt != null) p.setHeureDepart(dpt.toLocalTime());

        p.setHeuresTravaillees(rs.getDouble("heures_travaillees"));

        try { p.setStatut(Statut.valueOf(rs.getString("statut"))); }
        catch (Exception e) { p.setStatut(Statut.PRESENT); }

        try { p.setSource(Presence.Source.valueOf(rs.getString("source"))); }
        catch (Exception e) { p.setSource(Presence.Source.IMPORT); }

        p.setCommentaire(rs.getString("commentaire"));

        try {
            String dept = rs.getString("departement");
            if (dept != null) p.setDepartement(Departement.valueOf(dept));
        } catch (Exception ignored) {}

        return p;
    }

    // ── Helpers Excel ─────────────────────────────────────────────────────────

    private boolean contientHoraires(Row row) {
        for (int i = 1; i <= 11; i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellType() == CellType.NUMERIC) {
                double val = c.getNumericCellValue();
                // Les heures sont stockées comme fraction de jour (0-1) dans Excel
                if (val > 0 && val < 1) return true;
            }
            if (c != null && c.getCellType() == CellType.STRING) {
                String v = c.getStringCellValue().trim();
                if (v.matches("\\d{1,2}:\\d{2}")) return true;
            }
        }
        return false;
    }

    private LocalTime parseTimeFromCell(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                // Excel stocke les heures comme fraction de jour
                double val = cell.getNumericCellValue();
                if (val <= 0 || val >= 1) return null;
                int totalMinutes = (int) Math.round(val * 24 * 60);
                int h = totalMinutes / 60;
                int m = totalMinutes % 60;
                if (h >= 0 && h < 24 && m >= 0 && m < 60)
                    return LocalTime.of(h, m);
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (s.matches("\\d{1,2}:\\d{2}")) {
                    String[] parts = s.split(":");
                    return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }
}