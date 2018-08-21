package com.pankaj.searchaddress.util;

import android.os.Environment;

import com.pankaj.searchaddress.database.DBHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static com.pankaj.searchaddress.activity.BaseActivity.TAG;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class Util {
    public static final boolean debugMode = true/*BuildConfig.DEBUG*/;
    public static DBHelper db;


    public static void BackupDatabase() {
        try {
            Slog.i(TAG, "BackupDatabase Completed ");
            File sd = new File(Environment.getExternalStorageDirectory()
                    + "/Download");
            if (!sd.exists()) {
                sd.mkdir();
            }
            if (sd.canWrite()) {
                String currentDBPath = DBHelper.DB_PATH + DBHelper.DB_NAME;
                String backupDBPath = "SearchAddress.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
