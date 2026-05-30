package com.example.models;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

/**
 * Model class đại diện cho một giao dịch tài chính (Thu nhập hoặc Chi tiêu).
 * Đã cập nhật để tương thích với Firebase Firestore.
 */
public class Transaction implements Serializable {
    private String id; // Sử dụng String cho Firestore Document ID
    private String title;
    private double amount;
    private String type; // "Income" hoặc "Expense"
    private String category;
    private String date;
    private String userRef;

    public Transaction() {
        // Constructor rỗng cần thiết cho Firebase
    }

    public Transaction(String title, double amount, String type, String category, String date, String userRef) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.userRef = userRef;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getUserRef() {
        return userRef;
    }

    public void setUserRef(String userRef) {
        this.userRef = userRef;
    }
}
