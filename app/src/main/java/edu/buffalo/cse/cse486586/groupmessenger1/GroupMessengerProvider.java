package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    private static GroupMessengerProvider instance;
    private static HashMap<String, String> map;
    private static final String TAG = GroupMessengerProvider.class.getSimpleName();
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }
    static final String FILENAME = "groupMessengerStorage";
    FileOutputStream outputStream;
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        //weird assignment because, basically we will be just sending the messages between all the
        //different devices, but then we just have to check those messages against what's in the content
        //provider and then verify it.

        //Info and code template obtained from
        // PA1 and TA Hari

        String key = values.getAsString("key");
        Log.i(TAG, "Will everything break?");
        String value = values.getAsString("value");
        Log.i(TAG, "Msg/key to insert pt 1:" + value + key);
//        Log.e(TAG, "before current map" + map.toString());
//        //map.put(key, value);
//        Log.e(TAG, "after current map" + map.toString());
        value = value + "\n";
        FileOutputStream fos;
        Log.i(TAG, "Msg/key to insert:" + value + key);
        try {
            fos = getContext().getApplicationContext().openFileOutput(key, Context.MODE_PRIVATE);
//            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
//            oos.writeObject(map);
//            oos.close();
            fos.write(value.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "File write failed");
        }

        //Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.


//        try {
//            FileInputStream fis = new FileInputStream(file);
//            ObjectInputStream ois = new ObjectInputStream(fis);
//            map = (HashMap<String, String>) ois.readObject();
//            ois.close();
//            fis.close();
//        } catch (IOException e) {
//            Log.e(TAG, "onCreate IOException:" + e.getMessage());
//        } catch (ClassNotFoundException e) {
//            Log.e(TAG, "onCreate Class Not Found Exception:" + e.getMessage());
//        }


        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *String[] columnNames = {"key", "value"};
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        String[] columnNames = {"key", "value"};
        MatrixCursor mcursor = new MatrixCursor(columnNames);
        MatrixCursor.RowBuilder rowBuilder = mcursor.newRow();
        //https://www.baeldung.com/java-read-file
        try {
            Log.i(TAG, "key passed:" + selection);
            FileInputStream fis = new FileInputStream(getContext().getFileStreamPath(selection));
            Log.i(TAG, "fileReader created");

            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            Log.i(TAG, "bufferedREader created");
//            boolean First = true;
            String msg = reader.readLine();
            Log.i(TAG, "QUERY message received:" + msg);
            reader.close();
            rowBuilder.add("key", selection);
            rowBuilder.add("value", msg);
        }
        catch (IOException e){
            Log.e(TAG, "query IOexception:" + e.getMessage());
        }

        //cursor synchronization??
        //Log.v("query", selection);
        return mcursor;
    }
}
