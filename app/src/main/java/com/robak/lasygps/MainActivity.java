package com.robak.lasygps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.AndroidException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.robak.lasygps.domain.ForestData;
import com.robak.lasygps.domain.InspectorateUrl;
import com.robak.lasygps.domain.DivisionUrl;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private GoogleApiClient mGoogleApiClient;

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
    private DataLayer dataLayer;
    private LocationService location;

    //335845.0148, 705044.3988

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
        dataLayer = new DataLayer(getApplicationContext(), new DataLayer.Callback<String>() {
            @Override
            public void apply(String errorMessage) {
                progressDialog.dismiss();
                txtErrorText.setText(errorMessage);
                Log.d(TAG, errorMessage);
            }
        });

        if (checkPlayServices()) {
            mGoogleApiClient = buildGoogleApiClient();
            location = new LocationService(getApplicationContext(), mGoogleApiClient);
        }

        // Show location button click listener
        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearDisplayedForestData();
                progressDialog.setMessage("Szukam");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                displayLocation( location.getLastLocation() );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

        if (mGoogleApiClient.isConnected() && isConstantMode()) {
            location.startLocationUpdates(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        location.stopLocationUpdates(this);
    }


    private void startPeriodicLocationUpdates() {
        btnSearch.setText(getString(R.string.btn_start_location_updates));
        location.startLocationUpdates(this);
    }

    private void stopPeriodicLocationUpdates() {
        btnSearch.setText(getString(R.string.btn_stop_location_updates));
        location.stopLocationUpdates(this);
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation(Location currentLocation) {

        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();

            txtErrorText.setText("");
            DataLayer.Callback<ForestData> callback = new DataLayer.Callback<ForestData>() {
                @Override
                public void apply(ForestData forestData) {
                    updateDisplayedForestData(forestData);
                }
            };

            dataLayer.getForestDataAt(String.valueOf(latitude), String.valueOf(longitude), callback);
        } else {

            txtErrorText
                    .setText("UWAGA! \n(Nie można ustalić lokalizacji. Upewnij się, że usługi lokalizacji są uruchomione w urządzeniu)");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
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
                        "To urządzenie nie jest wspierane przez tę aplikację.", Toast.LENGTH_LONG)
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

    /**
     * Google api forestDataCallback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (isConstantMode()) {
            startPeriodicLocationUpdates();
        }
        else
        {
            stopPeriodicLocationUpdates();
        }
    }

    private boolean isConstantMode()
    {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(SettingsActivity.KEY_CONSTANT_MODE, false);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "Szukam", Toast.LENGTH_SHORT).show();
        displayLocation(location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void clearDisplayedForestData()
    {
        txtNadlesnictwo.setText("");
        txtLesnictwo.setText("");
        txtOddzial.setText("");
        txtPododzial.setText("");
        txtArea.setText("");
        txtTreeCode.setText("");
        txtTreeAge.setText("");
        txtDataAge.setText("");
    }

    private void updateDisplayedForestData(ForestData forestData) {
        if(forestData.getInspectorate() != null && forestData.getForestry() != null) {
            txtNadlesnictwo.setText(forestData.getInspectorate());
            txtLesnictwo.setText(forestData.getForestry());
        }
        txtOddzial.setText(forestData.getDivision());
        txtPododzial.setText(forestData.getSubdivision());
        txtArea.setText(forestData.getAreaSize());
        txtTreeCode.setText(forestData.getTreeCode());
        txtTreeAge.setText(forestData.getTreeAge());
        txtDataAge.setText(forestData.getDataAge());
    }

}