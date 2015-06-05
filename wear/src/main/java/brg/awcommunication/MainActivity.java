package brg.awcommunication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity
        implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private static final String TAG = "MainActivity";
    private static final String COUNT_KEY_PATH = "/count";
    private static final String COUNT_KEY = "com.example.key.count";
    private static final String IMAGE_KEY_PATH = "/image";
    private static final String IMAGE_KEY = "com.example.key.image";

    private Button sendMessageBtn;
    private TextView status;
    private ImageView image;

    private GoogleApiClient mGoogleApiClient;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                sendMessageBtn = (Button) stub.findViewById(R.id.send_message_btn);
                sendMessageBtn.setOnClickListener(MainActivity.this);

                status = (TextView)stub.findViewById(R.id.status);

                image = (ImageView)stub.findViewById(R.id.image_view);
            }
        });

        // Step 1
        // In onCreate(Bundle), create an instance of GoogleApiClient to work with the Data Layer API.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_message_btn) {
            sendMessage();
        }
    }

    private void sendMessage() {
        Log.i(TAG, "Sending count to device");
        status.setText(getString(R.string.sending) + " " + count);
        increaseCounter();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");
        // Now you can use the Data Layer API
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    private void increaseCounter() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(COUNT_KEY_PATH);
        putDataMapReq.getDataMap().putInt(COUNT_KEY, ++count);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.i(TAG, "Data item set: " + result.getDataItem().getUri());
                } else {
                    Log.i(TAG, "Failed to set data item");
                }
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());

                Uri uri = event.getDataItem().getUri();
                final DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                if (dataMapItem.getDataMap().containsKey(IMAGE_KEY)) {
                    Asset imageAsset = dataMapItem.getDataMap().getAsset(IMAGE_KEY);
                    final Bitmap bitmap = loadBitmapFromAsset(imageAsset);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            image.setImageBitmap(bitmap);
                        }
                    });
                }

                if (dataMapItem.getDataMap().containsKey(COUNT_KEY)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            count = dataMapItem.getDataMap().getInt(COUNT_KEY);
                            status.setText(getString(R.string.received) + " " + dataMapItem.getDataMap().getInt(COUNT_KEY));
                        }
                    });
                }
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(30, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
