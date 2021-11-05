package com.jr_eagle_ocr.go4lunch.ui.logout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.FragmentLogoutBinding;


public class LogOutDialogFragment extends DialogFragment {

    private LogOutViewModel viewModel;
    private FragmentLogoutBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(LogOutViewModel.class);
        binding = FragmentLogoutBinding.inflate(getLayoutInflater());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final TextView textView = binding.textLogout;
        textView.setText(getString(R.string.you_will_be_disconnected));

        binding.buttonOk.setOnClickListener(v -> {
            viewModel.signOut(requireContext());
            dismiss();
        });

        viewModel.isLoggedOut().observe(this, aBoolean -> {
            String message;
            if (aBoolean) {
                message = getString(R.string.disconnection_successful);
                dismiss();
            } else {
                message = getString(R.string.disconnection_unsuccessful);
            }
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}