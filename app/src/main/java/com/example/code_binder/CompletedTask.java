package com.example.code_binder;

import java.util.ArrayList;
import java.util.List;

public class CompletedTask {
    private String number;
    private List<Info> gtins;

    public CompletedTask(String number, List<Product> products) {
        this.number = number;
        this.gtins = new ArrayList<>();

        for (Product product : products)
            for (String string : product.getScannedPackages())
                gtins.add(new Info(product.getGtin(), string));
    }

    private static class Info {
        private String gtin;
        private String package_number;

        public Info(String gtin, String package_number) {
            this.gtin = gtin;
            this.package_number = package_number;
        }
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
