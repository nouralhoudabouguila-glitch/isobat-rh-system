package com.rh.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TauxChangeService {

    // API gratuite - pas besoin de clé
    private static final String API_URL_FREE = "https://api.exchangerate-api.com/v4/latest/EUR";

    // Cache des taux
    private static Map<String, Double> tauxCache = new HashMap<>();
    private static long derniereMaj = 0;
    private static final long CACHE_DUREE = 3600000; // 1 heure

    // Liste des devises supportées par l'API
    private static final String[] DEVISES_API = {"EUR", "USD", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "DZD", "TND", "MAD"};

    // Correspondance entre les devises de l'application et celles de l'API
    private static final Map<String, String> CORRESPONDANCE_DEVISES = new HashMap<>();
    static {
        CORRESPONDANCE_DEVISES.put("DT", "TND");
        CORRESPONDANCE_DEVISES.put("TND", "TND");
        CORRESPONDANCE_DEVISES.put("EUR", "EUR");
        CORRESPONDANCE_DEVISES.put("USD", "USD");
        CORRESPONDANCE_DEVISES.put("GBP", "GBP");
        CORRESPONDANCE_DEVISES.put("CHF", "CHF");
        CORRESPONDANCE_DEVISES.put("JPY", "JPY");
        CORRESPONDANCE_DEVISES.put("CAD", "CAD");
        CORRESPONDANCE_DEVISES.put("AUD", "AUD");
        CORRESPONDANCE_DEVISES.put("CNY", "CNY");
        CORRESPONDANCE_DEVISES.put("DZD", "DZD");
        CORRESPONDANCE_DEVISES.put("MAD", "MAD");
    }

    /**
     * Récupère tous les taux de change depuis l'API
     */
    public Map<String, Double> getTaux() {
        // Vérifier le cache
        if (System.currentTimeMillis() - derniereMaj < CACHE_DUREE && !tauxCache.isEmpty()) {
            return tauxCache;
        }

        try {
            String urlString = API_URL_FREE;
            System.out.println("📡 Appel API taux de change : " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject rates = json.getAsJsonObject("rates");

                tauxCache.clear();

                // Récupérer les taux pour toutes les devises supportées
                for (String devise : DEVISES_API) {
                    if (rates.has(devise)) {
                        tauxCache.put(devise, rates.get(devise).getAsDouble());
                    }
                }

                // Ajouter la correspondance pour DT
                if (tauxCache.containsKey("TND")) {
                    tauxCache.put("DT", tauxCache.get("TND"));
                }

                derniereMaj = System.currentTimeMillis();
                System.out.println("✅ Taux de change mis à jour : " + tauxCache.size() + " devises");
                System.out.println("   - EUR -> TND : " + tauxCache.get("TND"));
                System.out.println("   - EUR -> DT : " + tauxCache.get("DT"));
                return tauxCache;
            } else {
                System.err.println("Erreur API taux de change : " + responseCode);
                return getTauxFallback();
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des taux : " + e.getMessage());
            return getTauxFallback();
        }
    }

    /**
     * Taux de fallback en cas d'échec de l'API
     */
    private Map<String, Double> getTauxFallback() {
        if (!tauxCache.isEmpty()) {
            return tauxCache;
        }

        System.out.println("⚠️ Utilisation des taux de fallback");
        Map<String, Double> fallback = new HashMap<>();
        fallback.put("EUR", 1.0);
        fallback.put("USD", 1.08);
        fallback.put("GBP", 0.85);
        fallback.put("CHF", 0.96);
        fallback.put("JPY", 169.0);
        fallback.put("CAD", 1.48);
        fallback.put("AUD", 1.62);
        fallback.put("CNY", 7.82);
        fallback.put("DZD", 145.0);
        fallback.put("TND", 3.36);
        fallback.put("DT", 3.36);
        fallback.put("MAD", 10.7);

        return fallback;
    }

    /**
     * Convertit un montant d'une devise à une autre
     */
    public double convertir(double montant, String deviseSource, String deviseCible) {
        // Si les devises sont identiques
        if (deviseSource.equals(deviseCible)) {
            return montant;
        }

        // Obtenir les taux
        Map<String, Double> taux = getTaux();

        // Convertir les noms de devises si nécessaire
        String sourceKey = deviseSource;
        String cibleKey = deviseCible;

        // Si la devise source est "DT", utiliser "TND" pour l'API
        if ("DT".equals(deviseSource)) {
            sourceKey = "TND";
        }
        if ("DT".equals(deviseCible)) {
            cibleKey = "TND";
        }

        if (!taux.containsKey(sourceKey)) {
            System.err.println("⚠️ Devise source non supportée : " + deviseSource + " (clé: " + sourceKey + ") - Utilisation du taux 1.0");
            return montant;
        }

        if (!taux.containsKey(cibleKey)) {
            System.err.println("⚠️ Devise cible non supportée : " + deviseCible + " (clé: " + cibleKey + ") - Utilisation du taux 1.0");
            return montant;
        }

        double tauxSource = taux.get(sourceKey);
        double tauxCible = taux.get(cibleKey);

        // Convertir en EUR d'abord puis dans la devise cible
        double montantEUR = montant / tauxSource;
        double resultat = montantEUR * tauxCible;

        System.out.println("💱 Conversion : " + montant + " " + deviseSource + " (" + sourceKey + ") -> " + resultat + " " + deviseCible + " (" + cibleKey + ")");
        return resultat;
    }

    /**
     * Formate un montant avec la devise
     */
    public String formaterMontant(double montant, String devise) {
        String symbole = getSymboleDevise(devise);
        return String.format("%.2f %s", montant, symbole);
    }

    /**
     * Retourne le symbole de la devise
     */
    public String getSymboleDevise(String devise) {
        switch (devise) {
            case "EUR": return "€";
            case "USD": return "$";
            case "GBP": return "£";
            case "CHF": return "CHF";
            case "JPY": return "¥";
            case "CAD": return "C$";
            case "AUD": return "A$";
            case "CNY": return "¥";
            case "DZD": return "DA";
            case "TND": return "DT";
            case "DT": return "DT";
            case "MAD": return "DH";
            default: return devise;
        }
    }

    /**
     * Teste la connexion à l'API
     */
    public boolean testConnexion() {
        try {
            URL url = new URL(API_URL_FREE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retourne la date de dernière mise à jour
     */
    public String getDerniereMajFormatee() {
        if (derniereMaj == 0) return "Jamais";
        java.time.LocalDateTime date = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(derniereMaj),
                java.time.ZoneId.systemDefault()
        );
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}