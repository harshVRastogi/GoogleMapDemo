package com.harsh.mapdemo.common;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.harsh.mapdemo.model.Post;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Harsh Rastogi on 7/28/2017.
 */

public final class MapUtil {

    private static final float RADIUS_OF_EARTH = 6372.8f;//in kms

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
}
