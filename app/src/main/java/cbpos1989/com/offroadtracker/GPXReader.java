package cbpos1989.com.offroadtracker;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that reads a GPX file and parses the Latitude and Longtiude into an ArrayList of LatLng
 *
 * Created by Colm O'Sullivan on 30/09/2015.
 */
public class GPXReader extends AsyncTask<Object,Integer,List>{
    private List<LatLng> points = new ArrayList<LatLng>();
    private double latitude;
    private double longitude;
    private final String TAG = "GPXReader";
    MapsActivity mMap;
    int count;

    public GPXReader(){}

    @Override
    protected List doInBackground(Object... objects) {
        mMap = (MapsActivity)objects[1];

        if(objects[0] instanceof InputStream){
            readPath((InputStream)objects[0]);
        } else {
            readPath((File) objects[0]);
        }
        publishProgress(0);
        return points;
    }

    @Override
    protected void onProgressUpdate(Integer... integer) {
        Log.i(TAG,"Points: " + points.size());
        int i = 0;

        timedOutput(1000);
    }

    /**
     * Method to delay the drawing of polylines so as not the slow down the UI thread.
     * @param time
     */
    public void timedOutput(final int time){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.drawLine(points.get(count));
                Log.i("TimeTest", "2 Seconds Passed " + ++count);

                if(count <= points.size() -1){
                    timedOutput(time);
                }
            }
        }, time);
    }

    /**
     * Takes in file which is split into ArrayList of string containing the lines within the
     * GPX file that relate to longitude and latitude.
     * @param inputStream
     */
    public void readPath(InputStream inputStream){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            String output = "";

            while ((line = br.readLine()) != null) {
                if(line.contains("<trkpt")) {
                    output += line + " ";
                    //Log.i("Output", output);
                }
            }
            br.close();

            for (String str: output.split(" ")){
                lines.add(str);
                //Log.i("Split Output", str);
            }

            parseFile(lines);

            for(LatLng lt: getPoints()){
                //Log.i("GPX Output", lt.toString());
            }


        } catch (FileNotFoundException e) {
            Log.e(TAG,e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Takes in file which is split into ArrayList of string containing the lines within the
     * GPX file that relate to longitude and latitude.
     * @param file
     */
    public void readPath(File file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            String output = "";

            while ((line = br.readLine()) != null) {
                if(line.contains("<trkpt")) {
                    output += line + " ";
                    //Log.i("Output", output);
                }
            }
            br.close();

            for (String str: output.split(" ")){
                lines.add(str);
                //Log.i("Split Output", str);
            }

            parseFile(lines);

            for(LatLng lt: getPoints()){
                //Log.i("GPX Output", lt.toString());
            }


        } catch (FileNotFoundException e) {
            Log.e(TAG,e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Takes in the ArrayList of lines from readPath() and parses the list to populate a list of
     * points with latitude and longitude details.
     * @param lines
     */
    private void parseFile(ArrayList<String> lines) {
        for (String str : lines) {
            if (str.contains("<trkpt")) {
                continue;
            } else if (str.contains("lat")) {
                int index = str.indexOf('\u0022');

                try {
                    str = str.substring(index + 1, str.length() - 1);
                    latitude = Double.parseDouble(str);
                    //Log.i("LatitudeParsed", "Latitude: " + latitude);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            } else if (str.contains("lon")) {
                int index = str.indexOf('\u0022');
                try {
                    str = str.substring(index + 1, str.length() - 2);
                    longitude = Double.parseDouble(str);
                    //Log.i("LongitudeParsed", "Longitude: " + longitude);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                points.add(new LatLng(latitude, longitude));
            }
        }
    }

    /**
     * Getter method for accsess list of points.
     * @return points
     */
    public List<LatLng> getPoints(){
        return points;
    }
}