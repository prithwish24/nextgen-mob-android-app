package com.abc.product.app.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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

    private SessionManager sessionManager;
    private Gson gson = GsonFactory.getGson();

    private AIButton aiButton;
    //private AIDialog aiDialog;
    private TextView resultTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager.checkLogin();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final View headerView = navigationView.getHeaderView(0);
        final TextView navPaneUserFullName = headerView.findViewById(R.id.userFullName);
        final TextView navPaneUserEmail = headerView.findViewById(R.id.userEmail);
        navPaneUserFullName.setText(sessionManager.getData(SessionManager.KEY_NAME));
        navPaneUserEmail.setText(sessionManager.getData(SessionManager.KEY_NAME));


        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        final Location location = gpsTracker.getLocation();
        final Address address = gpsTracker.getAddress(location.getLatitude(), location.getLongitude());


        aiButton = findViewById(R.id.micButton);
        resultTextView = findViewById(R.id.resultTextView);

        final AIConfiguration config = new AIConfiguration(
                Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));


        //aiButton.initialize(config);
        //aiButton.setResultsListener(this);
        if (address != null) {
            final AIContext aiContext = new AIContext("CarRental");
            final Map<String, String> maps = new HashMap<>(1);
            maps.put("address", address.getPostalCode());
            aiContext.setParameters(maps);
            aiContext.setLifespan(2);
            final List<AIContext> contexts = Collections.singletonList(aiContext);
            final RequestExtras requestExtras = new RequestExtras(contexts, null);
            //final AIService aiService = aiButton.getAIService();
            //aiService.startListening(requestExtras);
            aiButton.initialize(config);
            //aiButton.setResultsListener(this);
            aiButton.startListening(requestExtras);

        } else {
            aiButton.initialize(config);
            aiButton.setResultsListener(this);
        }


        //aiDialog = new AIDialog(this, config);
        //aiDialog.setResultsListener(this);

        TTS.speak("Hi! How may I help you ?");

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        sessionManager.checkLogin();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            sessionManager.logoutUser();
        } else if (id == R.id.nav_change_passwd) {
            Toast.makeText(this, "Nothing assigned", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_list_res) {
            Toast.makeText(this, "Nothing assigned", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_update_profile) {
            Toast.makeText(this, "Nothing assigned", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onResult");
                Log.i(TAG, "Received success response");

                final TextView resulttext = findViewById(R.id.resultTextView);
                resulttext.setText(gson.toJson(response));

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
    protected void onPause() {
        super.onPause();
        aiButton.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aiButton.resume();
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

