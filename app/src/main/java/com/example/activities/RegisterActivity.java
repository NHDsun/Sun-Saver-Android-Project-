package com.example.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.database.DatabaseHelper;

/**
 * Màn hình Đăng ký tài khoản mới (RegisterActivity).
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtUsername, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvBtnGotoLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ánh xạ layout xml
        int layoutId = getResources().getIdentifier("activity_register", "layout", getPackageName());
        setTheme(getResources().getIdentifier("Theme.MyApplication", "style", getPackageName()));
        setContentView(layoutId);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ view
        String pkg = getPackageName();
        edtFullName = findViewById(getResources().getIdentifier("edt_fullname_reg", "id", pkg));
        edtUsername = findViewById(getResources().getIdentifier("edt_username_reg", "id", pkg));
        edtPassword = findViewById(getResources().getIdentifier("edt_password_reg", "id", pkg));
        edtConfirmPassword = findViewById(getResources().getIdentifier("edt_confirm_password_reg", "id", pkg));
        btnRegister = findViewById(getResources().getIdentifier("btn_register", "id", pkg));
        tvBtnGotoLogin = findViewById(getResources().getIdentifier("tv_btn_goto_login", "id", pkg));

        // Nút bấm đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        // Đi tới trang đăng nhập (bằng cách đóng activity hiện tại)
        tvBtnGotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        String fullname = edtFullName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra đầu vào hợp lệ
        if (TextUtils.isEmpty(fullname)) {
            edtFullName.setError("Họ và tên không được để trống!");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Tên đăng nhập không được để trống!");
            edtUsername.requestFocus();
            return;
        }

        if (username.contains(" ")) {
            edtUsername.setError("Tên đăng nhập viết liền, không chứa khoảng trắng!");
            edtUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Mật khẩu không được để trống!");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải dài tối thiểu 6 ký tự!");
            edtPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng nhập lại mật khẩu!");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không trùng khớp cũ!");
            edtConfirmPassword.requestFocus();
            return;
        }

        // 2. Kiểm tra xem tài khoản đã tồn tại hay chưa
        boolean userExists = dbHelper.checkUserExists(username);
        if (userExists) {
            Toast.makeText(this, "Tên đăng nhập này đã có người sử dụng! ☀️", Toast.LENGTH_LONG).show();
            edtUsername.setError("Tên đăng nhập đã tồn tại!");
            edtUsername.requestFocus();
            return;
        }

        // 3. Tiến hành lưu tài khoản vào cơ sở dữ liệu
        boolean success = dbHelper.registerUser(username, password, fullname);
        if (success) {
            Toast.makeText(this, "Chúc mừng! Bạn đã đăng ký tài khoản thành công! ☀️", Toast.LENGTH_LONG).show();
            finish(); // Hoàn thành đăng ký, tự động trả về màn hình Login trước đó
        } else {
            Toast.makeText(this, "Đăng ký thất bại, đã xảy ra lỗi không xác định!", Toast.LENGTH_SHORT).show();
        }
    }
}
