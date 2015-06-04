package brg.awcommunication.service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import brg.awcommunication.MainActivity;

/**
 * Created by jmoralez on 6/4/15.
 */
public class AWCListenerService extends WearableListenerService {

    private static final String TAG = "AWCListenerService";
    private static final String COUNT_KEY_PATH = "/count";
    private static final String COUNT_KEY = "com.example.key.count";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            Log.i(TAG, "Data Changed Received: " + uri);
            if (uri.getPath().contains(COUNT_KEY_PATH)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap dataMap = dataMapItem.getDataMap();
                Log.i(TAG, "Count Data Changed: " + dataMap.getInt(COUNT_KEY));
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainActivityIntent.putExtra(COUNT_KEY, dataMap.getInt(COUNT_KEY));
                startActivity(mainActivityIntent);
            }
        }
    }
}
