package com.example.chris.situaware;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String SHARED_PREFERENCES_NAME = "ourPrefs";

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //JSON array for parsed incidents
    JSONArray parsedIncidents = null;

    Incident[] incidents;

    String[] incidentTypes;

    Context mContext;
    // url to save report
    private static final String url_get_incidents = "http://situaware.kodstack.com/get_all_incidents.php";

    //mode for map: tracking or location picking
    public static String MODE="Track";

    LocationListener mLocationListener;
    LocationManager mLocationManager;
    Location mLastLocation;
    String mLatitude = "";
    String mLongitude = "";
    MarkerOptions mPosition;
    Marker mMarker;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_INCIDENTS = "incidents";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        if(getIntent().hasExtra(MODE)) {
            MODE="pick";
        }
        //Create a location manager
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(TrackingActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(TrackingActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        }

        try {
            mLocationListener = new MyLocationListener();
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
        }catch(SecurityException ex) {

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext = this;
        incidentTypes = getResources().getStringArray(R.array.incident_type_array);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mLocationListener = new MyLocationListener();
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
                        mLastLocation = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
                        if (mLastLocation != null) {
                            mLatitude = String.valueOf(mLastLocation.getLatitude());
                            mLongitude = String.valueOf(mLastLocation.getLongitude());
                        }
                    }catch(SecurityException ex) {
                        System.out.println("SECURITY EXCEPTION 2" + ex.toString());
                    }

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        SharedPreferences sharedPref = TrackingActivity.this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(sharedPref.contains("LastLatitude")) {
            System.out.println(sharedPref.getString("LastLatitude", "DIABOLICAL"));
        }
        String lat = sharedPref.getString("LastLatitude", String.valueOf(56.048495));
        String lon = sharedPref.getString("LastLongitude", String.valueOf(14.147706));
        LatLng myLoc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
        float f = (float)10.0;
        mPosition = new MarkerOptions()
                .position(myLoc)
                .title("Your present location")
                .snippet("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMarker = mMap.addMarker(mPosition);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        mMap.moveCamera((CameraUpdateFactory.zoomTo(f)));
        if(MODE!="pick") {
            new LoadAllIncidents().execute();
        }
        else{
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {
                    Toast.makeText(getApplicationContext(), point.toString(), Toast.LENGTH_SHORT).show();
                    final LatLng pt = point;
                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            mContext);
                    alert.setTitle("Confirm location");
                    alert.setMessage("Is " + point.toString() + " where the incident occured?");
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra("pickedloc", pt.toString());
                            setResult(RESULT_OK, intent);
                            finish();
                            dialog.dismiss();

                        }
                    });
                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            });
        }
    }



    class LoadAllIncidents extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest(url_get_incidents, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    parsedIncidents = json.getJSONArray(TAG_INCIDENTS);
                    incidents = new Incident[parsedIncidents.length()];
                    System.out.println("INCIDENTS: "+incidents.length);
                    // looping through All Products
                    for (int i = 0; i < parsedIncidents.length(); i++) {
                        JSONObject c = parsedIncidents.getJSONObject(i);

                        // Storing each json item in variable
                        //String id = c.getString(TAG_PID);
                        //String name = c.getString(TAG_NAME);

                        int incidentCode = c.getInt("incident_code");
                        int incidentDetail = c.getInt("detail_code");
                        String time = c.getString("incident_time");
                        String latitude = c.getString("incident_latitude");
                        String longitude = c.getString("incident_longitude");
                        System.out.println("LAT = "+latitude);
                        Incident inc = new Incident(incidentCode, incidentDetail, time, latitude, longitude, mContext);
                        incidents[i] = inc;

                        // creating new HashMap
                        //HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        //map.put(TAG_PID, id);
                        //map.put(TAG_NAME, name);

                        // adding HashList to ArrayList
                        //productsList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            // Add a marker in Sydney and move the camera
            for(Incident i: incidents){
                LatLng ltlng = new LatLng(Double.valueOf(i.getLatitude()), Double.valueOf(i.getLongitude()));
                mMap.addMarker(new MarkerOptions()
                        .position(ltlng)
                        .title(incidentTypes[i.getIncidentType()])
                        .snippet(i.getIncidentDetail()));
            }


        }

    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            //editLocation.setText("");
            //pb.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            //Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            //Log.v(TAG, latitude);

            LatLng myLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMarker.remove();
            float f = (float)10.0;
            mPosition = new MarkerOptions()
                    .position(myLoc)
                    .title("Your present location")
                    .snippet("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMarker = mMap.addMarker(mPosition);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
            mMap.moveCamera((CameraUpdateFactory.zoomTo(f)));

        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                    + cityName;
            // editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

}
