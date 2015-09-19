package com.robak.lasygps;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private TextView lblLocation;
    private TextView lblNadlesnictwo;
    private TextView lblLesnictwo;
    private Button btnShowLocation, btnStartLocationUpdates;

    private LesnictwoUrl lesnictwoUrl;

    private void getOddzial(final String latitude, final String longitude) {

        final String latitudeMock = "16.48363497723";
        final String longitudeMock = "54.183357627217";

        String urlOddzial = "http://mapserver.bdl.lasy.gov.pl/arcgis/rest/services/BDL_2_0/MapServer/16/query?where=&text=&objectIds=&time=&geometry="+ latitudeMock + "%2C+" + longitudeMock +"&geometryType=esriGeometryPoint&inSR=4326&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=subarea_id%2C+arodes_int_num%2C+adress_forest%2C+area_type_cd%2C+site_type_cd%2C+silviculture_cd%2C+forest_func_cd%2C+stand_struct_cd%2C+rotation_age%2C+sub_area%2C+prot_category_cd%2C+species_cd_d%2C+part_cd%2C+species_age%2C+a_year&returnGeometry=false&maxAllowableOffset=&geometryPrecision=&outSR=4326&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";
        String urlNadlesnictwo = "http://mapserver.bdl.lasy.gov.pl/arcgis/rest/services/BDL_2_0/MapServer/12/query?where=&text=&objectIds=&time=&geometry=335845.0148%2C+705044.3988&geometryType=esriGeometryPoint&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=inspectorate_id%2C+inspectorate_name&returnGeometry=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";
        String urlLesnictwo = "http://www.bdl.lasy.gov.pl/portal/BULiGL.BDL.Reports/Report/StandDescriptionData?arodesIntNum=1123028174&aYear=2014&adress_forest=11-23-1-09-574%20%20%20-i%20%20%20-00&jointOwnership=false";

// Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        BasicNetwork network = new BasicNetwork(new HurlStack());
//335845.0148, 705044.3988
// Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

// Start the queue
        mRequestQueue.start();

        JsonObjectRequest oddzialJsonRequest = getOddzialJsonRequest(urlOddzial);
        JsonObjectRequest nadlesnictwoJsonRequest = getNadlesnictwolJsonRequest(urlNadlesnictwo);


        JsonObjectRequest lesnictwoJsonRequest = getLesnictwolJsonRequest(urlNadlesnictwo);

// Add the request to the RequestQueue.
        mRequestQueue.add(oddzialJsonRequest);
        //mRequestQueue.add(nadlesnictwoJsonRequest);

    }

    @NonNull
    private JsonObjectRequest getNadlesnictwolJsonRequest(String url) {
        JSONObject attrib = new JSONObject();

        return new JsonObjectRequest( url, attrib,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject attributes = response.getJSONArray("features").getJSONObject(0).getJSONObject("attributes");
                            lblNadlesnictwo.setText("Nadleśnictwo: " + attributes.getString("inspectorate_name") );
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
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                            System.out.println("\n\n"+response.toString());
                            lblLocation.setText(lblLocation.getText() +
                                    "\nNadleśnictwo: " + nadlesnictwo +
                                    "\nLeśnictwo: " + lesnictwo);
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
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    JSONObject attributes = response.getJSONArray("features").getJSONObject(0).getJSONObject("attributes");
                    String forestAddress = attributes.getString("adress_forest");
                    lblLocation.setText("odziale: " + forestAddress.split("-")[4] +
                            "\npododziale: " + forestAddress.split("-")[5] +
                            "\npowierzchnia (ha): " + attributes.getString("sub_area") +
                    "\nkod gatunku: " + attributes.getString("species_cd_d") +
                    "\nwiek drzew: " + attributes.getString("species_age") +
                    "\ndane zebrane w roku: " + attributes.getString("a_year"));

                    lesnictwoUrl = new LesnictwoUrl(attributes.getString("arodes_int_num"),
                            attributes.getString("a_year"),
                            forestAddress);
                    mRequestQueue.add(getLesnictwolJsonRequest(lesnictwoUrl.getUrl()));
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
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        lblNadlesnictwo = (TextView) findViewById(R.id.nadlesnictwo);
        lblLesnictwo = (TextView) findViewById(R.id.lesnictwo);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        //btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        // Show location button click listener
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

       // mLastLocation = LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();


            getOddzial(String.valueOf(latitude), String.valueOf(longitude));


        } else {

            lblLocation
                    .setText("(Nie można ustalić lokalizacji. Upewnij się, że lokalizacja jest odblokowana w urządzeniu)");
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
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
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
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    public class Attributes {

        private String adressForest;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The adressForest
         */
        public String getAdressForest() {
            return adressForest;
        }

        /**
         *
         * @param adressForest
         * The adress_forest
         */
        public void setAdressForest(String adressForest) {
            this.adressForest = adressForest;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Example {

        private String displayFieldName;
        private FieldAliases fieldAliases;
        private String geometryType;
        private SpatialReference spatialReference;
        private List<Field> fields = new ArrayList<Field>();
        private List<Feature> features = new ArrayList<Feature>();
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The displayFieldName
         */
        public String getDisplayFieldName() {
            return displayFieldName;
        }

        /**
         *
         * @param displayFieldName
         * The displayFieldName
         */
        public void setDisplayFieldName(String displayFieldName) {
            this.displayFieldName = displayFieldName;
        }

        /**
         *
         * @return
         * The fieldAliases
         */
        public FieldAliases getFieldAliases() {
            return fieldAliases;
        }

        /**
         *
         * @param fieldAliases
         * The fieldAliases
         */
        public void setFieldAliases(FieldAliases fieldAliases) {
            this.fieldAliases = fieldAliases;
        }

        /**
         *
         * @return
         * The geometryType
         */
        public String getGeometryType() {
            return geometryType;
        }

        /**
         *
         * @param geometryType
         * The geometryType
         */
        public void setGeometryType(String geometryType) {
            this.geometryType = geometryType;
        }

        /**
         *
         * @return
         * The spatialReference
         */
        public SpatialReference getSpatialReference() {
            return spatialReference;
        }

        /**
         *
         * @param spatialReference
         * The spatialReference
         */
        public void setSpatialReference(SpatialReference spatialReference) {
            this.spatialReference = spatialReference;
        }

        /**
         *
         * @return
         * The fields
         */
        public List<Field> getFields() {
            return fields;
        }

        /**
         *
         * @param fields
         * The fields
         */
        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        /**
         *
         * @return
         * The features
         */
        public List<Feature> getFeatures() {
            return features;
        }

        /**
         *
         * @param features
         * The features
         */
        public void setFeatures(List<Feature> features) {
            this.features = features;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Feature {

        private Attributes attributes;
        private Geometry geometry;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The attributes
         */
        public Attributes getAttributes() {
            return attributes;
        }

        /**
         *
         * @param attributes
         * The attributes
         */
        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        /**
         *
         * @return
         * The geometry
         */
        public Geometry getGeometry() {
            return geometry;
        }

        /**
         *
         * @param geometry
         * The geometry
         */
        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Field {

        private String name;
        private String type;
        private String alias;
        private Integer length;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param name
         * The name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         *
         * @return
         * The type
         */
        public String getType() {
            return type;
        }

        /**
         *
         * @param type
         * The type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         *
         * @return
         * The alias
         */
        public String getAlias() {
            return alias;
        }

        /**
         *
         * @param alias
         * The alias
         */
        public void setAlias(String alias) {
            this.alias = alias;
        }

        /**
         *
         * @return
         * The length
         */
        public Integer getLength() {
            return length;
        }

        /**
         *
         * @param length
         * The length
         */
        public void setLength(Integer length) {
            this.length = length;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class FieldAliases {

        private String adressForest;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The adressForest
         */
        public String getAdressForest() {
            return adressForest;
        }

        /**
         *
         * @param adressForest
         * The adress_forest
         */
        public void setAdressForest(String adressForest) {
            this.adressForest = adressForest;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class Geometry {

        private List<List<List<Double>>> rings = new ArrayList<List<List<Double>>>();
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The rings
         */
        public List<List<List<Double>>> getRings() {
            return rings;
        }

        /**
         *
         * @param rings
         * The rings
         */
        public void setRings(List<List<List<Double>>> rings) {
            this.rings = rings;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    public class SpatialReference {

        private Integer wkid;
        private Integer latestWkid;
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         *
         * @return
         * The wkid
         */
        public Integer getWkid() {
            return wkid;
        }

        /**
         *
         * @param wkid
         * The wkid
         */
        public void setWkid(Integer wkid) {
            this.wkid = wkid;
        }

        /**
         *
         * @return
         * The latestWkid
         */
        public Integer getLatestWkid() {
            return latestWkid;
        }

        /**
         *
         * @param latestWkid
         * The latestWkid
         */
        public void setLatestWkid(Integer latestWkid) {
            this.latestWkid = latestWkid;
        }

        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }
}