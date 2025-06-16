package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LaporanController {

    @FXML private Label totalBukuLabel;
    @FXML private Label totalAnggotaLabel;
    @FXML private Label bukuDipinjamLabel;
    @FXML private Label bukuTerlambatLabel;
    @FXML private PieChart kategoriPieChart;

    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private TransactionDAO transactionDAO;

    public LaporanController() {
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        transactionDAO = new TransactionDAO();
    }

    @FXML
    public void initialize() {
        loadStatistikAngka();
        loadChartKategori();
    }

    private void loadStatistikAngka() {
        // Hitung semua statistik dari masing-masing DAO
        long totalBuku = bookDAO.getAllBooks().size();
        long totalAnggota = memberDAO.getAllMembers().size();
        
        List<Transaction> transaksiAktif = transactionDAO.getAllTransactions().stream()
                .filter(t -> t.getStatus() == Transaction.Status.DIPINJAM)
                .collect(Collectors.toList());
        
        long bukuDipinjam = transaksiAktif.size();
        long bukuTerlambat = transaksiAktif.stream()
                .filter(t -> LocalDate.now().isAfter(t.getDueDate()))
                .count();

        // Tampilkan di label-label yang sesuai
        totalBukuLabel.setText(String.valueOf(totalBuku));
        totalAnggotaLabel.setText(String.valueOf(totalAnggota));
        bukuDipinjamLabel.setText(String.valueOf(bukuDipinjam));
        bukuTerlambatLabel.setText(String.valueOf(bukuTerlambat));
    }

    private void loadChartKategori() {
        // Hitung jumlah buku per kategori menggunakan Stream API
        Map<String, Long> booksByCategory = bookDAO.getAllBooks().stream()
                .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));
        
        // Siapkan data untuk PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        booksByCategory.forEach((category, count) -> {
            pieChartData.add(new PieChart.Data(category + " (" + count + ")", count));
        });

        // Tampilkan data di chart
        kategoriPieChart.setData(pieChartData);
        kategoriPieChart.setLabelsVisible(true);
        kategoriPieChart.setLegendVisible(false); // Sembunyikan legenda agar tidak penuh
    }
}