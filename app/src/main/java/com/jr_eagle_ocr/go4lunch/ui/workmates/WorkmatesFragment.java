package com.jr_eagle_ocr.go4lunch.ui.workmates;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.jr_eagle_ocr.go4lunch.databinding.FragmentWorkmatesBinding;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;
import com.jr_eagle_ocr.go4lunch.ui.ViewModelFactory;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserAdapter;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.ArrayList;

/**
 * @author jrigault
 * A fragment representing a list of Items.
 */
public class WorkmatesFragment extends Fragment implements UserAdapter.DisplayChosenRestaurantListener {
    private FragmentWorkmatesBinding binding;
    private WorkmatesViewModel viewModel;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(WorkmatesViewModel.class);

        // Set current displayed fragment in mainviewmodel
        ((MainActivity) requireActivity()).getViewModel().setCurrentFragment(MainViewModel.WORKMATES);

        setRecyclerView();
    }

    private void setRecyclerView() {
        final RecyclerView recyclerView = binding.workmatesUserRecyclerview;
        recyclerView.setHasFixedSize(true);
        final UserAdapter userAdapter = new UserAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(userAdapter);

        viewModel.getUserViewStates().observe(getViewLifecycleOwner(), allUsers -> {
            if (allUsers != null) userAdapter.updateItems(allUsers);
        });
    }

    @Override
    public void onDisplayChosenRestaurant(String restaurantId) {
        Intent intent = RestaurantDetailActivity.navigate((Activity) this.requireContext(), restaurantId);
        this.startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}