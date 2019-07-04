package edu.buffalo.cse.cse486586.groupmessenger1;


import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;
import android.content.ContentResolver;
import android.widget.EditText;
import android.util.Log;
import android.os.AsyncTask;
import android.database.Cursor;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.telephony.TelephonyManager;
import android.net.Uri;
import android.content.ContentValues;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.*;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */

//https://www.androiddesignpatterns.com/2012/06/content-resolvers-and-content-providers.html
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerProvider.class.getSimpleName();

    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    int count = 0;
    String[] portsArray = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};

    Uri providerUri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        final EditText et = (EditText) findViewById(R.id.editText1);
        et.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    String msg = et.getText().toString() + "\n";
                    et.setText("");
                    tv.append("\t" + msg);
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

                    return true;

                }
                return false;


            }
        });


        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */


        Button button = (Button) findViewById(R.id.button4);
//      https://developer.android.com/guide/topics/ui/controls/button#java
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msg = et.getText().toString() + "\n";
                et.setText("");
                tv.append("\t" + msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

            }
        });

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.http://developer.android.com/training/basics/activity-lifecycle/index.html
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            Socket socket = null;
            String message;
            ObjectInputStream oiStream = null;
            ObjectOutputStream ooStream = null;

            try {

                while (true) {
                /*
                Create a new socket to accept the ServerSocket's connection request.
                */
                    socket = serverSocket.accept();
                    oiStream = new ObjectInputStream(socket.getInputStream());
                    message = String.valueOf(oiStream.readObject());
                    Log.e(TAG, "Received.");

                    ContentValues keyValueToInsert = new ContentValues();
                    keyValueToInsert.put("key", Integer.toString(count++));
                    keyValueToInsert.put("value", message);
                    getContentResolver().insert(
                            providerUri,    // assume we already created a Uri object with our provider URI
                            keyValueToInsert

                    );



                    publishProgress(message);


                    /*
                    Send Acknowledgement message to client.

                    */

                    ooStream = new ObjectOutputStream(socket.getOutputStream());
                    ooStream.writeObject("ACK");
                    System.out.println("Sending Acknowledgement.");
                    ooStream.flush();


                }

            } catch (IOException e) {
                Log.e(TAG, "IO Exception has occurred.");
                System.out.println(e + " Exception Occurred");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }


        protected void onProgressUpdate(String... strings) {

            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(strings[0]);
            localTextView.append("\n");

        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            String msgToSend = msgs[0];

            try {

                /*
                Creating Individual sockets for all the servers.
                */
                Socket socket = null;
                for (int i = 0; i < portsArray.length; i++) {
                    byte[] ipAddr = new byte[]{10, 0, 2, 2};
                    InetAddress addr = InetAddress.getByAddress(ipAddr);
                    socket = new Socket(addr, Integer.parseInt(portsArray[i]));
                    ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
                    ooStream.writeObject(msgToSend);
                    ooStream.flush();
                }
                Thread.sleep(10);
                Log.e(TAG, "Sent");

                ObjectInputStream oiStream = new ObjectInputStream(socket.getInputStream());
                String message = oiStream.readObject().toString();
                ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());

                if (message.equals("ACK")) {
                    System.out.println("Acknowledgement received from socket.");
                    socket.close();
                    ooStream.close();
                    System.out.println("Socket is closed now.");


                }

                return null;
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}










