package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BerandaMemberController {

    @FXML private Label welcomeLabel;
    @FXML private Label bukuDipinjamLabel;
    @FXML private Label totalPinjamLabel;
    @FXML private Label bukuTerlambatLabel;
    @FXML private HBox notifikasiBox;
    @FXML private Label notifikasiLabel;

    private MemberDAO memberDAO;
    private TransactionDAO transactionDAO;

    public BerandaMemberController() {
        memberDAO = new MemberDAO();
        transactionDAO = new TransactionDAO();
    }

    public void initData(String memberId) {
        // Personalisasi sapaan selamat datang
        Optional<Member> memberOpt = memberDAO.getMemberById(memberId);
        memberOpt.ifPresent(member -> welcomeLabel.setText("Halo, " + member.getFullName().split(" ")[0] + "!"));

        // Hitung dan tampilkan statistik
        loadMemberStats(memberId);
    }

    private void loadMemberStats(String memberId) {
        // Ambil semua transaksi milik member ini
        List<Transaction> memberTransactions = transactionDAO.getAllTransactions().stream()
                .filter(t -> t.getMemberId().equals(memberId))
                .collect(Collectors.toList());

        // Hitung total riwayat peminjaman
        long totalPinjam = memberTransactions.size();

        // Filter untuk transaksi yang masih aktif (sedang dipinjam)
        List<Transaction> activeTransactions = memberTransactions.stream()
                .filter(t -> t.getStatus() == Transaction.Status.DIPINJAM)
                .collect(Collectors.toList());
        
        long bukuDipinjam = activeTransactions.size();

        // Hitung buku yang terlambat dari transaksi yang aktif
        long bukuTerlambat = activeTransactions.stream()
                .filter(t -> LocalDate.now().isAfter(t.getDueDate()))
                .count();

        // Tampilkan di label-label
        totalPinjamLabel.setText(String.valueOf(totalPinjam));
        bukuDipinjamLabel.setText(String.valueOf(bukuDipinjam));
        bukuTerlambatLabel.setText(String.valueOf(bukuTerlambat));

        // Tampilkan notifikasi jika ada buku yang terlambat
        if (bukuTerlambat > 0) {
            notifikasiLabel.setText("Anda memiliki " + bukuTerlambat + " buku yang sudah melewati jatuh tempo!");
            notifikasiBox.setVisible(true);
            notifikasiBox.setManaged(true);
        } else {
            notifikasiBox.setVisible(false);
            notifikasiBox.setManaged(false);
        }
    }
}