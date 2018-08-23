package com.pankaj.searchaddress.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pankaj.searchaddress.SearchAddressApplication;
import com.pankaj.searchaddress.mvc.LocationData;
import com.pankaj.searchaddress.util.Slog;
import com.pankaj.searchaddress.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.pankaj.searchaddress.activity.BaseActivity.TAG;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class DBHelper {

    public static String DB_PATH = SearchAddressApplication.getContext().getApplicationInfo().dataDir + "/databases/";
    public static String DB_NAME = "SearchAddress.db";
    public static final int DATABASE_VERSION = 1;
    private final Context context;
    private DatabaseHelper dataHelper;

    private static SQLiteDatabase mDataBase;

    private static String TABLE_PICK_UP_POINTS = "pickuppoints";

    private static final String CREATE_TABLE_PICK_UP_POINTS = "CREATE TABLE IF NOT EXISTS " + TABLE_PICK_UP_POINTS +
            "(ID integer primary key autoincrement,locationName TEXT,latitude VARCHAR(50),longitude VARCHAR(50),locationAddress VARCHAR(50))";


    public DBHelper(Context ctx) {
        this.context = ctx;
        dataHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private Context context;


        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        /**
         * This method is used to check whether the database already exist in
         * device.
         *
         * @return
         */
        public boolean checkDataBase() {
            try {
                File f = new File(DB_PATH + DB_NAME);
                return f.exists();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_PICK_UP_POINTS);
                db.setTransactionSuccessful();


                Slog.i(TAG, "--- " + DBHelper.class.getSimpleName() + " Tables Created");
            } catch (Exception ex) {
                ex.printStackTrace();
                Slog.i(TAG, "--- " + DBHelper.class.getSimpleName() + " Tables Not Created");
            } finally {
                db.endTransaction();
            }
        }


        private void OnDrop(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("Delete FROM " + TABLE_PICK_UP_POINTS);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                db.endTransaction();
            }

        }

        /**
         * This method is used to upgrade the database
         */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion)
                SearchAddressApplication.getContext().deleteDatabase(DB_NAME);
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isOpen() {
        return (mDataBase != null && mDataBase.isOpen());
    }

    private static final Object lockObject = new Object();

    public DBHelper open() throws SQLException {
        try {
            boolean isExist = dataHelper.checkDataBase();
            if (isExist == false) {
                mDataBase = dataHelper.getWritableDatabase();
            }
            mDataBase = dataHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     *
     * @throws java.io.IOException io exception
     */
    public static void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = SearchAddressApplication.getContext()
                .getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void insertLocationDetails(List<LocationData> locationData) {
        try {
            for (int i = 0; i < locationData.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("locationName", locationData.get(i).locationName);
                values.put("locationAddress", locationData.get(i).locationAddress);
                values.put("latitude", locationData.get(i).latitude);
                values.put("longitude", locationData.get(i).longitude);

                Log.i("---------Insert-----", ":");
                mDataBase.insert(TABLE_PICK_UP_POINTS, null, values);
            }
            Util.BackupDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertLocationDetails(LocationData locationData) {
        try {
            ContentValues values = new ContentValues();
            values.put("locationName", locationData.locationName);
            values.put("locationAddress", locationData.locationAddress);
            values.put("latitude", locationData.latitude);
            values.put("longitude", locationData.longitude);

            Log.i("---------Insert-----", ":");
            mDataBase.insert(TABLE_PICK_UP_POINTS, null, values);
            Util.BackupDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LocationData> getAllLocationData() {
        mDataBase.beginTransaction();
        String query = "Select * from " + TABLE_PICK_UP_POINTS;
        ArrayList<LocationData> locationDataArrayList = new ArrayList<>();
        Cursor cursor = mDataBase.rawQuery(query, null);
        StringBuffer stringBuffer = new StringBuffer();
        LocationData locationData = null;

        while (cursor.moveToNext()) {
            locationData = new LocationData();

            int id = cursor.getInt(cursor.getColumnIndex("ID"));
            String locationName = cursor.getString(cursor.getColumnIndex("locationName"));
            Double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            Double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            String locationAddress = cursor.getString(cursor.getColumnIndex("locationAddress"));


            locationData.locationId = id;
            locationData.locationName = locationName;
            locationData.latitude = latitude;
            locationData.longitude = longitude;
            locationData.locationAddress = locationAddress;


            stringBuffer.append(locationData);
            locationDataArrayList.add(locationData);
        }

        mDataBase.endTransaction();
        return locationDataArrayList;
    }
}