package com.rh.scheduler;

import com.rh.services.ServiceNotification;
import javafx.application.Platform;
import com.rh.contollers.MainController;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static NotificationScheduler instance;
    private ScheduledExecutorService scheduler;
    private ServiceNotification serviceNotification;
    private ScheduledFuture<?> scheduledFuture;
    private boolean isRunning = false;

    private NotificationScheduler() {
        serviceNotification = new ServiceNotification();
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setName("NotificationScheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public static NotificationScheduler getInstance() {
        if (instance == null) {
            instance = new NotificationScheduler();
        }
        return instance;
    }

    public void demarrer() {
        if (isRunning) {
            System.out.println("⚠️ Le scheduler est déjà en cours d'exécution");
            return;
        }

        System.out.println("🔄 Démarrage du scheduler de notifications...");

        executerVerification();

        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            executerVerification();
        }, 5, 5, TimeUnit.MINUTES);

        isRunning = true;
        System.out.println("✅ Scheduler de notifications démarré (vérification toutes les 5 minutes)");
    }

    private void executerVerification() {
        try {
            System.out.println("🔔 [" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Vérification des notifications...");

            Thread verificationThread = new Thread(() -> {
                try {
                    serviceNotification.verifierEtGenererNotifications();

                    Platform.runLater(() -> {
                        MainController mainController = MainController.getInstance();
                        if (mainController != null) {
                            // La méthode sera ajoutée plus tard
                        }
                    });
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de la vérification : " + e.getMessage());
                    e.printStackTrace();
                }
            });
            verificationThread.setName("NotificationVerification");
            verificationThread.setDaemon(true);
            verificationThread.start();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'exécution du scheduler : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void executerVerificationManuelle() {
        System.out.println("🔄 Exécution manuelle de la vérification...");
        executerVerification();
    }

    public boolean isRunning() {
        return isRunning && scheduledFuture != null && !scheduledFuture.isCancelled();
    }

    public void arreter() {
        if (!isRunning) {
            System.out.println("⚠️ Le scheduler n'est pas en cours d'exécution");
            return;
        }

        System.out.println("⏹️ Arrêt du scheduler de notifications...");

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        isRunning = false;
        System.out.println("✅ Scheduler de notifications arrêté");
    }

    public String getStatistiques() {
        return "Scheduler de notifications\n" +
                "Statut : " + (isRunning() ? "✅ Actif" : "⏹️ Arrêté") + "\n" +
                "Périodicité : 5 minutes";
    }
}