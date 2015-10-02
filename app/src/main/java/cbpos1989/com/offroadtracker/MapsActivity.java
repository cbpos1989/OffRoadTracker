package cbpos1989.com.offroadtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.ArrayList;

//import com.google.android.gms.location.LocationListener;

/**
 * Created by Alex Scanlan & Colm O'Sullivan on 28/09/2015.
 *
 */
public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener {

    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location mLocation;
    private ArrayList<Location> points = new ArrayList<Location>();
    private Polyline route;

    private final String filename = "route.gpx";
    private boolean startStopLoc = false;
    static boolean firstCoord = true;
    private static LatLng prevCoordinates;

    File routeFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setUpMapIfNeeded();

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //firstCoord = sharedPreferences.getBoolean("first_coord", true);
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

        loadcurrentRoute();
    }
    /**
     * Retrieves gpx file from internal app files and sends file to parser
     * an ArrayList of LatLng is returned and used in for loop to redraw polylines route.
     */
    private void loadcurrentRoute(){
        points.clear();
        routeFile = new File(this.getFilesDir(), filename);
        GPXReader gpxReader = new GPXReader(this);
        gpxReader.readPath(routeFile);

        ArrayList<LatLng> polylinePoints = (ArrayList<LatLng>) gpxReader.getPoints();
        if(polylinePoints.size() > 1){
            prevCoordinates = polylinePoints.get(0);
            mMap.addMarker(new MarkerOptions().position(polylinePoints.get(0)).title("Marker"));
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

            if(route != null) {
                route.remove();
            }
            Log.i("WritingFile","Finished writing" + filename);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //SharedPreferences.Editor editor = sharedPreferences.edit();
            //editor.putBoolean("first_coord", firstCoord);
            //editor.apply();
            Log.i("drawLine","Value of commited firstCoord: " + firstCoord);

            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

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

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if(mLocation != null) {
            mLocation = location;
        }
        LatLng latLng = new LatLng(latitude, longitude);



        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        points.add(location);
        Log.i("onLocationChanged","Reached onLocationChanged before drawLine()");
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

        //createLocationRequest();
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


        if(firstCoord){
            Log.i("drawLine", "Reached drawLine if statement");
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

    private void drawLine(LatLng latlng) {

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

        if(startStopLoc){
            Toast.makeText(this, "IN STOP", Toast.LENGTH_SHORT).show();

            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

            button.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);

            startStopLoc = !startStopLoc;
        } else{
            Toast.makeText(this, "IN START", Toast.LENGTH_SHORT).show();

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

            button.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);

            startStopLoc = !startStopLoc;
        }
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
//        mMap.addMarker(new MarkerOptions().position(latLng).title(
//                latLng.toString()));

//        Toast.makeText(getApplicationContext(),
//                "New marker added@" + latLng.toString(), Toast.LENGTH_LONG)
//                .show();

        //---------------CRAZY CODE BELOW THIS POINT-----------------------

        // Init the dialog object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Enter description");

        // Set up the input

        LayoutInflater factory = LayoutInflater.from(getApplicationContext());

        final View v = factory.inflate(R.layout.dialog_layout, null);
        final EditText input = (EditText) v.findViewById(R.id.dialog_edit_text);
        final RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radio_group_dialog);
        //input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(v);

        //builder.set

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


                //Bitmap bitmap = drawableToBitmap(checkedButton.getBackground());

                //checkedButton.getButtonDrawable()

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

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void goBack(View v){
        onBackPressed();
    }
}
