/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abc.product.app.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abc.R;
import com.abc.product.app.ai.AIApplication;
import com.abc.product.app.util.SessionManager;
import com.abc.product.app.util.TTS;

import ai.api.AIServiceException;
import ai.api.android.AIDataService;


public abstract class BaseActivity extends AppCompatActivity {

    private AIApplication app;

    private static final long PAUSE_CALLBACK_DELAY = 500;
    private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;

    private final Handler handler = new Handler();
    private Runnable pauseCallback = new Runnable() {
        @Override
        public void run() {
            app.onActivityPaused();
        }
    };

    protected SessionManager sessionManager;
    private SparseIntArray mErrorString;
    protected TTS tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorString = new SparseIntArray();

        sessionManager = new SessionManager(getApplicationContext());

        app = (AIApplication) getApplication();
        tts = new TTS();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //checkAudioRecordPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.postDelayed(pauseCallback, PAUSE_CALLBACK_DELAY);
    }

    protected void showUserDetailsOnDrawer(final NavigationView.OnNavigationItemSelectedListener navView) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(navView);

        final View headerView = navigationView.getHeaderView(0);
        final TextView navPaneUserFullName = headerView.findViewById(R.id.userFullName);
        final TextView navPaneUserEmail = headerView.findViewById(R.id.userEmail);
        navPaneUserFullName.setText(sessionManager.getData(SessionManager.KEY_NAME));
        navPaneUserEmail.setText(sessionManager.getData(SessionManager.KEY_EMAIL));

        final ImageView profileImgView = headerView.findViewById(R.id.profileImageView);
        profileImgView.setImageResource(R.mipmap.ic_profile);

    }

    protected boolean actionOnDrawerItems(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            sessionManager.logoutUser();


        } else if (id == R.id.nav_home) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();

        } else if (id == R.id.nav_list_res) {
            startActivity(new Intent(this, ShowResActivity.class));
            finish();

        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();

        } else if (id == R.id.nav_show_profile) {
            Toast.makeText(this, "Nothing assigned", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int permission : grantResults) {
            permissionCheck = permissionCheck + permission;
        }
        if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                onPermissionsGranted(requestCode);
            } catch (AIServiceException e) {
                e.printStackTrace();
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), mErrorString.get(requestCode),
                    Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivity(intent);
                        }
                    }).show();
        }
    }

    public void requestAppPermissions(final String[] requestedPermissions,
                                      final int stringId, final int requestCode) {
        mErrorString.put(requestCode, stringId);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : requestedPermissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                Snackbar.make(findViewById(android.R.id.content), stringId,
                        Snackbar.LENGTH_INDEFINITE).setAction("GRANT",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(BaseActivity.this, requestedPermissions, requestCode);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        } else {
            try {
                onPermissionsGranted(requestCode);
            } catch (AIServiceException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onPermissionsGranted(int requestCode) throws AIServiceException;

}
