package com.jr_eagle_ocr.go4lunch.ui;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityMainBinding;
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;

/**
 * @author jrigault
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController mNavController;
    private ActivityMainBinding binding;
    private View header;

    private final TempUserRestaurantManager tempUserRestaurantManager = TempUserRestaurantManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map_view, R.id.nav_list_view, R.id.nav_workmates, R.id.nav_your_lunch, R.id.nav_settings, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, mNavController);

        BottomNavigationView bottomBar = binding.appBarMain.navBar;
        NavigationUI.setupWithNavController(bottomBar, mNavController);

        header = navigationView.getHeaderView(0);
        TextView userName = header.findViewById(R.id.drwr_user_name);
        ImageView userPhoto = header.findViewById(R.id.drwr_user_photo);
        tempUserRestaurantManager.getUserData().observe(this, user -> {
            if (user != null) {
                userName.setText(user.getUserName());

                Glide.with(MainActivity.this)
                        .load(user.getUserUrlPicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(userPhoto);

                Toast.makeText(MainActivity.this, "User details set for " + user.getUserName() + " !", Toast.LENGTH_LONG).show();
            }
        });

        TextView userEmailTextView = header.findViewById(R.id.drwr_user_email);
        FirebaseUser firebaseUser = tempUserRestaurantManager.getCurrentFirebaseUser();
        String userEmail = (firebaseUser != null) ? firebaseUser.getEmail() : "";
        userEmailTextView.setText(userEmail);
    }

    private void initToolbar() {
        final Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}