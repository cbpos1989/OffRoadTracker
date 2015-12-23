package cbpos1989.com.offroadtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class that reads a GPX file and parses the Latitude and Longitude into an ArrayList of LatLng
 *
 * Created by Colm O'Sullivan on 30/09/2015.
 */
public class GPXReader extends AsyncTask<Object,Integer,Integer>{
    private final String POINT_COUNT = "pointCount";
    private List<LatLng> points = new ArrayList<LatLng>();
    private double latitude;
    private double longitude;
    private final String TAG = "GPXReader";
    FragmentActivity mapsActivity;
    private int count;
    private int speed = 1000;

    public GPXReader(){

    }

    public GPXReader(MapsActivity mapsActivity, int count){
        this.mapsActivity = mapsActivity;
        this.count = count;

    }

    public GPXReader(LoadMapsActivity mapsActivity, int count, int speed){
        this.mapsActivity = mapsActivity;
        this.count = count;
        setSpeed(speed);
    }

    @Override
    protected Integer doInBackground(Object... objects) {
        if(!isCancelled()) {
            if (objects[0] instanceof InputStream) {
                readPath((InputStream) objects[0]);
            } else {
                readPath((File) objects[0]);
            }

            publishProgress(0);
        }
        return count;
    }

    @Override
    protected void onProgressUpdate(Integer... integer) {
        timedOutput(getSpeed());
    }

    /**
     * Method to delay the drawing of polylines so as not the slow down the UI thread.
     * @param playbackSpeed
     */
    public void timedOutput(int playbackSpeed){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isCancelled()) {
                    //Log.i(TAG, "Point: " + points.get(count));
                    //TODO Make Mapable Interface for both Activites to use these methods
                    ((LoadMapsActivity) mapsActivity).onPauseRoute(count);
                    ((LoadMapsActivity) mapsActivity).drawLine(points.get(count++));
                    if (count <= points.size() - 1) {
                        timedOutput(getSpeed());
                        Log.i(TAG,"Speed: " + speed);
                    }
                }
            }
        }, getSpeed());
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
     * Getter method for accessing list of points.
     * @return points
     */
    public List<LatLng> getPoints(){
        return points;
    }

    /**
     * reset method for points.
     */
    public void resetPoints(){
        points = null;
    }

    public int getSpeed(){
        return speed;
    }

    public void setSpeed(int speed){
        this.speed = speed;
    }
}