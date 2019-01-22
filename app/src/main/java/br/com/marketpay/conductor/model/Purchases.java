package br.com.marketpay.conductor.model;

import java.util.List;

public class Purchases {

    private List<Purchase> purchases;

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "Purchases{" +
                "purchases=" + purchases +
                '}';
    }
}
