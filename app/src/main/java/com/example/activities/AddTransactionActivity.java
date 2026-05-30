package com.example.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.database.FirestoreHelper;
import com.example.models.Transaction;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Màn hình Thêm Giao dịch mới kiêm Chỉnh sửa Giao dịch hiện tại.
 * Đã chuyển sang sử dụng Firebase Firestore để đồng bộ đám mây.
 */
public class AddTransactionActivity extends AppCompatActivity {

    private ImageButton btnBack, btnDeleteTop;
    private ImageButton btnVoiceTitle;
    private static final int REQUEST_CODE_SPEECH = 1002;
    private TextView tvTitlePage;
    private RadioGroup rgType;
    private RadioButton rbIncome, rbExpense;
    private EditText edtTitle, edtAmount, edtDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnDeleteForm;

    private TextView tplFood, tplCoffee, tplGas, tplShopping, tplSalary;
    private TextView btnAmtClear, btnAmt20k, btnAmt50k, btnAmt100k, btnAmt200k, btnAmt500k, btnAmt1M;
    private TextView btnDateToday, btnDateYesterday, btnDate2Days, btnDate3Days, btnDatePickerLabel;

    private FirestoreHelper firestoreHelper;
    private Transaction existingTransaction = null;
    private boolean isEditMode = false;

    private final String[] expenseCategories = {
        "Ăn uống 🍔", "Mua sắm 🛍️", "Học tập 📚", "Di chuyển 🚗", 
        "Giải trí 🎮", "Điện nước & Gas ⚡", "Sức khỏe 💊", 
        "Nhà cửa & Thuê phòng 🏠", "Đầu tư / Tiết kiệm 💰", "Du lịch & Chơi xa ✈️", "Khác ⚙️"
    };

    private final String[] incomeCategories = {
        "Lương tháng 💵", "Thưởng / Quà cáp 🎁", "Kinh doanh tự do 📈", 
        "Tiền trợ cấp 🍼", "Lãi đầu tư sinh lời 📊", "Thanh lý đồ cũ 📦", "Khác ☀️"
    };

