package com.jr_eagle_ocr.go4lunch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.jr_eagle_ocr.go4lunch.databinding.ActivityAuthenticationBinding;

public class AuthenticationActivity extends AppCompatActivity {

    private ActivityAuthenticationBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
    }
}