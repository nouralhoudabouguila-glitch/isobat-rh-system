package com.rh.services;

import com.rh.models.Planning;
import com.rh.utils.EmailConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Envoie un email de rappel pour un événement
     */
    public void envoyerRappel(String destinataire, Planning planning) {
        String sujet = "🔔 Rappel : " + planning.getTitre();

        String message = construireMessageEmail(planning);

        envoyerEmail(destinataire, sujet, message);
    }

    /**
     * Envoie un email de confirmation après création d'un événement
     */
    public void envoyerConfirmation(String destinataire, Planning planning) {
        String sujet = "✅ Confirmation : " + planning.getTitre();

        String message =
                "Bonjour,\n\n" +
                        "Votre événement a été créé avec succès :\n\n" +
                        "📌 " + planning.getTitre() + "\n" +
                        "📅 " + planning.getDateStr() + "\n" +
                        "⏰ " + planning.getHeureStr() + "\n" +
                        "📍 " + (planning.getLieu() != null ? planning.getLieu() : "Lieu non spécifié") + "\n" +
                        "📋 " + planning.getTypeLabel() + " - " + planning.getPrioriteLabel() + "\n\n" +
                        "Cordialement,\n" +
                        "L'équipe ISOBAT";

        envoyerEmail(destinataire, sujet, message);
    }

    /**
     * Envoie un email de rappel à 15 minutes
     */
    private String construireMessageEmail(Planning planning) {
        return
                "🔔 RAPPEL IMPORTANT\n" +
                        "═".repeat(40) + "\n\n" +
                        "Bonjour,\n\n" +
                        "Ceci est un rappel automatique pour votre événement qui aura lieu dans 15 minutes.\n\n" +
                        "📌 " + planning.getTitre() + "\n" +
                        "📅 " + planning.getDateStr() + "\n" +
                        "⏰ " + planning.getHeureStr() + "\n" +
                        "📍 " + (planning.getLieu() != null ? planning.getLieu() : "Lieu non spécifié") + "\n" +
                        "📋 " + planning.getTypeLabel() + "\n\n" +
                        "Bonne journée !\n" +
                        "L'équipe ISOBAT";
    }

    /**
     * Envoi d'email générique
     */
    private void envoyerEmail(String destinataire, String sujet, String message) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
            props.put("mail.smtp.ssl.trust", EmailConfig.SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.EMAIL_EXPEDITEUR, EmailConfig.MOT_DE_PASSE);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EmailConfig.EMAIL_EXPEDITEUR));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject(sujet);
            msg.setText(message);

            Transport.send(msg);
            System.out.println("✅ Email envoyé à " + destinataire + " - Sujet: " + sujet);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
        }
    }

    /**
     * Envoi d'email HTML (version améliorée)
     */
    public void envoyerEmailHTML(String destinataire, String sujet, String htmlMessage) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
            props.put("mail.smtp.ssl.trust", EmailConfig.SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.EMAIL_EXPEDITEUR, EmailConfig.MOT_DE_PASSE);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EmailConfig.EMAIL_EXPEDITEUR));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject(sujet);
            msg.setContent(htmlMessage, "text/html; charset=utf-8");

            Transport.send(msg);
            System.out.println("✅ Email HTML envoyé à " + destinataire);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur envoi email HTML : " + e.getMessage());
        }
    }
}