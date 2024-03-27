package com.kangxin.doctor.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.kangxin.doctor.R;

/**
 * 闪屏页面
 */
public class SplashActivity extends ComponentActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_splash);
    startActivity(new Intent(getBaseContext(), MainActivity.class));
  }
}
