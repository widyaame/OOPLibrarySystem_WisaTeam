package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.LibraryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class BookController implements NeedsLibraryManager {

    @FXML
    private TableView<Book> bookTableView;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> quantityColumn;
    @FXML
    private TextField filterField;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private LibraryManager libraryManager;
    private ObservableList<Book> bookData = FXCollections.observableArrayList();
    private FilteredList<Book> filteredData;

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        refreshBookTable(); // Muat data setelah LibraryManager tersedia
    }

    @FXML
    private void initialize() {
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        manageButtonState(false);
        System.out.println("BookController initialized. Menunggu LibraryManager untuk memuat data.");
    }
    
    private void loadBookDataInternal() { // Ubah nama dari loadBookData untuk menghindari konflik nama jika ada
        if (libraryManager == null) {
            System.err.println("LibraryManager belum di-set di BookController. Data buku tidak dapat dimuat.");
            bookData.clear();
            if (bookTableView != null) bookTableView.setItems(null);
            return;
        }
        
        bookData.clear();
        bookData.addAll(libraryManager.getAllBooks());
        
        if (filteredData == null) {
            filteredData = new FilteredList<>(bookData, _book -> true); // Parameter tidak digunakan, ganti nama jadi _book

            filterField.textProperty().addListener((_observable, _oldValue, newValue) -> { // Parameter tidak digunakan, ganti nama
                applyFilter(newValue);
            });
        } else {
            applyFilter(filterField.getText());
        }

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bookTableView.comparatorProperty());
        bookTableView.setItems(sortedData);
        
        bookTableView.getSelectionModel().selectedItemProperty().removeListener(this::selectionChangedListener);
        bookTableView.getSelectionModel().selectedItemProperty().addListener(this::selectionChangedListener);
        
        manageButtonState(bookTableView.getSelectionModel().getSelectedItem() != null);
        System.out.println(bookData.size() + " buku dimuat dan filter/sortir diaktifkan.");
    }

    private void selectionChangedListener(javafx.beans.value.ObservableValue<? extends Book> _observable, Book _oldValue, Book newValue) { // Parameter tidak digunakan
        manageButtonState(newValue != null);
    }

    private void applyFilter(String filterText) {
        filteredData.setPredicate(book -> {
            if (filterText == null || filterText.isEmpty() || filterText.isBlank()) {
                return true;
            }
            String lowerCaseFilter = filterText.toLowerCase();
            return book.getIsbn().toLowerCase().contains(lowerCaseFilter) ||
                   book.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                   book.getAuthor().toLowerCase().contains(lowerCaseFilter);
        });
    }

    private void manageButtonState(boolean isItemSelected) {
        if (editButton != null) editButton.setDisable(!isItemSelected);
        if (deleteButton != null) deleteButton.setDisable(!isItemSelected);
    }

    @FXML
    private void handleAddBookAction(ActionEvent event) {
        System.out.println("Tombol Tambah Buku diklik.");
        showBookFormDialog(null);
    }

    @FXML
    private void handleEditBookAction(ActionEvent event) {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            System.out.println("Tombol Edit Buku diklik untuk: " + selectedBook.getTitle());
            showBookFormDialog(selectedBook);
        } else {
            showAlert(Alert.AlertType.WARNING, "Edit Gagal", "Tidak ada buku yang dipilih.", "Silakan pilih buku dari tabel untuk diedit.");
        }
    }

    @FXML
    private void handleDeleteBookAction(ActionEvent event) {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Konfirmasi Hapus Buku");
            confirmationDialog.setHeaderText("Hapus Buku: " + selectedBook.getTitle());
            confirmationDialog.setContentText("Apakah Anda yakin ingin menghapus buku dengan ISBN: " + selectedBook.getIsbn() + "?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean deleted = libraryManager.deleteBook(selectedBook.getIsbn());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Hapus Berhasil", "Buku '" + selectedBook.getTitle() + "' telah berhasil dihapus.", null);
                    refreshBookTable(); // Panggil metode refresh tanpa argumen
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hapus Gagal", "Gagal menghapus buku '" + selectedBook.getTitle() + "'.", "Buku mungkin sudah dihapus atau terjadi kesalahan lain.");
                }
            } else {
                System.out.println("Penghapusan buku dibatalkan oleh pengguna.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Hapus Gagal", "Tidak ada buku yang dipilih.", "Silakan pilih buku dari tabel untuk dihapus.");
        }
    }

    /**
     * Metode yang terhubung ke tombol Refresh di FXML.
     */
    @FXML
    private void handleRefreshAction(ActionEvent event) {
        System.out.println("Tombol Refresh FXML diklik.");
        refreshBookTable();
    }

    /**
     * Logika inti untuk me-refresh data tabel buku.
     * Bisa dipanggil secara internal atau oleh handler event.
     */
    private void refreshBookTable() {
        System.out.println("Merefresh tabel buku...");
        if (filterField != null) {
             filterField.clear(); 
        }
        loadBookDataInternal(); // Panggil metode internal yang sudah diubah namanya
        if (bookTableView != null && bookTableView.getSelectionModel() != null) {
            bookTableView.getSelectionModel().clearSelection(); 
        }
        System.out.println("Data buku telah di-refresh.");
    }
    
    private void showBookFormDialog(Book bookToEdit) {
        try {
            String fxmlPath = "/com/perpustakaan/view/BookFormView.fxml";
            InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath);
            if (fxmlStream == null) {
                System.err.println("File FXML form buku tidak ditemukan: " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Kesalahan Internal", "Form untuk input data buku tidak ditemukan.", "Harap hubungi administrator.");
                return;
            }
            fxmlStream.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(bookToEdit == null ? "Tambah Buku Baru" : "Edit Buku");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // Jika Anda ingin mengaitkan owner (misalnya, jendela BookManagementView):
            // if (bookTableView.getScene() != null && bookTableView.getScene().getWindow() != null) {
            //    dialogStage.initOwner(bookTableView.getScene().getWindow());
            // }

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            Object loadedController = loader.getController();
            if (loadedController instanceof BookFormController) { // Ganti dengan nama controller form Anda
                 BookFormController controller = (BookFormController) loadedController;
                 controller.setLibraryManager(this.libraryManager);
                 controller.setDialogStage(dialogStage); 
                 if (bookToEdit != null) {
                     controller.setBookToEdit(bookToEdit); 
                 }
             } else if (loadedController != null) {
                 System.err.println("Controller untuk BookFormView.fxml tidak bertipe BookFormController atau tidak ditemukan.");
             } else {
                 System.err.println("Controller untuk BookFormView.fxml tidak berhasil dimuat.");
             }
            

            dialogStage.showAndWait();
            refreshBookTable(); // Panggil metode refresh tanpa argumen

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Memuat Form", "Gagal membuka form data buku.", e.getMessage());
        } catch (Exception e) { 
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Tidak Terduga", "Terjadi kesalahan tidak terduga saat membuka form buku.", e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}