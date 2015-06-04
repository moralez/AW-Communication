package brg.awcommunication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button sendMessageBtn;
    private TextView status;

    private static final String COUNT_KEY = "com.example.key.count";
    private int count = 0;

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
            sendMessage();
        }
    }

    private void sendMessage() {
        Log.i(TAG, "Sending picture to wearable");
        status.setText(getString(R.string.sending));
    }
}
