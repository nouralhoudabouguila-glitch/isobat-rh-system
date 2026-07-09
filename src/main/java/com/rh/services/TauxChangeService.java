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

    // API gratuite - ExchangeRate-API (nécessite une clé gratuite)
    private static final String API_KEY = System.getenv("EXCHANGE_API_KEY");; // Obtenez une clé sur exchangerate-api.com
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/EUR";

    // Alternative : API gratuite sans clé (limité)
    private static final String API_URL_FREE = "https://api.exchangerate-api.com/v4/latest/EUR";

    // Cache des taux
    private static Map<String, Double> tauxCache = new HashMap<>();
    private static long derniereMaj = 0;
    private static final long CACHE_DUREE = 3600000; // 1 heure

    // Liste des devises supportées
    public static final String[] DEVISES = {"EUR", "USD", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "DZD", "TND", "MAD"};

    /**
     * Récupère tous les taux de change depuis l'API
     */
    public Map<String, Double> getTaux() {
        // Vérifier le cache
        if (System.currentTimeMillis() - derniereMaj < CACHE_DUREE && !tauxCache.isEmpty()) {
            return tauxCache;
        }

        try {
            // Essayer avec la clé API
            String urlString = API_KEY.equals("VOTRE_CLE_API") ? API_URL_FREE : API_URL;
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
                for (String devise : DEVISES) {
                    if (rates.has(devise)) {
                        tauxCache.put(devise, rates.get(devise).getAsDouble());
                    }
                }

                derniereMaj = System.currentTimeMillis();
                System.out.println("✅ Taux de change mis à jour : " + tauxCache.size() + " devises");
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
        fallback.put("MAD", 10.7);

        System.out.println("⚠️ Utilisation des taux de fallback");
        return fallback;
    }

    /**
     * Convertit un montant d'une devise à une autre
     */
    public double convertir(double montant, String deviseSource, String deviseCible) {
        Map<String, Double> taux = getTaux();

        if (!taux.containsKey(deviseSource) || !taux.containsKey(deviseCible)) {
            System.err.println("Devise non supportée : " + deviseSource + " -> " + deviseCible);
            return montant;
        }

        double tauxSource = taux.get(deviseSource);
        double tauxCible = taux.get(deviseCible);

        // Convertir en EUR d'abord puis dans la devise cible
        double montantEUR = montant / tauxSource;
        return montantEUR * tauxCible;
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
            case "MAD": return "DH";
            default: return devise;
        }
    }

    /**
     * Teste la connexion à l'API
     */
    public boolean testConnexion() {
        try {
            String urlString = API_KEY.equals("VOTRE_CLE_API") ? API_URL_FREE : API_URL;
            URL url = new URL(urlString);
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