package com.perpustakaan.model; 

public class Book {
    // Atribut/properti buku
    private String isbn;
    private String title;
    private String author;
    private int quantity; 

    // Konstruktor
    public Book(String isbn, String title, String author, int quantity) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.quantity = quantity;
    }

    // Getter
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setter 
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        } else {
            System.err.println("Quantity cannot be negative.");
        }
    }

    // Metode toString() untuk representasi string dari objek Book
    @Override
    public String toString() {
        return "Book [ISBN=" + isbn + ", Title=" + title + ", Author=" + author + ", Quantity=" + quantity + "]";
    }
}