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
import com.example.database.DatabaseHelper;
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
 * Áp dụng hiệu năng cao của SQLite, Dialog chọn ngày mặc định thông minh, Spinners thân thiện.
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

    // Các thành phần mẫu và công cụ tăng tốc số tiền
    private TextView tplFood, tplCoffee, tplGas, tplShopping, tplSalary;
    private TextView btnAmtClear, btnAmt20k, btnAmt50k, btnAmt100k, btnAmt200k, btnAmt500k, btnAmt1M;

    // Các phím chọn nhanh ngày giao dịch thực hiện
    private TextView btnDateToday, btnDateYesterday, btnDate2Days, btnDate3Days, btnDatePickerLabel;

    private DatabaseHelper dbHelper;
    private Transaction existingTransaction = null;
    private boolean isEditMode = false;

    // Danh mục tương ứng trực quan hơn được tách biệt cho Thu nhập & Chi tiêu
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
        
        int layoutAdd = getResources().getIdentifier("activity_add_transaction", "layout", getPackageName());
        setContentView(layoutAdd);

        dbHelper = new DatabaseHelper(this);

        // 1. Ánh xạ toàn bộ view
        initViews();

        // Đăng ký bộ lắng nghe chuyển đổi loại giao dịch để đổi danh sách danh mục tương ứng
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == rbIncome.getId()) {
                currentCategories = incomeCategories;
            } else {
                currentCategories = expenseCategories;
            }
            setupCategorySpinner();
        });

        // 2. Thiết lập dữ liệu Adapter cho Spinner Danh mục
        setupCategorySpinner();

        // 3. Thiết lập hoạt động cho Ô Date Picker chọn ngày
        setupDatePicker();

        // 4. Nhận biết chế độ: SỬA (Edit Mode) hay THÊM MỚI (Add Mode)
        checkIntentAndPopulateData();

        // 5. Cài đặt các trình click sự kiện
        setupClickListeners();
    }

    private void initViews() {
        String pkg = getPackageName();
        btnBack = findViewById(getResources().getIdentifier("btn_back", "id", pkg));
        btnDeleteTop = findViewById(getResources().getIdentifier("btn_delete_top", "id", pkg));
        tvTitlePage = findViewById(getResources().getIdentifier("tv_title_page", "id", pkg));
        rgType = findViewById(getResources().getIdentifier("rg_type", "id", pkg));
        rbIncome = findViewById(getResources().getIdentifier("rb_income", "id", pkg));
        rbExpense = findViewById(getResources().getIdentifier("rb_expense", "id", pkg));
        edtTitle = findViewById(getResources().getIdentifier("edt_title", "id", pkg));
        edtAmount = findViewById(getResources().getIdentifier("edt_amount", "id", pkg));
        edtDate = findViewById(getResources().getIdentifier("edt_date", "id", pkg));
        btnVoiceTitle = findViewById(getResources().getIdentifier("btn_voice_title", "id", pkg));
        spinnerCategory = findViewById(getResources().getIdentifier("spinner_category", "id", pkg));
        btnSave = findViewById(getResources().getIdentifier("btn_save", "id", pkg));
        btnDeleteForm = findViewById(getResources().getIdentifier("btn_delete_form", "id", pkg));

        // Ánh xạ nút Mẫu giao dịch nhanh gợi ý
        tplFood = findViewById(getResources().getIdentifier("tpl_food", "id", pkg));
        tplCoffee = findViewById(getResources().getIdentifier("tpl_coffee", "id", pkg));
        tplGas = findViewById(getResources().getIdentifier("tpl_gas", "id", pkg));
        tplShopping = findViewById(getResources().getIdentifier("tpl_shopping", "id", pkg));
        tplSalary = findViewById(getResources().getIdentifier("tpl_salary", "id", pkg));

        // Ánh xạ các nút đề xuất/xoá nhanh số lượng
        btnAmtClear = findViewById(getResources().getIdentifier("btn_amt_clear", "id", pkg));
        btnAmt20k = findViewById(getResources().getIdentifier("btn_amt_20k", "id", pkg));
        btnAmt50k = findViewById(getResources().getIdentifier("btn_amt_50k", "id", pkg));
        btnAmt100k = findViewById(getResources().getIdentifier("btn_amt_100k", "id", pkg));
        btnAmt200k = findViewById(getResources().getIdentifier("btn_amt_200k", "id", pkg));
        btnAmt500k = findViewById(getResources().getIdentifier("btn_amt_500k", "id", pkg));
        btnAmt1M = findViewById(getResources().getIdentifier("btn_amt_1m", "id", pkg));

        // Ánh xạ các phím chọn nhanh ngày giao dịch thực hiện
        btnDateToday = findViewById(getResources().getIdentifier("btn_date_today", "id", pkg));
        btnDateYesterday = findViewById(getResources().getIdentifier("btn_date_yesterday", "id", pkg));
        btnDate2Days = findViewById(getResources().getIdentifier("btn_date_2days", "id", pkg));
        btnDate3Days = findViewById(getResources().getIdentifier("btn_date_3days", "id", pkg));
        btnDatePickerLabel = findViewById(getResources().getIdentifier("btn_date_picker", "id", pkg));
    }

    private void setupCategorySpinner() {
        // Sử dụng ArrayAdapter mặc định của nền tảng hỗ trợ hiển thị danh sách dạng dropdown đơn giản
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currentCategories);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setupDatePicker() {
        // Thiết lập giá trị ngày mặc định hiển thị là hôm nay
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        edtDate.setText(currentDate);

        // Highlight Today by default
        updateDateChipsSelection(0);

        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị lịch chọn ngày mặc định của Android
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTransactionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // Định dạng chuyển về chuỗi ngày chuẩn dd/MM/yyyy thân thiện
                                String dayStr = String.format(Locale.getDefault(), "%02d", selectedDayOfMonth);
                                String monthStr = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                                String formattedSelectedDate = dayStr + "/" + monthStr + "/" + selectedYear;
                                edtDate.setText(formattedSelectedDate);
                                updateDateChipsSelection(-1);
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });
    }

    private void checkIntentAndPopulateData() {
        // Kiểm tra xem MainActivity có gửi kèm đối tượng cần chỉnh sửa sang không
        existingTransaction = (Transaction) getIntent().getSerializableExtra("TRANSACTION_DATA");
        
        if (existingTransaction != null) {
            isEditMode = true;
            tvTitlePage.setText("Sửa giao dịch ✏️");
            btnSave.setText("CẬP NHẬT GIAO DỊCH ☀️");
            
            // Hiện các nút xoá
            btnDeleteTop.setVisibility(View.VISIBLE);
            btnDeleteForm.setVisibility(View.VISIBLE);

            // Gán dữ liệu sang các trường
            edtTitle.setText(existingTransaction.getTitle());
            
            // Xoá phần phẩy hoặc định dạng số nguyên khi hiển thị đưa vào form để dễ edit
            edtAmount.setText(String.valueOf((int) existingTransaction.getAmount()));
            edtDate.setText(existingTransaction.getDate());

            // Gán loại Thu / Chi
            if ("Income".equalsIgnoreCase(existingTransaction.getType())) {
                rbIncome.setChecked(true);
                currentCategories = incomeCategories;
            } else {
                rbExpense.setChecked(true);
                currentCategories = expenseCategories;
            }
            setupCategorySpinner();

            // Gán chỉ số của danh mục trong Spinner
            String category = existingTransaction.getCategory();
            for (int i = 0; i < currentCategories.length; i++) {
                String pureCat = currentCategories[i].replaceAll("[\\p{So}\\p{Cn}]", "").trim();
                if (currentCategories[i].equalsIgnoreCase(category) || 
                    category.contains(pureCat) || 
                    pureCat.contains(category)) {
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
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nhấn nút lưu/cập nhật dữ liệu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });

        // Click các nút đề xuất tăng nhanh số tiền
        if (btnAmtClear != null) btnAmtClear.setOnClickListener(v -> edtAmount.setText(""));
        if (btnAmt20k != null) btnAmt20k.setOnClickListener(v -> appendAmountValue(20000));
        if (btnAmt50k != null) btnAmt50k.setOnClickListener(v -> appendAmountValue(50000));
        if (btnAmt100k != null) btnAmt100k.setOnClickListener(v -> appendAmountValue(100000));
        if (btnAmt200k != null) btnAmt200k.setOnClickListener(v -> appendAmountValue(200000));
        if (btnAmt500k != null) btnAmt500k.setOnClickListener(v -> appendAmountValue(500000));
        if (btnAmt1M != null) btnAmt1M.setOnClickListener(v -> appendAmountValue(1000000));

        // Click các nút chọn nhanh ngày thực hiện giao dịch (để nhập ngày trước dễ dàng)
        if (btnDateToday != null) {
            btnDateToday.setOnClickListener(v -> setPastDateOffset(0));
        }
        if (btnDateYesterday != null) {
            btnDateYesterday.setOnClickListener(v -> setPastDateOffset(1));
        }
        if (btnDate2Days != null) {
            btnDate2Days.setOnClickListener(v -> setPastDateOffset(2));
        }
        if (btnDate3Days != null) {
            btnDate3Days.setOnClickListener(v -> setPastDateOffset(3));
        }
        if (btnDatePickerLabel != null) {
            btnDatePickerLabel.setOnClickListener(v -> {
                // Giả lập click vào edtDate để hiển thị DatePickerDialog mặc định
                edtDate.performClick();
            });
        }

        // Nhấn nút nhập liệu bằng giọng nói (Google Voice Speech-to-Text)
        if (btnVoiceTitle != null) {
            btnVoiceTitle.setOnClickListener(v -> {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN");
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói mô tả (Ví dụ: Cơm trưa 40k Hôm qua)...");
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Thiết bị của bạn không hỗ trợ Nhận diện Giọng nói từ Google!", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Click chọn mẫu điền nhanh gợi ý
        if (tplFood != null) {
            tplFood.setOnClickListener(v -> {
                rbExpense.setChecked(true);
                currentCategories = expenseCategories;
                setupCategorySpinner();
                edtTitle.setText("Ăn trưa cơm văn phòng 🍔");
                edtAmount.setText("40000");
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Ăn uống")) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            });
        }
        if (tplCoffee != null) {
            tplCoffee.setOnClickListener(v -> {
                rbExpense.setChecked(true);
                currentCategories = expenseCategories;
                setupCategorySpinner();
                edtTitle.setText("Cà phê giải khát ☕");
                edtAmount.setText("30000");
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Giải trí") || currentCategories[i].contains("Ăn uống")) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            });
        }
        if (tplGas != null) {
            tplGas.setOnClickListener(v -> {
                rbExpense.setChecked(true);
                currentCategories = expenseCategories;
                setupCategorySpinner();
                edtTitle.setText("Đổ xăng xe máy ⛽");
                edtAmount.setText("50000");
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Di chuyển")) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            });
        }
        if (tplShopping != null) {
            tplShopping.setOnClickListener(v -> {
                rbExpense.setChecked(true);
                currentCategories = expenseCategories;
                setupCategorySpinner();
                edtTitle.setText("Mua sắm nhu yếu phẩm 🛍️");
                edtAmount.setText("200000");
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Mua sắm")) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            });
        }
        if (tplSalary != null) {
            tplSalary.setOnClickListener(v -> {
                rbIncome.setChecked(true);
                currentCategories = incomeCategories;
                setupCategorySpinner();
                edtTitle.setText("Nhận lương / Thưởng định kỳ 💵");
                edtAmount.setText("3000000");
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Lương")) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            });
        }

        // Nhấn nút xoá trên góc Toolbar hoặc cuối Form
        View.OnClickListener deleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmDialog();
            }
        };
        btnDeleteTop.setOnClickListener(deleteClickListener);
        btnDeleteForm.setOnClickListener(deleteClickListener);
    }

    private void appendAmountValue(int val) {
        String currentText = edtAmount.getText().toString().trim();
        int currentVal = 0;
        if (!currentText.isEmpty()) {
            try {
                currentVal = Integer.parseInt(currentText);
            } catch (NumberFormatException ignored) {}
        }
        edtAmount.setText(String.valueOf(currentVal + val));
    }

    /**
     * Hàm kiểm tra tính hợp lệ của dữ liệu đầu vào và Lưu/Cập nhật vào SQLite
     */
    private void saveTransaction() {
        String title = edtTitle.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String dateStr = edtDate.getText().toString().trim();

        // 1. Kiểm tra tiêu đề rỗng
        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tên giao dịch!");
            edtTitle.requestFocus();
            return;
        }

        // 2. Kiểm tra số tiền rỗng
        if (amountStr.isEmpty()) {
            edtAmount.setError("Vui lòng nhập số tiền!");
            edtAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            edtAmount.setError("Số tiền không hợp lệ!");
            edtAmount.requestFocus();
            return;
        }

        // 3. Số tiền phải lớn hơn 0
        if (amount <= 0) {
            edtAmount.setError("Số tiền nhập vào phải lớn hơn 0 ₫!");
            edtAmount.requestFocus();
            return;
        }

        // Xác định loại giao dịch
        String type = rbIncome.isChecked() ? "Income" : "Expense";
        
        // Xác định danh mục được chọn
        String category = spinnerCategory.getSelectedItem().toString();

        if (isEditMode && existingTransaction != null) {
            // Chế độ CẬP NHẬT giao dịch có sẵn
            existingTransaction.setTitle(title);
            existingTransaction.setAmount(amount);
            existingTransaction.setType(type);
            existingTransaction.setCategory(category);
            existingTransaction.setDate(dateStr);

            int result = dbHelper.updateTransaction(existingTransaction);
            if (result > 0) {
                Toast.makeText(this, "Đã cập nhật giao dịch thành công! ☀️", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish(); // Đóng màn hình trả về Home
            } else {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Chế độ THÊM MỚI giao dịch
            android.content.SharedPreferences sharedPrefs = getSharedPreferences("SunSaverPrefs", android.content.Context.MODE_PRIVATE);
            String loggedInUser = sharedPrefs.getString("LOGGED_IN_USER", "guest");

            Transaction newTrans = new Transaction(title, amount, type, category, dateStr);
            newTrans.setUserRef(loggedInUser);

            long result = dbHelper.insertTransaction(newTrans);
            if (result > -1) {
                Toast.makeText(this, "Đã ghi chép giao dịch thành công! ☀️", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish(); // Đóng màn hình
            } else {
                Toast.makeText(this, "Không thể lưu giao dịch vào cơ sở dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setPastDateOffset(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        if (offsetDays > 0) {
            cal.add(Calendar.DAY_OF_YEAR, -offsetDays);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDate.setText(sdf.format(cal.getTime()));
        
        // Cập nhật đổi màu trạng thái để người dùng biết đã chọn nút nào
        updateDateChipsSelection(offsetDays);
    }

    private void updateDateChipsSelection(int offsetDays) {
        if (btnDateToday == null || btnDateYesterday == null || btnDate2Days == null || btnDate3Days == null || btnDatePickerLabel == null) return;
        
        int activeBg = getResources().getIdentifier("bg_filter_chip_active", "drawable", getPackageName());
        int inactiveBg = getResources().getIdentifier("bg_filter_chip_inactive", "drawable", getPackageName());
        int activeTextColor = getResources().getColor(getResources().getIdentifier("white", "color", getPackageName()));
        int inactiveTextColor = getResources().getColor(getResources().getIdentifier("text_primary_light", "color", getPackageName()));
        int secondaryTextColor = getResources().getColor(getResources().getIdentifier("text_secondary_light", "color", getPackageName()));

        btnDateToday.setBackgroundResource(offsetDays == 0 ? activeBg : inactiveBg);
        btnDateToday.setTextColor(offsetDays == 0 ? activeTextColor : inactiveTextColor);

        btnDateYesterday.setBackgroundResource(offsetDays == 1 ? activeBg : inactiveBg);
        btnDateYesterday.setTextColor(offsetDays == 1 ? activeTextColor : inactiveTextColor);

        btnDate2Days.setBackgroundResource(offsetDays == 2 ? activeBg : inactiveBg);
        btnDate2Days.setTextColor(offsetDays == 2 ? activeTextColor : inactiveTextColor);

        btnDate3Days.setBackgroundResource(offsetDays == 3 ? activeBg : inactiveBg);
        btnDate3Days.setTextColor(offsetDays == 3 ? activeTextColor : inactiveTextColor);

        btnDatePickerLabel.setBackgroundResource(offsetDays == -1 ? activeBg : inactiveBg);
        btnDatePickerLabel.setTextColor(offsetDays == -1 ? activeTextColor : secondaryTextColor);
    }

    /**
     * Xác nhận xoá giao dịch giống như ở màn hình chính
     */
    private void showDeleteConfirmDialog() {
        if (existingTransaction == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xoá ⚠️");
        builder.setMessage("Bạn có chắc chắn muốn xoá vĩnh viễn giao dịch này khỏi hệ thống không?");
        builder.setPositiveButton("Xoá ngay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteTransaction(existingTransaction.getId());
                Toast.makeText(AddTransactionActivity.this, "Đã xoá thành công giao dịch!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish(); // Đóng màn hình chỉnh sửa luôn
            }
        });
        builder.setNegativeButton("Không xoá", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    edtTitle.setText(spokenText);
                    parseSmartVoiceInput(spokenText);
                }
            }
        }
    }

    private void parseSmartVoiceInput(String spokenText) {
        if (spokenText == null || spokenText.trim().isEmpty()) return;

        String lower = spokenText.toLowerCase(Locale.getDefault());
        
        // 1. Phân tích Loại giao dịch (Thu nhập hay Chi tiêu)
        boolean isIncome = false;
        if (lower.contains("lương") || lower.contains("nhận tiền") || lower.contains("được tặng") || 
            lower.contains("thu nhập") || lower.contains("bán hàng") || lower.contains("hoa hồng") || 
            lower.contains("tiền trà nước") || lower.contains("tiền thưởng") || lower.contains("thưởng") || 
            lower.contains("trúng thưởng") || lower.contains("lài") || lower.contains("lãi") || lower.contains("bán đồ")) {
            isIncome = true;
        }

        if (isIncome) {
            rbIncome.setChecked(true);
            currentCategories = incomeCategories;
        } else {
            rbExpense.setChecked(true);
            currentCategories = expenseCategories;
        }
        setupCategorySpinner();

        // 2. Phân tích Số tiền tương ứng
        long parsedAmount = 0;
        
        // Trích xuất số bằng chữ hoặc số bằng số
        // Lọc các số có chữ "k" đi kèm, ví dụ "50k", "100k"
        java.util.regex.Matcher kMatcher = java.util.regex.Pattern.compile("(\\d+)\\s*k").matcher(lower);
        if (kMatcher.find()) {
            try {
                parsedAmount = Long.parseLong(kMatcher.group(1)) * 1000;
            } catch (Exception ignored) {}
        }
        
        // Tìm số bằng số thuần tuý đi sau bởi nghìn hoặc ngàn hoặc triệu
        if (parsedAmount == 0) {
            java.util.regex.Matcher numWordMatcher = java.util.regex.Pattern.compile("(\\d+)\\s*(triệu|tr|nghìn|ngàn|đồng|đ|vnd)").matcher(lower);
            if (numWordMatcher.find()) {
                try {
                    long base = Long.parseLong(numWordMatcher.group(1));
                    String unit = numWordMatcher.group(2);
                    if (unit.contains("triệu") || unit.equals("tr")) {
                        parsedAmount = base * 1000000;
                    } else if (unit.contains("nghìn") || unit.contains("ngàn")) {
                        parsedAmount = base * 1000;
                    } else {
                        parsedAmount = base;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Tìm bằng chữ hoàn toàn tiếng Việt quen thuộc
        if (parsedAmount == 0) {
            if (lower.contains("hai mươi nghìn") || lower.contains("hai chục nghìn") || lower.contains("hai mươi ngàn") || lower.contains("hai chục ngàn") || lower.contains("hai chục")) {
                parsedAmount = 20000;
            } else if (lower.contains("năm mươi nghìn") || lower.contains("năm chục nghìn") || lower.contains("năm mươi ngàn") || lower.contains("năm chục ngàn") || lower.contains("năm chục") || lower.contains("năm xị")) {
                parsedAmount = 50000;
            } else if (lower.contains("mười nghìn") || lower.contains("mười ngàn") || lower.contains("một chục") || lower.contains("mười ngàn")) {
                parsedAmount = 10000;
            } else if (lower.contains("ba mươi nghìn") || lower.contains("ba chục nghìn") || lower.contains("ba mươi ngàn") || lower.contains("ba chục ngàn") || lower.contains("ba chục")) {
                parsedAmount = 30000;
            } else if (lower.contains("bốn mươi nghìn") || lower.contains("bốn chục nghìn") || lower.contains("bốn mươi ngàn") || lower.contains("bốn chục ngàn") || lower.contains("bốn chục")) {
                parsedAmount = 40000;
            } else if (lower.contains("một trăm nghìn") || lower.contains("một trăm ngàn") || lower.contains("một xị")) {
                parsedAmount = 100000;
            } else if (lower.contains("hai trăm nghìn") || lower.contains("hai trăm ngàn") || lower.contains("hai xị")) {
                parsedAmount = 200000;
            } else if (lower.contains("ba trăm nghìn") || lower.contains("ba trăm ngàn") || lower.contains("ba xị")) {
                parsedAmount = 300000;
            } else if (lower.contains("bốn trăm nghìn") || lower.contains("bốn trăm ngàn") || lower.contains("bốn xị")) {
                parsedAmount = 400000;
            } else if (lower.contains("năm trăm nghìn") || lower.contains("năm trăm ngàn") || lower.contains("nửa triệu")) {
                parsedAmount = 500000;
            } else if (lower.contains("hai trăm rưỡi") || lower.contains("hai trăm năm mươi ngàn") || lower.contains("hai trăm năm mươi nghìn")) {
                parsedAmount = 250000;
            } else if (lower.contains("trăm rưỡi") || lower.contains("một trăm năm mươi ngàn") || lower.contains("một trăm năm mươi nghìn")) {
                parsedAmount = 150000;
            } else if (lower.contains("một triệu")) {
                parsedAmount = 1000000;
            } else if (lower.contains("hai triệu")) {
                parsedAmount = 2000000;
            } else if (lower.contains("ba triệu")) {
                parsedAmount = 3000000;
            } else if (lower.contains("năm triệu")) {
                parsedAmount = 5000000;
            } else if (lower.contains("mười triệu")) {
                parsedAmount = 10000000;
            }
        }

        // Tìm số bất kỳ trong văn bản để dán vào số tiền nếu chưa có phân tích thành công
        if (parsedAmount == 0) {
            java.util.regex.Matcher plainNumMatcher = java.util.regex.Pattern.compile("\\d+").matcher(lower.replaceAll("[.,]", ""));
            if (plainNumMatcher.find()) {
                try {
                    parsedAmount = Long.parseLong(plainNumMatcher.group());
                } catch (Exception ignored) {}
            }
        }

        if (parsedAmount > 0) {
            edtAmount.setText(String.valueOf(parsedAmount));
        }

        // 3. Phân loại Danh mục phù hợp tự động dựa theo từ khoá
        int selectedIndex = -1;
        if (isIncome) {
            if (lower.contains("lương") || lower.contains("thu nhập")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Lương")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("quà") || lower.contains("biếu") || lower.contains("tặng")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Thưởng")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("kinh doanh") || lower.contains("bán hàng") || lower.contains("hoa hồng")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Kinh doanh")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("cho") || lower.contains("bố mẹ") || lower.contains("trợ cấp")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("trợ cấp")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("lãi") || lower.contains("đầu tư") || lower.contains("gửi tiết kiệm")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Đầu tư")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("bán đồ") || lower.contains("thanh lý") || lower.contains("ve chai")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Thanh lý")) { selectedIndex = i; break; }
                }
            }
        } else {
            if (lower.contains("ăn") || lower.contains("uống") || lower.contains("phở") || lower.contains("bún") || lower.contains("cơm") || lower.contains("bánh") || lower.contains("trưa") || lower.contains("tối") || lower.contains("sáng") || lower.contains("cf") || lower.contains("cà phê") || lower.contains("trà sữa") || lower.contains("nhậu")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Ăn uống")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("mua") || lower.contains("áo") || lower.contains("quần") || lower.contains("giày") || lower.contains("dép") || lower.contains("shop") || lower.contains("siêu thị") || lower.contains("bách hóa")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Mua sắm")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("học") || lower.contains("khóa học") || lower.contains("sách") || lower.contains("vở") || lower.contains("bút") || lower.contains("trường") || lower.contains("học phí")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Học tập")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("xăng") || lower.contains("xe") || lower.contains("taxi") || lower.contains("grab") || lower.contains("vé xe") || lower.contains("gửi xe") || lower.contains("đổ xăng")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Di chuyển")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("chơi") || lower.contains("game") || lower.contains("phim") || lower.contains("netflix") || lower.contains("hát") || lower.contains("karaoke") || lower.contains("giải trí")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Giải trí")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("điện") || lower.contains("nước") || lower.contains("gas") || lower.contains("internet") || lower.contains("wifi") || lower.contains("cáp")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Điện nước")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("thuốc") || lower.contains("bệnh") || lower.contains("khám") || lower.contains("vắc xin") || lower.contains("sức khỏe") || lower.contains("bác sĩ")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Sức khỏe")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("nhà") || lower.contains("phòng") || lower.contains("trọ") || lower.contains("thuê nhà")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Nhà cửa")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("tiết kiệm") || lower.contains("đầu tư") || lower.contains("cổ phiếu") || lower.contains("vàng")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Đầu tư")) { selectedIndex = i; break; }
                }
            } else if (lower.contains("du lịch") || lower.contains("máy bay") || lower.contains("khách sạn") || lower.contains("phượt")) {
                for (int i = 0; i < currentCategories.length; i++) {
                    if (currentCategories[i].contains("Du lịch")) { selectedIndex = i; break; }
                }
            }
        }

        if (selectedIndex >= 0) {
            spinnerCategory.setSelection(selectedIndex);
        }

        // 4. Phân tích Ngày giao dịch (ví dụ: hôm qua, 2 ngày trước, v.v.)
        if (lower.contains("hôm qua")) {
            setPastDateOffset(1);
        } else if (lower.contains("2 ngày trước") || lower.contains("hai ngày trước")) {
            setPastDateOffset(2);
        } else if (lower.contains("3 ngày trước") || lower.contains("ba ngày trước")) {
            setPastDateOffset(3);
        } else if (lower.contains("hôm nay")) {
            setPastDateOffset(0);
        }

        // Thông báo cho người dùng
        String feedback = "🎙️ Đã nhận diện: \"" + spokenText + "\"";
        if (parsedAmount > 0) {
            feedback += "\n💰 Đã trích xuất số tiền: " + String.format(Locale.getDefault(), "%,d ₫", parsedAmount);
        }
        Toast.makeText(this, feedback, Toast.LENGTH_LONG).show();
    }
}
