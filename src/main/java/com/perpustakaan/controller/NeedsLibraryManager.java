package com.perpustakaan.controller;

import com.perpustakaan.model.LibraryManager;

/**
 * Interface untuk controller yang membutuhkan instance LibraryManager.
 * Controller yang mengimplementasikan interface ini akan menerima instance LibraryManager
 * melalui metode setLibraryManager().
 */
public interface NeedsLibraryManager {
    void setLibraryManager(LibraryManager libraryManager);
}