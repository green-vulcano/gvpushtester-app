package it.greenvulcano.gvpushtester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String SENDER_ID_KEY = "it.greenvulcano.gvpushtester.SENDER_ID";
    private final String REG_ID_KEY = "it.greenvulcano.gvpushtester.REG_ID";
    private final String PAYLOAD_KEY = "it.greenvulcano.gvpushtester.PAYLOAD_ID";
    String senderId = null;
    String regId = null;

    EditText txtSenderId;
    TextView txtRegID;
    TextView txtPayload;
    ImageButton cmdConnection;

    ProgressBar progressBar;

    GoogleCloudMessaging googleCloudMessaging;

    BroadcastReceiver gcmBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            StringBuilder payload = new StringBuilder("Received message:");
            payload.append('\n');

            Bundle extras = intent.getExtras();

            for (String key : extras.keySet()){
                payload.append(key).append(':')
                        .append('\n');

                payload.append(extras.get(key))
                        .append('\n')
                        .append("--------------------------------------")
                        .append('\n');
            }

            txtPayload.setText(payload.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        googleCloudMessaging = GoogleCloudMessaging.getInstance(MainActivity.this);

        txtSenderId = (EditText) findViewById(R.id.txtSenderID);
        txtRegID = (TextView) findViewById(R.id.txtRegID);
        txtPayload = (TextView) findViewById(R.id.txtPayload);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        cmdConnection = (ImageButton) findViewById(R.id.cmdConnection);
        cmdConnection.setOnClickListener(this);

        if (savedInstanceState!=null) {
            senderId = savedInstanceState.getString(SENDER_ID_KEY);
            regId = savedInstanceState.getString(REG_ID_KEY);
            txtPayload.setText(savedInstanceState.getString(PAYLOAD_KEY));
        }

        txtSenderId.setText(senderId);
        txtSenderId.setText(regId);

        if(TextUtils.isEmpty(regId)) {
            cmdConnection.setImageResource(R.drawable.ic_action_refresh);
        } else {
            cmdConnection.setImageResource(R.drawable.ic_action_cancel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.google.android.c2dm.intent.RECEIVE");
        intentFilter.addCategory("it.greenvulcano.gvpushtester");
        registerReceiver(gcmBroadcastReceiver, intentFilter);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SENDER_ID_KEY, senderId);
        outState.putString(REG_ID_KEY, regId);
        outState.putString(PAYLOAD_KEY, txtPayload.getText().toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gcmBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        txtSenderId.setError(null);
        senderId = txtSenderId.getText().toString();
        if(TextUtils.isEmpty(senderId)) {
           txtSenderId.setError(getText(R.string.msg_required));
        } else {
            RegistrationTask registrationTask = new RegistrationTask();
            registrationTask.execute();
        }
    }


    private class RegistrationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            cmdConnection.setEnabled(false);
            cmdConnection.setOnClickListener(null);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if(TextUtils.isEmpty(regId)) {
                    regId = googleCloudMessaging.register(senderId);
                } else {
                    googleCloudMessaging.unregister();
                }
                Log.d("[ GVPush Tester ]", "GCM registration complete, id "+regId);
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, R.string.msg_error,Toast.LENGTH_SHORT).show();
                Log.e("[ GVPush Tester ]", "GCM registration failed",e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(TextUtils.isEmpty(regId)) {
                cmdConnection.setImageResource(R.drawable.ic_action_refresh);
            } else {
                cmdConnection.setImageResource(R.drawable.ic_action_cancel);
            }

            txtRegID.setText(regId);
            cmdConnection.setEnabled(true);
            cmdConnection.setOnClickListener(MainActivity.this);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
