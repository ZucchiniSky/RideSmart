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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nghiavo on 12/6/15.
 */
public class WhiteListActivity extends Activity {
    private ArrayAdapter<String> mArrayAdapter;
    private ListView mListView;
    private EditText mEditText;
    private Set<String> mStringSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whitelist_main);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        mEditText = (EditText) findViewById(R.id.editWhitelistText);
        mListView = (ListView) findViewById(R.id.WhitelistView);

        Button saveWhitelist = (Button) parent.findViewById(R.id.saveWhiteList);

        SharedPreferences sharedPrefs = Utility.getSharedPreferences(this);
        mStringSet = sharedPrefs.getStringSet(Utility.WHITELIST, new HashSet<String>());

        if (!sharedPrefs.contains(Utility.WHITELIST)) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putStringSet(Utility.WHITELIST, mStringSet);
            editor.apply();
        }

        final List<String> arrayList = new ArrayList<String>(mStringSet);

        mArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                arrayList
        );

        mListView.setAdapter(mArrayAdapter);

        saveWhitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mEditText.getText().toString();

                if (mStringSet.contains(phoneNumber)) return;
                mStringSet.add(phoneNumber);

                mArrayAdapter.add(phoneNumber);
                mArrayAdapter.notifyDataSetChanged();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(WhiteListActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + position);
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mStringSet.remove(arrayList.get(position));

                        arrayList.remove(position);
                        mArrayAdapter.notifyDataSetChanged();
                    }
                });
                adb.show();
            }
        });
        return super.onCreateView(parent, name, context, attrs);
    }
}
