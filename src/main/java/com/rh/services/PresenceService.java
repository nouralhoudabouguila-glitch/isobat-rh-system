package com.rh.services;

import com.rh.models.Departement;
import com.rh.models.Employe;
import com.rh.models.Presence;
import com.rh.models.Presence.Statut;
import com.rh.utils.IsobatDB;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class PresenceService {

    private final Connection cnx;
    private final EmployeService employeService;

    public PresenceService() {
        this.cnx = IsobatDB.getInstance().getCnx();
        this.employeService = new EmployeService();
    }

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Presence> getAll() {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT * FROM presence ORDER BY date_presence DESC, employe_id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

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

    public void delete(int id) {
        String sql = "DELETE FROM presence WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IMPORT EXCEL - VERSION CORRIGÉE
    // ══════════════════════════════════════════════════════════════════════════

    public static class ImportResult {
        public int total;
        public int importees;
        public int erreurs;
        public int employes;
        public List<String> anomalies = new ArrayList<>();
        public List<Presence> presences = new ArrayList<>();
        public int presents;
        public int retards;
        public int absents;
        public int incomplets;
    }

    public ImportResult importerExcel(File fichier, Departement departement) {
        ImportResult result = new ImportResult();
        LocalDate dateImport = LocalDate.now();

        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook workbook = createWorkbook(fichier, fis)) {

            Sheet sheet = workbook.getSheet("Enre. de perf. de carte");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            System.out.println("📄 Lecture de l'onglet: " + sheet.getSheetName());

            Map<Integer, String> employeNoms = new HashMap<>();
            Map<Integer, List<LocalTime>> horaires = new HashMap<>();

            int currentId = -1;
            int ligneNum = 0;

            for (Row row : sheet) {
                ligneNum++;
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                String firstVal = getCellStringValue(firstCell);

                // ── Détection ligne ID ──────────────────────────────────
                if ("ID".equalsIgnoreCase(firstVal)) {
                    // L'ID est dans la colonne 3 (index 2)
                    Cell idCell = row.getCell(2);
                    if (idCell != null) {
                        String idStr = getCellStringValue(idCell);
                        try {
                            if (idStr.matches("\\d+")) {
                                currentId = Integer.parseInt(idStr);
                                System.out.println("🔍 ID trouvé ligne " + ligneNum + ": " + currentId);
                            }
                        } catch (Exception e) {
                            System.out.println("⚠️ Erreur lecture ID: " + idStr);
                            currentId = -1;
                        }
                    }

                    // Le nom est dans la colonne 10 (index 9) ou 9 (index 8)
                    Cell nomCell = row.getCell(9);
                    if (nomCell == null || getCellStringValue(nomCell).isEmpty()) {
                        nomCell = row.getCell(8);
                    }
                    if (nomCell != null) {
                        String nom = getCellStringValue(nomCell);
                        if (!nom.isEmpty() && !"Nom".equalsIgnoreCase(nom)) {
                            employeNoms.put(currentId, nom.trim());
                            System.out.println("📝 Nom trouvé: " + currentId + " -> " + nom);
                        }
                    }
                    continue;
                }

                // ── Ligne avec des horaires ──────────────────────────────
                List<LocalTime> times = extractAllTimesFromRow(row);
                if (!times.isEmpty() && currentId > 0) {
                    horaires.put(currentId, times);
                    System.out.println("⏰ Horaires pour ID " + currentId + ": " + times.size() + " heures");
                }
            }

            System.out.println("📊 Total IDs trouvés: " + horaires.size());

            // ── Créer les présences ──────────────────────────────────────
            for (Map.Entry<Integer, List<LocalTime>> entry : horaires.entrySet()) {
                int employeId = entry.getKey();
                List<LocalTime> times = entry.getValue();
                String nom = employeNoms.getOrDefault(employeId, "");

                Employe emp = employeService.findById(employeId);
                if (emp == null) {
                    result.anomalies.add("❌ Employé ID " + employeId + " (" + nom + ") introuvable en BDD");
                    result.erreurs++;
                    continue;
                }

                LocalTime arrivee = times.size() > 0 ? times.get(0) : null;
                LocalTime debPause = times.size() > 1 ? times.get(1) : null;
                LocalTime finPause = times.size() > 2 ? times.get(2) : null;
                LocalTime depart = times.size() > 3 ? times.get(3) : null;

                if (arrivee == null) {
                    result.anomalies.add("⚠️ Pas d'heure d'arrivée pour: " + emp.getNom() + " " + emp.getPrenom());
                    continue;
                }

                Presence p = new Presence();
                p.setEmploye(emp);
                p.setDatePresence(dateImport);
                p.setHeureArrivee(arrivee);
                p.setDebutPause(debPause);
                p.setFinPause(finPause);
                p.setHeureDepart(depart);
                p.setSource(Presence.Source.IMPORT);
                p.setDepartement(departement != null ? departement : emp.getDepartement());
                p.setCommentaire("Importé depuis: " + fichier.getName());

                Statut statut = p.detecterStatut();
                p.setStatut(statut);
                p.calculerHeuresTravaillees();

                switch (statut) {
                    case RETARD -> result.retards++;
                    case PRESENT -> result.presents++;
                    case INCOMPLET -> result.incomplets++;
                    case ABSENT_JUSTIFIE, ABSENT_NON_JUSTIFIE -> result.absents++;
                }

                if (statut == Statut.RETARD) {
                    result.anomalies.add("⏰ Retard: " + emp.getNom() + " " + emp.getPrenom() + " — " + p.getHeureArriveeStr());
                }
                if (statut == Statut.INCOMPLET) {
                    result.anomalies.add("⚠️ Incomplet: " + emp.getNom() + " " + emp.getPrenom());
                }

                result.presences.add(p);
                result.total++;
            }

            for (Presence p : result.presences) {
                add(p);
                result.importees++;
            }

            result.employes = (int) result.presences.stream()
                    .map(p -> p.getEmploye().getId())
                    .distinct().count();

            System.out.println("✅ Import terminé: " + result.importees + " présences importées");

        } catch (Exception e) {
            e.printStackTrace();
            result.anomalies.add("❌ Erreur lecture fichier: " + e.getMessage());
            result.erreurs++;
        }

        return result;
    }

    // ── Méthodes d'extraction ────────────────────────────────────────────────

    private List<LocalTime> extractAllTimesFromRow(Row row) {
        List<LocalTime> allTimes = new ArrayList<>();
        if (row == null) return allTimes;

        for (int col = 1; col <= 10; col++) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                List<LocalTime> times = extractTimesFromCell(cell);
                allTimes.addAll(times);
            }
        }
        return allTimes;
    }

    private List<LocalTime> extractTimesFromCell(Cell cell) {
        List<LocalTime> times = new ArrayList<>();
        if (cell == null) return times;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double val = cell.getNumericCellValue();
                if (val > 0 && val < 1) {
                    int totalMinutes = (int) Math.round(val * 24 * 60);
                    int h = totalMinutes / 60;
                    int m = totalMinutes % 60;
                    if (h >= 0 && h < 24 && m >= 0 && m < 60) {
                        times.add(LocalTime.of(h, m));
                    }
                }
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                Pattern pattern = Pattern.compile("\\d{2}:\\d{2}");
                java.util.regex.Matcher matcher = pattern.matcher(val);

                while (matcher.find()) {
                    String timeStr = matcher.group();
                    try {
                        String[] parts = timeStr.split(":");
                        int h = Integer.parseInt(parts[0]);
                        int m = Integer.parseInt(parts[1]);
                        if (h >= 0 && h < 24 && m >= 0 && m < 60) {
                            times.add(LocalTime.of(h, m));
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return times;
    }

    private Workbook createWorkbook(File fichier, FileInputStream fis) throws Exception {
        String name = fichier.getName().toLowerCase();
        if (name.endsWith(".xlsx")) return new XSSFWorkbook(fis);
        if (name.endsWith(".xls")) return new HSSFWorkbook(fis);
        throw new IllegalArgumentException("Format non supporté");
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

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

    public int countRetards(int employeId, int mois, int annee) {
        String sql = "SELECT COUNT(*) FROM presence WHERE employe_id=? " +
                "AND MONTH(date_presence)=? AND YEAR(date_presence)=? AND statut='RETARD'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ps.setInt(2, mois);
            ps.setInt(3, annee);
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