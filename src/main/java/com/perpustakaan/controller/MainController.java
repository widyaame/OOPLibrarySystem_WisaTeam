package com.perpustakaan.controller;

import com.perpustakaan.model.LibraryManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;     // Impor untuk tipe Menu
import javafx.scene.control.MenuItem; // Impor untuk tipe MenuItem
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;

public class MainController {

    private LibraryManager libraryManager;

    // Variabel untuk menyimpan informasi pengguna yang login
    private String currentUserId; 
    private String currentUserRole;
    private String currentDisplayName; 

    // === Injeksi elemen Menu dan MenuItem dari MainView.fxml ===
    // Pastikan fx:id di FXML Anda sesuai dengan nama variabel ini.
    @FXML private Menu fileMenu;
    @FXML private Menu manajemenMenu;
    @FXML private Menu transaksiMenu;
    @FXML private Menu laporanMenu;
    @FXML private Menu akunSayaMenu; 

    // MenuItem spesifik yang visibilitasnya ingin kita kontrol
    @FXML private MenuItem peminjamanMenuItem; 
    @FXML private MenuItem pengembalianMenuItem; 
    @FXML private MenuItem riwayatPeminjamanMenuItem;
    
    // Anda juga memiliki fx:id untuk item menu laporan di FXML Anda:
    // Jika Anda ingin mengontrol visibilitasnya secara individual, tambahkan juga:
    // @FXML private MenuItem laporanBukuDipinjamMenuItem;
    // @FXML private MenuItem statistikBulananMenuItem;


    public MainController() {
        this.libraryManager = new LibraryManager();
        System.out.println("LibraryManager instance created in MainController constructor.");
    }

    @FXML
    private void initialize() {
        System.out.println("MainController initialized via FXML.");
        // Sembunyikan menu spesifik peran pada awalnya, akan diatur setelah login
        adjustMenusForRole(null); 
    }

    /**
     * Dipanggil oleh LoginController setelah login berhasil.
     * Menyimpan informasi sesi dan menyesuaikan UI berdasarkan peran.
     */
    public void initUserSession(String userId, String userRole, String displayName) {
        this.currentUserId = userId; 
        this.currentUserRole = userRole;
        this.currentDisplayName = displayName; 
        System.out.println("Sesi pengguna diinisialisasi: " + displayName + " (" + userRole + ")");
        adjustMenusForRole(this.currentUserRole);
    }

    /**
     * Menyesuaikan visibilitas menu dan item menu berdasarkan peran pengguna.
     */
    private void adjustMenusForRole(String role) {
        boolean isAdmin = "ADMIN".equals(role);
        boolean isMember = "MEMBER".equals(role);

        // Menu File selalu terlihat (biasanya)
        if (fileMenu != null) fileMenu.setVisible(true);

        // Menu Manajemen hanya untuk Admin
        if (manajemenMenu != null) manajemenMenu.setVisible(isAdmin);

        // Menu Laporan hanya untuk Admin
        if (laporanMenu != null) laporanMenu.setVisible(isAdmin);
        
        // Menu Transaksi: terlihat oleh Admin atau Member
        if (transaksiMenu != null) transaksiMenu.setVisible(isAdmin || isMember);
        // Item spesifik di Menu Transaksi:
        if (peminjamanMenuItem != null) peminjamanMenuItem.setVisible(isAdmin || isMember); // Peminjaman bisa oleh Admin/Member
        if (pengembalianMenuItem != null) pengembalianMenuItem.setVisible(isAdmin);      // Pengembalian hanya oleh Admin

        // Menu Akun Saya hanya untuk Member
        if (akunSayaMenu != null) akunSayaMenu.setVisible(isMember);
        // Item spesifik di Menu Akun Saya (jika dikontrol terpisah dari menu utamanya)
        // Jika riwayatPeminjamanMenuItem adalah satu-satunya item di akunSayaMenu,
        // mengontrol akunSayaMenu.setVisible(isMember) sudah cukup.
        // Namun, jika Anda mendeklarasikan @FXML untuk riwayatPeminjamanMenuItem, Anda juga bisa mengaturnya:
        if (riwayatPeminjamanMenuItem != null) riwayatPeminjamanMenuItem.setVisible(isMember);


        // Logika untuk kondisi awal (sebelum login) atau peran tidak dikenal
        if (role == null) { 
            System.out.println("Tidak ada peran (sebelum login), menu khusus disembunyikan.");
            if (manajemenMenu != null) manajemenMenu.setVisible(false);
            // Untuk transaksiMenu, jika ada item yang mungkin selalu visible, biarkan menu visible
            // atau sembunyikan semua item di dalamnya.
            // Untuk kesederhanaan, jika transaksiMenu hanya berisi item yang butuh login, sembunyikan:
            if (transaksiMenu != null && !(isAdmin || isMember)) transaksiMenu.setVisible(false);
            if (laporanMenu != null) laporanMenu.setVisible(false);
            if (akunSayaMenu != null) akunSayaMenu.setVisible(false); 
        } else if (isAdmin) {
            System.out.println("Menampilkan menu sesuai peran ADMIN.");
        } else if (isMember) {
            System.out.println("Menampilkan menu sesuai peran MEMBER.");
        }
    }

    // --- Metode Handler untuk Aksi Menu ---
    // (Metode handleKeluarAction, handleBukuAction, handleAnggotaAction, 
    // handlePeminjamanAction, handlePengembalianAction, handleLaporanBukuDipinjamAction,
    // handleStatistikBulananAction, handleRiwayatPeminjamanAction tetap sama seperti
    // yang sudah Anda miliki dan sudah kita diskusikan sebelumnya, termasuk pengecekan peran di dalamnya.)

