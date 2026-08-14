package com.rh.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.rh.models.Planning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "ISOBAT - Gestion RH";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Calendar service;
    private boolean isAuthenticated = false;

    public GoogleCalendarService() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = getCredentials(HTTP_TRANSPORT);
            if (credential != null) {
                service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                isAuthenticated = true;
                System.out.println("✅ Google Calendar service initialisé avec succès");
            } else {
                System.err.println("⚠️ Échec de l'authentification Google Calendar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur initialisation Google Calendar : " + e.getMessage());
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Charger le fichier credentials.json depuis les ressources
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            System.err.println("❌ Fichier credentials.json non trouvé dans les ressources !");
            System.err.println("👉 Assurez-vous que le fichier est dans: src/main/resources/credentials.json");
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Créer le dossier tokens s'il n'existe pas
        File tokenDir = new File(TOKENS_DIRECTORY_PATH);
        if (!tokenDir.exists()) {
            tokenDir.mkdirs();
            System.out.println("📁 Dossier tokens créé: " + tokenDir.getAbsolutePath());
        }

        // Configurer le flow d'autorisation
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokenDir))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        // Démarrer le serveur local pour recevoir le callback OAuth
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .setHost("localhost")
                .build();

        try {
            // Autoriser l'application
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            System.out.println("✅ Authentification Google réussie !");
            return credential;
        } catch (TokenResponseException e) {
            System.err.println("❌ Erreur d'authentification Google : " + e.getMessage());
            System.err.println("👉 Vérifiez que:");
            System.err.println("   1. L'API Google Calendar est activée");
            System.err.println("   2. Le port 8888 est disponible");
            System.err.println("   3. L'heure de votre système est correcte");
            throw e;
        }
    }

    /**
     * Ajoute un événement au Google Calendar
     */
    public boolean ajouterEvenement(Planning planning) {
        if (!isAuthenticated || service == null) {
            System.err.println("⚠️ Service Google Calendar non authentifié, synchronisation ignorée");
            return false;
        }

        try {
            Event event = new Event()
                    .setSummary(planning.getTitre())
                    .setDescription(buildDescription(planning));

            // Date et heure
            LocalTime heureDebut = planning.getHeureDebut() != null ? planning.getHeureDebut() : LocalTime.of(9, 0);
            LocalTime heureFin = planning.getHeureFin() != null ? planning.getHeureFin() : heureDebut.plusHours(1);

            ZonedDateTime startDateTime = ZonedDateTime.of(
                    planning.getDateEvenement(),
                    heureDebut,
                    ZoneId.of("Europe/Paris")
            );

            ZonedDateTime endDateTime = ZonedDateTime.of(
                    planning.getDateEvenement(),
                    heureFin,
                    ZoneId.of("Europe/Paris")
            );

            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(startDateTime.toInstant().toEpochMilli()))
                    .setTimeZone("Europe/Paris");
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(endDateTime.toInstant().toEpochMilli()))
                    .setTimeZone("Europe/Paris");
            event.setEnd(end);

            // Lieu
            if (planning.getLieu() != null && !planning.getLieu().isEmpty()) {
                event.setLocation(planning.getLieu());
            }

            // Rappels
            EventReminder emailReminder = new EventReminder()
                    .setMethod("email")
                    .setMinutes(15);

            EventReminder popupReminder = new EventReminder()
                    .setMethod("popup")
                    .setMinutes(10);

            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(emailReminder, popupReminder));

            event.setReminders(reminders);

            // Ajouter à Google Calendar
            event = service.events().insert("primary", event).execute();

            System.out.println("✅ Événement ajouté à Google Calendar : " + event.getHtmlLink());
            return true;

        } catch (TokenResponseException e) {
            System.err.println("❌ Erreur d'authentification : Token expiré ou invalide.");
            System.err.println("👉 Supprimez le dossier 'tokens' et réessayez.");
            isAuthenticated = false;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur synchronisation Google Calendar : " + e.getMessage());
            return false;
        }
    }

    private String buildDescription(Planning planning) {
        StringBuilder sb = new StringBuilder();
        sb.append("Description : ").append(planning.getDescription() != null ? planning.getDescription() : "Aucune").append("\n");
        sb.append("Type : ").append(planning.getTypeLabel()).append("\n");
        sb.append("Priorité : ").append(planning.getPrioriteLabel());
        return sb.toString();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}