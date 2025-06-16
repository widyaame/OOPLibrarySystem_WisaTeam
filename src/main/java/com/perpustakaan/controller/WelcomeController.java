package com.perpustakaan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class WelcomeController {
    @FXML private Label welcomeLabel;
    @FXML private VBox kelolaBukuCard;
    @FXML private VBox kelolaAnggotaCard;
    @FXML private VBox pengembalianCard;
    private MainViewController mainViewController;


    public void initData(String username, MainViewController mainController) {
        this.mainViewController = mainController;
        String firstName = username.equalsIgnoreCase("admin") ? "Admin" : username;
        welcomeLabel.setText("Selamat Datang, " + firstName + "!");
    }

    @FXML
    public void initialize() {
        kelolaBukuCard.setOnMouseClicked(_ -> goToKelolaBuku());
        kelolaAnggotaCard.setOnMouseClicked(_ -> goToKelolaAnggota());
        pengembalianCard.setOnMouseClicked(_ -> goToPengembalian());
    }

    private void goToKelolaBuku() {
        if (mainViewController != null) {
            mainViewController.navigateTo("KelolaBukuView.fxml");
        }
    }
    
    private void goToKelolaAnggota() {
        if (mainViewController != null) {
            mainViewController.navigateTo("KelolaAnggotaView.fxml");
        }
    }

    private void goToPengembalian() {
        if (mainViewController != null) {
            mainViewController.navigateTo("PengembalianView.fxml");
        }
    }
}