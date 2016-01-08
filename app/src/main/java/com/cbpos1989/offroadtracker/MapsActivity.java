package com.cbpos1989.offroadtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class that will display the maps activity. Track the user's location and draw a path of the
 * user's route using polylines. User can also long click on the map to bring up the add marker
 * dialog and place custom markers on the map.
 *
 * Created by Alex Scanlan & Colm O'Sullivan on 28/09/2015.
 */
public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener, onPauseRoute{
    private static final String TAG = "MapsActivity";
    private final String FILENAME = "route.gpx";
    private final String USER_PREFERENCES = "saved_state";
    private String FIREBASE_URL;
    private final String FIREBASE_ROOT_NODE = "markers";
    SharedPreferences sharedPref;

    private Firebase mFirebase;
    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location mLocation;
    private String mLastUpdateTime;
    private ArrayList<Marker> mMarkerList = new ArrayList<>();
    private ArrayList<Object> points = new ArrayList<Object>();
    private Polyline route;
    private GPXReader gpxReader;

    private boolean stopLoc = false;
    private boolean liveRouteActive = false;
    private boolean routeFinished = true;
    private int moveCameraFactor = 10;
    static boolean firstCoord = true;
    private static LatLng prevCoordinates;

    private File routeFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Get user choice to either display demo route or live route
        sharedPref = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        String coords = sharedPref.getString("Coords",null);
        routeFinished = sharedPref.getBoolean("routeState",true);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Log.i(TAG, "RouteFinshed = " + routeFinished);




        //Log.i(TAG,"Coords from pref: " + coords);

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        ImageButton imageButton = (ImageButton) findViewById(R.id.playbackBtn);
        imageButton.setVisibility(View.INVISIBLE);

        //Setting up Google Map
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setUpMapIfNeeded();

        //Get points from saved state; clear map if new route
        if(!routeFinished) {
            if (bundle != null) {
                points = (ArrayList<Object>) bundle.getSerializable("pointsList");
                if (points != null) {
                    Log.i(TAG, "Points = " + points.size());
                } else {
                    points = new ArrayList<Object>();
                }
            }
        } else {
            mMap.clear();
        }

        //Setting up Firebase
        FIREBASE_URL = getString(R.string.firebase_url);
        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(FIREBASE_URL + FIREBASE_ROOT_NODE);
        new DrawMarkers(this,mMap).execute();
        //initializeMarkers();

       //Log.i("drawLine","Value of firstCoord" + firstCoord);

        //Log.i(TAG,"FirstCoord: " + firstCoord);
        //Move camera and set coordinates to last known position
        try {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(coords != null) {
                prevCoordinates = parseCoords(coords);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevCoordinates, 18));
            } else {
                if (lastKnownLocationGPS != null) {
                    prevCoordinates = new LatLng(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevCoordinates, 18));
                }
            }
        }catch(SecurityException se){
            se.printStackTrace();
        }


        gpxReader = new GPXReader();

        //Loads internal GPX File
        if(!routeFinished) {
            routeFile = new File(this.getFilesDir(), FILENAME);
            loadCurrentRoute(routeFile);
        }
    }

    /**
     * Retrieves gpx file from internal app files and sends file to parser
     * an ArrayList of LatLng is returned and used in for loop to redraw polylines route.
     * @param file takes in file of type .gpx
     */
    private void loadCurrentRoute(File file){
        //points.clear();

        gpxReader.readPath(file);

        ArrayList<LatLng> polylinePoints = (ArrayList<LatLng>) gpxReader.getPoints();

        Log.d(TAG, "PolylinePoints: " + polylinePoints.size());
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

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putBoolean("routeState", routeFinished);
        editor.commit();
        Log.i(TAG,"Route State Saved");

        //Load Internal GPX File
        if (!routeFinished) {
            routeFile = new File(this.getFilesDir(), FILENAME);
            saveCurrentRoute(routeFile);
        }

        if(routeFinished) {
            firstCoord = true;
        }

        super.onDestroy();
    }

    void saveCurrentRoute(File file){
        GPXWriter gpxFile = new GPXWriter();
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        if(!routeFinished) {
            Intent mainMenuActivity = new Intent(this, MainMenu.class);
            mainMenuActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putSerializable("pointsList", points);
            mainMenuActivity.putExtras(bundle);
            startActivity(mainMenuActivity);
            finish();
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
    void drawLine(LatLng latlng)  {

        LatLng currCoordinates = latlng;

        if(firstCoord){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currCoordinates, 18));
            mMap.addMarker(new MarkerOptions().position(currCoordinates).title("Marker"));
            prevCoordinates = currCoordinates;
            firstCoord = false;
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currCoordinates, 18));
        route = mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(prevCoordinates)
                        .add(currCoordinates)
        );

        route.setColor(Color.RED);
        route.setWidth(5.0F);
        //Log.i("Route Drawing", "Drawing from " + prevCoordinates + " to " + currCoordinates);
        prevCoordinates = currCoordinates;

    }

    public void stopLocationListener(View view) {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        ImageButton button = (ImageButton) findViewById(R.id.stopLocListenerBtn);

        if(stopLoc){
            pauseRoute(locationManager);
        } else{
            trackRoute(locationManager);

            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDialog();
                    return false;
                }
            });
        }
    }

    private void pauseRoute(LocationManager locationManager){
        Toast.makeText(this, "Pausing Route", Toast.LENGTH_SHORT).show();
        ImageButton button = (ImageButton) findViewById(R.id.stopLocListenerBtn);


        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }

        button.setImageResource(R.drawable.ic_navigation_red_48dp);

        stopLoc = !stopLoc;
    }

    private void trackRoute(LocationManager locationManager){

        routeFinished = false;
        liveRouteActive = true;

        ImageButton button = (ImageButton) findViewById(R.id.stopLocListenerBtn);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }


        button.setImageResource(R.drawable.ic_pause_red_48dp);

        stopLoc = !stopLoc;
    }

    private void stopRoute(){

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }


        //Reset Map
        MapsActivity.firstCoord = true;
        routeFinished = true;
        mMap.clear();

        Toast.makeText(this, "Route Finished", Toast.LENGTH_SHORT).show();

        Intent routeEndedActivity = new Intent(this, RouteEndedActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("pointsList",points);

        routeEndedActivity.putExtras(bundle);
        startActivity(routeEndedActivity);
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
                //Toast.makeText(getApplicationContext(), "in method", Toast.LENGTH_SHORT).show();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date();
                mLastUpdateTime = dateFormat.format(date).toString();
                String description;
                description = input.getText().toString();

                String markerType = "point_of_interest";

                RadioButton checkedButton = (RadioButton) v.findViewById(radioGroup.getCheckedRadioButtonId());

                switch (checkedButton.getId()) {
                    case R.id.interest_radio_button:
                        markerType = "point_of_interest";
                        break;
                    case R.id.warning_radio_button:
                        markerType = "warning";
                        break;
                    case R.id.trail_start_radio_button:
                        markerType = "trail_start";
                        break;
                    case R.id.obstacle_radio_button:
                        markerType = "obstacle";
                        break;
                    case R.id.dead_end_radio_button:
                        markerType = "dead_end";
                        break;
                }

                saveToFirebase(latLng, description, markerType);
                drawMarker();
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

    private void saveToFirebase(LatLng latLng, String description, String markerType){
        Map locations = new HashMap();
        locations.put("timestamp",mLastUpdateTime);
        locations.put("marker_type",markerType);
        locations.put("description", description);
        Map coordinate = new HashMap();
        coordinate.put("latitude",latLng.latitude);
        coordinate.put("longitude",latLng.longitude);
        locations.put("location",coordinate);
        mFirebase.push().setValue(locations);
    }



    private void drawMarker(){
        Log.i(TAG, "Time: " + mLastUpdateTime);
        Query queryRef = mFirebase.orderByChild("timestamp").startAt(mLastUpdateTime).limitToFirst(1);
        ChildEventListener childEventListener = queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Get data from Firebase
                Map data = (Map) dataSnapshot.getValue();
                String timestamp = (String) data.get("timestamp");
                String description = (String) data.get("description");
                String markerType = (String) data.get("marker_type");

                Map coordinate = (HashMap) data.get("location");
                double latitude = (double) (coordinate.get("latitude"));
                double longitude = (double) (coordinate.get("longitude"));

                LatLng latLng = new LatLng(latitude, longitude);

                //Add Marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(description)
                        .icon(BitmapDescriptorFactory.fromResource(assignBitmap(markerType)));
                Marker marker = mMap.addMarker(markerOptions);
                mMarkerList.add(marker);
                Log.i(TAG, "Markers on Map: " + mMarkerList.size());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private int assignBitmap(String markerType){
        switch (markerType){
            case "point_of_interest":   return R.drawable.ic_point_of_interest_48dp;
            case "warning": return R.drawable.ic_warning_48dp;
            case "trail_start":   return R.drawable.ic_trail_start_48dp;
            case "obstacle": return R.drawable.ic_obstacle_48dp;
            case "dead_end":   return R.drawable.ic_dead_end_48dp;
        }
        return 0;
    }

    @Override
    public void onPauseRoute(Integer count) {
    }


    /**
     * Dialog that will allow the user to stop tracking there route.
     */
    public static class StopTrackingDialogFragment extends DialogFragment {

        public static StopTrackingDialogFragment newInstance() {
            StopTrackingDialogFragment frag = new StopTrackingDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.stop_tracking_message)
                    .setPositiveButton(R.string.positive_button_message, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MapsActivity)getActivity()).doPositiveClick();
                        }
                    })
                    .setNegativeButton(R.string.negative_button_message, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MapsActivity)getActivity()).doNegativeClick();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    void showDialog() {
        DialogFragment newFragment = StopTrackingDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick() {
        stopRoute();
        //Log.i("FragmentAlertDialog", "Positive click!");
    }

    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }

    private LatLng parseCoords(String coords){
        //53.347132, -6.259146
        ArrayList<Double> coordsList = new ArrayList<>();

        Log.i(TAG,"Coords: " + coords);
        try {
            for (String str: coords.split(",")){
                str.trim();
                coordsList.add(Double.parseDouble(str));
            }
        } catch (NumberFormatException nfe){
            Toast.makeText(this,nfe.getMessage(),Toast.LENGTH_SHORT).show();
            return new LatLng(0,0);
        }

        return new LatLng(coordsList.get(0),coordsList.get(1));
    }
}




