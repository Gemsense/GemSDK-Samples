package com.gemsense.basicdemo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gemsense.common.AnalogInputData;
import com.gemsense.common.DigitalInputData;
import com.gemsense.gemsdk.ExtensionsAbstractListener;
import com.gemsense.gemsdk.Gem;
import com.gemsense.gemsdk.GemAbstractListener;
import com.gemsense.gemsdk.GemManager;
import com.gemsense.gemsdk.GemSDKUtilityApp;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //Gems instance
    private Gem gem;
    private boolean stateD0 = false;

    //GUI
    private Button toggleD0_Btn;
    private Button readD1_Btn;
    private TextView valD1_Text;
    private Button readA0_Btn;
    private TextView valA0_Text;
    private SeekBar pwmBar;
    private TextView pwmVal_Text;
    private Button disablePwm_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //GUI
        toggleD0_Btn = (Button)findViewById(R.id.toggle_d0);
        readD1_Btn = (Button)findViewById(R.id.read_d1);
        valD1_Text = (TextView)findViewById(R.id.val_d1);
        readA0_Btn = (Button)findViewById(R.id.read_a0);
        valA0_Text = (TextView)findViewById(R.id.val_a0);
        pwmBar = (SeekBar)findViewById(R.id.pwm_bar);
        pwmVal_Text = (TextView)findViewById(R.id.pwm_val);
        disablePwm_Btn = (Button)findViewById(R.id.disable_pwm);

        initGuiCallbacks();
    }


    private void initGuiCallbacks() {
        //Toggle D0 button
        toggleD0_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gem != null) {
                    stateD0 = !stateD0;
                    gem.writeDigital(Gem.EXTENSION_DIGITAL_0, stateD0);
                }
            }
        });

        //Read D1 button
        readD1_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gem != null) {
                    gem.readDigital(Gem.EXTENSION_DIGITAL_1);
                }
            }
        });

        //Read A0 button
        readA0_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gem != null) {
                    gem.readAnalog(Gem.EXTENSION_ANALOG_0);
                }
            }
        });

        //Pwm
        pwmBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int val, boolean b) {
                pwmVal_Text.setText(String.format(Locale.US, "%.2f V", val / 100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Set the value hear to prevent to many calls
                if(gem != null) {
                    gem.writePwm(seekBar.getProgress() / 100f);
                }
            }
        });

        disablePwm_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gem != null) {
                    gem.disablePwm();
                }
            }
        });
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
    protected void onPause() {
        super.onPause();
        //Unbind gem service from the application
        GemManager.getDefault().unbindService(this);
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

            //It's possible to do it in OnCreate() if white list (or address itself)
            //is not supposed to be changed when the app is in background
            gem = GemManager.getDefault().getGem(whitelist[0], gemListener);
            gem.setExtensionsListener(extensionsListener);
        }
    }

    //Hardware configuration
    private void configureExtensions() {
        gem.configDigital(Gem.EXTENSION_DIGITAL_0, Gem.EXTENSION_CONF_OUTPUT);
        gem.configDigital(Gem.EXTENSION_DIGITAL_1, Gem.EXTENSION_CONF_INPUT);
    }

    //Gem main callback
    private GemAbstractListener gemListener = new GemAbstractListener() {
        @Override
        public void onStateChanged(int state) {
            if(state == Gem.STATE_CONNECTED) {
                //Hardware configuration. Needs to be called on each connection
                configureExtensions();
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
    };

    //Gem callback for responses from extensions
    private ExtensionsAbstractListener extensionsListener = new ExtensionsAbstractListener() {
        @Override
        public void onDigitalRead(DigitalInputData data) {
            if(data.id == Gem.EXTENSION_DIGITAL_1) {
                valD1_Text.setText(data.value ? "High" : "Low");
            }
        }

        @Override
        public void onAnalogRead(AnalogInputData data) {
            if(data.id == Gem.EXTENSION_ANALOG_0) {
                valA0_Text.setText(String.format(Locale.US, "%.2fV", data.value));
            }
        }
    };

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