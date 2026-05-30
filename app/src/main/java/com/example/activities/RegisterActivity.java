package com.example.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Màn hình Đăng ký tài khoản mới sử dụng Firebase Authentication.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvBtnGotoLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        initViews();

        btnRegister.setOnClickListener(v -> performRegistration());
        tvBtnGotoLogin.setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edt_fullname_reg);
        edtEmail = findViewById(R.id.edt_username_reg); // Trong layout là username, ta dùng làm email
        edtPassword = findViewById(R.id.edt_password_reg);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password_reg);
        btnRegister = findViewById(R.id.btn_register);
        tvBtnGotoLogin = findViewById(R.id.tv_btn_goto_login);
    }

    private void performRegistration() {
        String fullname = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullname)) {
            edtFullName.setError("Họ và tên không được để trống!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email không được để trống!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Mật khẩu không được để trống!");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải từ 6 ký tự!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không trùng khớp!");
            return;
        }

        // Đăng ký bằng Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật Profile Name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullname)
                                    .build();
                            user.updateProfile(profileUpdates);
                        }
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công! ☀️", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
