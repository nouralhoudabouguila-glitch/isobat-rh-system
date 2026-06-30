package com.rh.services;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService {

    private static Timer timer;
    private static Runnable task;
    private static long interval = 60000; // 60 secondes par défaut

    /**
     * Démarre un timer qui exécute une tâche périodiquement
     */
    public static void startTimer(Runnable runnable, long intervalMs) {
        stopTimer();

        timer = new Timer(true);
        task = runnable;
        interval = intervalMs;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (task != null) {
                    task.run();
                }
            }
        }, 0, interval);

        System.out.println("⏰ Timer démarré (intervalle: " + intervalMs + "ms)");
    }

    /**
     * Arrête le timer
     */
    public static void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("⏰ Timer arrêté");
        }
    }

    /**
     * Redémarre le timer avec le même intervalle
     */
    public static void restartTimer() {
        if (task != null) {
            startTimer(task, interval);
        }
    }

    /**
     * Vérifie si le timer est actif
     */
    public static boolean isRunning() {
        return timer != null;
    }
}