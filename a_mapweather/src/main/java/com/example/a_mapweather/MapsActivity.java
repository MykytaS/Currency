package com.example.a_mapweather;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;




import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    final String LOG_TAG = "myLogs";
    EditText etCity;
    Button btnSearch;
    TextView tvWeather;

    //
    private GetWeather getWeather;
    public static final String API_KEY = "cd2162a7f9fe1d7abfb2f437f338f7b8";
    public static final String ENDPOINT_URL="http://api.openweathermap.org";
    //

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

        Retrofit retrofit = new Retrofit.Builder().baseUrl(ENDPOINT_URL).
                addConverterFactory(GsonConverterFactory.create()).
                build();

        getWeather = retrofit.create(GetWeather.class);

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
                weatherByName(etCity.getText().toString());

                break;
        }
    }

    private void weatherByName (String cityName)
    {
        final String name = cityName;
        Call<WeatherCoord> call = getWeather.selectByName(name, API_KEY);
        Log.d(LOG_TAG, "onClickBtn ");
        call.enqueue(new Callback<WeatherCoord>() {
            @Override
            public void onResponse(Call<WeatherCoord> call, Response<WeatherCoord> response) {
                WeatherCoord main = response.body();
                if (main != null) {
                    LatLng customCity = new LatLng(main.getCoord().getLat(),main.getCoord().getLon());
                    mMap.addMarker(new MarkerOptions().position(customCity).title(name));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(customCity));
                    tvWeather.setText(main.getMain().getTemp()+"°C, "+main.getMain().getPressure()+"hPa, "+main.getMain().getHumidity()+"%");
                }
            }

            @Override
            public void onFailure(Call<WeatherCoord> call, Throwable t) {
                Log.d(LOG_TAG, "onFailure " + t);
            }
        });

    }

    private void weatherByLatLng (LatLng ll)
    {
        final LatLng latLng = ll;
        Call<WeatherCoord> call = getWeather.selectByLatLon(ll.latitude,ll.longitude, API_KEY);
        Log.d(LOG_TAG, "onClickBtn ");
        call.enqueue(new Callback<WeatherCoord>() {
            @Override
            public void onResponse(Call<WeatherCoord> call, Response<WeatherCoord> response) {
                WeatherCoord main = response.body();
                if (main != null) {

                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    String addressText = "";
                    try {
                        while (addresses==null){
                            addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                        }

                        if (addresses != null && addresses.size() > 0) {
                            Address address = addresses.get(0);
                            addressText = String.format(
                                    "%s, %s, %s",
                                    address.getMaxAddressLineIndex() > 0 ? address
                                            .getAddressLine(0) : "", address
                                            .getLocality(), address.getCountryName());
                            Log.d(LOG_TAG, "addresses: " + addressText);
                            LatLng customCity = new LatLng(latLng.latitude,latLng.longitude);
                            mMap.addMarker(new MarkerOptions().position(customCity).title(addressText));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(customCity));
                            tvWeather.setText(main.getMain().getTemp()+"°C, "+main.getMain().getPressure()+"mm Hg, "+main.getMain().getHumidity()+"%");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<WeatherCoord> call, Throwable t) {
                Log.d(LOG_TAG, "onFailure " + t);
            }
        });

    }

    private void init() {

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(LOG_TAG, "onMapLongClick: " + latLng.latitude + "," + latLng.longitude);
                mMap.clear();
                etCity.setText("");

                weatherByLatLng(latLng);

            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = mMap.getMyLocation();
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                etCity.setText("");
                weatherByLatLng(myLocation);

                return true;
            }
        });

    }

}
