package com.potato.apicompiler;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.potato.tools.DebugManager;

public class MainActivity extends AppCompatActivity {

    @ApiLog(nameChinese = "初始化", nameEnglish = "onCreate()")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}