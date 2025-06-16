package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.model.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class KelolaAnggotaController {

    @FXML private TableView<Member> memberTableView;
    @FXML private TableColumn<Member, String> memberIdColumn;
    @FXML private TableColumn<Member, String> fullNameColumn;
    @FXML private TableColumn<Member, String> majorColumn;
    @FXML private TableColumn<Member, String> emailColumn;

    @FXML private TextField memberIdField;
    @FXML private PasswordField pinField;
    @FXML private TextField fullNameField;
    @FXML private TextField majorField;
    @FXML private TextField emailField;
    
    private MemberDAO memberDAO;
    private ObservableList<Member> memberList;

    public KelolaAnggotaController() {
        memberDAO = new MemberDAO();
    }

    @FXML
    public void initialize() {
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadMemberData();

        memberTableView.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> showMemberDetails(newValue));
    }

    private void loadMemberData() {
        memberList = FXCollections.observableArrayList(memberDAO.getAllMembers());
        memberTableView.setItems(memberList);
    }

    private void showMemberDetails(Member member) {
        if (member != null) {
            memberIdField.setText(member.getMemberId());
            pinField.setText(member.getPin());
            fullNameField.setText(member.getFullName());
            majorField.setText(member.getMajor());
            emailField.setText(member.getEmail());
            memberIdField.setEditable(false);
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleAddButton() {
        if (memberIdField.getText().isEmpty() || pinField.getText().isEmpty() || fullNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Validasi", "ID (NIM), PIN, dan Nama Lengkap tidak boleh kosong.");
            return;
        }

        if (memberDAO.getMemberById(memberIdField.getText()).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Error", "ID Anggota (NIM) sudah terdaftar.");
            return;
        }
        
        Member newMember = new Member(
                memberIdField.getText(),
                pinField.getText(),
                fullNameField.getText(),
                majorField.getText(),
                emailField.getText()
        );
        memberDAO.addMember(newMember);
        loadMemberData();
        clearForm();
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Anggota baru berhasil ditambahkan.");
    }

    @FXML
    private void handleUpdateButton() {
        Member selectedMember = memberTableView.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            Member updatedMember = new Member(
                    memberIdField.getText(),
                    pinField.getText(),
                    fullNameField.getText(),
                    majorField.getText(),
                    emailField.getText()
            );
            memberDAO.updateMember(updatedMember);
            loadMemberData();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data anggota berhasil diperbarui.");
        }
    }

    @FXML
    private void handleDeleteButton() {
        Member selectedMember = memberTableView.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Hapus");
            alert.setHeaderText("Hapus Anggota: " + selectedMember.getFullName());
            alert.setContentText("Apakah Anda yakin ingin menghapus anggota ini?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                memberDAO.deleteMember(selectedMember.getMemberId());
                loadMemberData();
                clearForm();
            }
        }
    }

    @FXML
    private void handleClearButton() {
        clearForm();
    }

    private void clearForm() {
        memberTableView.getSelectionModel().clearSelection();
        memberIdField.clear();
        pinField.clear();
        fullNameField.clear();
        majorField.clear();
        emailField.clear();
        memberIdField.setEditable(true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}