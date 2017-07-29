package com.harsh.mapdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.harsh.mapdemo.model.Post;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harsh Rastogi  on 7/28/2017.
 */


public abstract class AbstractMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnPostChangeListener, GoogleMap.OnCameraMoveListener {

    public static final int REQUEST_CODE_LOCATION = 123;
    public static final int REQUEST_CODE_GPS = 234;

    private String loadJSONFromAsset(String fileName) throws IOException {
        String json = null;
        InputStream is = getAssets().open(fileName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        return json;
    }

    protected final List<Post> loadData(String fileName) throws JSONException {
        String jsonString = null;
        try {
            jsonString = loadJSONFromAsset(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonString == null || TextUtils.isEmpty(jsonString)) {
            throw new JSONException("Data is not valid.");
        }
        JSONArray jsonArray = new JSONArray(jsonString);
        Gson gson = new Gson();
        List<Post> data = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            data.add(gson.fromJson(jsonArray.get(i).toString(), Post.class));
        }
        return data;
    }

    protected final boolean hasLocationPermission(Activity activity) {
        return (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    protected final void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
    }

    protected final Marker addPostMarker(GoogleMap mMap, Post post, Marker postMarker, float zoom) {
        LatLng postLatLng = new LatLng(post.getLocation().getLatitude(),
                post.getLocation().getLongitude());
        if (postMarker != null) {
            postMarker.remove();
        }
        changeCameraPosition(mMap, postLatLng, zoom);
        Marker marker = addMarker(mMap, post);
        marker.showInfoWindow();
        return marker;
    }

    private Marker addMarker(final GoogleMap mMap, final Post post) {
        final LatLng postLatLng = new LatLng(post.getLocation().getLatitude(), post.getLocation().getLongitude());
        return mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(post.getLocality())
                .position(postLatLng));

    }


    private void changeCameraPosition(final GoogleMap mMap, final LatLng latLng, final float zoom) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                mMap.moveCamera(CameraUpdateFactory
                        .newCameraPosition(CameraPosition.fromLatLngZoom(
                                latLng, zoom)));
            }
        });
    }

    protected final void showToast(String message, @IntRange(from = 0, to = 1) int length) {
        Toast.makeText(this, message, length).show();
    }

}
