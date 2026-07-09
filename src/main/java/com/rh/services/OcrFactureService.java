package com.rh.services;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrFactureService {

    /**
     * Extrait le texte d'une image
     */
    public String extraireTexte(File imageFile) {
        try {
            ITesseract tesseract = new Tesseract();
            // Chemin vers le dossier de données de Tesseract (à adapter)
            // Pour Windows, généralement : "C:/Program Files/Tesseract-OCR/tessdata"
            tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
            tesseract.setLanguage("fra"); // Français

            BufferedImage image = ImageIO.read(imageFile);
            String result = tesseract.doOCR(image);
            System.out.println("Texte extrait : \n" + result);
            return result;

        } catch (TesseractException | IOException e) {
            System.err.println("Erreur OCR : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrait les données d'une facture à partir du texte OCR
     */
    public FactureData extraireDonneesFacture(String texte) {
        FactureData data = new FactureData();

        if (texte == null || texte.isEmpty()) {
            return data;
        }

        // 1. Extraire le numéro de facture
        String numero = extraireNumeroFacture(texte);
        data.setNumeroFacture(numero);

        // 2. Extraire le fournisseur
        String fournisseur = extraireFournisseur(texte);
        data.setFournisseur(fournisseur);

        // 3. Extraire la date
        String date = extraireDate(texte);
        data.setDateFacture(date);

        // 4. Extraire le montant HT
        double ht = extraireMontantHT(texte);
        data.setMontantHt(ht);

        // 5. Extraire la TVA
        double tva = extraireTVA(texte);
        data.setMontantTva(tva);

        // 6. Extraire le montant TTC
        double ttc = extraireMontantTTC(texte);
        data.setMontantTtc(ttc);

        // 7. Extraire le mode de paiement
        String modePaiement = extraireModePaiement(texte);
        data.setModePaiement(modePaiement);

        return data;
    }

    /**
     * Extrait le numéro de facture
     */
    private String extraireNumeroFacture(String texte) {
        // Patterns pour différents formats
        String[] patterns = {
                "FACTURE N°[:\\s]*([A-Z0-9\\-]+)",
                "FAC[-\\s]*([A-Z0-9\\-]+)",
                "FACTURE[-\\s]*([A-Z0-9\\-]+)",
                "N°\\s*FACTURE[:\\s]*([A-Z0-9\\-]+)",
                "NUMERO\\s*FACTURE[:\\s]*([A-Z0-9\\-]+)"
        };

        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(texte);
            if (m.find()) {
                return m.group(1).trim();
            }
        }

        // Si non trouvé, chercher un format comme FC002595/2026
        Pattern p = Pattern.compile("FC\\d{6}\\s*/\\s*\\d{4}");
        Matcher m = p.matcher(texte);
        if (m.find()) {
            return m.group().trim();
        }

        return "";
    }

    /**
     * Extrait le nom du fournisseur
     */
    private String extraireFournisseur(String texte) {
        String[] lignes = texte.split("\n");
        List<String> fournisseursConnus = new ArrayList<>();
        fournisseursConnus.add("STE PAPETERIE ELITE");
        fournisseursConnus.add("MATEK");
        fournisseursConnus.add("MYKTEK");
        fournisseursConnus.add("MS JOKER");
        fournisseursConnus.add("JOKER");
        fournisseursConnus.add("PAPETERIE");
        fournisseursConnus.add("INFORMATIQUE");

        // Chercher dans les premières lignes
        for (int i = 0; i < Math.min(10, lignes.length); i++) {
            String ligne = lignes[i].toUpperCase().trim();
            for (String fournisseur : fournisseursConnus) {
                if (ligne.contains(fournisseur)) {
                    return lignes[i].trim();
                }
            }
        }

        // Si non trouvé, prendre la première ligne non vide
        for (String ligne : lignes) {
            if (ligne.trim().length() > 5 && !ligne.trim().matches(".*\\d+.*")) {
                return ligne.trim();
            }
        }

        return "";
    }

    /**
     * Extrait la date de la facture
     */
    private String extraireDate(String texte) {
        // Patterns pour dates
        String[] patterns = {
                "DATE\\s*[:]\\s*(\\d{2}[-/]\\d{2}[-/]\\d{4})",
                "DATE\\s*[:]\\s*(\\d{2}[-/]\\d{2}[-/]\\d{2})",
                "DATE\\s*:\\s*(\\d{2}/\\d{2}/\\d{4})",
                "DATE\\s*:\\s*(\\d{2}-\\d{2}-\\d{4})",
                "DATE\\s*:\\s*(\\d{2}/\\d{2}/\\d{2})",
                "DATE\\s*:\\s*(\\d{2}-\\d{2}-\\d{2})",
                "(\\d{2}[-/]\\d{2}[-/]\\d{4})",
                "(\\d{2}[-/]\\d{2}[-/]\\d{2})"
        };

        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(texte);
            if (m.find()) {
                return m.group(1).trim();
            }
        }

        return "";
    }

    /**
     * Extrait le montant HT
     */
    private double extraireMontantHT(String texte) {
        // Chercher des patterns comme "TOTAL HTVA : 31.249" ou "Total HT : 135,790"
        String[] patterns = {
                "TOTAL\\s*HT[VA]*\\s*[:]\\s*([\\d\\.,]+)",
                "HT\\s*[:]\\s*([\\d\\.,]+)",
                "BASE\\s*TVA\\s*[:]\\s*([\\d\\.,]+)",
                "Total HT\\s*[:]\\s*([\\d\\.,]+)",
                "MONTANT HT\\s*[:]\\s*([\\d\\.,]+)"
        };

        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(texte);
            if (m.find()) {
                String montant = m.group(1).trim().replace(",", ".");
                try {
                    return Double.parseDouble(montant);
                } catch (NumberFormatException e) {
                    // Continuer
                }
            }
        }

        return 0;
    }

    /**
     * Extrait le montant de TVA
     */
    private double extraireTVA(String texte) {
        String[] patterns = {
                "TVA\\s*[:]\\s*([\\d\\.,]+)",
                "MONTANT\\s*TVA\\s*[:]\\s*([\\d\\.,]+)",
                "TAUX\\s*19\\s*.*?([\\d\\.,]+)\\s*$"
        };

        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(texte);
            if (m.find()) {
                String montant = m.group(1).trim().replace(",", ".");
                try {
                    return Double.parseDouble(montant);
                } catch (NumberFormatException e) {
                    // Continuer
                }
            }
        }

        return 0;
    }

    /**
     * Extrait le montant TTC
     */
    private double extraireMontantTTC(String texte) {
        // Chercher "TTC" ou "TOTAL" à la fin
        String[] patterns = {
                "TTC\\s*[:]\\s*([\\d\\.,]+)",
                "TOTAL\\s*TTC\\s*[:]\\s*([\\d\\.,]+)",
                "MONTANT\\s*TTC\\s*[:]\\s*([\\d\\.,]+)",
                "ARRETEE.*?SOMME\\s*DE\\s*[:]\\s*([\\d\\.,]+)",
                "TOTAL\\s*[:]\\s*([\\d\\.,]+)\\s*DT"
        };

        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(texte);
            if (m.find()) {
                String montant = m.group(1).trim().replace(",", ".");
                try {
                    return Double.parseDouble(montant);
                } catch (NumberFormatException e) {
                    // Continuer
                }
            }
        }

        // Si non trouvé, chercher un nombre avec DT à la fin
        Pattern p = Pattern.compile("([\\d\\.,]+)\\s*DT");
        Matcher m = p.matcher(texte);
        if (m.find()) {
            String montant = m.group(1).trim().replace(",", ".");
            try {
                return Double.parseDouble(montant);
            } catch (NumberFormatException e) {
                // Continuer
            }
        }

        return 0;
    }

    /**
     * Extrait le mode de paiement
     */
    private String extraireModePaiement(String texte) {
        String[] modes = {"ESPECES", "CHEQUE", "VIREMENT", "CARTE", "CAISSE", "PAIEMENT"};

        String texteUpper = texte.toUpperCase();
        for (String mode : modes) {
            if (texteUpper.contains(mode)) {
                if (mode.equals("CAISSE")) return "Espèces";
                if (mode.equals("VIREMENT")) return "Virement";
                if (mode.equals("CHEQUE")) return "Chèque";
                if (mode.equals("CARTE")) return "Carte bancaire";
                if (mode.equals("ESPECES")) return "Espèces";
                if (mode.equals("PAIEMENT")) {
                    // Chercher plus de contexte
                    Pattern p = Pattern.compile("MODE\\s*DE\\s*PAIEMENT\\s*[:]\\s*([A-Z]+)");
                    Matcher m = p.matcher(texteUpper);
                    if (m.find()) {
                        String modeTrouve = m.group(1);
                        if (modeTrouve.contains("VIREMENT")) return "Virement";
                        if (modeTrouve.contains("CHEQUE")) return "Chèque";
                        if (modeTrouve.contains("CARTE")) return "Carte bancaire";
                        if (modeTrouve.contains("ESPECES")) return "Espèces";
                    }
                }
                return mode;
            }
        }

        return "Virement";
    }

    /**
     * Classe interne pour les données extraites
     */
    public static class FactureData {
        private String numeroFacture = "";
        private String fournisseur = "";
        private String dateFacture = "";
        private double montantHt = 0;
        private double montantTva = 0;
        private double montantTtc = 0;
        private String modePaiement = "Virement";

        // Getters et Setters
        public String getNumeroFacture() { return numeroFacture; }
        public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

        public String getFournisseur() { return fournisseur; }
        public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }

        public String getDateFacture() { return dateFacture; }
        public void setDateFacture(String dateFacture) { this.dateFacture = dateFacture; }

        public double getMontantHt() { return montantHt; }
        public void setMontantHt(double montantHt) { this.montantHt = montantHt; }

        public double getMontantTva() { return montantTva; }
        public void setMontantTva(double montantTva) { this.montantTva = montantTva; }

        public double getMontantTtc() { return montantTtc; }
        public void setMontantTtc(double montantTtc) { this.montantTtc = montantTtc; }

        public String getModePaiement() { return modePaiement; }
        public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    }
}