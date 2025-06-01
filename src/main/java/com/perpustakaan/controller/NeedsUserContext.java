package com.perpustakaan.controller;

public interface NeedsUserContext {
    /**
     * Mengatur konteks pengguna untuk controller ini.
     * @param userId ID pengguna yang sedang login (misalnya, ID Member "M001").
     * @param userRole Peran pengguna yang sedang login.
     * @param displayName Nama tampilan pengguna (misalnya, "Budi Santoso").
     */
    void setUserContext(String userId, String userRole, String displayName);
}