package com.github.jeffery.skin;

import android.app.Application;

/**
 * @author mxlei
 * @date 2022/8/28
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SkinManger.getInstance().init(this);
    }
}
