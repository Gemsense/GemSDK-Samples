package com.gemsense.twogemsdemo;

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

import com.gemsense.gemsdk.GemAbstractListener;
import com.gemsense.gemsdk.OnSensorsAbstractListener;
import com.gemsense.twogemsdemo.render.AccelView;
import com.gemsense.twogemsdemo.render.CubeView;
import com.gemsense.common.GemSensorsData;
import com.gemsense.common.GemSystemInfo;
import com.gemsense.common.PedometerData;
import com.gemsense.common.TapData;
import com.gemsense.gemsdk.Gem;
import com.gemsense.gemsdk.GemListener;
import com.gemsense.gemsdk.GemManager;
import com.gemsense.gemsdk.GemSDKUtilityApp;
import com.gemsense.gemsdk.OnPedometerListener;
import com.gemsense.gemsdk.OnTapListener;


public class MainActivity extends AppCompatActivity {
    //Gems instance
    private Gem gem1;
    private Gem gem2;


    //Renderers
    private CubeView cubeView1;
    private CubeView cubeView2;

    //Pedometer view

    private MediaPlayer tapSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //Setup visualization render surfaces
        cubeView1 = (CubeView) findViewById(R.id.cube_view1);
        cubeView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use current azimuth(yaw) angles as an origin
                if(gem1 != null) gem1.calibrateAzimuth();
            }
        });

        //Setup visualization render surfaces
        cubeView2 = (CubeView) findViewById(R.id.cube_view2);
        cubeView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use current azimuth(yaw) angles as an origin
                if(gem2 != null) gem2.calibrateAzimuth();
            }
        });
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
        if(item.getItemId() == R.id.btn_reconnect) {
            //Try to connect again if gems wasn't found or connection lost
            if(gem1 != null) gem1.reconnect();
            if(gem2 != null) gem2.reconnect();
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

        //Init gem1 if need
        if(whitelist.length > 0) {
            //If white list has been changed disconnect old one
            if(gem1 != null && !whitelist[0].equals(gem1.getAddress())) {
                GemManager.getDefault().releaseGem(gem1);
            }

            //It's possible to call it in OnCreate() if whitelist is not supposed to be changed
            gem1 = initGem(whitelist[0], cubeView1);
        }

        //Init gem2 if need
        if(whitelist.length > 1) {
            //If white list has been changed disconnect old one
            if(gem2 != null && !whitelist[1].equals(gem2.getAddress())) {
                GemManager.getDefault().releaseGem(gem2);
            }

            //It's possible to call it in OnCreate() if whitelist is not supposed to be changed
            gem2 = initGem(whitelist[1], cubeView2);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unbind gem service from the application
        GemManager.getDefault().unbindService(this);
    }


    private Gem initGem(final String address, final CubeView cubeTarget) {
        //Get gem instance
        Gem gem = GemManager.getDefault().getGem(address, new GemAbstractListener() {
            @Override
            public void onStateChanged(int state) {
                if(state == Gem.STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, "Connected: " + address, Toast.LENGTH_SHORT).show();
                }
                else if(state == Gem.STATE_DISCONNECTED) {
                    Toast.makeText(MainActivity.this, "Disconnected: " + address, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorOccurred(int errCode) {
                if(errCode == Gem.ERR_CONNECTING_TIMEOUT) {
                    Toast.makeText(MainActivity.this, "Can't find: " + address, Toast.LENGTH_SHORT).show();
                }
            }
        });

        gem.setSensorsListener(new OnSensorsAbstractListener() {
            @Override
            public void onSensorsChanged(GemSensorsData data) {
                //data.quaternion: float[4] {w, x, y, z}
                cubeTarget.setOrientation(data.quaternion);
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

        return gem;
    }
}