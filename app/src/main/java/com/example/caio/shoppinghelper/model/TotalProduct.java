package com.example.caio.shoppinghelper.model;

public class TotalProduct {

    private String TotalProduct;
    private String isPayed;

    public TotalProduct(){

        this.isPayed = "N";
    }

    public String getTotalProduct() {
        return TotalProduct;
    }

    public void setTotalProduct(String totalProduct) {
        TotalProduct = totalProduct;
    }

    public String getIsPayed() {
        return isPayed;
    }

    public void setIsPayed(String isPayed) {
        this.isPayed = isPayed;
    }
}
