package com.cbpos1989.offroadtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Colm on 11/02/2016.
 */
public class LoadRouteDialog extends AppCompatActivity{
    private final String TAG = "LoadRouteDialog";

    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//off-road_tracker_routes//");
    private String mChosenFile;
    private String mChosenRoute;
    private static final String FTYPE = ".gpx";
    private static final int DIALOG_LOAD_FILE = 1000;
    private static final int DIALOG_LOAD_NEW_FILE = 2000;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private SharedPreferences sharedPreferences;
    private Context context;

    public LoadRouteDialog(SharedPreferences sharedPreferences, Context context, int id){
        this.sharedPreferences = sharedPreferences;
        this.context = context;
        loadFileList();
        onCreateDialog(id);
    }

    /**
     * Loads all gpx files present in app directory used for storing routes.
     * @author schwiz (stackoverflow)
     */
    private void loadFileList(){
        try{
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }

        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    Log.i(TAG,"File = " + filename.contains(FTYPE));
                    return filename.contains(FTYPE) || sel.isDirectory();
                }
            };
            mFileList = mPath.list(filter);
            Log.e(TAG, "got list " + mPath.list(filter));
        } else {
            mFileList = new String[0];
        }
    }

    /**
     *  Create dialog for the user to chose gpx file to load.
     * @param id
     * @return Dialog
     * @author schwiz (stackoverflow)
     */
    @Override
    protected Dialog onCreateDialog(int id){
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        switch (id) {
            case DIALOG_LOAD_FILE: builder.setTitle(R.string.choose_route_menu);
                if (mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.putString("chosenRoute", mChosenFile);
                        editor.commit();

                        //TODO NOT Working need to figure out how to load after choosing
                        Intent loadMapActivity = new Intent(context, LoadMapsActivity.class);
                        startActivity(loadMapActivity);
                    }
                });
                builder.setPositiveButton(R.string.positive_button_message_alt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent loadMapActivity = new Intent(context, LoadMapsActivity.class);
                        startActivity(loadMapActivity);
                    }
                });
                builder.setNegativeButton(R.string.negative_button_message_alt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
                case DIALOG_LOAD_NEW_FILE: builder.setTitle(R.string.choose_route_menu);
                if (mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.putString("chosenRoute", mChosenFile);
                        editor.commit();
                    }
                });
                builder.setNegativeButton(R.string.negative_button_message_alt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }
}
