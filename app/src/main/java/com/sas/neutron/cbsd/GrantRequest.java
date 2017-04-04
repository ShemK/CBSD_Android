package com.sas.neutron.cbsd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.Boolean.TRUE;

public class GrantRequest extends AppCompatActivity {
    Bundle spectrumResponse;
    ArrayList<String> availableChannelStrings;
    ArrayList<String> eirp;
    JSONArray availableChannel;
    JSONObject operationalFrequencyRange;
    String maxEirp;
    String cbsdId;
    String link;
    MessagingService myService;
    String grantId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_request);

        spectrumResponse = getIntent().getExtras();
        availableChannelStrings = spectrumResponse.getStringArrayList("availableChannelStrings");
        cbsdId = spectrumResponse.getString("cbsdId");
        link = spectrumResponse.getString("link");

        myService = new MessagingService(link);


        String availableChannelToString = spectrumResponse.getString("availableChannel");
        try{
            availableChannel  = new JSONArray(availableChannelToString);

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        setEirp();
        if(availableChannelStrings.size()>0 && availableChannel.length() > 0) {
            updateUI();
        }


    }

    protected void updateUI() {
        ListView channelListView = (ListView) findViewById(R.id.approved_channel_list);
        ArrayAdapter<String> myAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,availableChannelStrings);
        myAdapter.notifyDataSetChanged();
        channelListView.setAdapter(myAdapter);

        channelListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            operationalFrequencyRange = null;
                            operationalFrequencyRange = new JSONObject();
                            operationalFrequencyRange.put("lowFrequency",availableChannel.getJSONObject(position).get("lowFrequency"));
                            operationalFrequencyRange.put("highFrequency",availableChannel.getJSONObject(position).get("highFrequency"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                }
        );


        ListView eirpListView = (ListView) findViewById(R.id.eirp_list);
        ArrayAdapter<String> eirpAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,eirp);
        eirpAdapter.notifyDataSetChanged();
        eirpListView.setAdapter(eirpAdapter);

        eirpListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        maxEirp = String.valueOf(parent.getItemAtPosition(position));
                    }
                }
        );

    }


    protected void setEirp() {
        eirp = new ArrayList<>();
        eirp.add("100");
        eirp.add("110");
        eirp.add("120");
        eirp.add("130");
        eirp.add("140");
        eirp.add("150");
    }

    public void sendGrantRequest(View view) {
        if(operationalFrequencyRange!=null && maxEirp!=null) {
            try{
                JSONObject operationParam = new JSONObject();
                operationParam.put("operationalFrequencyRange",operationalFrequencyRange);
                operationParam.put("maxEirp",maxEirp);
                JSONObject grantRequest = new JSONObject();
                grantRequest.put("cbsdId",cbsdId);
                grantRequest.put("operationParam",operationParam);
                JSONObject obj = new JSONObject();
                obj.put("grantRequest",grantRequest);

                myService.sendJSONHttpRequest(obj);

                AwaitReply.replyReceived = 0;
                AwaitReply.waitReply();
                JSONObject grantResponse = myService.getResponseObj().getJSONObject("grantResponse");
                String grantExpireTime = grantResponse.getString("grantExpireTime");
                int heartbeatInterval = grantResponse.getInt("heartbeatInterval");
                grantId = grantResponse.getString("grantId");

                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MMM-ddZZZHH:mm:sszzzz");
                Date serverDate = ft.parse(grantExpireTime);
                Date CurrentDate = new Date();
                long currentTimeSeconds = CurrentDate.getTime();
                long grantExpireSeconds =  serverDate.getTime() - currentTimeSeconds;
            //    Toast.makeText(GrantRequest.this,Long.toString(grantExpireSeconds),Toast.LENGTH_LONG).show();
                Log.i("Grant","Current Date: " + CurrentDate.toString());
                Log.i("Grant","Server End Date: " + serverDate.toString());
                Intent i = new Intent(this,HeartBeat.class);
                i.putExtra("grantExpireSeconds",grantExpireSeconds);
                i.putExtra("heartbeatInterval",heartbeatInterval);
                i.putExtra("grantId",grantId);
                i.putExtra("cbsdId",cbsdId);
                i.putExtra("link",link);
                startActivity(i);
                finish();
            } catch (Exception ex) {
                Toast.makeText(GrantRequest.this,"Need To Make Spectrum Request First",Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }

        } else {
            Toast.makeText(GrantRequest.this,"Choose Something",Toast.LENGTH_LONG).show();
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
}
