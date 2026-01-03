package org.example.utils; // Or whatever package you prefer

import org.example.Admin.SystemLogDAO;

import javax.swing.*;
import java.awt.event.ActionListener;


public class AutoRefresher {
    private Timer timer;
    private int lastLogId = 0;
    private final String actionFilter;
    private final Runnable refreshAction;
    private final SystemLogDAO logDAO;


    public AutoRefresher(String actionFilter, Runnable refreshAction) {
        this.actionFilter = actionFilter;
        this.refreshAction = refreshAction;
        this.logDAO = new SystemLogDAO();
        this.lastLogId = logDAO.getLatestLogId(actionFilter);

        startMonitoring();
    }

    private void startMonitoring() {

        timer = new Timer(3000, e -> checkForUpdates());
        timer.setRepeats(true);
        timer.start();
    }

    private void checkForUpdates() {
        // Run database check in background to avoid freezing UI
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                // This is the lightweight check!
                return logDAO.getLatestLogId(actionFilter);
            }

            @Override
            protected void done() {
                try {
                    int currentMaxId = get();
                    // If the database has a newer ID than what we remember...
                    if (currentMaxId > lastLogId) {
                        System.out.println("🔄 New Log Detected (ID: " + currentMaxId + "). Refreshing...");
                        lastLogId = currentMaxId; // Update our memory
                        refreshAction.run(); // TRIGGER THE REFRESH!
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }
}