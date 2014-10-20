package com.app.mehdichouag.hypemachine.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mehdichouag on 18/10/14.
 */
public class TrackModel
{
    @Expose
    public String artist;
    @Expose
    public String title;
    @Expose
    public String description;
    @SerializedName("thumb_url_large")
    @Expose public String imageUrl;
    @SerializedName("dateposted")
    @Expose public int date;
    @SerializedName("loved_count")
    @Expose public int love;
    @SerializedName("postid")
    @Expose public int postId;
    @SerializedName("dateloved")
    @Expose public int dateLoved = 10;
    public int id;

    private Bitmap mImage = null;

    public TrackModel()
    {}

    public Bitmap getmImage() {
        return mImage;
    }

    public void setmImage(Bitmap mImage) {
        this.mImage = mImage;
    }
}
