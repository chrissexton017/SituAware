package com.example.chris.situaware;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportIncidentActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    String[] spinners = {"Incident", "Incident Detail", "Time", "Place", "Ongoing"};
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String mLatitude;
    String mLongitude;
    String mIncidentCode;
    String mIncidentDetail;
    String mTime = "CURRENT_TIMESTAMP";
    java.sql.Timestamp qTime;

    Long time;
    // Progress Dialog
    private ProgressDialog pDialog;

    private Spinner mSpinner;
    private Spinner mSpinner2;
    private Spinner mSpinner3;
    private Spinner mSpinner4;

    private HashMap<Integer, Integer> spinnerMap = new HashMap<>();

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // url to save report
    private static final String url_save_report = "http://10.0.2.2:80//situaware/save_incident_report.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
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
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitude = String.valueOf(mLastLocation.getLatitude());
                mLongitude = String.valueOf(mLastLocation.getLongitude());
            }
        } catch(SecurityException ex) {
            System.out.println("SECURITY EXCEPTION");
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

    /*protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>()) {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates= result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        ...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    OuterClass.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        ...
                        break;
                }
            }
        });
    }*/

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
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    public void submitReport(View view) {
        mIncidentCode = String.valueOf(mSpinner.getSelectedItemPosition()+1);
        mIncidentDetail = String.valueOf(mSpinner2.getSelectedItemPosition()+1);
        AlertDialog.Builder alert = new AlertDialog.Builder(
                this);
        alert.setTitle("Submit report?");
        alert.setMessage("Are you sure you want to submit this incident report?");
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


}
