package com.example.a_currency;

import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    public static String LOG_TAG = "my_log";

    final String ATTRIBUTE_NAME_CCY         = "ccy";
    final String ATTRIBUTE_NAME_BUY         = "buy";
    final String ATTRIBUTE_NAME_SALE        = "sale";
    final String ATTRIBUTE_NAME_IMAGE       = "image";

    String cvv;

    ListView lvSimple;

    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
            3);

    DB db;

    SimpleAdapter sAdapter;
    DialogFragment dlg1;
    SharedPreferences sp;

    int eur_img = R.drawable.eur_img;
    int rur_img = R.drawable.rur_img;
    int usd_img = R.drawable.usd_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dlg1 = new ExchangeFragment();
        db = new DB(this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(LOG_TAG,"sp="+sp.getString("widget_pref", ""));

        String[] from = { ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_CCY,ATTRIBUTE_NAME_BUY,ATTRIBUTE_NAME_SALE};
        int[] to = { R.id.ivImg, R.id.tvCurrency, R.id.tvBuy, R.id.tvSell };
        sAdapter = new SimpleAdapter(this, data, R.layout.item_currencyexchange,
                from, to);

        lvSimple = (ListView) findViewById(R.id.lvSimple);
        lvSimple.setAdapter(sAdapter);
        lvSimple.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Bundle bundle = new Bundle();
                TextView text = (TextView) view.findViewById(R.id.tvCurrency);
                cvv = text.getText().toString();
                Log.d(LOG_TAG, cvv);
                bundle.putString("cvv",cvv);
                dlg1.setArguments(bundle);
                dlg1.show(getFragmentManager(), "dlg1");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "MainActivity: onResume()");
        data.clear();
        if (isNetworkAvailable()) {
            ParseTask task = new ParseTask();
            task.execute();
        }
        else {
            Toast.makeText(this, "No connection",Toast.LENGTH_LONG).show();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL url = new URL("https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5");

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

                resultJson = "{\"CurrencyExchange\":"+buffer.toString()+"}";

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
                JSONArray CurrencyExchange = dataJsonObj.getJSONArray("CurrencyExchange");

                db.open();
                db.delRec();

                for (int i = 0; i < CurrencyExchange.length(); i++) {
                    JSONObject Currency = CurrencyExchange.getJSONObject(i);

                    String ccy = Currency.getString("ccy");

                    if (ccy.equals("EUR") || ccy.equals("RUR") || ccy.equals("USD")) {
                        String base_ccy = Currency.getString("base_ccy");
                        String buy = nf.format(Float.parseFloat(Currency.getString("buy")));
                        String sale = nf.format(Float.parseFloat(Currency.getString("sale")));
                        int img = R.mipmap.ic_launcher;

                        m = new HashMap<String, Object>();
                        m.put(ATTRIBUTE_NAME_CCY, ccy);
                        m.put(ATTRIBUTE_NAME_BUY, buy);
                        m.put(ATTRIBUTE_NAME_SALE, sale);

                        switch (ccy) {
                            case "EUR":
                                m.put(ATTRIBUTE_NAME_IMAGE, eur_img);
                                //img=eur_img;
                                break;
                            case "RUR":
                                m.put(ATTRIBUTE_NAME_IMAGE, rur_img);
                                //img=rur_img;
                                break;
                            case "USD":
                                m.put(ATTRIBUTE_NAME_IMAGE, usd_img);
                                //img=usd_img;
                                break;
                        }

                        data.add(m);
                        db.addRec(ccy, img, buy, sale);

                        Log.d(LOG_TAG, "ccy: " + ccy);
                        Log.d(LOG_TAG, "base_ccy:" + base_ccy);
                        Log.d(LOG_TAG, "buy: " + buy);
                        Log.d(LOG_TAG, "sale: " + sale);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Cursor c=db.getAllData();
            if (c != null) {
                if (c.moveToFirst()) {
                    String str;
                    do {
                        str = "";
                        for (String cn : c.getColumnNames()) {
                            str = str.concat(cn + " = "
                                    + c.getString(c.getColumnIndex(cn)) + "; ");
                        }
                        Log.d(LOG_TAG, str);

                    } while (c.moveToNext());
                }
                c.close();
            } else
                Log.d(LOG_TAG, "Cursor is null");
            c.close();
            db.close();

            sAdapter.notifyDataSetChanged();

        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        getSupportActionBar().setTitle(R.string.currency);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        //Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        //return super.onOptionsItemSelected(item);

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
