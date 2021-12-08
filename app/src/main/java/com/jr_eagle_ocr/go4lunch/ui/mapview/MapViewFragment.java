package com.jr_eagle_ocr.go4lunch.ui.mapview;

import static com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel.DRAWABLE_RESOURCE;
import static com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel.LATLNG;
import static com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel.NAME;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.jr_eagle_ocr.go4lunch.data.models.FoundRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;
import com.jr_eagle_ocr.go4lunch.ui.ViewModelFactory;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;

/**
 * @author jrigault
 */
public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = MapViewFragment.class.getSimpleName();
    private GoogleMap map;
//    private CameraPosition cameraPosition;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(48.8057, 2.1323);
    private static final float DEFAULT_ZOOM = 18.5f;

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

    private MapViewViewModel viewModel;
    private Map<String, Restaurant> allRestaurants;
    private final Map<String, Marker> markers = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MapViewViewModel.class);

        viewModel.getAllRestaurants().observe(getViewLifecycleOwner(), allRestaurants ->
                this.allRestaurants = allRestaurants);

        // Retrieve and set mainviewmodel in mapviewmodel to observe filtered place from search feature
        MainViewModel mainViewModel = ((MainActivity) requireActivity()).getViewModel();
        viewModel.setMainViewModel(mainViewModel);
        // Observe the search result selected item to activate marker
        viewModel.getSelectedItem().observe(getViewLifecycleOwner(), selectedItem -> {
            if (selectedItem != null) {
                Marker marker = markers.get(selectedItem.getPlaceId());
                if (marker != null) {
                    marker.showInfoWindow();
                }
            }
        });

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
                    locationPermissionGranted = result;
                    viewModel.setLocationPermissionGranted(result);
                    if (result) {
//                        LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
//                        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 50, MapViewFragment.this::onLocationChanged);
                        refreshMap();
                    } else {
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
                            viewModel.setLocation(lastKnownLocation); //TODO: use location repo
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

    /**
     *
     */
    private void showCurrentPlaces() {
        if (map == null) return;

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.ID, Place.Field.TYPES);

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

                    List<String> foundRestaurantIds = new ArrayList<>();
                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
//                    for (int i = 0; i < 1; i++) {
//                        PlaceLikelihood placeLikelihood = likelyPlaces.getPlaceLikelihoods().get(0);
                        Place place = placeLikelihood.getPlace();
                        placeTypes = place.getTypes();
                        String placeId = place.getId();

                        if (placeId != null && placeTypes.contains(Place.Type.RESTAURANT)) {
                            foundRestaurantIds.add(placeId);

                            // Fetch only if not already available in Firestore "restaurants" collection
                            if (allRestaurants != null && !allRestaurants.containsKey(placeId)) {
                                // Create new foundRestaurant with place id
                                FoundRestaurant foundRestaurant = new FoundRestaurant(placeId);

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

                                    // Set foundRestaurant details
                                    foundRestaurant.setName(detailPlace.getName());
                                    foundRestaurant.setAddress(detailPlace.getAddress());
                                    foundRestaurant.setLatLng(detailPlace.getLatLng());
                                    foundRestaurant.setRating(detailPlace.getRating());
                                    foundRestaurant.setOpeningHours(detailPlace.getOpeningHours());
                                    foundRestaurant.setPhoneNumber(detailPlace.getPhoneNumber());
                                    Uri uri = detailPlace.getWebsiteUri();
                                    if (uri != null) foundRestaurant.setWebSiteUrl(uri.toString());

                                    // Get the photo metadata.
                                    final List<PhotoMetadata> metadata = detailPlace.getPhotoMetadatas();
                                    if (metadata == null || metadata.isEmpty()) {
                                        Log.w(TAG, "No photo metadata.");
                                        // Add foundRestaurant in found restaurants without photo
                                        setPhotoAndAddFoundRestaurant(null, foundRestaurant);
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

                                        // Add foundRestaurant in found restaurants with fetched photo
                                        setPhotoAndAddFoundRestaurant(bitmap, foundRestaurant);
                                    }).addOnFailureListener((exception) -> {
                                        if (exception instanceof ApiException) {
                                            final ApiException apiException = (ApiException) exception;
                                            Log.e(TAG, "Place not found: " + exception.getMessage());
                                            final int statusCode = apiException.getStatusCode();
                                            // TODO: Handle error with given status code.
                                            // Add foundRestaurant in found restaurants without photo
                                            setPhotoAndAddFoundRestaurant(null, foundRestaurant);
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
                            }
                        }
                    }
                    viewModel.setFoundRestaurantIds(foundRestaurantIds);
                } else {
                    Log.e(TAG, Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
        }
        setMarkerDetailsObserver();
    }

    /**
     * @param bitmap
     * @param foundRestaurant
     */
    private void setPhotoAndAddFoundRestaurant(@Nullable Bitmap bitmap, @NonNull FoundRestaurant foundRestaurant) {
        foundRestaurant.setPhoto(bitmap);
        viewModel.addFoundRestaurant(foundRestaurant);
    }

    /**
     *
     */
    private void setMarkerDetailsObserver() {
        viewModel.getMarkerDetails().observe(getViewLifecycleOwner(), markerDetails -> {
            // clear the map so remove all markers
            map.clear();
            // get details and add marker for each restaurant with green color for those chosen by other users
            for (Map.Entry<String, Map<String, Object>> entrySet : markerDetails.entrySet()) {
                String id = entrySet.getKey();
                Map<String, Object> details = entrySet.getValue();
                // place name for marker's title
                String name = (String) details.get(NAME);
                // place position
                LatLng latLng = (LatLng) details.get(LATLNG);
                // color according to place chosen or not
                Integer drawableResource = (Integer) details.get(DRAWABLE_RESOURCE);
                int resource = drawableResource != null ? drawableResource : R.drawable.orange_marker;
                BitmapDescriptor colorMarker = BitmapDescriptorFactory.fromResource(resource);

                // add marker and set its tag to place id (used for intent extra)
                if (latLng != null) {
                    Marker marker =
                            map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .icon(colorMarker));
                    if (marker != null) marker.setTag(id);
                    markers.put(id, marker);
                }
            }
        });
    }
}