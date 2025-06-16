package com.perpustakaan.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.perpustakaan.model.Member;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAO {
    private static final String CSV_FILE_PATH = "data/members.csv";

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            reader.skip(1);
            List<String[]> records = reader.readAll();
            for (String[] record : records) {
                Member member = new Member(record[0], record[1], record[2], record[3], record[4]);
                members.add(member);
            }
        } catch (IOException | CsvException e) {
            System.err.println("Error membaca file CSV anggota: " + e.getMessage());
        }
        return members;
    }

    public Optional<Member> getMemberById(String memberId) {
        return getAllMembers().stream()
                .filter(member -> member.getMemberId().equals(memberId))
                .findFirst();
    }

    public void addMember(Member newMember) {
        List<Member> members = getAllMembers();
        members.add(newMember);
        writeAllMembers(members);
    }
    
    public void updateMember(Member updatedMember) {
        List<Member> members = getAllMembers();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getMemberId().equals(updatedMember.getMemberId())) {
                members.set(i, updatedMember);
                break;
            }
        }
        writeAllMembers(members);
    }

    public void deleteMember(String memberId) {
        List<Member> members = getAllMembers();
        members.removeIf(member -> member.getMemberId().equals(memberId));
        writeAllMembers(members);
    }

    private void writeAllMembers(List<Member> members) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
            String[] header = {"memberId", "pin", "fullName", "major", "email"};
            writer.writeNext(header);
            for (Member member : members) {
                String[] record = {
                    member.getMemberId(),
                    member.getPin(),
                    member.getFullName(),
                    member.getMajor(),
                    member.getEmail()
                };
                writer.writeNext(record);
            }
        } catch (IOException e) {
            System.err.println("Error menulis ke file CSV anggota: " + e.getMessage());
        }
    }
}