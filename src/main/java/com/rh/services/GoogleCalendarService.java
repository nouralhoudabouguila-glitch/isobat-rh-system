package com.rh.services;

import com.google.api.client.auth.oauth2.Credential;
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

    public GoogleCalendarService() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère les credentials Google
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Ajoute un événement au Google Calendar
     */
    public boolean ajouterEvenement(Planning planning) {
        try {
            Event event = new Event()
                    .setSummary(planning.getTitre())
                    .setDescription(buildDescription(planning));

            // Date et heure
            ZonedDateTime startDateTime = ZonedDateTime.of(
                    planning.getDateEvenement(),
                    planning.getHeureDebut() != null ? planning.getHeureDebut() : LocalTime.of(9, 0),
                    ZoneId.of("Europe/Paris")
            );

            ZonedDateTime endDateTime = ZonedDateTime.of(
                    planning.getDateEvenement(),
                    planning.getHeureFin() != null ? planning.getHeureFin() : LocalTime.of(10, 0),
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

            // 🔥 CORRECTION : Utiliser EventReminder au lieu de Event.Reminder
            EventReminder reminder = new EventReminder()
                    .setMethod("email")
                    .setMinutes(15);

            EventReminder popupReminder = new EventReminder()
                    .setMethod("popup")
                    .setMinutes(10);

            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminder, popupReminder));

            event.setReminders(reminders);

            // Ajouter à Google Calendar
            String calendarId = "primary";
            event = service.events().insert(calendarId, event).execute();

            System.out.println("✅ Événement ajouté à Google Calendar : " + event.getHtmlLink());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur synchronisation Google Calendar : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un événement de Google Calendar
     */
    public boolean supprimerEvenement(String eventId) {
        try {
            service.events().delete("primary", eventId).execute();
            System.out.println("✅ Événement supprimé de Google Calendar");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère la liste des événements Google Calendar
     */
    public List<Event> getEvenements() {
        try {
            DateTime now = new DateTime(System.currentTimeMillis());
            List<Event> events = service.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                    .getItems();

            return events;

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Construit la description pour Google Calendar
     */
    private String buildDescription(Planning planning) {
        StringBuilder sb = new StringBuilder();
        sb.append("Description : ").append(planning.getDescription() != null ? planning.getDescription() : "Aucune").append("\n");
        sb.append("Type : ").append(planning.getTypeLabel()).append("\n");
        sb.append("Priorité : ").append(planning.getPrioriteLabel()).append("\n");
        sb.append("Créé le : ").append(planning.getDateCreation() != null ? planning.getDateCreation() : "Aujourd'hui");
        return sb.toString();
    }
}