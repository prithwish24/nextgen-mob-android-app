package com.abc.product.app.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.abc.R;
import com.abc.product.app.adapter.ChatAdapter;
import com.abc.product.app.ai.Config;
import com.abc.product.app.model.ChatMessage;
import com.abc.product.app.util.GPSTracker;
import com.abc.product.app.util.SessionManager;
import com.abc.product.app.util.TTS;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIListener;
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

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AIButton.AIButtonListener {
    public static final String TAG = HomeActivity.class.getName();
    public static final String START_SPEECH = "Hi! how may I help you ?";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private Gson gson = GsonFactory.getGson();

    private AIButton aiButton;
    private Address curentLocationAddress;
    private TextView resultTextView;
    private TextView partialResultsTextView;
    private final Handler handler;
    private ArrayList<ChatMessage> chatHistory;
    private ListView messagesContainer;
    private ChatAdapter adapter;

    public HomeActivity() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager.checkLogin();

        messagesContainer = (ListView) findViewById(R.id.messagesContainer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        showUserDetailsOnDrawer(this);

        requestAppPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_CONTACTS
                },
                R.string.permission_rationale_text, this.getTaskId());

        resolveCurrentGPSLocation();
        adapter = new ChatAdapter(HomeActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        Toast.makeText(this, "Permissions Received.", Toast.LENGTH_LONG).show();

        aiButton = findViewById(R.id.micButton);
        resultTextView = findViewById(R.id.resultTextView);
        partialResultsTextView = findViewById(R.id.partialResultsTextView);

        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String accessToken = defaultSharedPreferences.getString("dialogflow_agent_token", "");
        //Toast.makeText(this, key, Toast.LENGTH_SHORT).show();

        AIConfiguration config = null;
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            config = new AIConfiguration(
                    Config.ACCESS_TOKEN,
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);
        } else {
            config = new AIConfiguration(
                    Config.ACCESS_TOKEN,
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);
        }
        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));

        aiButton.initialize(config);
        aiButton.setResultsListener(this);

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

        aiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(true);

                messageET.setText("");

                displayMessage(chatMessage);
            }
        });


        /*final AIContext aiContext = new AIContext("CarRental");
        final Map<String, String> maps = new HashMap<>(1);
        maps.put("zipcode", curentLocationAddress.getPostalCode());
        aiContext.setParameters(maps);
        final List<AIContext> contexts = Collections.singletonList(aiContext);
        final RequestExtras requestExtras = new RequestExtras(contexts, null);
        aiButton.startListening(requestExtras);*/
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    private void resolveCurrentGPSLocation() {
        GPSTracker gpsTracker = new GPSTracker(this);
        final Location location = gpsTracker.getLocation();
        if (gpsTracker.isGPSServiceOn() && (location != null) ) {
            this.curentLocationAddress = gpsTracker.getAddress(location);
        } else {
            gpsTracker.tryEnablingGPS(); // response will be displayed on onActivityResult method
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case GPSTracker.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e("Settings", "Result OK");
                        //updateGPSStatus("GPS is Enabled in your device");
                        resolveCurrentGPSLocation();
                        break;
                    case RESULT_CANCELED:
                        Log.e("Settings", "Result Cancel");
                        //updateGPSStatus("GPS is Disabled in your device");
                        Toast.makeText(this, "GPS is Disabled in your device.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        sessionManager.checkLogin();

        return actionOnDrawerItems(item);
    }

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "onResult");
                Log.i(TAG, "Received success response");

                //resultTextView.setText(gson.toJson(response));

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();
                resultTextView.setText(speech);
                Log.i(TAG, "Speech: " + speech);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(speech);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(false);

                displayMessage(chatMessage);
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

