package br.com.marketpay.conductor.model;

import java.io.Serializable;

public class Resume implements Serializable {

    private String balance;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Resume{" +
                "balance='" + balance + '\'' +
                '}';
    }
}
