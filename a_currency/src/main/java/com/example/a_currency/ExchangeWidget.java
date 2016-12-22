package com.example.a_currency;

/**
 * Created by nikita on 04.12.2016.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExchangeWidget extends AppWidgetProvider {

    public static String LOG_TAG = "my_log";
    final String UPDATE_ALL_WIDGETS = "update_all_widgets";

    String widget_pref;
    SharedPreferences sp;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");

        Intent intent = new Intent(context, ExchangeWidget.class);
        intent.setAction(UPDATE_ALL_WIDGETS);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                60000, pIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.tvTime, configPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.tvCurrency, configPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.tvBuy, configPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.tvSale, configPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
        Intent intent = new Intent(context, ExchangeWidget.class);
        intent.setAction(UPDATE_ALL_WIDGETS);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase(UPDATE_ALL_WIDGETS)) {
            ComponentName thisAppWidget = new ComponentName(
                    context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);
            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID : ids) {
                updateWidget(context, appWidgetManager, appWidgetID);
            }
        }
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             int widgetID) {
        Log.d(LOG_TAG, "updateWidget " + widgetID);

        if (isNetworkAvailable(context)) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            widget_pref=sp.getString("widget_pref", "");
            Log.d(LOG_TAG, "widget_pref="+widget_pref);
            new ParseTask().execute(context);
        } else {
            RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                    R.layout.widget);
            widgetView.setTextViewText(R.id.tvTime, "No connection");
            appWidgetManager.updateAppWidget(widgetID, widgetView);
        }
    }

    public boolean isNetworkAvailable(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    class ParseTask extends AsyncTask<Context, Void, String> {

        private Context context;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        protected String doInBackground(Context... params) {

            try {

                context = params[0];

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
            RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                    R.layout.widget);
            JSONObject dataJsonObj = null;

            AppWidgetManager appWidgetManager  = AppWidgetManager.getInstance(context);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            try {
                dataJsonObj = new JSONObject(strJson);
                JSONArray CurrencyExchange = dataJsonObj.getJSONArray("CurrencyExchange");

                for (int i = 0; i < CurrencyExchange.length(); i++) {
                    JSONObject Currency = CurrencyExchange.getJSONObject(i);

                    String ccy = Currency.getString("ccy");
                    String base_ccy = Currency.getString("base_ccy");
                    String buy = nf.format(Float.parseFloat(Currency.getString("buy")));
                    String sale = nf.format(Float.parseFloat(Currency.getString("sale")));

                    Log.d(LOG_TAG, "ccy: " + ccy);
                    Log.d(LOG_TAG, "base_ccy:" + base_ccy);
                    Log.d(LOG_TAG, "buy: " + buy);
                    Log.d(LOG_TAG, "sale: " + sale);

                    if (ccy.equals(widget_pref))
                    {
                        Log.d(LOG_TAG, "Зашли в ccy="+widget_pref);
                        widgetView.setTextViewText(R.id.tvCurrency, ccy);
                        widgetView.setTextViewText(R.id.tvBuy, buy);
                        widgetView.setTextViewText(R.id.tvSale, sale);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateTimeString = sdf.format(new Date());
                        widgetView.setTextViewText(R.id.tvTime, currentDateTimeString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            appWidgetManager.updateAppWidget(new ComponentName(context, ExchangeWidget.class), widgetView);
            }
    }
}