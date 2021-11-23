package com.jr_eagle_ocr.go4lunch.ui.logout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.databinding.FragmentLogoutBinding;
import com.jr_eagle_ocr.go4lunch.ui.ViewModelFactory;

/**
 * @author jrigault
 */
public class LogOutDialogFragment extends DialogFragment {
    private LogOutViewModel viewModel;
    private FragmentLogoutBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(LogOutViewModel.class);
        binding = FragmentLogoutBinding.inflate(getLayoutInflater());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        init();

        return binding.getRoot();
    }

    /**
     * Set listener and livedata observer
     */
    private void init() {
        // Set OK button click listener
        binding.buttonOk.setOnClickListener(v -> {
            viewModel.signOut(requireActivity());
        });
        // Set sign out result observer
        viewModel.signOutResult().observe(getViewLifecycleOwner(), signOutResult -> {
            String message = getString(signOutResult);
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}