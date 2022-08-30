package eu.gload.ownattest;

import android.app.Application;

public class App extends Application {
    private static App mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static App getMContext() {
        return mContext;
    }
}
