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
import com.jr_eagle_ocr.go4lunch.authentication.UserManager;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityRestaurantDetailBinding;
import com.jr_eagle_ocr.go4lunch.databinding.RestaurantContentScrollingBinding;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.ui.UserAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestaurantDetailActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private ActivityRestaurantDetailBinding binding;
    private RestaurantContentScrollingBinding scrollingBinding;

    private FloatingActionButton fab;
    private UserAdapter userAdapter;

    // Used to pass selected restaurant to detail activity
    private static final String PLACE_ID = "PLACE_ID";

    private final UserManager userManager = UserManager.getInstance();
    private List<User> usersJoining;
    private final RestaurantRepository restaurantRepository = RestaurantRepository.getInstance();
    private Restaurant restaurant;
    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;

    private static final String LIKE = "LIKE";
    private static final String CHOICE = "CHOICE";
    private ImageView restaurantLike;
    private boolean isLiked;
    private int likeVisibility;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        scrollingBinding = RestaurantContentScrollingBinding.bind(view);

        setToolbar();
        setBottomNavigationView();
        setRestaurantDetails();
        initFab();
        setRecyclerView();
    }

    private void setToolbar() {
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        binding.toolbarLayout.setTitleEnabled(false);
    }

    private void setBottomNavigationView() {
        scrollingBinding.navBarDetail.setOnItemSelectedListener(this);
    }

    private void setRestaurantDetails() {
        Map<String, Restaurant> restaurants = restaurantRepository.getRestaurants();
        restaurantId = getIntent().getStringExtra(PLACE_ID);
        restaurant = restaurants.get(restaurantId);
        if (restaurant != null) {
            restaurantName = restaurant.getName();
            restaurantAddress = restaurant.getAddress();
            binding.restaurantImage.setImageBitmap(restaurant.getPhoto());
        }
        binding.restaurantName.setText(restaurantName);

        restaurantLike = binding.restaurantLike;
        isLiked = false; //TODO: when Firestore implemented retrieve liked restaurants and check if liked by current user
        likeVisibility = isLiked ? View.VISIBLE : View.INVISIBLE;
        restaurantLike.setVisibility(likeVisibility);

        binding.restaurantAddress.setText(restaurantAddress);
    }

    private void initFab() {
        fab = binding.fab;
        setFab(isChosen());
        //Manage click on fab
        fab.setOnClickListener(view -> {
            boolean isChosen = !isChosen();
            String choice = isChosen ? restaurantId : null;
            userManager.setUserChoice(choice);
            setFab(isChosen);
            snackLikeChoice(CHOICE, isChosen);
            //TODO: to be modified when Firestore implemented
            refreshUserJoiningList();
        });
    }

    private void refreshUserJoiningList() {
        usersJoining = userManager.getUsersLunchingAtGivenRestaurant(restaurantId);
        userAdapter.setItems(new ArrayList<>(usersJoining));
    }

    private void setRecyclerView() {
        final RecyclerView recyclerView = scrollingBinding.userRecyclerview;
        recyclerView.setHasFixedSize(true);
        userAdapter = new UserAdapter(usersJoining, true);
        recyclerView.setAdapter(userAdapter);
        refreshUserJoiningList();
    }

    private boolean isChosen() {
        return Objects.equals(userManager.getUserChoice(), restaurantId);
    }

    private void setFab(boolean isChosen) {
        Drawable drawable = isChosen ?
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_circle, getTheme())
                : ResourcesCompat.getDrawable(getResources(), R.drawable.ic_circle, getTheme());
        fab.setImageDrawable(drawable);
    }

    private void snackLikeChoice(String likeOrChoice, boolean isLikedOrChosen) {
        String snackMsg;
        switch (likeOrChoice) {
            case LIKE:
                snackMsg = isLikedOrChosen ?
                        restaurantName + getString(R.string.like_restaurant)
                        : restaurantName + getString(R.string.unlike_restaurant);
                break;
            case CHOICE:
                snackMsg = isLikedOrChosen ?
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
        switch (item.getItemId()) {
            case R.id.call:
                String phoneNumber = restaurant.getPhoneNumber();
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                startActivity(dial);
                return true;
            case R.id.like:
                isLiked = !isLiked;
                if (isLiked) {
                    likeVisibility = View.VISIBLE;
                    //TODO: add userId in restaurant like list
                } else {
                    likeVisibility = View.INVISIBLE;
                    //TODO: remove userId from restaurant like list
                }
                restaurantLike.setVisibility(likeVisibility);
                snackLikeChoice(LIKE, isLiked);
                return true;
            case R.id.website:
                String websiteUrl = restaurant.getWebSiteUrl();
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                startActivity(browse);
                return true;
            default:
                return false;
        }
    }

    public static Intent navigate(Activity startActivity, String restaurantId) {
        Intent intent = new Intent(startActivity, RestaurantDetailActivity.class);
        intent.putExtra(PLACE_ID, restaurantId);
        return intent;
    }
}