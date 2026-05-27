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
    private ImageButton btnDarkMode, btnLogout;
    private Button btnViewStatistics, btnShareSummary;
    
    // Nút bộ lọc mới thêm
    private Button btnFilterAll, btnFilterIncome, btnFilterExpense;
    private String currentFilterType = "ALL"; // "ALL", "INCOME", "EXPENSE"

    private String loggedInUser = "guest";
    private String userFullName = "Thành viên";
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
        
        // Khởi tạo các nút bộ lọc
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterIncome = findViewById(R.id.btn_filter_income);
        btnFilterExpense = findViewById(R.id.btn_filter_expense);
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

        // Sự kiện gửi báo cáo tài chính qua các app nhắn tin khác
        if (btnShareSummary != null) {
            btnShareSummary.setOnClickListener(v -> shareFinancialSummary());
        }

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

        // Sự kiện click nút bộ lọc
        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> {
                currentFilterType = "ALL";
                updateFilterButtonsUI();
                loadAllData();
            });
        }
        if (btnFilterIncome != null) {
            btnFilterIncome.setOnClickListener(v -> {
                currentFilterType = "INCOME";
                updateFilterButtonsUI();
                loadAllData();
            });
        }
        if (btnFilterExpense != null) {
            btnFilterExpense.setOnClickListener(v -> {
                currentFilterType = "EXPENSE";
                updateFilterButtonsUI();
                loadAllData();
            });
        }

        // Nút Dark Mode
        btnDarkMode.setOnClickListener(v -> toggleDarkMode());

        // Nút Đăng xuất
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void updateFilterButtonsUI() {
        if (btnFilterAll == null || btnFilterIncome == null || btnFilterExpense == null) return;

        int activeBg = R.drawable.bg_filter_chip_active;
        int inactiveBg = R.drawable.bg_filter_chip_inactive;
        int activeTextColor = getResources().getColor(R.color.white);
        int inactiveTextColor = getResources().getColor(R.color.text_secondary_light);

        // Thiết lập đồng bộ màu sắc và background theo trạng thái được kích hoạt
        if ("ALL".equals(currentFilterType)) {
            btnFilterAll.setBackgroundResource(activeBg);
            btnFilterAll.setTextColor(activeTextColor);
            btnFilterIncome.setBackgroundResource(inactiveBg);
            btnFilterIncome.setTextColor(inactiveTextColor);
            btnFilterExpense.setBackgroundResource(inactiveBg);
            btnFilterExpense.setTextColor(inactiveTextColor);
        } else if ("INCOME".equals(currentFilterType)) {
            btnFilterAll.setBackgroundResource(inactiveBg);
            btnFilterAll.setTextColor(inactiveTextColor);
            btnFilterIncome.setBackgroundResource(activeBg);
            btnFilterIncome.setTextColor(activeTextColor);
            btnFilterExpense.setBackgroundResource(inactiveBg);
            btnFilterExpense.setTextColor(inactiveTextColor);
        } else if ("EXPENSE".equals(currentFilterType)) {
            btnFilterAll.setBackgroundResource(inactiveBg);
            btnFilterAll.setTextColor(inactiveTextColor);
            btnFilterIncome.setBackgroundResource(inactiveBg);
            btnFilterIncome.setTextColor(inactiveTextColor);
            btnFilterExpense.setBackgroundResource(activeBg);
            btnFilterExpense.setTextColor(activeTextColor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllData();
    }

    private void loadAllData() {
        if (dbHelper == null) return;

        // Tải các chỉ số từ cơ sở dữ liệu SQLite theo user đang đăng nhập
        android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", MODE_PRIVATE);
        loggedInUser = sharedPrefs.getString("LOGGED_IN_USER", "guest");
        userFullName = sharedPrefs.getString("USER_FULL_NAME", "Thành viên");

        TextView tvGreeting = findViewById(R.id.tv_greeting);
        if (tvGreeting != null) {
            tvGreeting.setText("Xin chào " + userFullName + " ☀️");
        }

        String queryText = (edtSearch != null) ? edtSearch.getText().toString().trim() : "";
        List<Transaction> baseList;
        if (!TextUtils.isEmpty(queryText)) {
            baseList = dbHelper.searchTransactions(queryText, loggedInUser);
        } else {
            baseList = dbHelper.getAllTransactions(loggedInUser);
        }
        if (baseList == null) {
            baseList = new ArrayList<>();
        }

        // Lọc theo tabs/chips được chọn
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : baseList) {
            if ("ALL".equals(currentFilterType)) {
                filteredList.add(t);
            } else if ("INCOME".equals(currentFilterType) && "Income".equalsIgnoreCase(t.getType())) {
                filteredList.add(t);
            } else if ("EXPENSE".equals(currentFilterType) && "Expense".equalsIgnoreCase(t.getType())) {
                filteredList.add(t);
            }
        }
        transactionList = filteredList;

        if (adapter != null) {
            adapter.updateList(transactionList);
        }

        double totalIncome = dbHelper.getTotalIncome(loggedInUser);
        double totalExpense = dbHelper.getTotalExpense(loggedInUser);
        double balance = totalIncome - totalExpense;

        if (tvTotalIncome != null) {
            tvTotalIncome.setText("+" + formatter.format(totalIncome) + " ₫");
        }
        if (tvTotalExpense != null) {
            tvTotalExpense.setText("-" + formatter.format(totalExpense) + " ₫");
        }
        if (tvBalance != null) {
            tvBalance.setText(formatter.format(balance) + " ₫");
        }

        if (layoutEmptyState != null && recyclerView != null) {
            if (transactionList.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void filterTransactions(String keyword) {
        if (dbHelper == null) return;
        List<Transaction> baseList;
        if (TextUtils.isEmpty(keyword)) {
            baseList = dbHelper.getAllTransactions(loggedInUser);
        } else {
            baseList = dbHelper.searchTransactions(keyword, loggedInUser);
        }
        if (baseList == null) {
            baseList = new ArrayList<>();
        }

        // Lọc theo tabs/chips được chọn đồng bộ với tìm kiếm
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : baseList) {
            if ("ALL".equals(currentFilterType)) {
                filteredList.add(t);
            } else if ("INCOME".equals(currentFilterType) && "Income".equalsIgnoreCase(t.getType())) {
                filteredList.add(t);
            } else if ("EXPENSE".equals(currentFilterType) && "Expense".equalsIgnoreCase(t.getType())) {
                filteredList.add(t);
            }
        }
        transactionList = filteredList;

        if (adapter != null) {
            adapter.updateList(transactionList);
        }

        if (layoutEmptyState != null && recyclerView != null) {
            if (transactionList.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Load data on any result (ok or cancels - as fallback)
        loadAllData();
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
                intent.putExtra("TRANSACTION_DATA", transaction);
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

    private void shareFinancialSummary() {
        if (dbHelper == null) return;
        
        double totalIncome = dbHelper.getTotalIncome(loggedInUser);
        double totalExpense = dbHelper.getTotalExpense(loggedInUser);
        double balance = totalIncome - totalExpense;

        StringBuilder sb = new StringBuilder();
        sb.append("☀️ BÁO CÁO TÀI CHÍNH SUN SAVER ☀️\n");
        sb.append("Quản lý chi tiêu & tiết kiệm thông minh\n");
        sb.append("---------------------------------\n");
        sb.append("Chủ tài khoản: ").append(userFullName).append("\n");
        sb.append("Tổng thu nhập: +").append(formatter.format(totalIncome)).append(" ₫\n");
        sb.append("Tổng chi tiêu: -").append(formatter.format(totalExpense)).append(" ₫\n");
        sb.append("Số dư tích luỹ: ").append(formatter.format(balance)).append(" ₫\n");
        sb.append("---------------------------------\n");

        if (transactionList != null && !transactionList.isEmpty()) {
            sb.append("Giao dịch gần đây nhất:\n");
            int limit = Math.min(transactionList.size(), 3);
            for (int i = 0; i < limit; i++) {
                Transaction t = transactionList.get(i);
                String sign = "Income".equalsIgnoreCase(t.getType()) ? "+" : "-";
                sb.append(i + 1).append(". ").append(t.getTitle())
                  .append(" (").append(sign).append(formatter.format(t.getAmount())).append(" ₫)\n");
            }
            sb.append("---------------------------------\n");
        }
        sb.append("Ứng dụng quản lý tài chính Sun Saver ☀️");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Gửi báo cáo tài chính qua:");
        startActivity(shareIntent);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất ⚠️")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản Sun Saver hiện tại?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Xoá session trong SharedPreferences
                    android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean("IS_LOGGED_IN", false);
                    editor.putString("LOGGED_IN_USER", "guest");
                    editor.putString("USER_FULL_NAME", "Thành viên");
                    editor.apply();

                    Toast.makeText(MainActivity.this, "Đã đăng xuất thành công! Hẹn gặp lại bạn. ☀️", Toast.LENGTH_SHORT).show();

                    // Chuyển về màn hình Đăng nhập
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
