package com.perpustakaan.controller;

import com.perpustakaan.model.LibraryManager;
import com.perpustakaan.model.Member;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality; // <-- Tambahkan impor ini untuk dialog modal
import javafx.stage.Stage;
import javafx.stage.Window; 

import java.io.IOException;
import java.io.InputStream;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton; 
    @FXML
    private Label errorLabel;

    private LibraryManager libraryManager;

    public LoginController() {
        this.libraryManager = new LibraryManager();
        System.out.println("LoginController instance created with LibraryManager.");
    }

    @FXML
    private void initialize() {
        hideError(); 
        System.out.println("LoginController initialized via FXML.");
        passwordField.setOnAction(this::handleLoginAction);
        usernameField.setOnAction(this::handleLoginAction); 
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText(); 

        if (usernameInput.isEmpty()) {
            showError("Username/ID Anggota tidak boleh kosong.");
            usernameField.requestFocus();
            return;
        }
        if (passwordInput.isEmpty()) {
            showError("Password tidak boleh kosong.");
            passwordField.requestFocus();
            return;
        }

        loginButton.setDisable(true);
        hideError(); 

        String userRole = null;
        String actualUserId = null; 
        String displayName = null;  

        if ("admin".equalsIgnoreCase(usernameInput) && "admin123".equals(passwordInput)) {
            userRole = "ADMIN";
            actualUserId = "admin"; 
            displayName = "Administrator";
        } else {
            Member member = libraryManager.findMemberById(usernameInput); 
            if (member != null && member.getPassword() != null && member.getPassword().equals(passwordInput)) {
                userRole = "MEMBER";
                actualUserId = member.getId();       
                displayName = member.getFullName(); 
            }
        }

        if (userRole != null) {
            System.out.println("Login berhasil! ID: " + actualUserId + ", Peran: " + userRole + ", Nama: " + displayName);
            
            Window currentWindow = loginButton.getScene().getWindow();
            if (currentWindow instanceof Stage) {
                ((Stage) currentWindow).close();
            }

            loadMainView(userRole, actualUserId, displayName);
        } else {
            System.out.println("Login gagal untuk ID: " + usernameInput);
            showError("Username/ID Anggota atau password salah!");
            passwordField.clear();
            passwordField.requestFocus();
            loginButton.setDisable(false); 
        }
    }

    private void loadMainView(String userRole, String actualUserId, String displayName) {
        try {
            String fxmlPath = "/com/perpustakaan/view/MainView.fxml";
            
            InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath);
            if (fxmlStream == null) {
                System.err.println("Kesalahan Internal Aplikasi: File MainView.fxml tidak ditemukan di " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Kesalahan Kritis Aplikasi", 
                          "File utama aplikasi tidak ditemukan.", 
                          "Harap hubungi administrator sistem. Path: " + fxmlPath);
                Platform.exit(); 
                return;
            }
            fxmlStream.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object loadedController = loader.getController();
            if (loadedController instanceof MainController) {
                MainController mainController = (MainController) loadedController;
                mainController.initUserSession(actualUserId, userRole, displayName);
            } else {
                 System.err.println("MainController tidak ditemukan atau tipe tidak sesuai setelah memuat MainView.fxml.");
                 showAlert(Alert.AlertType.ERROR, "Kesalahan Internal Aplikasi", 
                           "Gagal menginisialisasi tampilan utama.", 
                           "Controller utama tidak dapat diakses.");
                 return; 
            }

            Stage mainStage = new Stage();
            mainStage.setTitle("Sistem Informasi Perpustakaan - [" + userRole + (displayName != null ? ": " + displayName : "") + "]");
            mainStage.setScene(new Scene(root));
            mainStage.setMaximized(true); 
            mainStage.setOnCloseRequest(event -> { // Ganti nama parameter dari 'e' ke 'event' agar lebih jelas
                Platform.exit();
                System.exit(0); 
            });
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Pemuatan", 
                      "Gagal memuat tampilan utama aplikasi.", 
                      "Detail: " + e.getMessage());
            Platform.exit(); 
        } catch (Exception e) { 
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Tidak Terduga", 
                      "Terjadi kesalahan tidak terduga saat memuat tampilan utama.", 
                      "Detail: " + e.getMessage());
            Platform.exit();
        }
    }
    
    // ================================================================================
    // === METODE BARU UNTUK MEMBUKA FORMULIR REGISTRASI ANGGOTA ===
    // ================================================================================
    @FXML
    private void handleOpenRegistrationFormAction(ActionEvent event) {
        System.out.println("Link/Tombol 'Daftar di sini' diklik.");
        try {
            // Path ke FXML untuk form registrasi anggota
            String fxmlPath = "/com/perpustakaan/view/MemberRegistrationView.fxml"; 

            // Verifikasi keberadaan file FXML
            InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath);
            if (fxmlStream == null) {
                System.err.println("File FXML registrasi (" + fxmlPath + ") tidak ditemukan.");
                showError("Formulir registrasi saat ini tidak tersedia. Silakan coba lagi nanti atau hubungi administrator.");
                return;
            }
            fxmlStream.close(); // Tutup stream setelah pengecekan

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent registrationRoot = loader.load();

            // Dapatkan controller dari form registrasi jika perlu meneruskan LibraryManager
            Object loadedRegController = loader.getController();
            if (loadedRegController instanceof NeedsLibraryManager) {
                // MemberRegistrationController akan membutuhkan LibraryManager untuk:
                // 1. Mengecek duplikasi ID atau Email saat pengguna mencoba mendaftar.
                // 2. Menambahkan member baru ke sistem jika registrasi valid.
                ((NeedsLibraryManager) loadedRegController).setLibraryManager(this.libraryManager);
                System.out.println("LibraryManager diteruskan ke MemberRegistrationController.");
            }
            // (Opsional) Jika MemberRegistrationController perlu menutup dirinya atau info lain:
            // if (loadedRegController instanceof MemberRegistrationController) {
            //     // Contoh: ((MemberRegistrationController) loadedRegController).setOwnerStage((Stage) loginButton.getScene().getWindow());
            // }


            Stage registrationStage = new Stage();
            registrationStage.setTitle("Formulir Registrasi Anggota Baru");
            registrationStage.setScene(new Scene(registrationRoot));
            
            // Buat dialog registrasi modal agar pengguna fokus menyelesaikan atau membatalkan registrasi
            registrationStage.initModality(Modality.APPLICATION_MODAL); 
            
            // (Opsional) Atur owner stage jika Anda ingin dialog ini terkait dengan jendela login
            Window owner = loginButton.getScene().getWindow();
            if (owner != null) {
               registrationStage.initOwner(owner);
            }
            
            registrationStage.showAndWait(); // Tampilkan dialog dan tunggu sampai ditutup

            // Setelah form registrasi ditutup, tidak ada aksi spesifik yang perlu dilakukan di sini
            // kecuali Anda ingin menampilkan pesan (misalnya, "Silakan login dengan akun baru Anda").
            System.out.println("Dialog registrasi anggota ditutup.");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Gagal membuka formulir registrasi. Detail: " + e.getMessage());
        } catch (Exception e) { // Menangkap exception lain yang mungkin tidak terduga
            e.printStackTrace();
            showError("Terjadi kesalahan tidak terduga saat membuka formulir registrasi. Detail: " + e.getMessage());
        }
    }
    // ================================================================================


    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}