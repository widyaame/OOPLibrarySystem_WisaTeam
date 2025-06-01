package com.perpustakaan; // Pastikan package ini sesuai dengan lokasi App.java Anda

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.application.Platform; // Pastikan impor ini ada

import java.io.IOException;
import java.io.InputStream; // Untuk memeriksa keberadaan FXML

public class App extends Application {

    /**
     * Metode start adalah entry point utama untuk semua aplikasi JavaFX.
     * Metode ini dipanggil setelah metode init() selesai, dan setelah sistem
     * siap untuk aplikasi mulai berjalan.
     *
     * @param primaryStage Stage utama (jendela utama) untuk aplikasi ini, awalnya untuk Login.
     */
    @Override
    public void start(Stage primaryStage) {
        // Path sekarang ke LoginView.fxml sebagai tampilan awal aplikasi.
        String fxmlPath = "/com/perpustakaan/view/LoginView.fxml"; 

        try {
            // 1. Verifikasi keberadaan file FXML utama.
            InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath);
            if (fxmlStream == null) {
                System.err.println("Kesalahan Kritis: File LoginView.fxml tidak ditemukan di: " + fxmlPath);
                // Mengambil nama file dan direktori untuk pesan error yang lebih informatif.
                String fxmlFileName = fxmlPath.contains("/") ? fxmlPath.substring(fxmlPath.lastIndexOf("/") + 1) : fxmlPath;
                String fxmlDir = fxmlPath.contains("/") ? fxmlPath.substring(0, fxmlPath.lastIndexOf("/") + 1) : "/";
                showErrorDialog("Kesalahan Pemuatan Aplikasi",
                                "File Login (" + fxmlFileName + ") tidak ditemukan.",
                                "Aplikasi tidak dapat dimulai. Pastikan file FXML ada di lokasi yang benar: " + 
                                fxmlDir + 
                                "\nHarap periksa struktur folder 'src/main/resources'.");
                Platform.exit(); // Keluar dari aplikasi jika FXML utama tidak ditemukan.
                return;
            }
            fxmlStream.close(); // Tutup InputStream setelah pengecekan.

            // 2. Muat file FXML.
            // FXMLLoader akan membaca file FXML dan membuat hierarki objek UI serta controllernya.
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load(); // Root node dari LoginView.fxml (dalam kasus ini, VBox).
            
            // 3. Buat Scene baru dengan root node yang sudah dimuat.
            Scene scene = new Scene(root);
            
            // 4. Atur judul untuk Stage (jendela login).
            primaryStage.setTitle("Login - Sistem Informasi Perpustakaan");
            
            // (Opsional) Jendela login biasanya ukurannya tetap.
            // primaryStage.setResizable(false); 
            
            // 5. Atur Scene ke Stage.
            primaryStage.setScene(scene);
            
            // 6. Tampilkan Stage.
            primaryStage.show();
            
        } catch (IOException e) {
            // Tangani error jika terjadi masalah I/O saat memuat FXML utama.
            e.printStackTrace(); // Cetak stack trace error ke konsol untuk debugging.
            showErrorDialog("Kesalahan Pemuatan Aplikasi",
                            "Gagal memuat antarmuka login.",
                            "Terjadi kesalahan teknis saat mencoba memuat file FXML: " + fxmlPath + 
                            "\nDetail: " + e.getMessage() +
                            "\nSilakan lihat konsol untuk detail error.");
            Platform.exit(); // Keluar jika FXML utama gagal dimuat.
        } catch (Exception e) {
            // Tangani error umum lainnya yang mungkin terjadi saat startup.
            e.printStackTrace();
             showErrorDialog("Kesalahan Tidak Terduga",
                            "Terjadi kesalahan tidak terduga saat memulai aplikasi.",
                            "Detail: " + e.getMessage());
            Platform.exit(); // Keluar jika terjadi error tak terduga.
        }
    }

    /**
     * Metode helper untuk menampilkan dialog error secara konsisten.
     * @param title Judul dialog error.
     * @param header Teks header untuk dialog error (bisa null).
     * @param content Pesan detail error.
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Memastikan dialog ditampilkan di thread JavaFX jika dipanggil dari thread lain.
        if (Platform.isFxApplicationThread()) {
            alert.showAndWait();
        } else {
            // Jika ini dipanggil dari thread non-FX, jalankan di thread FX.
            Platform.runLater(alert::showAndWait);
        }
    }

    /**
     * Metode main adalah entry point tradisional untuk aplikasi Java.
     * Untuk aplikasi JavaFX, metode ini biasanya hanya memanggil launch(args)
     * untuk memulai siklus hidup aplikasi JavaFX.
     *
     * @param args Argumen baris perintah yang di-pass ke aplikasi (biasanya tidak digunakan di sini).
     */
    public static void main(String[] args) {
        launch(args); // Memulai aplikasi JavaFX.
    }
}