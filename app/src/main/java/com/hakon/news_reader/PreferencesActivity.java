package com.hakon.news_reader;

import android.Manifest;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PreferencesActivity extends AppCompatActivity {

    /* UI elements */
    private EditText mEtTxtURL;
    private EditText mEtTxtArticlesAmount;
    private EditText mEtTxtUpdateRate;
    private Button mBtnApply;
    private Button mBtnCancel;


    /* Private constants */
    private static final String TAG = "PreferencesActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        this.initViews();

        final SharedPreferences preferences = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final SharedPreferences.Editor preferencesEditor = preferences.edit();

        // TODO: default values must also be set from mainactivity
        String oldURL = preferences.getString(
                MainActivity.PREFS_URL,
                MainActivity.DEFUALT_URL
        );

        final Integer oldArticlesAmount = preferences.getInt(
                MainActivity.PREFS_AMOUNT_OF_ARTICLES,
                MainActivity.DEFAULT_ARTICLES_AMOUNT
        );

        final Integer oldUpdateRate = preferences.getInt(
                MainActivity.PREFS_UPDATE_RATE,
                MainActivity.DEFAULT_UPDATE_RATE
        );


        Log.d(TAG, String.format("%s, %d, %d", oldURL, oldArticlesAmount, oldUpdateRate));
        /* Not sure yet if the old values are needed for anything else. If not, this is sufficient
        mEtTxtArticlesAmount.setText(preferences.getInt(
                "articlesAmount",
                DEFAULT_ARTICLES_AMOUNT
        ));
        */
        mEtTxtURL.setText(oldURL);
        mEtTxtArticlesAmount.setText(oldArticlesAmount.toString());
        mEtTxtUpdateRate.setText(oldUpdateRate.toString());


        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: These values might cause errors if they're empty etc. Might need to check
                // TODO: Validate that the url is valid rss/atom (idk how this is done tho so google:))

                Integer amountValue;
                try {
                    amountValue = Integer.parseInt(mEtTxtArticlesAmount.getText().toString());
                } catch(NumberFormatException e) {
                    Log.e(TAG, "onClick: Error parsing amount of articles");

                    amountValue = oldArticlesAmount; // If parsing fails, we set the old value
                }

                Integer updateValue;
                try {
                    updateValue = Integer.parseInt(mEtTxtUpdateRate.getText().toString());
                } catch(NumberFormatException e) {
                    Log.e(TAG, "onClick: Error parsing update amount");

                    updateValue = oldUpdateRate;
                }


                preferencesEditor.putString(
                        "url",
                        mEtTxtURL.getText().toString()
                );
                preferencesEditor.putInt(
                        "articlesAmount",
                        amountValue
                );
                preferencesEditor.putInt(
                        "updateRate",
                        updateValue
                );

                // TODO: Find out difference on apply() and commit()
                preferencesEditor.apply();

                finish();
            }
        });

        // Discard all changes and return to main activity
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    /**
     * Initializes all UI elements
     */
    private void initViews() {
        mEtTxtURL = findViewById(R.id.et_URL);
        mEtTxtArticlesAmount = findViewById(R.id.et_articlesAmount);
        mEtTxtUpdateRate = findViewById(R.id.et_updateRate);

        mBtnApply = findViewById(R.id.btn_apply);
        mBtnCancel = findViewById(R.id.btn_cancel);
    }
}
