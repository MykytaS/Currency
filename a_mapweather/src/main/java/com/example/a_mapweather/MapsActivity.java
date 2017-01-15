package com.example.a_mapweather;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        init();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName("1600 Amphitheatre Pkwy, Mountain View, CA 94043", 5);
            Log.d(LOG_TAG, "Зашли");
            if(addresses.size() > 0) {
                double latitude= addresses.get(0).getLatitude();
                double longitude= addresses.get(0).getLongitude();
                Log.d(LOG_TAG, "onMapLongClick: " + latitude + "," + longitude);
                LatLng customCity = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(customCity).title("1600 Amphitheatre Pkwy, Mountain View, CA 94043"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(customCity));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        ParseTask task = new ParseTask();
        task.execute();

        //mMap.clear();

    }

    private void init() {

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(LOG_TAG, "onMapLongClick: " + latLng.latitude + "," + latLng.longitude);
                mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = mMap.getMyLocation();
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(myLocation));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                return true;
            }
        });

    }


    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=London,uk&units=metric&appid=cd2162a7f9fe1d7abfb2f437f338f7b8");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                Log.d(LOG_TAG, buffer.toString());
                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            Log.d(LOG_TAG, strJson);
            JSONObject dataJsonObj = null;

            Map<String, Object> m;

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            try {
                dataJsonObj = new JSONObject(strJson);
                //JSONArray weather = dataJsonObj.getJSONArray("weather");
                JSONObject main = dataJsonObj.getJSONObject("main");
                Log.d(LOG_TAG, "temp "+main.getString("temp"));



            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

}
