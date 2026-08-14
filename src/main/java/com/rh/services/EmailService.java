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
     * Envoie un email de confirmation HTML professionnel
     */
    public void envoyerConfirmation(String destinataire, Planning planning) {
        String sujet = "✅ Confirmation : " + planning.getTitre();

        String htmlMessage = buildHtmlConfirmation(planning);

        envoyerEmailHTML(destinataire, sujet, htmlMessage);
    }

    /**
     * Envoie un email de rappel HTML
     */
    public void envoyerRappel(String destinataire, Planning planning) {
        String sujet = "🔔 Rappel : " + planning.getTitre();

        String htmlMessage = buildHtmlRappel(planning);

        envoyerEmailHTML(destinataire, sujet, htmlMessage);
    }

    /**
     * Construction du HTML de confirmation
     */
    private String buildHtmlConfirmation(Planning planning) {
        String prioriteColor = switch (planning.getPriorite()) {
            case URGENTE -> "#75070C";
            case HAUTE -> "#C8A000";
            case MOYENNE -> "#4F6815";
            default -> "#888888";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background-color: #f6f4f0; }
                    .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
                    .header { background: linear-gradient(135deg, #4F6815, #6B8A2A); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; }
                    .header p { color: #D4E8A0; margin: 8px 0 0 0; font-size: 14px; }
                    .content { padding: 30px; }
                    .event-card { background: #FAF8F5; border-radius: 12px; padding: 20px; border-left: 4px solid %s; }
                    .event-title { font-size: 20px; font-weight: bold; color: #1A1A1A; margin: 0 0 16px 0; }
                    .detail-row { display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid #F0EAE4; }
                    .detail-label { font-weight: 600; color: #666666; width: 80px; font-size: 13px; }
                    .detail-value { color: #1A1A1A; font-size: 13px; }
                    .priorite-badge { display: inline-block; background: %s; color: white; padding: 2px 12px; border-radius: 12px; font-size: 11px; font-weight: 600; }
                    .footer { background: #FAF8F5; padding: 20px; text-align: center; border-top: 1px solid #F0EAE4; }
                    .footer p { color: #999999; font-size: 12px; margin: 0; }
                    .btn-google { display: inline-block; background: #4285F4; color: white; padding: 10px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; margin-top: 16px; font-size: 13px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📅 Événement créé avec succès</h1>
                        <p>ISOBAT - Gestion RH</p>
                    </div>
                    <div class="content">
                        <div class="event-card">
                            <div class="event-title">%s</div>
                            <div class="detail-row">
                                <span class="detail-label">📅 Date</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">⏰ Horaire</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">📍 Lieu</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">📋 Type</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row" style="border-bottom: none;">
                                <span class="detail-label">🔴 Priorité</span>
                                <span class="priorite-badge">%s</span>
                            </div>
                        </div>
                        <div style="text-align: center;">
                            <a href="#" class="btn-google">➕ Ajouter à Google Calendar</a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>
                        <p>© 2026 ISOBAT - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                prioriteColor,
                prioriteColor,
                planning.getTitre(),
                planning.getDateStr(),
                planning.getHeureStr(),
                planning.getLieu() != null && !planning.getLieu().isEmpty() ? planning.getLieu() : "Non spécifié",
                planning.getTypeLabel(),
                planning.getPrioriteLabel()
        );
    }

    /**
     * Construction du HTML de rappel
     */
    private String buildHtmlRappel(Planning planning) {
        String prioriteColor = switch (planning.getPriorite()) {
            case URGENTE -> "#75070C";
            case HAUTE -> "#C8A000";
            case MOYENNE -> "#4F6815";
            default -> "#888888";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background-color: #f6f4f0; }
                    .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
                    .header { background: linear-gradient(135deg, #75070C, #C0392B); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; }
                    .header p { color: #FFB4A8; margin: 8px 0 0 0; font-size: 14px; }
                    .content { padding: 30px; }
                    .alert-box { background: #FFF0F0; border-radius: 12px; padding: 16px; border: 2px solid #75070C; text-align: center; margin-bottom: 20px; }
                    .alert-box .big-icon { font-size: 48px; }
                    .alert-box .alert-text { font-size: 16px; font-weight: bold; color: #75070C; }
                    .event-card { background: #FAF8F5; border-radius: 12px; padding: 20px; border-left: 4px solid %s; }
                    .event-title { font-size: 20px; font-weight: bold; color: #1A1A1A; margin: 0 0 16px 0; }
                    .detail-row { display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid #F0EAE4; }
                    .detail-label { font-weight: 600; color: #666666; width: 80px; font-size: 13px; }
                    .detail-value { color: #1A1A1A; font-size: 13px; }
                    .footer { background: #FAF8F5; padding: 20px; text-align: center; border-top: 1px solid #F0EAE4; }
                    .footer p { color: #999999; font-size: 12px; margin: 0; }
                    .btn-google { display: inline-block; background: #4285F4; color: white; padding: 10px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; margin-top: 16px; font-size: 13px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Rappel d'événement</h1>
                        <p>ISOBAT - Gestion RH</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <div class="big-icon">⏰</div>
                            <div class="alert-text">Cet événement aura lieu dans 15 minutes !</div>
                        </div>
                        <div class="event-card">
                            <div class="event-title">%s</div>
                            <div class="detail-row">
                                <span class="detail-label">📅 Date</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">⏰ Horaire</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">📍 Lieu</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row" style="border-bottom: none;">
                                <span class="detail-label">📋 Type</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        <div style="text-align: center;">
                            <a href="#" class="btn-google">➡️ Voir dans Google Calendar</a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Rappel automatique envoyé 15 minutes avant l'événement.</p>
                        <p>© 2026 ISOBAT - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                prioriteColor,
                planning.getTitre(),
                planning.getDateStr(),
                planning.getHeureStr(),
                planning.getLieu() != null && !planning.getLieu().isEmpty() ? planning.getLieu() : "Non spécifié",
                planning.getTypeLabel()
        );
    }

    /**
     * Envoi d'email HTML
     */
    private void envoyerEmailHTML(String destinataire, String sujet, String htmlMessage) {
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