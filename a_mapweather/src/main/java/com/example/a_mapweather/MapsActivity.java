package com.example.a_mapweather;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    EditText etCity;
    Button btnSearch;
    TextView tvWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        etCity = (EditText) findViewById(R.id.etCity);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        tvWeather = (TextView) findViewById(R.id.tvWeather);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        init();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    public void onClickBtn(View v) {

        switch (v.getId()) {
            case R.id.btnSearch:
                mMap.clear();
                Log.d(LOG_TAG, etCity.getText().toString());
                CoordByName task = new CoordByName();
                task.execute(etCity.getText().toString());
                break;
        }
    }


    private void init() {

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(LOG_TAG, "onMapLongClick: " + latLng.latitude + "," + latLng.longitude);
                mMap.clear();
                //mMap.addMarker(new MarkerOptions().position(latLng));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                CoordByLatLng task = new CoordByLatLng();
                task.execute(latLng);
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = mMap.getMyLocation();
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                //mMap.addMarker(new MarkerOptions().position(myLocation));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

                CoordByLatLng task = new CoordByLatLng();
                task.execute(myLocation);

                return true;
            }
        });

    }


    private class CoordByLatLng extends AsyncTask<LatLng, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        LatLng coordinates;
        String weather_api_key = getResources().getString(R.string.weather_api_key);

        @Override
        protected String doInBackground(LatLng... coord) {

            coordinates=coord[0];

            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+coord[0].latitude+"&lon="+coord[0].longitude+"&units=metric&appid="+weather_api_key);
                Log.d(LOG_TAG, "http://api.openweathermap.org/data/2.5/weather?lat="+coord[0].latitude+"&lon="+coord[0].longitude+"&units=metric&appid="+weather_api_key);
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

            try {
                dataJsonObj = new JSONObject(strJson);
                JSONObject main = dataJsonObj.getJSONObject("main");
                Log.d(LOG_TAG, "temp "+main.getString("temp"));
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                String addressText = "";
                try {
                    while (addresses==null){
                        addresses = geocoder.getFromLocation(coordinates.latitude,coordinates.longitude,1);
                    }

                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        addressText = String.format(
                                "%s, %s, %s",
                                address.getMaxAddressLineIndex() > 0 ? address
                                        .getAddressLine(0) : "", address
                                        .getLocality(), address.getCountryName());
                        Log.d(LOG_TAG, "addresses: " + addressText);
                        LatLng customCity = new LatLng(coordinates.latitude,coordinates.longitude);
                        mMap.addMarker(new MarkerOptions().position(customCity).title(addressText));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(customCity));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tvWeather.setText(main.getString("temp")+"°C, "+main.getString("pressure")+"mm Hg, "+main.getString("humidity")+"%");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class CoordByName extends AsyncTask<String, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String name;
        String weather_api_key = getResources().getString(R.string.weather_api_key);
        @Override
        protected String doInBackground(String... names) {
            Log.d(LOG_TAG, "api.openweathermap.org/data/2.5/weather?q="+names[0]+"&units=metric&appid="+weather_api_key);
            name=names[0];
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+names[0]+"&units=metric&appid="+weather_api_key);
                Log.d(LOG_TAG, "api.openweathermap.org/data/2.5/weather?q="+names[0]+"&units=metric&appid="+weather_api_key);
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

            try {
                dataJsonObj = new JSONObject(strJson);
                JSONObject main = dataJsonObj.getJSONObject("main");
                JSONObject coord = dataJsonObj.getJSONObject("coord");
                Log.d(LOG_TAG, "temp "+main.getString("temp"));

                LatLng customCity = new LatLng(Double.parseDouble(coord.getString("lat")),(Double.parseDouble(coord.getString("lon"))));
                mMap.addMarker(new MarkerOptions().position(customCity).title(name));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(customCity));

                tvWeather.setText(main.getString("temp")+"°C, "+main.getString("pressure")+"mm Hg, "+main.getString("humidity")+"%");

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

}
