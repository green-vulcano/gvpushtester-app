package it.greenvulcano.gvpushtester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.google.firebase.iid.FirebaseInstanceId;

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

    BroadcastReceiver tokenBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            txtRegID.setText(intent.getStringExtra("regID"));
        }
    };

    BroadcastReceiver payloadBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("[GVPush]", "********* Message received *******************" );
            StringBuilder payload = new StringBuilder("Received message:");
            payload.append('\n');

            payload.append(intent.getStringExtra("payload"))
                    .append('\n')
                    .append("--------------------------------------")
                    .append('\n');


            txtPayload.setText(payload.toString());
        }
   };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        txtSenderId = (EditText) findViewById(R.id.txtSenderID);
        txtRegID = (TextView) findViewById(R.id.txtRegID);
        txtPayload = (TextView) findViewById(R.id.txtPayload);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        cmdConnection = (ImageButton) findViewById(R.id.cmdConnection);
        cmdConnection.setOnClickListener(this);

        txtSenderId.setVisibility(View.GONE);
        cmdConnection.setVisibility(View.GONE);

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
        IntentFilter tokenIntentFilter = new IntentFilter();
        tokenIntentFilter.addAction("it.greenvulcano.gvpushtester.TOKEN");
        registerReceiver(tokenBroadcastReceiver, tokenIntentFilter);

        IntentFilter messageIntentFilter = new IntentFilter();
        messageIntentFilter.addAction("it.greenvulcano.gvpushtester.MESSAGE");
        registerReceiver(payloadBroadcastReceiver, messageIntentFilter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String token = FirebaseInstanceId.getInstance().getToken();

        if (token!=null) {
            txtRegID.setText(token);
        }
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
        unregisterReceiver(payloadBroadcastReceiver);
        unregisterReceiver(tokenBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        txtSenderId.setError(null);
        senderId = txtSenderId.getText().toString();

    }

}
