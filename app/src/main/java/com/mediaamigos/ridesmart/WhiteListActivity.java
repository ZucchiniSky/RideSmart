package com.mediaamigos.ridesmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nghiavo on 12/6/15.
 */
public class WhiteListActivity extends Activity {
    private ArrayAdapter<String> mArrayAdapter;
    private EditText mEditText;
    private HashMap<String, Integer> mPhoneNumbers;
    // NOTE: this increases, despite deletions--could get nasty!!!
    private int mWhiteListCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.whitelist_main);

        mEditText = (EditText) findViewById(R.id.editWhiteListText);
        ListView mListView = (ListView) findViewById(R.id.whiteListView);

        final List<String> phoneNumberArray = new ArrayList<String>();
        mPhoneNumbers = new HashMap<String, Integer>();

        final SharedPreferences sharedPrefs = Utility.getSharedPreferences(this);
        mWhiteListCount = sharedPrefs.getInt(Utility.WHITELIST_COUNT, 0);

        if (!sharedPrefs.contains(Utility.WHITELIST_COUNT)) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(Utility.WHITELIST_COUNT, 0);
            editor.apply();
        }

        for (int i = 0; i < mWhiteListCount; i++) {
            String phoneNumber = sharedPrefs.getString(Utility.WHITELIST_ENTRY + i, "");
            if (phoneNumber.isEmpty()) continue;

            mPhoneNumbers.put(phoneNumber, i);
            phoneNumberArray.add(phoneNumber);
        }

        Button saveWhitelist = (Button) findViewById(R.id.saveWhiteList);
        saveWhitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mEditText.getText().toString();

                if (mPhoneNumbers.containsKey(phoneNumber)) return;
                mPhoneNumbers.put(phoneNumber, mWhiteListCount);

                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Utility.WHITELIST_ENTRY + mWhiteListCount++, phoneNumber);
                editor.putInt(Utility.WHITELIST_COUNT, mWhiteListCount);
                editor.apply();

                mArrayAdapter.add(phoneNumber);
                mArrayAdapter.notifyDataSetChanged();
            }
        });

        mArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                phoneNumberArray
        );

        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(WhiteListActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + phoneNumberArray.get(position));
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.remove(Utility.WHITELIST_ENTRY + mPhoneNumbers.get(phoneNumberArray.get(position)));
                        editor.apply();

                        mPhoneNumbers.remove(phoneNumberArray.get(position));

                        phoneNumberArray.remove(position);
                        mArrayAdapter.notifyDataSetChanged();
                    }
                });
                adb.show();
            }
        });
    }
}
