package com.example.a_currency;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GraphActivity extends ActionBarActivity {

    private static String TAG = "MainActivity";
    public static String LOG_TAG = "my_log";
    private LineChart lineChart;

    String[] quarters= new String[5];
    String resultJson = "";
    TextView tvText;

    /*
    int myYear = 2011;
    int myMonth = 02;
    int myDay = 03;
    */

    //DatePickerDialog.OnDateSetListener from_dateListener,to_dateListener;

    int DATE_PICKER_TO = 0;
    //int DATE_PICKER_FROM = 1;

    List<Entry> valsComp1 = new ArrayList<Entry>();
    LineDataSet setComp1;
    LineData data;

    //float miny=0;
    //float maxy=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Log.d(TAG, "onCreate: starting to create chart");

        tvText = (TextView) findViewById(R.id.tvText);
        //tvDateTo = (TextView) findViewById(R.id.tvDateTo);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        //tvDateFrom.setText(sdf.format(new Date()));
        //tvDateTo.setText(sdf.format(new Date()));

        lineChart = (LineChart)  findViewById(R.id.idPieChart);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);


        for (int i=0; i<5; i++) valsComp1.add(new Entry(i, 28.00f));
        //valsComp1.add(new Entry(1, 23.70f));
        //valsComp1.add(new Entry(2, 23.70f));
        //valsComp1.add(new Entry(3, 23.70f));
        //valsComp1.add(new Entry(4, 23.70f));
        //valsComp1.add(new Entry(5, 23.70f));
        //valsComp1.add(new Entry(6, 23.70f));



        setComp1 = new LineDataSet(valsComp1, "USD");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return quarters[(int) value];
            }
            // we don't draw numbers, so no decimal digits needed
            public int getDecimalDigits() {  return 0; }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);


        data = new LineData(dataSets);

        data.setValueFormatter(new MyValueFormatter());
        //lineChart.setData(data);
        //lineChart.setVisibleXRangeMaximum(10);
        //lineChart.invalidate();
        //updateline();

    }

    protected void onResume() {
        super.onResume();

        if (isNetworkAvailable()) {
            if (resultJson.equals("")) {
                ParseTask task = new ParseTask();
                task.execute();
            }
        }
        else {
            Toast.makeText(this, "No connection",Toast.LENGTH_LONG).show();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.00"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value); //+ " $"; // e.g. append a dollar-sign
        }
    }


    public void onclickfrom(View view) {
        showDialog(DATE_PICKER_TO);
    }

    private void updateline ()
    {

        /*lineChart.notifyDataSetChanged();
        //lineChart.refreshDrawableState();
        setComp1.notifyDataSetChanged();
        data.notifyDataChanged();
        lineChart.setData(data);
        */
        //lineChart.clearValues();
        data.notifyDataChanged();
        lineChart.setData(data);
        lineChart.fitScreen();

        lineChart.notifyDataSetChanged();
        lineChart.moveViewToX(data.getEntryCount());
        lineChart.fitScreen();
        lineChart.refreshDrawableState();
        lineChart.resetViewPortOffsets();
        lineChart.setVisibleXRange(4,4);
        lineChart.getAxisLeft().setAxisMaximum(30);
        lineChart.getAxisLeft().setAxisMinimum(27);
        //lineChart.invalidate();
        //lineChart.invalidate();
        //lineChart.getLineData();
        Log.d(LOG_TAG, "line updated");
    }


    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        float q;

        @Override
        protected String doInBackground(Void... params) {

            float[] tempsale = new float[5];
            String[] tempdate = new String[5];

            //float miny=200f;
            //float maxy=0f;
            q = 0f;
            int j=0;

            valsComp1.clear();

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat sdmf = new SimpleDateFormat("dd.MM");
            Date startDate = new Date();
            //Date endDate = new Date();

            /*
            try {
                startDate = sdf.parse("15.12.2016");
                endDate = sdf.parse("20.12.2016");
            }
            catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */

            Calendar c = Calendar.getInstance();
            c.setTime(startDate);

            while (j<5) {

                try {

                    URL url = new URL("https://api.privatbank.ua/p24api/exchange_rates?json&date=" + sdf.format(startDate));

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

                    resultJson = buffer.toString();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                JSONObject dataJsonObj = null;
                //Map<String, Object> m;

                String currency;
                String saleRate;

                try {
                    dataJsonObj = new JSONObject(resultJson);
                    JSONArray CurrencyExchange = dataJsonObj.getJSONArray("exchangeRate");

                    if (CurrencyExchange.length()>0) {

                        for (int i = 0; i < CurrencyExchange.length(); i++) {
                            JSONObject Currency = CurrencyExchange.getJSONObject(i);
                            currency = Currency.getString("currency");
                            if (currency.equals("USD")) {
                                saleRate = nf.format(Float.parseFloat(Currency.getString("saleRateNB")));
                                Log.d(LOG_TAG, "saleRate: " + saleRate);

                                tempsale[j]=Float.parseFloat(saleRate);
                                tempdate[j] = sdmf.format(startDate);
                                //valsComp1.add(new Entry(j, Float.parseFloat(saleRate)));
                                //quarters[j] = sdmf.format(startDate);//"j"+String.valueOf(j);
                                //q = q + 1f;
                                //if (miny>Float.parseFloat(saleRate)) miny=Float.parseFloat(saleRate);
                                //if (maxy<Float.parseFloat(saleRate)) maxy=Float.parseFloat(saleRate);
                                //Log.d(LOG_TAG, "miny:" + miny + "maxy:"+maxy );
                                j++;
                            }
                        }
                    } else
                    {
                        //valsComp1.add(new Entry(q, Float.parseFloat("0")));
                        //quarters[j] ="j"+String.valueOf(j);
                        q = q + 1f;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(LOG_TAG, sdf.format(c.getTime()));
                c.add(Calendar.DATE, -1);
                startDate=c.getTime();

            }

            for (int i=0; i<5; i++) {
                valsComp1.add(new Entry(i, tempsale[4-i]));
                quarters[i] = tempdate[4-i];//"j"+String.valueOf(j);
            }
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            updateline();

            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        getSupportActionBar().setTitle(R.string.graph);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Menu:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intent);
                break;

            case R.id.Orders:
                Intent intent2 = new Intent(this, OrdersActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                this.startActivity(intent2);
                break;

            case R.id.Settings:
                Intent intent3 = new Intent(this, SettingsActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intent3);
                break;

            case R.id.Graph:
                Intent intent4 = new Intent(this, GraphActivity.class);
                intent4.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intent4);
                break;
        }
        return true;
    }

}