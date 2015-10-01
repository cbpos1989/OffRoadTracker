package cbpos1989.com.offroadtracker;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colm O'Sullivan on 30/09/2015.
 */
public class GPXReader {
    private List<LatLng> points = new ArrayList<LatLng>();
    private double latitude;
    private double longitude;
    private MapsActivity mapsActivity;

    public GPXReader(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
    }

    public void readPath(File file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            String output = "";

            while ((line = br.readLine()) != null) {
                if(line.contains("<trkpt")) {
                    output += line;
                }
            }
            br.close();

            for (String str: output.split(" ")){
                lines.add(str);
                Log.i("Split Output", str);
            }

            for(String str: lines) {
                if(str.contains("<trkpt") || str.contains("<time>")){
                    continue;
                } else if (str.contains("lat")) {
                        int index = str.indexOf('\u0022');

                        try {
                            str = str.substring(index + 1, str.length() - 1);
                            latitude = Double.parseDouble(str);
                            Log.i("LatitudeParsed", "Latitude: " + latitude);
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                } else if(str.contains("lon")){
                        int index = str.indexOf('\u0022');
                        try {
                            str = str.substring(index + 1, str.length() - 2);
                            longitude = Double.parseDouble(str);
                            Log.i("LongitudeParsed", "Longitude: " + longitude);
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }

                        points.add(new LatLng(latitude, longitude));
                }


            }


            for(LatLng lt: getPoints()){
                Log.i("GPX Output", lt.toString());
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<LatLng> getPoints(){
        return points;
    }
}
