package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.content.Context;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import java.lang.*;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * <p>
 * Please read:
 * <p>
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * <p>
 * before you start to get yourself familiarized with ContentProvider.
 * <p>
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 *
 * @author stevko
 */
public class GroupMessengerProvider extends ContentProvider {
    static final String TAG = GroupMessengerProvider.class.getSimpleName();

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
        /*
        REFERENCE:
        https://developer.android.com/training/data-storage/files#java

        */
        String fileName = (String) values.get("key");
        String value = (String) values.get("value");
        Context context = getContext();

        //String path = context.getFilesDir().getPath();
        FileOutputStream outputStream;

        //System.out.println("Path is: "+path);
        //File file = new File(path, fileName);

        /*
        https://stackoverflow.com/questions/6030744/android-reading-from-file-openfileinput%E2%80%8E%E2%80%8F
        * */
        try {

            outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            try {
                outputStream.write(value.getBytes());
                outputStream.close();
                System.out.println("File write successful.");

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "There was some error while creating the file");
        }


        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
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
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        Context context = getContext();
        String path = context.getFilesDir().getAbsolutePath();
        System.out.println(path);
        File fileName = new File(path);
        String[] cols={"key", "value"};
        MatrixCursor cursor = new MatrixCursor(cols);   //https://developer.android.com/reference/android/database/MatrixCursor
        BufferedReader in = null;
        System.out.println("File is:  "+fileName+"/"+selection);
        try {
            if (fileName.exists()) {

                in = new BufferedReader(new FileReader(fileName + "/" + selection));
                String value= in.readLine();
                System.out.println("Value is: "+value);
                String[] eachRow={selection,value};         //Adding key, value to array of eachRow
                cursor.addRow(eachRow);
                return cursor;
                }
        }catch(Exception e)
                {
            System.out.println("Exception "+e+" has occured.");
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.v("query", selection);
        return null;
    }
    }
