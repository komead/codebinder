package com.example.code_binder;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Application {
    private String id;
    private List<Product> products;
    private ArrayList<String> allGtins;

    public Application(String id, List<Product> products) {
        this.id = id;
        this.products = products;
        this.allGtins = new ArrayList<>();

        for (Product product : products)
            allGtins.add(product.getGtin());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("\tНомер заявки: " + id + "\n");
        string.append("Необходимо отсканировать: \n");

        for (Product product : products) {
            string.append(product.getTitle()).append(" в количестве ").append(product.getCount()).append(" шт.\n");
        }

        return string.toString();
    }

    public boolean gtinIsExist(String gtin) {
        return allGtins.contains(gtin);
    }
}
