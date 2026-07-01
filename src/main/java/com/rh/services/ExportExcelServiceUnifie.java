package com.rh.services;

import com.rh.contollers.DetailSalaireController;
import com.rh.models.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportExcelServiceUnifie {

    /**
     * Exporter les avances vers Excel
     */
    public boolean exporterAvances(List<Avance> avances, Stage stage) {
        if (avances == null || avances.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Avances", "avances");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("AVANCES");
            int rowNum = 0;

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"ID", "EMPLOYÉ", "DÉPARTEMENT", "MONTANT (DT)", "MOTIF", "DATE DEMANDE", "DATE VALIDATION", "STATUT", "COMMENTAIRE"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            double totalValide = 0;
            for (Avance a : avances) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, String.valueOf(a.getIdAvance()), dataStyle);
                createCell(row, col++, a.getNomCompletEmploye(), dataStyle);
                createCell(row, col++, a.getEmploye() != null && a.getEmploye().getDepartement() != null ?
                        a.getEmploye().getDepartement().toString() : "", dataStyle);
                createCell(row, col++, a.getMontant(), currencyStyle);
                createCell(row, col++, a.getMotif() != null ? a.getMotif() : "", dataStyle);
                createCell(row, col++, a.getDateDemandeFormatee(), dataStyle);
                createCell(row, col++, a.getDateValidationFormatee(), dataStyle);
                createCell(row, col++, a.getStatutLabel(), centerStyle);
                createCell(row, col++, a.getCommentaire() != null ? a.getCommentaire() : "", dataStyle);

                if ("Validée".equals(a.getStatutLabel())) {
                    totalValide += a.getMontant();
                }
            }

            // Ligne de total
            rowNum = addTotalRow(sheet, rowNum, "TOTAL AVANCES VALIDÉES", totalValide, totalStyle, currencyStyle);

            // Ajuster les colonnes
            autoSizeColumns(sheet, headers.length);

            // Écrire et ouvrir
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les retenues vers Excel
     */
    public boolean exporterRetenues(List<Retenue> retenues, Stage stage) {
        if (retenues == null || retenues.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Retenues", "retenues");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("RETENUES");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"ID", "EMPLOYÉ", "DÉPARTEMENT", "MONTANT (DT)", "MOTIF", "DATE", "COMMENTAIRE"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            double total = 0;
            for (Retenue r : retenues) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, String.valueOf(r.getIdRetenue()), dataStyle);
                createCell(row, col++, r.getNomCompletEmploye(), dataStyle);
                createCell(row, col++, r.getEmploye() != null && r.getEmploye().getDepartement() != null ?
                        r.getEmploye().getDepartement().toString() : "", dataStyle);
                createCell(row, col++, r.getMontant(), currencyStyle);
                createCell(row, col++, r.getMotif() != null ? r.getMotif() : "", dataStyle);
                createCell(row, col++, r.getDateRetenueFormatee(), dataStyle);
                createCell(row, col++, r.getCommentaire() != null ? r.getCommentaire() : "", dataStyle);

                total += r.getMontant();
            }

            // Ligne de total
            rowNum = addTotalRow(sheet, rowNum, "TOTAL RETENUES", total, totalStyle, currencyStyle);

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les tickets restaurant vers Excel
     */
    public boolean exporterTickets(List<TicketEmploye> tickets, Stage stage) {
        if (tickets == null || tickets.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Tickets", "tickets");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TICKETS RESTAURANT");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // Informations du mois/année
            if (!tickets.isEmpty()) {
                TicketEmploye premier = tickets.get(0);
                Row infoRow = sheet.createRow(rowNum++);
                Cell infoCell = infoRow.createCell(0);
                infoCell.setCellValue("Période : " + premier.getMois() + " " + premier.getAnnee());
                infoCell.setCellStyle(dateStyle);
                rowNum++;
            }

            // En-têtes
            String[] headers = {"EMPLOYÉ", "DÉPARTEMENT", "N° JOURS", "MONTANT / TICKET (DT)", "TOTAL (DT)"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            double totalGeneral = 0;
            int totalJours = 0;
            for (TicketEmploye t : tickets) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, t.getNomCompletEmploye(), dataStyle);
                createCell(row, col++, t.getEmploye() != null && t.getEmploye().getDepartement() != null ?
                        t.getEmploye().getDepartement().toString() : "", dataStyle);
                createCell(row, col++, t.getNbJours(), centerStyle);
                createCell(row, col++, t.getMontantParTicket(), currencyStyle);
                createCell(row, col++, t.getTotal(), currencyStyle);

                totalGeneral += t.getTotal();
                totalJours += t.getNbJours();
            }

            // Ligne de total
            Row totalRow = sheet.createRow(rowNum++);
            createCell(totalRow, 0, "TOTAL GÉNÉRAL", totalStyle);
            createCell(totalRow, 1, "", dataStyle);
            createCell(totalRow, 2, totalJours, totalStyle);
            createCell(totalRow, 3, "", dataStyle);
            createCell(totalRow, 4, totalGeneral, totalStyle);

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les factures vers Excel
     */
    public boolean exporterFactures(List<Facture> factures, Stage stage) {
        if (factures == null || factures.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Factures", "factures");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("FACTURES");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"NUMÉRO", "FOURNISSEUR", "MONTANT HT (DT)", "TVA (DT)", "MONTANT TTC (DT)", "DATE FACTURE", "ÉCHÉANCE", "STATUT", "MODE PAIEMENT"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            double totalTtc = 0;
            for (Facture f : factures) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, f.getNumeroFacture(), dataStyle);
                createCell(row, col++, f.getNomFournisseur(), dataStyle);
                createCell(row, col++, f.getMontantHt(), currencyStyle);
                createCell(row, col++, f.getMontantTva(), currencyStyle);
                createCell(row, col++, f.getMontantTtc(), currencyStyle);
                createCell(row, col++, f.getDateFactureFormatee(), dataStyle);
                createCell(row, col++, f.getDateEcheanceFormatee(), dataStyle);
                createCell(row, col++, f.getStatutLabel(), centerStyle);
                createCell(row, col++, f.getModePaiementLabel(), dataStyle);

                totalTtc += f.getMontantTtc();
            }

            // Ligne de total
            rowNum = addTotalRow(sheet, rowNum, "TOTAL TTC", totalTtc, totalStyle, currencyStyle);

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les fournisseurs vers Excel
     */
    public boolean exporterFournisseurs(List<Fournisseur> fournisseurs, Stage stage) {
        if (fournisseurs == null || fournisseurs.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Fournisseurs", "fournisseurs");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("FOURNISSEURS");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"ID", "NOM", "EMAIL", "TÉLÉPHONE", "ADRESSE", "VILLE", "PAYS", "MATRICULE FISCAL", "DATE AJOUT"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            for (Fournisseur f : fournisseurs) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, String.valueOf(f.getIdFournisseur()), dataStyle);
                createCell(row, col++, f.getNom(), dataStyle);
                createCell(row, col++, f.getEmail() != null ? f.getEmail() : "", dataStyle);
                createCell(row, col++, f.getTelephone() != null ? f.getTelephone() : "", dataStyle);
                createCell(row, col++, f.getAdresse() != null ? f.getAdresse() : "", dataStyle);
                createCell(row, col++, f.getVille() != null ? f.getVille() : "", dataStyle);
                createCell(row, col++, f.getPays() != null ? f.getPays() : "", dataStyle);
                createCell(row, col++, f.getMatriculeFiscal() != null ? f.getMatriculeFiscal() : "", dataStyle);
                createCell(row, col++, f.getDateCreation() != null ?
                        f.getDateCreation().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "", dataStyle);
            }

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les données de tri téléphonique vers Excel
     */
    public boolean exporterTriTelephonique(List<TriTelephonique> candidats, Stage stage) {
        if (candidats == null || candidats.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Tri_Telephonique", "tri_telephonique");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TRI TELEPHONIQUE");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"NOM", "PRÉNOM", "TÉLÉPHONE", "EXPÉRIENCE", "POSTE", "COMMENTAIRE", "STATUT"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            for (TriTelephonique c : candidats) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, c.getNom() != null ? c.getNom() : "", dataStyle);
                createCell(row, col++, c.getPrenom() != null ? c.getPrenom() : "", dataStyle);
                createCell(row, col++, c.getTelephone() != null ? c.getTelephone() : "", dataStyle);
                createCell(row, col++, c.getExperience() != null ? c.getExperience() : "", dataStyle);
                createCell(row, col++, c.getPoste() != null ? c.getPoste() : "", dataStyle);
                createCell(row, col++, c.getCommentaire() != null ? c.getCommentaire() : "", dataStyle);
                createCell(row, col++, c.getStatut() != null ? c.getStatut() : "En attente", centerStyle);
            }

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les entretiens physiques vers Excel
     */
    public boolean exporterEntretiensPhysiques(List<EntretienPhysique> entretiens, Stage stage) {
        if (entretiens == null || entretiens.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Entretiens_Physiques", "entretiens_physiques");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ENTRETIENS PHYSIQUES");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"NOM", "PRÉNOM", "POSTE", "DATE RDV", "STATUT"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            for (EntretienPhysique e : entretiens) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, e.getNom() != null ? e.getNom() : "", dataStyle);
                createCell(row, col++, e.getPrenom() != null ? e.getPrenom() : "", dataStyle);
                createCell(row, col++, e.getPoste() != null ? e.getPoste() : "", dataStyle);
                createCell(row, col++, e.getDateRdv() != null ? e.getDateRdv() : "", dataStyle);
                createCell(row, col++, e.getStatut() != null ? e.getStatut() : "", centerStyle);
            }

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les sessions de formation vers Excel
     */
    public boolean exporterSessionsFormation(List<ParticipationFormation> participations, Stage stage) {
        if (participations == null || participations.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Sessions_Formation", "sessions_formation");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SESSIONS FORMATION");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // En-têtes
            String[] headers = {"NOM", "PRÉNOM", "FORMATION", "DATE FORMATION", "STATUT"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            for (ParticipationFormation p : participations) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, p.getNom() != null ? p.getNom() : "", dataStyle);
                createCell(row, col++, p.getPrenom() != null ? p.getPrenom() : "", dataStyle);
                createCell(row, col++, p.getFormation() != null ? p.getFormation() : "", dataStyle);
                createCell(row, col++, p.getDateFormationFormatee(), dataStyle);
                createCell(row, col++, p.getStatut() != null ? p.getStatut() : "", centerStyle);
            }

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporter les salaires vers Excel
     */
    public boolean exporterSalaires(List<DetailSalaireController.EmployeSalaire> salaires, Stage stage, String centre) {
        if (salaires == null || salaires.isEmpty()) {
            showAlert("Aucune donnée à exporter.");
            return false;
        }

        File fichier = choisirFichier(stage, "Salaires_" + centre.replace(" ", "_"), "salaires");
        if (fichier == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SALAIRES");
            int rowNum = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            // Date d'export
            rowNum = addDateRow(sheet, rowNum, dateStyle);

            // Info centre
            Row infoRow = sheet.createRow(rowNum++);
            Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Centre : " + centre);
            infoCell.setCellStyle(dateStyle);
            rowNum++;

            // En-têtes
            String[] headers = {"EMPLOYÉ", "RIB / NUMÉRO DE COMPTE", "MONTANT (DT)"};
            rowNum = addHeaderRow(sheet, rowNum, headers, headerStyle);

            // Données
            double total = 0;
            for (DetailSalaireController.EmployeSalaire s : salaires) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                createCell(row, col++, s.getNom(), dataStyle);
                createCell(row, col++, s.getRib(), dataStyle);
                createCell(row, col++, s.getMontant(), currencyStyle);

                total += s.getMontant();
            }

            // Ligne de total
            rowNum = addTotalRow(sheet, rowNum, "TOTAL SALAIRES", total, totalStyle, currencyStyle);

            autoSizeColumns(sheet, headers.length);
            ecrireFichier(workbook, fichier);
            ouvrirFichier(fichier);
            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export : " + e.getMessage());
            return false;
        }
    }

    // ── Méthodes utilitaires ──

    private File choisirFichier(Stage stage, String titre, String prefixe) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter " + titre);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx")
        );

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        fileChooser.setInitialFileName(prefixe + "_" + dateStr + ".xlsx");

        return fileChooser.showSaveDialog(stage);
    }

    private int addDateRow(Sheet sheet, int rowNum, CellStyle style) {
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Exporté le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        dateCell.setCellStyle(style);
        return rowNum;
    }

    private int addHeaderRow(Sheet sheet, int rowNum, String[] headers, CellStyle style) {
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        return rowNum;
    }

    private int addTotalRow(Sheet sheet, int rowNum, String label, double valeur, CellStyle labelStyle, CellStyle valueStyle) {
        Row totalRow = sheet.createRow(rowNum++);

        // Créer une cellule de fusion pour le label sur plusieurs colonnes
        // On utilise simplement la première colonne
        Cell labelCell = totalRow.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        // Mettre la valeur dans la colonne du montant (généralement la dernière)
        // On suppose que la colonne du montant est la 3ème (index 2) ou la dernière
        int colMontant = 3; // Par défaut
        // Pour les salaires, le montant est en colonne 2
        if (label.contains("SALAIRES")) {
            colMontant = 2;
        }
        // Pour les factures, le montant est en colonne 4
        if (label.contains("TTC")) {
            colMontant = 4;
        }
        // Pour les tickets, le montant est en colonne 4
        if (label.contains("TICKET")) {
            colMontant = 4;
        }

        Cell valueCell = totalRow.createCell(colMontant);
        valueCell.setCellValue(valeur);
        valueCell.setCellStyle(valueStyle);

        return rowNum;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int nbColonnes) {
        for (int i = 0; i < nbColonnes; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 500, 18000));
        }
    }

    private void ecrireFichier(Workbook workbook, File fichier) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(fichier)) {
            workbook.write(fileOut);
        }
        System.out.println("Exportation réussie vers : " + fichier.getAbsolutePath());
    }

    private void ouvrirFichier(File fichier) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichier);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du fichier : " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        System.out.println("⚠️ " + message);
    }

    // ── Styles ──

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}