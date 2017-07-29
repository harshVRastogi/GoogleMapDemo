package com.harsh.mapdemo.model;


import com.google.gson.annotations.SerializedName;

/**
 * Created by Harsh Rastogi on 7/28/2017.
 */

public class Post {
    @SerializedName("title")
    private String title;
    @SerializedName("tag")
    private String tag;
    @SerializedName("thumbnail")
    private String thumbnailUrl;
    @SerializedName("location")
    private PostLocation location;

    private String locality;
    private float distance;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public PostLocation getLocation() {
        return location;
    }

    public void setLocation(PostLocation location) {
        this.location = location;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public static class PostLocation extends Object {
        @SerializedName("lan")
        private double latitude;
        @SerializedName("lng")
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PostLocation)) return false;

            PostLocation that = (PostLocation) o;

            if (Double.compare(that.latitude, latitude) != 0) return false;
            return Double.compare(that.longitude, longitude) == 0;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(latitude);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(longitude);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;

        Post post = (Post) o;

        if (Float.compare(post.distance, distance) != 0) return false;
        if (!title.equals(post.title)) return false;
        if (!tag.equals(post.tag)) return false;
        if (!thumbnailUrl.equals(post.thumbnailUrl)) return false;
        return location.equals(post.location);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + tag.hashCode();
        result = 31 * result + thumbnailUrl.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
        return result;
    }
}
