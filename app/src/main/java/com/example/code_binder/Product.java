package com.example.code_binder;

public class Product {
    private String title;
    private String gtin;
    private int count;

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
}
