package com.jr_eagle_ocr.go4lunch.ui.logout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.databinding.FragmentLogoutBinding;


public class LogOutDialogFragment extends DialogFragment {

    private LogOutViewModel mLogOutViewModel;
    private FragmentLogoutBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mLogOutViewModel = new ViewModelProvider(this).get(LogOutViewModel.class);
        binding = FragmentLogoutBinding.inflate(getLayoutInflater());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final TextView textView = binding.textLogout;
        mLogOutViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.buttonOk.setOnClickListener(v -> Toast.makeText(getContext(),
                "TO BE IMPLEMENTED", Toast.LENGTH_SHORT).show());

        return binding.getRoot();
    }

    //    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        mLogOutViewModel = new ViewModelProvider(this).get(LogOutViewModel.class);
//
//        binding = FragmentLogoutBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//        root.setBackgroundColor(getResources().getColor(R.color.orange_darker));
//        root.setAlpha(0.7f);
//
//        final TextView textView = binding.textLogout;
//        mLogOutViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//        return root;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}