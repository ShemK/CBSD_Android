package com.sas.neutron.cbsd;

import android.util.Log;

import java.util.ArrayList;

public class AwaitReply {
    public static String serverReply;
    public static int replyReceived = 0;
    public static final String response = "response";

    public static void waitReply() {
        replyReceived = 0;
        serverReply = "Awaiting Response";
        for(int i=0;i<50000;i++) {
            Log.i(response,AwaitReply.serverReply +" " + i);

            if(AwaitReply.replyReceived == -1) {
                Log.i("Await",AwaitReply.serverReply + " ");
                AwaitReply.replyReceived = 0;
                Log.i("Messaging", Integer.toString(AwaitReply.replyReceived));
                break;
            }
        }
    }
    public static void waitReply(int wait) {
        for(int i=0;i<wait;i++) {


        }
    }

}

