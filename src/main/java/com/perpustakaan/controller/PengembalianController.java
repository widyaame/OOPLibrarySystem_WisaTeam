package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.PeminjamanDetail;
import com.perpustakaan.model.Transaction;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PengembalianController {

    @FXML private TextField searchField;
    @FXML private TableView<PeminjamanDetail> peminjamanTableView; 
    @FXML private TableColumn<PeminjamanDetail, String> memberIdColumn;
    @FXML private TableColumn<PeminjamanDetail, String> judulColumn;
    @FXML private TableColumn<PeminjamanDetail, LocalDate> tglKembaliColumn;
    @FXML private TableColumn<PeminjamanDetail, String> dendaColumn;
    @FXML private Button prosesButton;

    private TransactionDAO transactionDAO;
    private BookDAO bookDAO;
    private ObservableList<PeminjamanDetail> peminjamanList = FXCollections.observableArrayList();

    public PengembalianController() {
        transactionDAO = new TransactionDAO();
        bookDAO = new BookDAO();
    }

    @FXML
    public void initialize() {
        memberIdColumn.setCellValueFactory(cellData -> 
            transactionDAO.getAllTransactions().stream()
                .filter(t -> t.getTransactionId().equals(cellData.getValue().getTransactionId()))
                .findFirst()
                .map(Transaction::getMemberId)
                .map(Object::toString)
                .map(s -> new Label(s).textProperty())
                .orElse(null)
        );
        judulColumn.setCellValueFactory(new PropertyValueFactory<>("judulBuku"));
        tglKembaliColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        dendaColumn.setCellValueFactory(new PropertyValueFactory<>("denda"));

        peminjamanTableView.getSelectionModel().selectedItemProperty().addListener( 
                (_, _, newSelection) -> prosesButton.setDisable(newSelection == null));

        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(10);
        
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName("SEARCH");
        icon.setSize("5em");
        icon.getStyleClass().add("placeholder-icon");
        
        Label text = new Label("Silakan cari NIM atau ISBN untuk menampilkan data peminjaman.");
        text.getStyleClass().add("placeholder-text");
        
        placeholder.getChildren().addAll(icon, text);
        
        peminjamanTableView.setPlaceholder(placeholder);
    }

    @FXML
    private void handleSearchButton() {
        String searchTerm = searchField.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            peminjamanList.clear();
            peminjamanTableView.setItems(peminjamanList); 
            return;
        }

        List<PeminjamanDetail> details = new ArrayList<>();
        List<Transaction> allTransactions = transactionDAO.getAllTransactions().stream()
            .filter(t -> t.getStatus() == Transaction.Status.DIPINJAM)
            .collect(Collectors.toList());

        for (Transaction t : allTransactions) {
            Optional<Book> bookOpt = bookDAO.getBookByIsbn(t.getBookIsbn());
            if (bookOpt.isPresent()) {
                if (t.getMemberId().toLowerCase().contains(searchTerm) || t.getBookIsbn().toLowerCase().contains(searchTerm)) {
                    details.add(new PeminjamanDetail(t, bookOpt.get()));
                }
            }
        }
        peminjamanList.setAll(details);
        peminjamanTableView.setItems(peminjamanList); 
    }

    @FXML
    private void handleProsesButton() {
        PeminjamanDetail selected = peminjamanTableView.getSelectionModel().getSelectedItem(); 
        if (selected == null) return;

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Pengembalian");
        confirmationAlert.setHeaderText("Proses pengembalian untuk buku: " + selected.getJudulBuku());
        confirmationAlert.setContentText("Denda: " + selected.getDenda() + "\n\nLanjutkan proses?");
        
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Optional<Transaction> transactionOpt = transactionDAO.getAllTransactions().stream()
                    .filter(t -> t.getTransactionId().equals(selected.getTransactionId())).findFirst();
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                transaction.setReturnDate(LocalDate.now());
                transaction.setStatus(Transaction.Status.KEMBALI);
                transactionDAO.updateTransaction(transaction);
            }

            Optional<Book> bookOpt = bookDAO.getBookByIsbn(selected.getBookIsbn());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                book.setStatus("Tersedia");
                bookDAO.updateBook(book);
            }

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Pengembalian Berhasil");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Buku '" + selected.getJudulBuku() + "' telah berhasil dikembalikan.");
            successAlert.showAndWait();

            handleSearchButton();
        }
    }
}