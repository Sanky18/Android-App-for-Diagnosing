package com.example.collectdata;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener2 {

    final Context context =this;
    private Button buttonPrompt;
    private EditText size;


    double legsize = 90.0;
    int stepFlag = 0;
    double theta = 0.0f;
    double w_prev = 0.0f;
    double w_t_prev = 0.0f;
    long prevTimeStep = 0;
    long timeAnchor=0;
    ArrayList<Double> acc_t = new ArrayList<Double>();
    ArrayList<Double> w_t = new ArrayList<Double>();
    ArrayList<Double> acc_y = new ArrayList<Double>();
    ArrayList<Double> w_z = new ArrayList<Double>();

    ArrayList<Double> step_time = new ArrayList<Double>();
    ArrayList<Double> pos_angles = new ArrayList<Double>();
    ArrayList<Double> pos_time = new ArrayList<Double>();
    ArrayList<Double> neg_angles = new ArrayList<Double>();
    ArrayList<Double> neg_time = new ArrayList<Double>();

    SensorManager manager;
    Button buttonStart;
    Button buttonStop;
    boolean isRunning;
    final String TAG = "SensorLog";
    FileWriter writer;

    TextView countView;
    public void clearAll(){
        stepFlag = 0;
        theta = 0.0f;
        w_prev = 0.0f;
        w_t_prev = 0.0f;
        prevTimeStep = 0;
        timeAnchor=0;
        acc_t.clear();
        w_t.clear();
        acc_y.clear();
        w_z.clear();

        step_time.clear();

        pos_angles.clear();
        pos_time.clear();
        neg_angles.clear();
        neg_time.clear();
    }
    public static ArrayList<Double> findStep(ArrayList<Double> a_t, ArrayList<Double> aa, double acceleration_threshold, double time_gap) {
        ArrayList<Double> step_time = new ArrayList<Double>();
        ArrayList<Integer> step_index = new ArrayList<Integer>();
        double lastTime = 0;
        for (int i = 0; i < aa.size(); i++) {
            double currentTime = a_t.get(i);
            if (aa.get(i) > acceleration_threshold && currentTime - lastTime > time_gap) {
                step_index.add(i);
                lastTime = currentTime;
            }
        }

        for (int i = 0; i < step_index.size(); i++) {
            if (i < step_index.size() - 1) {
                ArrayList<Double> sublist = new ArrayList<Double>();
                for (int j = step_index.get(i); j< step_index.get(i+1); j++){
                    sublist.add(aa.get(j));
                }

                step_time.add(a_t.get(step_index.get(i) + maxIndex(sublist)));
            } else {
                ArrayList<Double> sublist = new ArrayList<Double>();
                for (int j = step_index.get(i); j< aa.size(); j++){
                    sublist.add(aa.get(j));
                }
                step_time.add(a_t.get(step_index.get(i) + maxIndex(sublist)));
            }
        }

        return step_time;
    }
    private static int maxIndex(ArrayList<Double> arr) {
        int maxIndex = 0;
        for (int i = 1; i < arr.size(); i++) {
            if (arr.get(i) > arr.get(maxIndex)) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    private ArrayList<Double> Integrate(ArrayList<Double> y, ArrayList<Double> x) {
        ArrayList<Double> Integral = new ArrayList<Double>();
        double Area = 0;
        for (int i = 0 ; i< y.size()-1; i++){
            if (x.get(i)>4.0){
                Area += (double)((y.get(i + 1) + y.get(i))/2)*(x.get(i + 1) - x.get(i));
            }
            Integral.add(Area);
        }
        Area+=(double)(y.get(y.size()-1))*(x.get(y.size()-1) - x.get(y.size()-2));
        Integral.add(Area);
        return Integral;
    }
    private ArrayList<Double> getAngles(ArrayList<Double> w_t, ArrayList<Double> theta_values, ArrayList<Double> step_time) {
        ArrayList<Double> ang = new ArrayList<Double>();
        int t = 0;
        if (step_time.size()>0){
            double time = step_time.get(t);
            for (int i=0; i<w_t.size(); i++) {
                if (w_t.get(i) <= time && time <= w_t.get(i + 1)) {
                    ang.add(theta_values.get(i));

                    if (t < step_time.size() - 1) {
                        t += 1;
                        time = step_time.get(t);
                    }
                }
            }
        }
        return ang;
    }








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPrompt = (Button) findViewById(R.id.buttonPrompt);
//        size = (EditText) findViewById(R.id.editTextDialogUserInput);
        buttonPrompt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_length, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @SuppressLint("ClickableViewAccessibility")
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        // edit text

                                        String input = userInput.getText().toString();
                                        legsize = (double) Double.parseDouble(input);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return false;
            }
        });
        isRunning = false;

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        countView = (TextView)findViewById(R.id.resultBox);
        countView.setTextColor(Color.GREEN);
        countView.setTextSize(32);

        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                countView.setText("CALCULATING ... ");


                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);

                Log.d(TAG, "Writing to " + getStorageDir());
                try {
                    writer = new FileWriter(new File(getStorageDir(), "sensors_" + System.currentTimeMillis() + ".csv"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 0);
                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 0);
                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), 0);
                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), 0);
                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), 0);

                isRunning = true;
                return true;
            }
        });
        buttonStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                isRunning = false;
                manager.flush(MainActivity.this);
                manager.unregisterListener(MainActivity.this);
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ArrayList<Double> step_time =  findStep(acc_t, acc_y, 12.0, 0.4);

                ArrayList<Double> theta_values = Integrate(w_z,w_t);

                ArrayList<Double> angles = getAngles(w_t, theta_values, step_time);



                double p =0;
                double n =0;
                double pos_sine =0;
                double neg_sine =0;
                double stance_time=0;
                double swing_time=0;
                double number_swing=0;
                double number_stance=0;
                for (int i=1; i<angles.size()-1;i++){
                    double thet = angles.get(i);
                    if (thet>=0){
                        swing_time += step_time.get(i)-step_time.get(i-1);
                        p+=1;
                        pos_sine+= (double) Math.sin(thet);
                    }else {
                        stance_time += step_time.get(i) - step_time.get(i-1);
                        n += 1;
                        neg_sine += (double) Math.sin(thet);
                    }

                }
                double av_step_size = (double) ((pos_sine/p)-(neg_sine/n));
                double av_swing_time = (double) (swing_time/p);
                double av_stance_time = (double) (stance_time/n);
                double av_stride_time = (double) av_stance_time+av_swing_time;
                double cadance = (double) 0;
                if (step_time.size()>=1){
                    cadance = (double)  ((step_time.size()-1)/(step_time.get(step_time.size()-1)-step_time.get(0)))*60;
                }


                String Info = "Step Size = "+String.format("%.2f", av_step_size*legsize) + "cm\n";
                Info += "Swing Time = "+ String.format("%.2f", av_swing_time) +" s."+"\n";
                Info += "Stance Time = "+ String.format("%.2f", av_stance_time)+" s."+"\n";
                Info += "Stride Time = "+ String.format("%.2f", av_stride_time)+" s."+"\n";
                Info += "Cadance = " + String.format("%.2f", cadance) + " steps/min."+"\n";
                Info += "leg size = "+ String.format("%.2f",legsize) + "cm.";

                countView.setText(Info);
                clearAll();
                return true;
            }
        });
    }
    private String getStorageDir() {
        return this.getExternalFilesDir(null).getAbsolutePath();
    }
    @Override
    public void onFlushCompleted(Sensor sensor) {
    }
    @Override
    public void onSensorChanged(SensorEvent evt) {

        if(isRunning) {
            try {
                switch(evt.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        if (timeAnchor==0){
                            timeAnchor = evt.timestamp;
                        }
                        double t = (double)(evt.timestamp-timeAnchor)/1000000000;
                        double x = evt.values[0];
                        double y = evt.values[1];
                        double z = evt.values[2];
                        writer.write(String.format("ACC; %d; %f; %f; %f; %f; %f; %f\n", System.currentTimeMillis(), x, y, z, 0.f, 0.f, 0.f));
                        acc_y.add(y);
                        acc_t.add(t);
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        if (timeAnchor==0){
                            timeAnchor = evt.timestamp;
                        }
                        double t_ = (double) (evt.timestamp-timeAnchor)/1000000000;

                        writer.write(String.format("GYRO; %d; %f; %f; %f; %f; %f; %f\n", System.currentTimeMillis(), evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                        double w_now = evt.values[2];
                        w_z.add(w_now);
                        w_t.add(t_);
                        break;

//                    case Sensor.TYPE_ROTATION_VECTOR:
//                        writer.write(String.format("ROT; %d; %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2], evt.values[3], 0.f, 0.f));
//                        break;
//
//                    case Sensor.TYPE_MAGNETIC_FIELD:
//                        writer.write(String.format("MAG; %d; %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
//                        break;
//
//                    case Sensor.TYPE_STEP_COUNTER:
//                        writer.write(String.format("STPC; %d; %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
//                        break;
//
//                    case Sensor.TYPE_STEP_DETECTOR:
//                        writer.write(String.format("STPD; %d; %f; %f; %f; %f; %f; %f\n", evt.timestamp, 1.0f, 0.f, 0.f, 0.f, 0.f, 0.f));
//                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}