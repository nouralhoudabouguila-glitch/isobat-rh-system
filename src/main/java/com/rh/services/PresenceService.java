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
import java.time.YearMonth;
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
    // IMPORT EXCEL - AVEC MAPPING DES IDs POUR LES DEUX SITES
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

        try (FileInputStream fis = new FileInputStream(fichier);
             Workbook workbook = createWorkbook(fichier, fis)) {

            // ── Lire l'onglet "Info. de calendrier" pour le mois ──────────
            Sheet infoSheet = workbook.getSheet("Info. de calendrier");
            YearMonth yearMonth = YearMonth.of(2026, 6);

            if (infoSheet != null) {
                Row dateRow = infoSheet.getRow(1);
                if (dateRow != null) {
                    String dateStr = getCellStringValue(dateRow.getCell(1));
                    if (dateStr.contains("~")) {
                        try {
                            String[] parts = dateStr.split("~");
                            String debutStr = parts[0].trim();
                            if (debutStr.length() >= 10) {
                                LocalDate date = LocalDate.parse(debutStr);
                                yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
                            }
                        } catch (Exception e) {}
                    }
                }
            }

            System.out.println("📅 Mois: " + yearMonth);

            // ── Lire l'onglet "Enre. de perf. de carte" ────────────────────
            Sheet cardSheet = workbook.getSheet("Enre. de perf. de carte");
            if (cardSheet == null) {
                cardSheet = workbook.getSheetAt(0);
            }

            System.out.println("📄 Lecture: " + cardSheet.getSheetName());

            // 🔥 Détecter la ligne des jours
            int dayStartCol = -1;
            int dayEndCol = -1;

            for (Row row : cardSheet) {
                if (row == null) continue;
                for (int col = 0; col < 35; col++) {
                    Cell cell = row.getCell(col);
                    if (cell != null) {
                        String val = getCellStringValue(cell);
                        if ("1".equals(val)) {
                            dayStartCol = col;
                            for (int c = col; c < 35; c++) {
                                Cell c2 = row.getCell(c);
                                if (c2 != null) {
                                    String v2 = getCellStringValue(c2);
                                    if (v2.matches("\\d+")) {
                                        dayEndCol = c;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (dayStartCol != -1) break;
            }

            if (dayStartCol == -1) {
                dayStartCol = 3;
                dayEndCol = 31;
            }

            System.out.println("🔍 Jours: colonnes " + dayStartCol + " à " + dayEndCol);

            // ──────────────────────────────────────────────────────────────────
            // 🔥 MAPPING DES IDS SELON LE DÉPARTEMENT
            // ──────────────────────────────────────────────────────────────────
            // Bureau d'étude   : ID Excel 1-48   → ID Base 1-48   (pas de décalage)
            // Centre d'appel   : ID Excel 1-37   → ID Base 101-137 (décalage +100)
            // ──────────────────────────────────────────────────────────────────

            boolean isCentreAppel = departement == Departement.CENTRE_APPEL_B2B;
            boolean isBureauEtude = departement == Departement.BUREAU_ETUDE;

            // Si département non spécifié, on essaie de deviner par le nom du fichier
            if (departement == null) {
                String fileName = fichier.getName().toLowerCase();
                if (fileName.contains("appel") || fileName.contains("call")) {
                    isCentreAppel = true;
                    isBureauEtude = false;
                } else if (fileName.contains("bureau") || fileName.contains("etude")) {
                    isCentreAppel = false;
                    isBureauEtude = true;
                }
            }

            System.out.println("🏢 Département détecté: " +
                    (isCentreAppel ? "CENTRE D'APPEL (ID base = ID excel + 100)" :
                            isBureauEtude ? "BUREAU D'ÉTUDE (ID base = ID excel)" :
                                    "AUTO"));

            // ──────────────────────────────────────────────────────────────────
            // 🔥 1ère PASSE : Lire TOUS les IDs et Noms du fichier Excel
            // ──────────────────────────────────────────────────────────────────

            Map<Integer, Integer> excelIdToBaseId = new HashMap<>();
            Map<Integer, String> excelIdToName = new HashMap<>();

            for (Row row : cardSheet) {
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                String firstVal = getCellStringValue(firstCell);

                if (firstVal != null && firstVal.equalsIgnoreCase("ID")) {
                    Cell idCell = row.getCell(2);
                    if (idCell == null) idCell = row.getCell(1);
                    if (idCell != null) {
                        String idStr = getCellStringValue(idCell);
                        try {
                            if (idStr.matches("\\d+")) {
                                int excelId = Integer.parseInt(idStr);

                                // 🔥 Calculer l'ID Base selon le département
                                int baseId = excelId;
                                if (isCentreAppel && excelId >= 1 && excelId <= 37) {
                                    baseId = excelId + 100; // 1→101, 2→102, ..., 37→137
                                } else if (isBureauEtude && excelId >= 1 && excelId <= 48) {
                                    baseId = excelId; // 1→1, 2→2, ...
                                } else {
                                    // Mode auto : essayer les deux
                                    Employe emp = employeService.findById(excelId);
                                    if (emp != null) {
                                        baseId = excelId;
                                    } else {
                                        int testId = excelId + 100;
                                        emp = employeService.findById(testId);
                                        if (emp != null) {
                                            baseId = testId;
                                        } else {
                                            result.anomalies.add("❌ ID Excel " + excelId + " non trouvé en base (testé " + excelId + " et " + testId + ")");
                                            continue;
                                        }
                                    }
                                }

                                // 🔥 Vérifier que l'employé existe dans la base
                                Employe emp = employeService.findById(baseId);
                                if (emp != null) {
                                    excelIdToBaseId.put(excelId, baseId);
                                    excelIdToName.put(excelId, emp.getNom().toLowerCase().trim());
                                    System.out.println("🔍 Mapping: Excel " + excelId + " → Base " + baseId + " (" + emp.getNom() + " " + emp.getPrenom() + ")");
                                } else {
                                    result.anomalies.add("❌ ID Base " + baseId + " introuvable pour ID Excel " + excelId);
                                }
                            }
                        } catch (Exception e) {
                            // ignorer
                        }
                    }
                }
            }

            // ──────────────────────────────────────────────────────────────────
            // 🔥 2ème PASSE : Vérifier la cohérence des noms et corriger
            // ──────────────────────────────────────────────────────────────────

            int currentExcelId = -1;
            int currentBaseId = -1;
            Map<Integer, List<PresenceTemp>> employeData = new HashMap<>();

            for (Row row : cardSheet) {
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                String firstVal = getCellStringValue(firstCell);

                if (firstVal != null && firstVal.equalsIgnoreCase("ID")) {
                    Cell idCell = row.getCell(2);
                    if (idCell == null) idCell = row.getCell(1);
                    if (idCell != null) {
                        String idStr = getCellStringValue(idCell);
                        try {
                            if (idStr.matches("\\d+")) {
                                currentExcelId = Integer.parseInt(idStr);
                                if (excelIdToBaseId.containsKey(currentExcelId)) {
                                    currentBaseId = excelIdToBaseId.get(currentExcelId);
                                } else {
                                    currentBaseId = -1;
                                }
                            }
                        } catch (Exception e) {
                            currentBaseId = -1;
                            currentExcelId = -1;
                        }
                    }

                    // 🔥 Lire le nom et vérifier la cohérence
                    Cell nomCell = row.getCell(9);
                    if (nomCell == null) nomCell = row.getCell(8);
                    if (nomCell != null) {
                        String nomFichier = getCellStringValue(nomCell);
                        if (nomFichier != null && !nomFichier.isEmpty() &&
                                !nomFichier.equalsIgnoreCase("Nom") && !nomFichier.equalsIgnoreCase("Dépt.")) {

                            if (currentBaseId > 0) {
                                Employe emp = employeService.findById(currentBaseId);
                                if (emp != null) {
                                    String empNom = emp.getNom().toLowerCase().trim();
                                    String empPrenom = emp.getPrenom() != null ? emp.getPrenom().toLowerCase().trim() : "";
                                    String nomFichierClean = nomFichier.toLowerCase().trim().replace(" ", "");

                                    // 🔥 Vérifier si le nom correspond
                                    boolean nomOk = nomFichierClean.equals(empNom) ||
                                            nomFichierClean.equals(empPrenom) ||
                                            nomFichierClean.equals(empNom + empPrenom) ||
                                            nomFichierClean.equals(empPrenom + empNom);

                                    if (!nomOk) {
                                        result.anomalies.add("⚠️ INCOHÉRENCE: Excel ID " + currentExcelId +
                                                " → Base " + currentBaseId + " (" + emp.getNom() + " " + emp.getPrenom() +
                                                ") mais fichier dit '" + nomFichier + "'");

                                        // 🔥 Essayer de corriger par le nom
                                        Employe empCorrige = employeService.findByNomPrenom(nomFichier);
                                        if (empCorrige != null) {
                                            int correctId = empCorrige.getId();
                                            // Vérifier que le département correspond
                                            if (departement == null || empCorrige.getDepartement() == departement) {
                                                result.anomalies.add("✅ CORRECTION: Excel ID " + currentExcelId +
                                                        " → ID Base " + correctId + " (" + empCorrige.getNom() + " " + empCorrige.getPrenom() + ")");
                                                excelIdToBaseId.put(currentExcelId, correctId);
                                                currentBaseId = correctId;
                                            } else {
                                                result.anomalies.add("❌ CORRECTION IMPOSSIBLE: " + empCorrige.getNom() +
                                                        " n'est pas dans le bon département (" + departement + ")");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    continue;
                }

                // ──────────────────────────────────────────────────────────────
                // 🔥 3ème PASSE : Lire les données de pointage
                // ──────────────────────────────────────────────────────────────

                if (currentBaseId > 0) {
                    for (int col = dayStartCol; col <= dayEndCol; col++) {
                        Cell cell = row.getCell(col);
                        if (cell != null) {
                            List<LocalTime> times = extractTimesFromCell(cell);
                            if (!times.isEmpty()) {
                                int jour = col - dayStartCol + 1;
                                if (jour >= 1 && jour <= yearMonth.lengthOfMonth()) {
                                    LocalDate date = yearMonth.atDay(jour);

                                    LocalTime arrivee = times.size() > 0 ? times.get(0) : null;
                                    LocalTime debPause = times.size() > 1 ? times.get(1) : null;
                                    LocalTime finPause = times.size() > 2 ? times.get(2) : null;
                                    LocalTime depart = times.size() > 3 ? times.get(3) : null;

                                    if (arrivee != null || depart != null) {
                                        // 🔥 Utiliser l'ID base CORRIGÉ
                                        Employe emp = employeService.findById(currentBaseId);
                                        if (emp != null) {
                                            // Vérifier le département
                                            if (departement != null && emp.getDepartement() != departement) {
                                                result.anomalies.add("⚠️ " + emp.getNom() + " (" + currentBaseId +
                                                        ") n'est pas dans le département " + departement);
                                                continue;
                                            }

                                            PresenceTemp temp = new PresenceTemp();
                                            temp.date = date;
                                            temp.arrivee = arrivee;
                                            temp.debutPause = debPause;
                                            temp.finPause = finPause;
                                            temp.depart = depart;

                                            employeData.computeIfAbsent(currentBaseId, k -> new ArrayList<>()).add(temp);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ──────────────────────────────────────────────────────────────────
            // 🔥 TRAITER LES DONNÉES
            // ──────────────────────────────────────────────────────────────────

            System.out.println("📊 Employés avec données: " + employeData.size());

            for (Map.Entry<Integer, List<PresenceTemp>> entry : employeData.entrySet()) {
                int employeId = entry.getKey();
                List<PresenceTemp> temps = entry.getValue();

                Employe emp = employeService.findById(employeId);
                if (emp == null) {
                    result.anomalies.add("❌ Employé ID " + employeId + " introuvable en BDD");
                    result.erreurs++;
                    continue;
                }

                for (PresenceTemp temp : temps) {
                    if (temp.arrivee == null) {
                        Presence p = new Presence();
                        p.setEmploye(emp);
                        p.setDatePresence(temp.date);
                        p.setSource(Presence.Source.IMPORT);
                        p.setDepartement(departement != null ? departement : emp.getDepartement());
                        p.setStatut(Statut.ABSENT_NON_JUSTIFIE);
                        result.absents++;
                        result.presences.add(p);
                        result.total++;
                        continue;
                    }

                    Presence p = new Presence();
                    p.setEmploye(emp);
                    p.setDatePresence(temp.date);
                    p.setHeureArrivee(temp.arrivee);
                    p.setDebutPause(temp.debutPause);
                    p.setFinPause(temp.finPause);
                    p.setHeureDepart(temp.depart);
                    p.setSource(Presence.Source.IMPORT);
                    p.setDepartement(departement != null ? departement : emp.getDepartement());

                    Statut statut = p.detecterStatut();
                    p.setStatut(statut);
                    p.calculerHeuresTravaillees();

                    if (statut == Statut.RETARD) {
                        result.retards++;
                        result.anomalies.add("⏰ Retard: " + emp.getNom() + " " + emp.getPrenom() +
                                " — " + temp.date + " " + p.getHeureArriveeStr());
                    } else if (statut == Statut.PRESENT) {
                        result.presents++;
                    } else if (statut == Statut.INCOMPLET) {
                        result.incomplets++;
                        result.anomalies.add("⚠️ Incomplet: " + emp.getNom() + " " + emp.getPrenom() +
                                " — " + temp.date);
                    }

                    result.presences.add(p);
                    result.total++;
                }
            }

            // ── Sauvegarder en base ──────────────────────────────────────────
            for (Presence p : result.presences) {
                add(p);
                result.importees++;
            }

            result.employes = (int) result.presences.stream()
                    .map(p -> p.getEmploye().getId())
                    .distinct().count();

            System.out.println("✅ Importé: " + result.importees + " présences pour " + result.employes + " employés");

        } catch (Exception e) {
            e.printStackTrace();
            result.anomalies.add("❌ Erreur: " + e.getMessage());
        }

        return result;
    }

    // ─── CLASSES INTERNES ──────────────────────────────────────────────────────

    private static class PresenceTemp {
        LocalDate date;
        LocalTime arrivee;
        LocalTime debutPause;
        LocalTime finPause;
        LocalTime depart;
    }

    // ─── MÉTHODES UTILITAIRES ──────────────────────────────────────────────────

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
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}