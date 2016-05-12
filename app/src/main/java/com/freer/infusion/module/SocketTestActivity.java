package com.freer.infusion.module;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.freer.infusion.R;
import com.freer.infusion.module.service.IBackService;
import com.freer.infusion.module.service.SocketService;

import java.lang.ref.WeakReference;

/**
 * Created by 2172980000774 on 2016/5/12.
 */
public class SocketTestActivity extends Activity {

    private static final String TAG = "MainActivity";

    private IBackService iBackService;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iBackService = null;

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            iBackService = IBackService.Stub.asInterface(service);
            ((SocketService.TestBinder) service).sendMessage("hello");
        }
    };

    private TextView mResultText;
    private EditText mEditText;
    private Intent mServiceIntent;

    class MessageBackReciver extends BroadcastReceiver {
        private WeakReference<TextView> textView;

        public MessageBackReciver(TextView tv) {
            textView = new WeakReference<TextView>(tv);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView tv = textView.get();
            if (action.equals(SocketService.HEART_BEAT_ACTION)) {
                if (null != tv) {
                    tv.setText("Get a heart heat");
                }
            } else {
                String message = intent.getStringExtra("message");
                tv.setText(message);
            }
        };
    }

    private MessageBackReciver mReciver;

    private IntentFilter mIntentFilter;

    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_test);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mResultText = (TextView) findViewById(R.id.resule_text);
        mEditText = (EditText) findViewById(R.id.content_edit);

        mReciver = new MessageBackReciver(mResultText);

        mServiceIntent = new Intent(this, SocketService.class);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(SocketService.HEART_BEAT_ACTION);
        mIntentFilter.addAction(SocketService.MESSAGE_ACTION);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocalBroadcastManager.registerReceiver(mReciver, mIntentFilter);
        bindService(mServiceIntent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
        mLocalBroadcastManager.unregisterReceiver(mReciver);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                String content = mEditText.getText().toString();
                try {
                    boolean isSend = iBackService.sendMessage(content);//Send Content by socket
                    Toast.makeText(this, isSend ? "success" : "fail",
                            Toast.LENGTH_SHORT).show();
                    mEditText.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }
}
