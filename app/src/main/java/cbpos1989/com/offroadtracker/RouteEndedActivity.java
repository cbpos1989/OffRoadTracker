package cbpos1989.com.offroadtracker;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RouteEndedActivity extends AppCompatActivity {
    private final String TAG = "RouteEndedActivity";
    private final String FILENAME = "route.gpx";
    private final String DIRECTORY = "/off-road_tracker_routes";
    private final String FILETYPE = ".gpx";
    private File routeFile;
    private ArrayList<Object> points = new ArrayList<Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_ended);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        points = (ArrayList<Object>) bundle.getSerializable("pointsList");
    }

    public void discardRoute(View view){
        Toast.makeText(this,"Route Discarded",Toast.LENGTH_SHORT).show();
        routeFile = new File(getFilesDir(), FILENAME);
        routeFile.delete();
        points.clear();
        onBackPressed();
    }

    public void saveRoute(View view){
        Toast.makeText(this,points.size() + "",Toast.LENGTH_SHORT).show();


        EditText nameField = (EditText) findViewById(R.id.route_name_field);
        String fileName = nameField.getText().toString();

        routeFile = new File(getFilesDir(), FILENAME);

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + DIRECTORY);
        myDir.mkdirs();

        File file = new File(myDir,fileName + FILETYPE);

        GPXWriter gpxFile = new GPXWriter();
        try {
            file.createNewFile();
            gpxFile.writePath(file, fileName, points);

            Log.i(TAG, "Route Saved " + file.getName());

        } catch (Exception e) {
            Log.e("WritingFile", "Not completed writing" + file.getName());
        }

        onBackPressed();
    }
}
