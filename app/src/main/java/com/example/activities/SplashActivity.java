package com.example.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Màn hình chào mừng (Splash Screen).
 * Đã cập nhật để kiểm tra trạng thái đăng nhập từ Firebase Auth.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sử dụng Handler để trì hoãn chuyển cảnh đi 2 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Kiểm tra trạng thái đăng nhập trực tiếp từ Firebase
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // Đã đăng nhập -> Vào thẳng màn hình chính
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Chưa đăng nhập -> Chuyển sang màn hình Đăng nhập
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
