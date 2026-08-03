package com.rh.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rh.utils.IsobatDB;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

public class ChatbotService {

    // Configuration GROQ API
    private static final Dotenv dotenv = Dotenv.load();

    private static final String GROQ_API_KEY = dotenv.get("GROQ_API_KEY");
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private Connection cnx;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);

    // Schéma de la base de données
    private static final String SCHEMA_DESCRIPTION = """
        Voici la structure de la base de données RH (ISOBAT) :
        
        TABLE employe:
        - id (INT) : Identifiant unique
        - nom (VARCHAR) : Nom de l'employé
        - prenom (VARCHAR) : Prénom de l'employé
        - dateNaissance (DATE) : Date de naissance
        - situationFamiliale (VARCHAR) : Célibataire, Marié, Divorcé, Veuf
        - cin (VARCHAR) : Numéro de CIN
        - telephone (VARCHAR) : Numéro de téléphone
        - salaireMensuelNet (DOUBLE) : Salaire mensuel net
        - profil (VARCHAR) : Poste de l'employé
        - typeContrat (VARCHAR) : CDI, CDD, Stage, Freelance
        - statut (VARCHAR) : Actif, Inactif, En congé
        - dateEmbauche (DATE) : Date d'embauche
        - nRIB (VARCHAR) : Numéro RIB
        - nCNSS (VARCHAR) : Numéro CNSS
        - Departement (VARCHAR) : CENTRE_APPEL_B2B, CENTRE_APPEL_B2C, BUREAU_ETUDE, ADMINISTRATION
        
        TABLE avance:
        - id_avance (INT) : Identifiant unique
        - id_employe (INT) : Référence vers employe.id
        - montant (DOUBLE) : Montant de l'avance
        - motif (VARCHAR) : Motif de la demande
        - date_demande (DATE) : Date de la demande
        - date_validation (DATE) : Date de validation
        - statut (VARCHAR) : En attente, Validée, Refusée
        - commentaire (TEXT) : Commentaire
        
        TABLE retenue:
        - id_retenue (INT) : Identifiant unique
        - id_employe (INT) : Référence vers employe.id
        - montant (DOUBLE) : Montant de la retenue
        - motif (VARCHAR) : Motif de la retenue
        - date_retenue (DATE) : Date de la retenue
        - commentaire (TEXT) : Commentaire
        - date_creation (DATETIME) : Date de création
        
        TABLE ticket_employe:
        - id_ticket (INT) : Identifiant unique
        - id_employe (INT) : Référence vers employe.id
        - mois (VARCHAR) : Mois (Janvier, Février, etc.)
        - annee (INT) : Année
        - nb_jours (INT) : Nombre de jours
        - montant_par_ticket (DOUBLE) : Montant par ticket
        - total (DOUBLE) : Montant total
        - date_creation (DATETIME) : Date de création
        
        TABLE facture:
        - id_facture (INT) : Identifiant unique
        - numero_facture (VARCHAR) : Numéro de facture
        - id_fournisseur (INT) : Référence vers fournisseur.id_fournisseur
        - montant_ht (DOUBLE) : Montant hors taxe
        - montant_tva (DOUBLE) : Montant TVA
        - montant_ttc (DOUBLE) : Montant TTC
        - devise (VARCHAR) : Devise (TND, EUR, USD, etc.)
        - date_facture (DATE) : Date de facture
        - date_echeance (DATE) : Date d'échéance
        - statut (VARCHAR) : En attente, Payée, Annulée
        - mode_paiement (VARCHAR) : Espèces, Chèque, Virement, Carte bancaire
        - commentaire (TEXT) : Commentaire
        
        TABLE fournisseur:
        - id_fournisseur (INT) : Identifiant unique
        - nom (VARCHAR) : Nom du fournisseur
        - email (VARCHAR) : Email
        - telephone (VARCHAR) : Téléphone
        - adresse (VARCHAR) : Adresse
        - ville (VARCHAR) : Ville
        - pays (VARCHAR) : Pays
        - matricule_fiscal (VARCHAR) : Matricule fiscal
        - date_creation (DATETIME) : Date de création
        
        TABLE tri_telephonique:
        - id_cand (INT) : Identifiant unique
        - nom (VARCHAR) : Nom du candidat
        - prenom (VARCHAR) : Prénom du candidat
        - telephone (VARCHAR) : Téléphone
        - experience (TEXT) : Expérience
        - poste (VARCHAR) : Poste visé
        - commentaire (TEXT) : Commentaire
        - statut (VARCHAR) : En attente, Accepté, Pas accepté, Retenu
        
        TABLE entretien_physique:
        - id_entretien (INT) : Identifiant unique
        - id_candidat (INT) : Référence vers tri_telephonique.id_cand
        - nom (VARCHAR) : Nom
        - prenom (VARCHAR) : Prénom
        - poste (VARCHAR) : Poste
        - date_rdv (VARCHAR) : Date du rendez-vous
        - statut (VARCHAR) : En attente, Retenue, Absente, Reporté
        
        TABLE participation_formation:
        - id_participation (INT) : Identifiant unique
        - id_entretien (INT) : Référence vers entretien_physique.id_entretien
        - nom (VARCHAR) : Nom
        - prenom (VARCHAR) : Prénom
        - poste (VARCHAR) : Poste
        - date_formation (DATE) : Date de formation
        - formation (VARCHAR) : Nom de la formation
        - statut (VARCHAR) : Présente, Absente, FPF
        """;

    public ChatbotService() {
        cnx = IsobatDB.getInstance().getCnx();
    }

    /**
     * Pose une question et retourne une réponse humanisée
     */
    public String poserQuestion(String question) {
        try {
            // 1. Analyser la question et générer la requête SQL
            String sql = genererRequeteSQL(question);
            System.out.println("📝 SQL généré : " + sql);

            if (sql == null || sql.isEmpty() || sql.toLowerCase().contains("je ne comprends pas")) {
                return genererReponseIncomprise(question);
            }

            // 2. Exécuter la requête
            List<String> resultats = executerRequete(sql);

            // 3. Générer une réponse humanisée
            return genererReponseHumanisee(question, sql, resultats);

        } catch (Exception e) {
            System.err.println("Erreur Chatbot : " + e.getMessage());
            return "🤔 Je n'ai pas bien compris votre question. Pourriez-vous la reformuler ?\n\n" +
                    "💡 Essayez de poser une question plus précise sur :\n" +
                    "• Les employés (combien, liste, salaires)\n" +
                    "• Les avances et retenues\n" +
                    "• Les tickets restaurant\n" +
                    "• Les factures et fournisseurs\n" +
                    "• Le recrutement";
        }
    }

    /**
     * Génère une requête SQL avec instructions pour une réponse humanisée
     */
    private String genererRequeteSQL(String question) throws Exception {
        String systemPrompt = """
            Tu es un assistant RH sympathique et professionnel.
            Tu dois générer des requêtes SQL pour répondre aux questions.
            
            Voici le schéma de la base de données :
            
            %s
            
            Règles :
            1. Ne génère que des requêtes SELECT
            2. Utilise les bons noms de colonnes
            3. Pour les dates, utilise CURDATE()
            4. Pour les départements : 'CENTRE_APPEL_B2B', 'CENTRE_APPEL_B2C', 'BUREAU_ETUDE', 'ADMINISTRATION'
            5. Si la question est vague, fais une requête qui retourne les infos les plus pertinentes
            
            Réponds UNIQUEMENT avec la requête SQL, sans explication.
            """.formatted(SCHEMA_DESCRIPTION);

        return appelerGROQ(systemPrompt, question);
    }

    /**
     * Exécute la requête et retourne les résultats formatés
     */
    private List<String> executerRequete(String sql) {
        List<String> resultats = new ArrayList<>();
        sql = sql.replace("```sql", "").replace("```", "").replace(";", "").trim();

        if (!sql.toUpperCase().startsWith("SELECT")) {
            resultats.add("Seules les requêtes SELECT sont autorisées.");
            return resultats;
        }

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Récupérer les données
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }

            // Formater pour l'affichage
            if (rows.isEmpty()) {
                resultats.add("Aucun résultat trouvé.");
            } else {
                for (Map<String, Object> row : rows) {
                    StringBuilder ligne = new StringBuilder();
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        if (ligne.length() > 0) ligne.append(" | ");
                        ligne.append(entry.getKey()).append(": ").append(entry.getValue());
                    }
                    resultats.add(ligne.toString());
                }
                resultats.add("📊 " + rows.size() + " ligne(s) trouvée(s)");
            }

        } catch (SQLException e) {
            resultats.add("❌ Erreur : " + e.getMessage());
        }

        return resultats;
    }

    /**
     * Génère une réponse humanisée
     */
    private String genererReponseHumanisee(String question, String sql, List<String> resultats) {
        if (resultats.isEmpty() || resultats.get(0).startsWith("❌")) {
            return "❌ Je n'ai pas pu trouver l'information demandée.\n" +
                    "Vérifiez que vos données existent dans la base.";
        }

        // Compter les lignes de données (exclure le compteur final)
        int nbLignes = 0;
        List<String> dataLines = new ArrayList<>();
        for (String ligne : resultats) {
            if (!ligne.startsWith("📊") && !ligne.startsWith("Aucun résultat") && !ligne.startsWith("❌")) {
                dataLines.add(ligne);
                nbLignes++;
            }
        }

        // Construire la réponse humanisée
        StringBuilder reponse = new StringBuilder();

        // Introduction selon le contexte
        if (dataLines.isEmpty()) {
            return "📭 Aucune information trouvée correspondant à votre question.";
        }

        // Réponse personnalisée
        if (nbLignes == 1) {
            reponse.append("📌 Voici ce que j'ai trouvé :\n\n");
            reponse.append("▸ ").append(dataLines.get(0)).append("\n");
        } else if (nbLignes <= 5) {
            reponse.append("📋 Voici les " + nbLignes + " éléments que j'ai trouvés :\n\n");
            for (String ligne : dataLines) {
                reponse.append("▸ ").append(ligne).append("\n");
            }
        } else {
            reponse.append("📊 J'ai trouvé " + nbLignes + " résultats. Voici un aperçu :\n\n");
            for (int i = 0; i < Math.min(5, dataLines.size()); i++) {
                reponse.append("▸ ").append(dataLines.get(i)).append("\n");
            }
            if (nbLignes > 5) {
                reponse.append("\n... et " + (nbLignes - 5) + " autre(s) résultat(s).\n");
                reponse.append("💡 Posez une question plus précise pour plus de détails.");
            }
        }

        // Ajouter une question de suivi
        reponse.append("\n\n💡 Puis-je vous aider avec autre chose ?");

        return reponse.toString();
    }

    /**
     * Réponse pour les questions incomprises
     */
    private String genererReponseIncomprise(String question) {
        return "🤔 Je ne suis pas sûr de bien comprendre votre question : \"" + question + "\"\n\n" +
                "💡 Je peux vous aider avec :\n" +
                "• Les employés (effectif, salaires, départements)\n" +
                "• Les avances et retenues\n" +
                "• Les tickets restaurant\n" +
                "• Les factures et fournisseurs\n" +
                "• Le recrutement (candidats, entretiens, formation)\n\n" +
                "Posez-moi une question plus précise ! 😊";
    }

    /**
     * Appelle l'API GROQ
     */
    private String appelerGROQ(String systemPrompt, String userQuestion) throws Exception {
        URL url = new URL(GROQ_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);

        JsonObject request = new JsonObject();
        request.addProperty("model", GROQ_MODEL);
        request.addProperty("temperature", 0.1);
        request.addProperty("max_tokens", 512);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userQuestion);
        messages.add(userMessage);

        request.add("messages", messages);

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(request.toString().getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseCode == HttpURLConnection.HTTP_OK ?
                        conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
            JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            return message.get("content").getAsString()
                    .replace("```sql", "")
                    .replace("```", "")
                    .trim();
        }

        return null;
    }

    public boolean isApiConfigured() {
        return GROQ_API_KEY != null && !GROQ_API_KEY.isEmpty() && !GROQ_API_KEY.equals("VOTRE_CLE_API_GROQ");
    }

    public boolean testConnexion() {
        try {
            String testPrompt = "Réponds simplement 'OK' si tu es opérationnel.";
            String response = appelerGROQ("Tu es un assistant. Réponds brièvement.", testPrompt);
            return response != null && response.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}