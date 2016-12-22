package com.example.a_currency;


/**
 * Created by nikita on 26.11.2016.
 */
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.database.Cursor;

import java.text.NumberFormat;

public class ExchangeFragment extends DialogFragment implements OnClickListener {

    final String LOG_TAG = "myLogs";
    private static final int NOTIFY_ID = 101;

    DB db;
    String cvv;

    SharedPreferences sp;

    int img;

    TextView tvSum;
    TextView tvCourse;
    EditText etAmount;
    ImageView imgImage;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Bundle bundle = new Bundle();
        cvv =  getArguments().getString("cvv");

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        View v = inflater.inflate(R.layout.fragment_exchange, null);
        v.findViewById(R.id.btnBuy).setOnClickListener(this);
        v.findViewById(R.id.btnCancel).setOnClickListener(this);

        tvCourse = (TextView) v.findViewById(R.id.tvCourse);
        etAmount = (EditText) v.findViewById(R.id.etAmount);
        tvSum = (TextView) v.findViewById(R.id.tvSum);
        imgImage = (ImageView) v.findViewById(R.id.frImg);

        switch (cvv) {
            case "EUR":
                imgImage.setImageResource(R.drawable.eur_img);
                img=R.drawable.eur_img;
                break;
            case "RUR":
                imgImage.setImageResource(R.drawable.rur_img);
                img=R.drawable.rur_img;
                break;
            case "USD":
                imgImage.setImageResource(R.drawable.usd_img);
                img=R.drawable.usd_img;
                break;
        }

        db = new DB(getActivity());
        db.open();
        Cursor cursor = db.getCourse(cvv);
        if (cursor.moveToFirst()) {
            tvCourse.setText(cursor.getString(cursor.getColumnIndex("buy")));
        }
        db.close();


        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                if (etAmount.length()!=0) {
                    float amountFormat;
                    amountFormat = Float.parseFloat(tvCourse.getText().toString()) * Float.parseFloat(etAmount.getText().toString());
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(2);
                    nf.setMinimumFractionDigits(2);
                    tvSum.setText(nf.format(amountFormat));
                } else tvSum.setText("0.00");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }
        });


        return v;
    }

    public void onResume() {
        super.onResume();
        etAmount.setText("");
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnBuy:
                db.open();
                db.addRecHis(cvv,img,"Покупка",tvCourse.getText().toString(),etAmount.getText().toString(),tvSum.getText().toString());
                Cursor c=db.getAllDataHis();

                if (sp.getBoolean("notif", true)) {
                    sendNotif(cvv, tvCourse.getText().toString(), etAmount.getText().toString(), tvSum.getText().toString());
                }

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

            case R.id.btnCancel:
                dismiss();
                break;
        }

        dismiss();

    }

    private void sendNotif(String cvv, String cource, String amount, String sum) {

        Intent intent = new Intent(getActivity(), OrdersActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(getActivity());
        Resources res = this.getResources();
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setTicker("Покупка")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Покупка")
                .setContentText("Вы купили "+amount+" "+cvv+" по курсу " + cource + " на сумму "+sum+ " UAH"); // Текст уведомления

        Notification notification = builder.getNotification();
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(getActivity().NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
    }

}
