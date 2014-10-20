package com.app.mehdichouag.hypemachine;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

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
 * Created by mehdichouag on 20/10/14.
 */

public class RefreshData extends AsyncTask<Void, Void, Void> {

    private static final String URL_JSON = "http://hypem.com/playlist/loved/MehdiChouag/json/1/data.js";
    private static final String TAG = "RefreshData";
    private SwipeRefreshLayout mSwipe;
    private DataBaseList mDataBase;
    private boolean mModify = false;
    private RecyclerAdapter mAdapter;

    public RefreshData(SwipeRefreshLayout swipe, DataBaseList database, RecyclerAdapter adapter){
        super();
        this.mSwipe = swipe;
        this.mDataBase = database;
        this.mAdapter = adapter;
    }


    @Override
    protected Void doInBackground(Void... params) {
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
                    UpdateDataFromInternet(parseData(reader));
                    content.close();
                    return null;
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
        return null;
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

    private void UpdateDataFromInternet(List<TrackModel> iModels) {
        List<TrackModel> dModels = mDataBase.getAllTrack();
        boolean find;
        boolean update;
        int love = 0;

        for (TrackModel itemBase : dModels)
        {
            find = false;
            update = false;
            for (TrackModel itemInternet : iModels){
                if (itemBase.postId == itemInternet.postId){
                    find = true;
                    if (itemBase.love != itemInternet.love){
                        update = true;
                        love = itemInternet.love;
                        mModify = true;
                    }
                    break;
                }
            }
            if (!find){
                mDataBase.deleteFromPostId(itemBase.postId);
                mModify = true;
            }
            else if (update){
                mDataBase.updateLovedCount(itemBase.postId, love);
            }
        }
        addData(dModels, iModels);
    }

    private void addData(List<TrackModel> models, List<TrackModel> item){

        List<Integer> array = new ArrayList<Integer>();

        for (TrackModel dModel : models) {
            array.add(dModel.postId);
        }

        for (TrackModel iModel : item) {
            if (!isAdd(array, iModel.postId)){
                array.add(iModel.postId);
                mDataBase.addListItem(iModel);
                mModify = true;
            }
        }
    }

    private boolean isAdd(List<Integer> array, int id){
        for (Integer i : array){
            if (i == id)
                return true;
        }
        return false;
    }

    protected void onPostExecute(Void param){
        mSwipe.setRefreshing(false);
        if (mModify)
            mAdapter.refreshList();
    }
}