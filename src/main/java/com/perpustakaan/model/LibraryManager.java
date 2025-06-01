package com.perpustakaan.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeParseException;

public class LibraryManager {
    private List<Book> books;
    private List<Member> members;
    private List<Transaction> transactions;

    private static final String DATA_FOLDER = "data";
    private static final String BOOKS_FILE_PATH = DATA_FOLDER + "/books.csv";
    private static final String MEMBERS_FILE_PATH = DATA_FOLDER + "/members.csv";
    private static final String TRANSACTIONS_FILE_PATH = DATA_FOLDER + "/transactions.csv";

    public LibraryManager() {
        this.books = new ArrayList<>();
        this.members = new ArrayList<>();
        this.transactions = new ArrayList<>();

        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            System.err.println("Gagal membuat direktori data: " + e.getMessage());
        }

        loadBooks();
        loadMembers();
        loadTransactions();
    }

    // =================================================================================
    // Operasi CRUD untuk Buku (Book) 
    // =================================================================================
    // ... (metode addBook, findBookByIsbn, getAllBooks, updateBook, deleteBook, loadBooks, saveBooks tetap sama seperti yang Anda berikan) ...
    public boolean addBook(Book book) {
        if (findBookByIsbn(book.getIsbn()) != null) {
            System.out.println("Error: Buku dengan ISBN " + book.getIsbn() + " sudah ada.");
            return false;
        }
        this.books.add(book);
        saveBooks();
        return true;
    }

    public Book findBookByIsbn(String isbn) {
        for (Book book : this.books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(this.books);
    }

    public boolean updateBook(Book updatedBook) {
        Book existingBook = findBookByIsbn(updatedBook.getIsbn());
        if (existingBook != null) {
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setQuantity(updatedBook.getQuantity());
            saveBooks();
            return true;
        }
        System.out.println("Error: Buku dengan ISBN " + updatedBook.getIsbn() + " tidak ditemukan untuk diperbarui.");
        return false;
    }

    public boolean deleteBook(String isbn) {
        Book bookToRemove = findBookByIsbn(isbn);
        if (bookToRemove != null) {
            this.books.remove(bookToRemove);
            saveBooks();
            return true;
        }
        System.out.println("Error: Buku dengan ISBN " + isbn + " tidak ditemukan untuk dihapus.");
        return false;
    }

    private void loadBooks() {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE_PATH))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] values = line.split(",");
                if (values.length >= 4) {
                    String isbn = values[0].trim();
                    String title = values[1].trim();
                    String author = values[2].trim();
                    int quantity = Integer.parseInt(values[3].trim());
                    this.books.add(new Book(isbn, title, author, quantity));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + BOOKS_FILE_PATH + " tidak ditemukan. Membuat file baru dengan header.");
            try {
                if (!Files.exists(Paths.get(BOOKS_FILE_PATH))) {
                    Files.createFile(Paths.get(BOOKS_FILE_PATH));
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE_PATH, false))) { 
                    writer.write("ISBN,Title,Author,Quantity\n");
                }
            } catch (IOException ioException) {
                System.err.println("Gagal membuat atau menulis header ke file " + BOOKS_FILE_PATH + ": "
                        + ioException.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error membaca file " + BOOKS_FILE_PATH + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error format angka pada file " + BOOKS_FILE_PATH + ": " + e.getMessage());
        }
    }

    private void saveBooks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_FILE_PATH))) {
            bw.write("ISBN,Title,Author,Quantity\n");
            for (Book book : this.books) {
                String line = String.join(",",
                        book.getIsbn(),
                        book.getTitle(),
                        book.getAuthor(),
                        String.valueOf(book.getQuantity()));
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error menyimpan ke file " + BOOKS_FILE_PATH + ": " + e.getMessage());
        }
    }

    // =================================================================================
    // Implementasi CRUD untuk Anggota (Member)
    // =================================================================================

    public boolean addMember(Member member) {
        if (findMemberById(member.getId()) != null) {
            System.out.println("Error: Anggota dengan ID " + member.getId() + " sudah terdaftar.");
            return false;
        }
        if (findMemberByEmail(member.getEmail()) != null) {
            System.out.println("Error: Anggota dengan Email " + member.getEmail() + " sudah terdaftar.");
            return false;
        }
        this.members.add(member);
        saveMembers();
        System.out.println("Anggota " + member.getFullName() + " berhasil ditambahkan.");
        return true;
    }

    public Member findMemberById(String memberId) {
        for (Member member : this.members) {
            if (member.getId().equalsIgnoreCase(memberId)) {
                return member;
            }
        }
        return null;
    }

    public Member findMemberByEmail(String email) {
        for (Member member : this.members) {
            if (member.getEmail().equalsIgnoreCase(email)) {
                return member;
            }
        }
        return null;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(this.members);
    }

    public boolean updateMember(Member updatedMember) {
        Member existingMember = findMemberById(updatedMember.getId());
        if (existingMember != null) {
            // Validasi duplikasi email jika email diubah
            if (!existingMember.getEmail().equalsIgnoreCase(updatedMember.getEmail()) &&
                    findMemberByEmail(updatedMember.getEmail()) != null) {
                System.out.println("Error: Email " + updatedMember.getEmail() + " sudah digunakan oleh anggota lain.");
                return false;
            }
            
            // Perbarui atribut-atribut
            existingMember.setFullName(updatedMember.getFullName());
            existingMember.setMajor(updatedMember.getMajor());
            existingMember.setEmail(updatedMember.getEmail());
            
            // --- PERBAIKAN/PENEGASAN UNTUK UPDATE PASSWORD ---
            // Jika objek updatedMember memiliki password yang valid (misalnya, tidak null atau kosong jika itu aturannya)
            // dan berbeda dari password lama, atau jika Anda selalu ingin mengupdate password berdasarkan updatedMember.
            // Untuk kasus tes kita, updatedMember akan membawa password baru.
            if (updatedMember.getPassword() != null && !updatedMember.getPassword().isEmpty()) {
                 existingMember.setPassword(updatedMember.getPassword());
            }
            // --- AKHIR PERBAIKAN ---
            
            saveMembers();
            System.out.println("Data anggota " + existingMember.getId() + " berhasil diperbarui.");
            return true;
        }
        System.out.println("Error: Anggota dengan ID " + updatedMember.getId() + " tidak ditemukan untuk diperbarui.");
        return false;
    }

    public boolean deleteMember(String memberId) {
        Member memberToRemove = findMemberById(memberId);
        if (memberToRemove != null) {
            this.members.remove(memberToRemove);
            saveMembers();
            System.out.println("Anggota dengan ID " + memberId + " berhasil dihapus.");
            return true;
        }
        System.out.println("Error: Anggota dengan ID " + memberId + " tidak ditemukan untuk dihapus.");
        return false;
    }

    private void loadMembers() {
        String expectedHeader = "ID,NamaLengkap,Jurusan,Email,Password";
        try (BufferedReader br = new BufferedReader(new FileReader(MEMBERS_FILE_PATH))) {
            String line;
            String header = br.readLine();
            if (header == null || !header.trim().equalsIgnoreCase(expectedHeader)) {
                if (header != null && !header.trim().isEmpty()) {
                    System.err.println("Peringatan: Header file " + MEMBERS_FILE_PATH
                            + " tidak sesuai. Menggunakan baris pertama sebagai data jika memungkinkan.");
                    processMemberLine(header);
                } else {
                    System.out.println("File " + MEMBERS_FILE_PATH + " kosong atau header tidak ditemukan.");
                }
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                processMemberLine(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + MEMBERS_FILE_PATH + " tidak ditemukan. Membuat file baru dengan header: "
                    + expectedHeader);
            try {
                if (!Files.exists(Paths.get(MEMBERS_FILE_PATH))) {
                    Files.createFile(Paths.get(MEMBERS_FILE_PATH));
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBERS_FILE_PATH, false))) {
                    writer.write(expectedHeader + "\n");
                }
            } catch (IOException ioException) {
                System.err.println("Gagal membuat atau menulis header ke file " + MEMBERS_FILE_PATH + ": "
                        + ioException.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error membaca file " + MEMBERS_FILE_PATH + ": " + e.getMessage());
        }
    }

    private void processMemberLine(String line) {
        String[] values = line.split(",");
        if (values.length >= 5) {
            String id = values[0].trim();
            String fullName = values[1].trim();
            String major = values[2].trim();
            String email = values[3].trim();
            String password = values[4].trim();
            this.members.add(new Member(id, fullName, major, email, password));
        } else {
            System.err.println("Baris data anggota tidak lengkap di members.csv (kurang dari 5 kolom): " + line);
        }
    }

    private void saveMembers() {
        String header = "ID,NamaLengkap,Jurusan,Email,Password";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEMBERS_FILE_PATH))) {
            bw.write(header + "\n");
            for (Member member : this.members) {
                String line = String.join(",",
                        member.getId(),
                        member.getFullName(),
                        member.getMajor() != null ? member.getMajor() : "",
                        member.getEmail(),
                        member.getPassword());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error menyimpan ke file " + MEMBERS_FILE_PATH + ": " + e.getMessage());
        }
    }

    // =================================================================================
    // Implementasi Logika Bisnis & CRUD untuk Transaksi (Transaction)
    // =================================================================================
    // ... (metode borrowBook, returnBook, findTransactionById, getAllTransactions, getBorrowedBooksReport, loadTransactions, saveTransactions tetap sama seperti yang Anda berikan) ...
    public boolean borrowBook(String memberId, String isbn) {
        Member member = findMemberById(memberId);
        if (member == null) {
            System.out.println("Error Peminjaman: Anggota dengan ID " + memberId + " tidak ditemukan.");
            return false;
        }

        Book book = findBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Error Peminjaman: Buku dengan ISBN " + isbn + " tidak ditemukan.");
            return false;
        }

        if (book.getQuantity() <= 0) {
            System.out.println("Error Peminjaman: Stok buku \"" + book.getTitle() + "\" habis.");
            return false;
        }

        for (Transaction t : this.transactions) {
            if (t.getMemberId().equals(memberId) &&
                    t.getIsbn().equals(isbn) &&
                    ("Borrowed".equalsIgnoreCase(t.getStatus()) || "Overdue".equalsIgnoreCase(t.getStatus()))) {
                System.out.println("Error Peminjaman: Anggota " + memberId + " sudah meminjam buku \"" + book.getTitle()
                        + "\" dan belum dikembalikan.");
                return false;
            }
        }
        String transactionId = "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction newTransaction = new Transaction(transactionId, memberId, isbn, LocalDate.now());
        this.transactions.add(newTransaction);
        book.setQuantity(book.getQuantity() - 1);
        saveTransactions();
        saveBooks();
        System.out.println("Peminjaman berhasil: " + member.getFullName() + " meminjam \"" + book.getTitle()
                + "\". ID Transaksi: " + transactionId);
        return true;
    }

    public boolean returnBook(String transactionId) {
        Transaction transaction = findTransactionById(transactionId);
        if (transaction == null) {
            System.out.println("Error Pengembalian: Transaksi dengan ID " + transactionId + " tidak ditemukan.");
            return false;
        }
        if ("Returned".equalsIgnoreCase(transaction.getStatus())) {
            System.out.println("Info: Buku untuk transaksi ID " + transactionId + " sudah dikembalikan sebelumnya.");
            return false;
        }
        Book book = findBookByIsbn(transaction.getIsbn());
        if (book != null) {
            book.setQuantity(book.getQuantity() + 1);
        } else {
            System.err.println("Peringatan: Buku dengan ISBN " + transaction.getIsbn() + " yang terkait transaksi "
                    + transactionId + " tidak ditemukan di katalog. Kuantitas tidak diperbarui.");
        }
        transaction.setActualReturnDate(LocalDate.now());
        transaction.setStatus("Returned");
        double fine = transaction.calculateFine();
        saveTransactions();
        if (book != null) {
            saveBooks();
        }
        System.out.println("Pengembalian buku untuk transaksi ID " + transactionId + " berhasil.");
        if (fine > 0) {
            System.out.println("Denda keterlambatan: Rp" + fine);
        }
        return true;
    }

    public Transaction findTransactionById(String transactionId) {
        for (Transaction transaction : this.transactions) {
            if (transaction.getTransactionId().equalsIgnoreCase(transactionId)) {
                return transaction;
            }
        }
        return null;
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(this.transactions);
    }

    public List<Transaction> getBorrowedBooksReport() {
        return this.transactions.stream()
                .filter(t -> "Borrowed".equalsIgnoreCase(t.getStatus()) || "Overdue".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
    }

    private void loadTransactions() {
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE_PATH))) {
            String line;
            String header = br.readLine(); 
            if (header == null || !header.trim()
                    .equalsIgnoreCase("TransactionID,MemberID,ISBN,BorrowDate,DueDate,ActualReturnDate,Status,Fine")) {
                if (header != null && !header.trim().isEmpty()) { 
                    System.err.println("Peringatan: Header file " + TRANSACTIONS_FILE_PATH
                            + " tidak sesuai harapan. Konten aktual: " + header);
                }
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] values = line.split(",");
                if (values.length >= 8) {
                    try {
                        String id = values[0].trim();
                        String memberId = values[1].trim();
                        String isbn = values[2].trim();
                        LocalDate borrowDate = LocalDate.parse(values[3].trim());
                        LocalDate dueDate = LocalDate.parse(values[4].trim());
                        LocalDate actualReturnDate = (values[5].trim().isEmpty()
                                || "null".equalsIgnoreCase(values[5].trim())) ? null
                                        : LocalDate.parse(values[5].trim());
                        String status = values[6].trim();
                        double fine = Double.parseDouble(values[7].trim());

                        Transaction t = new Transaction(id, memberId, isbn, borrowDate);
                        t.setDueDate(dueDate);
                        t.setActualReturnDate(actualReturnDate);
                        t.setStatus(status);
                        t.setFine(fine);
                        this.transactions.add(t);

                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing tanggal di transactions.csv pada baris: " + line + ". Error: "
                                + e.getMessage());
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing angka (denda) di transactions.csv pada baris: " + line
                                + ". Error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Baris data transaksi tidak lengkap di transactions.csv: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + TRANSACTIONS_FILE_PATH + " tidak ditemukan. Membuat file baru dengan header.");
            try {
                if (!Files.exists(Paths.get(TRANSACTIONS_FILE_PATH))) {
                    Files.createFile(Paths.get(TRANSACTIONS_FILE_PATH));
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE_PATH, false))) {
                    writer.write("TransactionID,MemberID,ISBN,BorrowDate,DueDate,ActualReturnDate,Status,Fine\n");
                }
            } catch (IOException ioException) {
                System.err.println("Gagal membuat atau menulis header ke file " + TRANSACTIONS_FILE_PATH + ": "
                        + ioException.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error membaca file " + TRANSACTIONS_FILE_PATH + ": " + e.getMessage());
        }
    }

    private void saveTransactions() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE_PATH))) {
            bw.write("TransactionID,MemberID,ISBN,BorrowDate,DueDate,ActualReturnDate,Status,Fine\n");
            for (Transaction t : this.transactions) {
                String actualReturnDateStr = (t.getActualReturnDate() == null) ? ""
                        : t.getActualReturnDate().toString();
                String line = String.join(",",
                        t.getTransactionId(),
                        t.getMemberId(),
                        t.getIsbn(),
                        t.getBorrowDate().toString(),
                        t.getDueDate().toString(),
                        actualReturnDateStr,
                        t.getStatus(),
                        String.valueOf(t.getFine()));
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error menyimpan ke file " + TRANSACTIONS_FILE_PATH + ": " + e.getMessage());
        }
    }

    // Metode getTransactionsByMemberId dan getMonthlyStatistics tetap sama seperti yang Anda berikan
    public List<Transaction> getTransactionsByMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return this.transactions.stream()
                .filter(transaction -> memberId.equals(transaction.getMemberId()))
                .sorted((t1, t2) -> t2.getBorrowDate().compareTo(t1.getBorrowDate()))
                .collect(Collectors.toList());
    }

    public List<MonthlyStat> getMonthlyStatistics() {
        Map<YearMonth, MonthlyStat> statsMap = new HashMap<>();
        for (Transaction transaction : this.transactions) {
            YearMonth borrowMonthYear = YearMonth.from(transaction.getBorrowDate());
            statsMap.putIfAbsent(borrowMonthYear, new MonthlyStat(borrowMonthYear));
            statsMap.get(borrowMonthYear).incrementBorrows();

            if (transaction.getActualReturnDate() != null && "Returned".equalsIgnoreCase(transaction.getStatus())) {
                YearMonth returnMonthYear = YearMonth.from(transaction.getActualReturnDate());
                statsMap.putIfAbsent(returnMonthYear, new MonthlyStat(returnMonthYear));
                statsMap.get(returnMonthYear).incrementReturns();
                if (transaction.getFine() > 0) {
                    statsMap.get(returnMonthYear).addFines(transaction.getFine());
                }
            }
        }
        return statsMap.values().stream()
                .sorted((s1, s2) -> s1.getMonthYear().compareTo(s2.getMonthYear()))
                .collect(Collectors.toList());
    }
}