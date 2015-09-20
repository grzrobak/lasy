package com.robak.lasygps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.robak.lasygps.domain.DivisionUrl;
import com.robak.lasygps.domain.ForestData;
import com.robak.lasygps.domain.InspectorateUrl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Grzesiek on 20-09-2015.
 */
public class DataLayer
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Response.ErrorListener errorVolleyListener;

    private RequestQueue mRequestQueue;
    private ForestData forestData;

    private final Context context;

    Callback<ForestData> forestDataCallback;

    Callback<String> postErrorAction;

    public DataLayer(final Context context, final Callback<String> postErrorAction) {
        this.context = context;
        this.postErrorAction = postErrorAction;
        mRequestQueue = Volley.newRequestQueue(context);
        mRequestQueue.start();

        errorVolleyListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                postErrorAction.apply("");
            }
        };

    }

    public void getForestDataAt(String latitude, String longitude, Callback<ForestData> callback) {
        this.forestDataCallback = callback;
//      used for testing purposes:
//      String latitudeMock = "16.48363497723";
//      String longitudeMock = "54.183357627217";
//      latitude = latitudeMock;
//      longitude = longitudeMock;

        mRequestQueue.cancelAll(TAG);

        DivisionUrl divisionUrl = new DivisionUrl(latitude, longitude);

        JsonObjectRequest divisionJsonRequest = getLpDivisionJsonRequest(divisionUrl);
        Log.d(TAG, divisionJsonRequest.getUrl());
        mRequestQueue.add(divisionJsonRequest);

    }

    @NonNull
    private JsonObjectRequest getLpDivisionJsonRequest(final DivisionUrl url) {
        JsonObjectRequest request = new JsonObjectRequest( url.getLpUrl(), new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            forestData = new ForestData(response);

                            InspectorateUrl inspectorateUrl = new InspectorateUrl(forestData);

                            JsonObjectRequest forestryJsonRequest = getLesnictwolJsonRequest(inspectorateUrl.getUrl());
                            forestryJsonRequest.setTag(TAG);
                            forestryJsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));
                            mRequestQueue.add(forestryJsonRequest);
                        } catch (JSONException e) {
                            JsonObjectRequest notLpDivisionJsonRequest = getNotLpDivisionJsonRequest(url);
                            Log.d(TAG, notLpDivisionJsonRequest.getUrl());
                            mRequestQueue.add(notLpDivisionJsonRequest);
                        }
                        //"adress_forest": "11-23-1-09-574   -b   -00"
                    }
                }, errorVolleyListener);

        request.setTag(TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));

        return request;
    }

    @NonNull
    private JsonObjectRequest getNotLpDivisionJsonRequest(DivisionUrl url) {
        JsonObjectRequest request = new JsonObjectRequest( url.getNotLpUrl(), new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            forestData = new ForestData(response);

                            InspectorateUrl inspectorateUrl = new InspectorateUrl(forestData);
                            JsonObjectRequest lesnictwoJsonRequest = getLesnictwolJsonRequest(inspectorateUrl.getUrl());
                            mRequestQueue.add(lesnictwoJsonRequest);

                        } catch (JSONException e) {
                            postErrorAction.apply("Aktualna pozycja nie znajduje się w rejestrze Banku Danych o Lasach." +
                                    "\n\nPonów wyszukiwanie w innym miejscu.");
                        }
                        //"adress_forest": "11-23-1-09-574   -b   -00"
                    }
                }, errorVolleyListener);

        request.setTag(TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));

        return request;
    }

    @NonNull
    private JsonObjectRequest getLesnictwolJsonRequest(String url) {
        JsonObjectRequest request =  new JsonObjectRequest( url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String lesnictwo = response.getJSONArray("headerLP").getJSONObject(0).getString("forest_range_name");
                            String nadlesnictwo = response.getJSONArray("headerLP").getJSONObject(0).getString("inspectorate_name");
                            forestData.setForestry(lesnictwo);
                            forestData.setInspectorate(nadlesnictwo);
                        } catch (JSONException e) {
                            Toast.makeText(context, "Nie można pobrać niektórych danych", Toast.LENGTH_SHORT).show();
                        }
                        finally {
                            postErrorAction.apply("");
                            forestDataCallback.apply(forestData);
                        }
                    }
                }, errorVolleyListener);

        request.setTag(TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.0F));

        return request;
    }

    public static interface Callback<T>
    {
        void apply(T cos);
    }
}
