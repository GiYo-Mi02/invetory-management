package com.ccissc.inventory.util;

import com.ccissc.inventory.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.util.Duration;

/**
 * Monitors user inactivity and automatically logs out + redirects to the Login
 * screen after a configurable number of minutes.
 */
public final class SessionTimeoutManager {
    private static final int DEFAULT_TIMEOUT_MINUTES = 15;
    private static SessionTimeoutManager instance;

    private Timeline timeoutTimeline;
    private EventHandler<InputEvent> activityFilter;
    private Scene currentScene;

    private SessionTimeoutManager() {
    }

    public static synchronized SessionTimeoutManager getInstance() {
        if (instance == null) {
            instance = new SessionTimeoutManager();
        }
        return instance;
    }

    /**
     * Start monitoring the given scene for inactivity.
     */
    public void start(Scene scene) {
        stop(); // clean up any previous wiring

        currentScene = scene;

        timeoutTimeline = new Timeline(new KeyFrame(
                Duration.minutes(DEFAULT_TIMEOUT_MINUTES),
                event -> onTimeout()));
        timeoutTimeline.setCycleCount(1);
        timeoutTimeline.play();

        activityFilter = event -> resetTimer();
        scene.addEventFilter(InputEvent.ANY, activityFilter);
    }

    /**
     * Stop monitoring — called on logout or when switching to the Login screen.
     */
    public void stop() {
        if (timeoutTimeline != null) {
            timeoutTimeline.stop();
            timeoutTimeline = null;
        }
        if (currentScene != null && activityFilter != null) {
            currentScene.removeEventFilter(InputEvent.ANY, activityFilter);
        }
        activityFilter = null;
        currentScene = null;
    }

    private void resetTimer() {
        if (timeoutTimeline != null) {
            timeoutTimeline.playFromStart();
        }
    }

    private void onTimeout() {
        stop();
        new AuthService().logout();
        NavigationUtil.switchTo("Login.fxml");
    }
}
