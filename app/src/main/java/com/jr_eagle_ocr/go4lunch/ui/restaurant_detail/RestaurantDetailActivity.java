package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityRestaurantDetailBinding;
import com.jr_eagle_ocr.go4lunch.ui.PassingParameterViewModelFactory;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserAdapter;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jrigault
 */
public class RestaurantDetailActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private final String TAG = getClass().getSimpleName();
    // Used to pass selected restaurant to detail activity
    public static final String PLACE_ID = "PLACE_ID";
    private RestaurantDetailViewModel viewModel;
    private ActivityRestaurantDetailBinding binding;
    private FloatingActionButton chooseFab;
    private UserAdapter userAdapter;

    private RestaurantDetailViewSate restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String restaurantId = getIntent().getStringExtra(PLACE_ID);
        Log.d(TAG, "Restaurant id from extra: " + restaurantId);
        viewModel = new ViewModelProvider(this, new PassingParameterViewModelFactory(restaurantId)).get(RestaurantDetailViewModel.class);
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();
        setBottomNavigationView();
        initFab();
        setRestaurantDetails();
        setRecyclerView();
        setSnackbarOnEvent();
    }

    /**
     * Set toolbar without title
     */
    private void setToolbar() {
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        binding.toolbarLayout.setTitleEnabled(false);
    }

    /**
     * Set bottom navigation listener
     */
    private void setBottomNavigationView() {
        binding.scrollingLyt.navBarDetail.setOnItemSelectedListener(this);
    }

    /**
     * Set choose fab and its listener
     */
    private void initFab() {
        chooseFab = binding.fab;
        binding.fab.setOnClickListener(view -> viewModel.clickOnChooseFab());
    }

    /**
     * Set joining user recycler view list
     */
    private void setRecyclerView() {
        final RecyclerView recyclerView = binding.scrollingLyt.joiningUserRecyclerview;
        recyclerView.setHasFixedSize(true);
        userAdapter = new UserAdapter(new ArrayList<>(), null);
        recyclerView.setAdapter(userAdapter);
    }

    /**
     * Set restaurant viewstate observer
     */
    @SuppressLint("WrongConstant")
    private void setRestaurantDetails() {
        viewModel.getRestaurantViewState().observe(this, restaurant -> {
            this.restaurant = restaurant;
            binding.restaurantImage.setImageBitmap(restaurant.getPhoto());
            binding.restaurantName.setText(restaurant.getName());
            binding.restaurantAddress.setText(restaurant.getAddress());

            ImageView restaurantLike = binding.restaurantLike;
            restaurantLike.setVisibility(restaurant.getLikeVisibility());

            chooseFab.setImageResource(restaurant.getChooseResource());

            List<UserViewState> joiningUsers = restaurant.getJoiningUsers();
            if (joiningUsers != null) userAdapter.updateItems(joiningUsers);
        });
    }

    /**
     * Set snackbar event observer
     */
    private void setSnackbarOnEvent() {
        viewModel.getSnackbarMessageEvent().observe(this, messageResourceEvent -> {
            Integer resource = messageResourceEvent.getContentIfNotHandled();
            if (resource != null) {
                String message = getString(resource) + restaurant.getName();
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = viewModel.getIntent(item.getOrder());
        if (intent != null) {
            startActivity(intent);
        }
        return false;
    }

    /**
     * Navigate pattern
     *
     * @param caller       calling activity from which starting the intent
     * @param restaurantId the id of the restaurant to display details
     * @return a proper intent with extra
     */
    public static Intent navigate(Activity caller, String restaurantId) {
        Intent intent = new Intent(caller, RestaurantDetailActivity.class);
        intent.putExtra(PLACE_ID, restaurantId);
        return intent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}