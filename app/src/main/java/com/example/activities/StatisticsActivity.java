package com.example.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.database.DatabaseHelper;
import com.example.models.Transaction;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Thống kê Tài chính đơn giản của Sun Saver.
 * Hiển thị tổng thu, tổng chi, tổng số dư dưới dạng biểu đồ tỉ lệ nén
 * và bảng phân tích chi tiêu chi tiết theo 5 mảng danh mục học đường.
 */
public class StatisticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvStatsBalance, tvStatsIncome, tvStatsExpense;
    private TextView tvRatioIncomePercent, tvRatioExpensePercent;
    private ProgressBar pbRatio;

    // Các thành phần danh mục chi tiêu
    private TextView tvCatFoodAmount, tvCatStudyAmount, tvCatEntertainmentAmount, tvCatTransportAmount, tvCatOtherAmount;
    private ProgressBar pbCatFood, pbCatStudy, pbCatEntertainment, pbCatTransport, pbCatOther;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int layoutStats = getResources().getIdentifier("activity_statistics", "layout", getPackageName());
        setContentView(layoutStats);

        dbHelper = new DatabaseHelper(this);

        // 1. Ánh xạ toàn bộ view trong tệp layout_statistics
        initViews();

        // 2. Quay lại trang cũ khi bấm nút back
        btnBack.setOnClickListener(v -> finish());

        // 3. Tính toán và kết nối hiển thị các con số thống kê
        calculateAndDisplayStatistics();
    }

    private void initViews() {
        String pkg = getPackageName();
        btnBack = findViewById(getResources().getIdentifier("btn_back", "id", pkg));
        
        tvStatsBalance = findViewById(getResources().getIdentifier("tv_stats_balance", "id", pkg));
        tvStatsIncome = findViewById(getResources().getIdentifier("tv_stats_income", "id", pkg));
        tvStatsExpense = findViewById(getResources().getIdentifier("tv_stats_expense", "id", pkg));
        
        tvRatioIncomePercent = findViewById(getResources().getIdentifier("tv_ratio_income_percent", "id", pkg));
        tvRatioExpensePercent = findViewById(getResources().getIdentifier("tv_ratio_expense_percent", "id", pkg));
        pbRatio = findViewById(getResources().getIdentifier("pb_ratio", "id", pkg));

        // Ánh xạ phần danh mục chi tiêu
        tvCatFoodAmount = findViewById(getResources().getIdentifier("tv_cat_food_amount", "id", pkg));
        tvCatStudyAmount = findViewById(getResources().getIdentifier("tv_cat_study_amount", "id", pkg));
        tvCatEntertainmentAmount = findViewById(getResources().getIdentifier("tv_cat_entertainment_amount", "id", pkg));
        tvCatTransportAmount = findViewById(getResources().getIdentifier("tv_cat_transport_amount", "id", pkg));
        tvCatOtherAmount = findViewById(getResources().getIdentifier("tv_cat_other_amount", "id", pkg));

        pbCatFood = findViewById(getResources().getIdentifier("pb_cat_food", "id", pkg));
        pbCatStudy = findViewById(getResources().getIdentifier("pb_cat_study", "id", pkg));
        pbCatEntertainment = findViewById(getResources().getIdentifier("pb_cat_entertainment", "id", pkg));
        pbCatTransport = findViewById(getResources().getIdentifier("pb_cat_transport", "id", pkg));
        pbCatOther = findViewById(getResources().getIdentifier("pb_cat_other", "id", pkg));
    }

    private void calculateAndDisplayStatistics() {
        // TẢI các dữ liệu chỉ định cho người dùng đăng nhập từ SharedPreferences
        android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", MODE_PRIVATE);
        String loggedInUser = sharedPrefs.getString("LOGGED_IN_USER", "guest");

        // Tải các chỉ số từ cơ sở dữ liệu SQLite theo user đăng nhập
        double totalIncome = dbHelper.getTotalIncome(loggedInUser);
        double totalExpense = dbHelper.getTotalExpense(loggedInUser);
        double balance = totalIncome - totalExpense;

        // Định dạng tiền tệ hiển thị cho người học dễ quan sát
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

        // Gán dữ liệu số dư và thu chi tổng thế
        tvStatsBalance.setText(decimalFormat.format(balance) + " ₫");
        tvStatsIncome.setText(decimalFormat.format(totalIncome) + " ₫");
        tvStatsExpense.setText(decimalFormat.format(totalExpense) + " ₫");

        // Tính toán tỉ lệ giữa thu và chi (Trình diễn trực quan bằng ProgressBar)
        double totalSum = totalIncome + totalExpense;
        double incomePercent = 50;
        double expensePercent = 50;

        if (totalSum > 0) {
            incomePercent = (totalIncome / totalSum) * 100;
            expensePercent = 100 - incomePercent;
        }

        // Định dạng rút gọn phần trăm hiển thị
        tvRatioIncomePercent.setText("Thu nhập: " + String.format(Locale.getDefault(), "%.1f", incomePercent) + "%");
        tvRatioExpensePercent.setText("Chi tiêu: " + String.format(Locale.getDefault(), "%.1f", expensePercent) + "%");
        
        // Tiến trình biểu đồ tỉ lệ (màu xanh là tiến trình progress, màu đỏ là nền background)
        pbRatio.setProgress((int) incomePercent);

        // =======================================================
        // TINH TOÁN CHI TIÊU CHO TỪNG PHÂN LOẠI DANH MỤC CHI TIÊU
        // =======================================================
        List<Transaction> allTransactions = dbHelper.getAllTransactions(loggedInUser);
        
        double foodTotal = 0;
        double studyTotal = 0;
        double entertainmentTotal = 0;
        double transportTotal = 0;
        double otherTotal = 0;

        for (Transaction transaction : allTransactions) {
            // Chỉ xếp hạng thống kê các khoản "Chi tiêu" (Expense)
            if ("Expense".equalsIgnoreCase(transaction.getType())) {
                String cat = transaction.getCategory();
                double amt = transaction.getAmount();

                if (cat != null) {
                    if (cat.contains("Ăn uống")) {
                        foodTotal += amt;
                    } else if (cat.contains("Học tập")) {
                        studyTotal += amt;
                    } else if (cat.contains("Giải trí")) {
                        entertainmentTotal += amt;
                    } else if (cat.contains("Di chuyển")) {
                        transportTotal += amt;
                    } else {
                        otherTotal += amt;
                    }
                } else {
                    otherTotal += amt;
                }
            }
        }

        // Đổ số tiền tích lũy của từng nhóm lên UI
        tvCatFoodAmount.setText(decimalFormat.format(foodTotal) + " ₫");
        tvCatStudyAmount.setText(decimalFormat.format(studyTotal) + " ₫");
        tvCatEntertainmentAmount.setText(decimalFormat.format(entertainmentTotal) + " ₫");
        tvCatTransportAmount.setText(decimalFormat.format(transportTotal) + " ₫");
        tvCatOtherAmount.setText(decimalFormat.format(otherTotal) + " ₫");

        // Tính tỉ lệ phần trăm của các danh mục chi tiêu so với Tổng chi tiêu
        int foodProgress = 0;
        int studyProgress = 0;
        int entertainmentProgress = 0;
        int transportProgress = 0;
        int otherProgress = 0;

        if (totalExpense > 0) {
            foodProgress = (int) ((foodTotal / totalExpense) * 100);
            studyProgress = (int) ((studyTotal / totalExpense) * 100);
            entertainmentProgress = (int) ((entertainmentTotal / totalExpense) * 100);
            transportProgress = (int) ((transportTotal / totalExpense) * 100);
            otherProgress = (int) ((otherTotal / totalExpense) * 100);
        }

        // Cập nhật các thanh tiến trình danh mục chi tiêu
        pbCatFood.setProgress(foodProgress);
        pbCatStudy.setProgress(studyProgress);
        pbCatEntertainment.setProgress(entertainmentProgress);
        pbCatTransport.setProgress(transportProgress);
        pbCatOther.setProgress(otherProgress);
    }
}
