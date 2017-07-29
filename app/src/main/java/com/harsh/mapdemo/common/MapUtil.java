package com.harsh.mapdemo.common;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.harsh.mapdemo.R;
import com.harsh.mapdemo.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.internal.zzt.TAG;

/**
 * Created by Harsh Rastogi on 7/28/2017.
 */

public final class MapUtil {

    private static final float RADIUS_OF_EARTH = 6372.8f;//in kms
    public static final Object mLock = new Object();

    public static float calDistance(LatLng l1, LatLng l2) {
        final double lat1 = l1.latitude;
        final double lng1 = l1.longitude;
        final double lat2 = l2.latitude;
        final double lng2 = l2.longitude;
        final double deltaLat = toRads(lat2 - lat1);
        final double deltaLng = toRads(lng2 - lng1);

        final double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(toRads(lat1)) * Math.cos(toRads(lat2))
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (RADIUS_OF_EARTH * c);
    }

    private static double toRads(double v) {
        return Math.toRadians(v);
    }

    public static List<Post> sortByDistance(Location userLocation, List<Post> data) {
        final LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        for (Post post : data) {
            LatLng postLatLng = new LatLng(post.getLocation().getLatitude(),
                    post.getLocation().getLongitude());
            float distance = calDistance(userLatLng, postLatLng);
            post.setDistance(distance);
        }
        Collections.sort(data, DISTANCE_COMPARATOR);
        return data;
    }

    public static Comparator<Post> DISTANCE_COMPARATOR = new Comparator<Post>() {
        @Override
        public int compare(Post post, Post t1) {
            return new Float(post.getDistance())
                    .compareTo(new Float(t1.getDistance()));
        }
    };

    private static Map<Post, String> postMap = new HashMap<>();

    static class WorkerThread implements Runnable {
        private final Geocoder geocoder;
        private final Post post;

        WorkerThread(Geocoder geocoder, Post post) {
            this.geocoder = geocoder;
            this.post = post;
        }

        @Override
        public void run() {
            String locality = getLocality(post.getLocation());
            postMap.put(post, locality);
        }

        private String getLocality(Post.PostLocation postLocation) {
            final Location location = new Location("");
            location.setLatitude(postLocation.getLatitude());
            location.setLongitude(postLocation.getLongitude());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                return null;
            } catch (IllegalArgumentException illegalArgumentException) {
                return null;
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size() == 0) {
                return null;
            } else {
                Address address = addresses.get(0);
                return TextUtils.isEmpty(address.getLocality()) ? address.getAdminArea() : address.getLocality();
            }
        }
    }

    public static class LocalityLoader extends Thread {
        private final Context context;
        private Geocoder geocoder;
        private final List<Post> data;
        private final OnLocalityLoadListener l;
        public LocalityLoader(Context context, List<Post> data, OnLocalityLoadListener l) {
            this.context = context;
            this.data = data;
            this.l = l;
            this.geocoder = new Geocoder(context, Locale.getDefault());
        }

        private void load() throws InterruptedException {
            BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(data.size(), true, new ArrayBlockingQueue<Runnable>(data.size()));
            ThreadPoolExecutor executor = new ThreadPoolExecutor(data.size(), data.size(), 30, TimeUnit.SECONDS, blockingQueue);
            for (Post post : data) {
                executor.execute(new WorkerThread(geocoder, post));
            }
            while (executor.getTaskCount() != executor.getCompletedTaskCount()) {}
            System.out.println(new Gson().toJson(postMap));
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
            if(l != null){
                l.onLocalityLoad(postMap);
            }
        }

        @Override
        public synchronized void start() {
            super.start();
            try {
                load();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnLocalityLoadListener {
        void onLocalityLoad(Map<Post, String> map);
    }
}
