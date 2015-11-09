package com.mediaamigos.ridesmart;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity {

    private Context appContext;
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
            SharedPreferences sharedPrefs = Utility.getSharedPreferences(appContext);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(sharedPrefsString, b);
            editor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        isActivated = (TextView) parent.findViewById(R.id.textIsActivated);
        lastChecked = (TextView) parent.findViewById(R.id.textLastChecked);
        silencePhone = (CheckBox) parent.findViewById(R.id.checkBoxSilencePhone);
        sendAutomatedResponse = (CheckBox) parent.findViewById(R.id.checkBoxAutoResponse);
        automatedResponseText = (EditText) parent.findViewById(R.id.editTextAutoResponse);
        Button saveAutomatedResponse = (Button) parent.findViewById(R.id.buttonAutoResponse);
        appContext = context;

        fillData();

        silencePhone.setOnCheckedChangeListener(new SharedPrefsCheckBoxListener(Utility.ENABLED));
        sendAutomatedResponse.setOnCheckedChangeListener(new SharedPrefsCheckBoxListener(Utility.TEXT_RESPONSE_ENABLED));

        saveAutomatedResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = Utility.getSharedPreferences(appContext);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Utility.TEXT_RESPONSE_BODY, automatedResponseText.getText().toString());
                editor.apply();
            }
        });

        if (OnPhoneBoot.detectedActivitySubscription == null) {
            OnPhoneBoot.initializeRideSmart(appContext);
        }

        OnPhoneBoot.setActivityDetectionListener(new OnPhoneBoot.ActivityDetectionListener() {
            @Override
            public void onActivityDetected() {
                fillData();
            }
        });

        return super.onCreateView(parent, name, context, attrs);
    }

    public void fillData() {
        SharedPreferences sharedPrefs = Utility.getSharedPreferences(appContext);
        isActivated.setText(sharedPrefs.getBoolean(Utility.SILENCING, false) ? "silent mode on" : "silent mode off");
        lastChecked.setText("Last checked: " + DateUtils.getRelativeTimeSpanString(appContext, sharedPrefs.getLong(Utility.LAST_CHECKED, 0)));
        silencePhone.setChecked(sharedPrefs.getBoolean(Utility.ENABLED, false));
        sendAutomatedResponse.setChecked(sharedPrefs.getBoolean(Utility.TEXT_RESPONSE_ENABLED, false));
        automatedResponseText.setText(sharedPrefs.getString(Utility.TEXT_RESPONSE_BODY, Utility.TEXT_RESPONSE_BODY_DEFAULT));
    }
}
