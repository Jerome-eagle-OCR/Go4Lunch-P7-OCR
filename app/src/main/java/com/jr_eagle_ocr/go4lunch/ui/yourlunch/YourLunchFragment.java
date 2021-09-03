package com.jr_eagle_ocr.go4lunch.ui.yourlunch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.databinding.FragmentYourlunchBinding;

public class YourLunchFragment extends Fragment {

    private YourLunchViewModel mYourLunchViewModel;
    private FragmentYourlunchBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mYourLunchViewModel =
                new ViewModelProvider(this).get(YourLunchViewModel.class);

        binding = FragmentYourlunchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        mYourLunchViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}