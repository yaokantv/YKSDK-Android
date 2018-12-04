package com.yaokantv.yksdk;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        com.jiagu.sdk.yksdkProtected.install(this);
    }
}
