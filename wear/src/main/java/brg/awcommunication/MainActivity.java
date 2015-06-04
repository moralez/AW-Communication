package brg.awcommunication;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button sendMessageBtn;
    private TextView status;

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
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_message_btn) {
            sendMessage();
        }
    }

    private void sendMessage() {
        Log.i(TAG, "Sending message to device");
        status.setText(getString(R.string.sending));
    }
}
