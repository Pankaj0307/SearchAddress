package com.pankaj.searchaddress.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pankaj.searchaddress.R;
import com.pankaj.searchaddress.database.DBHelper;
import com.pankaj.searchaddress.util.Slog;
import com.pankaj.searchaddress.util.Util;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public static final String TAG = "SearchAddress";
    public View v;
    public LayoutInflater inflater;
    public FrameLayout lnr_content;
    public Toolbar toolbar;
    public TextView toolbar_title;
    public Button btn_back;
    public ImageView iv_toolbar_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initilizeDB(this);

        setContentView(R.layout.activity_base);
        inflater = getLayoutInflater();

        lnr_content = (FrameLayout) findViewById(R.id.lnr_content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        btn_back = (Button) findViewById(R.id.btn_back);
        iv_toolbar_icon = (ImageView) findViewById(R.id.iv_toolbar_icon);
    }

    public abstract void setContentLayout(int layout);

    public void initilizeDB(Context ctx) {
        try {
            if (Util.db == null) {
                Util.db = new DBHelper(ctx);
                Util.db.open();
            } else if (!Util.db.isOpen()) {
                Util.db.open();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Slog.i(TAG, "-------------initilizeDB---------" + e.getMessage());
        }
    }

}
