package com.sas.neutron.cbsd;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.*;
import java.io.*;

public class MessagingService implements Serializable{
    public String link;
    private URL serverURL; // url object
    public static final String requestError = "requestError";
    private String serverResponse;
    public boolean messageReceived;
    private JSONObject responseObj;
    private HttpURLConnection serverConnection;
    public boolean connectionEstablished;
    private JSONObject myObj;

    public MessagingService(String link) {
        this.link = link;
        this.serverResponse = "";
        messageReceived = false;
        connectionEstablished = establishConnection();
    }

    public boolean establishConnection() {

        try{
            serverURL = new URL(link); // create url
            // open url connection
            serverConnection = (HttpURLConnection) serverURL.openConnection();
            //modify content header
            serverConnection.setRequestProperty("Content-Type", "application/json");
            // set write on
            serverConnection.setDoOutput(true);
            return true;
        }catch (MalformedURLException ex) {
            return false;
        }catch (IOException ex) {
            return false;
        }

    }

    public void closeConnection(){
        if(connectionEstablished ) {
            serverConnection.disconnect();
            connectionEstablished = false;
        }

    }
    // method to send requests to the server and get reply
    public void sendJSONHttpRequest(JSONObject obj) {
        myObj = obj;
        if(connectionEstablished) {
            serverResponse = "";
            messageReceived = false;
            final JSONObject input = obj;
            Runnable httpRunnable = new Runnable() {
                @Override
                public void run() {
                    try{
                        AwaitReply.replyReceived = 0;

                        OutputStreamWriter out = new OutputStreamWriter(serverConnection.getOutputStream());
                        PrintWriter writer = new PrintWriter(out);
                        writer.print(input);
                        writer.flush(); // flush buffer
                        writer.close(); // close output stream
                        out.close();
                        // receive reply
                        InputStream in = new BufferedInputStream(serverConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        int c;
                        while((c = reader.read()) != -1) {

                            serverResponse = serverResponse + (char) c;

                        }

                        responseObj = new JSONObject(serverResponse);

                        Log.i("Messaging", "Received Response");
                        Log.i("Messaging", "Response is: " +serverResponse);
                        Log.i("Messaging", "JSONResponse is: " + responseObj.toString());
                        messageReceived = true;
                        AwaitReply.serverReply = serverResponse;
                        AwaitReply.replyReceived = -1;
                        in.close();
                        closeConnection(); // close HTTP Connection but TCP connection will remain open

                    }catch (MalformedURLException ex) {
                        Log.i(requestError,"Unreachable URL");
                        messageReceived = false;
                        serverResponse = "";
                    }catch (IOException ex) {
                        ex.printStackTrace();
                        Log.i(requestError,"Error with Connection");
                        messageReceived = false;
                        serverResponse = "";
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        Log.i(requestError,"JSON Conversion Error");
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            };

            // create thread object with above runnable
            Thread requestThread = new Thread(httpRunnable);
            // start thread
            requestThread.start();
        } else {
            connectionEstablished = establishConnection();
            // TCP connection will only be re-established if lost
            if(connectionEstablished) {
                sendJSONHttpRequest(myObj);
            }
        }


    }

    public JSONObject getResponseObj() {
        return responseObj;
    }

}
