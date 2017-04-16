package com.codingblocks.permissionmanager;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;

import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textView;

    public static float SHAKE_THRESHOLD_GRAVITY = 1.7f;

    Button start, pause, reset, lap;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;

    Handler handler;

    int Seconds, Minutes, MilliSeconds;

    ListView listView;

    SensorEventListener sel, sel1;

    SensorManager sensMan, sensMan1;

    Sensor accl, accl1;

    List<String> ListElementsArrayList;

    ArrayAdapter<String> adapter;

    int i = 0, count = 0, index = 0;

    long now;

    public static final String TAG = "Looper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        start = (Button) findViewById(R.id.button);
        pause = (Button) findViewById(R.id.button2);
        reset = (Button) findViewById(R.id.button3);
        lap = (Button) findViewById(R.id.button4);
        listView = (ListView) findViewById(R.id.listview1);


        sensMan = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        accl = sensMan.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sensMan1 = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        accl1 = sensMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        handler = new Handler();

        ListElementsArrayList = new ArrayList<>();

        adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                ListElementsArrayList
        );

        listView.setAdapter(adapter);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sensMan1.registerListener(sel1, accl1, SensorManager.SENSOR_DELAY_UI);
                pause.setEnabled(false);
            }
        });


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensMan.unregisterListener(sel);
                sensMan1.registerListener(sel1, accl1, SensorManager.SENSOR_DELAY_GAME);

                TimeBuff += MillisecondTime;

                handler.removeCallbacks(runnable);

                reset.setEnabled(true);
                start.setEnabled(true);

                i = 0;

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                textView.setText("00:00:00");

                ListElementsArrayList.clear();

                adapter.notifyDataSetChanged();

                count = 0;

                i = 0;
            }
        });

        lap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;

                ListElementsArrayList.add(index, count + ".  " + textView.getText().toString());

                adapter.notifyDataSetChanged();

            }
        });

        sel1 = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float gx = x / sensMan.GRAVITY_EARTH;
                float gy = y / sensMan.GRAVITY_EARTH;
                float gz = z / sensMan.GRAVITY_EARTH;

                float gra = (float) Math.sqrt(gx * gx + gy * gy + gz * gz);//close to one when no movement

                if (gra > SHAKE_THRESHOLD_GRAVITY && gra < 2.5 && System.currentTimeMillis() - now > 1000) {
                    i++;
                    if (i == 1) {
                        afterDrop();
                        now = System.currentTimeMillis();
                    } else {
                        now = System.currentTimeMillis();
                        count++;
                        ListElementsArrayList.add(index, count + ".  " + textView.getText().toString());
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sel = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float gra = event.values[0];
                if (gra < 5.0) {
                    TimeBuff += MillisecondTime;

                    handler.removeCallbacks(runnable);//removes any pending post in the message queue

                    reset.setEnabled(true);
                    sensMan.unregisterListener(sel);

                    sensMan1.registerListener(sel1, accl1, SensorManager.SENSOR_DELAY_UI);

                    i = 0;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    public void afterDrop() {

        StartTime = SystemClock.uptimeMillis();

        handler.postDelayed(runnable, 0);

        reset.setEnabled(false);
        start.setEnabled(false);

        sensMan.registerListener(sel, accl, SensorManager.SENSOR_DELAY_FASTEST);

    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);//no of seconds excluding the limit 60 to cal minute

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;//no of sec within 60

            MilliSeconds = (int) (UpdateTime % 1000);

            textView.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);//adds runnable to msg queue .runnable will  runn on the thread attached to handler
        }

    };
}