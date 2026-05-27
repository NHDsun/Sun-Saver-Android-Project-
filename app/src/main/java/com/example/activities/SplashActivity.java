package com.example.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình chào mừng (Splash Screen).
 * Hiển thị logo Sun Saver trong 2 giây rồi tự động chuyển sang Màn hình chính (Home).
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng theme splash screen đặc biệt từ resources
        int themeSplash = getResources().getIdentifier("Theme.MyApplication.Splash", "style", getPackageName());
        setTheme(themeSplash);
        
        super.onCreate(savedInstanceState);
        
        // Gán layout màn hình splash
        int layoutSplash = getResources().getIdentifier("activity_splash", "layout", getPackageName());
        setContentView(layoutSplash);

        // Sử dụng Handler để trì hoãn chuyển cảnh đi 2000 mili giây (2 giây)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Kiểm tra xem người dùng đã đăng nhập chưa từ SharedPreferences
                android.content.SharedPreferences sharedPreferences = getSharedPreferences("SunSaverPrefs", android.content.Context.MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false);

                Intent intent;
                if (isLoggedIn) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                
                // Đóng SplashActivity hiện tại để user không thể bấm nút Quay lại (Back) để mở lại Splash
                finish();
            }
        }, 2000);
    }
}
