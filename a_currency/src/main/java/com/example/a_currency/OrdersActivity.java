package com.example.a_currency;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.database.sqlite.SQLiteOpenHelper;


public class OrdersActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

    ListView lvOrders;
    //final String ATTRIBUTE_NAME_TEXT = "text";
    //final String ATTRIBUTE_NAME_IMAGE = "image";
    //final String ATTRIBUTE_NAME_SIZE = "size";
    //final String ATTRIBUTE_NAME_PRICE = "price";

    DB db;
    SimpleCursorAdapter scAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        //int eur_img = R.drawable.eur_img;
        //int rur_img = R.drawable.rur_img;
        //int usd_img = R.drawable.usd_img;

        db = new DB(this);
        db.open();

        String[] from = {DB.COLUMN_Img, DB.COLUMN_Type, DB.COLUMN_Course, DB.COLUMN_Amount, DB.COLUMN_Sum};
        int[] to = {R.id.ivImg, R.id.tvType, R.id.tvCourse, R.id.tvAmount, R.id.tvSum };

        scAdapter = new SimpleCursorAdapter(this, R.layout.item_orders, null, from, to, 0);
        lvOrders = (ListView) findViewById(R.id.lvOrders);
        lvOrders.setAdapter(scAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        getSupportActionBar().setTitle("Orders");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

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

    /*
    public void onButtonClick(View view) {
        getSupportLoaderManager().getLoader(0).forceLoad();
    }
    */

    /*
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
    */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    static class MyCursorLoader extends CursorLoader {

        DB db;

        public MyCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllDataHis();
            return cursor;
        }

    }
}
