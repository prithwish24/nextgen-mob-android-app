package com.abc.product.app.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abc.R;
import com.abc.product.app.ai.Config;
import com.abc.product.app.util.GPSTracker;
import com.abc.product.app.util.SessionManager;
import com.abc.product.app.util.TTS;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.PartialResultsListener;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.ui.AIButton;
//import ai.api.ui.AIDialog;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AIButton.AIButtonListener {
    public static final String TAG = HomeActivity.class.getName();
    public static final String START_SPEECH = "Hi! how may I help you ?";


    private Gson gson = GsonFactory.getGson();

    private AIButton aiButton;
    //private AIDialog aiDialog;
    private TextView resultTextView;
    private TextView partialResultsTextView;
    private final Handler handler;

    public HomeActivity() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager.checkLogin();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        showUserDetailsOnDrawer(this);

        requestAppPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_CONTACTS
                },
                R.string.permission_rationale_text, 20);

    }

    /*private void showSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                HomeActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        HomeActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }*/

    @Override
    public void onPermissionsGranted(int requestCode) {
        Toast.makeText(this, "Permissions Received.", Toast.LENGTH_LONG).show();

        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        final Location location = gpsTracker.getLocation();
        final Address address = gpsTracker.getAddress(location.getLatitude(), location.getLongitude());

        aiButton = findViewById(R.id.micButton);
        resultTextView = findViewById(R.id.resultTextView);
        partialResultsTextView = findViewById(R.id.partialResultsTextView);

        final AIConfiguration config = new AIConfiguration(
                Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));

        aiButton.initialize(config);
        aiButton.setResultsListener(this);

        if (address != null) {
            final AIContext aiContext = new AIContext("CarRental");
            final Map<String, String> maps = new HashMap<>(1);
            maps.put("address", address.getPostalCode());
            aiContext.setParameters(maps);
            //aiContext.setLifespan(2);
            final List<AIContext> contexts = Collections.singletonList(aiContext);
            final RequestExtras requestExtras = new RequestExtras(contexts, null);
            //final AIService aiService = aiButton.getAIService();
            //aiService.startListening(requestExtras);

            //aiButton.setResultsListener(this);
            aiButton.startListening(requestExtras);
        }

        //aiDialog = new AIDialog(this, config);
        //aiDialog.setResultsListener(this);

        TTS.speak(START_SPEECH);
        resultTextView.setText(START_SPEECH);

        aiButton.setPartialResultsListener(new PartialResultsListener() {
            @Override
            public void onPartialResults(List<String> partialResults) {
                final String result = partialResults.get(0);
                if (!TextUtils.isEmpty(result)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (partialResultsTextView != null) {
                                partialResultsTextView.setText(result);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        //menu.getItem(R.id.nav_home).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        sessionManager.checkLogin();

        return super.actionOnDrawerItems(item);
    }

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "onResult");
                Log.i(TAG, "Received success response");

                resultTextView.setText(gson.toJson(response));

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                TTS.speak(speech);
            }
        });
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();

                resultTextView.setText(error.getMessage());
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                Toast.makeText(getApplicationContext(), "Action Cancelled", Toast.LENGTH_SHORT).show();
                resultTextView.setText("Action Cancelled !");
            }
        });
    }


}

