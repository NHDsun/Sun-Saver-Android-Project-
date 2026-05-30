package com.example.database;

import com.example.models.Transaction;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private final CollectionReference transactionsRef;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        transactionsRef = db.collection("transactions");
    }

    public Task<DocumentReference> addTransaction(Transaction transaction) {
        return transactionsRef.add(transaction);
    }

    public Query getTransactions(String userRef) {
        return transactionsRef.whereEqualTo("userRef", userRef)
                .orderBy("date", Query.Direction.DESCENDING);
    }

    public Task<Void> updateTransaction(Transaction transaction) {
        return transactionsRef.document(transaction.getId()).set(transaction);
    }

    public Task<Void> deleteTransaction(String id) {
        return transactionsRef.document(id).delete();
    }

    // Lưu ý: Thống kê nên được xử lý thông qua QuerySnapshot hoặc tính toán tại Client/Cloud Functions
}
