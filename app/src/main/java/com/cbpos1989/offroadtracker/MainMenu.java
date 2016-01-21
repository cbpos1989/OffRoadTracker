package com.cbpos1989.offroadtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {
    private final String TAG = "MainMenu";
    private final String FILENAME = "route.gpx";
    private final String USER_PREFERENCES = "userOptions";
    private EditText coordsField;
    private SharedPreferences sharedpreferences;
    private ArrayList<Object> points = new ArrayList<Object>();

    private String[] mFileLlist;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//off-road_tracker_routes//");
    private String mChosenFile;
    private String mChosenRoute;
    private static final String FTYPE = ".gpx";
    private static final int DIALOG_LOAD_FILE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //Log.i(TAG, "In Main Menu");

        coordsField = (EditText) findViewById(R.id.coords_field);
        //coordsField.setText(" ");

        //TODO Might add settings menu later will need to display ActionBar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        //Load in gpx files for use with load route map
        loadFileList();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //Load List of points feom the live route to maintain users current route
        //TODO Convert GPX data to JSON and save info to SQLite
        if(bundle != null) {
            Log.i(TAG, "Bundle not null");
            points = (ArrayList<Object>) bundle.getSerializable("pointsList");
            if(points != null) {
                Log.i(TAG, "Points = " + points.size());
            } else {
                points = new ArrayList<Object>();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        getPreferences();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void getPreferences(){
        sharedpreferences = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);

        mChosenRoute = sharedpreferences.getString("chosenRoute", null);
        Log.i(TAG,"mChosenRoute = " + mChosenRoute);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickMapButton(View view){
        String coords = coordsField.getText().toString();
        if(coords.equals("")) {
            Log.i(TAG, "Coords: " + coords);
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.putString("UserChoice", "Live");
        if(!(coords.equals(""))) {
            Log.i(TAG,"Don't do this");
            editor.putString("Coords", coords);
        }

        editor.commit();

        Intent mapActivity = new Intent(this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("pointsList", points);

        mapActivity.putExtras(bundle);
        startActivity(mapActivity);


    }

    public void onClickLoadRoute(View view){
        if(mChosenRoute != null){
            Intent loadMapActivity = new Intent(getApplicationContext(), LoadMapsActivity.class);
            startActivity(loadMapActivity);
        } else {
            onCreateDialog(DIALOG_LOAD_FILE);
        }


    }

    /**
     * Loads all gpx files present in app directory used for storing routes.
     * @author schwiz (stackoverflow)
     */
    private void loadFileList(){
        try{
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG,"unable to write on the sd card " + e.toString());
        }

        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(FTYPE) || sel.isDirectory();
                }
            };
            mFileLlist = mPath.list(filter);
        } else {
            mFileLlist = new String[0];
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_LOAD_FILE: builder.setTitle(R.string.choose_route_menu);
                if (mFileLlist == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileLlist, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileLlist[which];

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.clear();
                        editor.putString("chosenRoute", mChosenFile);
                        editor.commit();

                        Intent loadMapActivity = new Intent(getApplicationContext(), LoadMapsActivity.class);
                        startActivity(loadMapActivity);
                    }
                });
                builder.setPositiveButton(R.string.positive_button_message_alt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent loadMapActivity = new Intent(getApplicationContext(), LoadMapsActivity.class);
                        startActivity(loadMapActivity);
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

    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Do you want to quit app?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MapsActivity.firstCoord = true;
                    finish();
                    File routeFile = new File(getFilesDir(), FILENAME);
                    boolean routeDeleted = routeFile.delete();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.remove("chosenRoute");
                    editor.commit();
                    Log.i(TAG, "Route Deleted");
                    System.exit(0);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();



            return  true;
        }

        return super.onKeyDown(keyCode,event);
    }



}
