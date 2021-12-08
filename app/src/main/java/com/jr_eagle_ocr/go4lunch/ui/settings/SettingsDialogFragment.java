package com.jr_eagle_ocr.go4lunch.ui.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.FragmentSettingsBinding;
import com.jr_eagle_ocr.go4lunch.ui.ViewModelFactory;

/**
 * @author jrigault
 */
public class SettingsDialogFragment extends DialogFragment {
    private SettingsViewModel viewModel;
    private FragmentSettingsBinding binding;
    private SwitchCompat switchNotification;
    private TextInputEditText userNameInputEditText;
    private TextInputEditText userUrlPictureInputEditText;
    private TextInputEditText userDelete;
    private Button buttonValidate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(SettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        init();
        setListeners();
        setObservers();
        return binding.getRoot();
    }

    /**
     * Set all widgets
     */
    private void init() {
        // Get viewstate from viewmodel
        SettingsDialogViewState viewState = viewModel.getSettingsDialogViewState();
        // Set dialog title
        binding.textSettings.setText(R.string.menu_settings);
        // Set notification activation switch
        switchNotification = binding.switchNotification;
        switchNotification.setText(R.string.reminder);
        switchNotification.setChecked(viewState.isSwitchNotificationChecked());
        // Set user name textview
        userNameInputEditText = binding.userName;
        userNameInputEditText.setText(viewState.getUserName());
        // Set user photo URL textview
        userUrlPictureInputEditText = binding.userUrlPicture;
        userUrlPictureInputEditText.setText(viewState.getUserURLpicture());
        // Set delete user "fake button"
        userDelete = binding.userDelete;
        userDelete.setText(R.string.delete_your_account);
        // Set validation button
        buttonValidate = binding.buttonValidate;
        buttonValidate.setText(R.string.validate);
    }

    /**
     * Set widgets listeners
     */
    private void setListeners() {
        // Set notification activation switch check change listener
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setNoonReminderEnabled(isChecked));
        // Set user name textview text change listener
        userNameInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setUserName(s.toString());
            }
        });
        // Set user photo URL textview text change listener
        userUrlPictureInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setUserUrlPicture(s.toString());
            }
        });
        // Set delete user "fake button" click listener
        userDelete.setOnClickListener(v -> {
            AlertDialog confirm = new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_your_account) + " ?")
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        viewModel.deleteUser(requireActivity());
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.fui_cancel, (dialog, which) -> dialog.dismiss())
                    .create();
            confirm.show();
        });
        // Set validation button click listener
        buttonValidate.setOnClickListener(v -> viewModel.clickOnButtonValidate());
    }

    /**
     * Set livedata observers
     */
    private void setObservers() {
        // Set delete user result observer
        viewModel.deleteUserResult().observe(getViewLifecycleOwner(), deleteUserResult -> {
            String message = getString(deleteUserResult);
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
            dismiss();
        });
        // Set validation result observer
        viewModel.validateResult().observe(getViewLifecycleOwner(), validateResult -> {
            String message = getString(validateResult);
            View view = requireActivity().findViewById(android.R.id.content);
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}