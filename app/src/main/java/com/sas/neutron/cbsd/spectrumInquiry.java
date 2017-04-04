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

import java.util.ArrayList;


public class spectrumInquiry extends AppCompatActivity {
    ArrayList<String> channels;
    JSONObject channelObjects[];
    JSONArray inquiredChannels;
    public Bundle cbsdData;
    private String cbsdId;
    private MessagingService myService;
    private JSONObject spectrumInquiryRequest;
    private ArrayList<String> availableChannelStrings;
    private String link;
    private int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectrum_inquiry);
        channels = new ArrayList<>();
        cbsdData = getIntent().getExtras();
        cbsdId = cbsdData.getString("cbsdId");
        link = cbsdData.getString("link");
        myService = new MessagingService(link);
        //Toast.makeText(spectrumInquiry.this,cbsdId,Toast.LENGTH_LONG).show();
        spectrumInquiryRequest = new JSONObject();
        try{
            spectrumInquiryRequest.put("cbsdId",cbsdId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        availableChannelStrings = new ArrayList<>();
        count = 0;
        updateUI();
    }


    protected void updateUI() {

        myChannels();
        count = 0;
        ListView myListView = (ListView) findViewById(R.id.spectrum_list);
        ArrayAdapter<String> myAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,channels);
        myAdapter.notifyDataSetChanged();
        myListView.setAdapter(myAdapter);
        inquiredChannels = new JSONArray();
        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        try{
                            boolean exists = false;
                            for(int i = 0; i < inquiredChannels.length();i++) {
                                if(channelObjects[position].equals(inquiredChannels.get(i))) {
                                    exists = true;
                                }
                            }
                            if(!exists) {
                                inquiredChannels.put(count,channelObjects[position]);
                                count++;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                    }
                }
        );


    }

    protected void myChannels() {
        channels.add("820 - 830");
        channels.add("830 - 840");
        channels.add("840 - 850");
        channels.add("850 - 860");
        channels.add("860 - 870");
        channels.add("870 - 880");
        channels.add("880 - 890");
        channels.add("890 - 900");
      //  channels.add("3610 - 3620");
      //  channels.add("3620 - 3630");
      //  channels.add("3630 - 3640");
      //  channels.add("3640 - 3650");

        channelObjects = new JSONObject[channels.size()];
        try{
            channelObjects[0] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":820,\n" +
                    "\t\t\t\t\t\"highFrequency\":830\n" +
                    "\t\t\t\t}");

            channelObjects[1] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":830,\n" +
                    "\t\t\t\t\t\"highFrequency\":840\n" +
                    "\t\t\t\t}");
            channelObjects[2] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":840,\n" +
                    "\t\t\t\t\t\"highFrequency\":850\n" +
                    "\t\t\t\t}");
            channelObjects[3] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":850,\n" +
                    "\t\t\t\t\t\"highFrequency\":860\n" +
                    "\t\t\t\t}");
            channelObjects[4] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":860,\n" +
                    "\t\t\t\t\t\"highFrequency\":870\n" +
                    "\t\t\t\t}");
            channelObjects[5] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":870,\n" +
                    "\t\t\t\t\t\"highFrequency\":880\n" +
                    "\t\t\t\t}");
            channelObjects[6] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":880,\n" +
                    "\t\t\t\t\t\"highFrequency\":890\n" +
                    "\t\t\t\t}");
            channelObjects[7] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":890,\n" +
                    "\t\t\t\t\t\"highFrequency\":900\n" +
                    "\t\t\t\t}");
  /*          channelObjects[8] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":3630,\n" +
                    "\t\t\t\t\t\"highFrequency\":3640\n" +
                    "\t\t\t\t}");
            channelObjects[9] = new JSONObject("\t\t\t{\n" +
                    "\t\t\t\t\t\"lowFrequency\":3640,\n" +
                    "\t\t\t\t\t\"highFrequency\":3650\n" +
                    "\t\t\t\t}");
                    */
        } catch (Exception ex) {
            Log.i("Inquire Spectrum", "Received Response");
        }

    }

    public void sendInquiry(View view) {
        try{

            spectrumInquiryRequest.put("inquiredSpectrum",inquiredChannels);
            JSONObject obj = new JSONObject();
            obj.put("spectrumInquiryRequest",spectrumInquiryRequest);
            myService.sendJSONHttpRequest(obj);
            AwaitReply.replyReceived = 0;
            AwaitReply.waitReply();
            JSONObject spectrumInquiryResponse = myService.getResponseObj().getJSONObject("spectrumInquiryResponse");
            JSONArray availableChannel = spectrumInquiryResponse.getJSONArray("availableChannel");
            Log.i("Inquire Spectrum", Integer.toString(availableChannel.length()));
            for(int i = 0; i < availableChannel.length();i++) {
                for(int j = 0; j < channelObjects.length;j++) {
                  //  JSONObject tempChannel = availableChannel.getJSONObject(i).get("lowFrequency");
                    if((channelObjects[j].get("lowFrequency").toString()).equals(availableChannel.getJSONObject(i).get("lowFrequency").toString())) {
                        availableChannelStrings.add(channels.get(j));
                        Log.i("Inquire","Found");
                    }
                }

            }

            if((availableChannel.length()>0)&&(availableChannelStrings.size()>0)) {
                goToGrantRequest(availableChannel,availableChannelStrings);
            } else{
                spectrumInquiryRequest.remove("inquiredSpectrum");
                availableChannelStrings.clear();
                count = 0;
                inquiredChannels = new JSONArray();
                Toast.makeText(spectrumInquiry.this,"No Available Channels",Toast.LENGTH_SHORT).show();
                Toast.makeText(spectrumInquiry.this,"Choose New Channels",Toast.LENGTH_LONG).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void goToGrantRequest(JSONArray availableChannel,ArrayList<String>availableChannelStrings){

        Intent i = new Intent(this,GrantRequest.class);
        i.putExtra("availableChannel",availableChannel.toString());
        i.putStringArrayListExtra("availableChannelStrings",availableChannelStrings);
        i.putExtra("cbsdId",cbsdId);
        i.putExtra("link",link);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {

        Toast.makeText(spectrumInquiry.this,"Closing App",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}


