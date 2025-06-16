package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.PeminjamanDetail;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BukuSayaController {

    @FXML private TableView<PeminjamanDetail> pinjamanTableView;
    @FXML private TableColumn<PeminjamanDetail, String> judulColumn;
    @FXML private TableColumn<PeminjamanDetail, LocalDate> tglPinjamColumn;
    @FXML private TableColumn<PeminjamanDetail, LocalDate> tglKembaliColumn;
    @FXML private TableColumn<PeminjamanDetail, String> terlambatColumn;
    @FXML private TableColumn<PeminjamanDetail, String> dendaColumn;

    private String currentMemberId;
    private TransactionDAO transactionDAO;
    private BookDAO bookDAO;
    private ObservableList<PeminjamanDetail> peminjamanList = FXCollections.observableArrayList();

    public BukuSayaController() {
        transactionDAO = new TransactionDAO();
        bookDAO = new BookDAO();
    }

    public void initData(String memberId) {
        this.currentMemberId = memberId;
        loadPeminjamanData();
    }

    @FXML
    public void initialize() {
        judulColumn.setCellValueFactory(new PropertyValueFactory<>("judulBuku"));
        tglPinjamColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalPinjam"));
        tglKembaliColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        terlambatColumn.setCellValueFactory(new PropertyValueFactory<>("terlambatHari"));
        dendaColumn.setCellValueFactory(new PropertyValueFactory<>("denda"));
    }

    private void loadPeminjamanData() {
        peminjamanList.clear();
        List<PeminjamanDetail> details = new ArrayList<>();
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();

        for (Transaction t : allTransactions) {
            if (t.getMemberId().equals(currentMemberId) && t.getStatus() == Transaction.Status.DIPINJAM) {
                Optional<Book> bookOpt = bookDAO.getBookByIsbn(t.getBookIsbn());
                bookOpt.ifPresent(book -> details.add(new PeminjamanDetail(t, book)));
            }
        }
        peminjamanList.addAll(details);
        pinjamanTableView.setItems(peminjamanList);
    }
}