package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityRestaurantDetailBinding;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserAdapter;
import com.jr_eagle_ocr.go4lunch.ui.adaptersviewstates.UserViewState;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.usecases.SetClearChosenRestaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class RestaurantDetailActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private ActivityRestaurantDetailBinding binding;

    private FloatingActionButton fab;

    // Used to pass selected restaurant to detail activity
    private static final String PLACE_ID = "PLACE_ID";
    // Used to display appropriated snackbar
    private static final String LIKE = "LIKE";
    private static final String CHOICE = "CHOICE";


    private final TempUserRestaurantManager tempUserRestaurantManager = TempUserRestaurantManager.getInstance();
    private final SetClearChosenRestaurant mSetClearChosenRestaurant = Go4LunchApplication.getDependencyContainer().getUseCaseSetClearChosenRestaurant();
    private final GetCurrentUserChosenRestaurantId mGetCurrentUserChosenRestaurantId = Go4LunchApplication.getDependencyContainer().getUseCaseGetCurrentUserChosenRestaurantId();

    private List<UserViewState> joiningUsers = new ArrayList<>();
    private Restaurant restaurant;
    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private boolean isChosen;

    private ImageView restaurantLike;
    private boolean isLiked;
    private int likeVisibility;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();
        setBottomNavigationView();
        setRestaurantDetails();
        setRecyclerView();
        setFab();
    }

    private void setToolbar() {
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        binding.toolbarLayout.setTitleEnabled(false);
    }

    private void setBottomNavigationView() {
        binding.scrollingLyt.navBarDetail.setOnItemSelectedListener(this);
    }

    private void setRestaurantDetails() {
        restaurantId = getIntent().getStringExtra(PLACE_ID);
        if (restaurantId == null) {
            restaurantId = mGetCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId().getValue();
        }
        Map<String, Restaurant> restaurants = tempUserRestaurantManager.getFoundRestaurants().getValue();
        if (restaurants != null) {
            restaurant = restaurants.get(restaurantId);
        }
        if (restaurant != null) {
            restaurantName = restaurant.getName();
            restaurantAddress = restaurant.getAddress();
            binding.restaurantImage.setImageBitmap(restaurant.getPhoto());
        }
        binding.restaurantName.setText(restaurantName);

        restaurantLike = binding.restaurantLike;
        tempUserRestaurantManager.getIsLiked(restaurantId).observe(this, aBoolean -> {
            isLiked = aBoolean;
            likeVisibility = isLiked ? View.VISIBLE : View.INVISIBLE;
            restaurantLike.setVisibility(likeVisibility);
        });

        binding.restaurantAddress.setText(restaurantAddress);
    }

    private void setRecyclerView() {
        final RecyclerView recyclerView = binding.scrollingLyt.joiningUserRecyclerview;
        recyclerView.setHasFixedSize(true);
        final UserAdapter userAdapter = new UserAdapter(joiningUsers, null);
        recyclerView.setAdapter(userAdapter);

        tempUserRestaurantManager.getJoiningUserViewStates(restaurantId).observe(this, userViewStates -> {
            if (userViewStates != null) {
                joiningUsers = userViewStates;
                userAdapter.updateItems(joiningUsers);
            }
        });
    }

    private void setFab() {
        fab = binding.fab;
        // Update fab
        tempUserRestaurantManager.getIsChosen().observe(this, aBoolean -> {
            if (aBoolean != null) {
                isChosen = aBoolean;
                manageFabDisplay(isChosen);
            }
        });
        //Manage click on fab
        fab.setOnClickListener(view -> {
            isChosen = !isChosen;
            if (isChosen) {
                mSetClearChosenRestaurant.setChosenRestaurant(restaurantId)
                        .observe(this, aBoolean -> snackLikeChoice(CHOICE, true));
            } else {
                mSetClearChosenRestaurant.clearChosenRestaurant(restaurantId)
                        .observe(this, aBoolean -> snackLikeChoice(CHOICE, false));
            }
        });
    }

    private void manageFabDisplay(boolean isChosen) {
        Drawable drawable = isChosen ?
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_circle, getTheme())
                : ResourcesCompat.getDrawable(getResources(), R.drawable.ic_circle, getTheme());
        fab.setImageDrawable(drawable);
    }

    private void snackLikeChoice(String likeOrChoice, boolean isLikedChosen) {
        String snackMsg;
        switch (likeOrChoice) {
            case LIKE:
                snackMsg = isLikedChosen ?
                        restaurantName + getString(R.string.like_restaurant)
                        : restaurantName + getString(R.string.unlike_restaurant);
                break;
            case CHOICE:
                snackMsg = isLikedChosen ?
                        getString(R.string.choose_restaurant) + restaurantName + "."
                        : getString(R.string.unchoose_restaurant) + restaurantName + ".";
                break;
            default:
                return;
        }
        Snackbar.make(binding.getRoot(), snackMsg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getOrder()) {
            case 100:
                String phoneNumber = restaurant.getPhoneNumber();
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                startActivity(dial);
                return true;
            case 200:
                isLiked = !isLiked;
                if (isLiked) {
                    tempUserRestaurantManager.setLikedRestaurant(restaurantId)
                            .observe(this, aBoolean -> {
                                if (aBoolean) snackLikeChoice(LIKE, isLiked);
                            });
                } else {
                    tempUserRestaurantManager.clearLikedRestaurant(restaurantId)
                            .observe(this, aBoolean -> {
                                if (aBoolean) snackLikeChoice(LIKE, isLiked);
                            });
                }
                return true;
            case 300:
                String websiteUrl = restaurant.getWebSiteUrl();
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                startActivity(browse);
                return true;
            default:
                return false;
        }
    }

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