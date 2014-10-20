package com.app.mehdichouag.hypemachine.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mehdichouag.hypemachine.R;
import com.app.mehdichouag.hypemachine.RecyclerAdapter;
import com.app.mehdichouag.hypemachine.database.DataBaseList;
import com.app.mehdichouag.hypemachine.models.TrackModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends Activity {

    private ImageView mCover;
    private TextView mArtist;
    private TextView mTitle;
    private TextView mLove;
    private TextView mDate;
    private TextView mDescription;
    private DataBaseList mDataBase;
    private TrackModel mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mDataBase = new DataBaseList(this);
        getView();
        getData(getIntent().getIntExtra(RecyclerAdapter.SimpleItemViewHolder.TAG_ID, 0));
    }

    private void getView(){
        mCover = (ImageView)findViewById(R.id.cover_track);
        mArtist = (TextView)findViewById(R.id.artist_track);
        mTitle = (TextView)findViewById(R.id.title_track);
        mLove = (TextView)findViewById(R.id.love_track);
        mDate = (TextView)findViewById(R.id.date_track);
        mDescription = (TextView)findViewById(R.id.description_track);
    }

    private void getData(int i){
        if (i != 0){
            mItem = mDataBase.getTrackById(i);
            setView();
        }
        else
            Toast.makeText(this, "Impossible to get the track", Toast.LENGTH_SHORT).show();
    }

    private void setView(){
        mCover.setImageBitmap(mItem.getmImage());
        mArtist.setText(mItem.artist);
        mTitle.setText(mItem.title);
        mLove.setText("  "  + String.valueOf(mItem.love));
        mDate.setText(convertToDate(mItem.date));
        mDescription.setText(mItem.description);
    }

    private String convertToDate(int date){

        long timeStamp = ((long)date) * 1000;

        try{
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return String.valueOf(date);
        }

    }
}
