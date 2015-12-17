package cbpos1989.com.offroadtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

public class MainMenu extends AppCompatActivity {
    private final String TAG = "MainMenu";
    private final String FILENAME = "route.gpx";
    private final String USER_PREFERENCES = "userOptions";
    private MainMenu thisActivity = this;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Log.i(TAG, "In Main Menu");
        sharedpreferences = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    protected void onDestroy(){
        File routeFile = new File(this.getFilesDir(), FILENAME);
        boolean routeDeleted = routeFile.delete();
        Log.i("Deleted", "Route Deleted");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickMapButton(View view){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USER_PREFERENCES,"Live");
        editor.commit();

        Intent mapActivity = new Intent(this, MapsActivity.class);
        startActivity(mapActivity);

    }

    public void onClickLoadRoute(View view){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USER_PREFERENCES,"Load");
        editor.commit();

        Intent mapActivity = new Intent(this, MapsActivity.class);
        startActivity(mapActivity);

    }


    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Do you want to quit app?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MapsActivity.firstCoord = true;
                    finish();
                    File routeFile = new File(getFilesDir(), FILENAME);
                    boolean routeDeleted = routeFile.delete();
                    System.exit(0);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();



            return  true;
        }

        return super.onKeyDown(keyCode,event);
    }


}
