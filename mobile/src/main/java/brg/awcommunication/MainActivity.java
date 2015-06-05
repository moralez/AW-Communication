package brg.awcommunication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private static final String TAG = "MainActivity";

    private Button sendMessageBtn;
    private TextView status;

    private static final String COUNT_KEY_PATH = "/count";
    private static final String COUNT_KEY = "com.example.key.count";
    private static final String IMAGE_KEY_PATH = "/image";
    private static final String IMAGE_KEY = "com.example.key.image";
    private int count = 0;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(COUNT_KEY)) {
            count = intent.getIntExtra(COUNT_KEY, -1);
            status.setText(getString(R.string.received) + " " + count);
        } else {
            status.setText(getString(R.string.standby));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendMessageBtn = (Button)findViewById(R.id.send_message_btn);
        sendMessageBtn.setOnClickListener(this);

        status = (TextView)findViewById(R.id.status);

        if (getIntent().hasExtra(COUNT_KEY)) {
            status.setText(getString(R.string.received) + " " + getIntent().getIntExtra(COUNT_KEY, -1));
        } else {
            status.setText(getString(R.string.standby));
        }

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
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_message_btn) {
            sendImage();
        }
    }

    private void sendImage() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(IMAGE_KEY_PATH);

        Bitmap possibru = BitmapFactory.decodeResource(getResources(), R.drawable.possibru);
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        possibru.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Asset asset = Asset.createFromBytes(byteStream.toByteArray());

        putDataMapReq.getDataMap().putAsset(IMAGE_KEY, asset);
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
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
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

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                final DataMap dataMap = dataMapItem.getDataMap();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (dataMap.containsKey(COUNT_KEY)) {
                            count = dataMap.getInt(COUNT_KEY);
                            status.setText(getString(R.string.received) + " " + dataMap.getInt(COUNT_KEY));
                        }
                    }
                });
            }
        }
    }
}
