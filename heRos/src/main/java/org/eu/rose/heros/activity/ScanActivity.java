package org.eu.rose.heros.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ConfigCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseConfig;
import com.parse.ParseException;

import org.eu.rose.heros.R;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.heRos.ScanThread;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by eric on 17/06/15.
 */
public class ScanActivity extends StandingActivity implements View.OnClickListener{

    private ImageButton scanButton= null;
    private Button connectButton= null;
    private Spinner herosSpinner = null;

    private MediaPlayer soundScanning;

    public static AtomicBoolean scanningDone = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.scan_view);
        scanButton = (ImageButton) findViewById(R.id.scan_button);

        connectButton = (Button) findViewById(R.id.connect_button);
        herosSpinner = (Spinner) findViewById(R.id.heros_spinner);

        // Load sound
        soundScanning = MediaPlayer.create(this, R.raw.scanning);

        // Add event on button click
        scanButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);

    }

    public void onStart(){
        super.onStart();
        connectButton.setVisibility(View.INVISIBLE);
        herosSpinner.setVisibility(View.INVISIBLE);
    }

    private void connectHeRos() {
        if (herosSpinner.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "You need to select your HeRos first!", Toast.LENGTH_SHORT)
                    .show();
        } else {
            final ProgressDialog dlg = new ProgressDialog(this);
            dlg.setTitle("Connecting to HeRos");
            dlg.setMessage("Connecting to your HeRos, please wait");
            dlg.show();

            HeRosApplication.getHeRosConnection().setHeRosMacAddress(herosSpinner.getSelectedItemPosition());
            HeRosApplication.getHeRosConnection().setHeRosName(herosSpinner.getSelectedItemPosition());

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("macAddress", HeRosApplication.getHeRosConnection().getHeRosMacAddress());

            // Ask the server if the HeRos is not already piloted by someone else
            ParseCloud.callFunctionInBackground("checkIfAvailable", params, new FunctionCallback<Boolean>() {
                String name = HeRosApplication.getHeRosConnection().getHeRosName();
                public void done(Boolean available, ParseException e) {
                    if (e == null) {

                        if (available) {

                            if(HeRosApplication.getHeRosConnection().connecToHeRos()){
                                ((HeRosApplication)getApplicationContext()).getCurrentActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dlg.dismiss();
                                        Intent menuIntent = new Intent(returnMe(), MainMenuActivity.class);
                                        menuIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(menuIntent);
                                    }
                                });
                            }else{
                                Toast.makeText(((HeRosApplication)getApplicationContext()).getCurrentActivity(), "Failed to connect with HeRos " + name, Toast.LENGTH_SHORT)
                                        .show();
                            }

                        } else {
                            Toast.makeText(((HeRosApplication)getApplicationContext()).getCurrentActivity(), "HeRos " + name + " is unavailable!", Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } else {
                        Toast.makeText(((HeRosApplication)getApplicationContext()).getCurrentActivity(), "Unable to check " + name + "'s status", Toast.LENGTH_SHORT)
                                .show();
                    }
                    dlg.dismiss();
                }
            });
        }
    }

    private void startScanning(){
        this.startStandingState("Scanning for HeRos", "Scanning your network, please wait");
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException ex) {
                if (ex != null)
                    config = ParseConfig.getCurrentConfig();

                Thread scanThread = new ScanThread(ScanActivity.this, config);
                scanThread.start();
                if(HeRosApplication.soundOn)
                    soundScanning.start();

            }
        });
    }

    // Show connect ui
    public void connectAvailable(ArrayAdapter adapterHeros, int size){
        Toast.makeText(this, size + " HeRos detected on your network", Toast.LENGTH_SHORT)
                .show();
        herosSpinner.setAdapter(adapterHeros);
        connectButton.setVisibility(View.VISIBLE);
        herosSpinner.setVisibility(View.VISIBLE);
    }

    // Hide connect ui
    public void connectUnavailable(){
        connectButton.setVisibility(View.INVISIBLE);
        herosSpinner.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scan_button:
                startScanning();
                break;
            case R.id.connect_button:
                connectHeRos();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return false;
    }

    public Activity returnMe(){
        return this;
    }
}
