package com.example.mycs.totalkomp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class SplashPermissionActivity extends AppCompatActivity {

    private int timeoutMillis = 0;
    private long startTimeMillis = 0;
    private static final int PERMISSION_REQUEST = 123;
    private static final Random random = new Random();

    private TextView textView = null;
    private static final int textViewID = View.generateViewId();

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    @SuppressWarnings("rawtypes")
    public Class getNextActivityClass() {
        return MainActivity.class;
    }

    public String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (permissions == null)
            return new String[0];
        else
            return permissions.clone();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView = new TextView(this);
        textView.setTextSize(24);
        textView.setId(textViewID);
        textView.setText(R.string.permissionRequirementExplanation);
        mainLayout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        int off = 128;
        int rest = 256 - off;
        int color = Color.argb(255, off + random.nextInt(rest), off + random.nextInt(rest), off + random.nextInt(rest));
        mainLayout.setBackgroundColor(color);

        setContentView(mainLayout);

        startTimeMillis = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= 23)
            checkPermissions();
        else
            startNextActivity();

        //setContentView(R.layout.activity_splash_permission);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST)
            checkPermissions();
    }

    private void startNextActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("Permission granted...");
            }
        });

        long delayMillis = getTimeoutMillis() - (System.currentTimeMillis() - startTimeMillis);

        if (delayMillis < 0)
            delayMillis = 0;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashPermissionActivity.this, getNextActivityClass()));
                finish();
            }
        }, delayMillis);
    }

    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();

        if (ungrantedPermissions.length == 0)
            startNextActivity();
        else
            requestPermissions(ungrantedPermissions, PERMISSION_REQUEST);
    }

    private String[] requiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();

        for (String permission : getRequiredPermissions()) {
            permissions.add(permission);
        }

        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();

            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(SplashPermissionActivity.class.getSimpleName(),"Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(SplashPermissionActivity.class.getSimpleName(), "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }
}