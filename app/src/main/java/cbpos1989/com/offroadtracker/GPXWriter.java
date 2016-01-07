package cbpos1989.com.offroadtracker;

import java.io.File;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



/**
 * Class that will take in location data and write it to gpx file format
 * Created by carlosefonseca.
 */

public class GPXWriter {
    private static final String TAG = GPXWriter.class.getName();

    public GPXWriter() {
    }

    /**
     * Writes locations to gpx file format
     *
     * @param file file for the gpx
     * @param n name for the file
     * @param points List of locations to be written to gpx format
     */
    public static void writePath(File file, String n, List<Object> points) {

        String header = "<gpx creator=\"Off-Road Tracker\" version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n";
        String metadata = " <metadata>\n" + "   <time>1900-01-01T00:00:00Z</time>" + "\n  </metadata>";
        String name = " <trk>\n  <name>" + n + "</name>\n  <trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


        if (points.get(0) instanceof Location) {
            Log.i(TAG,"hERE LOCATION");
            metadata = " <metadata>\n" + "  <time>" + df.format(new Date(((Location)points.get(0)).getTime())) + "Z</time>\n </metadata>\n";
            for (Object l : points) {
                segments += "   <trkpt lat=\"" + ((Location)l).getLatitude() + "\" lon=\"" + ((Location)l).getLongitude() + "\">\n    <ele>0.0</ele>\n    <time>" + df.format(new Date(((Location)l).getTime())) + "Z</time>\n   </trkpt>\n";
            }
        } else {
            Log.i(TAG,"hERE OBJECT");
            for (Object l : points) {
                segments += "   <trkpt lat=\"" + ((LatLng)l).latitude + "\" lon=\"" + ((LatLng)l).longitude + "\">\n    <ele>0.0</ele>\n    <time>" + df.format(new Date()) + "Z</time>\n   </trkpt>\n";
            }
        }

        String footer = "  </trkseg>\n </trk>\n</gpx>";

        try {
            FileWriter writer = new FileWriter(file, true);
            writer.append(header);
            writer.append(metadata);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();

            Log.i(TAG, "Saved " + points.size() + " points.");

        } catch (IOException e) {
            //Toast.makeText(mapsActivity.getApplicationContext(),"File not found",Toast.LENGTH_SHORT);
            Log.e(TAG, "Error Writting Path",e);
        }
    }


}
