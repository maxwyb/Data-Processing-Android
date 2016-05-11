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

//    public static String textViewMessage; // the right-side TextView message for showing results

    public static String temperature = ""; // a parsed String to send temperature data to server
    public static String result = ""; // the analysis result coming from server

    public static String PC_IP = "131.179.30.195";
    public static String ANDROID_IP = "131.179.45.16";
    public static Integer PORT = 9940;

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
        try {
            textView1.setText(output);
        } catch (NullPointerException exception) {
            Log.d("sunnyDay", exception.getMessage());
        }

        sendDataToServer();
        receiveResultFromServer();

        updateTextView2(result);
    }


    public static void updateTextView2(String str) {
        TextView textView2 = (TextView) instance.findViewById(R.id.textView2);
        try {
            textView2.setText(str);
        } catch (NullPointerException exception) {
            Log.d("sunnyDay", exception.getMessage());
        }
    }

    public static void generateTemperature() {
        Random rand = new Random();
        int min = 0, max = 100;
        for (int i = 0; i < DATA_COUNT; i++) {
            num[i] = rand.nextInt(max - min + 1) + min;
        }


    }

    public static void sendDataToServer() {

        // parse the temperature data array to a String
        temperature = "";
        for (int i = 0; i < DATA_COUNT - 1; i++) {
            temperature += (num[i].toString() + ", ");
        }
        temperature += (num[DATA_COUNT - 1].toString() + "\n");

        Log.d("sunnyDay", temperature);

        // send this String to the server
        new Thread() {
            public void run() {
                Socket mySocket = null;
                DataOutputStream os = null;
                // DataInputStream is = null;
                BufferedReader is = null;

                // initialize a Socket for TCP/IP communication; here Android device is client and PC is server
                try {
                    mySocket = new Socket(PC_IP, PORT);
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
//                        os.writeBytes("Lo and Behold!\n");
//                        os.writeBytes("QUIT");
                        os.writeBytes(temperature);
//                        for (int i = 0; i < DATA_COUNT; i++) {
//                            Log.d("sunnyDay", "Sending messages to server...");
//                            os.writeInt(num[i]);
//                        }

                        // currently we are not expecting reply from the server
                        /*
                        String responseLine;
                        Log.d("sunnyDay", "Waiting to get response from server...");
                        while ((responseLine = is.readLine()) != null) {
                            String message = "Got server reply: " + responseLine;
                            Log.d("sunnyDay", message);

                            textViewMessage = message;
                            // updateTextView2(message);
                        }
                        */

                        // close the Socket in client mode; later start in server mode to receiver result data from server.
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

//        updateTextView2(textViewMessage);
    }

    public static void receiveResultFromServer() {
        new Thread() {
            public void run() {

                // initailize a Socket. Here Android device is serve and PC is client.
                ServerSocket echoServer = null;
                String line;
                // DataInputStream is;
                BufferedReader is;
                PrintStream os;
                Socket clientSocket = null;

                Log.d("sunnyDay", "Initializing Socket...");
                try {
                    echoServer = new ServerSocket(PORT);
                } catch (IOException e) {
                    System.out.println(e);
                }
                // Create a socket object from the ServerSocket to listen and accept
                // connections.
                // Open input and output streams
                try {
                    clientSocket = echoServer.accept();
                    // is = new DataInputStream(clientSocket.getInputStream());
                    is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                    os = new PrintStream(clientSocket.getOutputStream());

                    Log.d("sunnyDay", "Connection with client established. Listening for incoming messages...");
                    while (true) {
                        line = is.readLine();
                        // os.println(line);
                        // Log.d("sunnyDay", "Echoed the message from client.");

                        result = line; // received the String from PC for result
                        Log.d("sunnyDay", result);
                        Log.d("sunnyDay", "Received analysis result. Closing server...");
                        break;
                    }
                } catch (IOException e) {
                    Log.d("sunnyDay", e.getMessage());
                }

                // After receiving the analysis result from PC, close the serverSocket.
                Log.d("sunnyDay", "Closing sockets...");
                try {
                    clientSocket.close();
                    echoServer.close();
                } catch (IOException exception) {
                    Log.d("sunnyDay", exception.getMessage());
                }
            }
        }.start();
    }

}
