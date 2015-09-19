package com.robak.lasygps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.robak.lasygps.domain.ForestData;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Location updates intervals in sec
    private final static int UPDATE_INTERVAL = 10000; // 10 sec
    private final static int FATEST_INTERVAL = 5000; // 5 sec
    private final static int DISPLACEMENT = 10; // 10 meters

    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private RequestQueue mRequestQueue;

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

    private ProgressDialog progressDialog;
    private ForestData forestData;

    private void getOddzial( String latitude, String longitude) {

        mRequestQueue.cancelAll(TAG);
        
//      used for testing purposes:
//      String latitudeMock = "16.48363497723";
//      String longitudeMock = "54.183357627217";
//      latitude = latitudeMock;
//      longitude = longitudeMock;

        OddzialUrl oddzialUrl = new OddzialUrl(latitude, longitude);
        
        JsonObjectRequest oddzialJsonRequest = getOddzialJsonRequest(oddzialUrl.getUrl());


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
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            updateDisplayedForestData(forestData);
                            Toast.makeText(getApplicationContext(), "Nie można pobrać niektórych danych", Toast.LENGTH_SHORT).show();
                        }
                        //"adress_forest": "11-23-1-09-574   -b   -00"
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    @NonNull
    private JsonObjectRequest getOddzialJsonRequest(String url) {
        JsonObjectRequest request = new JsonObjectRequest( url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            forestData = new ForestData(response);

                            LesnictwoUrl lesnictwoUrl = new LesnictwoUrl(forestData);

                            JsonObjectRequest lesnictwoJsonRequest = getLesnictwolJsonRequest(lesnictwoUrl.getUrl());
                            lesnictwoJsonRequest.setTag(TAG);
                            lesnictwoJsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));
                            mRequestQueue.add(lesnictwoJsonRequest);
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            txtErrorText.setText("Aktualna pozycja nie znajduje się w rejestrze Banku Danych o Lasach." +
                                    "\n\nPonów wyszukiwanie w innym miejscu.");
                        }
                        //"adress_forest": "11-23-1-09-574   -b   -00"
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

        request.setTag(TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));

        return request;
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

        progressDialog = new ProgressDialog(this);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

//335845.0148, 705044.3988

            mRequestQueue = Volley.newRequestQueue(getApplicationContext());

// Start the queue
            mRequestQueue.start();
        }

        // Show location button click listener
        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearDisplayedForestData();
                progressDialog.setMessage("Szukam");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
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
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

}