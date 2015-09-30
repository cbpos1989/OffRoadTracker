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
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

//import com.google.android.gms.location.LocationListener;

/**
 * Created by Alex Scanlan & Colm O'Sullivan on 28/09/2015.
 */
public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener {

    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationRequest mLocationRequest;
    private Location mLocation;
    private ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
    private boolean firstLoc = true;
    private boolean startStopLoc = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        firstLoc = true;

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null){
            try{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
            }catch(SecurityException se){
                se.printStackTrace();
            }
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setUpMapIfNeeded();

        try {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocationGPS != null) {
                onLocationChanged(lastKnownLocationGPS);
                Toast.makeText(this, lastKnownLocationGPS.toString(), Toast.LENGTH_SHORT).show();
            }
        }catch(SecurityException se){
            se.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

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

    private void setUpMap() { mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if(mLocation != null) {
            mLocation = location;
        }
        LatLng latLng = new LatLng(latitude, longitude);

        //Toast.makeText(this, "Lat: " + latitude + " Long: " + longitude, Toast.LENGTH_SHORT).show();
        coordinates.add(new Coordinate(latitude, longitude));
        //Toast.makeText(this, location.toString(), Toast.LENGTH_LONG).show();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));

        if(firstLoc) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            firstLoc = false;
        }
        if (coordinates.size() > 1) {
            drawLine();
        }
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
        //LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void drawLine() {
        //Toast.makeText(this, "In Draw Line Method", Toast.LENGTH_SHORT).show();
        Coordinate prevCoordinates = coordinates.get(coordinates.size() - 2);
        Coordinate currCoordinates = coordinates.get(coordinates.size() - 1);

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(prevCoordinates.getLatitude(), prevCoordinates.getLongitude()),
                        new LatLng(currCoordinates.getLatitude(), currCoordinates.getLongitude()))
                .strokeWidth(5)
                .strokeColor(Color.RED));

    }

    public void stopLocationListener(View view) {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Button button = (Button) findViewById(R.id.stopLocListenerBtn);

        if(startStopLoc){
            Toast.makeText(this, "IN STOP", Toast.LENGTH_SHORT).show();

            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

            button.setText("START");

            startStopLoc = !startStopLoc;
        } else{
            Toast.makeText(this, "IN START", Toast.LENGTH_SHORT).show();

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
            } catch (SecurityException se) {
                se.printStackTrace();
            }

            button.setText("STOP");

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
}
