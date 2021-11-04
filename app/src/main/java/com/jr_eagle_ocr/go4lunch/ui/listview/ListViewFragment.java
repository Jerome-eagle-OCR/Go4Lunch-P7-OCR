package com.jr_eagle_ocr.go4lunch.ui.listview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jr_eagle_ocr.go4lunch.databinding.FragmentListviewBinding;
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantAdapter;
import com.jr_eagle_ocr.go4lunch.ui.adaptersviewstates.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ListViewFragment extends Fragment implements RestaurantAdapter.DisplayRestaurantListener {

    private FragmentListviewBinding binding;
    private final TempUserRestaurantManager tempUserRestaurantManager = TempUserRestaurantManager.getInstance();
    private List<RestaurantViewSate> restaurants = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: viewmodel

        setRecyclerView();
    }

    private void setRecyclerView() {
        final RecyclerView recyclerView = binding.restaurantRecyclerview;
        recyclerView.setHasFixedSize(true);
        final RestaurantAdapter adapter = new RestaurantAdapter(restaurants, this);
        recyclerView.setAdapter(adapter);

        //TODO: modify to VM getAllRestaurants livedata observer
        tempUserRestaurantManager.getAllRestaurantViewStates().observe(getViewLifecycleOwner(), restaurantViewSates -> {
            if (restaurantViewSates != null) {
                restaurants = restaurantViewSates;
                adapter.updateItems(restaurants);
            }
        });
    }

    @Override
    public void onDisplayRestaurant(String restaurantId) {
        Intent intent = RestaurantDetailActivity.navigate(this.requireActivity(), restaurantId);
        this.startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}