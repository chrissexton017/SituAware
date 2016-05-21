package com.example.chris.situaware;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    LocationManager mLocationManager;
    Location mLastLocation;

    private final String SHARED_PREFERENCES_NAME = "ourPrefs";

    private Integer[] mThumbIds = {
            R.drawable.reportbutton,
            R.drawable.track,
            R.drawable.capture,
            R.drawable.info
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context mContext = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        fab.hide();

        //Create a location manager
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // Check if the Access Fine Location permission has been granted by the user
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If it hasn't, request the permission

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            // The callback method gets the
            // result of the request. See method OnRequestPermissionsResult below.

            //if the permission is already granted, we will get the current location and write it to SharedPreferences
            //this can then be used as universal default location in case of problems with location updates.
        } else {
            mLastLocation = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("LastLatitude", String.valueOf(mLastLocation.getLatitude()));
                editor.putString("LastLongitude", String.valueOf(mLastLocation.getLongitude()));
                editor.commit();
            } else{
                //WE COULD NOT GET A LOCATION
            }
        }

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        //set the grid view's adapter as a new instance of custom MyAdapter class
        gridview.setAdapter(new MyAdapter(this));
        gridview.setNumColumns(2);
        //the OnItemClickListener listens for clicks on individual items in the grid view and the if statements open the relevant
        //activity based on the position of the clicked item (see array positions above)
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent(mContext, ReportIncidentActivity.class);
                    startActivity(intent);
                } else if (position == 1) {
                    //open activity for tracking
                    Intent intent = new Intent(mContext, TrackingActivity.class);
                    startActivity(intent);
                } else if (position == 2) {
                    //open activity for capturing
                    Intent intent = new Intent(mContext, CaptureActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted. get current location and save to SharedPreferences.
                    try {
                        mLastLocation = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
                        if (mLastLocation != null) {
                            SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("LastLatitude", String.valueOf(mLastLocation.getLatitude()));
                            editor.putString("LastLongitude", String.valueOf(mLastLocation.getLongitude()));
                            editor.commit();
                        }
                    }catch(SecurityException ex) {
                        System.out.println("SECURITY EXCEPTION 2" + ex.toString());
                    }

                } else {
                    //permission was denied. we will not be able to use location services.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public class MyAdapter extends BaseAdapter {

        private Context mContext;

        public MyAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int arg0) {
            return mThumbIds[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View grid;

            if(convertView==null){
                grid = new View(mContext);
                LayoutInflater inflater=getLayoutInflater();
                grid=inflater.inflate(R.layout.grid_item, parent, false);
            }else{
                grid = (View)convertView;
            }
            //sets ImageButton (with id "imageButton" in grid_item.xml) to the image at mThumbIds[position]
            ImageButton imageButton = (ImageButton)grid.findViewById(R.id.imageButton);
            imageButton.setImageResource(mThumbIds[position]);
            if(position == 0) {
                imageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(mContext, ReportIncidentActivity.class);
                        startActivity(intent);
                    }

                });
            }
            if(position == 1) {
                imageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(mContext, TrackingActivity.class);
                        startActivity(intent);
                    }

                });
            }
            if(position == 2) {
                imageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(mContext, CaptureActivity.class);
                        startActivity(intent);
                    }

                });
            }

            return grid;
        }
    }

}
