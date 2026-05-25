package com.example.models;

import java.io.Serializable;

/**
 * Model class đại diện cho một giao dịch tài chính (Thu nhập hoặc Chi tiêu).
 * Đạt chuẩn dễ học, dễ nhớ cho sinh viên nghiên cứu Android cơ bản.
 */
public class Transaction implements Serializable {
    private int id;
    private String title;
    private double amount;
    private String type; // "Income" (Thu nhập) hoặc "Expense" (Chi tiêu)
    private String category; // "Ăn uống", "Học tập", "Giải trí", "Di chuyển", "Khác"
    private String date; // Định dạng dd/MM/yyyy

    public Transaction() {
    }

    public Transaction(int id, String title, double amount, String type, String category, String date) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public Transaction(String title, double amount, String type, String category, String date) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
