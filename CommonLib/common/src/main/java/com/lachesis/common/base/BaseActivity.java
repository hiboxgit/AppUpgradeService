package com.lachesis.common.base;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lachesis.common.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }
}
