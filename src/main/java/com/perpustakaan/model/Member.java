package com.perpustakaan.model;

public class Member {
    // Atribut/properti anggota
    private String id;
    private String fullName;
    private String major;
    private String email;
    private String password; // Tambahkan jika perlu untuk autentikasi

    // Konstruktor
    public Member(String id, String fullName, String major, String email,  String password) {
        this.id = id;
        this.fullName = fullName;
        this.major = major;
        this.email = email;
        this.password = password;
    }

    // Metode Getter
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMajor() {
        return major;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Metode toString() (opsional tapi sangat berguna untuk debugging)
    @Override
    public String toString() {
        return "Member [ID=" + id + ", Name=" + fullName + ", Major=" + major + ", Email=" + email + "]";
    }

    // Metode equals() dan hashCode() untuk perbandingan objek (penting untuk validasi duplikasi)
    // Ini membantu saat mencari anggota berdasarkan ID atau Email.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        // Kita bisa membandingkan berdasarkan ID atau Email, tergantung kebutuhan utama
        // Untuk validasi duplikasi ID atau Email, perbandingan ini akan penting.
        return id.equals(member.id) || email.equalsIgnoreCase(member.email);
    }

    @Override
    public int hashCode() {
        // Gabungkan hash code dari ID dan Email jika keduanya digunakan untuk kesetaraan
        return java.util.Objects.hash(id, email.toLowerCase());
    }
}