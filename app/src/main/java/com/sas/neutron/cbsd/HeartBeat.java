package com.sas.neutron.cbsd;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.view.menu.ExpandedMenuView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class HeartBeat extends AppCompatActivity {
    Bundle grantResponse;
    String grantId;
    String cbsdId;
    MessagingService myService;
    String link;
    boolean grantEnd = FALSE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_beat);
        grantResponse = getIntent().getExtras();
        final int heartbeatInterval = grantResponse.getInt("heartbeatInterval");
        long grantExpireSeconds = grantResponse.getLong("grantExpireSeconds");
        grantId = grantResponse.getString("grantId");
        cbsdId = grantResponse.getString("cbsdId");
        link = grantResponse.getString("link");
        myService = new MessagingService(link);


        new CountDownTimer(grantExpireSeconds,1000) {
            TextView clockValue = (TextView) findViewById(R.id.textClock);
            @Override
            public void onTick(long millisUntilFinished) {
                if(grantEnd == FALSE) {
                    clockValue.setText("Grant Ends In: " + millisUntilFinished / 1000);
                } else {
                    clockValue.setText("GRANT ENDED");
                }
            }

            @Override
            public void onFinish() {
                clockValue.setText("GRANT ENDED");
                grantEnd = TRUE;
                relinquishGrant();

            }
        }.start();


        final Handler myHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(HeartBeat.this,"Sending Heartbeat",Toast.LENGTH_SHORT).show();
            }
        };

        Runnable heartBeatRunnable = new Runnable() {
            @Override
            public void run() {
                JSONObject heartbeatRequest = new JSONObject();
                try{
                    heartbeatRequest.put("cbsdId",cbsdId);
                    heartbeatRequest.put("grantId",grantId);
                    heartbeatRequest.put("grantRenew",FALSE);
                    heartbeatRequest.put("operationState","AUTHORIZED");

                    JSONObject obj = new JSONObject();
                    obj.put("heartbeatRequest",heartbeatRequest);
                    long heartBeatMilli = heartbeatInterval*1000;
                    while(grantEnd==FALSE) {
                        myService.sendJSONHttpRequest(obj);
                        Thread.sleep(heartBeatMilli);
                        Message msg = new Message();
                        myHandler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        Thread heartbeatThread = new Thread(heartBeatRunnable);
        heartbeatThread.start();


    }

    public void relinquishGrant(View view) {
        JSONObject relinquishmentRequest = new JSONObject();
        try{
            relinquishmentRequest.put("cbsdId",cbsdId);
            relinquishmentRequest.put("grantId",grantId);
            JSONObject obj = new JSONObject();
            obj.put("relinquishmentRequest",relinquishmentRequest);
            myService.sendJSONHttpRequest(obj);
            grantEnd = TRUE;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void relinquishGrant() {
        JSONObject relinquishmentRequest = new JSONObject();
        try{
            relinquishmentRequest.put("cbsdId",cbsdId);
            relinquishmentRequest.put("grantId",grantId);
            JSONObject obj = new JSONObject();
            obj.put("relinquishmentRequest",relinquishmentRequest);
            myService.sendJSONHttpRequest(obj);
            grantEnd = TRUE;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        relinquishGrant();
        Intent i = new Intent(this,spectrumInquiry.class);
        i.putExtra("cbsdId",cbsdId);
        i.putExtra("link",link);
        startActivity(i);
        finish();
    }

    @Override
    public void onPause() {
        relinquishGrant();
        AwaitReply.replyReceived = 0;
        AwaitReply.waitReply();
        super.onPause();
    }

    @Override
    public void onStop() {
        relinquishGrant();
        AwaitReply.replyReceived = 0;
        AwaitReply.waitReply();
        super.onStop();
    }

}
