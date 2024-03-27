package com.kangxin.doctor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.byh.module.onlineoutser.BHServiceImp;
import com.kangxin.doctor.R;


public class MainActivity extends ComponentActivity {

    private BHServiceImp hxService = new BHServiceImp();
    TextView loginTV,loginIM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.app_activity_main);

        getIMInfo();

        findViewById(R.id.tv_web).setOnClickListener(v -> {
            startActivity(new Intent(this, JsWebActivity.class));
        });

        /*DialActivity.Companion.launch(Utils.getApp(), new CallMsg(), new CallVideoListener() {
            @Override
            public void receiveListener() {
                ToastUtils.INSTANCE.center(MainActivity.this,"接听");
            }

            @Override
            public void rejectListener() {
                ToastUtils.INSTANCE.center(MainActivity.this,"拒绝");
            }

            @Override
            public void videoEndListener() {
                ToastUtils.INSTANCE.center(MainActivity.this,"结束");
            }
        });*/
    }

    private void getIMInfo() {
        String account = "1679409592439668736*EHOS_PATIENT";
        String userSign = "eJw1js0KgkAAhN9ljxG2un*u0MGDYCRWukV0Cc3NtihMRcvo3du0jjPzzTAvIILYkI9ClRI4JiHEghCOe7eRJXCAZUAw6Cq7JEWhMs1hDdk2Q-aQqEzeanVUfcGkjGPICbcw4pRqio48fxHvl66YeaH4r6lcw9u5bPDGzVfB4Xlec8Hup7DtqiSKd2kkUrPDW*Ul0p*0*fRXrNX1e5VBjKCJGHt-AA5jN8c_";
        hxService.imLogin(account,userSign);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitAfterTwice();
        }
        return super.onKeyDown(keyCode,event);
    }

    private long exitTime = 0;

    public void exitAfterTwice() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

}
