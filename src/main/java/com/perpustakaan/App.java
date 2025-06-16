package com.perpustakaan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    /**
     * Metode start adalah titik masuk utama untuk semua aplikasi JavaFX.
     * @param primaryStage Jendela utama yang disediakan oleh JavaFX.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Memuat file FXML untuk tampilan login.
        URL fxmlLocation = getClass().getResource("/com/perpustakaan/view/LoginView.fxml");
        if (fxmlLocation == null) {
            System.err.println("Tidak dapat menemukan file FXML. Pastikan path sudah benar.");
            return;
        }
        Parent root = FXMLLoader.load(fxmlLocation);

        // 2. Membuat Scene baru dengan root dari FXML.
        Scene scene = new Scene(root);

        // 3. Memuat dan menerapkan file CSS untuk styling.
        URL cssLocation = getClass().getResource("/com/perpustakaan/view/styles.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        } else {
            System.err.println("Tidak dapat menemukan file CSS.");
        }

        // 4. Mengatur properti jendela (Stage).
        primaryStage.setTitle("Sistem Informasi Perpustakaan");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Opsional: membuat ukuran jendela tidak bisa diubah.

        // 5. Menampilkan jendela ke layar.
        primaryStage.show();
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}