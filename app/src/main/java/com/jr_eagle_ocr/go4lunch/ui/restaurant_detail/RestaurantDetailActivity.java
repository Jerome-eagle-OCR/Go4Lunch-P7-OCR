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
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.UserViewState;
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;
import com.jr_eagle_ocr.go4lunch.ui.UserAdapter;

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
        initFab();
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
        Map<String, Restaurant> restaurants = tempUserRestaurantManager.getFoundRestaurants();
        restaurant = restaurants.get(restaurantId);
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
        final RecyclerView recyclerView = binding.scrollingLyt.userRecyclerview;
        recyclerView.setHasFixedSize(true);
        final UserAdapter userAdapter = new UserAdapter(joiningUsers, null);
        recyclerView.setAdapter(userAdapter);

        tempUserRestaurantManager.getJoiningUsers(restaurantId).observe(this, userViewStates -> {
            if (userViewStates != null) {
                joiningUsers = userViewStates;
                userAdapter.updateItems(joiningUsers);
            }
        });
    }

    private void initFab() {
        fab = binding.fab;
//        isChosen = false;
//        setFab(false);
        // Update fab
        tempUserRestaurantManager.getIsChosen().observe(this, aBoolean -> {
            if (aBoolean != null) {
                isChosen = aBoolean;
                setFab(isChosen);
            }
        });
        //Manage click on fab
        fab.setOnClickListener(view -> {
            isChosen = !isChosen;
            if (isChosen) {
                tempUserRestaurantManager.setChosenRestaurant(restaurantId)
                        .observe(this, aBoolean -> snackLikeChoice(CHOICE, true));
            } else {
                tempUserRestaurantManager.clearChosenRestaurant(restaurantId)
                        .observe(this, aBoolean -> snackLikeChoice(CHOICE, false));
            }
//            setFab(isChosen);
        });
    }

    private void setFab(boolean isChosen) {
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
        Snackbar.make(binding.getRoot(), snackMsg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getOrder()) {
            case 1:
                String phoneNumber = restaurant.getPhoneNumber();
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                startActivity(dial);
                return true;
            case 2:
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
            case 3:
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