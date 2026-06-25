package com.rh.services;

import com.rh.models.TriTelephonique;
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

public class ExportExcelService {

    /**
     * Exporter les données vers un fichier Excel et l'ouvrir automatiquement
     * @param candidats Liste des candidats à exporter
     * @param stage Stage pour la boîte de dialogue de sauvegarde
     * @return true si l'exportation a réussi, false sinon
     */
    public boolean exporterVersExcel(List<TriTelephonique> candidats, Stage stage) {
        if (candidats == null || candidats.isEmpty()) {
            System.out.println("Aucune donnée à exporter");
            return false;
        }

        // Ouvrir la boîte de dialogue de sauvegarde
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les données");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx")
        );

        // Nom de fichier par défaut avec date
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        fileChooser.setInitialFileName("Tri_Telephonique_" + dateStr + ".xlsx");

        File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) {
            return false; // L'utilisateur a annulé
        }

        try {
            // Créer le workbook et la feuille
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TRI TELEPHONIQUE");

            // Créer les styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowNum = 0;

            // ── Lignes vides ──
            for (int i = 0; i < 6; i++) {
                sheet.createRow(rowNum++);
            }

            // ── Ligne de date ──
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dateCell.setCellStyle(dateStyle);

            // ── Lignes vides ──
            for (int i = 0; i < 2; i++) {
                sheet.createRow(rowNum++);
            }

            // ── En-têtes des colonnes ──
            String[] headers = {"NOM & PRENOM", "TEL", "EXPERIENCE", "COMMENTAIRE", "Poste", "Statut"};
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Données ──
            for (TriTelephonique candidat : candidats) {
                Row row = sheet.createRow(rowNum++);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(candidat.getNomComplet());
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(candidat.getTelephone() != null ? candidat.getTelephone() : "");
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(candidat.getExperience() != null ? candidat.getExperience() : "");
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(candidat.getCommentaire() != null ? candidat.getCommentaire() : "");
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(candidat.getPoste() != null ? candidat.getPoste() : "");
                cell4.setCellStyle(dataStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(candidat.getStatut() != null ? candidat.getStatut() : "");
                cell5.setCellStyle(dataStyle);
            }

            // ── Ajuster la largeur des colonnes ──
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 1000, 20000));
            }

            // ── Écrire le fichier ──
            try (FileOutputStream fileOut = new FileOutputStream(fichier)) {
                workbook.write(fileOut);
            }

            workbook.close();
            System.out.println("Exportation réussie vers : " + fichier.getAbsolutePath());

            // ── Ouvrir le fichier automatiquement ──
            ouvrirFichier(fichier);

            return true;

        } catch (IOException e) {
            System.err.println("Erreur lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ouvrir le fichier avec l'application par défaut
     * @param fichier Le fichier à ouvrir
     */
    private void ouvrirFichier(File fichier) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichier);
                System.out.println("Fichier ouvert automatiquement : " + fichier.getAbsolutePath());
            } else {
                System.out.println("Desktop non supporté, impossible d'ouvrir le fichier automatiquement.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du fichier : " + e.getMessage());
        }
    }

    // ── Styles ──

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
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
}