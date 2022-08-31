package com.github.jeffery.skin;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private TextView tvSkinName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSkinName = findViewById(R.id.tv_skin_name);
        tvSkinName.setText(getCurrentSkinName());
        findViewById(R.id.btn_default_skin).setOnClickListener(v -> {
            SkinManger.getInstance().setAppSkin(null);
            tvSkinName.setText(getCurrentSkinName());
        });
        findViewById(R.id.btn_gray_skin).setOnClickListener(v -> {
            File file1 = new File(v.getContext().getExternalFilesDir("skin"), "skin-gray-release.apk");
            SkinManger.getInstance().setAppSkin(file1.getAbsolutePath());
            tvSkinName.setText(getCurrentSkinName());
        });
        findViewById(R.id.btn_red_skin).setOnClickListener(v -> {
            File file12 = new File(v.getContext().getExternalFilesDir("skin"), "skin-red-release.apk");
            SkinManger.getInstance().setAppSkin(file12.getAbsolutePath());
            tvSkinName.setText(getCurrentSkinName());
        });
        findViewById(R.id.btn_fragment).setOnClickListener(v -> startActivity(new Intent(v.getContext(), SecondActivity.class)));
        ActivityCompat.requestPermissions(this, new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 100);
    }

    private String getCurrentSkinName() {
        String skin = SkinManger.getInstance().getCurrentSkin();
        if (skin == null || skin.isEmpty()) {
            return "当前皮肤：默认";
        }
        return "当前皮肤：" + skin.substring(skin.lastIndexOf('/') + 1);
    }
}