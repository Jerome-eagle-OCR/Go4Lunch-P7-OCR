package com.jr_eagle_ocr.go4lunch.ui;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityMainBinding;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;
import com.jr_eagle_ocr.go4lunch.ui.viewstates.UserViewState;

/**
 * @author jrigault
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String JUMP_TO_RESTAURANT_DETAIL = "JUMP_TO_RESTAURANT_DETAIL";
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;
    private View header;

    private MainViewModel viewModel;
    private UserViewState currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean jumpToRestaurantDetail = getIntent().getBooleanExtra(JUMP_TO_RESTAURANT_DETAIL, false);
        viewModel = new ViewModelProvider(this, new PassingParameterViewModelFactory(jumpToRestaurantDetail)).get(MainViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();
        setNavigationViews();
        setCurrentUserObserver();
        setNavigateToObserver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setWorker();
        }
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

    private void setCurrentUserObserver() {
        viewModel.getCurrentUserViewState().observe(this, currentUser -> {
            this.currentUser = currentUser;
            if (currentUser == null) {
                navController.navigate(R.id.authentication);
            } else {
                setDrawerHeader();
            }
        });
    }

    private void setDrawerHeader() {
        TextView userName = header.findViewById(R.id.drwr_user_name);
        ImageView userPhoto = header.findViewById(R.id.drwr_user_photo);
        userName.setText(currentUser.getName());

        Glide.with(MainActivity.this)
                .load(currentUser.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(userPhoto);

        TextView userEmailTextView = header.findViewById(R.id.drwr_user_email);
        String userEmail = currentUser.getEmail();
        userEmailTextView.setText(userEmail);

        Log.d(TAG, "onCreate: " + "User details set for " + currentUser.getName());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWorker() {
        WorkManager workManager = WorkManager.getInstance(this);
        PeriodicWorkRequest periodicWorkRequest = viewModel.getPeriodicWorkRequest();
        workManager.enqueueUniquePeriodicWork("periodicWorkRequest", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
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
        viewModel.navigationItemSelected(item.getItemId());
        return false;
    }

    private void setNavigateToObserver() {
        viewModel.navigateTo().observe(this, event -> {
            if (!event.getHasBeenHandled()) {
                Integer navigateTo = event.getContentIfNotHandled();
                if (navigateTo != null) {
                    Bundle args = new Bundle();
                    String placeId = currentUser.getChosenRestaurantId();
                    args.putString(RestaurantDetailActivity.PLACE_ID, placeId);
                    navController.navigate(navigateTo, args);
                } else {
                    Snackbar.make(this.header, R.string.you_have_not_decided_yet, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public static Intent navigate(AppCompatActivity caller, boolean jumpToRestaurantDetail) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(JUMP_TO_RESTAURANT_DETAIL, jumpToRestaurantDetail);
        return new Intent(caller, MainActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}