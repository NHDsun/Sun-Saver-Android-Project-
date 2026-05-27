package com.example.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
 * Màn hình Đăng nhập (LoginActivity).
 * Hỗ trợ xác thực các tài khoản được lưu trữ cục bộ trong SQLite DatabaseHelper.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView tvBtnGotoRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ánh xạ layout xml
        int layoutId = getResources().getIdentifier("activity_login", "layout", getPackageName());
        setContentView(layoutId);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ view
        String pkg = getPackageName();
        edtUsername = findViewById(getResources().getIdentifier("edt_username", "id", pkg));
        edtPassword = findViewById(getResources().getIdentifier("edt_password", "id", pkg));
        btnLogin = findViewById(getResources().getIdentifier("btn_login", "id", pkg));
        tvBtnGotoRegister = findViewById(getResources().getIdentifier("tv_btn_goto_register", "id", pkg));

        // Nút bấm đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Đi tới trang đăng ký
        tvBtnGotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Kiểm tra hợp lệ các trường đầu vào
        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Tên đăng nhập không được để trống!");
            edtUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Mật khẩu không được để trống!");
            edtPassword.requestFocus();
            return;
        }

        // Xác thực đăng nhập qua database
        boolean authenticate = dbHelper.checkLogin(username, password);
        if (authenticate) {
            String fullName = dbHelper.getUserFullName(username);
            
            // Lưu trạng thái đăng nhập vào SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("SunSaverPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("IS_LOGGED_IN", true);
            editor.putString("LOGGED_IN_USER", username);
            editor.putString("USER_FULL_NAME", fullName);
            editor.apply();

            Toast.makeText(this, "Chào mừng " + fullName + " quay lại với Sun Saver! ☀️", Toast.LENGTH_SHORT).show();

            // Đi tới trang chủ chính
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu chưa chính xác! ☀️", Toast.LENGTH_LONG).show();
        }
    }
}
