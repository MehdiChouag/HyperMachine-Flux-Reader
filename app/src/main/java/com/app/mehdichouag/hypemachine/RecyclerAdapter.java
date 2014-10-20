package com.app.mehdichouag.hypemachine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.mehdichouag.hypemachine.activity.DetailActivity;
import com.app.mehdichouag.hypemachine.database.DataBaseList;
import com.app.mehdichouag.hypemachine.models.TrackModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mehdichouag on 19/10/14.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.SimpleItemViewHolder>{
    private List<TrackModel> items;
    private Activity mActivity;
    private DataBaseList mDataBase;

    public RecyclerAdapter(List<TrackModel> items, Activity activity) {
        this.items = items;
        this.mActivity = activity;
        mDataBase = new DataBaseList(activity);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.recycler_view, viewGroup, false);
        return new SimpleItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final SimpleItemViewHolder viewHolder, int position) {
        final TrackModel item = items.get(position);
        viewHolder.title.setText(item.artist + " - " + item.title);
        viewHolder.love.setText("  " + String.valueOf(item.love));
        if (item.getmImage() == null){
            Picasso.with(mActivity).load(item.imageUrl).into(viewHolder.image, new Callback() {
                @Override
                public void onSuccess() {

                    item.setmImage(drawableToBitmap(viewHolder.image.getDrawable()));
                    mDataBase.updateImageItem(item);
                }

                @Override
                public void onError() {

                }
            });
        }
        else
            viewHolder.image.setImageBitmap(item.getmImage());
        viewHolder.activity = mActivity;
        viewHolder.setTrackModel(item);
    }

    private Bitmap drawableToBitmap(Drawable drawable){
        return ((BitmapDrawable)drawable).getBitmap();
    }

    public void add(TrackModel item, int position) {
        items.add(position, item);
        notifyItemInserted(position);

    }

    public void refreshList(){
        items = mDataBase.getAllTrack();
        notifyDataSetChanged();
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener{
        public final static String TAG_ID = "id";
        TextView title;
        TextView love;
        ImageView image;
        Activity activity;
        private TrackModel mItem;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            love = (TextView)itemView.findViewById(R.id.love_count);
            image = (ImageView)itemView.findViewById(R.id.image_list);

        }
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, DetailActivity.class);
            intent.putExtra(TAG_ID, mItem.id);
            activity.startActivity(intent);
        }

        public void setTrackModel(TrackModel model){
            mItem = model;
        }

    }
}
