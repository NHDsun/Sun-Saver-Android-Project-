package com.example.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.database.FirestoreHelper;
import com.example.models.Transaction;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Thống kê Tài chính - Đã cập nhật sử dụng Firebase Firestore.
 */
public class StatisticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvStatsBalance, tvStatsIncome, tvStatsExpense;
    private TextView tvRatioIncomePercent, tvRatioExpensePercent;
    private ProgressBar pbRatio;

    private TextView tvCatFoodAmount, tvCatStudyAmount, tvCatEntertainmentAmount, tvCatTransportAmount, tvCatOtherAmount;
    private ProgressBar pbCatFood, pbCatStudy, pbCatEntertainment, pbCatTransport, pbCatOther;

    private FirestoreHelper firestoreHelper;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        firestoreHelper = new FirestoreHelper();

        initViews();
        btnBack.setOnClickListener(v -> finish());
        loadStatisticsFromFirestore();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvStatsBalance = findViewById(R.id.tv_stats_balance);
        tvStatsIncome = findViewById(R.id.tv_stats_income);
        tvStatsExpense = findViewById(R.id.tv_stats_expense);
        
        tvRatioIncomePercent = findViewById(R.id.tv_ratio_income_percent);
        tvRatioExpensePercent = findViewById(R.id.tv_ratio_expense_percent);
        pbRatio = findViewById(R.id.pb_ratio);

        tvCatFoodAmount = findViewById(R.id.tv_cat_food_amount);
        tvCatStudyAmount = findViewById(R.id.tv_cat_study_amount);
        tvCatEntertainmentAmount = findViewById(R.id.tv_cat_entertainment_amount);
        tvCatTransportAmount = findViewById(R.id.tv_cat_transport_amount);
        tvCatOtherAmount = findViewById(R.id.tv_cat_other_amount);

        pbCatFood = findViewById(R.id.pb_cat_food);
        pbCatStudy = findViewById(R.id.pb_cat_study);
        pbCatEntertainment = findViewById(R.id.pb_cat_entertainment);
        pbCatTransport = findViewById(R.id.pb_cat_transport);
        pbCatOther = findViewById(R.id.pb_cat_other);
    }

    private void loadStatisticsFromFirestore() {
        android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", MODE_PRIVATE);
        String loggedInUser = sharedPrefs.getString("LOGGED_IN_USER", "guest");

        firestoreHelper.getTransactions(loggedInUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0;
                    double totalExpense = 0;
                    
                    double foodTotal = 0;
                    double studyTotal = 0;
                    double entertainmentTotal = 0;
                    double transportTotal = 0;
                    double otherTotal = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            if ("Income".equalsIgnoreCase(t.getType())) {
                                totalIncome += t.getAmount();
                            } else {
                                totalExpense += t.getAmount();
                                // Phân loại chi tiêu
                                String cat = t.getCategory();
                                double amt = t.getAmount();
                                if (cat != null) {
                                    if (cat.contains("Ăn uống")) foodTotal += amt;
                                    else if (cat.contains("Học tập")) studyTotal += amt;
                                    else if (cat.contains("Giải trí")) entertainmentTotal += amt;
                                    else if (cat.contains("Di chuyển")) transportTotal += amt;
                                    else otherTotal += amt;
                                } else {
                                    otherTotal += amt;
                                }
                            }
                        }
                    }

                    updateUI(totalIncome, totalExpense, foodTotal, studyTotal, entertainmentTotal, transportTotal, otherTotal);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(double income, double expense, double food, double study, double entertainment, double transport, double other) {
        double balance = income - expense;
        
        tvStatsBalance.setText(formatter.format(balance) + " ₫");
        tvStatsIncome.setText(formatter.format(income) + " ₫");
        tvStatsExpense.setText(formatter.format(expense) + " ₫");

        double totalSum = income + expense;
        int incomePercent = 50;
        if (totalSum > 0) {
            incomePercent = (int) ((income / totalSum) * 100);
        }
        
        tvRatioIncomePercent.setText("Thu nhập: " + incomePercent + "%");
        tvRatioExpensePercent.setText("Chi tiêu: " + (100 - incomePercent) + "%");
        pbRatio.setProgress(incomePercent);

        tvCatFoodAmount.setText(formatter.format(food) + " ₫");
        tvCatStudyAmount.setText(formatter.format(study) + " ₫");
        tvCatEntertainmentAmount.setText(formatter.format(entertainment) + " ₫");
        tvCatTransportAmount.setText(formatter.format(transport) + " ₫");
        tvCatOtherAmount.setText(formatter.format(other) + " ₫");

        if (expense > 0) {
            pbCatFood.setProgress((int) ((food / expense) * 100));
            pbCatStudy.setProgress((int) ((study / expense) * 100));
            pbCatEntertainment.setProgress((int) ((entertainment / expense) * 100));
            pbCatTransport.setProgress((int) ((transport / expense) * 100));
            pbCatOther.setProgress((int) ((other / expense) * 100));
        }
    }
}
