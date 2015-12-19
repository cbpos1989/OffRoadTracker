package cbpos1989.com.offroadtracker;

import android.os.AsyncTask;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Colm on 19/12/2015.
 */
public class DrawMarkers extends AsyncTask<Object,Void,Void> {
    private final String TAG = "DrawMarkers";
    private static final String FIREBASE_URL = "https://offroad-tracker.firebaseio.com/markers";
    private Firebase mFirebase;
    private GoogleMap mMap;
    private String mLastUpdateTime;

    private ArrayList<Marker> mMarkerList = new ArrayList<>();

    @Override
    protected Void doInBackground(Object... params) {
        mMap = (GoogleMap) params[1];

        //Setting up Firebase
        Firebase.setAndroidContext((MapsActivity)params[0]);
        mFirebase = new Firebase(FIREBASE_URL);

        initializeMarkers();
        return null;
    }

    private void initializeMarkers(){
        mFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(TAG, "Data Count: " + snapshot.getChildrenCount());
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
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
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }



    private int assignBitmap(String markerType){
        switch (markerType){
            case "point_of_interest":   return R.drawable.ic_point_of_interest_48dp;
            case "danger": return R.drawable.ic_close_white_48dp;
        }
        return 0;
    }
}
