package com.ucla.max.androiddata;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static MainActivity instance; // for calling TextView's findViewById in a static function

    public static int DATA_COUNT = 10;
    public static Integer[] num = new Integer[DATA_COUNT];

    public static String textViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
    }

    public void runSimulation(View view) {
        Log.d("sunnyDay", "Run button clicked.");

        generateTemperature();

        // updating the temperature data generated on mobile UI
        String output = "Temperature data = ";
        for (int i = 0; i < DATA_COUNT; i++) {
            output += (num[i].toString() + ", ");
        }

        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setText(output);

        sendDataToServer();
    }

    public static void updateTextView2(String str) {
        TextView textView2 = (TextView) instance.findViewById(R.id.textView2);
        textView2.setText(str);
    }

    public static void generateTemperature() {
        Random rand = new Random();
        int min = 0, max = 100;
        for (int i = 0; i < DATA_COUNT; i++) {
            num[i] = rand.nextInt(max - min + 1) + min;
        }


    }

    public static void sendDataToServer() {

        new Thread() {
            public void run() {

                Socket mySocket = null;
                DataOutputStream os = null;
                // DataInputStream is = null;
                BufferedReader is = null;

                try {
                    mySocket = new Socket("131.179.30.188", 9900);
                    os = new DataOutputStream(mySocket.getOutputStream());
                    // is = new DataInputStream(mySocket.getInputStream());
                    is = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
                } catch (UnknownHostException exception) {
                    Log.d("sunnyDay", exception.getMessage());
                } catch (IOException exception) {
                    Log.d("sunnyDay", exception.getMessage());
                }

                if (mySocket != null && os != null && is != null) {
                    try {
                        os.writeBytes("Lo and Behold!\n");
                        // os.writeBytes("QUIT");
//                        for (int i = 0; i < DATA_COUNT; i++) {
//                            Log.d("sunnyDay", "Sending messages to server...");
//                            os.writeByte(num[i]);
//                        }

                        String responseLine;
                        Log.d("sunnyDay", "Waiting to get response from server...");
                        while ((responseLine = is.readLine()) != null) {
                            String message = "Got server reply: " + responseLine;
                            Log.d("sunnyDay", message);

                            textViewMessage = message;
                            // updateTextView2(message);
                        }

                        Log.d("sunnyDay", "Closing the Socket...");
                        os.close();
                        is.close();
                        mySocket.close();
                    }  catch (IOException exception) {
                        Log.d("sunnyDay", exception.getMessage());
                    }
                }
            }

        }.start();

        updateTextView2(textViewMessage);
    }
}
