package com.example.chris.situaware;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //JSON array for parsed incidents
    JSONArray parsedIncidents = null;

    Incident[] incidents;

    // url to save report
    private static final String url_get_incidents = "http://10.0.2.2:80//situaware/get_all_incidents.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_INCIDENTS = "incidents";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        new LoadAllIncidents().execute();
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
                    // looping through All Products
                    for (int i = 0; i < parsedIncidents.length(); i++) {
                        JSONObject c = parsedIncidents.getJSONObject(i);

                        // Storing each json item in variable
                        //String id = c.getString(TAG_PID);
                        //String name = c.getString(TAG_NAME);

                        int incidentCode = c.getInt("incident_code");
                        int incidentDetail = c.getInt("detail_code");
                        String time = c.getString("incident_time");
                        String latitude = String.valueOf(c.getLong("incident_latitude"));
                        String longitude = String.valueOf(c.getLong("incident_longitude"));

                        Incident inc = new Incident(incidentCode, incidentDetail, time, latitude, longitude);
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
            LatLng hkr = new LatLng(Double.valueOf(incidents[0].getLatitude()), Double.valueOf(incidents[0].getLongitude()));
            mMap.addMarker(new MarkerOptions().position(hkr).title(incidents[0].getIncidentType()));

        }

    }
}
