package org.openjfx.BookCatalogueClient.task;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;

public class TaskTracker {
    private final int total;
    private final Runnable onAllSuccess;
    private final Runnable onAnyError;
    private final AtomicInteger finished = new AtomicInteger(0);
    private final AtomicBoolean errorOccurred = new AtomicBoolean(false);

    public TaskTracker(int total, Runnable onAllSuccess, Runnable onAnyError) {
        this.total = total;
        this.onAllSuccess = onAllSuccess;
        this.onAnyError = onAnyError;
    }

    public void taskSucceeded() {
        checkFinished(false);
    }

    public void taskFailed() {
        checkFinished(true);
    }

    private void checkFinished(boolean isError) {
        if (isError) {
            errorOccurred.set(true);
        }

        if (finished.incrementAndGet() == total) {
            Platform.runLater(() -> {
                if (errorOccurred.get()) {
                    if (onAnyError != null) onAnyError.run();
                } else {
                    if (onAllSuccess != null) onAllSuccess.run();
                }
            });
        }
    }
}