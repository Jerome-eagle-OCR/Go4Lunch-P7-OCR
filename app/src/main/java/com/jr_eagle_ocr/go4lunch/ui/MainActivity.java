package com.jr_eagle_ocr.go4lunch.ui;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityMainBinding;
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;

/**
 * @author jrigault
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;
    private View header;

    private final TempUserRestaurantManager tempUserRestaurantManager = TempUserRestaurantManager.getInstance();
    private FirebaseUser currentFirebaseUser;
    private String currentUserChosenRestaurant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();
        setNavigationViews();
        setDrawerHeader();
        setCurrentUserListeners();
    }

    private void setToolbar() {
        final Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
    }

    private void setNavigationViews() {
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map_view, R.id.nav_list_view, R.id.nav_workmates, R.id.nav_your_lunch,
                R.id.nav_settings, R.id.nav_logout, R.id.authentication)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawers();
            return onNavigationItemSelected(item);
        });

        BottomNavigationView bottomBar = binding.appBarMain.navBar;
        NavigationUI.setupWithNavController(bottomBar, navController);

        header = navigationView.getHeaderView(0);
    }

    private void setDrawerHeader() {
        TextView userName = header.findViewById(R.id.drwr_user_name);
        ImageView userPhoto = header.findViewById(R.id.drwr_user_photo);
        tempUserRestaurantManager.getUserData().observe(this, user -> {
            if (user != null) {
                userName.setText(user.getUserName());

                Glide.with(MainActivity.this)
                        .load(user.getUserUrlPicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(userPhoto);

                Log.d(TAG, "onCreate: " + "User details set for " + user.getUserName());
            }
        });
    }

    private void setCurrentUserListeners() {
        tempUserRestaurantManager.getCurrentFirebaseUser().observe(this, firebaseUser -> {
            this.currentFirebaseUser = firebaseUser;
            if (firebaseUser == null) {
                navController.navigate(R.id.authentication);
            }
            setEmail();
        });
        tempUserRestaurantManager.getCurrentUserChosenRestaurant().observe(this, restaurantId ->
                currentUserChosenRestaurant = restaurantId);
    }

    private void setEmail() {
        TextView userEmailTextView = header.findViewById(R.id.drwr_user_email);
        String userEmail = (currentFirebaseUser != null) ? currentFirebaseUser.getEmail() : "";
        userEmailTextView.setText(userEmail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean hasCurrentUserChosen = currentUserChosenRestaurant != null;
        if (item.getItemId() == R.id.nav_your_lunch && !hasCurrentUserChosen) {
            Snackbar.make(this.header, R.string.you_have_not_decided_yet, Snackbar.LENGTH_LONG).show();
        } else {
            navController.navigate(item.getItemId());
        }
        return false;
    }

    public static Intent navigate(AppCompatActivity caller) {
        return new Intent(caller, MainActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}