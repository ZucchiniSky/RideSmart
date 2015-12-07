package com.mediaamigos.ridesmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nghiavo on 12/6/15.
 */
public class WhiteListActivity extends Activity {
    private Context appContext;
    private ArrayAdapter<String> arrayAdapter;
    private ListView whitelistView;
    private EditText display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whitelist_main);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        display = (EditText) findViewById(R.id.editWhitelistText);
        whitelistView = (ListView) findViewById(R.id.WhitelistView);

        Button saveWhitelist = (Button) parent.findViewById(R.id.saveWhiteList);
        appContext = context;

        final List<String> arrayList = new ArrayList<String>();

        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                arrayList
        );

        whitelistView.setAdapter(arrayAdapter);

        saveWhitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = Utility.getSharedPreferences(appContext);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                String task = display.getText().toString();

                arrayAdapter.add(task);
                arrayAdapter.notifyDataSetChanged();
                SavePreferences("WHITELIST", task);
            }
        });

        whitelistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(WhiteListActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + position);
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        arrayList.remove(positionToRemove);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
                adb.show();
            }
        });
        return super.onCreateView(parent, name, context, attrs);
    }

    protected void SavePreferences(String key, String value) {
        // TODO Auto-generated method stub
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = data.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected void LoadPreferences(){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(this);
        String dataSet = data.getString("WHITELIST", "The list is empty");

        arrayAdapter.add(dataSet);
        arrayAdapter.notifyDataSetChanged();
    }
}
