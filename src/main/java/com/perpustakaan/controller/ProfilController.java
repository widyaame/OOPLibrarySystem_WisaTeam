package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.model.Member;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Optional;

public class ProfilController {

    @FXML private Label welcomeLabel;
    @FXML private Label memberIdHeaderLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label majorLabel;
    @FXML private TextField emailField;
    @FXML private Hyperlink ubahPinLink;
    @FXML private VBox pinContainer;
    @FXML private PasswordField pinField;
    @FXML private PasswordField konfirmasiPinField;
    @FXML private Button simpanButton;

    private MemberDAO memberDAO;
    private Member currentMember;

    public ProfilController() {
        this.memberDAO = new MemberDAO();
    }

    @FXML
    public void initialize() {
        // Posisikan form PIN di atas (tersembunyi) dan buat tidak terlihat
        pinContainer.setTranslateY(-pinContainer.getHeight());
        pinContainer.setVisible(false);
        pinContainer.setManaged(false);
    }

    public void initData(String memberId) {
        Optional<Member> memberOpt = memberDAO.getMemberById(memberId);
        if (memberOpt.isPresent()) {
            this.currentMember = memberOpt.get();
            populateProfileData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Data anggota tidak ditemukan.");
        }
    }

    private void populateProfileData() {
        welcomeLabel.setText("Halo, " + currentMember.getFullName().split(" ")[0] + "!");
        memberIdHeaderLabel.setText("ID Anggota: " + currentMember.getMemberId());
        fullNameLabel.setText(currentMember.getFullName());
        majorLabel.setText(currentMember.getMajor());
        emailField.setText(currentMember.getEmail());
    }
    
    @FXML
    private void handleUbahPinLink() {
        // Tampilkan form PIN dengan animasi geser ke bawah
        pinContainer.setVisible(true);
        pinContainer.setManaged(true);
        ubahPinLink.setVisible(false);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(350), pinContainer);
        tt.setToY(0);
        tt.play();
    }

    @FXML
    private void handleSimpanButton() {
        if (currentMember == null) return;

        // Hanya proses validasi PIN jika formnya terlihat
        if (pinContainer.isVisible()) {
            String newPin = pinField.getText();
            String confirmPin = konfirmasiPinField.getText();

            if (!newPin.isEmpty() || !confirmPin.isEmpty()) {
                if (!newPin.equals(confirmPin)) {
                    showAlert(Alert.AlertType.ERROR, "Error Validasi", "PIN baru dan konfirmasi PIN tidak cocok.");
                    return;
                }
                if (newPin.length() < 4) {
                    showAlert(Alert.AlertType.ERROR, "Error Validasi", "PIN baru minimal harus 4 digit.");
                    return;
                }
                currentMember.setPin(newPin);
            }
        }

        currentMember.setEmail(emailField.getText());
        memberDAO.updateMember(currentMember);

        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Profil Anda berhasil diperbarui.");
        
        // Reset tampilan perubahan PIN dengan animasi geser ke atas
        hidePinForm();
    }

    private void hidePinForm() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(350), pinContainer);
        tt.setToY(-pinContainer.getHeight());
        tt.setOnFinished(_ -> {
            pinContainer.setVisible(false);
            pinContainer.setManaged(false);
            ubahPinLink.setVisible(true);
            pinField.clear();
            konfirmasiPinField.clear();
        });
        tt.play();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}