package com.perpustakaan.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class MainViewController {

    @FXML private BorderPane mainPane;
    @FXML private StackPane contentArea;
    @FXML private VBox sidebarMenuBox;
    @FXML private Label sidebarTitle;

    private String currentUserRole;
    private String currentUsername;
    private final ToggleGroup menuToggleGroup = new ToggleGroup();

    /**
     * Menerima data dari LoginController dan memulai setup dashboard.
     */
    public void initData(String role, String username) {
        this.currentUserRole = role;
        this.currentUsername = username;
        setupDashboardByRole();
    }

    /**
     * Membangun tombol-tombol di sidebar sesuai dengan peran pengguna.
     */
    private void setupDashboardByRole() {
        sidebarMenuBox.getChildren().clear(); 

        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            sidebarTitle.setText("ADMIN PANEL");
            createMenuButton("Beranda", "HOME", "WelcomeView.fxml", true);
            createMenuButton("Kelola Buku", "BOOK", "KelolaBukuView.fxml", false);
            createMenuButton("Kelola Anggota", "USERS", "KelolaAnggotaView.fxml", false);
            createMenuButton("Pengembalian Buku", "UNDO", "PengembalianView.fxml", false);
            createMenuButton("Laporan", "BAR_CHART", "LaporanView.fxml", false);
        } else if ("Mahasiswa".equalsIgnoreCase(currentUserRole)) {
            sidebarTitle.setText("MEMBER AREA");
            createMenuButton("Beranda", "HOME", "BerandaMemberView.fxml", true);
            createMenuButton("Katalog Buku", "BOOK", "KatalogBukuView.fxml", false);
            createMenuButton("Buku Saya", "TICKET", "BukuSayaView.fxml", false);
            createMenuButton("Profil", "USER", "ProfilView.fxml", false);
        }
    }

    /**
     * Metode pembantu untuk membuat satu tombol menu.
     */
    private void createMenuButton(String text, String glyphName, String fxmlFile, boolean selectedByDefault) {
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName(glyphName);
        icon.setSize("1.2em");
        icon.getStyleClass().add("sidebar-icon");

        ToggleButton button = new ToggleButton(text);
        button.setGraphic(icon);
        button.getStyleClass().add("sidebar-button");
        button.setToggleGroup(menuToggleGroup);
        
        button.setUserData(fxmlFile); // Simpan nama file FXML di tombol
        
        button.setOnAction(_ -> navigateTo((String) button.getUserData()));

        sidebarMenuBox.getChildren().add(button);

        if (selectedByDefault) {
            navigateTo(fxmlFile);
        }
    }
    
    /**
     * Menangani aksi logout.
     */
    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) mainPane.getScene().getWindow();
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/perpustakaan/view/LoginView.fxml"));
        Scene loginScene = new Scene(loginRoot);
        loginScene.getStylesheets().add(getClass().getResource("/com/perpustakaan/view/styles.css").toExternalForm());
        currentStage.setScene(loginScene);
        currentStage.setTitle("Sistem Informasi Perpustakaan - Login");
        currentStage.setResizable(false);
        currentStage.centerOnScreen();
    }
    
    /**
     * Metode navigasi utama, dipanggil dari sidebar atau controller lain.
     */
    public void navigateTo(String fxmlFile) {
        loadView(fxmlFile);

        for (Node node : sidebarMenuBox.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                btn.setSelected(fxmlFile.equals(btn.getUserData()));
            }
        }
    }

    /**
     * Memuat file FXML dan mengirimkan data ke controller yang sesuai.
     */
    private void loadView(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/com/perpustakaan/view/" + fxmlFileName);
            if (fxmlUrl == null) {
                contentArea.getChildren().setAll(new Label("View untuk " + fxmlFileName + " belum dibuat."));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            Object controller = loader.getController();

            // Rantai if-else if untuk mengirim data ke controller yang tepat
            if (controller instanceof WelcomeController) {
                ((WelcomeController) controller).initData(currentUsername, this);
            } else if (controller instanceof BerandaMemberController) {
                ((BerandaMemberController) controller).initData(currentUsername);
            } else if (controller instanceof KatalogBukuController) {
                ((KatalogBukuController) controller).initData(currentUsername);
            } else if (controller instanceof BukuSayaController) {
                ((BukuSayaController) controller).initData(currentUsername);
            } else if (controller instanceof ProfilController) {
                ((ProfilController) controller).initData(currentUsername);
            }
            
            contentArea.getChildren().setAll(view);
            animateView(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Memberikan animasi fade-in pada view yang baru dimuat.
     */
    private void animateView(Parent node) {
        FadeTransition ft = new FadeTransition(Duration.millis(350), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}