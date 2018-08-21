package com.pankaj.searchaddress;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class SearchAddressApplication extends MultiDexApplication {

    private static Context sContext;
    private static SearchAddressApplication searchAddressApplication = new SearchAddressApplication();

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        searchAddressApplication = this;


    }

    public static SearchAddressApplication getInstance() {
        return searchAddressApplication;
    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return searchAddressApplication.getApplicationContext();
    }


}
