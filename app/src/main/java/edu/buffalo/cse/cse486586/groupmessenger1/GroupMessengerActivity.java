package edu.buffalo.cse.cse486586.groupmessenger1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORTS = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    private static int msgCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "ARE WE THERE YET");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            Log.i(TAG, "my port is" + myPort);
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            //return;
        }
        
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
//        The following code was taken partially from PA1 and this website:
//        https://stackoverflow.com/questions/30082892/best-way-to-implement-view-onclicklistener-in-android

        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText editText = (EditText) findViewById(R.id.editText1);
                        String msg = editText.getText().toString() + "\n";
                        Log.i(TAG, "MSG ON CLICK IS " + msg);
                        editText.setText(""); // This is one way to reset the input box.
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                    }
                });
//        TextView localTextView = (TextView) findViewById(R.id.local_text_display);
//        localTextView.append("\t" + msg); // This is one way to display a string.
//        TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
//        remoteTextView.append("\n");

        /*
         * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
         * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
         * the difference, please take a look at
         * http://developer.android.com/reference/android/os/AsyncTask.html
         */

    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(ServerSocket... sockets) {


            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            while(true) {
                try {
                    /*
                     * The following code, as well as that of client task was partially created using
                     * https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
                     */
                    ServerSocket serverSocket = sockets[0];
                    Log.i(TAG, "ARE WE HERE");
                    Socket clisco = serverSocket.accept();
                    //declare new input streeam as variable, get servers output stream same as client side
                    String msg;


                    InputStreamReader inStream = new InputStreamReader(clisco.getInputStream());
                    BufferedReader in = new BufferedReader(inStream);

                    msg = in.readLine();
                    Log.i(TAG, "MAYBE HERE");
                    if(msg == null){
                        //clisco.close();
                        Log.i(TAG, "riprip");
                       continue;
                    }
                    Log.i(TAG, "MAYBE HERE 2");
                    ContentValues cv = new ContentValues();
                    cv.put("key", String.valueOf(msgCount));
                    msgCount++;
                    cv.put("value", msg);
                    Log.i(TAG, "Server msg received:" + msg);
                    PrintWriter out = new PrintWriter(clisco.getOutputStream());
                    out.println("Acknowledgement!");
                    Uri uri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");
                    getContentResolver().insert(uri, cv);

                    this.onProgressUpdate(msg);

                    clisco.close();
                } catch (IOException e) {
                    Log.e(TAG, "ServerTask socket IOException:" + e.getMessage());
                    //e.printStackTrace();
                }
            }
            //return null;
        }

        protected void onProgressUpdate(final String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            Log.i(TAG, "Progress update");
            /*
             * The following code was obtained from the top reponse at https://stackoverflow.com/questions/
             * 5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String strReceived = strings[0].trim();
                    Log.i(TAG, "STR RECEIVED" + strReceived);
                    TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                    remoteTextView.append(strReceived + "\t\n");
//                    TextView localTextView = (TextView) findViewById(R.id.local_text_display);
//                    localTextView.append("\n");



                    /*
                     * The following code creates a file in the AVD's internal storage and stores a file.
                     *
                     * For more information on file I/O on Android, please take a look at
                     * http://developer.android.com/training/basics/data-storage/files.html
                     */

//                    String filename = "SimpleMessengerOutput";
//                    String string = strReceived + "\n";
//                    FileOutputStream outputStream;

//                    try {
//                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                        outputStream.write(string.getBytes());
//                        outputStream.close();
//                    } catch (Exception e) {
//                        Log.e(TAG, "File write failed");
//                    }
                }
            });


            return;
        }
    }
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                for(int i = 0; i < 5; i++) {


                    String remotePort = REMOTE_PORTS[i];

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    String msgToSend = msgs[0];
                    /*
                     * TODO: Fill in your client code that sends out a message.
                     */
//                    Log.e(TAG, "WTF");
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                  //  Log.e(TAG, "out problem");

                    //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //Log.e(TAG, "in problem");


                    out.println(msgToSend);
                    in.readLine();
                    out.close();
                    in.close();
                  //  Log.e(TAG, "ClientTask msg sent:" + msgToSend);
                   // in.readLine();
                    socket.close();
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");

            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException:"+e.getMessage());
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}
