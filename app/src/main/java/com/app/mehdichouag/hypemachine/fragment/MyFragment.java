package com.app.mehdichouag.hypemachine.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.mehdichouag.hypemachine.DividerItemDecoration;
import com.app.mehdichouag.hypemachine.R;
import com.app.mehdichouag.hypemachine.RecyclerAdapter;
import com.app.mehdichouag.hypemachine.RefreshData;
import com.app.mehdichouag.hypemachine.database.DataBaseList;
import com.app.mehdichouag.hypemachine.models.TrackModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mehdichouag on 18/10/14.
 */
public class MyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View mView;
    private ProgressBar mProgress;
    private Activity mActivity;
    private RecyclerView mRecycler;
    private DataBaseList mDataBase;
    private SwipeRefreshLayout mSwipe;
    private RecyclerAdapter mItems;
    private RecyclerView.LayoutManager mLayoutManager;


    public MyFragment() {
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my, container, false);
        mProgress = (ProgressBar)mView.findViewById(R.id.progressBar);
        mActivity = getActivity();
        mRecycler = (RecyclerView)mView.findViewById(R.id.my_recycler_view);
        mSwipe = (SwipeRefreshLayout)mView.findViewById(R.id.swipe_container);
        mSwipe.setOnRefreshListener(this);
        mRecycler.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST);
        mRecycler.addItemDecoration(itemDecoration);
        mDataBase = new DataBaseList(mActivity);
        if (isOnline() && mDataBase.getCount() == 0)
            new FetchData().execute();
        else if (mDataBase.getCount() > 0)
            getDataFromDataBase();
        else
            notifyUserConnection(true);
        return mView;
    }

    private void getDataFromDataBase()
    {
        mItems = new RecyclerAdapter(mDataBase.getAllTrack(), mActivity);
        mRecycler.setAdapter(mItems);
    }

    private void notifyUserConnection(final boolean close){
        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
        alertDialog.setTitle("Internet");
        alertDialog.setMessage("Check your internet connection to fetch data");
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (close)
                    mActivity.finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onRefresh() {
        if (mProgress.getVisibility() != View.GONE)
            mSwipe.setRefreshing(false);
        if (!isOnline()){
            mSwipe.setRefreshing(false);
            notifyUserConnection(false);
        }
        else{
            new RefreshData(mSwipe, mDataBase, mItems).execute();
        }

    }

    private class FetchData extends AsyncTask<Void, Void, List<TrackModel>> {

        private static final String URL_JSON = "http://hypem.com/playlist/loved/MehdiChouag/json/1/data.js";
        private static final String TAG = "FetchData";
        private List<TrackModel> posts = null;

        @Override
        protected void onPreExecute(){
            mProgress.setVisibility(View.VISIBLE);
        }
        @Override
        protected List<TrackModel> doInBackground(Void... params) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(URL_JSON);

                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    try {
                        Reader reader = new InputStreamReader(content);
                        posts = parseData(reader);
                        content.close();
                        return posts;
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse JSON due to: " + ex);
                    }
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                }
            } catch (ClientProtocolException e) {
                Log.e(TAG, "Client " + e);
            } catch (IOException e) {
                Log.e(TAG, "IO " + e);
            }
            return posts;
        }

        private List<TrackModel> parseData(Reader reader) throws IOException, JSONException {
            BufferedReader json = new BufferedReader(reader);
            StringBuffer buffer = new StringBuffer();
            String line;
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            JsonParser parser = new JsonParser();
            JsonObject obj;
            List<TrackModel> posts = new ArrayList<TrackModel>();
            JSONObject jsonObject;

            while ((line = json.readLine()) != null) {
                buffer.append(line);
            }
            line = buffer.toString();
            if (buffer.length() == 0) {
                return null;
            }
            jsonObject = new JSONObject(line);
            obj = parser.parse(line).getAsJsonObject();
            for (int i = 0; i != jsonObject.length() - 1; i++){
                TrackModel model = gson.fromJson(obj.getAsJsonObject(String.valueOf(i)), TrackModel.class);
                posts.add(model);
            }
            reader.close();
            return posts;
        }
        protected void onPostExecute(List<TrackModel> models){
            mProgress.setVisibility(View.GONE);
            if (models != null){
                for (TrackModel model : models){
                    mDataBase.addListItem(model);
                }
                mItems = new RecyclerAdapter(mDataBase.getAllTrack(), mActivity);
                mRecycler.setAdapter(mItems);
            }
            else
                Toast.makeText(getActivity(), String.valueOf("null"), Toast.LENGTH_SHORT).show();
        }
    }
}
