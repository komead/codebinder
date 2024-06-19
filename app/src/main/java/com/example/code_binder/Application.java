package com.example.code_binder;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Application implements Serializable {
    private String id;
    private List<Product> products;
    private HashMap<String, Integer> allGtins;

    public Application(String id, List<Product> products) {
        this.id = id;
        this.products = products;
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

    public void fillAllGtins() {
        allGtins = new HashMap<>();

        for (Product product : products)
            allGtins.put(product.getGtin(), product.getCount());
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
        return allGtins.containsKey(gtin);
    }

    public int getCountByGtin(String gtin) {
        return allGtins.get(gtin);
    }

    public Product getProductByGtin(String gtin) {
        for (Product product : products)
            if (product.getGtin().equals(gtin))
                return product;
        return null;
    }
}