    @FXML
    private void handleKeluarAction(ActionEvent event) {
        System.out.println("Menu Keluar diklik. Aplikasi akan ditutup.");
        Platform.exit();
    }

    @FXML
    private void handleBukuAction(ActionEvent event) {
        if (!"ADMIN".equals(currentUserRole)) {
            showErrorAlert("Akses Ditolak", "Hanya Admin yang dapat mengakses fitur ini.", null);
            return;
        }
        System.out.println("Menu Data Buku diklik oleh " + currentUserRole);
        loadView("/com/perpustakaan/view/BookManagementView.fxml", "Manajemen Data Buku");
    }

    @FXML
    private void handleAnggotaAction(ActionEvent event) {
        if (!"ADMIN".equals(currentUserRole)) {
            showErrorAlert("Akses Ditolak", "Hanya Admin yang dapat mengakses fitur ini.", null);
            return;
        }
        System.out.println("Menu Data Anggota diklik oleh " + currentUserRole);
        loadView("/com/perpustakaan/view/MemberManagementView.fxml", "Manajemen Data Anggota");
    }

    @FXML
    private void handlePeminjamanAction(ActionEvent event) {
        if (!"ADMIN".equals(currentUserRole) && !"MEMBER".equals(currentUserRole)) {
             showErrorAlert("Akses Ditolak", "Fitur ini memerlukan login.", null);
            return;
        }
        System.out.println("Menu Peminjaman Buku diklik oleh " + currentUserRole);
        loadView("/com/perpustakaan/view/BorrowBookView.fxml", "Peminjaman Buku");
    }

    @FXML
    private void handlePengembalianAction(ActionEvent event) {
        if (!"ADMIN".equals(currentUserRole)) {
            showErrorAlert("Akses Ditolak", "Hanya Admin yang dapat memproses pengembalian.", null);
            return;
        }
        System.out.println("Menu Pengembalian Buku diklik oleh " + currentUserRole);
        loadView("/com/perpustakaan/view/ReturnBookView.fxml", "Pengembalian Buku");
    }

    @FXML
    private void handleLaporanBukuDipinjamAction(ActionEvent event) {
        if (!"ADMIN".equals(currentUserRole)) {
            showErrorAlert("Akses Ditolak", "Hanya Admin yang dapat mengakses laporan ini.", null);
            return;
        }
        System.out.println("Menu Laporan Buku Dipinjam diklik oleh " + currentUserRole);
        loadView("/com/perpustakaan/view/BorrowedBooksReportView.fxml", "Laporan Buku Dipinjam");
    }
    
    @FXML
    private void handleStatistikBulananAction(ActionEvent event) {
        if (!"ADMIN".equals(currentUserRole)) {
            showErrorAlert("Akses Ditolak", "Hanya Admin yang dapat mengakses statistik.", null);
            return;
        }
        System.out.println("Menu Statistik Bulanan diklik oleh " + currentUserRole);
        loadView("/com/perpustakaan/view/MonthlyStatisticsView.fxml", "Laporan Statistik Bulanan");
    }

    @FXML
    private void handleRiwayatPeminjamanAction(ActionEvent event) {
        if (!"MEMBER".equals(currentUserRole)) {
            showErrorAlert("Akses Ditolak", "Fitur ini hanya untuk Member.", null);
            return;
        }
        System.out.println("Menu Riwayat Peminjaman Saya diklik oleh Member: " + currentUserId);
        loadView("/com/perpustakaan/view/MyBorrowsView.fxml", "Riwayat Peminjaman Saya");
    }


    private void loadView(String fxmlPath, String title) {
        try {
            InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath);
            if (fxmlStream == null) {
                String fxmlFileName = fxmlPath.substring(fxmlPath.lastIndexOf("/") + 1);
                showErrorAlert("Kesalahan Pemuatan Tampilan", 
                               "File tampilan (" + fxmlFileName + ") tidak ditemukan.", 
                               "Pastikan file FXML '" + fxmlFileName + "' ada di lokasi yang benar.");
                return; 
            }
            fxmlStream.close(); 

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load(); 

            Object loadedController = loader.getController();
            if (loadedController instanceof NeedsLibraryManager) {
                ((NeedsLibraryManager) loadedController).setLibraryManager(this.libraryManager);
            }
            if (loadedController instanceof NeedsUserContext) {
                ((NeedsUserContext) loadedController).setUserContext(this.currentUserId, this.currentUserRole, this.currentDisplayName);
            }

            Stage stage = new Stage();
            String userInfo = (currentUserRole != null ? currentUserRole : "Guest");
            if (currentDisplayName != null && !currentDisplayName.isEmpty()) { // Gunakan DisplayName jika ada
                 userInfo = currentDisplayName + " (" + currentUserRole + ")";
            } else if (currentUserId != null && ! currentUserId.isEmpty()){ // Fallback ke UserId jika DisplayName kosong
                 userInfo = currentUserId + " (" + currentUserRole + ")";
            }
            stage.setTitle(title + " - [" + userInfo + "]");
            stage.setScene(new Scene(root));
            stage.show(); 

        } catch (IOException e) {
            e.printStackTrace(); 
            showErrorAlert("Kesalahan Sistem", "Gagal memuat tampilan: " + title, "Detail: " + e.getMessage());
        }
    }
    
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}