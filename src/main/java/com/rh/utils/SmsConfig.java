package com.rh.utils;

public class SmsConfig {
    // 🔥 Remplace par tes identifiants Twilio
    public static final String ACCOUNT_SID = "VOTRE_ACCOUNT_SID";
    public static final String AUTH_TOKEN = "VOTRE_AUTH_TOKEN";
    public static final String FROM_PHONE_NUMBER = "+1234567890"; // Numéro Twilio

    // Code de validation (6 chiffres)
    public static final int CODE_LENGTH = 6;
    public static final int CODE_EXPIRATION_MINUTES = 5;
}