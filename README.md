# Sistem Informasi Perpustakaan Desktop

![Java CI with Maven](https://img.shields.io/badge/build-passing-brightgreen)
![Java Version](https://img.shields.io/badge/java-17%2B-blue)
![JavaFX Version](https://img.shields.io/badge/javafx-21-orange)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

Dokumentasi teknis untuk aplikasi desktop Sistem Informasi Perpustakaan yang dibangun menggunakan Java dan JavaFX.

---

##  CANVAS PROYEK

### Deskripsi Singkat
Aplikasi ini adalah sebuah sistem manajemen perpustakaan (Library Management System) berbasis desktop yang dirancang untuk memfasilitasi operasi sehari-hari di sebuah perpustakaan. Aplikasi ini mendukung dua jenivs peran pengguna (Admin dan Mahasiswa) dengan fungsionalitas yang berbeda untuk masing-masing peran. Seluruh data aplikasi disimpan dalam format file `.csv` untuk portabilitas dan kemudahan.

## Fitur Utama 📋

Aplikasi ini memiliki fitur yang terpisah berdasarkan peran pengguna:

### 👤 Fitur Sisi Admin
- **Login & Dashboard**: Halaman login khusus dan dashboard utama yang berisi navigasi cepat.
- **Kelola Katalog Buku**: Fungsionalitas CRUD (Create, Read, Update, Delete) penuh untuk data buku.
- **Kelola Data Anggota**: Fungsionalitas CRUD penuh untuk data anggota, termasuk validasi ID duplikat.
- **Proses Pengembalian Buku**: Halaman khusus untuk mencari transaksi peminjaman aktif dan memproses pengembalian buku, lengkap dengan perhitungan denda otomatis.
- **Laporan & Statistik**: Halaman visual yang menampilkan statistik kunci (total buku, anggota, dll.) dan grafik distribusi buku berdasarkan kategori.

### 🎓 Fitur Sisi Mahasiswa (Member)
- **Login & Dashboard**: Halaman login menggunakan NIM & PIN, serta dashboard pribadi yang menampilkan ringkasan statistik peminjaman.
- **Katalog Buku**: Menampilkan semua buku yang tersedia untuk dipinjam, lengkap dengan fitur pencarian.
- **Proses Peminjaman**: Anggota dapat memilih durasi peminjaman (3, 7, 14, atau 30 hari) dan melihat tanggal jatuh tempo secara dinamis.
- **Halaman "Buku Saya"**: Menampilkan daftar buku yang sedang dipinjam oleh anggota, beserta status keterlambatan dan total denda saat ini.
- **Halaman Profil**: Anggota dapat melihat data diri mereka dan memperbarui informasi kontak (email) serta PIN keamanan.

---

## Tumpukan Teknologi 🛠️

- **Bahasa Pemrograman**: Java (direkomendasikan versi 17 atau lebih baru)
- **Framework GUI**: JavaFX 21
- **Manajemen Proyek & Dependensi**: Apache Maven
- **Library Pihak Ketiga**:
    - `OpenCSV`: Untuk membaca dan menulis data dari/ke file `.csv`.
    - `FontAwesomeFX`: Untuk menampilkan ikon-ikon modern di antarmuka pengguna.

---

## Struktur Proyek 📂

Proyek ini mengikuti pola desain yang terinspirasi dari MVC (Model-View-Controller) untuk menjaga kode tetap terorganisir.
```
├── data/
│   ├── books.csv
│   ├── members.csv
│   └── transactions.csv
├── src/
│   └── main/
│       ├── java/
│       │   └── com/perpustakaan/
│       │       ├── controller/ (Logika untuk setiap View)
│       │       ├── dao/        (Data Access Object - Logika interaksi dengan file CSV)
│       │       ├── model/      (Representasi data: Book, Member, Transaction)
│       │       └── App.java    (Titik masuk utama aplikasi)
│       └── resources/
│           └── com/perpustakaan/
│               └── view/       (File FXML untuk desain UI dan styles.css)
└── pom.xml                     (File konfigurasi Maven dan dependensi)
```

- **`data/`**: Direktori untuk menyimpan semua file database `.csv`.
- **`controller/`**: Berisi kelas-kelas Java yang mengontrol logika di balik setiap halaman FXML.
- **`dao/`**: Jembatan antara aplikasi dan "database" (file CSV). Semua logika baca/tulis file ada di sini.
- **`model/`**: Kelas POJO (Plain Old Java Object) yang mendefinisikan struktur data seperti `Book` dan `Member`.
- **`view/`**: Berisi semua file `.fxml` yang mendefinisikan antarmuka pengguna dan file `styles.css` untuk styling.

---

## Instalasi & Penyiapan ⚙️

Untuk menjalankan proyek ini di lingkungan lokal Anda, ikuti langkah-langkah berikut:

1.  **Clone Repositori**
    ```bash
    git clone https://github.com/widyaame/OOPLibrarySystem_WisaTeam.git
    cd OOPLibrarySystem_WisaTeam
    ```
2.  **Prasyarat**
    - Pastikan Anda memiliki **JDK (Java Development Kit)** versi 17 atau yang lebih baru terpasang.
    - Pastikan Anda memiliki **Apache Maven** terpasang dan dikonfigurasi di sistem Anda.

3.  **Siapkan Direktori Data**
    - Di dalam direktori root proyek Anda, buat sebuah folder baru bernama `data`.
    - Di dalam folder `data`, buat tiga file kosong: `books.csv`, `members.csv`, dan `transactions.csv`.
    - Isi file-file tersebut dengan data awal (terutama header-nya) agar program tidak error saat pertama kali dijalankan.

4.  **Instal Dependensi**
    - Buka terminal atau command prompt di direktori root proyek, lalu jalankan perintah Maven berikut untuk mengunduh semua library yang dibutuhkan:
    ```bash
    mvn clean install
    ```

---

## Cara Menjalankan Aplikasi 🚀

Setelah semua dependensi terpasang, Anda bisa menjalankan aplikasi langsung menggunakan plugin Maven JavaFX:

```bash
mvn javafx:run
```
Jendela aplikasi akan muncul, dimulai dari halaman login.

```
Kredensial Default:

Admin:
- Username: admin
- Password: admin123

Mahasiswa:
- Gunakan data memberId dan pin yang ada di members.csv Anda.
```