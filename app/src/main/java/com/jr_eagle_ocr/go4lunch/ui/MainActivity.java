package com.jr_eagle_ocr.go4lunch.ui;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.authentication.UserManager;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityMainBinding;
import com.jr_eagle_ocr.go4lunch.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController mNavController;
    private ActivityMainBinding binding;
    private View header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);

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

        UserManager userManager = UserManager.getInstance();
        User user = userManager.getCurrentUser();

        header = navigationView.getHeaderView(0);
        TextView userName = header.findViewById(R.id.drwr_user_name);
        userName.setText(user.getUserName());
        TextView userEmail = header.findViewById(R.id.drwr_user_email);
        userEmail.setText(userManager.getCurrentFirebaseUser().getEmail());

        ImageView userPhoto = header.findViewById(R.id.drwr_user_photo);
        Bitmap bm = getUserBitmap(user);
        userPhoto.setImageBitmap(bm);
    }

    private Bitmap getUserBitmap(User user) {
        AtomicReference<Bitmap> userPhotoBtmp = new AtomicReference<>();
        ExecutorService executors = Executors.newSingleThreadExecutor();
        if (user.getUserUrlPicture() != null) {
            executors.execute(() -> {
                URL url = null;
                try {
                    url = new URL(user.getUserUrlPicture());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpsURLConnection connection = null;
                try {
                    assert url != null;
                    connection = (HttpsURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStream inputStream = null;
                try {
                    assert connection != null;
                    inputStream = connection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                userPhotoBtmp.set(BitmapFactory.decodeStream(inputStream));
                executors.shutdown();
            });
            while (!executors.isShutdown()) {
                Log.d("PHOTO URL: ", "Still running...");
            }
        } else {
            userPhotoBtmp.set(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar));
        }
        return userPhotoBtmp.get();
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