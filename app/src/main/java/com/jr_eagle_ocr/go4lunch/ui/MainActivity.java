package com.jr_eagle_ocr.go4lunch.ui;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.CANCEL_ALARM;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.CANCEL_WORKER;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.SET_ALARM;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.SET_WORKER;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityMainBinding;
import com.jr_eagle_ocr.go4lunch.ui.notification.AlertReceiver;
import com.jr_eagle_ocr.go4lunch.ui.notification.NotificationsWorker;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.concurrent.TimeUnit;

/**
 * @author jrigault
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String JUMP_TO_RESTAURANT_DETAIL = "JUMP_TO_RESTAURANT_DETAIL";
    private static final String NOON_REMINDER = "NOON_REMINDER";

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private WorkManager workManager;

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private View header;

    private MainViewState viewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean jumpToRestaurantDetail = getIntent().getBooleanExtra(JUMP_TO_RESTAURANT_DETAIL, false);
        viewModel = new ViewModelProvider(this, new PassingParameterViewModelFactory(jumpToRestaurantDetail)).get(MainViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        workManager = WorkManager.getInstance(this);

        setToolbar();
        setNavigationViews();
        setNavigateToObserver();
        setViewStateObserver();
    }

    private void setToolbar() {
        final Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
    }

    /**
     * Set navigation views (drawer and bottom bar), controller and drawer specific listener
     */
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
            viewModel.navigationItemSelected(item.getItemId());
            return false;
        });

        BottomNavigationView bottomBar = binding.appBarMain.navBar;
        NavigationUI.setupWithNavController(bottomBar, navController);

        header = navigationView.getHeaderView(0);
    }

    /**
     * Set navigateTo event livedata observer and perform proper action
     * (navigation to specified item or toast a message)
     */
    private void setNavigateToObserver() {
        viewModel.navigateTo().observe(this, event -> {
            if (!event.getHasBeenHandled()) {
                Integer navigateTo = event.getContentIfNotHandled();
                if (navigateTo != null) {
                    Bundle args = new Bundle();
                    String currentUserChosenRestaurantId = viewState.getCurrentUserChosenRestaurantId();
                    String placeId = viewState != null ? currentUserChosenRestaurantId : null;
                    args.putString(RestaurantDetailActivity.PLACE_ID, placeId);
                    navController.navigate(navigateTo, args);
                } else {
                    Snackbar.make(this.header, R.string.you_have_not_decided_yet, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Set mainviewstate livedata observer to be used to set current user details in drawer header
     * and perform proper action about noon reminder feature
     */
    @SuppressLint("NewApi") // Managed by MainViewModel
    private void setViewStateObserver() {
        viewModel.getMainViewState().observe(this, viewState -> {
            this.viewState = viewState;
            if (viewState != null) {
                setDrawerHeader();
                String action = viewState.getAction();
                if (action != null) {
                    switch (action) {
                        case SET_WORKER:
                            setWorker();
                            break;
                        case SET_ALARM:
                            setAlarm();
                            break;
                        case CANCEL_WORKER:
                            cancelWorker();
                            break;
                        case CANCEL_ALARM:
                            cancelAlarm();
                            break;
                    }
                }
            }
        });
    }

    /**
     * Set current user details in drawer header
     */
    private void setDrawerHeader() {
        TextView userName = header.findViewById(R.id.drwr_user_name);
        ImageView userPhoto = header.findViewById(R.id.drwr_user_photo);
        userName.setText(viewState.getUserName());

        Glide.with(MainActivity.this)
                .load(viewState.getUserUrlPicture())
                .error(R.drawable.no_photo)
                .apply(RequestOptions.circleCropTransform())
                .into(userPhoto);

        TextView userEmailTextView = header.findViewById(R.id.drwr_user_email);
        String userEmail = viewState.getUserEmail();
        userEmailTextView.setText(userEmail);

        Log.d(TAG, "onCreate: " + "User details set for " + userName.getText());
    }

    /**
     * Set noon reminder if API version >= 26
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWorker() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationsWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(viewModel.getInitialDelay(), TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniqueWork(NOON_REMINDER, ExistingWorkPolicy.REPLACE, workRequest);
    }

    /**
     * Cancel noon reminder on API version >= 26
     */
    private void cancelWorker() {
        workManager.cancelUniqueWork(NOON_REMINDER);
    }

    /**
     * Set noon reminder if API version < 26
     */
    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, viewModel.getAlarmTrigger(), this.getPendingIntent());
    }

    /**
     * Cancel noon reminder on API version < 26
     */
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(this.getPendingIntent());
    }

    /**
     * Get pending intent to bew used for noon reminder on API version < 26
     *
     * @return a pending intent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, AlertReceiver.class);
        return PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Navigate pattern
     *
     * @param caller                 the context from where to launch this activity
     * @param jumpToRestaurantDetail a boolean indicating if once this activity is started
     *                               it must navigate to restaurant detail activity
     * @return an intent containing extra to use with {@link #startActivity(Intent)}
     */
    public static Intent navigate(Context caller, boolean jumpToRestaurantDetail) {
        Intent intent = new Intent(caller, MainActivity.class);
        intent.putExtra(JUMP_TO_RESTAURANT_DETAIL, jumpToRestaurantDetail);
        return intent;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        Glide.get(this).clearDiskCache();
    }
}