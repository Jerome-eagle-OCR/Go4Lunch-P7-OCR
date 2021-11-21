package com.jr_eagle_ocr.go4lunch.ui.listview;

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

import com.jr_eagle_ocr.go4lunch.databinding.FragmentListviewBinding;
import com.jr_eagle_ocr.go4lunch.ui.ViewModelFactory;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantAdapter;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.ArrayList;

/**
 * @author jrigault
 * A fragment representing a list of Items.
 */
public class ListViewFragment extends Fragment implements RestaurantAdapter.DisplayRestaurantListener {

    private ListViewViewModel viewModel;
    private FragmentListviewBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(ListViewViewModel.class);

        setRecyclerView();
    }

    private void setRecyclerView() {
        final RecyclerView recyclerView = binding.restaurantRecyclerview;
        recyclerView.setHasFixedSize(true);
        final RestaurantAdapter adapter = new RestaurantAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        viewModel.getAllRestaurantViewStates().observe(getViewLifecycleOwner(), restaurants -> {
            if (restaurants != null) {
                adapter.updateItems(restaurants);
            }
        });
    }

    @Override
    public void onDisplayRestaurant(String restaurantId) {
        Intent intent = RestaurantDetailActivity.navigate((Activity) this.requireContext(), restaurantId);
        this.startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}