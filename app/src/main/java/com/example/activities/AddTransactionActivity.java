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
    private TextView tvTitlePage;
    private RadioGroup rgType;
    private RadioButton rbIncome, rbExpense;
    private EditText edtTitle, edtAmount, edtDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnDeleteForm;

    private DatabaseHelper dbHelper;
    private Transaction existingTransaction = null;
    private boolean isEditMode = false;

    // Danh sách danh mục hiển thị trong Spinner
    private final String[] categories = {"Ăn uống", "Học tập", "Giải trí", "Di chuyển", "Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int layoutAdd = getResources().getIdentifier("activity_add_transaction", "layout", getPackageName());
        setContentView(layoutAdd);

        dbHelper = new DatabaseHelper(this);

        // 1. Ánh xạ toàn bộ view
        initViews();

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
        spinnerCategory = findViewById(getResources().getIdentifier("spinner_category", "id", pkg));
        btnSave = findViewById(getResources().getIdentifier("btn_save", "id", pkg));
        btnDeleteForm = findViewById(getResources().getIdentifier("btn_delete_form", "id", pkg));
    }

    private void setupCategorySpinner() {
        // Sử dụng ArrayAdapter mặc định của nền tảng hỗ trợ hiển thị danh sách dạng dropdown đơn giản
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setupDatePicker() {
        // Thiết lập giá trị ngày mặc định hiển thị là hôm nay
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        edtDate.setText(currentDate);

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
            } else {
                rbExpense.setChecked(true);
            }

            // Gán chỉ số của danh mục trong Spinner
            String category = existingTransaction.getCategory();
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(category)) {
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
                finish(); // Đóng màn hình trả về Home
            } else {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Chế độ THÊM MỚI giao dịch
            Transaction newTrans = new Transaction(title, amount, type, category, dateStr);
            long result = dbHelper.insertTransaction(newTrans);
            if (result > -1) {
                Toast.makeText(this, "Đã ghi chép giao dịch thành công! ☀️", Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình
            } else {
                Toast.makeText(this, "Không thể lưu giao dịch vào cơ sở dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        }
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
                finish(); // Đóng màn hình chỉnh sửa luôn
            }
        });
        builder.setNegativeButton("Không xoá", null);
        builder.show();
    }
}
