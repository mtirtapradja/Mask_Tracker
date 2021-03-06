package mtirtapradja.project.huawei.masktracker.Fragments;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;

import mtirtapradja.project.huawei.masktracker.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = HistoryFragment.class.getSimpleName();
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";


    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LatLng currPosition;
    private ArrayList<LatLng> LatLngArrayList;
    private MapView mMapView;
    private Marker mMarker;
    private HuaweiMap hMap;

    private TextView noLocationHistory;

    private HomeFragment homeFragment = new HomeFragment();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        currPosition = homeFragment.getCurrPosition();
        Log.d(TAG, "pos onCreate: " + currPosition);
        LatLngArrayList = homeFragment.getMapLatLng();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        // Inflate the layout for this fragment
        noLocationHistory = view.findViewById(R.id.noDataText);
        mMapView = view.findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        Log.d(TAG, "onCreateView: ");
        dynamicPermission();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        getCurrentLocation();
        return view;
    }

    private void dynamicPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "android sdk <= 28 Q");
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), strings, 1);
            }
        } else {
            // Dynamically apply for required permissions if the API level is greater than 28. The android.permission.ACCESS_BACKGROUND_LOCATION permission is required.
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(getActivity(), strings, 2);
            }
        }
    }

    private void getCurrentLocation() {
        mLocationRequest = new LocationRequest();
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        mLocationRequest = new LocationRequest();
        // Set the location update interval (in milliseconds).
        mLocationRequest.setInterval(10000);
        // Set the location type.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    double latitude = locationResult.getLastLocation().getLatitude();
                    double longitude = locationResult.getLastLocation().getLongitude();
                    // Process the location callback result.
                    currPosition = new LatLng(latitude, longitude);
                    Log.d(TAG, "onLocationResult: " + currPosition);
                    stopTracking();
                }
            }
        };
        // Check the device location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                // Define callback for success in checking the device location settings.
                .addOnSuccessListener(locationSettingsResponse -> {
                    // Initiate location requests when the location settings meet the requirements.
                    fusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                            // Define callback for success in requesting location updates.
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Getting your location"));
                })
                // Define callback for failure in checking the device location settings.
                .addOnFailureListener(e -> {
                    // Device location settings do not meet the requirements.
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                // Call startResolutionForResult to display a pop-up asking the user to enable related permission.
                                rae.startResolutionForResult(getActivity(), 0);
                            } catch (IntentSender.SendIntentException sie) {
                                // ...
                            }
                            break;
                    }
                });
    }

    private void stopTracking() {
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                // Define callback for success in stopping requesting location updates.
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // ...
                        Log.d(TAG, "Location update removes");
                    }
                })
                // Define callback for failure in stopping requesting location updates.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // ...
                        Log.d(TAG, "Failed to remove location update");
                    }
                });
    }

    private void moveCameraAndAddMarker() {
        Log.d(TAG, "moveCameraAndAddMarker: masuk");

        if (LatLngArrayList.isEmpty()) {
            mMapView.setVisibility(View.INVISIBLE);
        } else {
            noLocationHistory.setVisibility(View.INVISIBLE);
            for (LatLng position : LatLngArrayList) {
                Log.d(TAG, "pos " + position.latitude + " " + position.longitude);

                String reason = homeFragment.getChangeMaskReason();

                MarkerOptions options = new MarkerOptions()
                        .position(new LatLng(position.latitude, position.longitude))
                        .title(reason == null ? "You don't specify your reason" : reason)
                        .snippet("Changed at " + homeFragment.getChangeMaskTime());
                mMarker = hMap.addMarker(options);
            }

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currPosition, 15f);
            hMap.animateCamera(cameraUpdate);
        }

    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hMap = huaweiMap;
        moveCameraAndAddMarker();
    }
}