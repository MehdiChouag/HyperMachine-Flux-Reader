package com.app.mehdichouag.hypemachine.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.app.mehdichouag.hypemachine.models.TrackModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mehdichouag on 18/10/14.
 */
public class DataBaseList extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "track.db";
    private static final String TABLE_NAME = "list_item";

    private static final String KEY_ID = "id";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_LOVE = "love";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_URL = "url";
    private static final String KEY_POSTID = "postid";
    private static final String KEY_DATELOVED = "dateloved";
    private Context mContext;

    public DataBaseList(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
       mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_ARTIST + " TEXT NOT NULL, "
                + KEY_TITLE + " TEXT NOT NULL," + KEY_DESCRIPTION + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " INTEGER," + KEY_LOVE + " INTEGER,"
                + KEY_IMAGE + " BLOB," + KEY_URL + " TEXT NOT NULL," + KEY_POSTID +" INTEGER, " + KEY_DATELOVED + " INTEGER" +");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void addListItem(TrackModel models)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ARTIST, models.artist);
        values.put(KEY_TITLE, models.title);
        values.put(KEY_DESCRIPTION, models.description);
        values.put(KEY_TIMESTAMP, models.date);
        values.put(KEY_LOVE, models.love);
        values.put(KEY_IMAGE, imageDB(models.getmImage()));
        values.put(KEY_URL, models.imageUrl);
        values.put(KEY_POSTID, models.postId);
        values.put(KEY_DATELOVED, models.dateLoved);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateImageItem(TrackModel model){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IMAGE, imageDB(model.getmImage()));
        db.update(TABLE_NAME, values, KEY_ID + "=" + model.id, null);
        db.close();
    }

    private byte[] imageDB(Bitmap bitmap){
        if (bitmap != null){
            ByteArrayOutputStream img = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, img);
            return img.toByteArray();
        }
        return new byte[0];
    }

    public int getCount()
    {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int test = cursor.getCount();
        cursor.close();
        db.close();
        return test;
    }
    public void reset()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
    public List<TrackModel> getAllTrack() {
        List<TrackModel> trackItems = new ArrayList<TrackModel>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY date(" + KEY_DATELOVED + ") DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                trackItems.add(setTrackFromDb(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return trackItems;
    }
    private Bitmap convertByteArray(byte[] array)
    {
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    public TrackModel getTrackById(int id)
    {
        TrackModel item = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[] {KEY_ID, KEY_ARTIST, KEY_TITLE, KEY_DESCRIPTION, KEY_TIMESTAMP, KEY_LOVE, KEY_IMAGE, KEY_URL, KEY_POSTID, KEY_DATELOVED},
                KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            item = setTrackFromDb(cursor);
            cursor.close();
        }
        db.close();
        return item;
    }

    private TrackModel setTrackFromDb(Cursor cursor)
    {
        TrackModel item = new TrackModel();
        item.id = cursor.getInt(0);
        item.artist = cursor.getString(1);
        item.title = cursor.getString(2);
        item.description = cursor.getString(3);
        item.date = cursor.getInt(4);
        item.love = cursor.getInt(5);
        item.setmImage(convertByteArray(cursor.getBlob(6)));
        item.imageUrl = cursor.getString(7);
        item.postId = cursor.getInt(8);
        item.dateLoved = cursor.getInt(9);
        return  item;
    }

    public void deleteFromPostId(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_POSTID + " =? ", new String[]{String.valueOf(id)});
        db.close();
    }
    public void updateLovedCount(int id, int love){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOVE, love);
        db.update(TABLE_NAME, values, KEY_POSTID + "=" + id, null);
        db.close();
    }
}
