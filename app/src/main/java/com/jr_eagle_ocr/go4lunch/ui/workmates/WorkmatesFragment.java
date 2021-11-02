package com.jr_eagle_ocr.go4lunch.ui.workmates;

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
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserAdapter;
import com.jr_eagle_ocr.go4lunch.ui.adaptersviewstates.UserViewState;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jrigault
 */
public class WorkmatesFragment extends Fragment implements UserAdapter.DisplayChosenRestaurantListener {

    private FragmentWorkmatesBinding binding;
    private WorkmatesViewModel mViewModel;
    private final TempUserRestaurantManager tempUserRestaurantManager = TempUserRestaurantManager.getInstance();
    private List<UserViewState> allUsers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(WorkmatesViewModel.class);

        setRecyclerView();
    }

    private void setRecyclerView() {
        final RecyclerView recyclerView = binding.workmatesUserRecyclerview;
        recyclerView.setHasFixedSize(true);
        final UserAdapter userAdapter = new UserAdapter(allUsers, this);
        recyclerView.setAdapter(userAdapter);

        tempUserRestaurantManager.getAllUsers().observe(getViewLifecycleOwner(), userViewStates -> {
            allUsers = userViewStates;
            userAdapter.updateItems(allUsers);
        });
    }

    @Override
    public void onDisplayChosenRestaurant(String restaurantId) {
        Intent intent = RestaurantDetailActivity.navigate(this.getActivity(), restaurantId);
        this.startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}