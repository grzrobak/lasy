package com.robak.lasygps;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.robak.lasygps.com.robak.lasygps.domain.ForestData;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    private RequestQueue mRequestQueue;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private final String USER_AGENT = "Mozilla/5.0";

    // UI elements
    private TextView txtNadlesnictwo;
    private TextView txtLesnictwo;
    private TextView txtOddzial;
    private TextView txtPododzial;
    private TextView txtArea;
    private TextView txtTreeCode;
    private TextView txtTreeAge;
    private TextView txtDataAge;
    private TextView txtErrorText;
    private Button btnSearch;

    private LesnictwoUrl lesnictwoUrl;

    private ProgressBar pb;
    private ForestData forestData;

    private void getOddzial(final String latitude, final String longitude) {

        mRequestQueue.cancelAll("abc");
        final String latitudeMock = "16.48363497723";
        final String longitudeMock = "54.183357627217";

        String urlOddzial = "http://mapserver.bdl.lasy.gov.pl/arcgis/rest/services/BDL_2_0/MapServer/16/query?where=&text=&objectIds=&time=&geometry="+ latitudeMock + "%2C+" + longitudeMock +"&geometryType=esriGeometryPoint&inSR=4326&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=subarea_id%2C+arodes_int_num%2C+adress_forest%2C+area_type_cd%2C+site_type_cd%2C+silviculture_cd%2C+forest_func_cd%2C+stand_struct_cd%2C+rotation_age%2C+sub_area%2C+prot_category_cd%2C+species_cd_d%2C+part_cd%2C+species_age%2C+a_year&returnGeometry=false&maxAllowableOffset=&geometryPrecision=&outSR=4326&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";
        String urlLesnictwo = "http://www.bdl.lasy.gov.pl/portal/BULiGL.BDL.Reports/Report/StandDescriptionData?arodesIntNum=1123028174&aYear=2014&adress_forest=11-23-1-09-574%20%20%20-i%20%20%20-00&jointOwnership=false";

        JsonObjectRequest oddzialJsonRequest = getOddzialJsonRequest(urlOddzial);
        oddzialJsonRequest.setTag("abc");
        oddzialJsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000,3,1.0F));



        // Add the request to the RequestQueue.
        System.out.println("Pytanie do mapServer");
        mRequestQueue.add(oddzialJsonRequest);

    }

    @NonNull
    private JsonObjectRequest getLesnictwolJsonRequest(String url) {
        JSONObject attrib = new JSONObject();

        return new JsonObjectRequest( url, attrib,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String lesnictwo = response.getJSONArray("headerLP").getJSONObject(0).getString("forest_range_name");
                            String nadlesnictwo = response.getJSONArray("headerLP").getJSONObject(0).getString("inspectorate_name");
                            forestData.setLesnictwo(lesnictwo);
                            forestData.setNadlesnictwo(nadlesnictwo);
                            updateDisplayedForestData(forestData);
                            pb.setVisibility(ProgressBar.INVISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //"adress_forest": "11-23-1-09-574   -b   -00"
                        System.out.println(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getMessage());
                        System.out.println(error.getCause());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        pb.setVisibility(ProgressBar.INVISIBLE);
                    }
                });
    }

    @NonNull
    private JsonObjectRequest getOddzialJsonRequest(String url) {
        JSONObject attrib = new JSONObject();

        return new JsonObjectRequest( url, attrib,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    forestData = new ForestData(response);

                    updateDisplayedForestData(forestData);

                    lesnictwoUrl = new LesnictwoUrl(forestData.getArodes_int_num(),
                            forestData.getDataAge(),
                            forestData.getForestAddress());
                    System.out.println("Pytanie o Leśnictwo");

                    JsonObjectRequest lesnictwoJsonRequest = getLesnictwolJsonRequest(lesnictwoUrl.getUrl());
                    lesnictwoJsonRequest.setTag("abc");
                    lesnictwoJsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));
                    mRequestQueue.add(lesnictwoJsonRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //"adress_forest": "11-23-1-09-574   -b   -00"

             }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
                System.out.println(error.getCause());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                pb.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    private void clearDisplayedForestData()
    {
        txtNadlesnictwo.setText("");
        txtNadlesnictwo.setVisibility(TextView.INVISIBLE);
        txtLesnictwo.setText("");
        txtLesnictwo.setVisibility(TextView.INVISIBLE);
        txtOddzial.setText("");
        txtOddzial.setVisibility(TextView.INVISIBLE);
        txtPododzial.setText("");
        txtPododzial.setVisibility(TextView.INVISIBLE);
        txtArea.setText("");
        txtArea.setVisibility(TextView.INVISIBLE);
        txtTreeCode.setText("");
        txtTreeCode.setVisibility(TextView.INVISIBLE);
        txtTreeAge.setText("");
        txtTreeAge.setVisibility(TextView.INVISIBLE);
        txtDataAge.setText("");
        txtDataAge.setVisibility(TextView.INVISIBLE);
    }

    private void updateDisplayedForestData(ForestData forestData) {
        if(forestData.getNadlesnictwo() != null && forestData.getLesnictwo() != null) {
            txtNadlesnictwo.setText(forestData.getNadlesnictwo());
            txtNadlesnictwo.setVisibility(TextView.VISIBLE);
            txtLesnictwo.setText(forestData.getLesnictwo());
            txtLesnictwo.setVisibility(TextView.VISIBLE);
        }
        txtOddzial.setText(forestData.getOddzial());
        txtOddzial.setVisibility(TextView.VISIBLE);
        txtPododzial.setText(forestData.getPododdzal());
        txtPododzial.setVisibility(TextView.VISIBLE);
        txtArea.setText(forestData.getAreaSize());
        txtArea.setVisibility(TextView.VISIBLE);
        txtTreeCode.setText(forestData.getTreeCode());
        txtTreeCode.setVisibility(TextView.VISIBLE);
        txtTreeAge.setText(forestData.getTreeAge());
        txtTreeAge.setVisibility(TextView.VISIBLE);
        txtDataAge.setText(forestData.getDataAge());
        txtDataAge.setVisibility(TextView.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNadlesnictwo = (TextView) findViewById(R.id.txtNadlesnictwo);
        txtLesnictwo = (TextView) findViewById(R.id.txtLesnictwo);
        txtOddzial = (TextView) findViewById(R.id.txtOddzial);
        txtPododzial = (TextView) findViewById(R.id.txtPododdzial);
        txtArea = (TextView) findViewById(R.id.txtArea);
        txtTreeCode = (TextView) findViewById(R.id.txtTreeCode);
        txtTreeAge = (TextView) findViewById(R.id.txtTreeAge);
        txtDataAge = (TextView) findViewById(R.id.txtDataAge);
        txtErrorText = (TextView) findViewById(R.id.errorText);
        btnSearch = (Button) findViewById(R.id.btnSearch);

        pb = (ProgressBar) findViewById(R.id.pbLoading);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
            BasicNetwork network = new BasicNetwork(new HurlStack());
//335845.0148, 705044.3988
// Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);

// Start the queue
            mRequestQueue.start();
        }

        // Show location button click listener
        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearDisplayedForestData();
                pb.setVisibility(ProgressBar.VISIBLE);
                displayLocation();
            }
        });
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            txtErrorText.setText("");
            getOddzial(String.valueOf(latitude), String.valueOf(longitude));

        } else {

            txtErrorText
                    .setText("UWAGA! \n(Nie można ustalić lokalizacji. Upewnij się, że lokalizacja jest odblokowana w urządzeniu)");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        // displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

}