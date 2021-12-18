package com.jr_eagle_ocr.go4lunch.ui;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.UNSET_WORKER;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.SET_WORKER;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.WORKMATES;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.jr_eagle_ocr.go4lunch.ui.notification.NotificationsWorker;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.Arrays;
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
    private boolean locationPermissionGranted;
    private boolean onQueryTextChangeEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean jumpToRestaurantDetail = getIntent().getBooleanExtra(JUMP_TO_RESTAURANT_DETAIL, false);
        viewModel = new ViewModelProvider(this, new PassingParameterViewModelFactory(jumpToRestaurantDetail)).get(MainViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        workManager = WorkManager.getInstance(this);

        checkLocationPermission();
        setToolbar();
        setNavigationViews();
        setToastOnEvent();
        setNavigateToObserver();
        setViewStateObserver();
    }

    /**
     * Check if access fine location is granted and valorize boolean here and in VM
     */
    private void checkLocationPermission() {
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        viewModel.setLocationPermissionGranted(locationPermissionGranted);
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
        // Enable or disabled bottom bar navigation on location permission granted or not
        viewModel.getLocationPermissionGranted().observe(this, aBoolean -> {
            bottomBar.getMenu().getItem(1).setEnabled(aBoolean != null && aBoolean);
            this.locationPermissionGranted = aBoolean != null && aBoolean;
        });

        header = navigationView.getHeaderView(0);
    }

    /**
     * Set toast event observer
     */
    private void setToastOnEvent() {
        viewModel.getToastMessageEvent().observe(this, messageResourceEvent -> {
            Integer resource = messageResourceEvent.getContentIfNotHandled();
            if (resource != null) {
                String message = getString(resource);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
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
                    String placeId = viewState != null ? viewState.getCurrentUserChosenRestaurantId() : null;
                    args.putString(RestaurantDetailActivity.PLACE_ID, placeId);
                    navController.navigate(navigateTo, args);
                }
            }
        });
    }

    /**
     * Set mainviewstate livedata observer to be used to set current user details in drawer header
     * and perform proper action about noon reminder feature
     */
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
                        case UNSET_WORKER:
                            cancelWorker();
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
     * Set noon reminder
     */
    private void setWorker() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationsWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(viewModel.getInitialDelay(), TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniqueWork(NOON_REMINDER, ExistingWorkPolicy.REPLACE, workRequest);
    }

    /**
     * Cancel noon reminder
     */
    private void cancelWorker() {
        workManager.cancelUniqueWork(NOON_REMINDER);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * We additionaly create autocomplete search
     *
     * @param menu The options menu in which are placed the items.
     * @return true for the menu to be displayed; false for not to be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.action_search);
        // Get SearchView object.
        SearchView searchView = (SearchView) searchMenu.getActionView();
        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(Color.LTGRAY);
        searchAutoComplete.setTextColor(Color.DKGRAY);
        searchAutoComplete.setDropDownBackgroundResource(R.color.ic_launcher_background);
        //searchAutoComplete.setThreshold(3);

        setSearchViewAndSearchAutocompleteListeners(searchView, searchAutoComplete);

        setSearchViewAndSearchAutocompleteRelatedObservers(searchMenu, searchView, searchAutoComplete);

        return true;
    }

    private void setSearchViewAndSearchAutocompleteListeners(SearchView searchView, SearchView.SearchAutoComplete searchAutoComplete){
        // Toast error message in case location permission is not granted
        searchView.setOnSearchClickListener(v -> {
            if (!locationPermissionGranted) {
                Toast.makeText(MainActivity.this, getString(R.string.places_search_error), Toast.LENGTH_LONG).show();
            }
        });
        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener((adapterView, view, itemIndex, id) -> {
            AutocompleteRestaurantViewState selectedItem = (AutocompleteRestaurantViewState) adapterView.getItemAtPosition(itemIndex);
            onQueryTextChangeEnabled = false;
            searchAutoComplete.setText(selectedItem.toString());
            viewModel.setSelectedItem(selectedItem);
            onQueryTextChangeEnabled = true;
            hideKeyboard();
        });
        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (onQueryTextChangeEnabled) {
                    if (viewModel.getSelectedItem().getValue() != null) {
                        viewModel.setSelectedItem(null);
                    }
                    viewModel.setPlaceAutocompleteSearchText(newText);
                }
                return false;
            }
        });
        // Reset when closing
        searchView.setOnCloseListener(() -> {
            viewModel.setPlaceAutocompleteSearchText(null);
            return false;
        });
    }

    private void setSearchViewAndSearchAutocompleteRelatedObservers(MenuItem searchMenu,
                                                                    SearchView searchView,
                                                                    SearchView.SearchAutoComplete searchAutoComplete
    ){
        // Create a new ArrayAdapter and add data to search auto complete object.
        viewModel.getAutocompleteRestaurantArray().observe(this, restaurantArray -> {
            if (restaurantArray == null) {
                restaurantArray = new AutocompleteRestaurantViewState[0];
            } else {
                int initialLength = restaurantArray.length;
                restaurantArray = Arrays.copyOf(restaurantArray, initialLength + 1);
                restaurantArray[initialLength] = new AutocompleteRestaurantViewState(getString(R.string.places_powered_by_google), "");
            }
            ArrayAdapter<AutocompleteRestaurantViewState> arrayAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, restaurantArray);
            searchAutoComplete.setAdapter(arrayAdapter);
            if (restaurantArray.length > 0) {
                searchAutoComplete.showDropDown();
            } else {
                searchAutoComplete.dismissDropDown();
            }
        });
        // Manage search autocomplete enabling depending on location permission
        viewModel.getLocationPermissionGranted().observe(this, aBoolean -> {
            if (aBoolean != null) {
                searchAutoComplete.setEnabled(aBoolean);
                searchMenu.setEnabled(aBoolean);
            }
        });
        // Manage search autocomplete enabling depending on current fragment
        viewModel.getCurrentFragment().observe(this, currentFragment -> {
            boolean isEnabled = true;
            if (currentFragment.equals(WORKMATES)) {
                isEnabled = false;
                searchView.invalidate();
            }
            searchAutoComplete.setEnabled(isEnabled);
            searchMenu.setEnabled(isEnabled);
        });
    }

    /**
     * Hides the soft keyboard
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    /**
     * Getter to allow each fragment to pass mainviewmodel to its own viewmodel
     *
     * @return MainViewModel current instance
     */
    public MainViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}