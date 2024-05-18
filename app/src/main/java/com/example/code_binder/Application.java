package com.example.code_binder;

import java.util.List;

public class Application {
    private String id;
    private List<Product> products;

    public Application() {
    }

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

    public boolean gtinIsExist(String gtin) {
        for (Product product : products)
            if (product.getGtin().equals(gtin))
                return true;
        return false;
    }
}
