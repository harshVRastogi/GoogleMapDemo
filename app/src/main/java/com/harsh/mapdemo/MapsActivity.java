package com.harsh.mapdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.harsh.mapdemo.common.MapUtil;
import com.harsh.mapdemo.model.Post;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

/**
 * Created by Harsh Rastogi on 7/28/2017.
 */
public class MapsActivity extends AbstractMapActivity implements MapUtil.OnLocalityLoadListener {


    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private RecyclerView recyclerView;
    private LocationRequest locationRequest;
    private Marker postMarker;
    private Handler handler;
    private Post post;
    private float zoom = 6f;
    public static final String TAG = "MapsActivity";
    private PostAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        initGoogleApiClient();
        handler = new Handler(Looper.getMainLooper());
    }

    private void showProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting location.");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        locationRequest = new LocationRequest();
        locationRequest.setFastestInterval(10000);
        locationRequest.setInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        if (!hasLocationPermission(this)) {
            requestLocationPermission(this);
        }else {
            turnGPSOn();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION && (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            turnGPSOn();
        }
    }

    private void initPostAdapter(Location currentLocation) {
        List<Post> data;
        try {
            data = loadData("data.json");
        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error parsing json file.", Toast.LENGTH_LONG);
            return;
        }
        List<Post> posts = MapUtil.sortByDistance(currentLocation, data);
        adapter = new PostAdapter(recyclerView, posts);
        recyclerView.setAdapter(adapter);
        adapter.setOnPostChangeListener(this);
        MapUtil.LocalityLoader localityLoader = new MapUtil.LocalityLoader(this, posts, this);
        localityLoader.start();
    }

    private void turnGPSOn() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());

        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    REQUEST_CODE_GPS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        showToast("Can't turn GPS on. Google map won't work.", Toast.LENGTH_LONG);
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
        initPostAdapter(location);
        stopLocationUpdates();
        progressDialog.setMessage("Getting locality.");
    }

    private void startLocationUpdates() {
        try {
            showProgress();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GPS && resultCode == Activity.RESULT_OK) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onPostChange(Post post) {
        if (this.post != null && this.post.equals(post)) {
            return;
        }
        this.post = post;
        postMarker = addPostMarker(mMap, post, postMarker, zoom);
    }

    @Override
    public void onCameraMove() {
        zoom = mMap.getCameraPosition().zoom;
    }

    @Override
    public void onLocalityLoad(final Map<Post, String> map) {

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (adapter == null) {
                    return;
                }
                if (adapter.getData() != null) {
                    List<Post> data = adapter.getData();
                    for (Post post : data) {
                        post.setLocality(map.get(post));
                    }
                    adapter.setData(data);
                }
            }
        });
        progressDialog.dismiss();
    }

}
