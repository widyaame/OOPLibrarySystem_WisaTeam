package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.model.Member;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private RadioButton mahasiswaRadioButton;
    @FXML private RadioButton adminRadioButton;
    @FXML private ToggleGroup roleToggleGroup;
    @FXML private TextField usernameField;
    @FXML private PasswordField pinField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private MemberDAO memberDAO;

    public LoginController() {
        this.memberDAO = new MemberDAO();
    }

    @FXML
    public void initialize() {
        roleToggleGroup.selectedToggleProperty().addListener((_, _, _) -> {
            if (mahasiswaRadioButton.isSelected()) {
                usernameField.setPromptText("Masukkan NIM");
                pinField.setPromptText("Masukkan PIN");
            } else if (adminRadioButton.isSelected()) {
                usernameField.setPromptText("Masukkan Username");
                pinField.setPromptText("Masukkan Password");
            }
        });
    }

    @FXML
    private void handleLoginButtonAction() throws IOException {
        String username = usernameField.getText();
        String pin = pinField.getText();
        RadioButton selectedRole = (RadioButton) roleToggleGroup.getSelectedToggle();
        String role = selectedRole.getText();

        if (username.isEmpty() || pin.isEmpty()) {
            showError("Username dan PIN/Password tidak boleh kosong.");
            return;
        }

        boolean loginSuccess = false;
        if (role.equals("Admin")) {
            if (username.equalsIgnoreCase("Galih Wicaksono") && pin.equals("galih123")) {
                loginSuccess = true;
            }
        } else if (role.equals("Mahasiswa")) {
            Optional<Member> memberOpt = memberDAO.getMemberById(username);
            if (memberOpt.isPresent() && memberOpt.get().getPin().equals(pin)) {
                loginSuccess = true;
            }
        }

        if (loginSuccess) {
            System.out.println("Login Berhasil sebagai " + role + " dengan ID: " + username);
            navigateToDashboard(role, username);
        } else {
            showError("Login Gagal! Periksa kembali kredensial Anda.");
        }
    }

    private void navigateToDashboard(String role, String username) throws IOException {
        Stage stage = (Stage) loginButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/perpustakaan/view/MainView.fxml"));
        Parent mainViewRoot = loader.load();

        MainViewController controller = loader.getController();
        controller.initData(role, username);

        Scene scene = new Scene(mainViewRoot);
        scene.getStylesheets().add(getClass().getResource("/com/perpustakaan/view/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Dashboard Perpustakaan");
        stage.setResizable(true);
        stage.centerOnScreen();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }
}