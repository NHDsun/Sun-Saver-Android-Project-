package com.example.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.adapters.TransactionAdapter;
import com.example.database.FirestoreHelper;
import com.example.models.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity của Sun Saver - Đã cập nhật sử dụng Firebase Firestore.
 */
public class MainActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionLongClickListener {

    private static final int REQUEST_CODE_ADD = 101;
    private static final int REQUEST_CODE_EDIT = 102;

    private TextView tvBalance, tvTotalIncome, tvTotalExpense;
    private EditText edtSearch;
    private ImageButton btnDarkMode, btnLogout;
    private Button btnViewStatistics, btnShareSummary;
    
    private Button btnFilterAll, btnFilterIncome, btnFilterExpense;
    private String currentFilterType = "ALL";

    private String loggedInUser = "guest";
    private String userFullName = "Thành viên";
    private LinearLayout layoutEmptyState;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private FirestoreHelper firestoreHelper;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestoreHelper = new FirestoreHelper();

        initViews();
        setupRecyclerView();
        setupListeners();
        updateThemeButtonIcon();
        updateFilterButtonsUI();
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tv_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        edtSearch = findViewById(R.id.edt_search);
        btnDarkMode = findViewById(R.id.btn_dark_mode);
        btnLogout = findViewById(R.id.btn_logout);
        btnViewStatistics = findViewById(R.id.btn_view_statistics);
        btnShareSummary = findViewById(R.id.btn_share_summary);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        recyclerView = findViewById(R.id.recycler_view_transactions);
        fabAdd = findViewById(R.id.fab_add_transaction);
        
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterIncome = findViewById(R.id.btn_filter_income);
        btnFilterExpense = findViewById(R.id.btn_filter_expense);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, transactionList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD);
        });

        btnViewStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        if (btnShareSummary != null) {
            btnShareSummary.setOnClickListener(v -> shareFinancialSummary());
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDataFromFirestore();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> {
                currentFilterType = "ALL";
                updateFilterButtonsUI();
                loadDataFromFirestore();
            });
        }
        if (btnFilterIncome != null) {
            btnFilterIncome.setOnClickListener(v -> {
                currentFilterType = "INCOME";
                updateFilterButtonsUI();
                loadDataFromFirestore();
            });
        }
        if (btnFilterExpense != null) {
            btnFilterExpense.setOnClickListener(v -> {
                currentFilterType = "EXPENSE";
                updateFilterButtonsUI();
                loadDataFromFirestore();
            });
        }

        btnDarkMode.setOnClickListener(v -> toggleDarkMode());

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void updateFilterButtonsUI() {
        if (btnFilterAll == null) return;
        int activeBg = R.drawable.bg_filter_chip_active;
        int inactiveBg = R.drawable.bg_filter_chip_inactive;
        btnFilterAll.setBackgroundResource("ALL".equals(currentFilterType) ? activeBg : inactiveBg);
        btnFilterIncome.setBackgroundResource("INCOME".equals(currentFilterType) ? activeBg : inactiveBg);
        btnFilterExpense.setBackgroundResource("EXPENSE".equals(currentFilterType) ? activeBg : inactiveBg);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDataFromFirestore();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void loadDataFromFirestore() {
        android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", MODE_PRIVATE);
        loggedInUser = sharedPrefs.getString("LOGGED_IN_USER", "guest");
        userFullName = sharedPrefs.getString("USER_FULL_NAME", "Thành viên");

        TextView tvGreeting = findViewById(R.id.tv_greeting);
        if (tvGreeting != null) tvGreeting.setText("Xin chào " + userFullName + " ☀️");

        if (firestoreListener != null) firestoreListener.remove();

        firestoreListener = firestoreHelper.getTransactions(loggedInUser)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Transaction> fullList = new ArrayList<>();
                    double income = 0;
                    double expense = 0;
                    String search = edtSearch.getText().toString().toLowerCase().trim();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            t.setId(doc.getId());
                            
                            // Tính toán tổng thu chi
                            if ("Income".equalsIgnoreCase(t.getType())) income += t.getAmount();
                            else expense += t.getAmount();

                            // Lọc theo search và type
                            boolean matchesSearch = TextUtils.isEmpty(search) || t.getTitle().toLowerCase().contains(search);
                            boolean matchesType = "ALL".equals(currentFilterType) || 
                                                 ("INCOME".equals(currentFilterType) && "Income".equalsIgnoreCase(t.getType())) ||
                                                 ("EXPENSE".equals(currentFilterType) && "Expense".equalsIgnoreCase(t.getType()));

                            if (matchesSearch && matchesType) {
                                fullList.add(t);
                            }
                        }
                    }

                    transactionList = fullList;
                    adapter.updateList(transactionList);
                    updateUI(income, expense);
                });
    }

    private void updateUI(double income, double expense) {
        tvTotalIncome.setText("+" + formatter.format(income) + " ₫");
        tvTotalExpense.setText("-" + formatter.format(expense) + " ₫");
        tvBalance.setText(formatter.format(income - expense) + " ₫");

        if (transactionList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTransactionLongClick(Transaction transaction, int position) {
        String[] options = {"Chỉnh sửa", "Xoá"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                        intent.putExtra("TRANSACTION_DATA", transaction);
                        startActivityForResult(intent, REQUEST_CODE_EDIT);
                    } else {
                        confirmDeleteTransaction(transaction);
                    }
                }).show();
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Xoá '" + transaction.getTitle() + "'?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    firestoreHelper.deleteTransaction(transaction.getId())
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xoá!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void toggleDarkMode() {
        int mode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) ?
                AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void updateThemeButtonIcon() {
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        btnDarkMode.setImageResource(nightMode == Configuration.UI_MODE_NIGHT_YES ? 
                android.R.drawable.ic_menu_day : android.R.drawable.ic_menu_recent_history);
    }

    private void shareFinancialSummary() {
        // Logic tóm tắt dữ liệu từ transactionList và gửi Intent
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setMessage("Đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    getSharedPreferences("SunSaverPrefs", MODE_PRIVATE).edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Hủy", null).show();
    }
}
