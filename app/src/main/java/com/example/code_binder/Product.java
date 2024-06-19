package com.example.code_binder;

import java.util.HashSet;
import java.util.Set;

public class Product {
    private String title;
    private String gtin;
    private int count;
    private int alreadyScanned;
    private Set<String> scannedPackages = new HashSet<>();

    public Product() {
    }

    public Product(String title, String gtin, int count) {
        this.title = title;
        this.gtin = gtin;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAlreadyScanned() {
        return alreadyScanned;
    }

    public void setAlreadyScanned(int alreadyScanned) {
        this.alreadyScanned = alreadyScanned;
    }

    public Set<String> getScannedPackages() {
        return scannedPackages;
    }

    public void addPackage(String string) {
        scannedPackages.add(string);
    }

    public void deletePackage(String string) {
        scannedPackages.remove(string);
    }

    public boolean isScanned(String string) {
        return scannedPackages.contains(string);
    }
}
