package com.perpustakaan.controller;

import com.perpustakaan.model.LibraryManager;
import com.perpustakaan.model.MonthlyStat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell; // <-- Impor untuk TableCell
import java.text.NumberFormat;      // <-- Impor untuk NumberFormat
import java.util.Locale;            // <-- Impor untuk Locale (untuk format Rupiah)

public class MonthlyStatisticsController implements NeedsLibraryManager {

    @FXML
    private TableView<MonthlyStat> statisticsTableView;
    @FXML
    private TableColumn<MonthlyStat, String> monthYearColumn;
    @FXML
    private TableColumn<MonthlyStat, Integer> totalBorrowsColumn;
    @FXML
    private TableColumn<MonthlyStat, Integer> totalReturnsColumn;
    @FXML
    private TableColumn<MonthlyStat, Double> totalFinesColumn; // Tipe data tetap Double

    @FXML
    private Button refreshButton;

    private LibraryManager libraryManager;
    private ObservableList<MonthlyStat> statisticsData = FXCollections.observableArrayList();

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        loadStatisticsData();
    }

    @FXML
    private void initialize() {
        monthYearColumn.setCellValueFactory(new PropertyValueFactory<>("monthYearFormatted"));
        totalBorrowsColumn.setCellValueFactory(new PropertyValueFactory<>("totalBorrows"));
        totalReturnsColumn.setCellValueFactory(new PropertyValueFactory<>("totalReturns"));
        
        // Mengatur CellValueFactory untuk kolom denda
        totalFinesColumn.setCellValueFactory(new PropertyValueFactory<>("totalFines"));
        // Menggunakan setCellFactory dengan helper untuk memformat sebagai Rupiah
        totalFinesColumn.setCellFactory(column -> TableCellHelper.getFormattedCurrencyCell()); // <--- AKTIFKAN INI

        statisticsTableView.setItems(statisticsData);
        System.out.println("MonthlyStatisticsController initialized with currency formatting for fines.");
    }

    private void loadStatisticsData() {
        if (libraryManager == null) {
            System.err.println("LibraryManager belum di-set di MonthlyStatisticsController.");
            statisticsData.clear();
            return;
        }
        statisticsData.clear();
        statisticsData.addAll(libraryManager.getMonthlyStatistics());
        statisticsTableView.refresh();
        System.out.println(statisticsData.size() + " data statistik bulanan dimuat.");
    }

    @FXML
    private void handleRefreshStatsAction(ActionEvent event) {
        System.out.println("Tombol Refresh Statistik diklik.");
        loadStatisticsData();
    }
    
    // --- Implementasi TableCellHelper sebagai static nested class ---
    static class TableCellHelper {
        /**
         * Membuat TableCell yang memformat angka Double sebagai mata uang Rupiah.
         * @return TableCell yang sudah diformat.
         */
        public static <S> TableCell<S, Double> getFormattedCurrencyCell() {
            return new TableCell<>() {
                // Buat instance NumberFormat untuk Rupiah (Indonesia)
                // Menggunakan Locale.of() untuk menghindari deprecation warning
                private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID")); // <--- PERUBAHAN DI SINI

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null); 
                        setGraphic(null);
                    } else {
                        setText(currencyFormat.format(item)); 
                    }
                }
            };
        }
    }
}