package com.harsh.mapdemo.loader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Harsh Rastogi  on 7/27/2017.
 */

public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imgViewRef;

    public ImageLoaderTask(WeakReference<ImageView> imgViewRef) {
        this.imgViewRef = imgViewRef;
    }


    @Override
    protected Bitmap doInBackground(String... strings) {
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) {
            imgViewRef.get().setImageBitmap(bitmap);
        }
    }
}
