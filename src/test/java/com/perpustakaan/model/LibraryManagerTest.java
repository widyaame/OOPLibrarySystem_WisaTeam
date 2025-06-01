package com.perpustakaan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.io.TempDir; // Opsional untuk pendekatan file sementara yang lebih canggih

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class LibraryManagerTest {

    private LibraryManager libraryManager;
    // Path ke direktori data. Untuk isolasi tes, ini idealnya adalah direktori tes
    // sementara.
    // Namun, untuk kesederhanaan, kita menggunakan direktori 'data' standar dan
    // membersihkannya.
    private static final String DATA_FOLDER_PATH = "data";
    private static final String BOOKS_TEST_FILE = DATA_FOLDER_PATH + "/books.csv";
    private static final String MEMBERS_TEST_FILE = DATA_FOLDER_PATH + "/members.csv";
    private static final String TRANSACTIONS_TEST_FILE = DATA_FOLDER_PATH + "/transactions.csv";

    /**
     * Metode setUp dijalankan sebelum setiap metode tes.
     * Ini membersihkan file data yang ada untuk memastikan setiap tes dimulai
     * dengan kondisi awal yang bersih dan independen. LibraryManager akan membuat
     * file baru (kosong dengan header) saat diinisialisasi.
     * 
     * @throws IOException jika terjadi error saat menghapus atau membuat direktori.
     */
    @BeforeEach
    void setUp() throws IOException {
        // Hapus file jika ada agar constructor LibraryManager membuat yang baru (kosong
        // dengan header)
        Files.deleteIfExists(Paths.get(BOOKS_TEST_FILE));
        Files.deleteIfExists(Paths.get(MEMBERS_TEST_FILE));
        Files.deleteIfExists(Paths.get(TRANSACTIONS_TEST_FILE));

        // Pastikan direktori 'data' ada (constructor LibraryManager juga akan melakukan
        // ini)
        Files.createDirectories(Paths.get(DATA_FOLDER_PATH));

        // Inisialisasi LibraryManager baru untuk setiap tes
        libraryManager = new LibraryManager();
    }

    // =================================================================================
    // Tes untuk CRUD Buku (Book)
    // =================================================================================

    @Test
    void testAddAndFindBook() {
        Book newBook = new Book("TESTISBN001", "Test Driven Development", "Kent Beck", 3);
        assertTrue(libraryManager.addBook(newBook), "addBook seharusnya mengembalikan true untuk buku baru.");

        Book foundBook = libraryManager.findBookByIsbn("TESTISBN001");
        assertNotNull(foundBook, "Buku seharusnya ditemukan setelah ditambahkan.");
        assertEquals("Test Driven Development", foundBook.getTitle(), "Judul buku yang ditemukan tidak cocok.");
        assertEquals(3, foundBook.getQuantity(), "Kuantitas buku yang ditemukan tidak cocok.");

        // Tes menambahkan buku dengan ISBN yang sama (seharusnya gagal karena ISBN
        // unik)
        Book duplicateBook = new Book("TESTISBN001", "Another Book", "Another Author", 1);
        assertFalse(libraryManager.addBook(duplicateBook),
                "addBook seharusnya mengembalikan false untuk ISBN duplikat.");
    }

    @Test
    void testGetAllBooks_EmptyAndAfterAdding() {
        List<Book> booksEmpty = libraryManager.getAllBooks();
        assertNotNull(booksEmpty, "getAllBooks seharusnya tidak mengembalikan null.");
        assertTrue(booksEmpty.isEmpty(), "getAllBooks seharusnya mengembalikan list kosong pada awalnya.");

        libraryManager.addBook(new Book("ISBN001", "Book A", "Author A", 1));
        libraryManager.addBook(new Book("ISBN002", "Book B", "Author B", 2));

        List<Book> booksAfterAdd = libraryManager.getAllBooks();
        assertEquals(2, booksAfterAdd.size(), "getAllBooks seharusnya mengembalikan 2 buku setelah penambahan.");
    }

    @Test
    void testUpdateBook() {
        Book book = new Book("ISBNUPDATE01", "Original Title", "Original Author", 5);
        libraryManager.addBook(book); // Tambahkan buku awal

        // Buat objek buku baru dengan data yang diperbarui (ISBN sama)
        Book bookToUpdate = new Book("ISBNUPDATE01", "Updated Title", "Updated Author", 10);
        assertTrue(libraryManager.updateBook(bookToUpdate),
                "updateBook seharusnya mengembalikan true jika buku ada dan berhasil diupdate.");

        Book updatedBook = libraryManager.findBookByIsbn("ISBNUPDATE01");
        assertNotNull(updatedBook, "Buku yang diupdate seharusnya masih ada.");
        assertEquals("Updated Title", updatedBook.getTitle(), "Judul buku seharusnya sudah diperbarui.");
        assertEquals("Updated Author", updatedBook.getAuthor(), "Pengarang buku seharusnya sudah diperbarui.");
        assertEquals(10, updatedBook.getQuantity(), "Kuantitas buku seharusnya sudah diperbarui.");

        // Tes update buku yang tidak ada
        Book nonExistentBook = new Book("ISBNNONEXIST", "Non Existent", "N/A", 1);
        assertFalse(libraryManager.updateBook(nonExistentBook), "updateBook seharusnya false jika buku tidak ada.");
    }

    @Test
    void testDeleteBook() {
        Book book = new Book("ISBNDELETE01", "To Be Deleted", "Author Delete", 1);
        libraryManager.addBook(book); // Tambahkan buku yang akan dihapus

        assertNotNull(libraryManager.findBookByIsbn("ISBNDELETE01"), "Buku seharusnya ada sebelum dihapus.");

        assertTrue(libraryManager.deleteBook("ISBNDELETE01"),
                "deleteBook seharusnya true jika buku ada dan berhasil dihapus.");
        assertNull(libraryManager.findBookByIsbn("ISBNDELETE01"), "Buku seharusnya tidak ditemukan setelah dihapus.");

        // Tes hapus buku yang tidak ada
        assertFalse(libraryManager.deleteBook("ISBNNONEXIST"), "deleteBook seharusnya false jika buku tidak ada.");
    }

    // =================================================================================
    // Tes untuk CRUD Anggota (Member)
    // =================================================================================

    @Test
    void testAddAndFindMember() {
        Member newMember = new Member("MTEST001", "Nama Member Test", "Teknik Informatika", "test.member@example.com",
                "pass123");
        assertTrue(libraryManager.addMember(newMember), "addMember seharusnya mengembalikan true untuk anggota baru.");

        Member foundMember = libraryManager.findMemberById("MTEST001");
        assertNotNull(foundMember, "Anggota seharusnya ditemukan setelah ditambahkan.");
        assertEquals("Nama Member Test", foundMember.getFullName(), "Nama lengkap anggota yang ditemukan tidak cocok.");
        assertEquals("Teknik Informatika", foundMember.getMajor(), "Jurusan anggota yang ditemukan tidak cocok.");
        assertEquals("test.member@example.com", foundMember.getEmail(), "Email anggota yang ditemukan tidak cocok.");
        assertEquals("pass123", foundMember.getPassword(), "Password anggota yang ditemukan tidak cocok.");

        // Tes menambahkan anggota dengan ID yang sama (seharusnya gagal)
        Member duplicateIdMember = new Member("MTEST001", "Nama Lain", "Jurusan Lain", "lain@example.com", "passlain");
        assertFalse(libraryManager.addMember(duplicateIdMember),
                "addMember seharusnya mengembalikan false untuk ID anggota duplikat.");

        // Tes menambahkan anggota dengan Email yang sama (seharusnya gagal)
        Member duplicateEmailMember = new Member("MTEST002", "Nama Member Test Dua", "Teknik Sipil",
                "test.member@example.com", "pass456");
        assertFalse(libraryManager.addMember(duplicateEmailMember),
                "addMember seharusnya mengembalikan false untuk Email anggota duplikat.");
    }

    @Test
    void testGetAllMembers_EmptyAndAfterAdding() {
        List<Member> membersEmpty = libraryManager.getAllMembers();
        assertNotNull(membersEmpty, "getAllMembers seharusnya tidak mengembalikan null untuk anggota.");
        assertTrue(membersEmpty.isEmpty(),
                "getAllMembers seharusnya mengembalikan list kosong pada awalnya untuk anggota.");

        libraryManager.addMember(new Member("M001A", "Anggota A", "Fisika", "a@example.com", "passA"));
        libraryManager.addMember(new Member("M002B", "Anggota B", "Kimia", "b@example.com", "passB"));

        List<Member> membersAfterAdd = libraryManager.getAllMembers();
        assertEquals(2, membersAfterAdd.size(), "getAllMembers seharusnya mengembalikan 2 anggota setelah penambahan.");
    }

    @Test
    void testUpdateMember() {
        // Tambahkan anggota awal
        Member originalMember = new Member("MUPDATE01", "Nama Awal", "Jurusan Awal", "awal@example.com", "passAwal");
        libraryManager.addMember(originalMember);

        // Buat objek Member baru untuk update dengan ID yang sama
        Member memberToUpdate = new Member("MUPDATE01", "Nama Diperbarui", "Jurusan Diperbarui", "update@example.com",
                "passBaru123");
        // Di sini kita juga menguji update password, pastikan setter di Member dan
        // logika update di LibraryManager mendukungnya.

        assertTrue(libraryManager.updateMember(memberToUpdate),
                "updateMember seharusnya mengembalikan true jika anggota ada dan berhasil diupdate.");

        Member updatedMember = libraryManager.findMemberById("MUPDATE01");
        assertNotNull(updatedMember, "Anggota seharusnya masih ada setelah diupdate.");
        assertEquals("Nama Diperbarui", updatedMember.getFullName(),
                "Nama lengkap anggota seharusnya sudah diperbarui.");
        assertEquals("Jurusan Diperbarui", updatedMember.getMajor(), "Jurusan anggota seharusnya sudah diperbarui.");
        assertEquals("update@example.com", updatedMember.getEmail(), "Email anggota seharusnya sudah diperbarui.");
        assertEquals("passBaru123", updatedMember.getPassword(), "Password seharusnya sudah diperbarui.");

        // Tes update email ke email yang sudah ada (seharusnya gagal)
        libraryManager.addMember(
                new Member("MDUPE001", "Member Email Duplikat", "Sastra", "duplikat@example.com", "passDupe"));
        Member memberToFailEmailUpdate = new Member("MUPDATE01", "Nama Gagal Update", "Jurusan Gagal Update",
                "duplikat@example.com", "passBaru123");
        assertFalse(libraryManager.updateMember(memberToFailEmailUpdate),
                "updateMember seharusnya false jika email baru sudah digunakan anggota lain.");

        // Tes update anggota yang tidak ada
        Member nonExistentMember = new Member("MNOTEXIST", "Tidak Ada", "N/A", "not@exist.com", "passNE");
        assertFalse(libraryManager.updateMember(nonExistentMember),
                "updateMember seharusnya false jika anggota tidak ada.");
    }

    @Test
    void testDeleteMember() {
        Member member = new Member("MDELETE01", "Akan Dihapus", "Jurusan Hapus", "delete@example.com", "passDel");
        libraryManager.addMember(member);

        assertNotNull(libraryManager.findMemberById("MDELETE01"), "Anggota seharusnya ada sebelum dihapus.");

        assertTrue(libraryManager.deleteMember("MDELETE01"),
                "deleteMember seharusnya true jika anggota ada dan berhasil dihapus.");
        assertNull(libraryManager.findMemberById("MDELETE01"), "Anggota seharusnya tidak ditemukan setelah dihapus.");

        // Tes hapus anggota yang tidak ada
        assertFalse(libraryManager.deleteMember("MNOTEXIST"), "deleteMember seharusnya false jika anggota tidak ada.");
    }

    // =================================================================================
    // Tes untuk Logika Transaksi (borrowBook, returnBook)
    // =================================================================================

    @Test
    void testBorrowBook_Successful() {
        // Setup: Tambahkan member dan buku yang akan dipinjam
        Member member = new Member("MEMBER01", "Peminjam Test", "TI", "pinjam@example.com", "pass");
        Book book = new Book("ISBNBORROW01", "Buku Untuk Dipinjam", "Author Pinjam", 1); // Stok awal 1
        libraryManager.addMember(member);
        libraryManager.addBook(book);

        assertTrue(libraryManager.borrowBook("MEMBER01", "ISBNBORROW01"), "Peminjaman buku seharusnya berhasil.");

        // Verifikasi:
        // 1. Kuantitas buku berkurang
        Book borrowedBook = libraryManager.findBookByIsbn("ISBNBORROW01");
        assertNotNull(borrowedBook);
        assertEquals(0, borrowedBook.getQuantity(), "Kuantitas buku seharusnya berkurang menjadi 0 setelah dipinjam.");

        // 2. Transaksi tercatat
        List<Transaction> transactions = libraryManager.getTransactionsByMemberId("MEMBER01");
        assertFalse(transactions.isEmpty(), "Seharusnya ada transaksi tercatat untuk member.");
        assertEquals(1, transactions.size(), "Seharusnya hanya ada satu transaksi.");

        Transaction latestTransaction = transactions.get(0); // Karena diurutkan terbaru dulu
        assertEquals("ISBNBORROW01", latestTransaction.getIsbn());
        assertEquals("MEMBER01", latestTransaction.getMemberId());
        assertEquals("Borrowed", latestTransaction.getStatus());
        assertEquals(LocalDate.now(), latestTransaction.getBorrowDate());
        assertEquals(LocalDate.now().plusDays(7), latestTransaction.getDueDate()); // Asumsi default 7 hari
    }

    @Test
    void testBorrowBook_Fail_OutOfStock() {
        Member member = new Member("MEMBER02", "Peminjam Stok Habis", "SI", "stokhabis@example.com", "pass");
        Book book = new Book("ISBNSTOKHABIS01", "Buku Stok Habis", "Author Stok", 0); // Stok awal 0
        libraryManager.addMember(member);
        libraryManager.addBook(book);

        assertFalse(libraryManager.borrowBook("MEMBER02", "ISBNSTOKHABIS01"),
                "Peminjaman buku seharusnya gagal karena stok habis.");

        // Pastikan kuantitas buku tidak berubah (tetap 0)
        Book checkedBook = libraryManager.findBookByIsbn("ISBNSTOKHABIS01");
        assertNotNull(checkedBook);
        assertEquals(0, checkedBook.getQuantity(), "Kuantitas buku seharusnya tetap 0.");
        assertTrue(libraryManager.getTransactionsByMemberId("MEMBER02").isEmpty(),
                "Seharusnya tidak ada transaksi baru tercatat.");
    }

    @Test
    void testBorrowBook_Fail_MemberNotFound() {
        Book book = new Book("ISBNMEMBERNF01", "Buku Member NF", "Author NF", 1);
        libraryManager.addBook(book);

        assertFalse(libraryManager.borrowBook("MEMBERNOTFOUND", "ISBNMEMBERNF01"),
                "Peminjaman buku seharusnya gagal karena member tidak ditemukan.");
    }

    @Test
    void testBorrowBook_Fail_BookNotFound() {
        Member member = new Member("MEMBERBOOKNF01", "Peminjam Buku NF", "DKV", "bukunf@example.com", "pass");
        libraryManager.addMember(member);

        assertFalse(libraryManager.borrowBook("MEMBERBOOKNF01", "ISBNNOTFOUND"),
                "Peminjaman buku seharusnya gagal karena buku tidak ditemukan.");
    }

    @Test
    void testBorrowBook_Fail_AlreadyBorrowedAndNotReturned() {
        Member member = new Member("MEMBERALREADY01", "Peminjam Lagi", "Hukum", "lagi@example.com", "pass");
        Book book = new Book("ISBNALREADY01", "Buku Dipinjam Lagi", "Author Lagi", 2);
        libraryManager.addMember(member);
        libraryManager.addBook(book);

        assertTrue(libraryManager.borrowBook("MEMBERALREADY01", "ISBNALREADY01"),
                "Peminjaman pertama seharusnya berhasil.");
        assertFalse(libraryManager.borrowBook("MEMBERALREADY01", "ISBNALREADY01"),
                "Peminjaman kedua buku yang sama oleh member yang sama sebelum dikembalikan seharusnya gagal.");
    }

    @Test
    void testReturnBook_Successful_WithFine() {
        // Setup member dan buku
        Member member = new Member("MEMBERFINE01", "Peminjam Denda", "Akuntansi", "denda@example.com", "pass");
        Book book = new Book("ISBNFINE01", "Buku Kena Denda", "Author Denda", 1);
        libraryManager.addMember(member);
        libraryManager.addBook(book);

        // Simulasikan peminjaman yang sudah lama terjadi agar terlambat saat
        // dikembalikan
        // Kita perlu cara untuk membuat transaksi dengan borrowDate di masa lalu.
        // Karena borrowBook() menggunakan LocalDate.now(), kita buat transaksi manual
        // untuk tes ini.
        // Atau, kita bisa modifikasi objek transaksi setelah borrowBook() jika
        // memungkinkan (tapi kurang ideal untuk tes).

        // Alternatif: Buat transaksi "lama" secara manual dan tambahkan ke list
        // transaksi LibraryManager
        // Ini memerlukan akses ke list transaksi internal atau metode addTransaction di
        // LibraryManager (yang tidak ada).
        // Untuk kesederhanaan tes, mari kita fokus pada logika returnBook dengan
        // transaksi yang ada.
        // Kita akan meminjam buku, lalu memanipulasi objek transaksi tersebut untuk
        // mensimulasikan keterlambatan.

        libraryManager.borrowBook("MEMBERFINE01", "ISBNFINE01");
        Transaction transactionToReturn = libraryManager.getTransactionsByMemberId("MEMBERFINE01").get(0);
        assertNotNull(transactionToReturn, "Transaksi seharusnya ada.");

        // Ubah borrowDate dan dueDate secara manual untuk simulasi keterlambatan
        // Ini hanya untuk keperluan tes agar bisa menguji denda.
        // Dalam aplikasi nyata, tanggal ini akan sesuai saat peminjaman.
        LocalDate oldBorrowDate = LocalDate.now().minusDays(10); // Dipinjam 10 hari lalu
        LocalDate oldDueDate = oldBorrowDate.plusDays(7); // Jatuh tempo 3 hari lalu dari sekarang

        // Refleksikan perubahan ini ke objek transaksi yang ada di LibraryManager.
        // Cara terbaik adalah jika Transaction memiliki setter atau jika kita bisa
        // mengambil objeknya
        // dan mengubahnya lalu LibraryManager.returnBook memprosesnya.
        // Saat ini, Transaction.java punya setter.
        transactionToReturn.setBorrowDate(oldBorrowDate);
        transactionToReturn.setDueDate(oldDueDate);
        // Kita perlu memastikan LibraryManager menggunakan objek transaksi yang sudah
        // dimodifikasi ini.
        // Karena getTransactionsByMemberId mengembalikan transaksi dari list, perubahan
        // pada objek ini
        // akan tercermin.

        assertTrue(libraryManager.returnBook(transactionToReturn.getTransactionId()),
                "Pengembalian buku seharusnya berhasil.");

        // Verifikasi:
        // 1. Kuantitas buku bertambah
        Book returnedBook = libraryManager.findBookByIsbn("ISBNFINE01");
        assertNotNull(returnedBook);
        assertEquals(1, returnedBook.getQuantity(), "Kuantitas buku seharusnya kembali ke 1 setelah dikembalikan.");

        // 2. Status transaksi menjadi "Returned" dan actualReturnDate terisi
        Transaction updatedTransaction = libraryManager.findTransactionById(transactionToReturn.getTransactionId());
        assertNotNull(updatedTransaction);
        assertEquals("Returned", updatedTransaction.getStatus());
        assertEquals(LocalDate.now(), updatedTransaction.getActualReturnDate());

        // 3. Denda dihitung dengan benar (3 hari terlambat * 1000/hari = 3000)
        // Perhitungan denda (3 hari terlambat * 1000 per hari)
        // LocalDate.now() vs oldDueDate (LocalDate.now().minusDays(3))
        double expectedFine = 3 * 1000.0;
        assertEquals(expectedFine, updatedTransaction.getFine(), 0.01, "Perhitungan denda tidak sesuai.");
    }

    @Test
    void testReturnBook_Successful_NoFine() {
        Member member = new Member("MEMBERNOFINE01", "Peminjam Tepat Waktu", "Biologi", "tepat@example.com", "pass");
        Book book = new Book("ISBNNOFINE01", "Buku Tepat Waktu", "Author Tepat", 1);
        libraryManager.addMember(member);
        libraryManager.addBook(book);

        libraryManager.borrowBook("MEMBERNOFINE01", "ISBNNOFINE01");
        Transaction transactionToReturn = libraryManager.getTransactionsByMemberId("MEMBERNOFINE01").get(0);
        assertNotNull(transactionToReturn);

        // Tidak perlu manipulasi tanggal, karena pengembalian dilakukan hari ini
        // (sebelum/pas jatuh tempo)

        assertTrue(libraryManager.returnBook(transactionToReturn.getTransactionId()),
                "Pengembalian buku tepat waktu seharusnya berhasil.");

        Transaction updatedTransaction = libraryManager.findTransactionById(transactionToReturn.getTransactionId());
        assertNotNull(updatedTransaction);
        assertEquals("Returned", updatedTransaction.getStatus());
        assertEquals(0.0, updatedTransaction.getFine(), "Seharusnya tidak ada denda untuk pengembalian tepat waktu.");

        Book returnedBook = libraryManager.findBookByIsbn("ISBNNOFINE01");
        assertNotNull(returnedBook);
        assertEquals(1, returnedBook.getQuantity(), "Kuantitas buku seharusnya kembali normal.");
    }

    @Test
    void testReturnBook_Fail_TransactionNotFound() {
        assertFalse(libraryManager.returnBook("TRANSNOTFOUND"),
                "Pengembalian seharusnya gagal jika ID transaksi tidak ditemukan.");
    }

    @Test
    void testReturnBook_Fail_AlreadyReturned() {
        Member member = new Member("MEMBERALREADYRET01", "Peminjam Sudah Kembali", "Fisika", "sudahkembali@example.com",
                "pass");
        Book book = new Book("ISBNALREADYRET01", "Buku Sudah Kembali", "Author Kembali", 1);
        libraryManager.addMember(member);
        libraryManager.addBook(book);

        libraryManager.borrowBook("MEMBERALREADYRET01", "ISBNALREADYRET01");
        Transaction transaction = libraryManager.getTransactionsByMemberId("MEMBERALREADYRET01").get(0);
        libraryManager.returnBook(transaction.getTransactionId()); // Kembalikan pertama kali

        // Coba kembalikan lagi
        assertFalse(libraryManager.returnBook(transaction.getTransactionId()),
                "Pengembalian kedua untuk transaksi yang sama seharusnya gagal.");
    }
}