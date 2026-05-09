package com.example.oop2_capstone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import workers.AppPrefs;
import workers.MockDataProvider;
import workers.SessionManager;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        String savedEmail = AppPrefs.prefs().get(AppPrefs.KEY_EMAIL, null);
        if (savedEmail != null && !savedEmail.isBlank()) {
            User u = MockDataProvider.findUserByEmail(savedEmail);
            if (u != null && u.isActive()) {
                SessionManager.setLoggedUser(u);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/MainLayout.fxml"));
                Scene scene = new Scene(loader.load(), 1280, 800);
                stage.setTitle("TicketFlow");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
                return;
            }
            AppPrefs.prefs().remove(AppPrefs.KEY_EMAIL);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("TicketFlow - Login");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}