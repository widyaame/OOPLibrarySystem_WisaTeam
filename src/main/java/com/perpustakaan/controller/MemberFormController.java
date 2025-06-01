package com.perpustakaan.controller;

import com.perpustakaan.model.Member;
import com.perpustakaan.model.LibraryManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField; // <-- Impor PasswordField
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class MemberFormController implements NeedsLibraryManager {

    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField idField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField; // <-- @FXML untuk PasswordField BARU

    // Tombol simpan tidak perlu @FXML jika tidak dimanipulasi langsung, tapi tidak apa-apa jika ada
    // @FXML
    // private Button simpanButton; 

    private LibraryManager libraryManager;
    private Stage dialogStage;
    private Member memberToEdit;
    private boolean isEditMode = false;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Mengisi form untuk mode edit.
     * Password biasanya tidak ditampilkan atau diisi ulang untuk diedit karena alasan keamanan,
     * kecuali ada fitur "ubah password" yang terpisah.
     * Untuk form edit member umum, kita akan mengosongkan field password atau tidak menampilkannya.
     * Jika ingin mengedit password, perlu field konfirmasi password baru.
     * Untuk saat ini, kita akan mengosongkan password field saat edit dan password hanya di-set saat tambah baru.
     * atau jika ada password lama, jangan diubah kecuali ada input baru.
     */
    public void setMemberToEdit(Member member) {
        this.memberToEdit = member;
        this.isEditMode = true;

        formTitleLabel.setText("Edit Data Anggota");
        idField.setText(member.getId());
        idField.setDisable(true); 
        fullNameField.setText(member.getFullName());
        majorField.setText(member.getMajor() != null ? member.getMajor() : ""); 
        emailField.setText(member.getEmail());
        
        // Untuk mode edit, kita tidak mengisi field password dengan password lama.
        // Jika pengguna ingin mengubah password, mereka harus mengetik yang baru.
        // Jika field password kosong saat edit, kita bisa memilih untuk tidak mengubah password yang sudah ada.
        passwordField.setPromptText("Kosongkan jika tidak ingin mengubah password"); 
        // Atau, jika fitur ubah password adalah bagian dari form ini:
        // passwordField.setPromptText("Masukkan password baru jika ingin diubah");
    }

    @FXML
    private void initialize() {
        // Setup awal
    }

    @FXML
    private void handleSimpanAction(ActionEvent event) {
        if (!isInputValid()) { // Validasi juga akan mengecek password jika mode tambah
            return;
        }

        String id = idField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String major = majorField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText(); // Ambil password (tidak di-trim)

        if (isEditMode) {
            if (memberToEdit == null) {
                showAlert(Alert.AlertType.ERROR, "Kesalahan Edit", "Objek anggota untuk diedit tidak ditemukan.", "Silakan coba lagi.");
                return;
            }
            if (!memberToEdit.getEmail().equalsIgnoreCase(email) && libraryManager.findMemberByEmail(email) != null) {
                showAlert(Alert.AlertType.ERROR, "Edit Gagal", "Email sudah terdaftar.", "Email '" + email + "' sudah digunakan oleh anggota lain.");
                emailField.requestFocus();
                return;
            }

            memberToEdit.setFullName(fullName);
            memberToEdit.setMajor(major.isEmpty() ? null : major); 
            memberToEdit.setEmail(email);
            
            // Logika untuk update password saat edit:
            // Hanya update password jika field password diisi.
            if (!password.isEmpty()) {
                memberToEdit.setPassword(password);
            } 
            // Jika field password kosong, password lama memberToEdit tidak diubah.

            boolean updated = libraryManager.updateMember(memberToEdit);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Update Berhasil", "Data anggota berhasil diperbarui.", null);
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Gagal", "Gagal memperbarui data anggota.", "Terjadi kesalahan.");
            }
        } else { // Mode Tambah
            if (libraryManager.findMemberById(id) != null) {
                showAlert(Alert.AlertType.ERROR, "Tambah Gagal", "ID Anggota sudah ada.", "ID Anggota '" + id + "' sudah terdaftar.");
                idField.requestFocus();
                return;
            }
            if (libraryManager.findMemberByEmail(email) != null) {
                showAlert(Alert.AlertType.ERROR, "Tambah Gagal", "Email sudah terdaftar.", "Email '" + email + "' sudah digunakan.");
                emailField.requestFocus();
                return;
            }
            // Validasi password (misalnya tidak boleh kosong) sudah ada di isInputValid() untuk mode Tambah

            // Panggil constructor Member dengan 5 argumen
            Member newMember = new Member(id, fullName, (major.isEmpty() ? null : major), email, password);
            boolean added = libraryManager.addMember(newMember);
            if (added) {
                showAlert(Alert.AlertType.INFORMATION, "Tambah Berhasil", "Anggota baru berhasil ditambahkan.", null);
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Tambah Gagal", "Gagal menambahkan anggota baru.", "Pastikan ID dan Email unik.");
            }
        }
    }

    @FXML
    private void handleBatalAction(ActionEvent event) {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (idField.getText() == null || idField.getText().trim().isEmpty()) {
            errorMessage += "ID Anggota tidak boleh kosong!\n";
        }
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            errorMessage += "Nama Lengkap tidak boleh kosong!\n";
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "Email tidak boleh kosong!\n";
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            errorMessage += "Format email tidak valid! (Contoh: user@example.com)\n";
        }

        // Validasi password HANYA jika dalam mode TAMBAH,
        // atau jika dalam mode EDIT dan field password DIISI (menandakan ingin diubah).
        // Untuk kesederhanaan, kita bisa buat password wajib diisi saat mode Tambah.
        if (!isEditMode) { // Wajib diisi saat mode Tambah
            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                errorMessage += "Password tidak boleh kosong!\n";
            }
            // Anda bisa menambahkan validasi panjang minimal password di sini jika perlu
            // else if (passwordField.getText().length() < 6) {
            //     errorMessage += "Password minimal 6 karakter!\n";
            // }
        } else { // Mode Edit
            // Jika field password diisi saat edit, mungkin Anda ingin validasi panjangnya juga.
            // Jika field password kosong saat edit, itu berarti tidak ingin mengubah password.
            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                // Tambahkan validasi panjang minimal jika password diisi untuk diubah
                // if (passwordField.getText().length() < 6) {
                //     errorMessage += "Password baru minimal 6 karakter!\n";
                // }
            }
        }


        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Harap perbaiki field yang tidak valid:", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (dialogStage != null && dialogStage.getOwner() != null) { // Pastikan dialogStage sudah diinisialisasi
             alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }
}