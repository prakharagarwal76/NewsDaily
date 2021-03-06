package com.example.prakharagarwal.newsdaily.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by prakharagarwal on 29/12/16.
 */
public class NewsSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static NewsSyncAdapter sNewsSyncAdapter = null;

    @Override
    public void onCreate() {

        synchronized (sSyncAdapterLock) {
            if (sNewsSyncAdapter == null) {
                sNewsSyncAdapter = new NewsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sNewsSyncAdapter.getSyncAdapterBinder();
    }
}
