package com.gemsense.basicdemo;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gemsense.basicdemo.render.AccelView;
import com.gemsense.basicdemo.render.CubeView;
import com.gemsense.common.GemSensorsData;
import com.gemsense.common.GemSystemInfo;
import com.gemsense.common.PedometerData;
import com.gemsense.common.TapData;
import com.gemsense.gemsdk.Gem;
import com.gemsense.gemsdk.GemAbstractListener;
import com.gemsense.gemsdk.GemListener;
import com.gemsense.gemsdk.GemManager;
import com.gemsense.gemsdk.GemSDKUtilityApp;
import com.gemsense.gemsdk.OnPedometerListener;
import com.gemsense.gemsdk.OnSensorsAbstractListener;
import com.gemsense.gemsdk.OnTapListener;


public class MainActivity extends AppCompatActivity {
    //Gems instance
    private Gem gem;

    //Renderers
    private CubeView cubeView;
    private AccelView accelView;

    //Pedometer view
    private PedometerDataView pedometerView;

    private MediaPlayer tapSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        pedometerView = (PedometerDataView)findViewById(R.id.pedometer_view_left);

        //Setup visualization render surfaces
        cubeView = (CubeView) findViewById(R.id.cube_view);
        cubeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use current azimuth(yaw) angles as an origin
                gem.calibrateAzimuth();
            }
        });

        accelView = (AccelView) findViewById(R.id.accel_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if(item.getItemId() == R.id.btn_reconnect) {
            //Try to connect again if gems wasn't found or connection lost
            gem.reconnect();
        }
        else if(itemId == R.id.btn_info) {
            showInfoDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Bind gem service to the application (GemSense software must be installed)
        GemManager.getDefault().bindService(this);

        //Get list of addresses from the utility app
        String[] whitelist = GemSDKUtilityApp.getWhiteList(this);

        if(whitelist.length > 0) {
            //If white list has been changed disconnect old one
            if(gem != null && !whitelist[0].equals(gem.getAddress())) {
                GemManager.getDefault().releaseGem(gem);
            }

            //It's possible to call it in OnCreate() if whitelist is not supposed to be changed
            initGem(whitelist[0]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unbind gem service from the application
        GemManager.getDefault().unbindService(this);
    }


    private void initGem(String address) {
        //Get gem instance
        gem = GemManager.getDefault().getGem(address, new GemAbstractListener() {
            @Override
            public void onStateChanged(int state) {
                if(state == Gem.STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                }
                else if(state == Gem.STATE_DISCONNECTED) {
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorOccurred(int errCode) {
                if(errCode == Gem.ERR_CONNECTING_TIMEOUT) {
                    Toast.makeText(MainActivity.this, "Can't find a gem", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Configure motion sensors callback
        gem.setSensorsListener(new OnSensorsAbstractListener() {
            @Override
            public void onSensorsChanged(GemSensorsData data) {
                //data.quaternion: float[4] {w, x, y, z}
                cubeView.setOrientation(data.quaternion);
                //data.acceleration: float[3] {x, y, z}
                accelView.setAcceleration(data.acceleration);
            }
        });

        //Configure pedometer callback
        gem.setPedometerListener(new OnPedometerListener() {
            @Override
            public void OnPedometerUpdate(PedometerData pedometerData) {
                //pedometerData.walkTime: float
                pedometerView.setWalkTime(pedometerData.walkTime);
                //pedometerData.steps: int
                pedometerView.setStepsCount(pedometerData.steps);
            }
        });

        //Setup sound for tap event
        tapSound = MediaPlayer.create(this, R.raw.success_sound);

        //Configure tap callback
        gem.setTapListener(new OnTapListener() {
            @Override
            public void onTap(TapData tapData) {
                if (tapSound.isPlaying()) {
                    tapSound.seekTo(0);
                } else {
                    tapSound.start();
                }
            }
        });
    }

    private void showInfoDialog() {
        View aboutView = LayoutInflater.from(this).inflate(R.layout.system_info, null, false);

        ((TextView)aboutView.findViewById(R.id.text_sdk_version))
                .setText(GemManager.getSDKVersion());

        ((TextView)aboutView.findViewById(R.id.text_gem_address)).setText(gem.getAddress());
        ((TextView)aboutView.findViewById(R.id.text_fw_revision)).setText(gem.getFirmwareVersion());

        (new AlertDialog.Builder(this))
                .setTitle(R.string.title_info)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(aboutView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}