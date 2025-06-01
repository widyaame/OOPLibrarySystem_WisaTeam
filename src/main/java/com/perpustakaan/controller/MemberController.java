package com.perpustakaan.controller;

import com.perpustakaan.model.Member;
import com.perpustakaan.model.LibraryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Akan digunakan untuk memuat dialog form
import javafx.scene.Parent; // Akan digunakan untuk memuat dialog form
import javafx.scene.Scene; // Akan digunakan untuk memuat dialog form
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; // Untuk dialog konfirmasi
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality; // Untuk dialog modal
import javafx.stage.Stage; // Untuk dialog baru
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional; // Untuk hasil dialog konfirmasi

public class MemberController implements NeedsLibraryManager {

    @FXML
    private TableView<Member> memberTableView;
    @FXML
    private TableColumn<Member, String> idColumn;
    @FXML
    private TableColumn<Member, String> fullNameColumn;
    @FXML
    private TableColumn<Member, String> majorColumn;
    @FXML
    private TableColumn<Member, String> emailColumn;

    @FXML
    private TextField filterField;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private LibraryManager libraryManager;
    private ObservableList<Member> memberData = FXCollections.observableArrayList();
    private FilteredList<Member> filteredData;

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        refreshMemberTable();
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        manageButtonState(false);
        System.out.println("MemberController initialized. Menunggu LibraryManager untuk memuat data.");
    }

    private void loadMemberDataInternal() {
        if (libraryManager == null) {
            System.err.println("LibraryManager belum di-set di MemberController. Data anggota tidak dapat dimuat.");
            memberData.clear();
            if (memberTableView != null)
                memberTableView.setItems(null);
            return;
        }

        memberData.clear();
        memberData.addAll(libraryManager.getAllMembers());

        if (filteredData == null) {
            filteredData = new FilteredList<>(memberData, _member -> true);
            filterField.textProperty().addListener((_observable, _oldValue, newValue) -> {
                applyFilter(newValue);
            });
        } else {
            applyFilter(filterField.getText());
        }

        SortedList<Member> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(memberTableView.comparatorProperty());
        memberTableView.setItems(sortedData);

        memberTableView.getSelectionModel().selectedItemProperty().removeListener(this::selectionChangedListener);
        memberTableView.getSelectionModel().selectedItemProperty().addListener(this::selectionChangedListener);

        manageButtonState(memberTableView.getSelectionModel().getSelectedItem() != null);
        System.out.println(memberData.size() + " anggota dimuat dan filter/sortir diaktifkan.");
    }

    private void selectionChangedListener(javafx.beans.value.ObservableValue<? extends Member> _observable,
            Member _oldValue, Member newValue) {
        manageButtonState(newValue != null);
    }

    private void applyFilter(String filterText) {
        filteredData.setPredicate(member -> {
            if (filterText == null || filterText.isEmpty() || filterText.isBlank()) {
                return true;
            }
            String lowerCaseFilter = filterText.toLowerCase();
            return member.getId().toLowerCase().contains(lowerCaseFilter) ||
                    member.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                    (member.getMajor() != null && member.getMajor().toLowerCase().contains(lowerCaseFilter)) || // Tambah
                                                                                                                // null
                                                                                                                // check
                                                                                                                // untuk
                                                                                                                // major
                    member.getEmail().toLowerCase().contains(lowerCaseFilter);
        });
    }

    private void manageButtonState(boolean isItemSelected) {
        if (editButton != null)
            editButton.setDisable(!isItemSelected);
        if (deleteButton != null)
            deleteButton.setDisable(!isItemSelected);
    }

    @FXML
    private void handleAddMemberAction(ActionEvent event) {
        System.out.println("Tombol Tambah Anggota diklik.");
        showMemberFormDialog(null); // Panggil dialog untuk menambah anggota baru
    }

    @FXML
    private void handleEditMemberAction(ActionEvent event) {
        Member selectedMember = memberTableView.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            System.out.println("Tombol Edit Anggota diklik untuk: " + selectedMember.getFullName());
            showMemberFormDialog(selectedMember); // Panggil dialog untuk mengedit anggota yang dipilih
        } else {
            showAlert(Alert.AlertType.WARNING, "Edit Gagal", "Tidak ada anggota yang dipilih.",
                    "Silakan pilih anggota dari tabel untuk diedit.");
        }
    }

    @FXML
    private void handleDeleteMemberAction(ActionEvent event) {
        Member selectedMember = memberTableView.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Konfirmasi Hapus Anggota");
            confirmationDialog.setHeaderText("Hapus Anggota: " + selectedMember.getFullName());
            confirmationDialog.setContentText(
                    "Apakah Anda yakin ingin menghapus anggota dengan ID: " + selectedMember.getId() + "?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean deleted = libraryManager.deleteMember(selectedMember.getId());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Hapus Berhasil",
                            "Anggota '" + selectedMember.getFullName() + "' telah berhasil dihapus.", null);
                    refreshMemberTable();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hapus Gagal",
                            "Gagal menghapus anggota '" + selectedMember.getFullName() + "'.",
                            "Anggota mungkin tidak ditemukan atau terjadi kesalahan lain.");
                }
            } else {
                System.out.println("Penghapusan anggota dibatalkan oleh pengguna.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Hapus Gagal", "Tidak ada anggota yang dipilih.",
                    "Silakan pilih anggota dari tabel untuk dihapus.");
        }
    }

    @FXML
    private void handleRefreshMemberAction(ActionEvent event) {
        System.out.println("Tombol Refresh Anggota diklik.");
        refreshMemberTable();
    }

    private void refreshMemberTable() {
        System.out.println("Merefresh tabel anggota...");
        if (filterField != null) {
            filterField.clear();
        }
        loadMemberDataInternal();
        if (memberTableView != null && memberTableView.getSelectionModel() != null) {
            memberTableView.getSelectionModel().clearSelection();
        }
        System.out.println("Data anggota telah di-refresh.");
    }

    /**
     * Menampilkan dialog/form untuk menambah atau mengedit data anggota.
     * 
     * @param memberToEdit Anggota yang akan diedit, atau null jika ingin menambah
     *                     anggota baru.
     */
    private void showMemberFormDialog(Member memberToEdit) {
        try {
            String fxmlPath = "/com/perpustakaan/view/MemberFormView.fxml"; // Path ke FXML form anggota

            InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath);
            if (fxmlStream == null) {
                System.err.println("File FXML form anggota tidak ditemukan: " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Kesalahan Internal", "Form untuk input data anggota tidak ditemukan.",
                        "Harap hubungi administrator.");
                return;
            }
            fxmlStream.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(memberToEdit == null ? "Tambah Anggota Baru" : "Edit Data Anggota");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // (Opsional) Atur owner window
            // if (memberTableView.getScene() != null &&
            // memberTableView.getScene().getWindow() != null) {
            // dialogStage.initOwner(memberTableView.getScene().getWindow());
            // }

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // --- BAGIAN UNTUK BERINTERAKSI DENGAN MemberFormController ---
            Object loadedController = loader.getController();
            if (loadedController instanceof MemberFormController) {
                MemberFormController controller = (MemberFormController) loadedController;
                controller.setLibraryManager(this.libraryManager);
                controller.setDialogStage(dialogStage);
                if (memberToEdit != null) {
                    controller.setMemberToEdit(memberToEdit); // Anda perlu membuat metode ini di MemberFormController
                }
            } else if (loadedController != null) {
                System.err.println("Controller untuk MemberFormView.fxml tidak bertipe MemberFormController.");
            } else {
                System.err.println("Controller untuk MemberFormView.fxml tidak berhasil dimuat.");
            }
            // --- AKHIR BAGIAN INTERAKSI DENGAN MemberFormController ---

            dialogStage.showAndWait(); // Baris ini akan diaktifkan setelah interaksi controller form siap

            // Setelah dialog ditutup (nantinya), refresh tabel.
            refreshMemberTable(); // Baris ini juga akan lebih relevan setelah dialog benar-benar berinteraksi

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Memuat Form", "Gagal membuka form data anggota.",
                    e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Tidak Terduga",
                    "Terjadi kesalahan tidak terduga saat membuka form anggota.", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // (Opsional) Atur owner untuk alert jika dialogStage ada dan terlihat
        // if (dialogStage != null && dialogStage.isShowing()) {
        // alert.initOwner(dialogStage);
        // } else if (memberTableView.getScene() != null &&
        // memberTableView.getScene().getWindow() != null) {
        // alert.initOwner(memberTableView.getScene().getWindow());
        // }
        alert.showAndWait();
    }
}