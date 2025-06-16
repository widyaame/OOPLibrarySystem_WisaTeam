package com.perpustakaan.model;

public class Member {
    private String memberId; 
    private String pin;
    private String fullName;
    private String major; 
    private String email;

    public Member(String memberId, String pin, String fullName, String major, String email) {
        this.memberId = memberId;
        this.pin = pin;
        this.fullName = fullName;
        this.major = major;
        this.email = email;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}