package cbpos1989.com.offroadtracker;

import android.content.Context;
import android.graphics.Color;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;



//import com.google.android.gms.location.LocationListener;

/**
 * Created by Alex Scanlan & Colm O'Sullivan on 28/09/2015.
 */
public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationRequest mLocationRequest;
    private Location mLocation;
    private ArrayList<Location> points = new ArrayList<Location>();
    private Polyline route;
    private final String filename = "route.gpx";

    private static LatLng prevCoordinates;

    File routeFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 2, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setUpMapIfNeeded();

        try {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocationGPS != null) {

                prevCoordinates = new LatLng(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevCoordinates, 18));
                //onLocationChanged(lastKnownLocationGPS);
            }
        }catch(SecurityException se){
            se.printStackTrace();
        }

        points.clear();
        routeFile = new File(this.getFilesDir(), filename);
        GPXReader gpxReader = new GPXReader(this);
        gpxReader.readPath(routeFile);
        //routeFile.deleteOnExit();

        ArrayList<LatLng> polylinePoints = (ArrayList<LatLng>) gpxReader.getPoints();
        if(polylinePoints.size() > 1){
            prevCoordinates = polylinePoints.get(0);
            for(int i = 0; i < polylinePoints.size();++i){
                Log.i("Array Sizes", i + "Reader Array: " + gpxReader.getPoints().size() + " Polyline Array: " + polylinePoints.size());

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
        GPXWriter gpxFile = new GPXWriter(this);
        try {
            routeFile = new File(this.getFilesDir(),filename);
            routeFile.createNewFile();
            gpxFile.writePath(routeFile, "GPX_Route", points);
            route.remove();

            Toast.makeText(this,"Finished writing" + filename,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
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

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if(mLocation != null) {
            mLocation = location;
        }

        LatLng latLng = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        points.add(location);

        drawLine(location);
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void drawLine(Location location) {
        LatLng currCoordinates = new LatLng(location.getLatitude(),location.getLongitude());

        //Toast.makeText(this, prevCoordinates.toString() + "//// " + currCoordinates.toString(),Toast.LENGTH_SHORT).show();


        Polyline liveRoute = mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(prevCoordinates)
                        .add(currCoordinates)
        );

        liveRoute.setColor(Color.RED);
        liveRoute.setWidth(2.5F);
        Log.i("Route Drawing", "Drawing from " + prevCoordinates + " to " + currCoordinates);
        prevCoordinates = currCoordinates;
    }

    private void drawLine(LatLng latlng) {

        LatLng currCoordinates = latlng;


        //Toast.makeText(this, prevCoordinates.toString() + "//// " + currCoordinates.toString(),Toast.LENGTH_SHORT).show();


        route = mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(prevCoordinates)
                        .add(currCoordinates)
        );

        route.setColor(Color.RED);
        route.setWidth(2.5F);
        Log.i("Route Drawing", "Drawing from " + prevCoordinates + " to " + currCoordinates);
        prevCoordinates = currCoordinates;


    }

    public void stopLocationListener(View view) {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

}
