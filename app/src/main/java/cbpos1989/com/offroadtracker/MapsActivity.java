package cbpos1989.com.offroadtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Class that will display the maps activity. Track the user location and draw a path of the user's route using polylines.
 *
 * Created by Alex Scanlan & Colm O'Sullivan on 28/09/2015.
 *
 */
public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener {

    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location mLocation;
    private ArrayList<Location> points = new ArrayList<Location>();
    private Polyline route;

    private final String FILENAME = "route.gpx";
    private boolean stopLoc = false;
    private int moveCameraFactor = 10;
    static boolean firstCoord = true;
    private static LatLng prevCoordinates;

    File routeFile;

    private Timer timer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setUpMapIfNeeded();

        Log.i("drawLine","Value of firstCoord" + firstCoord);

        try {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocationGPS != null) {
                prevCoordinates = new LatLng(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevCoordinates, 18));
            }
        }catch(SecurityException se){
            se.printStackTrace();
        }

        //Loads internal GPX File
        routeFile = new File(this.getFilesDir(), FILENAME);

        loadCurrentRoute(routeFile);

        //Loads external GPX File
        //InputStream XmlFileInputStream = getResources().openRawResource(R.raw.slieve_bloom_mountains_mtb_trail);
        //loadPreviousRoute(XmlFileInputStream);

    }

    private void setLastKnownLocation(){

    }

    private void loadPreviousRoute(InputStream inputStream){

        GPXReader gpxReader = new GPXReader();
        //gpxReader.readPath(inputStream);

        final ArrayList<LatLng> polylinePoints = (ArrayList<LatLng>) gpxReader.getPoints();

        for(LatLng latLng: polylinePoints){
            Log.i("Points", latLng.toString());
        }

        if(polylinePoints.size() > 1) {
            prevCoordinates = polylinePoints.get(0);
            mMap.addMarker(new MarkerOptions().position(polylinePoints.get(0)).title("Marker"));
        }
    }

    /**
     * Retrieves gpx file from internal app files and sends file to parser
     * an ArrayList of LatLng is returned and used in for loop to redraw polylines route.
     * @param file takes in file of type .gpx
     */
    private void loadCurrentRoute(File file){
        points.clear();

        GPXReader gpxReader = new GPXReader();
        gpxReader.readPath(file);

        ArrayList<LatLng> polylinePoints = (ArrayList<LatLng>) gpxReader.getPoints();

        if(polylinePoints.size() > 1){
            prevCoordinates = polylinePoints.get(0);
            mMap.addMarker(new MarkerOptions().position(polylinePoints.get(0)).title("Marker"));

            //Re-draw current route.
            for (int i = 0; i < polylinePoints.size();++i) {
                drawLine(polylinePoints.get(i));
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy(){

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }

        //Load Internal GPX File
        routeFile = new File(this.getFilesDir(), FILENAME);
        saveCurrentRoute(routeFile);

        super.onDestroy();
    }

    void saveCurrentRoute(File file){
        GPXWriter gpxFile = new GPXWriter(this);
        try {
            file.createNewFile();
            gpxFile.writePath(file, "GPX_Route", points);

            if(route != null) {
                route.remove();
            }

            Log.i("WritingFile", "Completed writing" + file.getName());

        } catch (Exception e) {
            Log.e("WritingFile", "Not completed writing" + file.getName());
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                mMap.setOnMapLongClickListener(this);
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    //Method to demo app without gps signal
//    public synchronized void noGPSLocation(View view){
//        final double[][] mockLocations = {{53.1886100,-6.2280610},{53.1884770,-6.2280640},{53.1880730,-6.2286810},{53.1876860,-6.2293080},{53.1872790,-6.2299980},{53.1870960,-6.2301030}};
//        prevCoordinates = new LatLng(53.1886090,-6.2280710);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevCoordinates, 18));
//        mMap.addMarker(new MarkerOptions().position(prevCoordinates).title("Marker"));
//    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if(mLocation != null) {
            mLocation = location;
        }

        points.add(location);
        drawLine(location);

        //Only move camera after certain amount of location changes
        if(points.size() % moveCameraFactor == 0) {
            moveCamera(new LatLng(location.getLatitude(),location.getLongitude()));
        }

    }

    private void moveCamera(LatLng latLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    /**
     * Used for drawing the live route the the user will see updating as thier reamin on the MapsActivity
     * First checks if it is the first coordinates that are recieved and places a starting marker to indicate this
     * @param location
     */
    private void drawLine(Location location) {
        LatLng currCoordinates = new LatLng(location.getLatitude(),location.getLongitude());

        if(firstCoord){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currCoordinates, 18));
            mMap.addMarker(new MarkerOptions().position(currCoordinates).title("Marker"));
            prevCoordinates = currCoordinates;
            firstCoord = false;
        }

        Polyline liveRoute = mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(prevCoordinates)
                        .add(currCoordinates)
        );

        liveRoute.setColor(Color.RED);
        liveRoute.setWidth(5.0F);
        Log.i("Route Drawing", "Drawing from " + prevCoordinates + " to " + currCoordinates);
        prevCoordinates = currCoordinates;
    }

    /**
     * Used for redrawing the users route when they navigate back to the MapsActivity from another Activity
     * @param latlng
     */
    private synchronized void drawLine(LatLng latlng)  {

        LatLng currCoordinates = latlng;

        route = mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(prevCoordinates)
                        .add(currCoordinates)
        );

        route.setColor(Color.RED);
        route.setWidth(5.0F);
        Log.i("Route Drawing", "Drawing from " + prevCoordinates + " to " + currCoordinates);
        prevCoordinates = currCoordinates;

    }

    public void stopLocationListener(View view) {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        ImageButton button = (ImageButton) findViewById(R.id.stopLocListenerBtn);

        if(stopLoc){
           Toast.makeText(this, "Pausing Route", Toast.LENGTH_SHORT).show();

            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

            button.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);

            stopLoc = !stopLoc;
        } else{
           Toast.makeText(this, "Tracking Route", Toast.LENGTH_SHORT).show();

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

            button.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);

            stopLoc = !stopLoc;
        }
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {

        // Init the dialog object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up the input
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());

        final View v = factory.inflate(R.layout.dialog_layout, null);
        final EditText input = (EditText) v.findViewById(R.id.dialog_edit_text);
        final RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radio_group_dialog);

        builder.setView(v);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "in method", Toast.LENGTH_SHORT).show();

                String description;
                description = input.getText().toString();

                int bitmap = 0;

                RadioButton checkedButton = (RadioButton) v.findViewById(radioGroup.getCheckedRadioButtonId());

                switch (checkedButton.getId()){
                    case R.id.interest_radio_button:
                        bitmap = R.drawable.ic_explore_white_48dp;
                        break;

                    case R.id.danger_radio_button:
                        bitmap = R.drawable.ic_close_white_48dp;
                        break;
                }

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(description)
                        .icon(BitmapDescriptorFactory.fromResource(bitmap)));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void goBack(View v){
        onBackPressed();
    }
}
