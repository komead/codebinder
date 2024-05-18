package com.example.code_binder.adapters;

import java.util.HashSet;
import java.util.Set;

public class ScannedCodesStorage {
    private Set<String> codesForAdd;
    private Set<String> codesForDelete;

    public ScannedCodesStorage() {
        this.codesForAdd = new HashSet<>();
        this.codesForDelete = new HashSet<>();
    }


    public boolean isAdded(String code) {
        if (codesForAdd.contains(code))
            return true;
        else
            return false;
    }

    public boolean isDeleted(String code) {
        if (codesForDelete.contains(code))
            return true;
        else
            return false;
    }

    public boolean isScanned(String code) {
        if (codesForDelete.contains(code) || codesForAdd.contains(code))
            return true;
        else
            return false;
    }

    public String[] dataCutter(String data) {
        String[] dataParts = new String[3];

        char[] buffer = new char[14];
        data.getChars(3, 17, buffer, 0);
        dataParts[0] = String.copyValueOf(buffer);

        buffer = new char[8];
        data.getChars(19, 27, buffer, 0);
        dataParts[1] = String.copyValueOf(buffer);

        buffer = new char[4];
        data.getChars(30, 34, buffer, 0);
        dataParts[2] = String.copyValueOf(buffer);

        return dataParts;
    }

    public int quantity() {
        return codesForAdd.size() + codesForDelete.size();
    }

    public Set<String> getCodesForAdd() {
        return codesForAdd;
    }

    public Set<String> getCodesForDelete() {
        return codesForDelete;
    }

    public void addCode(String code) {
        codesForAdd.add(code);
    }

    public void deleteCode(String code) {
        codesForDelete.add(code);
    }
}
