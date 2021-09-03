package com.jr_eagle_ocr.go4lunch.ui.mapview;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jr_eagle_ocr.go4lunch.R;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;

public class MapsViewFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapsViewFragment.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The entry point to the Places API.
    private PlacesClient placesClient;

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

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 20;
    private List<Place.Type> placeTypes;
//    private String[] likelyPlaceNames;
//    private String[] likelyPlaceAddresses;
//    private List[] likelyPlaceAttributions;
//    private LatLng[] likelyPlaceLatLngs;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        map.setOnMarkerClickListener(this);
    }

    /**
     * Prompts the user for permission to use the device location and handles the result
     */
    private final ActivityResultLauncher<String> getPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        locationPermissionGranted = true;
                        updateLocationUI(); //Turn on the My Location layer and the related control on the map.
                        getDeviceLocation(); //Get the current location of the device and set the position of the map.
                        showCurrentPlaces();
                    } else {
                        locationPermissionGranted = false;
                        new AppSettingsDialog.Builder(requireActivity()).build().show();
                    }
                }
            });

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
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
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
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();

//                    // Set the count, handling cases where less than x entries are returned.
//                    int count;
//                    if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
//                        count = likelyPlaces.getPlaceLikelihoods().size();
//                    } else {
//                        count = M_MAX_ENTRIES;
//                    }

                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
//                        int i = 0;
                        placeTypes = placeLikelihood.getPlace().getTypes();
                        if (placeTypes.contains(Place.Type.RESTAURANT)) {
//                            int color;
//                            if (user.getChoice == placeLikelihood.getPlace()) {
//                                color = getResources().getColor(android.R.color.holo_green_dark);
//                            } else {
//                                color = getResources().getColor(android.R.color.holo_orange_dark);
//                            }

                            map.addMarker(new MarkerOptions()
                                    .position(Objects.requireNonNull(placeLikelihood.getPlace().getLatLng()))
                                    .title(placeLikelihood.getPlace().getName())
                                    .icon(vectorToBitmap(R.drawable.resto_pin,
                                            getResources().getColor(android.R.color.holo_orange_dark))));

//                            i++;
//                            if (i > (count - 1)) {
//                                break;
//                            }
                        }
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
        }
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(requireActivity(), marker.getTitle() + " will be detailed soon !", Toast.LENGTH_SHORT).show();
        return false;
    }


//    private void showCurrentPlace() {
//        if (map == null) {
//            return;
//        }
//
//        if (locationPermissionGranted) {
//            // Use fields to define the data types to return.
//            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
//                    Place.Field.LAT_LNG);
//
//            // Use the builder to create a FindCurrentPlaceRequest.
//            FindCurrentPlaceRequest request =
//                    FindCurrentPlaceRequest.newInstance(placeFields);
//
//            // Get the likely places - that is, the businesses and other points of interest that
//            // are the best match for the device's current location.
//            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
//                    placesClient.findCurrentPlace(request);
//            placeResult.addOnCompleteListener(task -> {
//                if (task.isSuccessful() && task.getResult() != null) {
//                    FindCurrentPlaceResponse likelyPlaces = task.getResult();
//
//                    // Set the count, handling cases where less than 5 entries are returned.
//                    int count;
//                    if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
//                        count = likelyPlaces.getPlaceLikelihoods().size();
//                    } else {
//                        count = M_MAX_ENTRIES;
//                    }
//
//                    int i = 0;
//                    likelyPlaceNames = new String[count];
//                    likelyPlaceAddresses = new String[count];
//                    likelyPlaceAttributions = new List[count];
//                    likelyPlaceLatLngs = new LatLng[count];
//
//                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
//                        // Build a list of likely places to show the user.
//                        likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
//                        likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
//                        likelyPlaceAttributions[i] = placeLikelihood.getPlace()
//                                .getAttributions();
//                        likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
////                        if (placeLikelihood.getPlace().getTypes().contains(Place.Type.RESTAURANT))
//
//                        i++;
//                        if (i > (count - 1)) {
//                            break;
//                        }
//                    }
//
//                    // Show a dialog offering the user the list of likely places, and add a
//                    // marker at the selected place.
//                    MapsViewFragment.this.openPlacesDialog();
//                } else {
//                    Log.e(TAG, "Exception: %s", task.getException());
//                }
//            });
//        } else {
//            // The user has not granted permission.
//            Log.i(TAG, "The user did not grant location permission.");
//
//            // Add a default marker, because the user hasn't selected a place.
//            map.addMarker(new MarkerOptions()
//                    .title(getString(R.string.default_info_title))
//                    .position(defaultLocation)
//                    .snippet(getString(R.string.default_info_snippet)));
//
//            // Prompt the user for permission.
////            getLocationPermission();
//        }
//    }

//    /**
//     * Displays a form allowing the user to select a place from a list of likely places.
//     */
//    private void openPlacesDialog() {
//        // Ask the user to choose the place where they are now.
//        DialogInterface.OnClickListener listener = (dialog, which) -> {
//            // The "which" argument contains the position of the selected item.
//            LatLng markerLatLng = likelyPlaceLatLngs[which];
//            String markerSnippet = likelyPlaceAddresses[which];
//            if (likelyPlaceAttributions[which] != null) {
//                markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
//            }
//
//            // Add a marker for the selected place, with an info window
//            // showing information about that place.
//            map.addMarker(new MarkerOptions()
//                    .title(likelyPlaceNames[which])
//                    .position(markerLatLng)
//                    .snippet(markerSnippet));
//
//            // Position the map's camera at the location of the marker.
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
//                    DEFAULT_ZOOM));
//        };
//
//        // Display the dialog.
//        AlertDialog dialog = new AlertDialog.Builder(requireContext())
//                .setTitle(R.string.pick_place)
//                .setItems(likelyPlaceNames, listener)
//                .show();
//    }
}