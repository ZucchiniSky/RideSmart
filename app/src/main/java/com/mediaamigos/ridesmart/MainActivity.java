package com.mediaamigos.ridesmart;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

import java.lang.Override;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private TextView isActivated;
    private TextView lastChecked;
    private CheckBox silencePhone;
    private CheckBox sendAutomatedResponse;
    private EditText automatedResponseText;

    private class SharedPrefsCheckBoxListener implements CompoundButton.OnCheckedChangeListener {

        private String sharedPrefsString;

        public SharedPrefsCheckBoxListener(String prefString) {
            sharedPrefsString = prefString;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            SharedPreferences sharedPrefs = Utility.getSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(sharedPrefsString, b);
            editor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "launching RideSmart MainActivity");

        Utility.serviceConnector.startServiceAndBind(this);

        setContentView(R.layout.activity_main);

        isActivated = (TextView) findViewById(R.id.textIsActivated);
        lastChecked = (TextView) findViewById(R.id.textLastChecked);
        silencePhone = (CheckBox) findViewById(R.id.checkBoxSilencePhone);
        sendAutomatedResponse = (CheckBox) findViewById(R.id.checkBoxAutoResponse);
        automatedResponseText = (EditText) findViewById(R.id.editTextAutoResponse);
        Button saveAutomatedResponse = (Button) findViewById(R.id.buttonAutoResponse);
        Button forceSilent = (Button) findViewById(R.id.buttonForceSilent);
        Button forceUnsilent = (Button) findViewById(R.id.buttonForceUnsilent);

        fillData();

        silencePhone.setOnCheckedChangeListener(new SharedPrefsCheckBoxListener(Utility.ENABLED));
        sendAutomatedResponse.setOnCheckedChangeListener(new SharedPrefsCheckBoxListener(Utility.TEXT_RESPONSE_ENABLED));

        saveAutomatedResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = Utility.getSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Utility.TEXT_RESPONSE_BODY, automatedResponseText.getText().toString());
                editor.apply();
            }
        });

        forceSilent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("forcing silent mode", TAG);
                Utility.onActivityCheck(true, getApplicationContext());
            }
        });

        forceUnsilent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("forcing unsilent mode", TAG);
                Utility.onActivityCheck(false, getApplicationContext());
            }
        });

        if (Utility.serviceConnector.isServiceBound && Utility.serviceConnector.rideSmartService.getSubscription() == null) {
            Log.d(TAG, "launching RideSmart but no detect activity subscription");
            OnPhoneBoot.initializeRideSmart(getApplicationContext());
        }

        Utility.setActivityDetectionListener(new Utility.ActivityDetectionListener() {
            @Override
            public void onActivityDetected() {
                fillData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Utility.setActivityDetectionListener(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void fillData() {
        SharedPreferences sharedPrefs = Utility.getSharedPreferences(getApplicationContext());
        isActivated.setText(sharedPrefs.getBoolean(Utility.SILENCING, false) ? "silent mode on" : "silent mode off");
        lastChecked.setText("Last checked: " + DateUtils.getRelativeTimeSpanString(getApplicationContext(), sharedPrefs.getLong(Utility.LAST_CHECKED, 0)));
        silencePhone.setChecked(sharedPrefs.getBoolean(Utility.ENABLED, false));
        sendAutomatedResponse.setChecked(sharedPrefs.getBoolean(Utility.TEXT_RESPONSE_ENABLED, false));
        automatedResponseText.setText(sharedPrefs.getString(Utility.TEXT_RESPONSE_BODY, Utility.TEXT_RESPONSE_BODY_DEFAULT));
    }

    public void openWhiteList(View view) {
        Intent intent = new Intent(this, WhiteListActivity.class);
        startActivity(intent);
    }
}
