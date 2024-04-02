package com.example.code_binder;

public class Application {
    private String title;
    private int count;

    private int alreadyScanned;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
