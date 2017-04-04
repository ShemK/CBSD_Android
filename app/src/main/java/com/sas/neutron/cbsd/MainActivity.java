package com.sas.neutron.cbsd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private String link;
    private String cbsdId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        link = "http://128.173.95.235/spectrumAccessSystem/start.php";

    }


    private  JSONObject registrationRequestObj() {
        JSONObject obj = new JSONObject();
        TextView getValue = (TextView) findViewById(R.id.fccId);
        String fccId = getValue.getText().toString();
        getValue = (TextView) findViewById(R.id.cbsdCategory);
        String cbsdCategory = getValue.getText().toString();
        getValue = (TextView) findViewById(R.id.callSign);
        String callSign = getValue.getText().toString();
        getValue = (TextView) findViewById(R.id.cbsdSerialNumber);
        String cbsdSerialNumber = getValue.getText().toString();
        getValue = (TextView) findViewById(R.id.userId);
        String userId = getValue.getText().toString();

        try{
            JSONObject registrationRequest = new JSONObject();
            registrationRequest.put("fccId", fccId);
            registrationRequest.put("cbsdCategory", cbsdCategory);
            registrationRequest.put("callSign", callSign);
            registrationRequest.put("cbsdSerialNumber",cbsdSerialNumber);
            registrationRequest.put("userId", userId);
            obj.put("registrationRequest",registrationRequest);
            String installationParam = "{\n" +
                    "\t\t\t\t\"latitude\": 37.425056,\n" +
                    "\t\t\t\t\"longitude\": -122.084113,\n" +
                    "\t\t\t\t\"height\": 9.3,\n" +
                    "\t\t\t\t\"heightType\": \"AGL\",\n" +
                    "\t\t\t\t\"indoorDeployment\": false,\n" +
                    "\t\t\t\t\"antennaAzimuth\": 271,\n" +
                    "\t\t\t\t\"antennaDowntilt\": 3,\n" +
                    "\t\t\t\t\"antennaGain\": 16,\n" +
                    "\t\t\t\t\"antennaBeamwidth\": 30\n" +
                    "\t\t\t}";
            registrationRequest.put("installationParam",installationParam);
            String measCapability = " [\"EUTRA_CARRIER_RSSI_ALWAYS\",\n" +
                    "\t\t\t\t\t\t\"EUTRA_CARRIER_RSSI_NON_TX\"\n" +
                    "\t\t\t]";
            registrationRequest.put("measCapability",measCapability);


        } catch (Exception ex){
          //  ex.printStackTrace();
        }
        return obj;
    }
    public void sendRegistrationRequest(View view) {

        TextView responseView = (TextView) findViewById(R.id.responseView);
        MessagingService myService = new MessagingService(link);
        myService.sendJSONHttpRequest(registrationRequestObj());
        AwaitReply.replyReceived = 0;
        AwaitReply.waitReply();
        String response = "No Response Received";
        try{
            Log.i("Response_SendR",myService.getResponseObj().getString("registrationResponse"));
            JSONObject registrationResponse = myService.getResponseObj().getJSONObject("registrationResponse");
            JSONObject responseObj = registrationResponse.getJSONObject("response");
            String responseCode = responseObj.getString("responseCode");
            responseCode = responseCode.replaceAll("\\s","");
            Log.i("Response_SendR",responseCode);
            if( responseCode.equalsIgnoreCase("0")) {
                response = "Registration Accepted";
                Intent i = new Intent(this,spectrumInquiry.class);
                cbsdId = responseObj.getString("cbsdId");

                i.putExtra("cbsdId",cbsdId);
                i.putExtra("link",link);
            //    i.putExtra("MessagingService",myService);
                startActivity(i);
                finish();
            } else {
                response = "Registration Rejected";
                myService.closeConnection();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        responseView.setText(response);

    }

}

