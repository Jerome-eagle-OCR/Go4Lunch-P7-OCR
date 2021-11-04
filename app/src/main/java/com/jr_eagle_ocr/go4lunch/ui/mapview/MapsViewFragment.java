package com.jr_eagle_ocr.go4lunch.ui.mapview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.repositories.TempUserRestaurantManager;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;

/**
 * @author jrigault
 */
public class MapsViewFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapsViewFragment.class.getSimpleName();
    private GoogleMap map;
//    private CameraPosition cameraPosition;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(48.8057, 2.1323);
    private static final int DEFAULT_ZOOM = 18;

    private boolean locationPermissionGranted;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    // The entry point to the Places API.
    private PlacesClient placesClient;
    // Used for selecting the current place.
    private List<Place.Type> placeTypes;
    private FindCurrentPlaceResponse likelyPlaces;

    private final TempUserRestaurantManager tempUserRestaurantManager = TempUserRestaurantManager.getInstance();
    private Map<String, Restaurant> foundRestaurants;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapsview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tempUserRestaurantManager.getFoundRestaurantsLiveData().observe(this.requireActivity(), foundRestaurants ->
                this.foundRestaurants = foundRestaurants);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Construct a PlacesClient
        Places.initialize(requireContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(requireContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));

        getPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        //Go to detail activity when marker info balloon is clicked
        map.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            String restaurantId = tag != null ? tag.toString() : null;
            Intent intent = RestaurantDetailActivity.navigate(requireActivity(), restaurantId);
            startActivity(intent);
        });

        map.setOnCameraMoveListener(() -> {
            //TODO: could refresh map with
            // refreshMap();
        });
    }

    /**
     * Prompts the user for permission to use the device location and handles the result
     */
    private final ActivityResultLauncher<String> getPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        locationPermissionGranted = true;
//                        LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
//                        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 50, MapsViewFragment.this::onLocationChanged);
                        refreshMap();
                    } else {
                        locationPermissionGranted = false;
                        new AppSettingsDialog.Builder(requireActivity()).build().show();
                    }
                }
            });


    /**
     * Call methods to successively update UI, location and places
     */
    private void refreshMap() {
        updateLocationUI(); //Turn on the My Location layer and the related control on the map.
        getDeviceLocation(); //Get the current location of the device and set the position of the map.
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            tempUserRestaurantManager.setLocation(lastKnownLocation);
                            showCurrentPlaces(); //Get places around, filter only restaurants and add them to the list
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void showCurrentPlaces() {
        if (map == null) return;

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.ID, Place.Field.NAME,
                    Place.Field.LAT_LNG, Place.Field.TYPES);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    likelyPlaces = task.getResult();

//                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                    for (int i = 0; i < 1; i++) {
                        PlaceLikelihood placeLikelihood = likelyPlaces.getPlaceLikelihoods().get(0);
                        Place place = placeLikelihood.getPlace();
                        placeTypes = place.getTypes();
                        String placeId = place.getId();

                        if (placeId != null && placeTypes.contains(Place.Type.RESTAURANT)) {
                            // Add a marker on the map for each restaurant with green color for those chosen by other users
                            tempUserRestaurantManager.getChosenRestaurantIds().observe(this, chosenPlaceIds -> {
                                // clear the map so remove all markers
                                map.clear();
                                // set color according to place chosen or not
                                int orange = getResources().getColor(android.R.color.holo_orange_dark);
                                int green = getResources().getColor(android.R.color.holo_green_dark);
                                int color = orange;
                                if (chosenPlaceIds != null) {
                                    color = (chosenPlaceIds.contains(placeId)) ? green : orange;
                                }
                                // create and add marker
                                Marker marker =
                                        map.addMarker(new MarkerOptions()
                                                .position(Objects.requireNonNull(placeLikelihood.getPlace().getLatLng()))
                                                .title(placeLikelihood.getPlace().getName())
                                                .icon(drawableToBitmap(R.drawable.resto_pin, color)));
                                if (marker != null) marker.setTag(place.getId());
                            });

                            if (foundRestaurants != null && !foundRestaurants.containsKey(placeId)) {
                                // Create new restaurant with place id
                                Restaurant restaurant = new Restaurant(placeId);

                                List<Place.Field> detailFields = Arrays.asList(
                                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                                        Place.Field.ADDRESS, Place.Field.PHONE_NUMBER,
                                        Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI,
                                        Place.Field.RATING, Place.Field.PHOTO_METADATAS);

                                // Construct a request object, passing the place ID and fields array for details.
                                final FetchPlaceRequest detailRequest = FetchPlaceRequest.newInstance(placeId, detailFields);

                                placesClient.fetchPlace(detailRequest).addOnSuccessListener((response) -> {
                                    Place detailPlace = response.getPlace();
                                    Log.i(TAG, "Place found: " + detailPlace.getName());

                                    // Set restaurant details
                                    restaurant.setName(detailPlace.getName());
                                    restaurant.setLatLng(detailPlace.getLatLng());
                                    restaurant.setAddress(detailPlace.getAddress());
                                    restaurant.setPhoneNumber(detailPlace.getPhoneNumber());
                                    restaurant.setOpeningHours(detailPlace.getOpeningHours());
                                    restaurant.setRating(detailPlace.getRating());
                                    Uri uri = detailPlace.getWebsiteUri();
                                    if (uri != null) restaurant.setWebSiteUrl(uri.toString());

                                    // Get the photo metadata.
                                    final List<PhotoMetadata> metadata = detailPlace.getPhotoMetadatas();
                                    if (metadata == null || metadata.isEmpty()) {
                                        Log.w(TAG, "No photo metadata.");
                                        return;
                                    }
                                    final PhotoMetadata photoMetadata = metadata.get(0);

                                    // Get the attribution text.
                                    final String attributions = photoMetadata.getAttributions();

                                    // Create a FetchPhotoRequest.
                                    final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                            .setMaxWidth(500) // Optional.
                                            .setMaxHeight(300) // Optional.
                                            .build();
                                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                                        Bitmap bitmap = fetchPhotoResponse.getBitmap();

                                        // Set restaurant photo
                                        restaurant.setPhoto(bitmap);
                                    }).addOnFailureListener((exception) -> {
                                        if (exception instanceof ApiException) {
                                            final ApiException apiException = (ApiException) exception;
                                            Log.e(TAG, "Place not found: " + exception.getMessage());
                                            final int statusCode = apiException.getStatusCode();
                                            // TODO: Handle error with given status code.
                                        }
                                    });

                                }).addOnFailureListener((exception) -> {
                                    if (exception instanceof ApiException) {
                                        final ApiException apiException = (ApiException) exception;
                                        Log.e(TAG, "Place not found: " + exception.getMessage());
                                        final int statusCode = apiException.getStatusCode();
                                        // TODO: Handle error with given status code.
                                    }
                                });

                                tempUserRestaurantManager.addFoundRestaurant(restaurant);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
        }
    }

    private BitmapDescriptor drawableToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = null;
        if (drawable != null) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            DrawableCompat.setTint(drawable, color);
            drawable.draw(canvas);
        }
        if (bitmap != null) {
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }
}