    private String[] currentCategories = expenseCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.R.layout.activity_add_transaction);

        firestoreHelper = new FirestoreHelper();

        initViews();
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == com.example.R.id.rb_income) {
                currentCategories = incomeCategories;
            } else {
                currentCategories = expenseCategories;
            }
            setupCategorySpinner();
        });

        setupCategorySpinner();
        setupDatePicker();
        checkIntentAndPopulateData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(com.example.R.id.btn_back);
        btnDeleteTop = findViewById(com.example.R.id.btn_delete_top);
        tvTitlePage = findViewById(com.example.R.id.tv_title_page);
        rgType = findViewById(com.example.R.id.rg_type);
        rbIncome = findViewById(com.example.R.id.rb_income);
        rbExpense = findViewById(com.example.R.id.rb_expense);
        edtTitle = findViewById(com.example.R.id.edt_title);
        edtAmount = findViewById(com.example.R.id.edt_amount);
        edtDate = findViewById(com.example.R.id.edt_date);
        btnVoiceTitle = findViewById(com.example.R.id.btn_voice_title);
        spinnerCategory = findViewById(com.example.R.id.spinner_category);
        btnSave = findViewById(com.example.R.id.btn_save);
        btnDeleteForm = findViewById(com.example.R.id.btn_delete_form);

        tplFood = findViewById(com.example.R.id.tpl_food);
        tplCoffee = findViewById(com.example.R.id.tpl_coffee);
        tplGas = findViewById(com.example.R.id.tpl_gas);
        tplShopping = findViewById(com.example.R.id.tpl_shopping);
        tplSalary = findViewById(com.example.R.id.tpl_salary);

        btnAmtClear = findViewById(com.example.R.id.btn_amt_clear);
        btnAmt20k = findViewById(com.example.R.id.btn_amt_20k);
        btnAmt50k = findViewById(com.example.R.id.btn_amt_50k);
        btnAmt100k = findViewById(com.example.R.id.btn_amt_100k);
        btnAmt200k = findViewById(com.example.R.id.btn_amt_200k);
        btnAmt500k = findViewById(com.example.R.id.btn_amt_500k);
        btnAmt1M = findViewById(com.example.R.id.btn_amt_1m);

        btnDateToday = findViewById(com.example.R.id.btn_date_today);
        btnDateYesterday = findViewById(com.example.R.id.btn_date_yesterday);
        btnDate2Days = findViewById(com.example.R.id.btn_date_2days);
        btnDate3Days = findViewById(com.example.R.id.btn_date_3days);
        btnDatePickerLabel = findViewById(com.example.R.id.btn_date_picker);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currentCategories);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setupDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        edtDate.setText(currentDate);
        updateDateChipsSelection(0);

        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddTransactionActivity.this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String dayStr = String.format(Locale.getDefault(), "%02d", selectedDayOfMonth);
                        String monthStr = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                        String formattedSelectedDate = dayStr + "/" + monthStr + "/" + selectedYear;
                        edtDate.setText(formattedSelectedDate);
                        updateDateChipsSelection(-1);
                    }, year, month, day);

            datePickerDialog.show();
        });
    }

    private void checkIntentAndPopulateData() {
        existingTransaction = (Transaction) getIntent().getSerializableExtra("TRANSACTION_DATA");
        
        if (existingTransaction != null) {
            isEditMode = true;
            tvTitlePage.setText("Sửa giao dịch ✏️");
            btnSave.setText("CẬP NHẬT GIAO DỊCH ☀️");
            btnDeleteTop.setVisibility(View.VISIBLE);
            btnDeleteForm.setVisibility(View.VISIBLE);

            edtTitle.setText(existingTransaction.getTitle());
            edtAmount.setText(String.valueOf((int) existingTransaction.getAmount()));
            edtDate.setText(existingTransaction.getDate());

            if ("Income".equalsIgnoreCase(existingTransaction.getType())) {
                rbIncome.setChecked(true);
                currentCategories = incomeCategories;
            } else {
                rbExpense.setChecked(true);
                currentCategories = expenseCategories;
            }
            setupCategorySpinner();

            String category = existingTransaction.getCategory();
            for (int i = 0; i < currentCategories.length; i++) {
                if (currentCategories[i].equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        } else {
            isEditMode = false;
            tvTitlePage.setText("Thêm giao dịch ＋");
            btnDeleteTop.setVisibility(View.GONE);
            btnDeleteForm.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());

        if (btnAmtClear != null) btnAmtClear.setOnClickListener(v -> edtAmount.setText(""));
        if (btnAmt20k != null) btnAmt20k.setOnClickListener(v -> appendAmountValue(20000));
        if (btnAmt50k != null) btnAmt50k.setOnClickListener(v -> appendAmountValue(50000));
        if (btnAmt100k != null) btnAmt100k.setOnClickListener(v -> appendAmountValue(100000));
        if (btnAmt200k != null) btnAmt200k.setOnClickListener(v -> appendAmountValue(200000));
        if (btnAmt500k != null) btnAmt500k.setOnClickListener(v -> appendAmountValue(500000));
        if (btnAmt1M != null) btnAmt1M.setOnClickListener(v -> appendAmountValue(1000000));

        if (btnDateToday != null) btnDateToday.setOnClickListener(v -> setPastDateOffset(0));
        if (btnDateYesterday != null) btnDateYesterday.setOnClickListener(v -> setPastDateOffset(1));
        if (btnDate2Days != null) btnDate2Days.setOnClickListener(v -> setPastDateOffset(2));
        if (btnDate3Days != null) btnDate3Days.setOnClickListener(v -> setPastDateOffset(3));
        if (btnDatePickerLabel != null) btnDatePickerLabel.setOnClickListener(v -> edtDate.performClick());

        if (btnVoiceTitle != null) {
            btnVoiceTitle.setOnClickListener(v -> {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói mô tả...");
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(this, "Thiết bị không hỗ trợ giọng nói!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View.OnClickListener deleteClickListener = v -> showDeleteConfirmDialog();
        btnDeleteTop.setOnClickListener(deleteClickListener);
        btnDeleteForm.setOnClickListener(deleteClickListener);
    }

    private void appendAmountValue(int val) {
        String currentText = edtAmount.getText().toString().trim();
        int currentVal = 0;
        if (!currentText.isEmpty()) {
            try { currentVal = Integer.parseInt(currentText); } catch (NumberFormatException ignored) {}
        }
        edtAmount.setText(String.valueOf(currentVal + val));
    }

    private void saveTransaction() {
        String title = edtTitle.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String dateStr = edtDate.getText().toString().trim();

        if (title.isEmpty()) { edtTitle.setError("Vui lòng nhập tên!"); return; }
        if (amountStr.isEmpty()) { edtAmount.setError("Vui lòng nhập tiền!"); return; }

        double amount = Double.parseDouble(amountStr);
        String type = rbIncome.isChecked() ? "Income" : "Expense";
        String category = spinnerCategory.getSelectedItem().toString();

        android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", MODE_PRIVATE);
        String loggedInUser = sharedPrefs.getString("LOGGED_IN_USER", "guest");

        if (isEditMode && existingTransaction != null) {
            existingTransaction.setTitle(title);
            existingTransaction.setAmount(amount);
            existingTransaction.setType(type);
            existingTransaction.setCategory(category);
            existingTransaction.setDate(dateStr);

            firestoreHelper.updateTransaction(existingTransaction)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã cập nhật Firestore! ☀️", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
        } else {
            Transaction newTrans = new Transaction(title, amount, type, category, dateStr, loggedInUser);
            firestoreHelper.addTransaction(newTrans)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đã lưu lên Firestore! ☀️", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
        }
    }

    private void setPastDateOffset(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        if (offsetDays > 0) cal.add(Calendar.DAY_YEAR, -offsetDays);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDate.setText(sdf.format(cal.getTime()));
        updateDateChipsSelection(offsetDays);
    }

    private void updateDateChipsSelection(int offsetDays) {
        if (btnDateToday == null) return;
        int activeBg = com.example.R.drawable.bg_filter_chip_active;
        int inactiveBg = com.example.R.drawable.bg_filter_chip_inactive;
        btnDateToday.setBackgroundResource(offsetDays == 0 ? activeBg : inactiveBg);
        btnDateYesterday.setBackgroundResource(offsetDays == 1 ? activeBg : inactiveBg);
        btnDate2Days.setBackgroundResource(offsetDays == 2 ? activeBg : inactiveBg);
        btnDate3Days.setBackgroundResource(offsetDays == 3 ? activeBg : inactiveBg);
        btnDatePickerLabel.setBackgroundResource(offsetDays == -1 ? activeBg : inactiveBg);
    }

    private void showDeleteConfirmDialog() {
        if (existingTransaction == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá ⚠️")
                .setMessage("Xoá vĩnh viễn khỏi Firestore?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    firestoreHelper.deleteTransaction(existingTransaction.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xoá thành công!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                edtTitle.setText(result.get(0));
            }
        }
    }
}
