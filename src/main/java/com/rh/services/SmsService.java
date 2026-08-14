package com.rh.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID") != null
            ? System.getenv("TWILIO_ACCOUNT_SID")
            : "VOTRE_ACCOUNT_SID"; // 🔥 Remplace par ton SID

    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN") != null
            ? System.getenv("TWILIO_AUTH_TOKEN")
            : "VOTRE_AUTH_TOKEN"; // 🔥 Remplace par ton Token

    private static final String FROM_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER") != null
            ? System.getenv("TWILIO_PHONE_NUMBER")
            : "+1234567890"; // 🔥 Remplace par ton numéro Twilio

    private static boolean initialized = false;

    public SmsService() {
        if (!initialized) {
            try {
                if (!ACCOUNT_SID.equals("VOTRE_ACCOUNT_SID") && !AUTH_TOKEN.equals("VOTRE_AUTH_TOKEN")) {
                    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                    initialized = true;
                    System.out.println("✅ Twilio initialisé avec succès");
                } else {
                    System.out.println("⚠️ Mode simulation Twilio (config manquante)");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur initialisation Twilio : " + e.getMessage());
            }
        }
    }

    /**
     * Envoie un SMS avec le code de réinitialisation
     */
    public boolean envoyerCodeReinitialisation(String telephone, String code) {
        // 🔥 Mode simulation si Twilio n'est pas configuré
        if (!initialized) {
            System.out.println("📱 === SMS SIMULATION ===");
            System.out.println("📱 Téléphone : " + telephone);
            System.out.println("📱 Code : " + code);
            System.out.println("📱 ======================");
            return true;
        }

        try {
            String message = String.format(
                    "🔐 ISOBAT - Code de réinitialisation\n\n" +
                            "Votre code de validation est : %s\n" +
                            "Ce code est valable 5 minutes.\n\n" +
                            "Ne partagez ce code avec personne.",
                    code
            );

            Message.creator(
                    new PhoneNumber(telephone),
                    new PhoneNumber(FROM_PHONE_NUMBER),
                    message
            ).create();

            System.out.println("✅ SMS envoyé à " + telephone);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi SMS : " + e.getMessage());
            return false;
        }
    }
}