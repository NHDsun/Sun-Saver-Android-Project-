package com.example.activities;

import android.content.DialogInterface;
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
import com.example.database.DatabaseHelper;
import com.example.models.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity của Sun Saver - Quản lý chi tiêu sinh viên.
 * Tương thích hoàn hảo với:
 * - TransactionAdapter (3 tham số: Context, List, OnTransactionLongClickListener)
 * - DatabaseHelper (deleteTransaction(int) trả về void)
 */
public class MainActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionLongClickListener {

    private static final int REQUEST_CODE_ADD = 101;
    private static final int REQUEST_CODE_EDIT = 102;

    private TextView tvBalance, tvTotalIncome, tvTotalExpense;
    private EditText edtSearch;
    private ImageButton btnDarkMode;
    private Button btnViewStatistics;
    private LinearLayout layoutEmptyState;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Helper SQLite
        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupListeners();
        updateThemeButtonIcon();
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tv_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        edtSearch = findViewById(R.id.edt_search);
        btnDarkMode = findViewById(R.id.btn_dark_mode);
        btnViewStatistics = findViewById(R.id.btn_view_statistics);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        recyclerView = findViewById(R.id.recycler_view_transactions);
        fabAdd = findViewById(R.id.fab_add_transaction);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Sử dụng constructor 3 tham số sẵn có trên Disk
        adapter = new TransactionAdapter(this, transactionList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Mở màn hình Thêm giao dịch mới
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD);
        });

        // Mở màn hình Thống kê chi tiết
        btnViewStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        // Tìm kiếm thời gian thực
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút Dark Mode
        btnDarkMode.setOnClickListener(v -> toggleDarkMode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllData();
    }

    private void loadAllData() {
        String queryText = edtSearch.getText().toString().trim();
        if (!TextUtils.isEmpty(queryText)) {
            filterTransactions(queryText);
        } else {
            transactionList = dbHelper.getAllTransactions();
            adapter.updateList(transactionList);
        }

        double totalIncome = dbHelper.getTotalIncome();
        double totalExpense = dbHelper.getTotalExpense();
        double balance = totalIncome - totalExpense;

        tvTotalIncome.setText("+" + formatter.format(totalIncome) + " ₫");
        tvTotalExpense.setText("-" + formatter.format(totalExpense) + " ₫");
        tvBalance.setText(formatter.format(balance) + " ₫");

        if (transactionList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void filterTransactions(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            transactionList = dbHelper.getAllTransactions();
        } else {
            transactionList = dbHelper.searchTransactions(keyword);
        }
        adapter.updateList(transactionList);

        if (transactionList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadAllData();
        }
    }

    /**
     * Triển khai interface OnTransactionLongClickListener từ TransactionAdapter
     */
    @Override
    public void onTransactionLongClick(Transaction transaction, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lựa chọn thao tác");
        
        String[] options = {"Chỉnh sửa giao dịch", "Xoá giao dịch này"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Sửa giao dịch: Gửi sang AddTransactionActivity kèm Object
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                intent.putExtra("EDIT_TRANSACTION_KEY", transaction);
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            } else if (which == 1) {
                // Xác thực và xoá
                confirmDeleteTransaction(transaction);
            }
        });
        builder.show();
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xác thực xoá")
                .setMessage("Bạn chắc chắn muốn xoá ghi chép '" + transaction.getTitle() + "'?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // dbHelper.deleteTransaction trả về void nên gọi tuần tự
                    dbHelper.deleteTransaction(transaction.getId());
                    Toast.makeText(MainActivity.this, "Đã loại bỏ thành công!", Toast.LENGTH_SHORT).show();
                    loadAllData();
                })
                .setNegativeButton("Bỏ qua", null)
                .show();
    }

    private void toggleDarkMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Đã tắt Chế độ tối ☀️", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Đã bật Chế độ tối 🌙", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateThemeButtonIcon() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            btnDarkMode.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnDarkMode.setImageResource(android.R.drawable.ic_menu_compass);
        }
    }
}
