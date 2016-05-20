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
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportIncidentActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    String[] spinners = {"Incident", "Incident Detail", "Time", "Place", "Ongoing"};
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String mLatitude = "";
    String mLongitude = "";
    //for emulator use
    String dummyLat = String.valueOf(56.048495);
    String dummyLong = String.valueOf(14.147706);
    String mIncidentCode;
    String mIncidentDetail;
    String mTime = "CURRENT_TIMESTAMP";
    java.sql.Timestamp qTime;
    private String ANDROID_ID = "test";

    Long time;
    // Progress Dialog
    private ProgressDialog pDialog;

    LocationListener mLocationListener;
    LocationManager mLocationManager;
    private TextView mLocationTextView;
    private Spinner mSpinner;
    private Spinner mSpinner2;
    private Spinner mSpinner3;
    private Spinner mSpinner4;

    private HashMap<Integer, Integer> spinnerMap = new HashMap<>();

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // url to save report
    private static final String url_save_report = "http://situaware.kodstack.com/save_incident_report.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private final String SHARED_PREFERENCES_NAME = "ourPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);
        mLocationTextView = (TextView)findViewById(R.id.textView5);

        if(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)!=null) {
            ANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        /* Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/

        //Create a location manager for this activity
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // We will check again if access fine location permission has been granted (should have been during MainActivity).
        // If not, ask again.
        if (ContextCompat.checkSelfPermission(ReportIncidentActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportIncidentActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            //result of permissions request handled below in onRequestPermissionResult

        } else{
            //permission has been granted (should test true in majority of cases)
            // get current location and display it in incident location text view as default
            mLastLocation = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                mLatitude = String.valueOf(mLastLocation.getLatitude());
                mLongitude = String.valueOf(mLastLocation.getLongitude());
                mLocationTextView.setText(getCityFromCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
            else{
                //LOCATION CAME BACK NULL: TRY SharedPreferences
            }
        }
        //if permission has been granted this should work
        try {
            mLocationListener = new MyLocationListener();
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
        }catch(SecurityException ex) {

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Report an Incident");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSpinner = (Spinner)findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.incident_type_array, R.layout.spinner_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner2 = (Spinner)findViewById(R.id.spinner2);
        mSpinner2.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> spinner2Adapter = ArrayAdapter.createFromResource(this,
                R.array.accident_detail_array, R.layout.spinner_item);
        mSpinner2.setAdapter(spinner2Adapter);
        mSpinner3 = (Spinner)findViewById(R.id.spinner3);
        mSpinner3.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> spinner3Adapter = ArrayAdapter.createFromResource(this,
                R.array.incident_time_array, R.layout.spinner_item);
        mSpinner3.setAdapter(spinner3Adapter);
        mSpinner4 = (Spinner)findViewById(R.id.spinner4);
        ArrayAdapter<CharSequence> spinner4Adapter = ArrayAdapter.createFromResource(this,
                R.array.incident_location_array, R.layout.spinner_item);
        mSpinner4.setAdapter(spinner4Adapter);
        mSpinner4.setOnItemSelectedListener(this);

        spinnerMap.put(0, R.array.accident_detail_array);
        spinnerMap.put(1, R.array.antisocial_detail_array);
        spinnerMap.put(2, R.array.burglary_detail_array);
        spinnerMap.put(3, R.array.explosion_detail_array);
        spinnerMap.put(4, R.array.fire_detail_array);
        spinnerMap.put(5, R.array.suspicious_detail_array);
        spinnerMap.put(6, R.array.theft_detail_array);
        spinnerMap.put(7, R.array.vandalism_detail_array);
        spinnerMap.put(8, R.array.violence_detail_array);
        spinnerMap.put(9, R.array.weapon_detail_array);
    }

    @Override
    protected void onStart() {
        //mGoogleApiClient.connect();
        super.onStart();
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(Double.parseDouble(mLatitude),
                    Double.parseDouble(mLongitude), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = mLatitude + "\n" + mLongitude + "\n\nMy Current City is: "
                + cityName;
        mLocationTextView.setText(s);
    }

    @Override
    protected void onStop() {
        // mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        /*try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitude = String.valueOf(mLastLocation.getLatitude());
                mLongitude = String.valueOf(mLastLocation.getLongitude());
            }
       */
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
                            mLocationTextView.setText(getCityFromCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
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

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        System.out.println(result.toString());
        mLatitude = String.valueOf(56.048495);
        mLongitude = String.valueOf(14.147706);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        if(parent == mSpinner) {
            ArrayAdapter<CharSequence> spinner2Adapter = ArrayAdapter.createFromResource(this,
                    spinnerMap.get(mSpinner.getSelectedItemPosition()), R.layout.spinner_item);
            mSpinner2.setAdapter(spinner2Adapter);
        }
        if(item.equals("Happened earlier (choose time)")) {
            final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                    TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                    Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth(),
                            timePicker.getCurrentHour(),
                            timePicker.getCurrentMinute());

                    time = calendar.getTimeInMillis();
                    qTime = new java.sql.Timestamp(calendar.getTimeInMillis());
                    mTime = "'"+qTime.toString()+"'";
                    alertDialog.dismiss();
                }});
            alertDialog.setView(dialogView);
            alertDialog.show();

        }
        if(item.equals("Another place (choose location)")) {
            Intent i = new Intent(this,  TrackingActivity.class);
            i.putExtra(TrackingActivity.MODE, "pick");
            startActivityForResult(i, 1);
        }
        if(item.equals("My current location")) {
            if(mLastLocation!=null){
                mLocationTextView.setText(getCityFromCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    public String getCityFromCoordinates(double Latitude, double Longitude) {
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(Latitude,
                    Longitude, 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = "Current incident location:"+ "\n" +Latitude + "\n" + Longitude + "\n\nLocale: "
                + cityName;
        return s;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String loc=data.getStringExtra("pickedloc");
                String location = loc.substring(10, loc.length() - 1);
                mSpinner4.setPrompt(loc);
                String[] latlong = location.split(",");
                mLatitude = latlong[0];
                mLongitude = latlong[1];
                System.out.println("PICKED LOCATION: " + location);
                //convert co-ords to city name and update incident location text view with these details
                mLocationTextView.setText(getCityFromCoordinates(Double.parseDouble(latlong[0]), Double.parseDouble(latlong[1])));
            }
        }
    }

    public void submitReport(View view) {
        mIncidentCode = String.valueOf(mSpinner.getSelectedItemPosition()+1);
        mIncidentDetail = String.valueOf(mSpinner2.getSelectedItemPosition()+1);
        if(mLatitude.isEmpty()) {
            System.out.println("SIMULATING LOCATION");
            mLatitude = dummyLat;
            mLongitude = dummyLong;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(
                this);
        alert.setTitle("Submit report?");
        alert.setMessage("Are you sure you want to submit this incident report? \n"+mLatitude+"\n"+mLongitude);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SaveReport().execute();
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SaveReport extends AsyncTask<Void, Void, Boolean> {

        /*private final String mEmail;
        private final String mPassword;
        private final String mTeamName;
        private final String mCountry;
        private final String mCity;*/

        /*SaveReport(String email, String password, String teamName, String country, String city) {
            mEmail = email;
            mPassword = password;
            mTeamName = teamName;
            mCountry = country;
            mCity = city;
        }*/

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ReportIncidentActivity.this);
            pDialog.setMessage("Saving report");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Building Parameters
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("incident_code", mIncidentCode));
            parameters.add(new BasicNameValuePair("detail_code", mIncidentDetail));
            parameters.add(new BasicNameValuePair("incident_time", mTime));
            parameters.add(new BasicNameValuePair("incident_latitude", mLatitude));
            parameters.add(new BasicNameValuePair("incident_longitude", mLongitude));
            parameters.add(new BasicNameValuePair("android_id", ANDROID_ID));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_save_report,
                    "POST", parameters);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    return true;
                } else {
                    // failed to create product
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //mAuthTask = null;
            //showProgress(false);
            pDialog.dismiss();
            if (success) {
                // successfully created product
                //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                //intent.putExtra(LoginActivity.NEW_USER, "Yes");
                //intent.putExtra(LoginActivity.NEW_USER_MAIL, mEmail);
                //startActivity(intent);
                finish();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
            //showProgress(false);
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
