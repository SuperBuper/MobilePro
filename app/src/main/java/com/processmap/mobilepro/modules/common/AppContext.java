package com.processmap.mobilepro.modules.common;

import android.content.Context;

public class AppContext {
    private boolean initDone = false;
    private Context mContext = null;

    private static AppContext ourInstance = new AppContext();
    public static AppContext getInstance() {
        return ourInstance;
    }
    private AppContext() {
    }

    public void  init(Context context) {
        if (!initDone) {
            mContext = context;
            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //
    //

    public Context getContext() {
        return mContext;
    }
}
